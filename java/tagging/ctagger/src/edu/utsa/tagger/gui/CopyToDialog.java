package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.ConstraintLayout;

/**
 * Dialog for user to select what events to copy the tag to
 * 
 * @author Dung Truong
 */
public class CopyToDialog extends JDialog implements ItemListener, PropertyChangeListener {
	
	private JOptionPane bgPanel = new JOptionPane();
	private JTextField textField;
	String btnString1 = "Copy"; // used to communicate between input and optionpane
    String btnString2 = "Cancel";
    String typedText = null; 
    TaggerView appView;
    GuiTagModel guiTagModel = null;
    Tagger tagger = null;
    TaggedEvent tgevt;
    JPanel typeOptionsPanel;
    HashMap<String,Integer> boxIdMap = new HashMap<String,Integer>();
	public CopyToDialog(Tagger tgr,TaggerView view, GuiTagModel gtm) {
		super(view.getFrame(),true);
		appView = view;
		guiTagModel = gtm;
		tagger = tgr;

		bgPanel.setLayout(new ConstraintLayout());
		bgPanel.setBackground(Color.white);
		bgPanel.setPreferredSize(new Dimension(400, 200));
		
		// message display
		textField = new JTextField(20);
		String message = "Select events:";
		typeOptionsPanel = new JPanel(new GridLayout(0, 3));
		Set<Integer> groupIds = appView.selectedGroups;
		for (TaggedEvent event : tagger.getEgtSet()) {
			if (!groupIds.contains(event.getEventGroupId())) {
				JCheckBox cb = new JCheckBox(event.getEventModel().getCode());
				cb.addItemListener(this);
				typeOptionsPanel.add(cb);
				boxIdMap.put(cb.getText(), event.getEventGroupId());
			}
		}
		Object[] array = {message,typeOptionsPanel};

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
		setLocationRelativeTo(appView.getFrame());
		
		//Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });
        
        //Register an event handler that reacts to option pane state changes.
        bgPanel.addPropertyChangeListener(this); // cuz implement Property Change Listener
        
        setVisible(true);
	}
	
    /** Listens to the check boxes. */
    public void itemStateChanged(ItemEvent e) {
        int index = 0;
        char c = '-';
        Object source = e.getItemSelectable();
        
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
            	Component[] components = typeOptionsPanel.getComponents();
            	Set<Integer> groupIds = new HashSet<Integer>();
            	for (Component comp : components) {
            		JCheckBox cb = (JCheckBox) comp;
            		if (cb.getSelectedObjects() != null) {
            			groupIds.add(boxIdMap.get(cb.getText()));
            		}
            	}
            	setVisible(false);
            	guiTagModel.requestToggleTag(groupIds);
            } 
            dispose();
        }
    }
    
}
