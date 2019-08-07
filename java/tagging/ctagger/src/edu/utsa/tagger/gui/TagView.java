package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.gui.ContextMenu.ContextMenuAction;
import edu.utsa.tagger.guisupport.ClickDragThreshold;
import edu.utsa.tagger.guisupport.ConstraintLayout;

/**
 * View used to display a tag in the hierarchy.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
@SuppressWarnings("serial")
public class TagView extends JComponent implements MouseListener {

	private static final String UNCOLLAPSED = "\ue011";
	private static final String COLLAPSED = "\ue00f";

	private final Tagger tagger;
	private final TaggerView appView;
	private final GuiTagModel model;
	private boolean highlight = false;

	private boolean hover = false;
	private boolean pressed = false;

	private Color fg = null;
	private Color bg = null;
	private Font font = FontsAndColors.contentFont;
	private String toolTip;

	private Rectangle collapserBounds = new Rectangle(0, 0, 0, 0);
	ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			TagView.this.highlight = false;
			TagView.this.repaint();
		}
	};

	public TagView(Tagger tagger, TaggerView appview, GuiTagModel model) {
		this.tagger = tagger;
		this.appView = appview;
		this.model = model;
		setLayout(null);
		setOpaque(true);
		addMouseListener(this);
		new ClickDragThreshold(this);
		update();
	}

	/**
	 * Creates a context menu to display where the mouse was clicked on the
	 * screen with options to edit the tag, create a new child tag, or delete
	 * the tag.
	 * 
	 * @param e
	 */
	private void displayContextMenu(MouseEvent e) {
        Map<String, ContextMenuAction> map = new LinkedHashMap();
        if (this.tagger.getExtensionsAllowed() && this.tagger.isExtensionTag(this.model)) {
			map.put("add tag", new ContextMenuAction() {
				@Override
				public void doAction() {
					AbstractTagModel newTag = tagger.addNewTag(model, new String());
					GuiTagModel newGuiTag = (GuiTagModel) newTag;
					if (newTag != null) {
						newGuiTag.setFirstEdit(true);
                        TagView.this.model.setCollapsed(false);
                        TagView.this.appView.updateTagsPanel();
                        TagView.this.appView.scrollToTagEdit(newTag);
                    }

					}
            });
        }

        if (this.tagger.getExtensionsAllowed() && this.tagger.isNewTag(this.model)) {
            map.put("edit", new ContextMenuAction() {
                public void doAction() {
                    TagView.this.model.setInEdit(true);
                    TagView.this.appView.updateTagsPanel();
                    TagView.this.appView.scrollToTag(TagView.this.model);
				}
			});
			map.put("delete", new ContextMenuAction() {
				@Override
				public void doAction() {
					int delete = 0;
					if (model.isCollapsable()) {
						delete = appView.showTaggerMessageDialog(MessageConstants.TAG_DELETE_WARNING, "Okay", "Cancel",
								null);
					}
					if (delete == 0) {
                        TagView.this.tagger.deleteTag(TagView.this.model);
                        TagView.this.tagger.setHedExtended(true);
                        TagView.this.appView.updateTagsPanel();
                        TagView.this.appView.updateEventsPanel();
					}
				}
			});
		}
		appView.showContextMenu(map);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(0, (int) (24 * ConstraintLayout.scale));
	}

	/**
	 * When the right mouse button is clicked, it displays a context menu. When
	 * the left mouse button is clicked, if the collapse arrow was clicked, it
	 * collapses or uncollapses this tag's children. Otherwise, if the tag takes
	 * values, it opens the view to add values, and if not, it attempts to
	 * toggle the tag.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e) && !"~".equals(model.getName())) {
			displayContextMenu(e);
		} else if (SwingUtilities.isLeftMouseButton(e)) {
            if (this.model.isCollapsable() && this.collapserBounds.contains(e.getPoint())) {
                this.model.setCollapsed(!this.model.isCollapsed());
                this.appView.updateTagsPanel();
            } else if (!this.model.takesValue() && !this.model.isNumeric()) {
                this.model.requestToggleTag();
                this.appView.scrollToEventTag(this.model);
			} else {
                this.model.setInAddValue(true);
                this.appView.updateTagsPanel();
                this.appView.scrollToTagAddIn(this.model);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		hover = !collapserBounds.contains(e.getPoint());
		repaint();
		appView.repaintTagsScrollPane();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hover = false;
		repaint();
		appView.repaintTagsScrollPane();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		pressed = !collapserBounds.contains(e.getPoint());
		repaint();
		appView.repaintTagsScrollPane();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressed = false;
		repaint();
		appView.repaintTagsScrollPane();
	}

	@Override
	protected void paintComponent(Graphics g) {
		// Set foreground
		fg = FontsAndColors.TAG_FG_NORMAL;
		try {
			Field f = FontsAndColors.class.getField(model.getHighlight().toString().toUpperCase());
			bg = (Color) f.get(bg);
			if (pressed || highlight)
				bg = FontsAndColors.BLUE_VERY_LIGHT;
		} catch (Exception ex) {
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		g2d.setColor(bg);
		g2d.fill(new Rectangle2D.Double(0, 0, getWidth() - 16, getHeight()));

		double x = 0;
		double y = getHeight() * 0.75;

		x = getHeight() * (model.getDepth() + 1);

		if (model.isCollapsable()) {
			g2d.setColor(fg);
			g2d.setFont(FontsAndColors.symbolFont);
			double xBound;
			if (model.isCollapsed()) {
				g2d.drawString(COLLAPSED, (int) x, (int) y);
				xBound = x + (g2d.getFontMetrics().stringWidth(COLLAPSED) - getHeight()) / 2;
			} else {
				g2d.drawString(UNCOLLAPSED, (int) x, (int) y);
				xBound = x + (g2d.getFontMetrics().stringWidth(UNCOLLAPSED) - getHeight()) / 2;
			}
			collapserBounds.setBounds((int) xBound, 0, getHeight(), getHeight());
		} else {
			collapserBounds.setBounds(0, 0, 0, 0);
		}
		x += getHeight();

		if (hover || highlight) {
			g2d.setColor(FontsAndColors.TAG_FG_HOVER);
		} else if (pressed) {
			g2d.setColor(FontsAndColors.TAG_FG_PRESSED);
		} else {
			g2d.setColor(fg);
		}

		g2d.setFont(font);
		g2d.drawString(model.getName() + " ", (int) x, (int) y);

		String info = null;
		if (model.isChildRequired()) {
			info = "(child required)";
		}

		if (info != null) {
			x += g2d.getFontMetrics().stringWidth(model.getName() + " ");
			x += getHeight();
			g2d.drawString(info, (int) x, (int) y);
		}
	}

	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * Updates tag view to reflect information currently in the underlying tag
	 * model.
	 */
	public void update() {
		if (model.takesValue() || model.isNumeric()) {
			toolTip = MessageConstants.TAKES_VALUE;
		}
		setToolTipText(toolTip);
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
