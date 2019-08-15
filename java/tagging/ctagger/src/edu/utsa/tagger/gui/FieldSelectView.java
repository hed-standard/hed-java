package edu.utsa.tagger.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicArrowButton;

import edu.utsa.tagger.FieldSelectLoader;

/**
 * This class creates a GUI that allows the user to select which fields to
 * exclude or tag.
 * 
 * @author Jeremy Cockfield, Kay Robbins
 *
 */
@SuppressWarnings("serial")
public class FieldSelectView extends JPanel {

	private boolean isTagBox = false;
    private JLabel descriptionLabel = new JLabel(new String(), SwingConstants.CENTER);
    private JLabel primaryFieldLabel = new JLabel("Primary field: ", SwingConstants.CENTER);
    private String primaryFieldDescription = "(to change, double-click the field name in the right panel)";
	private JPanel parentPanel = new JPanel(new BorderLayout());
	private JPanel northPanel = new JPanel(new BorderLayout());
	private JPanel centerPanel = new JPanel(new BorderLayout());
	private JPanel southPanel = new JPanel(new BorderLayout());
    private JLabel tagBoxLabel = new JLabel("Use these events fields for tagging");
    private JLabel ignoreBoxLabel = new JLabel("Ignore these event fields");
	private JList<String> taggedListBox;
    private JList<String> ignoredListBox;
	private JButton transferButton = new JButton("<< Transfer >>");
	private JButton removeAllButton = new JButton("<< Remove all");
	private JButton addAllButton = new JButton("Add all >>");
	private JButton cancelButton = new JButton("Cancel");
	private JButton okayButton = new JButton("Ok");
	private String primaryField = new String();
	private JFrame jFrame = new JFrame();
	private FieldSelectLoader loader;
	private String frameTitle;
	private BasicArrowButton rightUpButton = new BasicArrowButton(BasicArrowButton.NORTH);
	private BasicArrowButton rightDownButton = new BasicArrowButton(BasicArrowButton.SOUTH);

    public FieldSelectView(FieldSelectLoader loader, String frameTitle, String[] ignoredFields, String[] taggedFields, String primaryField) {
		this.loader = loader;
		this.frameTitle = frameTitle;
		this.primaryField = primaryField;
        this.ignoredListBox = this.intializeJList(ignoredFields);
        this.taggedListBox = this.intializeJList(taggedFields);
        this.layoutComponents(ignoredFields, taggedFields);
        this.primaryFieldLabel.setText("<html>Primary field: <b>" + primaryField + "</b> " + this.primaryFieldDescription + "</html>");
	}

	/**
	 * Initializes a JList.
	 * 
	 * @param elements
	 *            The elements that is used to initialize a JList.
	 */
	private JList<String> intializeJList(String[] elements) {
        if (elements != null) {
            Arrays.sort(elements);
            return new JList(elements);
        } else {
            return new JList();
        }
	}
	/**
	 * Layout the components of the GUI.
	 *
	 * @param ignoredFields
	 *            An array of strings containing the excluded fields.
	 * @param taggedFields
	 *            An array of strings containing the tagged fields.
	 */
    private void layoutComponents(String[] ignoredFields, String[] taggedFields) {
        this.setListeners();
        this.setColors();
        this.northPanel.add(this.descriptionLabel);
        this.centerPanel.add(this.createPanelForListBoxWithLabel(this.ignoredListBox, this.ignoreBoxLabel, this.addAllButton), "West");
        this.centerPanel.add(this.createCenterPanelButtons(), "Center");
        this.centerPanel.add(this.createPanelForListBoxWithLabel(this.taggedListBox, this.tagBoxLabel, this.removeAllButton), "East");
        this.southPanel.add(this.createSouthPanel());
        this.parentPanel.add(this.northPanel, "North");
        this.parentPanel.add(this.centerPanel, "Center");
        this.parentPanel.add(this.southPanel, "South");
        this.createFrame(this.parentPanel);
	}

	/**
	 * Creates a JFrame and adds a parent panel to it.
	 * 
	 * @param parentPanel
	 *            The parent panel that is added to the JFrame.
	 */
	private void createFrame(JPanel parentPanel) {
		jFrame.add(parentPanel);
		ImageIcon img = new ImageIcon("src\\vml_logo.png");
		jFrame.setIconImage(img.getImage());
		jFrame.setTitle(frameTitle);
		jFrame.pack();
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
		jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Creates the south panel.
	 * 
	 * @return A JPanel that is added to the south region of the parent panel.
	 */
	private JPanel createSouthPanel() {
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
		buttonPanel.add(Box.createRigidArea(new Dimension(350, 50)));
		buttonPanel.add(primaryFieldLabel, BorderLayout.CENTER);
		buttonPanel.add(createSouthPanelButtons(), BorderLayout.EAST);
		return buttonPanel;
	}

	/**
	 * Creates the south panel buttons.
	 * 
	 * @return A button JPanel that is added to the south region of the parent
	 *         panel.
	 */
	private JPanel createSouthPanelButtons() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
		buttonPanel.add(this.cancelButton);
//		buttonPanel.add(Box.createRigidArea(new Dimension(3, 0)));
		buttonPanel.add(okayButton);
//        Dimension d = this.cancelButton.getMaximumSize();
//        this.okayButton.setPreferredSize(new Dimension(d));
		return buttonPanel;
	}

	/**
	 * Creates the center panel buttons.
	 * 
	 * @return A button JPanel that is added to the center region of the parent
	 *         panel.
	 */
	private JPanel createCenterPanelButtons() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		transferButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		transferButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		buttonPanel.add(transferButton);
		return buttonPanel;
	}

	/**
	 * Sets the listeners for all of the components.
	 */
	private void setListeners() {
        this.transferButton.addActionListener(new FieldSelectView.addRemoveListener());
        this.removeAllButton.addActionListener(new FieldSelectView.removeAddAllListener());
        this.addAllButton.addActionListener(new FieldSelectView.removeAddAllListener());
        this.taggedListBox.addListSelectionListener(new FieldSelectView.SharedListSelectionHandler());
        this.ignoredListBox.addListSelectionListener(new FieldSelectView.SharedListSelectionHandler());
        this.cancelButton.addActionListener(new FieldSelectView.cancelOkayListener());
        this.okayButton.addActionListener(new FieldSelectView.cancelOkayListener());
        this.jFrame.addWindowListener(new FieldSelectView.frameWindowListener());
        this.taggedListBox.addKeyListener(new FieldSelectView.LeftRightListener());
        this.ignoredListBox.addKeyListener(new FieldSelectView.LeftRightListener());
        this.taggedListBox.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
                    String selectedValue = (String)FieldSelectView.this.taggedListBox.getSelectedValue();
                    if (selectedValue != null && !selectedValue.equals(FieldSelectView.this.primaryField)) {
                        FieldSelectView.this.primaryField = selectedValue;
                        FieldSelectView.this.updatePrimaryField();
                        FieldSelectView.this.sortJList(FieldSelectView.this.taggedListBox);
					} else {
						primaryField = new String();
						updatePrimaryField();
					}
				}
			}
		});
        this.ignoredListBox.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
                    String selectedValue = (String)FieldSelectView.this.ignoredListBox.getSelectedValue();
					if (selectedValue != null) {
                        FieldSelectView.this.isTagBox = true;
                        FieldSelectView.this.primaryField = selectedValue;
                        int primaryFieldIndex = FieldSelectView.this.ignoredListBox.getSelectedIndex();
                        FieldSelectView.this.ignoredListBox.setSelectedIndex(primaryFieldIndex);
                        FieldSelectView.this.transferFromBoxes(FieldSelectView.this.ignoredListBox, FieldSelectView.this.taggedListBox);
                        FieldSelectView.this.taggedListBox.setSelectedIndex(FieldSelectView.this.taggedListBox.getModel().getSize() - 1);
                        FieldSelectView.this.taggedListBox.requestFocusInWindow();
                        FieldSelectView.this.updatePrimaryField();
                        FieldSelectView.this.sortJList(FieldSelectView.this.ignoredListBox);
                        FieldSelectView.this.sortJList(FieldSelectView.this.taggedListBox);
					}
				}
			}
		});
	}

	/**
	 * Checks to see if the primary field is selected in the tag JList.
	 */
	private void checkPrimary() {
		List<String> selectedValues = taggedListBox.getSelectedValuesList();
		if (!containsElement(taggedListBox, primaryField) && !selectedValues.contains(primaryField)) {
			primaryField = new String();
		}
	}

	/**
	 * Checks to see if a element is in a JList.
	 * 
	 * @param jList
	 *            The JList searched through.
	 * @param element
	 *            The element that is searched for.
	 * 
	 * @return true if found in the JList, false if otherwise.
	 */
	private boolean containsElement(JList<String> jList, String element) {
		ListModel<String> jModel = jList.getModel();
		int numElements = jModel.getSize();
		for (int i = 0; i < numElements; i++) {
			if (jModel.getElementAt(i).equals(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the colors for all of the components.
	 */
	private void setColors() {
	    descriptionLabel.setForeground(FontsAndColors.BLUE_DARK);
		ignoreBoxLabel.setForeground(FontsAndColors.BLUE_DARK);
		primaryFieldLabel.setForeground(FontsAndColors.BLUE_DARK);
		tagBoxLabel.setForeground(FontsAndColors.BLUE_DARK);
		removeAllButton.setBackground(Color.gray);
		removeAllButton.setOpaque(false);
		addAllButton.setBackground(Color.gray);
		addAllButton.setOpaque(false);
		transferButton.setBackground(Color.gray);
		transferButton.setOpaque(false);
		cancelButton.setBackground(Color.gray);
		cancelButton.setOpaque(false);
		okayButton.setBackground(Color.gray);
		okayButton.setOpaque(false);
	}

	/**
	 * Creates a JPanel that holds a list box.
	 * 
	 * @param listBox
	 *            The list box that is added to the panel.
	 * @param listBoxLabel
	 *            The label of the list box.
	 * @param transferButton
	 *            The button to transfer all of the elements in the list box to
	 *            the other list box.
	 * @return
	 */
	private JPanel createPanelForListBoxWithLabel(JList<String> listBox, JLabel listBoxLabel, JButton transferButton,
			JButton upButton, JButton downButton) {
		JPanel panel = new JPanel(new BorderLayout(0, 3));
		listBox.setVisibleRowCount(20);
		panel.add(listBoxLabel, BorderLayout.NORTH);
		JScrollPane scrollPaneForListBox = new JScrollPane(listBox);
		scrollPaneForListBox.setPreferredSize(new Dimension(200, 400));
		panel.setBorder(new EmptyBorder(new Insets(10, 40, 30, 40)));
		panel.add(scrollPaneForListBox, BorderLayout.CENTER);
		panel.add(transferButton, BorderLayout.SOUTH);
		JPanel arrowPanel = new JPanel(new BorderLayout(0, 2));
		arrowPanel.setLayout(new BoxLayout(arrowPanel, BoxLayout.Y_AXIS));
		arrowPanel.add(upButton);
		arrowPanel.add(downButton);
		panel.add(arrowPanel, BorderLayout.EAST);
		return panel;
	}

	/**
	 * Creates a JPanel that holds a list box.
	 * 
	 * @param listBox
	 *            The list box that is added to the panel.
	 * @param listBoxLabel
	 *            The label of the list box.
	 * @param transferButton
	 *            The button to transfer all of the elements in the list box to
	 *            the other list box.
	 * @return
	 */
	private JPanel createPanelForListBoxWithLabel(JList<String> listBox, JLabel listBoxLabel, JButton transferButton) {
		JPanel panel = new JPanel(new BorderLayout(0, 3));
		panel.setBackground(FontsAndColors.BLUE_MEDIUM);
		listBox.setVisibleRowCount(20);
		panel.add(listBoxLabel, BorderLayout.NORTH);
		JScrollPane scrollPaneForListBox = new JScrollPane(listBox);
		scrollPaneForListBox.setPreferredSize(new Dimension(200, 400));
		panel.setBorder(new EmptyBorder(new Insets(10, 40, 30, 40)));
		panel.add(scrollPaneForListBox, BorderLayout.CENTER);
		panel.add(transferButton, BorderLayout.SOUTH);
		JPanel arrowPanel = new JPanel(new BorderLayout(0, 2));
		arrowPanel.setLayout(new BoxLayout(arrowPanel, BoxLayout.Y_AXIS));
		panel.add(arrowPanel, BorderLayout.EAST);
		return panel;
	}

	/**
	 * Gets all of the tag fields from the tag ListBox.
	 * 
	 * @return A String array containing all of the tag fields.
	 */
	public String[] getTaggedFields() {
		return listBoxToArray(taggedListBox);
	}

    public String[] getIgnoredFields() {
        return this.listBoxToArray(this.ignoredListBox);
	}

	/**
	 * Puts the JList elements in a String array.
	 * 
	 * @param listBox
	 *            The ListBox whose elements are put into a String array.
	 * @return The String array that contains all of the elements in the JList.
	 */
	public String[] listBoxToArray(JList<String> listBox) {
		ListModel<String> listBoxModel = listBox.getModel();
		int numElements = listBoxModel.getSize();
		String[] listBoxArray = new String[numElements];
		for (int i = 0; i < numElements; i++) {
			listBoxArray[i] = listBoxModel.getElementAt(i);
		}
		return listBoxArray;
	}

	/**
	 * Updates the primary field label.
	 */
	private void updatePrimaryField() {
        this.checkPrimary();
        this.primaryFieldLabel.setText("<html>Primary field: <b>" + this.primaryField + "</b> " + this.primaryFieldDescription + "</html>");
	}

	/**
	 * Transfers the selected elements in one JList to another.
	 * 
	 * @param jList1
	 *            The JList that the selected elements are transferred from.
	 * @param jList2
	 *            The JList that the selected elements from the other JList are
	 *            transferred to.
	 */
	private void transferFromBoxes(JList<String> jList1, JList<String> jList2) {
		int[] selectedIndexes = jList1.getSelectedIndices();
		List<Integer> indexes = new ArrayList<Integer>();
		for (int value : selectedIndexes) {
			indexes.add(value);
		}
		if (indexes.isEmpty())
			return;
		addElements(jList1, jList2, indexes);
		removeElements(jList1, indexes);
	}

	/**
	 * Transfers all elements in one JList to another.
	 * 
	 * @param jList1
	 *            The JList that all elements are transferred from.
	 * @param jList2
	 *            The JList that all the elements from the other JList are
	 *            transferred to.
	 */
	private void transferAllFromBoxes(JList<String> jList1, JList<String> jList2) {
		addAllElements(jList1, jList2);
		removeAllElements(jList1);
	}

	/**
	 * Moves up the selected elements in a JList.
	 * 
	 * @param jList
	 *            The JList whose selected elements are moved up.
	 */
	private void moveUpElements(JList<String> jList) {
		int[] selectedIndexes = jList.getSelectedIndices();
		List<Integer> indexes = new ArrayList<Integer>();
		for (int value : selectedIndexes) {
			indexes.add(value);
		}
		if (indexes.isEmpty())
			return;
		shiftUpElements(jList, indexes);
	}

	/**
	 * Moves down the selected elements in a JList.
	 * 
	 * @param jList
	 *            The JList whose selected elements are moved down.
	 */
	private void moveDownElements(JList<String> jList) {
		int[] selectedIndexes = jList.getSelectedIndices();
		List<Integer> indexes = new ArrayList<Integer>();
		for (int value : selectedIndexes) {
			indexes.add(value);
		}
		if (indexes.isEmpty())
			return;
		shiftDownElements(jList, indexes);
	}

	/**
	 * Moves the selected element to the beginning of the JList.
	 * 
	 * @param jList
	 *            The JList whose selected element is moved to the beginning.
	 */
	private void moveElementToStart(JList<String> jList) {
		int index = jList.getSelectedIndex();
		if (index == -1)
			return;
		shiftElementsToStart(jList, index);
	}

	/**
	 * Shift the elements up to the beginning in the JList.
	 * 
	 * @param jList
	 *            The JList whose element is shifted to the beginning.
	 * @param index
	 *            The index whose element is shifted to the beginning.
	 */
	private void shiftElementsToStart(JList<String> jList, int index) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		ListModel<String> lm = jList.getModel();
		int numElements = lm.getSize();
		model.addElement(lm.getElementAt(index));
		for (int i = 0; i < numElements; i++) {
			if (i == index)
				continue;
			model.addElement(lm.getElementAt(i));
		}
		jList.setModel(model);
		jList.setSelectedIndex(0);
	}

	/**
	 * Shift the elements up one position in the JList.
	 * 
	 * @param jList
	 *            The JList whose elements are shifted up one position.
	 * @param indexes
	 *            The indices whose elements are shifted up one position.
	 */
	private void shiftUpElements(JList<String> jList, List<Integer> indexes) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		ListModel<String> lm = jList.getModel();
		List<Integer> newIndecies = new ArrayList<Integer>();
		int numElements = lm.getSize();
		int numIndexes = indexes.size();
		int start = 0;
		for (int i = 0; i < numElements; i++) {
			model.addElement(lm.getElementAt(i));
		}
		for (int i = 0; i < numIndexes; i++) {
			if (indexes.get(i) - 1 >= 0 && indexes.get(i) != start) {
				swapElements(model, indexes.get(i), indexes.get(i) - 1);
				newIndecies.add(indexes.get(i) - 1);
			} else {
				start++;
				newIndecies.add(indexes.get(i));
			}
		}
		jList.setModel(model);
		jList.setSelectedIndices(integerToInt(newIndecies));
	}

	/**
	 * Shift the elements down one position in the JList.
	 * 
	 * @param jList
	 *            The JList whose elements are shifted down one position.
	 * @param indices
	 *            The indices whose elements are shifted down one position.
	 */
	private void shiftDownElements(JList<String> jList, List<Integer> indices) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		ListModel<String> lm = jList.getModel();
		List<Integer> newIndecies = new ArrayList<Integer>();
		int numElements = lm.getSize();
		int numIndexes = indices.size();
		int finish = numElements - 1;
		for (int i = 0; i < numElements; i++) {
			model.addElement(lm.getElementAt(i));
		}
		for (int i = numIndexes - 1; i >= 0; i--) {
			if (indices.get(i) + 1 <= numElements - 1 && indices.get(i) != finish) {
				swapElements(model, indices.get(i), indices.get(i) + 1);
				newIndecies.add(indices.get(i) + 1);
			} else {
				finish--;
				newIndecies.add(indices.get(i));
			}
		}
		jList.setModel(model);
		jList.setSelectedIndices(integerToInt(newIndecies));
	}

	/**
	 * Swap two indices in a DefaultListModel.
	 * 
	 * @param defaultListModel
	 *            The DefaultListModel that the two indices are swapped.
	 * @param firstIndex
	 *            The first index.
	 * @param secondIndex
	 *            The second index.
	 */
	private void swapElements(DefaultListModel<String> defaultListModel, int firstIndex, int secondIndex) {
		String oldElement = defaultListModel.getElementAt(firstIndex);
		String newElement = defaultListModel.getElementAt(secondIndex);
		defaultListModel.remove(firstIndex);
		defaultListModel.add(firstIndex, newElement);
		defaultListModel.remove(secondIndex);
		defaultListModel.add(secondIndex, oldElement);
	}

	/**
	 * Converts a Integer List to a integer array.
	 * 
	 * @param integers
	 *            A Integer List.
	 * @return A integer array.
	 */
	private int[] integerToInt(List<Integer> integers) {
		int[] ints = new int[integers.size()];
		int i = 0;
		for (int value : integers) {
			ints[i] = value;
			i++;
		}
		return ints;
	}

	/**
	 * Removes the indices from the JList.
	 * 
	 * @param jList
	 *            The JList that the indices are removed from.
	 * @param indices
	 *            The indices in the JList that are removed.
	 */
	private void removeElements(JList<String> jList, List<Integer> indices) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		ListModel<String> lm = jList.getModel();
		int numElements = lm.getSize();
		for (int i = 0; i < numElements; i++) {
			if (!indices.contains(i)) {
				model.addElement(lm.getElementAt(i));
			}
		}
		jList.clearSelection();
		jList.setModel(model);
	}

	/**
	 * Removes all of the elements from a JList.
	 * 
	 * @param jList
	 *            The JList that all of the elements are removed from.
	 */
	private void removeAllElements(JList<String> jList) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		jList.setModel(model);
	}

	/**
	 * Adds the selected elements from one JList to another JList.
	 * 
	 * @param jList1
	 *            The JList that the selected elements are transferred from.
	 * @param jList2
	 *            The JList that the selected elements from the other JList are
	 *            transferred to.
	 */
	private void addElements(JList<String> jList1, JList<String> jList2, List<Integer> indexes) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		ListModel<String> lm1 = jList1.getModel();
		ListModel<String> lm2 = jList2.getModel();
		int numElements = lm2.getSize();
		for (int i = 0; i < numElements; i++)
			model.addElement(lm2.getElementAt(i));
		for (int value : indexes)
			model.addElement(lm1.getElementAt(value));
		jList2.setModel(model);
	}

	/**
	 * Adds all of the elements from one JList to another JList.
	 * 
	 * @param jList1
	 *            The JList that all of the elements are transferred from.
	 * @param jList2
	 *            The JList that all of the elements from the other JList are
	 *            transferred to.
	 */
	private void addAllElements(JList<String> jList1, JList<String> jList2) {
		DefaultListModel<String> model = new DefaultListModel<String>();
		ListModel<String> lm1 = jList1.getModel();
		ListModel<String> lm2 = jList2.getModel();
		int numElements2 = lm2.getSize();
		int numElements1 = lm1.getSize();
		for (int i = 0; i < numElements2; i++)
			model.addElement(lm2.getElementAt(i));
		for (int i = 0; i < numElements1; i++)
			model.addElement(lm1.getElementAt(i));
		jList2.setModel(model);
	}

	/**
	 * The action listener class for the JList list boxes.
	 */
	public class SharedListSelectionHandler implements ListSelectionListener {
		public SharedListSelectionHandler() {
		}

		public void valueChanged(ListSelectionEvent e) {
			JList<String> sourceList = (JList)e.getSource();
			if (sourceList.getValueIsAdjusting()) {
				if (sourceList == FieldSelectView.this.taggedListBox) {
					FieldSelectView.this.isTagBox = true;
					FieldSelectView.this.ignoredListBox.clearSelection();
				} else {
					FieldSelectView.this.isTagBox = false;
					FieldSelectView.this.taggedListBox.clearSelection();
				}
			}

		}
	}

	private void sortJList(JList<String> jList) {
		ListModel<String> model = jList.getModel();
		String[] strings = new String[model.getSize()];

		for(int i = 0; i < strings.length; ++i) {
			strings[i] = ((String)model.getElementAt(i)).toString();
		}

		Arrays.sort(strings);
		jList.setListData(strings);
	}

	/**
	 * Creates a integer array containing elements between a range.
	 *
	 * @param start
	 *            The start of the range.
	 * @param end
	 *            The end of the range.
	 * @return The integer array containing the elements between the range.
	 */
	private int[] createArrayFromRange(int start, int end) {
		int numElements = end - start;
		int[] array = new int[numElements];

		for(int i = 0; i < numElements; ++i) {
			array[i] = start++;
		}

		return array;
	}

	/**
	 * A dialog that asks if the user would like to cancel the FieldSelectView.
	 *
	 * @param message
	 *            The message that is displayed in the dialog.
	 * @return 0 if the FieldSelctView should be canceled, 1 if otherwise.
	 */
	private int createYesNoDialog(String message) {
		YesNoDialog dialog = new YesNoDialog(this.jFrame, message);
		int option = dialog.showDialog();
		if (option == 0) {
			this.loader.setNotified(true);
			this.jFrame.dispose();
			this.loader.setIgnoredFields(this.getIgnoredFields());
			this.loader.setTaggedFields(this.getTaggedFields());
			this.loader.setPrimaryField(this.primaryField);
		}

		return option;
	}
	/**
	 * The action listener class for the Left and Right arrow buttons.
	 */
	public class LeftRightListener implements KeyListener {
		public void keyTyped(KeyEvent e) {
			// Invoked when a key has been typed.
		}

		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
            int currentSize;
            int newSize;
            int[] selectedIndexes;
            if (key == KeyEvent.VK_LEFT && FieldSelectView.this.isTagBox) {
                FieldSelectView.this.isTagBox = false;
                currentSize = FieldSelectView.this.ignoredListBox.getModel().getSize();
                FieldSelectView.this.transferFromBoxes(FieldSelectView.this.taggedListBox, FieldSelectView.this.ignoredListBox);
                newSize = FieldSelectView.this.ignoredListBox.getModel().getSize();
                selectedIndexes = FieldSelectView.this.createArrayFromRange(currentSize, newSize);
                FieldSelectView.this.ignoredListBox.setSelectedIndices(selectedIndexes);
                FieldSelectView.this.updatePrimaryField();
                FieldSelectView.this.sortJList(FieldSelectView.this.ignoredListBox);
                FieldSelectView.this.ignoredListBox.requestFocusInWindow();
            } else if (key == KeyEvent.VK_RIGHT && !FieldSelectView.this.isTagBox) {
                FieldSelectView.this.isTagBox = true;
                currentSize = FieldSelectView.this.taggedListBox.getModel().getSize();
                FieldSelectView.this.transferFromBoxes(FieldSelectView.this.ignoredListBox, FieldSelectView.this.taggedListBox);
                newSize = FieldSelectView.this.taggedListBox.getModel().getSize();
                selectedIndexes = FieldSelectView.this.createArrayFromRange(currentSize, newSize);
                FieldSelectView.this.taggedListBox.setSelectedIndices(selectedIndexes);
                FieldSelectView.this.updatePrimaryField();
                FieldSelectView.this.sortJList(FieldSelectView.this.taggedListBox);
                FieldSelectView.this.taggedListBox.requestFocusInWindow();
			}
		}

		public void keyReleased(KeyEvent e) {
			// Invoked when a key has been released.
		}
	}


	/**
	 * The action listener class for the Remove and Add buttons.
	 */
	public class addRemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton sourceButton = (JButton)e.getSource();
			int currentSize;
			int newSize;
			int[] selectedIndexes;
			if (sourceButton == FieldSelectView.this.transferButton && FieldSelectView.this.isTagBox) {
				FieldSelectView.this.isTagBox = false;
				currentSize = FieldSelectView.this.ignoredListBox.getModel().getSize();
				FieldSelectView.this.transferFromBoxes(FieldSelectView.this.taggedListBox, FieldSelectView.this.ignoredListBox);
				newSize = FieldSelectView.this.ignoredListBox.getModel().getSize();
				selectedIndexes = FieldSelectView.this.createArrayFromRange(currentSize, newSize);
				FieldSelectView.this.ignoredListBox.setSelectedIndices(selectedIndexes);
				FieldSelectView.this.taggedListBox.clearSelection();
				FieldSelectView.this.updatePrimaryField();
				FieldSelectView.this.sortJList(FieldSelectView.this.ignoredListBox);
				FieldSelectView.this.ignoredListBox.requestFocusInWindow();
			} else if (sourceButton == FieldSelectView.this.transferButton && !FieldSelectView.this.isTagBox) {
				FieldSelectView.this.isTagBox = true;
				currentSize = FieldSelectView.this.taggedListBox.getModel().getSize();
				FieldSelectView.this.transferFromBoxes(FieldSelectView.this.ignoredListBox, FieldSelectView.this.taggedListBox);
				newSize = FieldSelectView.this.taggedListBox.getModel().getSize();
				selectedIndexes = FieldSelectView.this.createArrayFromRange(currentSize, newSize);
				FieldSelectView.this.taggedListBox.setSelectedIndices(selectedIndexes);
				FieldSelectView.this.ignoredListBox.clearSelection();
				FieldSelectView.this.updatePrimaryField();
				FieldSelectView.this.sortJList(FieldSelectView.this.taggedListBox);
				FieldSelectView.this.taggedListBox.requestFocusInWindow();
			}

		}
	}

	/**
	 * The action listener class for the Cancel and Okay buttons.
	 */
	public class cancelOkayListener implements ActionListener {
		public cancelOkayListener() {
		}

		public void actionPerformed(ActionEvent e) {
			JButton sourceButton = (JButton)e.getSource();
			if (sourceButton == FieldSelectView.this.cancelButton) {
				String message = "Are you sure you want to exit?";
				FieldSelectView.this.createYesNoDialog(message);
			} else {
				FieldSelectView.this.loader.setNotified(true);
				FieldSelectView.this.loader.setSubmitted(true);
				FieldSelectView.this.jFrame.dispose();
			}

			FieldSelectView.this.loader.setIgnoredFields(FieldSelectView.this.getIgnoredFields());
			FieldSelectView.this.loader.setTaggedFields(FieldSelectView.this.getTaggedFields());
			FieldSelectView.this.loader.setPrimaryField(FieldSelectView.this.primaryField);
		}
	}

	/**
	 * The frame window listener class for the close button.
	 */
	public class frameWindowListener extends WindowAdapter {
		public frameWindowListener() {
		}

		public void windowClosing(WindowEvent evt) {
			String message = "Are you sure you want to exit?";
			FieldSelectView.this.createYesNoDialog(message);
		}
	}

	/**
	 * The action listener class for the Remove all and Add all buttons.
	 */
	public class removeAddAllListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton sourceButton = (JButton)e.getSource();
			int currentSize;
			int newSize;
			int[] selectedIndexes;
			if (sourceButton == FieldSelectView.this.removeAllButton && FieldSelectView.this.taggedListBox.getModel().getSize() > 0) {
				FieldSelectView.this.isTagBox = false;
				currentSize = FieldSelectView.this.ignoredListBox.getModel().getSize();
				FieldSelectView.this.transferAllFromBoxes(FieldSelectView.this.taggedListBox, FieldSelectView.this.ignoredListBox);
				newSize = FieldSelectView.this.ignoredListBox.getModel().getSize();
				selectedIndexes = FieldSelectView.this.createArrayFromRange(currentSize, newSize);
				FieldSelectView.this.ignoredListBox.setSelectedIndices(selectedIndexes);
				FieldSelectView.this.updatePrimaryField();
				FieldSelectView.this.sortJList(FieldSelectView.this.ignoredListBox);
				FieldSelectView.this.ignoredListBox.requestFocusInWindow();
			} else if (sourceButton == FieldSelectView.this.addAllButton && FieldSelectView.this.ignoredListBox.getModel().getSize() > 0) {
				FieldSelectView.this.isTagBox = true;
				currentSize = FieldSelectView.this.taggedListBox.getModel().getSize();
				FieldSelectView.this.transferAllFromBoxes(FieldSelectView.this.ignoredListBox, FieldSelectView.this.taggedListBox);
				newSize = FieldSelectView.this.taggedListBox.getModel().getSize();
				selectedIndexes = FieldSelectView.this.createArrayFromRange(currentSize, newSize);
				FieldSelectView.this.taggedListBox.setSelectedIndices(selectedIndexes);
				FieldSelectView.this.updatePrimaryField();
				FieldSelectView.this.sortJList(FieldSelectView.this.taggedListBox);
				FieldSelectView.this.taggedListBox.requestFocusInWindow();
			}

		}
	}


	/**
	 * The action listener class for the Move up and Move down buttons.
	 */
	public class moveUpDownListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton sourceButton = (JButton) e.getSource();
			if (sourceButton == rightUpButton && isTagBox) {
				moveUpElements(taggedListBox);
			} else if (sourceButton == rightDownButton && isTagBox) {
				moveDownElements(taggedListBox);
			}
		}
	}


	/**
	 * The action listener class for the Remove primary button.
	 */
	public class removePrimaryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			primaryField = new String();
			updatePrimaryField();
		}
	}


}