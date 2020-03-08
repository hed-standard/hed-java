package edu.utsa.tagger.gui;

import edu.utsa.tagger.EventModel;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.guisupport.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog used to show report of shared and unique tags of selected events
 *
 * @author Dung Truong, Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@SuppressWarnings("serial")
public class TagSummaryDialog extends JDialog {
	/**
	 * Closes the dialog and returns true.
	 */
	private class OkButtonListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			response = true;
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Component to display a tag's path
	 * containing the tag.
	 */
	private class TagItem extends JComponent {
		private String message;

		private TagItem(String message) {
			this.message = message;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

			Color fg = FontsAndColors.BLUE_DARK;//Color.gray;
			Color bg = FontsAndColors.BLUE_VERY_LIGHT;//Color.white;

			g2d.setColor(bg);
			g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
			double x = getHeight() * 0.4 + 7;
			double y = getHeight() * 0.75;

			g2d.setColor(fg);
			g2d.setFont(FontsAndColors.contentFont);
			g2d.drawString(message, (int) x, (int) y);
		}
	}

	private boolean response;
	ArrayList<String> sharedTags;
	HashMap<String, ArrayList<String>> uniqueTags;
	private JPanel bgPanel = new JPanel();

	private JPanel sharedTagsPanel = new JPanel();
	private JPanel uniqueTagsPanel = new JPanel();
	private XButton okButton;
	private XButton cancelButton;
	private ScrollLayout fileScrollLayout;

	private JLayeredPane fileScrollPane = new JLayeredPane();

	private ScrollLayout uniqueTagsScrollLayout;

	private JLayeredPane uniqueTagsScrollPane = new JLayeredPane();

	/**
	 * Sets up the dialog with the given parameters.
	 *
	 * @param frame
	 *            Frame to show dialog in reference to
	 * @param sharedTags
	 *            List of tags that all events share
	 * @param uniqueTags
	 *            List of tags unique to each event
	 */
	public TagSummaryDialog(JFrame frame, ArrayList<String> sharedTags, HashMap<String, ArrayList<String>> uniqueTags) {
		super(frame, "Tag Summary", true);
		this.sharedTags = sharedTags;
		this.uniqueTags = uniqueTags;

		bgPanel.setLayout(new ConstraintLayout());
		bgPanel.setBackground(FontsAndColors.DIALOG_BG);
		bgPanel.setPreferredSize(new Dimension(700, 650));

		// add event list section
        JTextArea eventListPanel = new JTextArea(1,10) {
        	@Override
			public Font getFont() {
				return FontsAndColors.contentFont;
			}
		};
        eventListPanel.setEditable(false);
        eventListPanel.append("Events: " + String.join(", ", uniqueTags.keySet()));
		eventListPanel.setBackground(FontsAndColors.DIALOG_BG);
		eventListPanel.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
		eventListPanel.setLineWrap(true);
		eventListPanel.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(eventListPanel);
		bgPanel.add(scrollPane, new Constraint(
				"top:10 height:40 left:20 right:20"));


//		ScrollLayout eventListScrollLayout;
//		JLayeredPane eventListScrollPane = new JLayeredPane();
//		eventListScrollLayout = new ScrollLayout(eventListScrollPane, eventListPanel);
//		eventListScrollPane.setLayout(eventListScrollLayout);
//		bgPanel.add(eventListScrollPane, new Constraint(
//				"top:30 height:200 left:20 right:20"));

		// add shared tags section
		JLabel sharedTagsLabel = new JLabel(){
			@Override
			public Font getFont(){
				return FontsAndColors.headerFont.deriveFont(Font.BOLD);
			}
		};
		sharedTagsLabel.setText("Tags shared by all events:");
		sharedTagsLabel.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
		bgPanel.add(sharedTagsLabel, new Constraint(
				"top:60 height:30 left:20 right:10"));
		sharedTagsPanel.setLayout(new ConstraintLayout());
		sharedTagsPanel.setBackground(FontsAndColors.BLUE_VERY_LIGHT);
		sharedTagsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		addSharedTags();
		fileScrollLayout = new ScrollLayout(fileScrollPane, sharedTagsPanel);
		fileScrollPane.setLayout(fileScrollLayout);
		bgPanel.add(fileScrollPane, new Constraint(
					"top:90 height:200 left:20 right:20"));

		// add unique tags section
		JLabel uniqueTagsLabel = new JLabel() {
			@Override
			public Font getFont() {
				return FontsAndColors.headerFont.deriveFont(Font.BOLD);
			}
		};
		uniqueTagsLabel.setText("Tags unique to each event:");
		uniqueTagsLabel.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
		bgPanel.add(uniqueTagsLabel, new Constraint(
				"top:300 height:30 left:20 right:10"));
		uniqueTagsPanel.setLayout(new ConstraintLayout());
		uniqueTagsPanel.setBackground(FontsAndColors.BLUE_VERY_LIGHT);
		uniqueTagsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		addUniqueTags();
		uniqueTagsScrollLayout = new ScrollLayout(uniqueTagsScrollPane, uniqueTagsPanel);
		uniqueTagsScrollPane.setLayout(uniqueTagsScrollLayout);
		bgPanel.add(uniqueTagsScrollPane, new Constraint(
				"top:330 height:250 left:20 right:20"));

		// add options buttons
		okButton = TaggerView.createMenuButton("Ok");
		okButton.addMouseListener(new OkButtonListener());
		bgPanel.add(okButton, new Constraint(
				"bottom:10 height:30 right:5 width:120"));

		getContentPane().add(bgPanel);
		pack();
		setLocationRelativeTo(frame);
	}

	/**
	 * Adds shared tags to the dialog.
	 */
	private void addSharedTags() {
		int top = 10;

		if (sharedTags.size() > 0) {
			for (String tag : sharedTags) {
				sharedTagsPanel.add(new TagItem(tag), new Constraint("top:" + top
						+ " height:30 left:10 right:10"));
				top += 30;
			}
		}
		else {
			JLabel none = new JLabel("None");
			none.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
			sharedTagsPanel.add(none, new Constraint("top:" + top
					+ " height:30 left:10 right:10"));
		}
	}

	/**
	 * Adds shared tags to the dialog.
	 */
	private void addUniqueTags() {
		int top = 10;
		for (Map.Entry<String, ArrayList<String>> entry : uniqueTags.entrySet()) {
		    JLabel eventTitle = new JLabel("Event " + entry.getKey() + ": ") {
				@Override
				public Font getFont() {
					return FontsAndColors.contentFont.deriveFont(Font.BOLD);
				}
			};
		    eventTitle.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
			uniqueTagsPanel.add(eventTitle, new Constraint("top:" + top
					+ " height:30 left:10 right:10"));
			top += 30;
			if (entry.getValue().size() > 0) {
				for (String tag : entry.getValue()) {
					uniqueTagsPanel.add(new TagItem(tag), new Constraint("top:" + top
							+ " height:30 left:20 right:10"));
					top += 30;
				}
			}
			else {
				JLabel none = new JLabel("None");
				none.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
				uniqueTagsPanel.add(none, new Constraint("top:" + top
						+ " height:30 left:20 right:10"));
			}
		}
	}

	/**
	 * Shows the dialog on the screen
	 * 
	 * @return The user's response. True for the "Okay" option and false for the
	 *         "Cancel" option.
	 */
	public boolean showDialog() {
		setVisible(true);
		return response;
	}
}
