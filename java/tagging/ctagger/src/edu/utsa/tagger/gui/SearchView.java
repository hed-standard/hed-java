package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.Constraint;
import edu.utsa.tagger.guisupport.ConstraintContainer;
import edu.utsa.tagger.guisupport.ConstraintLayout;
import edu.utsa.tagger.guisupport.DropShadowBorder;
import edu.utsa.tagger.guisupport.ListLayout;
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
public class SearchView extends JTextArea{
	/* Fields */
	private JLayeredPane container;
	private TaggedEvent taggedEvent; // Event associated with this search 
	private Constraint constraint;
	private TaggerView appView;
	private Tagger tagger;
	
	private JPanel searchResults = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.white);
			g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
			g2d.setColor(new Color(200, 200, 200));
			g2d.draw(new Line2D.Double(6, 0, 6, getHeight() - 5));
		}
	};
	
	private JScrollPane searchResultsScrollPane; // searchResults is put in a scrollable panel
	
	private int focusedResult = -1;
	
	
	/**
	 *  Constructor
	 */
	public SearchView(TaggerView appView, JLayeredPane container, TaggedEvent taggedEvent, Tagger tagger, Constraint constraint) {
		super("search for tags ...");
		this.appView = appView;
		this.container = container;
		this.taggedEvent = taggedEvent;
		this.tagger = tagger;
		this.constraint = constraint;
		
		/* Initialize GUI component */
		init();
		
		/* Action listener */
		setActionListener();
		
		/* Set up key bindings */
		setKeyListener();

	}
	
	/**
	 * Initialize GUI component
	 */
	private void init() {
		this.getDocument().putProperty("filterNewlines", Boolean.TRUE);
//		this.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
//		this.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "doNothing");
//		this.setText("search for tags ...");
		
		searchResults.setBackground(Color.WHITE);
		searchResults.setBorder(new DropShadowBorder());
		searchResults.setLayout(new ListLayout(0, 0, 0, 0));
		
		searchResultsScrollPane = new JScrollPane(searchResults);
		searchResultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		searchResultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		searchResultsScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		searchResultsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
		searchResultsScrollPane.setVisible(false);
		// searchResultsScrollPane need to be at the top layer.
		this.container.setLayer(searchResultsScrollPane, 1);
		// Constraint is dynamically updated in TaggerView.updateEventsPanel(). Element in eventsPanel is added in a 
		// linear order through a loop with updated top position.
		// We take in the constraint created for the search textfield to add in the search result below the search text
		this.container.add(searchResultsScrollPane,new Constraint("top:" + (Math.round(this.constraint.getTop())+27) + " height:300 left:" +
							Math.round(this.constraint.getLeft()) + " right:0"));
	}
	
	/**
	 * Set action listener
	 */
	private void setActionListener() {
		this.getDocument().addDocumentListener(new DocumentListener() {
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
		this.addFocusListener(new FocusListener() {
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
		searchResults.removeAll();
		focusedResult = -1;
		Set<GuiTagModel> tagModels = tagger.getSearchTags(getText());
		if (tagModels == null || tagModels.isEmpty()) {
			searchResultsScrollPane.setVisible(false);
			return;
		}
		for (GuiTagModel tag : tagModels) {
			searchResults.add(tag.getTagSearchView());
		}
		searchResults.revalidate();
		searchResults.repaint();
		searchResultsScrollPane.setVisible(true);
	}

	/**
	 * Cancels the search, causing any search items displayed to disappear.
	 */
	public void cancelSearch() {
		this.setText(new String());
	}
	
	private void selectAllText() {
		this.selectAll();
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
				if (isFocusOwner()) { // if search textfield is focus owner
					TagSearchView searchResult = null;
					JScrollBar vertical = searchResultsScrollPane.getVerticalScrollBar();
					switch (event.getKeyCode()) {
						case KeyEvent.VK_DOWN:
							if (focusedResult > -1) { 
								searchResult = ((TagSearchView)searchResults.getComponent(focusedResult));  // old component
								searchResult.setHover(false);
							}
							if (focusedResult == searchResults.getComponentCount()-1)
								focusedResult = searchResults.getComponentCount()-1;
							else
								focusedResult++;
							searchResult = ((TagSearchView)searchResults.getComponent(focusedResult));  // new component
							searchResult.setHover(true);
							
							if (focusedResult > 0)
								vertical.setValue(vertical.getValue()+vertical.getUnitIncrement());
							searchResults.repaint();
							break;
						case KeyEvent.VK_UP:
							if (focusedResult > -1) {
								searchResult = ((TagSearchView)searchResults.getComponent(focusedResult));  // old component
								searchResult.setHover(false);
							}
							if (focusedResult <= 0) // first key press or reach beginning
								focusedResult = 0;
							else {
								vertical.setValue(vertical.getValue()-vertical.getUnitIncrement());
								focusedResult--;
							}
							searchResult = ((TagSearchView)searchResults.getComponent(focusedResult));  // new component
							searchResult.setHover(true);
							
							searchResults.repaint();
							break;
						case KeyEvent.VK_ENTER:
							if (focusedResult > -1 && focusedResult < searchResults.getComponentCount()) {
								searchResult = (TagSearchView)searchResults.getComponent(focusedResult);
								searchResult.setPressed(true);
//								appView.updateEventsPanel();
								appView.selectedGroups.clear();
								appView.selectedGroups.add(taggedEvent.getEventGroupId());								
								searchResult.getModel().requestToggleTag();
								appView.scrollToEvent(taggedEvent); // doesn't really work --> Fix later
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
		addKeyListener(keyListener);
		searchResults.addKeyListener(keyListener);
	}
}
