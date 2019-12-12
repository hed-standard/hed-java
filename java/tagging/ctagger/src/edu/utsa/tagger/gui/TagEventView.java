package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.TaggerSet;
import edu.utsa.tagger.gui.ContextMenu.ContextMenuAction;
import edu.utsa.tagger.guisupport.ClickDragThreshold;
import edu.utsa.tagger.guisupport.ConstraintLayout;
import edu.utsa.tagger.guisupport.FontsAndColors;
import edu.utsa.tagger.guisupport.MessageConstants;

/**
 * View used to display a tag in the events panel.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@SuppressWarnings("serial")
public class TagEventView extends JComponent implements MouseListener {

	private final int MAX_GROUP_TILDES = 2;
	private final Tagger tagger;
	private final TaggerView appView;
	private final Integer groupId;
	private final GuiTagModel model;
	private String text;
	private boolean hover = false;;
	private boolean pressed = false;
	private boolean highlight = false;
	private DeleteTagComponent deleteTag;
	private MaskForMissingTag maskForMissingTag;

	public TagEventView(Tagger tagger, TaggerView appView, Integer groupId, GuiTagModel model, boolean nameOnly) {
		this.tagger = tagger;
		this.appView = appView;
		this.groupId = groupId;
		this.model = model;
		if (nameOnly) {
			text = model.getName();
		} else {
			text = model.getPath();
		}
		deleteTag = new DeleteTagComponent();
		if (model.isMissing())
			maskForMissingTag = new MaskForMissingTag();
		setLayout(null);
		addMouseListener(this);
		new ClickDragThreshold(this);
	}

	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * When the left mouse button is clicked, it scrolls to and highlights the
	 * tag in the hierarchy. When the right mouse button is clicked, it shows a
	 * context menu containing the option to remove the tag.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
            this.appView.updateTagsPanel();
            this.appView.scrollToTag(this.model);
		} else if (SwingUtilities.isRightMouseButton(e)) {
            Map<String, ContextMenuAction> map = new LinkedHashMap();
            TaggedEvent taggedEvent = this.tagger.getEventByGroupId(this.groupId);
            if (taggedEvent.getEventLevelId() != this.groupId) {
				map.put("add ~ before", new ContextMenuAction() {
					@Override
					public void doAction() {
						TaggedEvent taggedEvent = tagger.getEventByGroupId(groupId);
						int index = taggedEvent.findTagIndex(groupId, model);
						addTilde(index);
					}
				});
				map.put("add ~ after", new ContextMenuAction() {
					@Override
					public void doAction() {
						TaggedEvent taggedEvent = tagger.getEventByGroupId(groupId);
						int index = taggedEvent.findTagIndex(groupId, model) + 1;
						addTilde(index);
					}
				});
			}
			if (model.isMissing()) {
				map.put("edit", new ContextMenuAction() {
					public void doAction() {
						model.setInEdit(true);
						appView.updateEventsPanel();
					}
				});
			}
			map.put("remove", new ContextMenuAction() {
				@Override
				public void doAction() {
					if ("Event/Label/".equals(model.getParentPath())) {
						TaggerSet<TaggedEvent> taggedEvents = tagger.getEventSet();
						for (TaggedEvent taggedEvent : taggedEvents) {
							if (taggedEvent.containsTagInGroup(groupId, model)) {
								tagger.editEventCodeLabel(taggedEvent, model, taggedEvent.getEventModel().getCode(),
										new String());
							}
						}
					} else {
						Set<Integer> groupIds = new LinkedHashSet<Integer>();
						groupIds.add(groupId);
						tagger.unassociate(model, groupIds);
					}
					appView.updateEventsPanel();
				}
			});
			appView.showContextMenu(map);
		}
	}

	private void addTilde(int index) {
		int numTildes = 0;
		TaggedEvent taggedEvent = tagger.getEventByGroupId(groupId);
		if (taggedEvent != null) {
			numTildes = taggedEvent.findGroupTildeCount(groupId);
		}
		if (numTildes < MAX_GROUP_TILDES) {
			GuiTagModel newTag = (GuiTagModel) tagger.getFactory().createAbstractTagModel(tagger);
			newTag.setPath("~");
			newTag.setAppView(appView);
			Set<Integer> groupSet = new HashSet<Integer>();
			groupSet.add(groupId);
			tagger.associate(newTag, index, groupSet);
			appView.updateEventsPanel();
			appView.scrollToEventTag(newTag);
		} else {
			appView.showTaggerMessageDialog(MessageConstants.TILDE_ERROR, "Ok", null, null);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		hover = true;
		deleteTag.highlight = true;
		repaint();
		deleteTag.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hover = false;
		deleteTag.highlight = false;
		repaint();
		deleteTag.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressed = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressed = false;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		Color bg;
		Color fg;
		Font font1 = FontsAndColors.contentFont;

		bg = FontsAndColors.EVENTTAG_BG_NORMAL;
		if (pressed && hover) {
			fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_PRESSED : FontsAndColors.EVENTTAG_FG_PRESSED;
		} else if (!pressed && hover) {
			fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_HOVER : FontsAndColors.EVENTTAG_FG_HOVER;
		} else {
			fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_NORMAL : FontsAndColors.EVENTTAG_FG_NORMAL;
		}

		if (highlight) {
			fg = FontsAndColors.EVENTTAG_FG_HOVER;
			bg = FontsAndColors.EVENT_BG_PRESSED;
		}

		if (bg != null) {
			g2d.setColor(bg);
			g2d.fill(SwingUtilities.calculateInnerArea(this, null));
		}

		if (fg != null) {
			double x = 10 * ConstraintLayout.scale;
			double y = g2d.getFontMetrics().getHeight();

			g2d.setColor(fg);
			g2d.setFont(font1);
			g2d.drawString(text + " ", (int) x, (int) y);
		}

	}

	ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			highlight = false;
			repaint();
		}
	};

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

	public DeleteTagComponent getDelete() {
		return deleteTag;
	}

	public MaskForMissingTag getMaskForMissingTag() {
		return maskForMissingTag;
	}

	private class DeleteTagComponent extends JComponent implements MouseListener{
		boolean pressed;
		boolean hover;
		boolean highlight;

		public DeleteTagComponent() {
			setLayout(null);
			addMouseListener(this);
			setToolTipText("Click 'X' to remove tag");
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			hover = true;
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			hover = false;
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			pressed = true;
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			pressed = false;
			repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				if ("Event/Label/".equals(model.getParentPath())) {
					TaggerSet<TaggedEvent> taggedEvents = tagger.getEventSet();
					for (TaggedEvent taggedEvent : taggedEvents) {
						if (taggedEvent.containsTagInGroup(groupId, model)) {
							tagger.editEventCodeLabel(taggedEvent, model, taggedEvent.getEventModel().getCode(),
									new String());
						}
					}
				} else {
					Set<Integer> groupIds = new LinkedHashSet<Integer>();
					groupIds.add(groupId);
					tagger.unassociate(model, groupIds);
				}
				appView.updateEventsPanel();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

			Color bg;
			Color fg;
			Font font1 = FontsAndColors.BASE_MED_FONT;

			bg = FontsAndColors.EVENTTAG_BG_NORMAL;
			if (pressed && hover) {
				fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_PRESSED : FontsAndColors.EVENTTAG_FG_PRESSED;
			} else if (!pressed && hover) {
				fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_HOVER : FontsAndColors.BLUE_DARK;
			} else {
				fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_NORMAL : FontsAndColors.BLUE_VERY_LIGHT;
			}

			if (highlight) {
				fg = FontsAndColors.BLUE_VERY_LIGHT;
			}

			if (bg != null) {
				g2d.setColor(bg);
				g2d.fill(SwingUtilities.calculateInnerArea(this, null));
			}

			if (fg != null) {
				double x = 10 * ConstraintLayout.scale;
				double y = g2d.getFontMetrics().getHeight();

				g2d.setColor(fg);
				g2d.setFont(font1);
				g2d.drawString("x", (int) x, (int) y);
			}
		}

	}
	private class MaskForMissingTag extends JComponent {
		private String path;
		public MaskForMissingTag() {
			AbstractTagModel ancestorTag = tagger.getTagAncestor(model.getPath());
			if (ancestorTag == null)
				path = "";
			else
				path = ancestorTag.getPath();
		}
		public String getPath() {
			return path;
		}
		@Override
		protected void paintComponent(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

			Color bg;
			Color fg;
			Font font1 = FontsAndColors.contentFont;

			bg = FontsAndColors.EVENTTAG_BG_NORMAL;
//				if (pressed && hover) {
//					fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_PRESSED : FontsAndColors.EVENTTAG_FG_PRESSED;
//				} else if (!pressed && hover) {
//					fg = model.isMissing() ? FontsAndColors.EVENTTAG_FG_MISSING_HOVER : FontsAndColors.EVENTTAG_FG_HOVER;
//				} else {
			fg = FontsAndColors.EVENTTAG_FG_NORMAL;
//				}

//				if (highlight) {
//					fg = FontsAndColors.EVENTTAG_FG_HOVER;
//					bg = FontsAndColors.EVENT_BG_PRESSED;
//				}

			if (bg != null) {
				g2d.setColor(bg);
				g2d.fill(SwingUtilities.calculateInnerArea(this, null));
			}

			if (fg != null) {
				double x = 10 * ConstraintLayout.scale;
				double y = g2d.getFontMetrics().getHeight();

				g2d.setColor(fg);
				g2d.setFont(font1);
				g2d.drawString(path + " ", (int) x, (int) y);
			}

		}

	}
}
