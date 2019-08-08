package edu.utsa.tagger.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.ListLayout;

/**
 * Search input field and search result display panel
 * Takes in the container (eventsPanel), tagger (for tag data), and constraint
 * (to match with the layout specification since constraint is created dynamically in the container scope)
 * @author Dung
 *
 */
public class EventEnterTagView extends JComponent {
    /* Fields */
    private Tagger tagger;
    private TaggerView appView;
    private TaggedEvent taggedEvent;
    private JTextArea jTextArea;
    private JPanel searchResults = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.white);
            g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
            g2d.setColor(new Color(200, 200, 200));
        }
    };
    private JScrollPane searchResultsScrollPane; // searchResults is put in a scrollable panel
    private int focusedResult = -1;

    public EventEnterTagView(Tagger tagger, TaggerView appView) {
        this.tagger = tagger;
        this.appView = appView;
        /* Initialize GUI component */
        jTextArea = new JTextArea("Enter tag ...");
        jTextArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.jTextArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);

        searchResults.setBackground(Color.WHITE);
        searchResults.setLayout(new ListLayout(0, 0, 0, 0));

        searchResultsScrollPane = new JScrollPane(searchResults);
        searchResultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        searchResultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        searchResultsScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        searchResultsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        searchResultsScrollPane.setVisible(false);

        /* Action listener */
        this.jTextArea.getDocument().addDocumentListener(new DocumentListener() {
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
        this.jTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                selectAllText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                searchResultsScrollPane.setVisible(false);
                focusedResult = -1;
            }
        });

        setKeyListener();

    }

    /* Constructor */
    public EventEnterTagView(Tagger tagger, TaggedEvent taggedEvent) {
        this.tagger = tagger;
        this.taggedEvent = taggedEvent;
        /* Initialize GUI component */
        jTextArea = new JTextArea("Enter tag ...");
        jTextArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.jTextArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);

        searchResults.setBackground(Color.WHITE);
        searchResults.setLayout(new ListLayout(0, 0, 0, 0));

        searchResultsScrollPane = new JScrollPane(searchResults);
        searchResultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        searchResultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        searchResultsScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        searchResultsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        searchResultsScrollPane.setVisible(false);

        /* Action listener */
        this.jTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearch();
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
        this.jTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                selectAllText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                searchResultsScrollPane.setVisible(false);
                focusedResult = -1;
            }
        });

        setKeyListener();

    }


    /* Action methods */
    /**
     * Updates the search items displayed to match the current text in the
     * search bar.
     */
    private void updateSearch() {
        searchResults.removeAll();
        searchResults.revalidate();
        Set<GuiTagModel> tagModels = tagger.getSearchTags(jTextArea.getText());
        if (tagModels == null || tagModels.isEmpty()) {
            searchResultsScrollPane.setVisible(false);
            return;
        }
        for (GuiTagModel tag : tagModels) {
            searchResults.add(new TagEnterSearchView(this, tag));
        }
        searchResults.revalidate();
        searchResultsScrollPane.setVisible(true);
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
                if (jTextArea.isFocusOwner()) { // if search textfield is focus owner
                    TagEnterSearchView searchResult = null;
                    JScrollBar vertical = searchResultsScrollPane.getVerticalScrollBar();
                    switch (event.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            if (focusedResult > -1) {
                                searchResult = ((TagEnterSearchView)searchResults.getComponent(focusedResult));  // old component
                                searchResult.setHover(false);
                            }
                            if (focusedResult == searchResults.getComponentCount()-1)
                                focusedResult = searchResults.getComponentCount()-1;
                            else
                                focusedResult++;
                            searchResult = ((TagEnterSearchView)searchResults.getComponent(focusedResult));  // new component
                            searchResult.setHover(true);
                            if (focusedResult > 0)
                                vertical.setValue(vertical.getValue()+vertical.getUnitIncrement());
                            searchResults.repaint();
                            break;
                        case KeyEvent.VK_UP:
                            if (focusedResult > -1) {
                                searchResult = ((TagEnterSearchView)searchResults.getComponent(focusedResult));  // old component
                                searchResult.setHover(false);
                            }
                            if (focusedResult <= 0) // first key press or reach beginning
                                focusedResult = 0;
                            else{
                                vertical.setValue(vertical.getValue() - vertical.getUnitIncrement());
                                focusedResult--;
                            }
                            searchResult = ((TagEnterSearchView)searchResults.getComponent(focusedResult));  // new component
                            searchResult.setHover(true);

                            searchResults.repaint();
                            break;
                        case KeyEvent.VK_ENTER:
                            if (focusedResult > -1 && focusedResult < searchResults.getComponentCount()) {
                                searchResult = (TagEnterSearchView)searchResults.getComponent(focusedResult);
                                GuiTagModel model = searchResult.getModel();
                                if (model.takesValue()) {
                                    TagValueInputDialog dialog = new TagValueInputDialog(EventEnterTagView.this, searchResult);
                                    dialog.setVisible(true);
                                }
                                else {
                                    searchResult.addTagToEvent();
                                }
                            }
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {

            }
        };
        jTextArea.addKeyListener(keyListener);
        searchResults.addKeyListener(keyListener);
    }
    /**
     * Cancels the search, causing any search items displayed to disappear.
     */
    public void cancelSearch() {
        this.jTextArea.setText(new String());
    }

    private void selectAllText() {
        this.jTextArea.selectAll();
    }

    public JTextArea getjTextArea() {
        return jTextArea;
    }

    public JScrollPane getSearchResultsScrollPane() {
        return searchResultsScrollPane;
    }

    public TaggerView getAppView() {
        return appView;
    }

    public void setAppView(TaggerView appView) {
        this.appView = appView;
    }

    public Tagger getTagger() {
        return tagger;
    }

    public TaggedEvent getTaggedEvent() {
        return taggedEvent;
    }
}
