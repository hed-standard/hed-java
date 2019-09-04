package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.Constraint;
import edu.utsa.tagger.guisupport.ConstraintContainer;
import edu.utsa.tagger.guisupport.ConstraintLayout;
import edu.utsa.tagger.guisupport.ITagDisplay;
import edu.utsa.tagger.guisupport.XButton;
import edu.utsa.tagger.guisupport.XScrollTextBox;
import edu.utsa.tagger.guisupport.XTextBox;

/**
 * View allowing the user to add a value to a tag that takes values.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@SuppressWarnings("serial")
public class AddValueView extends ConstraintContainer {

	public final static int HEIGHT = 80;
	private int top = 15;
	private final Tagger tagger;
	private final TaggerView appView;
	private final GuiTagModel tagModel;
	private boolean highlight = false;
	private final ITagDisplay alternateView;
	private final TagEditOptionButton okButton = new TagEditOptionButton("OK") {
		@Override
		public Font getFont() {
			Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
			fontAttributes.put(TextAttribute.UNDERLINE,
					TextAttribute.UNDERLINE_ON);
			return FontsAndColors.contentFont.deriveFont(fontAttributes);
		}
	};

	final XScrollTextBox valueField = new XScrollTextBox(new XTextBox()) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	final JComboBox units = new JComboBox(new String[] {}) {

	};

	final JLabel unitsLabel = new JLabel("units", JLabel.LEFT) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final TagEditOptionButton cancelButton = new TagEditOptionButton("Cancel") {
		@Override
		public Font getFont() {
			Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
			fontAttributes.put(TextAttribute.UNDERLINE,
					TextAttribute.UNDERLINE_ON);
			return FontsAndColors.contentFont.deriveFont(fontAttributes);
		}
	};

	final JLabel valueLabel = new JLabel("value", JLabel.LEFT) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			highlight = false;
			repaint();
		}
	};

	public AddValueView(Tagger tagger, TaggerView appView,
			ITagDisplay alternateView, GuiTagModel guiTagModel) {
		this.tagger = tagger;
		this.appView = appView;
		this.tagModel = guiTagModel;
		this.alternateView = alternateView;
		addGuiComponents();
	}

	public AddValueView(final Tagger tagger, final TaggerView appView,
						final GuiTagModel guiTagModel) {
		this.tagger = tagger;
		this.appView = appView;
		this.tagModel = guiTagModel;
		this.alternateView = null;
		addGuiComponents();
	}

	private class ValueFieldListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (valueField.getJTextArea().getText().equals(new String())) {
				okButton.setEnabled(false);
				okButton.setVisible(false);
			} else {
				okButton.setEnabled(true);
				okButton.setVisible(true);
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
			okButton.setVisible(true);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (valueField.getJTextArea().getText().equals("")) {
				okButton.setEnabled(false);
				okButton.setVisible(false);
			}
		}

	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		double scale = ConstraintLayout.scale;
		g2d.setColor(FontsAndColors.EDITTAG_BG);
		int indent = tagModel.getDepth() + 3;
		g2d.fill(new Polygon(new int[] { (int) (scale * 24 * indent),
				(int) (scale * 24 * indent + scale * 10),
				(int) (scale * 24 * indent + scale * 20) }, new int[] {
				(int) (scale * 10), 0, (int) (scale * 10) }, 3));
		g2d.fill(new Rectangle2D.Double(10 * scale, 10 * scale, getWidth() - 36
				* scale, getHeight() - 15 * scale));
		if (highlight) {
			g2d.setColor(FontsAndColors.TAG_FG_HOVER);
		}
	}

	/**
	 * Populates the combo box that contains the units of the unit classes.
	 * 
	 * @return The units combo box.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateUnitsComboBox() {
		String[] unitClassArray = Tagger.trimStringArray(tagModel
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
		units.setModel(new DefaultComboBoxModel(unitsArray));
		setDefaultUnit();
	}

	/**
	 * Set the units combo box to the default unit.
	 */
	private void setDefaultUnit() {
		String[] unitClassArray = Tagger.trimStringArray(tagModel
				.getUnitClass().split(","));
		String unitClassDefault = tagger.unitClassDefaults
				.get(unitClassArray[0]);
		for (int i = 0; i < units.getItemCount(); i++) {
			if (unitClassDefault.toLowerCase().equals(
					units.getItemAt(i).toString().toLowerCase())) {
				units.setSelectedIndex(i);
				break;
			}
		}
	}

	/**
	 * Adds the units combo box to the view.
	 */
	private void addUnitsComboBox() {
		int unitHeight = HEIGHT + 50;
		top += 25;
		add(unitsLabel, new Constraint("top:" + top
				+ " height:20 left:15 width:50"));
		top += 25;
		add(units,
				new Constraint("top:" + top + " height:26 left:15 right:130"));
		add(new JComponent() {
		}, new Constraint("top:0 height:" + unitHeight + " left:0 right:0"));
	}
	private void addGuiComponents() {
		addContainer();
		addLabels();
		addButtons();
		addUnits();
		execute();
	}

	private void execute() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
                if (AddValueView.this.tagger.isHEDVersionTag(AddValueView.this.tagModel)) {
                    AddValueView.this.valueField.getJTextArea().setText(AddValueView.this.tagger.getHEDVersion());
                }

                AddValueView.this.valueField.getJTextArea().requestFocusInWindow();
			}
		});
	}

	private void addContainer() {
		add(new JComponent() {
		}, new Constraint("top:0 height:80 left:0 width:0"));
	}

	private void addUnits() {
		if (!tagModel.getUnitClass().isEmpty()) {
			populateUnitsComboBox();
			addUnitsComboBox();
		}
	}

	private void addLabels() {
		setLabelBackgroundColors();
		setLabelForegroundColors();
		addLabelListeners();
		valueLabel.setForeground(FontsAndColors.EDITTAG_FG);
		valueField.getJTextArea().getDocument()
				.addDocumentListener(new ValueFieldListener());
		if (tagModel.getParentPath().equals("HED"))
			valueLabel.setText("Name of new HED schema");
		add(valueLabel, new Constraint("top:" + top
				+ " height:20 left:15 width:200"));
		top += 25;
		add(valueField, new Constraint("top:" + top
				+ " height:26 left:15 width:200"));
		top += 30;
		// Add warning when /HED is in add value to warn user save new HED schema to disk
		if (tagModel.getParentPath().equals("HED")) {
			JLabel warningLabel = new JLabel("Warning: 'File > Save HED schema' to save to disk", JLabel.LEFT) {
				@Override
				public Font getFont() {
					return FontsAndColors.contentFont;
				}
			};
			warningLabel.setBackground(FontsAndColors.TRANSPARENT);
			warningLabel.setForeground(FontsAndColors.RED_MEDIUM);
			add(warningLabel, new Constraint("top:" + top + " height:27 left:15 width:500"));
		}
	}

	private void addButtons() {
		okButton.setEnabled(false);
		okButton.setVisible(false);
		setButtonBackgroundColors();
		setButtonForegroundColors();
		addButtonListeners();
		add(okButton, new Constraint("top:10 height:20 right:81 width:35"));
		add(cancelButton, new Constraint("top:10 height:20 right:26 width:50"));
	}

	private void addButtonListeners() {
		/**
		 * When okay button is clicked, it creates a new tag model with the
		 * value typed in and attempts to toggle the tag with the selected tag
		 * groups.
		 */
		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleValueInput();
			}
		});

		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleCancel();
			}
		});
	}

	private void addLabelListeners() {
		valueField.getJTextArea().getDocument()
				.putProperty("filterNewlines", Boolean.TRUE);
		valueField.getJTextArea().getInputMap()
				.put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
		valueField.getJTextArea().getInputMap()
				.put(KeyStroke.getKeyStroke("TAB"), "doNothing");
		valueField.getJTextArea().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleValueInput();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					handleCancel();
				}
			}

		});
	}

	private void setLabelBackgroundColors() {
		valueLabel.setBackground(FontsAndColors.EDITTAG_BG);
	}

	private void setButtonBackgroundColors() {
		okButton.setNormalBackground(FontsAndColors.TRANSPARENT);
		okButton.setHoverBackground(FontsAndColors.TRANSPARENT);
		okButton.setPressedBackground(FontsAndColors.TRANSPARENT);
		cancelButton.setPressedBackground(FontsAndColors.TRANSPARENT);
		cancelButton.setNormalBackground(FontsAndColors.TRANSPARENT);
		cancelButton.setHoverBackground(FontsAndColors.TRANSPARENT);
	}

	private void setLabelForegroundColors() {
		valueField.setForeground(FontsAndColors.GREY_DARK);
	}

	private void setButtonForegroundColors() {
		okButton.setPressedForeground(FontsAndColors.SOFT_BLUE);
		okButton.setNormalForeground(FontsAndColors.SOFT_BLUE);
		okButton.setHoverForeground(Color.BLACK);
		cancelButton.setNormalForeground(FontsAndColors.SOFT_BLUE);
		cancelButton.setHoverForeground(Color.BLACK);
		cancelButton.setPressedForeground(FontsAndColors.SOFT_BLUE);
	}

	private void handleCancel() {
        this.tagModel.setInAddValue(false);
        if (this.tagger.isHEDVersionTag(this.tagModel)) {
            this.appView.updateTagsPanel();
            this.appView.scrollToPreviousTag();
        } else {
			if (this.alternateView != null) {
				this.alternateView.valueAdded((AbstractTagModel) null);
			}
			appView.updateTagsPanel();
		}
	}

	private void handleValueInput() {
        if (this.okButton.isEnabled()) {
            String valueStr = this.valueField.getJTextArea().getText();
            if (this.tagModel.isNumeric()) {
                String unitString = "";
                if (this.units.getSelectedItem() != null) {
                    unitString = this.units.getSelectedItem().toString();
                }

                valueStr = this.validateNumericValue(valueStr.trim(), unitString);
                if (valueStr.isEmpty()) {
                    this.appView.showTaggerMessageDialog(MessageConstants.TAG_UNIT_ERROR, "OK", null, (String)null);
                    return;
				}
            }

            AbstractTagModel transientTag = this.tagger.createTransientTagModel(this.tagModel, valueStr);
            this.tagModel.setInAddValue(false);
            if (this.tagger.isHEDVersionTag(this.tagModel)) {
                this.handleHEDTag(transientTag);
                return;
            }


            if (this.alternateView != null) {
                this.alternateView.valueAdded(transientTag);
			} else {
                GuiTagModel gtm = (GuiTagModel)transientTag;
                gtm.setAppView(this.appView);
				gtm.requestToggleTag();
                this.appView.updateTagsPanel();
                this.appView.updateEventsPanel();
                this.appView.scrollToEventTag((GuiTagModel)transientTag);
			}
		}
	}

	private void handleHEDTag(AbstractTagModel tag) {
		this.tagger.setHedExtended(true);
		this.tagger.setHEDVersion(tag.getName());
		this.appView.updateHEDVersion();
		this.appView.updateTagsPanel();
		this.appView.scrollToPreviousTag();
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
        if (numericValue.matches("^(\\.?[0-9]+)*$")) {
            return unit.isEmpty() ? numericValue : numericValue + " " + unit;
        } else {
            return "";
		}
    }


	/**
	 * Updates the information shown in the view to match the underlying tag
	 * model.
	 */
	public void highlight() {
		highlight = true;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Timer timer = new Timer(2000, taskPerformer);
				timer.setRepeats(false);
				timer.start();
			}
		});
	}

}
