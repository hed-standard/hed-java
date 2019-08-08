package edu.utsa.tagger.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.ConstraintLayout;

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
        bgPanel.setBackground(Color.white);
        bgPanel.setPreferredSize(new Dimension(400, 200));

        // message display
        populateUnitsComboBox();
        textField = new JTextField(20);
        String unitMessage = "Unit:";
        String tagMessage = "Value:";
        Object[] array = null;
        if (guiTagModel.isNumeric()) {
            array = new Object[4];
            array[0] = unitMessage; array[1] = unitComboBox; array[2] = tagMessage; array[3] = textField;
        }
        else {
            array = new Object[2];
            array[0] = tagMessage; array[1] = textField;
        }
        // dialog buttons
        Object[] options = {btnString1, btnString2};
        bgPanel = new JOptionPane(array,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        setContentPane(bgPanel);

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

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        bgPanel.setValue(btnString1);
    }

    /** This method reacts to state changes in the option pane. */
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

            if (btnString1.equals(value)) {
                typedText = textField.getText();
                if (guiTagModel.isNumeric()) {
                    String unitString = unitComboBox.getSelectedItem().toString();
                    typedText = validateNumericValue(typedText.trim(), unitString);
                    if (typedText != null) {
                        //we're done; clear, dismiss the dialog, and update tag event
                        finished();
                    }
                    else {
                        //text was invalid
                        textField.selectAll();
                        JOptionPane.showMessageDialog(
                                TagValueInputDialog.this,
                                "Input must be numeric",
                                "Try again",
                                JOptionPane.ERROR_MESSAGE);
                        typedText = null;
                        textField.requestFocusInWindow();
                    }
                }
                else { // if not numeric takes in any value
                    finished();
                }
            }
            else { //user closed dialog or clicked cancel
                typedText = null;
                finished();
            }
        }
    }


    /** This method clears the dialog and hides it. */
    public void finished() {
        guiTagModel.setPath(guiTagModel.getParentPath() + "/" + typedText);
        setVisible(false);
        dispose();
        tagEnterSearchView.addTagToEvent();
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
        if (numericValue.matches("^[0-9]+(\\.[0-9]+)?$")
                || numericValue.matches("^\\.[0-9]+$"))
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