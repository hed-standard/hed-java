package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.Constraint;
import edu.utsa.tagger.guisupport.ConstraintContainer;
import edu.utsa.tagger.guisupport.ConstraintLayout;
import edu.utsa.tagger.guisupport.ScrollLayout;
import edu.utsa.tagger.guisupport.XButton;
import edu.utsa.tagger.guisupport.XCheckBox;
import edu.utsa.tagger.guisupport.XScrollTextBox;
import edu.utsa.tagger.guisupport.XTextBox;
import edu.utsa.tagger.TagXmlModel.PredicateType;

/**
 * View used to edit a tag's information in the hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@SuppressWarnings("serial")
public class TagEditView extends ConstraintContainer {

	public final static int HEIGHT = 225;
	private final Tagger tagger;
	private int top = 15;
	private final TaggerView appView;
	private final GuiTagModel tagModel;
	final JLabel nameLabel = new JLabel("name (required)", JLabel.LEFT) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final XScrollTextBox name = new XScrollTextBox(new XTextBox()) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final JLabel descriptionLabel = new JLabel("description") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	private JLayeredPane descriptionScrollPane = new JLayeredPane();

	ScrollLayout descriptionScrollLayout;
	final XTextBox descriptionField = new XTextBox(3, 0) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final JLabel childRequiredLabel = new JLabel("child required") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	final XCheckBox childRequired = new XCheckBox(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK);

	final JLabel takesValueLabel = new JLabel("takes value") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	final XCheckBox takesValue = new XCheckBox(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK);

	final JLabel isNumericLabel = new JLabel("is numeric") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	final XCheckBox isNumeric = new XCheckBox(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK);

	final JLabel requiredLabel = new JLabel("required") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	final XCheckBox required = new XCheckBox(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK);

	final JLabel recommendedLabel = new JLabel("recommended") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	final XCheckBox recommended = new XCheckBox(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK);;

	final JLabel positionLabel = new JLabel("position") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};
	final XScrollTextBox position = new XScrollTextBox(new XTextBox()) {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final JLabel predicateTypeLabel = new JLabel("predicate type") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	final JComboBox predicateTypes = new JComboBox(new String[] {}) {

	};

    final JLabel unitClassesLabel = new JLabel("unit class") {
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final JComboBox unitClasses = new JComboBox(new String[0]) {
	};

	final JLabel uniqueLabel = new JLabel("unique") {
		@Override
		public Font getFont() {
			return FontsAndColors.contentFont;
		}
	};

	final XCheckBox unique = new XCheckBox(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK);

	final XButton saveButton = new XButton("save") {
		@Override
		public Font getFont() {
			Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
			fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			return FontsAndColors.contentFont.deriveFont(fontAttributes);
		}
	};

	final XButton cancelButton = new XButton("cancel") {
		@Override
		public Font getFont() {
			Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
			fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			return FontsAndColors.contentFont.deriveFont(fontAttributes);
		}
	};
	public TagEditView(Tagger tagger, TaggerView appView, GuiTagModel model) {
		this.tagger = tagger;
		this.appView = appView;
		this.tagModel = model;

		add(nameLabel, new Constraint("top:" + top + " height:20 left:15 width:150"));
		top += 25;
		add(name, new Constraint("top:" + top + " height:26 left:15 right:130"));
		top += 26;
		add(descriptionLabel, new Constraint("top:" + top + " height:20 left:15 width:100"));
		top += 25;
		descriptionScrollLayout = new ScrollLayout(descriptionScrollPane, descriptionField);
		descriptionScrollPane.setLayout(descriptionScrollLayout);
		descriptionField.addCaretListener(new DescriptionCaretListener());
		add(descriptionScrollPane, new Constraint("top:" + top + " height:45 left:15 right:20"));
		top += 50;
		add(childRequired, new Constraint("top:" + top + " height:20 left:15 width:20"));
		add(childRequiredLabel, new Constraint("top:" + top + " height:20 left:40 width:120"));
		add(takesValue, new Constraint("top:" + top + " height:20 left:200 width:20"));
		add(takesValueLabel, new Constraint("top:" + top + " height:20 left:230 width:120"));
		add(positionLabel, new Constraint("top:" + top + " height:20 left:415 width:120"));
		add(position, new Constraint("top:" + top + " height:26 left:475 width:40"));
		top += 25;
		add(required, new Constraint("top:" + top + " height:20 left:15 width:20"));
		add(requiredLabel, new Constraint("top:" + top + " height:20 left:40 width:120"));
		add(recommended, new Constraint("top:" + top + " height:20 left:200 width:20"));
		add(recommendedLabel, new Constraint("top:" + top + " height:20 left:230 width:120"));
		add(predicateTypeLabel, new Constraint("top:" + top + " height:20 left:415 width:120"));
		add(new JComponent() {
		}, new Constraint("top:0 height:" + HEIGHT + " left:0 right:0"));

		top += 25;
		add(unique, new Constraint("top:" + top + " height:20 left:15 width:20"));
		add(uniqueLabel, new Constraint("top:" + top + " height:20 left:40 width:120"));
		add(isNumeric, new Constraint("top:" + top + " height:20 left:200 width:20"));
		add(isNumericLabel, new Constraint("top:" + top + " height:20 left:230 width:120"));
		populatePredicateTypeComboBox();
		add(predicateTypes, new Constraint("top:" + top + " height:20 left:415 width:120"));
		this.populateUnitClassesComboBox();
		this.add(this.unitClasses, new Constraint("top:" + this.top + " height:20 left:630 width:120"));
		add(cancelButton, new Constraint("top:10 height:20 right:20 width:45"));
		add(saveButton, new Constraint("top:10 height:20 right:70 width:35"));

		nameLabel.setBackground(FontsAndColors.EDITTAG_BG);
		name.setForeground(FontsAndColors.GREY_DARK);
		name.getJTextArea().getDocument().putProperty("filterNewlines", Boolean.TRUE);
		name.getJTextArea().getInputMap().put(KeyStroke.getKeyStroke("TAB"), "doNothing");
		name.getJTextArea().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
		descriptionLabel.setBackground(FontsAndColors.EDITTAG_BG);
		descriptionField.setForeground(FontsAndColors.GREY_DARK);
		descriptionField.setWrapStyleWord(true);
		descriptionField.setLineWrap(true);
		descriptionField.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "doNothing");
		descriptionField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
		childRequiredLabel.setBackground(FontsAndColors.EDITTAG_BG);

		position.getJTextArea().getDocument().putProperty("filterNewlines", Boolean.TRUE);
		position.getJTextArea().getInputMap().put(KeyStroke.getKeyStroke("TAB"), "doNothing");
		position.getJTextArea().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
		saveButton.setEnabled(false);
		saveButton.setVisible(false);

		saveButton.setNormalBackground(FontsAndColors.TRANSPARENT);
		saveButton.setNormalForeground(FontsAndColors.SOFT_BLUE);
		saveButton.setHoverBackground(FontsAndColors.TRANSPARENT);
		saveButton.setHoverForeground(Color.BLACK);
		saveButton.setPressedBackground(FontsAndColors.TRANSPARENT);
		saveButton.setPressedForeground(FontsAndColors.SOFT_BLUE);

		saveButton.addMouseListener(new SaveButtonListener());

		cancelButton.setNormalBackground(FontsAndColors.TRANSPARENT);
		cancelButton.setNormalForeground(FontsAndColors.SOFT_BLUE);
		cancelButton.setHoverBackground(FontsAndColors.TRANSPARENT);
		cancelButton.setHoverForeground(Color.BLACK);
		cancelButton.setPressedBackground(FontsAndColors.TRANSPARENT);
		cancelButton.setPressedForeground(FontsAndColors.SOFT_BLUE);

		name.getJTextArea().getDocument().addDocumentListener(new NameFieldListener());

		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleCancel();
			}
		});

		name.getJTextArea().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				name.getJTextArea().selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		descriptionField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				descriptionField.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		position.getJTextArea().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				position.getJTextArea().selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});

		isNumeric.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isNumeric.isChecked() && SwingUtilities.isLeftMouseButton(e)) {
					takesValue.setChecked(true);
					repaint();
				}
			}
		});

		takesValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isNumeric.isChecked() && !takesValue.isChecked() && SwingUtilities.isLeftMouseButton(e)) {
					isNumeric.setChecked(false);
					repaint();
				}
			}
		});

		name.getJTextArea().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					name.getJTextArea().transferFocus();
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSave();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					handleCancel();
				}
			}

		});

		descriptionField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					descriptionField.transferFocus();
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSave();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					handleCancel();
				}
			}

		});

		position.getJTextArea().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					name.getJTextArea().requestFocus();
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSave();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					handleCancel();
				}
			}

		});

		update();
	}

	/**
	 * Populates the combo box that contains the predicate types.
	 * 
	 * @return The predicate type combo box.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populatePredicateTypeComboBox() {
		String[] predicateTypeArray = new String[PredicateType.values().length];
		int i = 0;
		for (PredicateType pt : PredicateType.values()) {
			predicateTypeArray[i++] = pt.toString();
		}
		Arrays.sort(predicateTypeArray);
		predicateTypes.setModel(new DefaultComboBoxModel(predicateTypeArray));
	}


	private void populateUnitClassesComboBox() {
		String[] unitClassesArray = this.tagger.getUnitClasses();
		Arrays.sort(unitClassesArray);
		this.unitClasses.setModel(new DefaultComboBoxModel(unitClassesArray));
	}

	/**
	 * Caret listener to allow automatic scrolling in the description field.
	 */
	private class DescriptionCaretListener implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
			int caret = e.getDot();
			try {
				Rectangle viewCaret = descriptionField.modelToView(caret);
				int scrollPos = 0;
				if (viewCaret != null) {
					scrollPos = viewCaret.y;
				}
				descriptionScrollLayout.scrollTo(scrollPos);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}

	}

	private class NameFieldListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (!name.getJTextArea().getText().trim().isEmpty()) {
				saveButton.setEnabled(true);
				saveButton.setVisible(true);
			} else {
				saveButton.setEnabled(false);
				saveButton.setVisible(false);
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (!name.getJTextArea().getText().trim().isEmpty()) {
				saveButton.setEnabled(true);
				saveButton.setVisible(true);
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (name.getJTextArea().getText().trim().isEmpty()) {
				saveButton.setEnabled(false);
				saveButton.setVisible(false);
			}
		}
	}

	/**
	 * Save data in this view to the tag model and updates the AppView
	 * accordingly. Checks that the information entered is valid.
	 */
	private class SaveButtonListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			handleSave();
		}
	}

	public void handleSave() {
		if (saveButton.isEnabled()) {
			int pos = -1;
			String unitClass = this.unitClasses.isEnabled() ? this.unitClasses.getSelectedItem().toString() : "";
			if (!position.getJTextArea().getText().isEmpty()) {
				try {
					pos = Integer.parseInt(position.getJTextArea().getText());
				} catch (NumberFormatException ex) {
					appView.showTaggerMessageDialog(MessageConstants.TAG_POSITION_ERROR, "Okay", null, null);
					return;
				}
			}
			if (takesValue.isChecked() && !name.getJTextArea().getText().equals("#")) {
				appView.showTaggerMessageDialog(MessageConstants.TAKES_VALUE_ERROR, "Okay", null, null);
				return;
			}
			String nameStr = name.getJTextArea().getText();
			if (nameStr.contains("/")) {
				appView.showTaggerMessageDialog(MessageConstants.TAG_NAME_INVALID, "Okay", null, null);
				return;
			}
			if (tagger.isDuplicate(tagModel.getParentPath() + "/" + nameStr, tagModel)) {
				appView.showTaggerMessageDialog(MessageConstants.TAG_NAME_DUPLICATE, "Okay", null, null);
				return;
			}
			if (required.isChecked() && recommended.isChecked()) {
				appView.showTaggerMessageDialog(MessageConstants.TAG_RR_ERROR, "Okay", null, null);
				return;
			}

            this.tagger.editTag(this.tagModel, this.name.getJTextArea().getText(), this.descriptionField.getText(), this.tagModel.isExtensionAllowed(), this.childRequired.isChecked(), this.takesValue.isChecked(), this.isNumeric.isChecked(), this.required.isChecked(), this.recommended.isChecked(), this.unique.isChecked(), pos, PredicateType.valueOf(this.predicateTypes.getSelectedItem().toString().toUpperCase()), unitClass);
            this.tagModel.setInEdit(false);
            this.tagModel.setFirstEdit(false);
            this.tagger.setChildToPropertyOf();
            this.appView.updateTagsPanel();
            this.appView.scrollToTag(this.tagModel);
            if (!this.tagger.getHEDExtended()) {
                this.appView.showTaggerMessageDialog("The HED has been extended. Please specify the current version", (String)null, "OK", (String)null);
                GuiTagModel hedTag = (GuiTagModel)this.tagger.getTagModel("HED/#");
                hedTag.setInAddValue(true);
                this.tagger.setHedExtended(true);
                this.appView.updateTagsPanel();
                this.appView.scrollToTagAddIn(hedTag);
                this.appView.setPreviousTag(this.tagModel);
			}
		}

    }

	private void handleCancel() {
		tagModel.setInEdit(false);
		if (tagModel.isFirstEdit()) {
			AbstractTagModel parent = tagger.tagFound(tagModel.getParentPath());
			if (parent != null) {
				appView.scrollToTag(parent);
			}
			tagger.deleteTag(tagModel);
		}
		appView.updateTagsPanel();
	}

	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double scale = ConstraintLayout.scale;

		g2d.setColor(FontsAndColors.EDITTAG_BG);
		int indent = tagModel.getDepth() + 3;
		g2d.fill(new Polygon(
				new int[] { (int) (scale * 24 * indent), (int) (scale * 24 * indent + scale * 10),
						(int) (scale * 24 * indent + scale * 20) },
				new int[] { (int) (scale * 10), 0, (int) (scale * 10) }, 3));
		g2d.fill(new Rectangle2D.Double(10 * scale, 10 * scale, getWidth() - 20 * scale, getHeight() - 15 * scale));

	}

	/**
	 * Updates the information shown in the view to match the underlying tag
	 * model.
	 */
	public void update() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
                TagEditView.this.name.getJTextArea().requestFocusInWindow();
                TagEditView.this.name.getJTextArea().setText(TagEditView.this.tagModel.getName());
                TagEditView.this.descriptionField.setText(TagEditView.this.tagModel.getDescription());
                TagEditView.this.childRequired.setChecked(TagEditView.this.tagModel.isChildRequired());
                TagEditView.this.isNumeric.setChecked(TagEditView.this.tagModel.isNumeric());
                TagEditView.this.required.setChecked(TagEditView.this.tagModel.isRequired());
                TagEditView.this.recommended.setChecked(TagEditView.this.tagModel.isRecommended());
                TagEditView.this.unique.setChecked(TagEditView.this.tagModel.isUnique());
                TagEditView.this.setPosition();
                TagEditView.this.setTakesValue();
                TagEditView.this.predicateTypes.setSelectedItem(TagEditView.this.tagModel.getPredicateType().toString());
                TagEditView.this.unitClasses.setEnabled(TagEditView.this.tagModel.isNumeric());
                TagEditView.this.unitClasses.setSelectedItem(TagEditView.this.tagModel.getUnitClass());
                TagEditView.this.repaint();
            }
        });
    }

    private void setTakesValue() {
        if (this.tagModel.isNumeric()) {
            this.takesValue.setChecked(true);
				} else {
            this.takesValue.setChecked(this.tagModel.takesValue());
        }

    }

    private void setPosition() {
        int pos = this.tagModel.getPosition();
        if (pos > 0) {
            this.position.getJTextArea().setText(Integer.toString(pos));
        } else {
            this.position.getJTextArea().setText(new String());
        }

    }

}
