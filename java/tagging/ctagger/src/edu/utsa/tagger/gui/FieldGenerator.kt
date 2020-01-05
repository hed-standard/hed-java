package edu.utsa.tagger.gui
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import tornadofx.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

object JSON

class FieldGenerator : App(MainView::class) {
    val controller: EventController by inject()
    var isSubmitted = false
    var newFieldMap = LinkedHashMap<String, ArrayList<String>>()
    override fun start(stage: Stage) {
        val eventJson = parameters.unnamed[0]
        controller.getEventValues(eventJson)
        super.start(stage)
    }

    override fun stop() {
        isSubmitted = true
        newFieldMap = controller.newFieldMap
        println(newFieldMap)
    }
}

class MainView: View() {
    val controller: EventController by inject() // value of controller properties was updated in start() of App

    var eventFields = controller.fields.keys
    var fieldListView : ListView<String> by singleAssign()

    var eventValues =  FXCollections.observableArrayList<String>()
    var selectedValues = listOf("")

    var newValues = FXCollections.observableArrayList<String>()
    var newValueListView : ListView<String> by singleAssign()
    override val root = GridPane()

    val eventFieldPane = vbox{
        label("Event fields:")
        val fields = FXCollections.observableArrayList(eventFields)
        fieldListView = listview(fields) {
            selectionModel.selectionMode = SelectionMode.MULTIPLE

            /* listener to update values list when fields are selected */
        }
        button("Get values from selected fields") {
            useMaxWidth = true
            action {
                // get values from all selected fields (fresh each time)
                val selectedFields = fieldListView.selectionModel.selectedItems
                val values = ArrayList<String>()
                for (field in selectedFields) {
                    if (field != null)
                        values.addAll(controller.getValuesFromSelectedField(field))
                }
                // update
                eventValues.setAll(values)
            }
        }
    }
    val eventValuePane = vbox{
        label("Event values:")
        listview(eventValues) {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            selectedValues = selectionModel.selectedItems
        }
        button{
            label("Generate new field value")
            useMaxWidth = true
            action {
                if (!selectedValues.isEmpty()) {
                    controller.selectedValues = ArrayList<String>(selectedValues)
                    openInternalWindow<NewValueWindow>()
                }
            }
        }
    }
    val newEventValuePane = vbox{
        label("New event values:")
        newValueListView = listview(newValues){
            selectionModel.selectionMode = SelectionMode.MULTIPLE
        }
        borderpane {
            left = button("Remove selected") {
                useMaxWidth = true
                action {
                    val selectedValues = ArrayList<String>(newValueListView.selectionModel.selectedItems)
                    for (value in selectedValues) {
                        controller.removeValueFromNewFieldMap(value)
                        newValues.remove(value)
                    }
                }
            }
            right = button("Done") {
                useMaxWidth = true
                action {
                    close()
                }
            }
        }
    }


    init {
        title = "Generate values for new field \"${controller.newFieldName}\""
        with(root) {
            eventFieldPane.gridpaneConstraints { columnRowIndex(0,0) }
            eventValuePane.gridpaneConstraints { columnRowIndex(1,0) }
            newEventValuePane.gridpaneConstraints { columnRowIndex(2, 0) }
        }
    }
}

class EventController : Controller() {
    val fields = mutableMapOf<String, Any?>()
    var newFieldName: String? = null
    var selectedValues = ArrayList<String>()
    var newFieldMap = LinkedHashMap<String, ArrayList<String>>()
    val mainView: MainView by inject()
    var isSubmitted = false

    fun getValuesFromSelectedField(selectedField: String): ArrayList<String> {
        val values = fields[selectedField] as ArrayList<String>
        val returningList = ArrayList<String>()
        for (item in values) {
            returningList.add(item)
        }
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
                    fields.put(field.toString(), eventFields[field])
                }
            }
        }
    }

    fun addValueToNewFieldMap(newValueName: String) {
        if (newValueName != null && !newValueName.isEmpty()) {
            newFieldMap.put(newValueName, selectedValues)
            mainView.newValues.setAll(newFieldMap.keys)
        }
    }

    fun removeValueFromNewFieldMap(value: String) {
        if (value != null && !value.isEmpty() && newFieldMap.containsKey(value))
            newFieldMap.remove(value)
    }
}

class NewValueWindow: Fragment() {
    val controller : EventController by inject()
    var newValueName: TextField by singleAssign()
    override val root = vbox {
        label("New value name")
        newValueName = textfield()
        button("Ok") {
            useMaxWidth = true
            action {
                controller.addValueToNewFieldMap(newValueName.text)
                close()
            }
        }
    }
}
