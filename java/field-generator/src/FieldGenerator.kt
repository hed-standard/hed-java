import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.embed.swing.JFXPanel
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import org.json.simple.JSONArray
import tornadofx.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.ArrayList
import java.util.LinkedHashMap


class FieldGenerator : App(MainView::class) {
    val controller: FieldGeneratorController by inject()
    var newFieldMap = LinkedHashMap<String, LinkedHashMap<String,ArrayList<String>>>()
    override fun start(stage: Stage) {
        if (parameters.unnamed.isEmpty()) {
            println("Please provide field map information")
            System.exit(-1) // ok to kill since it's a separate process with separate JVM so won't kill MATLAB JVM
        }
        else {
            val json = parameters.unnamed[0]
            controller.getEventValues(json)
            super.start(stage)
        }
    }

    override fun stop() {
        newFieldMap = controller.newValuesFieldMaps.valuesFieldMap

        val obj = JSONObject()
        for (field in newFieldMap.keys) { // for each new field value
            // there's associated field-value object LinkedHashMap
            val associatedObj = JSONObject()
            val map: LinkedHashMap<String, ArrayList<String>>? = newFieldMap[field]
            if (map != null) {
                for (associatedField in map.keys) {
                    val list = JSONArray()
                    val arrayList = map[associatedField] as ArrayList<String>
                    for (value in arrayList) {
                        list.add(value)
                    }

                    associatedObj.put(associatedField, list)
                }
            }
            obj.put(field, associatedObj)
        }
        println(obj.toJSONString())
    }

}

class MainView: View() {
    val controller: FieldGeneratorController by inject() // value of controller properties was updated in start() of App

    var valueNameTF : TextField by singleAssign()
    var fieldCB : ComboBox<String> by singleAssign()
    var valueListView : ListView<String> by singleAssign()
    var mainTabPane : TabPane by singleAssign()

    override val root = vbox {
        borderpane {
            center {
                label("Create new field \"${controller.newFieldName}\"") {
                    font = Font.font("Cambria", 20.0)
                }
            }
        }
        mainTabPane = tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab("Add value") {
                vbox {
                    padding = Insets(10.0,10.0,10.0,10.0)
                    hbox {
                        label("New value name: ")
                        valueNameTF = textfield { }
                    }

                    vbox {
                        label("Associated event values") {
                            padding = Insets(20.0,0.0,10.0,0.0)
                        }
                        hbox {
                            label("EEG.event field ") {
                            }
                            fieldCB = combobox<String> {
                                items = FXCollections.observableArrayList(controller.initialfieldMap.keys)
                                selectionModel.selectedItemProperty().onChange {
                                    controller.newValueMapSelectFieldHandler()
                                }
                            }
                        }
                        valueListView = listview<String> {
                            selectionModel.selectionMode = SelectionMode.MULTIPLE
                            onMouseClicked = EventHandler{
                                controller.fieldValueSelectedHandler()
                            }
                        }
                    }
                    button("Add") {
                        padding = Insets(10.0,10.0,10.0,10.0)
                        action {
                            controller.addNewValue()
                        }
                    }
                }
            }
            tab("Current values") {
                var fields = FXCollections.observableArrayList<String>()
                var eventValues =  FXCollections.observableArrayList<String>()
                var selectedValue = ""
                gridpane {
                    padding = Insets(10.0,10.0,10.0,10.0)
                    row {
                        /* New event value panel */
                        label("New event values:")
                        label("Associated event values")
                    }
                    row {
                        label()
                        label("EEG.event field: ") {
                        }
                        combobox<String> {
                            items = fields
                            selectionModel.selectedItemProperty().onChange {
                                if (selectionModel.selectedItem != null) {
//                                    println("fieldMap: " + controller.newValuesFieldMaps.getFieldMap(selectedValue))
                                    if (!selectedValue.isEmpty()) {
                                        val fieldMap = controller.newValuesFieldMaps.getFieldMap(selectedValue)
                                        if (fieldMap != null) {
                                            eventValues.setAll(fieldMap[selectionModel.selectedItem.toString()])
                                        }
                                    }
                                }
                            }
                        }
                    }
                    row {
                        listview(controller.newValuesFieldMaps.valuesList) {
                            onMouseClicked = EventHandler {
                                if (selectionModel.selectedItem != null) { // when an item is selected
                                    selectedValue = selectionModel.selectedItem.toString()
                                    val fieldMap = controller.newValuesFieldMaps.getFieldMap(selectedValue)
                                    if (fieldMap != null) {
                                        fields.setAll(fieldMap.keys)
                                        eventValues.clear()
                                    }
                                }
                            }
                        }
                        listview(eventValues) {
                            gridpaneConstraints {
                                columnSpan = 2 // to match with two columns above
                            }
                            selectionModel.selectionMode = SelectionMode.SINGLE
                        }
                    }
                    row {
                        button("Remove") {
                            action {
                                controller.removeValue(selectedValue)
                            }
                        }
                        button("Done") {
                            gridpaneConstraints {
                                columnSpan = 2
                            }
                            action {
                                primaryStage.hide()
                            }
                        }
                    }
                }
            }
        }
    }


    init {
        title = "Create new field"
    }
}


class FieldGeneratorController : Controller() {
    var newFieldName: String? = null
    val initialfieldMap = mutableMapOf<String, Any?>()

    val mainView: MainView by inject()

    val newValuesFieldMaps = NewValuesFieldMap()
    val tmpValueMap = LinkedHashMap<String, ArrayList<String>>()

    fun getValuesFromSelectedField(selectedField: String): ArrayList<String> {
        val values = initialfieldMap[selectedField]
        var returningList = ArrayList<String>()
        if (values is String)
            returningList.add(values)
        else
            returningList = values as ArrayList<String>

//            for (item in values as Arra) {
//                if (item is String)
//                    returningList.add(item)
//                else
//                    returningList.add(item.toString())
//            }
        return returningList
    }

    fun getEventValues(eventFieldJson: String) {
        val parser = JSONParser()
        val json = parser.parse(eventFieldJson)
        if (json is JSONObject) {
            newFieldName = json.get("newFieldName").toString()
            val eventFields = json.get("eventFields")
            if (eventFields is JSONObject) {
                val fieldNames = eventFields.keys
                for (field in fieldNames) {
                    initialfieldMap.put(field.toString(), eventFields[field])
                }
            }
        }
    }

    fun newValueMapSelectFieldHandler() {
        val selectedField = mainView.fieldCB.selectionModel.selectedItem

        if (selectedField != null) {
            // update valueListView
            mainView.valueListView.items = FXCollections.observableArrayList(getValuesFromSelectedField(selectedField))

            // highlight values of field if previously selected
            if (tmpValueMap.containsKey(selectedField)) {
                val selectedValues = tmpValueMap[selectedField]
                if (selectedValues != null) {
                    for (item in selectedValues) {
                        mainView.valueListView.selectionModel.select(item)
                    }
                }
            }
        }
    }

    fun fieldValueSelectedHandler() {
        if (!mainView.valueNameTF.text.isEmpty()) {
            val selectedItems = mainView.valueListView.selectionModel.selectedItems
            val listView = mainView.valueListView
            if (selectedItems != null && listView.items.size > 0) {
                val field = mainView.fieldCB.selectedItem.toString()

                if (selectedItems.size == 0 && tmpValueMap.containsKey(field)) {
                    // no value for this field, remove from map
                    tmpValueMap.remove(field)
                }
                else
                    tmpValueMap.put(field, ArrayList(selectedItems))

//                println(tmpValueMap)
            }
        }
    }
    fun addNewValue() {
        val newValueName = mainView.valueNameTF.text

        // validation
        if (newValueName == null || newValueName.isEmpty()) {
            alert(Alert.AlertType.ERROR, "Empty value name", "Please enter new value name", ButtonType.OK)
            return
        }
        if (tmpValueMap.isEmpty()) {
            alert(Alert.AlertType.ERROR, "Empty field map", "Please select associated fields and field values", ButtonType.OK)
            return

        }
        // add to value field map
        newValuesFieldMaps.addValue(newValueName, tmpValueMap)
        tmpValueMap.clear()

        // update UI
        mainView.valueNameTF.clear()
        mainView.fieldCB.value = null
        mainView.valueListView.items = FXCollections.observableArrayList()
        mainView.mainTabPane.selectionModel.selectNext()
    }

    fun removeValue(valueName : String) {
        newValuesFieldMaps.removeValue(valueName)
    }
}

class NewValuesFieldMap {
    val valuesFieldMap = LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>()
    val valuesList = FXCollections.observableArrayList<String>()
    fun addValue(valueName: String, fieldMap: LinkedHashMap<String, ArrayList<String>>) {
        // deep copy fieldMap
        val cloneMap = LinkedHashMap<String, ArrayList<String>>()
        fieldMap.forEach { field, values -> cloneMap.put(field, values.clone() as ArrayList<String>) } // deep copy

        // update
        valuesFieldMap.put(valueName, cloneMap)
        valuesList.setAll(valuesFieldMap.keys)
    }

    fun removeValue(valueName: String) {
        if (valuesFieldMap.containsKey(valueName)) {
            valuesFieldMap.remove(valueName)
        }
        if (valuesList.contains(valueName)) {
            valuesList.remove(valueName)
        }

    }

    fun getFieldMap(value: String) : LinkedHashMap<String, ArrayList<String>>?{
        return valuesFieldMap[value]
    }

    override fun toString() : String{
        return valuesFieldMap.toString()
    }
}

