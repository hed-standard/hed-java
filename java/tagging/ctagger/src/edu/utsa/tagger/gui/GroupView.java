package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.ToggleTagMessage;
import edu.utsa.tagger.gui.ContextMenu.ContextMenuAction;
import edu.utsa.tagger.guisupport.*;
import edu.utsa.tagger.guisupport.XCheckBox.StateListener;

/**
 * This class represents the view for a tag group.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 * 
 */
@SuppressWarnings("serial")
public class GroupView extends JComponent implements MouseListener,
		StateListener {

	private final int MAX_GROUP_TILDES = 2;
    private static Tagger tagger;
    private static TaggerView appView;
    private final Integer id;
    private HashMap<AbstractTagModel, TagEventView> tagEventViews = new HashMap();
	private boolean highlight = false;
	private boolean selected = false;
	XCheckBox checkbox;
	LayoutManager layout = new LayoutManager() {
		@Override
		public void addLayoutComponent(String s, Component c) {
		}

		@Override
		public void layoutContainer(Container target) {
			Component c = target.getComponent(0);
			c.setBounds(0,
					(target.getHeight() - c.getPreferredSize().height) / 2,
					c.getPreferredSize().width, c.getPreferredSize().height);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			return null;
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return null;
		}

		@Override
		public void removeLayoutComponent(Component c) {
		}

	};

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
	public GroupView(Tagger tagger, TaggerView appView, Integer id) {
		GroupView.tagger = tagger;
		GroupView.appView = appView;
		this.id = id;
		this.createLayout();
	}

    private void createLayout() {
        this.createCheckbox();
        this.setLayout(this.layout);
        this.addMouseListener(this);
        new ClickDragThreshold(this);
    }

    private void createCheckbox() {
        this.checkbox = new XCheckBox(FontsAndColors.TRANSPARENT, Color.black, FontsAndColors.TRANSPARENT, Color.blue, FontsAndColors.TRANSPARENT, Color.blue) {
            public Dimension getPreferredSize() {
                return new Dimension((int)(ConstraintLayout.scale * 20.0D), (int)(ConstraintLayout.scale * 20.0D));
            }
        };
        this.checkbox.addStateListener(new GroupView.CheckBoxListener());
        this.add(this.checkbox);
    }

    private void handleStateChange() {
        if (appView.isSelected(this.id)) {
            appView.removeSelectedGroup(this.id);
            this.setSelected(false);
        } else {
            appView.addSelectedGroup(this.id);
            this.setSelected(true);
        }

    }

	public Integer getGroupId() {
        return this.id;
	}

	public void addTagEgtView(AbstractTagModel tagModel, TagEventView tagEgtView) {
        this.tagEventViews.put(tagModel, tagEgtView);
	}

	public TagEventView getTagEgtViewByKey(AbstractTagModel tagModel) {
        return (TagEventView)this.tagEventViews.get(tagModel);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
            this.changeState();
		} else if (SwingUtilities.isRightMouseButton(e)) {
            Map<String, ContextMenuAction> map = new LinkedHashMap();
			map.put("add ~", new ContextMenuAction() {
				@Override
				public void doAction() {
                    TaggedEvent taggedEvent = GroupView.tagger.getEventByGroupId(GroupView.this.id);
                    int numTags = taggedEvent.getNumTagsInGroup(GroupView.this.id);
                    GroupView.this.addTilde(numTags);
				}
			});
			map.put("duplicate group", new ContextMenuAction() {
				@Override
				public void doAction() {
				    // Create new group
					TaggedEvent taggedEvent = GroupView.tagger.getEventByGroupId(GroupView.this.id);
					int groupId = tagger.addNewGroup(taggedEvent);
					HashSet<Integer> idSet = new HashSet<>();
					idSet.add(groupId);

					// Copy tags
                    for (AbstractTagModel tagModel : tagEventViews.keySet()) {
						AbstractTagModel duplicateTag = tagger.getFactory().createAbstractTagModel(tagger);
						duplicateTag.setPath(tagModel.getPath());
						GuiTagModel gtm = (GuiTagModel) duplicateTag;
						gtm.setAppView(appView);
						ToggleTagMessage message = tagger.toggleTag(gtm, idSet);
						if (message != null) {
							if (message.rrError) {
								appView.showTaggerMessageDialog(
										MessageConstants.ASSOCIATE_RR_ERROR, "Ok", null, null);
							} else if (message.descendants.size() > 0) {
								appView.showDescendantDialog(message);
							} else if (message.uniqueValues.size() > 0) {
								appView.showUniqueDialog(message);
							} else {
								appView.showAncestorDialog(message);
							}
						}
					}

					appView.updateEventsPanel();
					appView.scrollToNewGroup(taggedEvent, groupId);
				}
			});
			map.put("remove group", new ContextMenuAction() {
				@Override
				public void doAction() {
                    GroupView.tagger.removeGroup(GroupView.this.id);
                    GroupView.appView.updateEventsPanel();
				}
			});
			appView.showContextMenu(map, 105);
		}
	}

	private void addTilde(int index) {
		int numTildes = 0;
        TaggedEvent taggedEvent = tagger.getEventByGroupId(this.id);
		if (taggedEvent != null) {
            numTildes = taggedEvent.findGroupTildeCount(this.id);
		}
		if (numTildes < MAX_GROUP_TILDES) {
			GuiTagModel newTag = (GuiTagModel) tagger.getFactory()
					.createAbstractTagModel(tagger);
			newTag.setPath("~");
			newTag.setAppView(appView);
            Set<Integer> groupSet = new HashSet();
            groupSet.add(this.id);
			tagger.associate(newTag, index, groupSet);
			appView.updateEventsPanel();
			appView.scrollToEventTag(newTag);
		} else {
			appView.showTaggerMessageDialog(MessageConstants.TILDE_ERROR,
					"Okay", null, null);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		if (selected) {
			g2d.setColor(FontsAndColors.GROUP_SELECTED);
		} else {
			g2d.setColor(FontsAndColors.GROUP_UNSELECTED);
		}
		g2d.fill(SwingUtilities.calculateInnerArea(this, null));

		if (highlight) {
			g2d.setColor(FontsAndColors.BLUE_DARK);
		} else {
			g2d.setColor(Color.BLACK);
		}

		double scale = ConstraintLayout.scale;

		g2d.fill(new Rectangle2D.Double(getWidth() - 10 * scale, 0, 1 * scale,
				getHeight() - 1 * scale - 1));
		g2d.fill(new Rectangle2D.Double(getWidth() - 10 * scale, 0, 10 * scale,
				1 * scale));
		g2d.fill(new Rectangle2D.Double(getWidth() - 10 * scale, getHeight()
				- 1 * scale - 1, 10 * scale, 1 * scale));
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		checkbox.setChecked(selected);
		repaint();
	}

    public void changeState() {
        this.handleStateChange();
	}


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

    private class CheckBoxListener implements StateListener {
        private CheckBoxListener() {
        }

        public void changeState() {
            GroupView.this.handleStateChange();
		}
		public void stateChanged() {
			GroupView.this.handleStateChange();
		}
    }
}
