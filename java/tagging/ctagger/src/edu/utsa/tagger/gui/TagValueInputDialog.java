package edu.utsa.tagger.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.ConstraintLayout;
import edu.utsa.tagger.guisupport.FontsAndColors;
import edu.utsa.tagger.guisupport.MessageConstants;

/**
 * Dialog used to prompt for user's input when selecting a tag through EventEnterTagView represented by an TagEnterSearchView.
 *
 * @author Dung Truong
 */
public class TagValueInputDialog extends JDialog implements ActionListener,
        PropertyChangeListener{

    private JOptionPane bgPanel = new JOptionPane();
    private JTextField textField;
    private JComboBox unitComboBox = new JComboBox(new String[] {});
    String btnString1 = "Save"; // used to communicate between input and optionpane
    String btnString2 = "Cancel";
    JPanel panelUseFieldValue = new JPanel();
    JCheckBox ckBoxUseFieldValue = new JCheckBox();
    JButton useFieldValueBtn = new JButton("Use event field value");
    String typedText = null;
    GuiTagModel guiTagModel;
    Tagger tagger;
    EventEnterTagView eventEnterTagView;
    TagEnterSearchView tagEnterSearchView;

    public TagValueInputDialog(EventEnterTagView eventEnterTagView, TagEnterSearchView enteredTag) {
        super(eventEnterTagView.getAppView().getFrame(),true);
        guiTagModel = enteredTag.getModel();
        this.tagger = eventEnterTagView.getTagger();
        this.eventEnterTagView = eventEnterTagView;
        this.tagEnterSearchView = enteredTag;

        bgPanel.setLayout(new ConstraintLayout());
        bgPanel.setPreferredSize(new Dimension(400, 200));
        // message display
        populateUnitsComboBox();
        textField = new JTextField(20);
        ckBoxUseFieldValue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                if (cb.isSelected()) {
                    textField.setText(textField.getText() + "{field value}");
                }
            }
        });
        panelUseFieldValue.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelUseFieldValue.add(ckBoxUseFieldValue);
        panelUseFieldValue.add(new JLabel("Use event field value"));
        String unitMessage = "Unit:";
        String tagMessage = "Value:";
        Object[] array = null;
        if (guiTagModel.isNumeric()) {
            array = new Object[5];
            array[0] = unitMessage; array[1] = unitComboBox; array[2] = tagMessage; array[3] = panelUseFieldValue; array[4] = textField;
        }
        else {
            array = new Object[3];
            array[0] = tagMessage; array[1] = panelUseFieldValue; array[2] = textField;
        }
        // dialog buttons
        Object[] options = {btnString1, btnString2};
        bgPanel = new JOptionPane(array,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        setComponentsColor(bgPanel);
        setContentPane(bgPanel);
        getContentPane().setBackground(FontsAndColors.BLUE_MEDIUM);
        getContentPane().setForeground(FontsAndColors.BLUE_DARK);

        pack();
        setLocationRelativeTo(eventEnterTagView.getAppView().getFrame());

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        //Register an event handler that puts the text into the option pane.
        textField.addActionListener(this); // cuz implement ActionPerformed

        //Register an event handler that reacts to option pane state changes.
        bgPanel.addPropertyChangeListener(this); // cuz implement Property Change Listener
    }

    private void setComponentsColor(Container c){

        Component[] m = c.getComponents();

        for(int i = 0; i < m.length; i++){

            if(m[i].getClass().getName() == "javax.swing.JPanel") {
                m[i].setBackground(FontsAndColors.BLUE_MEDIUM);
                m[i].setForeground(FontsAndColors.BLUE_DARK);
            }

            if(c.getClass().isInstance(m[i]));
            setComponentsColor((Container)m[i]);
        }
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        bgPanel.setValue(btnString1);
    }

    /** This method reacts to state changes in the option pane. e.g. when a menu button is clicked */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == bgPanel)
                && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = bgPanel.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            bgPanel.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value) && !textField.getText().isEmpty()) { // user supposedly enter a value and hit ok
                typedText = textField.getText();
                TaggerView appView = eventEnterTagView.getAppView();
                if (typedText.contains(",")) {
                    inputFailed();
                    appView.showTaggerMessageDialog(
                            MessageConstants.TAG_COMMA_ERROR, "Ok", null, null);
                    return;
                }
                if (ckBoxUseFieldValue.isSelected()) {
                    useFieldValue();
                }
                else {
                    noFieldValue();
                }
            }
            else { //user closed dialog or clicked cancel
                typedText = null;
                inputFailed();
            }
        }
    }

    public void useFieldValue() {
        TaggerView appView = eventEnterTagView.getAppView();
        Tagger tagger = eventEnterTagView.getTagger();
        Set<Integer> selectedEvents = appView.getSelected();
        ArrayList<Integer> selectedEventsList = new ArrayList<>(selectedEvents.size());
        selectedEventsList.addAll(selectedEvents);

        /* Create an array containing entered tag value with field value replaced */
        String[] tagValues = new String[selectedEvents.size()];
        for (int i=0; i < selectedEvents.size(); i++) {
            Integer selectedEventID = selectedEventsList.get(i);
            TaggedEvent event = tagger.getTaggedEventFromGroupId(selectedEventID);
            String eventCode = event.getEventModel().getCode();
            // e.g. typedText: start of trial number {field value}
            String enterTextWithValue = typedText.replace("{field value}", eventCode);
            tagValues[i] = enterTextWithValue;
        }

       /* Validate numeric tag */
       if (guiTagModel.isNumeric()) {
           String unitString = new String();
           if (unitComboBox.getSelectedItem() != null) {
               unitString = unitComboBox.getSelectedItem().toString();
           }
           for (int i=0; i < tagValues.length; i++) {
               String validatedText = validateNumericValue(tagValues[i].trim(), unitString);
               if (validatedText == null) {
                   inputFailed();
                   TaggedEvent incompatibleEvent = tagger.getTaggedEventFromGroupId(selectedEventsList.get(i));
                   String message = "Problem with event " + incompatibleEvent.getEventModel().getCode() + ": " + MessageConstants.TAG_UNIT_ERROR;
                   appView.showTaggerMessageDialog(
                           message, "Ok", null, null);
                   return;
               }
           }
       }

       /* Add tags to events */
       for (int i=0; i < tagValues.length; i++) {
           AbstractTagModel newTag = tagger.createTransientTagModel(guiTagModel,
                   tagValues[i]);
           GuiTagModel gtm = (GuiTagModel) newTag;
           gtm.setAppView(appView);
           Set<Integer> eventID = new HashSet<Integer>();
           eventID.add(selectedEventsList.get(i));
           inputSuccess(gtm, eventID);
       }
    }

    public void noFieldValue() {
        TaggerView appView = eventEnterTagView.getAppView();
        if (guiTagModel.isNumeric()) {
            String unitString = new String();
            if (unitComboBox.getSelectedItem() != null) {
                unitString = unitComboBox.getSelectedItem().toString();
            }
            typedText = validateNumericValue(typedText.trim(), unitString);
            if (typedText == null) {
                inputFailed();
                appView.showTaggerMessageDialog(
                        MessageConstants.TAG_UNIT_ERROR, "Ok", null, null);
                return;
            }
        }
        AbstractTagModel newTag = tagger.createTransientTagModel(guiTagModel,
                typedText);
        GuiTagModel gtm = (GuiTagModel) newTag;
        gtm.setAppView(appView);
        inputSuccess(gtm, appView.getSelected());

    }

    /** This method clears the dialog and hides it. */
    public void inputSuccess(GuiTagModel tagModel, Set<Integer> eventIDs) {
        setVisible(false);
        dispose();
        tagEnterSearchView.addTagToEvent(tagModel, eventIDs);
    }

    /** This method clears the dialog and hides it. */
    public void inputFailed() {
        setVisible(false);
        dispose();
    }

    /**
     * Validates numerical value
     *
     * @param numericValue
     *            A numerical value
     * @param unit
     *            Unit The unit associated with numerical value
     * @return Null if invalid, numerical value with unit appended if valid
     */
    private String validateNumericValue(String numericValue, String unit) {
        if (numericValue.matches("^-?[0-9]+(\\.[0-9]+)?$")
                || numericValue.matches("^-?\\.[0-9]+$"))
            numericValue = numericValue + " " + unit;
        else
            numericValue = null;
        return numericValue;
    }

    /**
     * Populates the combo box that contains the units of the unit classes.
     *
     * @return The units combo box.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void populateUnitsComboBox() {
        String[] unitClassArray = Tagger.trimStringArray(guiTagModel
                .getUnitClass().split(","));
        String[] unitsArray = {};
        for (int i = 0; i < unitClassArray.length; i++) {
            if (tagger.unitClasses.get(unitClassArray[i]) != null) {
                String[] units = Tagger.trimStringArray(tagger.unitClasses.get(
                        unitClassArray[i]).split(","));
                unitsArray = Tagger.concat(unitsArray, units);
            }
        }
        Arrays.sort(unitsArray);
        unitComboBox.setModel(new DefaultComboBoxModel(unitsArray));
        setDefaultUnit();
    }

    /**
     * Set the units combo box to the default unit.
     */
    private void setDefaultUnit() {
        String[] unitClassArray = Tagger.trimStringArray(guiTagModel.getUnitClass().split(","));
        String unitClassDefault = tagger.unitClassDefaults.get(unitClassArray[0]);
        for (int i = 0; i < unitComboBox.getItemCount(); i++) {
            if (unitClassDefault.toLowerCase().equals(
                    unitComboBox.getItemAt(i).toString().toLowerCase())) {
                unitComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /* Getter */
    public String getTypedText() {
        return typedText;
    }
//	public static void main(String[] args) {
//		JFrame frame = new JFrame();
//		frame.setSize(600,500);
//		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//		frame.setVisible(true);
//		new TagValueInputDialog(frame, null, null);
//
//	}
}