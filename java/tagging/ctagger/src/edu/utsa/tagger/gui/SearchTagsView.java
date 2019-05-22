package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.TaggerSet;
import edu.utsa.tagger.guisupport.Constraint;
import edu.utsa.tagger.guisupport.ConstraintContainer;
import edu.utsa.tagger.guisupport.ConstraintLayout;
import edu.utsa.tagger.guisupport.DropShadowBorder;
import edu.utsa.tagger.guisupport.ListLayout;
import edu.utsa.tagger.guisupport.ScrollLayout;
import edu.utsa.tagger.guisupport.XScrollTextBox;
import edu.utsa.tagger.guisupport.XTextBox;
import edu.utsa.tagger.guisupport.ConstraintContainer.Unit;

/**
 * Search input field and search result display panel
 * Takes in the container (eventsPanel), tagger (for tag data), and constraint
 * (to match with the layout specification since constraint is created dynamically in the container scope)
 * @author Dung
 *
 */
public class SearchTagsView {
	/* Reference Fields */
	private TaggedEvent taggedEvent; // Event associated with this search 
	private TaggerView appView;
	private Tagger tagger;
	
	/* Component fields */
	private JTextArea searchText = new JTextArea("search for tags ...");
	private JPanel searchResultsPanel = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.white);
			g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
			g2d.setColor(new Color(200, 200, 200));
			g2d.draw(new Line2D.Double(6, 0, 6, getHeight() - 5));
		}
	};	
	private JScrollPane searchResultsScrollPane; // searchResultsPanel is put in a scrollable panel	
	private int focusedResult = -1;
	
	
	/**
	 *  Constructor
	 */
	public SearchTagsView(TaggerView appView, TaggedEvent taggedEvent, Tagger tagger) {
		this.appView = appView;
		this.taggedEvent = taggedEvent;
		this.tagger = tagger;
		
		/* Initialize GUI component */
		init();
		
		/* Action listener */
		setActionListener();
		
		/* Set up key bindings */
		setKeyListener();

	}
	
	/* Initialize GUI component */
	private void init() {
		searchText.getDocument().putProperty("filterNewlines", Boolean.TRUE);
		
		searchResultsPanel.setBackground(Color.WHITE);
		searchResultsPanel.setBorder(new DropShadowBorder());
		searchResultsPanel.setLayout(new ListLayout(0, 0, 0, 0));
		
		searchResultsScrollPane = new JScrollPane(searchResultsPanel);
		searchResultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		searchResultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		searchResultsScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		searchResultsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
		searchResultsScrollPane.setVisible(false);
	}
	/**
	 * Add the SearchTagsView (search text field and the search result pane) to container given constraint for search text field.
	 * Search result pane will be put in one layer higher than that of search text field
	 * @param container		a JLayeredPane container to which the search view will be added
	 * @param constraint	constraint specifying the position of the search text field
	 */
	public void addToContainer(JLayeredPane container, Constraint constraint) {
		container.add(searchText, constraint);
		container.setLayer(searchResultsScrollPane, JLayeredPane.getLayer(searchText)+1);
		container.add(searchResultsScrollPane,new Constraint("top:" + (Math.round(constraint.getTop())+27) + " height:300 left:" +
				Math.round(constraint.getLeft()) + " right:0"));
	}
	
	/**
	 * Set action listener
	 */
	private void setActionListener() {
		searchText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSearch();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSearch();
			}
		});
		searchText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				selectAllText();
			}

			@Override
			public void focusLost(FocusEvent e) {
				searchResultsScrollPane.setVisible(false);
			}
		});
	}

	/* Action methods */
	/**
	 * Updates the search items displayed to match the current text in the
	 * search bar.
	 */
	private void updateSearch() {
		searchResultsPanel.removeAll();
		focusedResult = -1;
		Set<GuiTagModel> tagModels = tagger.getSearchTags(searchText.getText());
		if (tagModels == null || tagModels.isEmpty()) {
			searchResultsScrollPane.setVisible(false);
			return;
		}
		for (GuiTagModel tag : tagModels) {
			searchResultsPanel.add(tag.getTagSearchView());
		}
		searchResultsPanel.revalidate();
		searchResultsPanel.repaint();
		searchResultsScrollPane.setVisible(true);
	}

	/**
	 * Cancels the search, causing any search items displayed to disappear.
	 */
	public void cancelSearch() {
		searchText.setText(new String());
	}
	
	private void selectAllText() {
		searchText.selectAll();
	}


	/**
	 * Set key binding for components
	 */
	private void setKeyListener() {
		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (searchText.isFocusOwner()) { // if search textfield is focus owner
					TagSearchView searchResult = null;
					JScrollBar vertical = searchResultsScrollPane.getVerticalScrollBar();
					switch (event.getKeyCode()) {
						case KeyEvent.VK_DOWN:
							if (focusedResult > -1) { 
								searchResult = ((TagSearchView)searchResultsPanel.getComponent(focusedResult));  // old component
								searchResult.setHover(false);
							}
							if (focusedResult == searchResultsPanel.getComponentCount()-1)
								focusedResult = searchResultsPanel.getComponentCount()-1;
							else
								focusedResult++;
							searchResult = ((TagSearchView)searchResultsPanel.getComponent(focusedResult));  // new component
							searchResult.setHover(true);
							
							if (focusedResult > 0)
								vertical.setValue(vertical.getValue()+vertical.getUnitIncrement());
							searchResultsPanel.repaint();
							break;
						case KeyEvent.VK_UP:
							if (focusedResult > -1) {
								searchResult = ((TagSearchView)searchResultsPanel.getComponent(focusedResult));  // old component
								searchResult.setHover(false);
							}
							if (focusedResult <= 0) // first key press or reach beginning
								focusedResult = 0;
							else {
								vertical.setValue(vertical.getValue()-vertical.getUnitIncrement());
								focusedResult--;
							}
							searchResult = ((TagSearchView)searchResultsPanel.getComponent(focusedResult));  // new component
							searchResult.setHover(true);
							
							searchResultsPanel.repaint();
							break;
						case KeyEvent.VK_ENTER:
							if (focusedResult > -1 && focusedResult < searchResultsPanel.getComponentCount()) {
								searchResult = (TagSearchView)searchResultsPanel.getComponent(focusedResult);
								GuiTagModel model = searchResult.getModel();
								if (model.takesValue()) {
									TagValueInputDialog dialog = new TagValueInputDialog(SearchTagsView.this,model);
									dialog.setVisible(true);
								}
								else
									addTagToEvent(model);
							}
							break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		searchText.addKeyListener(keyListener);
		searchResultsPanel.addKeyListener(keyListener);
	}

	/**
	 * Add tag to the event. Scroll to the event and continue 
	 * putting search text field in focus
	 * @param tgevt	the event containing searched tag
	 * @param tagModel	the tag to include in the event
	 */
	public void addTagToEvent(GuiTagModel tagModel) {
		appView.selectedGroups.clear();
		appView.selectedGroups.add(taggedEvent.getEventGroupId());	
		tagModel.requestToggleTag();
		TaggedEvent tgevt = tagger.getTaggedEventFromGroupId(Collections.max(appView.selectedGroups));
		tgevt.getSearchView().requestFocusInWindow();
		appView.scrollToEvent(tgevt);
	}
	
	public void requestFocusInWindow() {
		// TODO Auto-generated method stub
		searchText.requestFocusInWindow();
	}

	public TaggedEvent getTaggedEvent() {
		return taggedEvent;
	}

	public TaggerView getAppView() {
		return appView;
	}

	public Tagger getTagger() {
		return tagger;
	}
	
	/* Getters and Setters */
	
}
