package edu.utsa.tagger.gui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.guisupport.FontsAndColors;
import edu.utsa.tagger.guisupport.ListLayout;

/**
 * Panel for quickly search and enter tag to event through keyboard
 * @author Dung
 *
 */
public class EventEnterTagView extends JComponent {
    /* Fields */
    private Tagger tagger;
    private TaggerView appView;
    private TaggedEvent taggedEvent;
    private JTextArea jTextArea;
    private JScrollPane borderPanel; // containing jTextArea
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
    private String searchBoxPrompt = "Search for tag to enter ...";

    public EventEnterTagView(Tagger tagger, TaggerView appView) {
        this.tagger = tagger;
        this.appView = appView;
        /* Initialize GUI component */
        jTextArea = new JTextArea(searchBoxPrompt);
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jTextArea.setBorder(border);
        this.jTextArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);

        borderPanel = new JScrollPane(jTextArea) {
            @Override
            public Font getFont() {
                return FontsAndColors.contentFont;
            }
        };
        borderPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        borderPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        borderPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
        borderPanel.setBorder(null);

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
                textAreaFocusGained();
            }

            @Override
            public void focusLost(FocusEvent e) {
                textAreaFocusLost();
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
        focusedResult = -1;
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
        searchResults.repaint();
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
                            if (searchResults.getComponentCount() > 0) {
                                if (focusedResult > -1) {
                                    searchResult = ((TagEnterSearchView) searchResults.getComponent(focusedResult));  // old component
                                    searchResult.setHover(false);
                                }
                                if (focusedResult == searchResults.getComponentCount() - 1)
                                    focusedResult = searchResults.getComponentCount() - 1;
                                else
                                    focusedResult++;
                                searchResult = ((TagEnterSearchView) searchResults.getComponent(focusedResult));  // new component
                                searchResult.setHover(true);
                                if (focusedResult > 0)
                                    vertical.setValue(vertical.getValue() + vertical.getUnitIncrement());
                                searchResults.repaint();
                                break;
                            }
                        case KeyEvent.VK_UP:
                            if (searchResults.getComponentCount() > 0) {
                                if (focusedResult > -1) {
                                    searchResult = ((TagEnterSearchView) searchResults.getComponent(focusedResult));  // old component
                                    searchResult.setHover(false);
                                }
                                if (focusedResult <= 0) // first key press or reach beginning
                                    focusedResult = 0;
                                else {
                                    vertical.setValue(vertical.getValue() - vertical.getUnitIncrement());
                                    focusedResult--;
                                }
                                searchResult = ((TagEnterSearchView) searchResults.getComponent(focusedResult));  // new component
                                searchResult.setHover(true);

                                searchResults.repaint();
                                break;
                            }
                        case KeyEvent.VK_ENTER:
                            if (focusedResult > -1 && focusedResult < searchResults.getComponentCount()) {
                                searchResult = (TagEnterSearchView)searchResults.getComponent(focusedResult);
                                searchResult.mouseClickedEvent(tagger,appView);
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

    private void textAreaFocusGained() {
        this.jTextArea.setText("");//.selectAll();
    }

    private void textAreaFocusLost() {
        this.jTextArea.setText(searchBoxPrompt);
        searchResultsScrollPane.setVisible(false);
        focusedResult = -1;
    }

    public JScrollPane getjTextAreaPanel() {
        return borderPanel;
    }

    public JTextArea getjTextArea() {
        return jTextArea;
    }

    public JScrollPane getSearchResultsScrollPane() {
        return searchResultsScrollPane;
    }

    public void setSearchResultsScrollPane(JPanel searchResultsScrollPane1) {

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

    @Override
    public Font getFont() {
        return FontsAndColors.contentFont;
    }
}
