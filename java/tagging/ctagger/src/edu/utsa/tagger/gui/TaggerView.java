package edu.utsa.tagger.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.EventModel;
import edu.utsa.tagger.HistoryItem;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.TaggerLoader;
import edu.utsa.tagger.TaggerSet;
import edu.utsa.tagger.ToggleTagMessage;
import edu.utsa.tagger.gui.ContextMenu.ContextMenuAction;
import edu.utsa.tagger.guisupport.*;

/**
 * This class represents the main Tagger GUI view.
 *
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 * Robbins
 */
@SuppressWarnings("serial")
public class TaggerView extends ConstraintContainer {
    private XButton addEvent = new XButton("Add event") {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
    private XButton addGroup = new XButton(new ImageIcon(getClass().getResource("/img/group_scaled.png"))) {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
//    private XButton addTag = new XButton("Add tag") {
//        @Override
//        public Font getFont() {
//            return FontsAndColors.buttonFont;
//        }
//    };
    private boolean autoCollapse = false;
    private int autoCollapseDepth;
    //private XButton exit = createMenuButton("go exit");
    private XButton cancel = createMenuButton("Cancel");
    /**
     * Creates a collapse button.
     */
    private XButton collapseAll = new XButton("Level 1") {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
    /**
     * Creates a collapse level label.
     */
    private JLabel collapseLabel = new JLabel("Show to Level", JLabel.CENTER) {
        @Override
        public Font getFont() {
            return FontsAndColors.BASE_CONTENT_FONT;
        }
    };
    /**
     * Creates a collapse level text box.
     */
    private XScrollTextBox collapseLevel = new XScrollTextBox(new XTextBox()) {
        @Override
        public Font getFont() {
            return FontsAndColors.contentFont;
        }
    };
    private ContextMenu contextMenu;
    private XCheckBox selectAllorNone = new XCheckBox(FontsAndColors.TRANSPARENT,FontsAndColors.BLUE_DARK, FontsAndColors.TRANSPARENT, FontsAndColors.BLUE_HOVER_MEDIUM, FontsAndColors.TRANSPARENT, FontsAndColors.BLUE_HOVER_MEDIUM) {
        @Override
        public Font getFont() {
            return FontsAndColors.headerFont;
        }
    };
    private JLayeredPane eventsScrollPane = new JLayeredPane();
    private JLabel eventsTitle = new JLabel("<html>Data<br>Events</html>") {
        @Override
        public Font getFont() {
            return FontsAndColors.secondHeaderFont;
        }
    };

    /**
     * Creates a expand button.
     */
    private XButton expandAll = new XButton("Expand") { @Override
        public Font getFont() {
            return FontsAndColors.headerFont;
        }
    };
    private JFrame frame;
    private JLabel hoverMessage = new JLabel();
    private TaggerLoader loader;
    private boolean isStandAloneVersion;
    private Notification notification = new Notification();
    private XButton ok = createMenuButton("Ok");
    private XButton redo = new HistoryButton(new ImageIcon(getClass().getResource("/img/redo_scaled.png")), false);
    private JPanel searchResults = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.white);
            g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
            g2d.setColor(new Color(200, 200, 200));
        }
    };
    private JScrollPane searchResultsScrollPane = new JScrollPane(searchResults);
    private XScrollTextBox searchTags = new XScrollTextBox(new XTextBox()) {
        @Override
        public Font getFont() {
            return FontsAndColors.contentFont;
        }
    };

    private EventEnterTagView eventEnterTagView;
    private Set<Integer> selected = new HashSet<>();
    private Set<Integer> selectedEvents = new HashSet<>();
    private Set<Integer> selectedGroups = new HashSet<Integer>();
    private GuiTagModel previousTag;
    private JComponent shield = new JComponent() {
    };
    private ConstraintContainer splitPaneLeft = new ConstraintContainer();
    private ConstraintContainer splitPaneRight = new ConstraintContainer();
    private Tagger tagger;
    private JFrame schemaFrame;
    private JPanel tagsPanel = new JPanel();
    private JLayeredPane eventsPanel = new JLayeredPane();
    //private boolean startOver;
    //private boolean fMapLoaded;
    //private String fMapPath;
    private ScrollLayout tagsScrollLayout;
    private ScrollLayout eventsScrollLayout;
    private JPanel topPane = new JPanel(new GridLayout(1,3));
    private JLayeredPane splitContainer = new JLayeredPane();
    private JLayeredPane tagsScrollPane = new JLayeredPane();
    private JLabel hedTitle = new JLabel("HED schema:") {
        @Override
        public Font getFont() { return FontsAndColors.contentFont;}
    };
    private JLabel tagsTitle = new JLabel("<html>Schema<br>Tags</html>") {
        @Override
        public Font getFont() {
            return FontsAndColors.secondHeaderFont;
        }
    };
    private ConstraintContainer btnPanel = new ConstraintContainer();
    private XButton undo;
    int hasMissingTag = 0;
//    private JPanel headerPanel = new JPanel();
//    private JPanel warningPanel = new JPanel();
    private JLabel incompatibleTagWarning = new JLabel("<html><div style='text-align: left;'>" + "Red tags violate HED schema!" + "</div></html>") {
        @Override
        public Font getFont() {
            return FontsAndColors.headerFont;
        }
    };
    private JLabel titleLabel = new JLabel() {
        @Override
        public Font getFont() {
            return FontsAndColors.headerFont;
        }
    };
    private Icon icon = new ImageIcon(getClass().getResource("/img/delete_scaled.png"));
    private JButton clearAll = new JButton(icon);
    private XButton copyTo = new XButton(new ImageIcon(getClass().getResource("/img/copy_to_scaled.png"))) {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
    private JButton showSchema = new JButton("HED") {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont.deriveFont(Font.BOLD);
        }
    };
    private XButton tagSummary = new XButton(new ImageIcon(getClass().getResource("/img/report_scaled.png"))) {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
    private XButton toFMap = new XButton(new ImageIcon(getClass().getResource("/img/export_scaled.png"))) {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
    private XButton help = new XButton("User manual") {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };
    private XButton strategyGuide = new XButton("Tagging guide") {
        @Override
        public Font getFont() {
            return FontsAndColors.buttonFont;
        }
    };

    /**
     * Constructor creates the GUI and sets up functionality of the buttons
     * displayed.
     *
     * @param loader
     */
    public TaggerView(TaggerLoader loader) {
        this.loader = loader;
        this.tagger = loader.getTagger();
        this.isStandAloneVersion = loader.checkFlags(16);
        autoCollapseDepth = loader.getInitialDepth();
        createLayout();
        updateHEDVersion();
    }

    public TaggerView(TaggerLoader loader, Tagger tagger, String title) {
        this.loader = loader;
        this.tagger = loader.getTagger();
        this.isStandAloneVersion = loader.checkFlags(16);
        autoCollapseDepth = loader.getInitialDepth();
        createLayout();
        updateHEDVersion();
    }

    public void updateHEDVersion() {
        this.hedTitle.setText("HED schema : " + this.tagger.getHEDVersion());
    }

    public void createLayout() {
        this.createFrame();
        this.setupComponents();
        this.setColors();
        this.setListeners();
        this.addComponents();
        this.updateTagsPanel();
        this.updateEventsPanel();
    }

    private void createFrame() {
        System.out.println("test");
        JMenuBar menuBar = this.createMenuBar();
        this.frame = new JFrame();
        this.frame.setSize(685, 800);
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(0);
        this.frame.setTitle("CTAGGER - " + loader.getTitle());
        this.frame.setJMenuBar(menuBar);
        this.frame.getContentPane().add(this);

        // schema Frame
        this.schemaFrame = new JFrame();
        this.schemaFrame.setSize(512,768);
        this.schemaFrame.setVisible(false);
        this.schemaFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.schemaFrame.setTitle("HED Schema");
    }

    private void setupComponents() {
        titleLabel.setText("<html><div style='text-align: left;'>" + loader.getTitle() + "</div></html>");
//        headerPanel.add(titleLabel, BorderLayout.LINE_START);
        strategyGuide.setToolTipText("Guide on choosing tags to add to events");
        help.setToolTipText("CTAGGER manual");
        try {
//            System.out.println(TaggerView.class.getResourceAsStream("/img/undo_scaled.png"));
            undo = new HistoryButton(new ImageIcon(ImageIO.read(TaggerView.class.getResourceAsStream("/img/undo_scaled.png"))), true);
        }
        catch (Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
            System.err.println(ex.getStackTrace());
        }
    }

    private void setColors() {
        this.setOpaque(true);
        this.setBackground(FontsAndColors.APP_BG);
        this.hedTitle.setForeground(FontsAndColors.BLUE_DARK);
//        this.headerPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
        this.cancel.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.ok.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.topPane.setBackground((FontsAndColors.BLUE_MEDIUM));
        this.btnPanel.setBackground((FontsAndColors.BLUE_MEDIUM));
        this.eventsPanel.setOpaque(true);
        this.eventsPanel.setBackground(FontsAndColors.BLUE_2);
        this.tagsPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
        this.eventsTitle.setForeground(FontsAndColors.BLUE_DARK);
        this.tagsTitle.setForeground(FontsAndColors.BLUE_DARK);
        this.addEvent.setNormalBackground(FontsAndColors.TRANSPARENT);
        this.addEvent.setNormalForeground(FontsAndColors.BLUE_VERY_LIGHT);
        this.addEvent.setHoverBackground(FontsAndColors.TRANSPARENT);
        this.addEvent.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.addEvent.setPressedBackground(FontsAndColors.TRANSPARENT);
        this.addEvent.setPressedForeground(FontsAndColors.BLUE_VERY_LIGHT);
        this.addGroup.setNormalBackground(FontsAndColors.TRANSPARENT);
        this.addGroup.setNormalForeground(FontsAndColors.BLUE_VERY_LIGHT);
        this.addGroup.setHoverBackground(FontsAndColors.TRANSPARENT);
        this.addGroup.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.addGroup.setPressedBackground(FontsAndColors.TRANSPARENT);
        this.addGroup.setPressedForeground(FontsAndColors.BLUE_VERY_LIGHT);
        this.collapseLabel.setBackground(FontsAndColors.TRANSPARENT);
        this.collapseLabel.setForeground(FontsAndColors.BLUE_DARK);
        this.collapseAll.setNormalBackground(FontsAndColors.TRANSPARENT);
        this.collapseAll.setNormalForeground(FontsAndColors.BLUE_VERY_LIGHT);
        this.collapseAll.setHoverBackground(FontsAndColors.TRANSPARENT);
        this.collapseAll.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.collapseAll.setPressedBackground(FontsAndColors.TRANSPARENT);
        this.collapseAll.setPressedForeground(FontsAndColors.BLUE_VERY_LIGHT);
//        this.expandAll.setNormalBackground(FontsAndColors.TRANSPARENT);
//        this.expandAll.setNormalForeground(FontsAndColors.BLUE_VERY_LIGHT);
//        this.expandAll.setHoverBackground(FontsAndColors.TRANSPARENT);
//        this.expandAll.setHoverForeground(FontsAndColors.BLUE_DARK);
//        this.expandAll.setPressedBackground(FontsAndColors.TRANSPARENT);
//        this.expandAll.setPressedForeground(FontsAndColors.BLUE_VERY_LIGHT);
//        this.zoomIn.setHoverForeground(FontsAndColors.BLUE_DARK);
//        this.zoomOut.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.redo.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.undo.setHoverForeground(FontsAndColors.BLUE_DARK);
        this.hoverMessage.setBackground(FontsAndColors.LIGHT_YELLOW);
        this.searchResults.setBackground(Color.WHITE);
        this.incompatibleTagWarning.setForeground(FontsAndColors.RED_MEDIUM);
        this.incompatibleTagWarning.setBackground(FontsAndColors.TRANSPARENT);
        this.titleLabel.setForeground(FontsAndColors.BLUE_DARK);
        this.titleLabel.setBackground(FontsAndColors.TRANSPARENT);

        schemaFrame.getContentPane().setBackground(FontsAndColors.APP_BG);
    }

    private void setListeners() {
        this.collapseLevel.getJTextArea().setText(Integer.toString(this.loader.getInitialDepth()));
        this.collapseLevel.getJTextArea().getDocument().putProperty("filterNewlines", Boolean.TRUE);
        this.ok.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.proceed(e);
                TaggerView.this.loader.setBack(false);
            }
        });
        this.cancel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                askBeforeClose();
                //TaggerView.this.proceed(e);
                //TaggerView.this.loader.setBack(true);
            }
        });
//        this.zoomOut.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                TaggerView.this.zoomOut();
//            }
//        });
//        this.zoomIn.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                TaggerView.this.zoomIn();
//            }
//        });
        this.selectAllorNone.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (selectAllorNone.isChecked()) {
                    selectAllorNone.setNormalBg(FontsAndColors.BLUE_HOVER_MEDIUM);
                    selectAllorNone.setNormalFg(FontsAndColors.BLUE_DARK);
                    selectAllorNone.repaint();
                    TaggerView.this.selectAll();
                }
                else {
                    selectAllorNone.setNormalBg(FontsAndColors.TRANSPARENT);
                    selectAllorNone.setNormalFg(FontsAndColors.BLUE_DARK);
                    selectAllorNone.repaint();
                    TaggerView.this.deselectAll();
                }
            }
        });
        this.addGroup.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.addGroup();
            }
        });
        this.collapseLevel.getJTextArea().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (!TaggerView.this.collapseLevel.getJTextArea().getText().isEmpty()) {
                    int level = 0;

                    try {
                        level = Integer.parseInt(TaggerView.this.collapseLevel.getJTextArea().getText());
                    } catch (NumberFormatException var4) {
                    }

                    if (level > 0) {
                        TaggerView.this.autoCollapseDepth = level;
                        TaggerView.this.autoCollapse = true;
                        TaggerView.this.updateTagsPanel();
                    }
                }

            }

            public void removeUpdate(DocumentEvent e) {
                if (!TaggerView.this.collapseLevel.getJTextArea().getText().isEmpty()) {
                    int level = 0;

                    try {
                        level = Integer.parseInt(TaggerView.this.collapseLevel.getJTextArea().getText());
                    } catch (NumberFormatException var4) {
                    }

                    if (level > 0) {
                        TaggerView.this.autoCollapseDepth = level;
                        TaggerView.this.autoCollapse = true;
                        TaggerView.this.updateTagsPanel();
                    }
                }

            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.collapseAll.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.collapseAll();
            }
        });
//        this.expandAll.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                TaggerView.this.expandAll();
//            }
//        });
        this.notification.getToggleDetailsButton().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.showNotification();
            }
        });
        this.notification.getHideButton().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.hideNotification();
            }
        });
        this.addEvent.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.addEvent();
            }
        });
//        this.addTag.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                TaggerView.this.addTag();
//            }
//        });
        this.searchTags.getJTextArea().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                TaggerView.this.updateSearch();
            }

            public void removeUpdate(DocumentEvent e) {
                TaggerView.this.updateSearch();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
        this.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                TaggerView.this.askBeforeClose();
            }
        });
        this.shield.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.hideContextMenu();
            }
        });
        this.frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                TaggerView.this.resetSplitContainer();
            }
        });
        this.searchTags.getJTextArea().getDocument().putProperty("filterNewlines", Boolean.TRUE);
        this.searchTags.getJTextArea().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
        this.searchTags.getJTextArea().getInputMap().put(KeyStroke.getKeyStroke("TAB"), "doNothing");
        this.searchTags.getJTextArea().setText("Search for existing tags ...");
        this.searchTags.getJTextArea().addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                TaggerView.this.searchTags.getJTextArea().setText("");
            }

            public void focusLost(FocusEvent e) {
                TaggerView.this.searchTags.getJTextArea().setText("Search for existing tags ...");
                TaggerView.this.searchResultsScrollPane.setVisible(false);
            }
        });

        this.clearAll.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
              clearAllTags();
          }
        });
        this.copyTo.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                copyTag();
            }
        });
        this.showSchema.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                schemaFrame.setVisible(true);
            }
        });
        this.tagSummary.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.getTagSummaryOfSelected();
            }
        });
        this.toFMap.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TaggerView.this.saveFieldMapDialog(4);
            }
        });
        this.help.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("http://www.hedtags.org/hed-docs/docs/HEDToolsUserManual.pdf"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    showTaggerMessageDialog("Unable to open in system browser. Access instead at: <br>http://www.hedtags.org/hed-docs","Ok",null,null);
                }
            }
        });
        this.strategyGuide.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("http://www.hedtags.org/hed-docs/docs/HEDTaggingStrategyGuide.pdf"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    showTaggerMessageDialog("Unable to open in system browser. Access instead at: <br>http://www.hedtags.org/hed-docs","Ok",null,null);
                }
            }
        });
    }

    private void addComponents() {
        this.setLayout(new ConstraintLayout());
        this.addOptionComponents();

//        this.topPane.add(warningPanel);
//        this.topPane.add(titleLabel, BorderLayout.LINE_START);

        this.searchResults.setLayout(new ListLayout(0, 0, 0, 0));
        this.searchResultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.searchResultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.searchResultsScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        this.searchResultsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        this.eventEnterTagView = new EventEnterTagView(tagger, this);
        this.tagsPanel.setLayout(new ListLayout(1, 1, 0, 1));
        this.eventsPanel.setLayout(new ConstraintLayout());
        this.eventsScrollLayout = new ScrollLayout(this.eventsScrollPane, this.eventsPanel);
        this.eventsScrollPane.setLayout(this.eventsScrollLayout);
        this.tagsScrollLayout = new ScrollLayout(this.tagsScrollPane, this.tagsPanel);
        this.tagsScrollPane.setLayout(this.tagsScrollLayout);

        this.splitPaneLeft.add(this.eventsTitle, new Constraint("top:0 height:50 left:10 width:100"));
        this.splitPaneLeft.add(this.eventEnterTagView.getjTextAreaPanel(), new Constraint("top:12 height:26 left:130 right:100"));
        this.splitPaneLeft.add(this.eventEnterTagView.getSearchResultsScrollPane(), new Constraint("top:40 bottom:300 left:130 right:0"));
        this.splitPaneLeft.setLayer(this.eventEnterTagView.getSearchResultsScrollPane(), 1);
//        this.splitPaneLeft.add(this.titleLabel, new Constraint("top:55 height:50 left:1 width:500"));
        this.splitPaneLeft.add(this.selectAllorNone, new Constraint("top:55 height:30 left:1 width:20"));
        this.splitPaneLeft.add(this.eventsScrollPane, new Constraint("top:90 bottom:0 left:0 right:5"));

        this.splitPaneRight.add(this.tagsTitle, new Constraint("top:0 height:57 left:5 width:100"));
        this.splitPaneRight.add(this.searchTags, new Constraint("top:12 height:26 left:110 right:100"));
        this.splitPaneRight.add(this.searchResultsScrollPane, new Constraint("top:40 bottom:300 left:110 right:0"));
        this.splitPaneRight.setLayer(this.searchResultsScrollPane, 1);
//        this.splitPaneRight.add(this.expandAll, new Constraint("top:52 height:30 left:215 width:100"));
        this.splitPaneRight.add(this.collapseLabel, new Constraint("top:55 height:30 right:20 width:200"));
        this.splitPaneRight.add(this.collapseLevel, new Constraint("top:53 height:30 right:25 width:30"));
        this.splitPaneRight.add(this.collapseAll, new Constraint("top:57 height:30 right:250 width:100"));
        this.splitPaneRight.add(this.tagsScrollPane, new Constraint("top:90 bottom:0 left:5 right:0"));

        addGroup.setToolTipText("Add tag group to selected event(s)");
        btnPanel.add(addGroup, new Constraint("top:0 left:72 height:33 width:33"));
        copyTo.setToolTipText("Copy tags of selected event to others");
        btnPanel.add(copyTo, new Constraint("top:0 left:106 height:33 width:33"));
        clearAll.setToolTipText("Delete all tags");
        btnPanel.add(clearAll, new Constraint("top:0 left:140 height:33 width:33"));
        tagSummary.setToolTipText("Show summary of tags from selected events");
        btnPanel.add(tagSummary, new Constraint("top:0 left:174 height:33 width:33"));
        toFMap.setToolTipText("Export tags to FieldMap MATLAB structure");
        btnPanel.add(toFMap, new Constraint("top:0 left:208 height:33 width:33"));
        showSchema.setToolTipText("Show HED Schema");
        btnPanel.add(showSchema, new Constraint("top:0 left:242 height:33 width:50"));
        this.add(this.notification, new Constraint("top:10 height:30 left:305 right:245"));
        this.setLayer(this.notification, 1);
        this.notification.setVisible(false);
        this.hoverMessage.setOpaque(true);
        this.hoverMessage.setHorizontalAlignment(0);
        this.add(this.hoverMessage);
        this.setLayer(this.hoverMessage, 2);
        this.hoverMessage.setVisible(false);
        this.add(this.shield);
        this.setLayer(this.shield, 2);
        this.shield.setVisible(false);
        this.add(this.hedTitle, new Constraint("top:0 height:26 right:10 width:150"));
//        this.add(headerPanel, new Constraint("top:0 height:33 left:10 right:0"));
        this.add(btnPanel, new Constraint("top:0 height:33 left:10 right:0"));
        this.add(incompatibleTagWarning, new Constraint("top:33 height:33 left:10 right:0"));
//        this.add(this.splitContainer, new Constraint("top:55 bottom:50 left:10 right:10"));
        this.add(this.splitPaneLeft, new Constraint("top:68 bottom:50 left:10 right:10"));
//        int splitterPos = this.frame.getWidth() / 2;
//        VerticalSplitLayout splitLayout = new VerticalSplitLayout(this.splitContainer, this.splitPaneLeft, this.splitPaneRight, splitterPos);
//        this.splitContainer.setLayout(splitLayout);

        schemaFrame.getContentPane().add(this.splitPaneRight);
    }

    public void updateTagsPanel() {
        this.tagger.updateTagHighlights(true);
        this.searchResultsScrollPane.setVisible(false);
        this.tagsPanel.removeAll();
        String lastVisibleTagPath = null;

        Iterator var3 = this.tagger.getTagSet().iterator();
        while (true) {
            AbstractTagModel tagModel;
            GuiTagModel guiTagModel;
            do {
                if (!var3.hasNext()) {
                    this.autoCollapse = false;
                    this.schemaFrame.getContentPane().validate();
                    this.schemaFrame.getContentPane().repaint();
//                    validate();
//                    repaint();
                    return;
                }

                tagModel = (AbstractTagModel) var3.next();
                guiTagModel = (GuiTagModel) tagModel;
                guiTagModel.setAppView(this);
                guiTagModel.setCollapsable(this.tagger.hasChildTags(guiTagModel));
                if (guiTagModel.isCollapsable() && this.autoCollapse) {
                    guiTagModel.setCollapsed(guiTagModel.getDepth() > this.autoCollapseDepth);
                }
            } while (lastVisibleTagPath != null && tagModel.getPath().startsWith(lastVisibleTagPath));

            lastVisibleTagPath = guiTagModel.isCollapsed() ? guiTagModel.getPath() : null;
            guiTagModel.getTagView().update();
            this.tagsPanel.add(guiTagModel.getTagView());
            if (guiTagModel.isInEdit()) {
                guiTagModel.getTagEditView().update();
                this.tagsPanel.add(guiTagModel.getTagEditView());
            }

            if (guiTagModel.isInAddValue()) {
                this.tagsPanel.add(guiTagModel.getAddValueView());
            }
        }
    }

    public void updateEventsPanel() {
        hasMissingTag = 0; // reset
        pruneSelectedGroups();
        eventsPanel.removeAll();
        int top = 0;

        for (TaggedEvent taggedEvent : tagger.getEventSet()) {
            top = addEvents(taggedEvent, top);
            top = addRRTags(taggedEvent, top);
            top = addOtherTags(taggedEvent, top);
        }
        // add warning to tagger view if there exists tag(s) that are not compatible with schema
        if (hasMissingTag > 0) {
            if (hasMissingTag == 1)
                incompatibleTagWarning.setText("<html><div style='text-align: left;'>" + "Red tag violate HED schema!" + "</div></html>");
//            if (incompatibleTagWarning.getParent() != warningPanel) {
//                warningPanel.add(incompatibleTagWarning, BorderLayout.LINE_START);
//            }
        }
        else {
            this.remove(incompatibleTagWarning);
//            if (incompatibleTagWarning.getParent() == warningPanel)
//                warningPanel.remove(incompatibleTagWarning);
        }
        validate();
        repaint();
    }

    /*** support method for createFrame ***/

    /**
     * Checks if the file load was successful.
     *
     * @param option      The dialog option.
     * @param loadFile    The file selected from the file chooser.
     * @param loadSuccess True if the file was loaded successful, false if otherwise.
     * @return -1 if a file was not selected or load failed, a different value
     * if otherwise.
     */
    private int checkLoadSuccess(int option, File loadFile, boolean loadSuccess) {
        if (loadFile == null)
            return -1;
        if (!loadSuccess) {
            TaggerView.this.showTaggerMessageDialog(MessageConstants.LOAD_ERROR, "Ok", null, null);
            return -1;
        }
        refreshPanels();
        return option;
    }

    /**
     * Checks if the file save was successful.
     *
     * @param option      The dialog option.
     * @param saveFile    The file selected from the file chooser.
     * @param saveSuccess True if the file was saved successful, false if otherwise.
     * @return -1 if a file was not selected or save failed, a different value
     * if otherwise.
     */
    private int checkSaveSuccess(int option, File saveFile, boolean saveSuccess) {
        if (saveFile == null) {
            return -1;
        }
        if (!saveSuccess) {
            TaggerView.this.showTaggerMessageDialog(MessageConstants.SAVE_ERROR, "Ok", null, null);
            return -1;
        }
        return option;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenu subMenu = new JMenu("New");
        JMenuItem subItem = new JMenuItem("Group");
        subItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.addGroup();
            }
        });
        subMenu.add(subItem);
        if (this.isStandAloneVersion) {
            subItem = new JMenuItem("Event");
            subItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    TaggerView.this.addEvent();
                }
            });
            subMenu.add(subItem);
        }

//        if (this.tagger.getExtensionsAllowed()) {
//            subItem = new JMenuItem("Tag");
//            subItem.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    TaggerView.this.addTag();
//                }
//            });
//            subMenu.add(subItem);
//        }
        menu.add(subMenu);

        menu.addSeparator();
        subMenu = new JMenu("Import tagged events");
        subItem = new JMenuItem("From tab-deliminated file (.tsv)");
        subItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTSVDialog(2);
            }
        });
        subMenu.add(subItem);
        subItem = new JMenuItem("From FieldMap MATLAB structure (.mat)");
        subItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFieldMapDialog(4);
            }
        });
        subMenu.add(subItem);
        menu.add(subMenu);


        subMenu = new JMenu("Export tagged events");
        subItem = new JMenuItem("To tab-deliminated file (.tsv)");
        subItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTSVDialog(2);
            }
        });
        subMenu.add(subItem);
        subItem = new JMenuItem("To FieldMap MATLAB structure (.mat)");
        subItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFieldMapDialog(4);
            }
        });
        subMenu.add(subItem);
        menu.add(subMenu);

        menu.addSeparator();
        JMenuItem item = new JMenuItem("Load HED schema");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadHEDDialog(1);
            }
        });
        menu.add(item);
        item = new JMenuItem("Save HED schema");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveHEDDialog(1);
            }
        });
        menu.add(item);

        if (this.isStandAloneVersion) {
            menu.addSeparator();
            item = new JMenuItem("Clear");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    TaggerView.this.clear();
                }
            });
            menu.add(item);
        }

        menu.addSeparator();
        item = new JMenuItem("Exit");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.askBeforeClose();
            }
        });
        menu.add(item);

        menu = new JMenu("Edit");
        menuBar.add(menu);
        item = new JMenuItem("Select All");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.selectAll();
            }
        });
        menu.add(item);
        item = new JMenuItem("Deselect All");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.deselectAll();
            }
        });
        menu.add(item);

        menu.addSeparator();
        item = new JMenuItem("Copy tags of selected event");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.copyTag();
            }
        });
        menu.add(item);

        menu.addSeparator();
        item = new JMenuItem("Delete all tags");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.clearAllTags();
            }
        });
        menu.add(item);

        menu.addSeparator();
        item = new JMenuItem("Get tag summary of selected events");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.getTagSummaryOfSelected();
            }
        });
        menu.add(item);

        menu = new JMenu("View");
        menuBar.add(menu);
//        item = new JMenuItem("Collapse");
//        item.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                TaggerView.this.collapseAll();
//            }
//        });
//        menu.add(item);
//        item = new JMenuItem("Expand");
//        item.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                TaggerView.this.expandAll();
//            }
//        });
//        menu.add(item);
//        menu.addSeparator();
//        item = new JMenuItem("Zoom In");
//        item.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                TaggerView.this.zoomIn();
//            }
//        });
//        menu.add(item);
//        item = new JMenuItem("Zoom Out");
//        item.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                TaggerView.this.zoomOut();
//            }
//        });
//        menu.add(item);
//        menu.addSeparator();
        item = new JMenuItem("Hide Required Tags");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.hideRRTags();
            }
        });
        menu.add(item);
        item = new JMenuItem("Show Required Tags");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.showRRTags();
            }
        });
        menu.add(item);
        menu = new JMenu("History");
        menuBar.add(menu);
        item = new JMenuItem("Undo");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.undoAction();
            }
        });
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Redo");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaggerView.this.redoAction();
            }
        });
        menu.add(item);
        return menuBar;
    }

    private void clear() {
        this.eventsPanel.removeAll();
        this.tagsPanel.removeAll();
        this.tagger.clearLists();
        this.validate();
        this.repaint();
    }

    public int loadHEDDialog(int option) {
        boolean loadSuccess = true;
        File loadFile = this.showFileChooserDialog("Load HED XML", "Load", "Load .xml file", "XML files", new String[]{"xml"});
        if (loadFile != null) {
            loadSuccess = this.tagger.loadHED(loadFile);
            this.updateHEDVersion();
        }

        return this.checkLoadSuccess(option, loadFile, loadSuccess);
    }

    public int loadFieldMapDialog(int option) {
        File fMapFile = this.showFileChooserDialog("Load field map", "Load", "Load .mat file", ".mat files", new String[]{"mat"});
        if (fMapFile != null) {
            this.loader.setFMapPath(fMapFile.getAbsolutePath());
            YesNoDialog dialog = new YesNoDialog(this.frame, "A field map has been opened. Would you like to start over?");
            int startOverOption = dialog.showDialog();
            if (startOverOption == 0) {
                this.loader.setStartOver(true);
            }

            this.loader.setFMapLoaded(true);
            this.loader.setNotified(true);
            this.frame.dispose();
        }

        return option;
    }

    public int loadTaggerDataDialog(int option) {
        boolean loadSuccess = true;
        File loadFile = this.showFileChooserDialog("Load Combined events + HED XML", "Load", "Load .xml file", "XML files", new String[]{"xml"});
        if (loadFile != null) {
            loadSuccess = this.tagger.loadEventsAndHED(loadFile);
        }

        return this.checkLoadSuccess(option, loadFile, loadSuccess);
    }

    public int loadTSVDialog(int option) {
        boolean loadSuccess = true;
        File loadFile = this.showFileChooserDialog("Load Events, tab-delimited text", "Load", "Load .tsv file", "TSV files", new String[]{"tsv", "txt"});
        if (loadFile != null) {
            String[] tabSeparatedOptions = this.showTabSeparatedOptions();
            if (tabSeparatedOptions.length == 3) {
                loadSuccess = this.tagger.loadTabDelimitedEvents(loadFile, Integer.parseInt(tabSeparatedOptions[0].trim()), this.StringToIntArray(tabSeparatedOptions[1]), this.StringToIntArray(tabSeparatedOptions[2]));
            }
        }

        return this.checkLoadSuccess(option, loadFile, loadSuccess);
    }

    private int open() {
        int option = -1;
        if (option == -1) {
            FileFormatDialog dialog = new FileFormatDialog(this.frame, "What kind of data would you like to open?", this.isStandAloneVersion);
            option = dialog.showDialog();
            switch (option) {
                case 1:
                    return this.loadHEDDialog(option);
                case 2:
                    return this.loadTSVDialog(option);
                case 3:
                    return this.loadTaggerDataDialog(option);
                case 4:
                    return this.loadFieldMapDialog(option);
                default:
                    dialog.dispose();
                    return 0;
            }
        } else {
            return option;
        }
    }

    private void redoAction() {
        HistoryItem item = null;
        item = this.tagger.redo();
        this.updateEventsPanel();
        this.updateTagsPanel();
        String hoverText = this.tagger.getRedoMessage();
        this.hoverMessage.setText(hoverText);
        if (item != null) {
            this.historyScroll(item);
        }

    }

    private int save() {
        int option = -1;
        if (option == -1) {
            FileFormatDialog dialog = new FileFormatDialog(this.frame, "In which format would you like to save the data?", this.isStandAloneVersion);
            option = dialog.showDialog();
            switch (option) {
                case 1:
                    return this.saveHEDDialog(option);
                case 2:
                    return this.saveTSVDialog(option);
                case 3:
                    return this.saveTaggerDataDialog(option);
                case 4:
                    return this.saveFieldMapDialog(option);
                default:
                    dialog.dispose();
                    return 0;
            }
        } else {
            return option;
        }
    }

    public int saveHEDDialog(int option) {
        boolean saveSuccess = true;
        File saveFile = this.showFileChooserSaveDialog("Save HED XML", "Save", "Save .xml file", "HED" + this.tagger.getHEDVersion(), "XML files", new String[]{"xml"});
        if (saveFile != null) {
            saveFile = this.addExtensionToFile(saveFile, "xml");
            saveSuccess = this.tagger.saveHED(saveFile);
        }

        return this.checkSaveSuccess(option, saveFile, saveSuccess);
    }

    public int saveFieldMapDialog(int option) {
        File fMapFile = this.showFileChooserSaveDialog("Save field map", "Save", "Save .mat file", ".mat files", new String[]{"xml"});
        if (fMapFile != null) {
            this.loader.setFMapSaved(true);
            this.loader.setFMapPath(fMapFile.getAbsolutePath());
        }

        return option;
    }

    public int saveTaggerDataDialog(int option) {
        boolean saveSuccess = true;
        File saveFile = this.showFileChooserSaveDialog("Save Combined events + HED XML", "Save", "Save .xml file", "XML files", new String[]{"xml"});
        if (saveFile != null) {
            saveFile = this.addExtensionToFile(saveFile, "xml");
            saveSuccess = this.tagger.saveEventsAndHED(saveFile);
        }

        return this.checkSaveSuccess(option, saveFile, saveSuccess);
    }

    public int saveTSVDialog(int option) {
        boolean saveSuccess = true;
        File saveFile = this.showFileChooserSaveDialog("Save Events, tab-delimited text", "Save", "Save .tsv file", "TSV files", new String[]{"tsv"});
        if (saveFile != null) {
            saveFile = this.addExtensionToFile(saveFile, "tsv");
            saveSuccess = this.tagger.saveTSVFile(saveFile);
        }

        return this.checkSaveSuccess(option, saveFile, saveSuccess);
    }

    public File showFileChooserDialog(String dialogTitle, String approveButton, String approveButtonToolTip, String fileExtensionType, String[] fileExtenstions) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setApproveButtonText(approveButton);
        fileChooser.setApproveButtonToolTipText(approveButtonToolTip);
        FileFilter imageFilter = new FileNameExtensionFilter(fileExtensionType, fileExtenstions);
        fileChooser.setFileFilter(imageFilter);
        int returnVal = fileChooser.showOpenDialog(this.frame);
        if (returnVal == 0) {
            File file = fileChooser.getSelectedFile();
            return file;
        } else {
            return null;
        }
    }

    public File showFileChooserDialog(String dialogTitle, String approveButton, String approveButtonToolTip, String defaultFile, String fileExtensionType, String[] fileExtenstions) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setSelectedFile(new File(defaultFile));
        fileChooser.setApproveButtonText(approveButton);
        fileChooser.setApproveButtonToolTipText(approveButtonToolTip);
        FileFilter imageFilter = new FileNameExtensionFilter(fileExtensionType, fileExtenstions);
        fileChooser.setFileFilter(imageFilter);
        int returnVal = fileChooser.showOpenDialog(this.frame);
        if (returnVal == 0) {
            File file = fileChooser.getSelectedFile();
            return file;
        } else {
            return null;
        }
    }

    public File showFileChooserSaveDialog(String dialogTitle, String approveButton, String approveButtonToolTip, String fileExtensionType, String[] fileExtenstions) {
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setApproveButtonText(approveButton);
        fileChooser.setApproveButtonToolTipText(approveButtonToolTip);
        FileFilter imageFilter = new FileNameExtensionFilter(fileExtensionType, fileExtenstions);
        fileChooser.setFileFilter(imageFilter);
        int returnVal = fileChooser.showSaveDialog(this.frame);
        if (returnVal == 0) {
            File file = fileChooser.getSelectedFile();
            return file;
        } else {
            return null;
        }
    }

    public File showFileChooserSaveDialog(String dialogTitle, String approveButton, String approveButtonToolTip, String defaultFile, String fileExtensionType, String[] fileExtenstions) {
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setSelectedFile(new File(defaultFile));
        fileChooser.setApproveButtonText(approveButton);
        fileChooser.setApproveButtonToolTipText(approveButtonToolTip);
        FileFilter imageFilter = new FileNameExtensionFilter(fileExtensionType, fileExtenstions);
        fileChooser.setFileFilter(imageFilter);
        int returnVal = fileChooser.showSaveDialog(this.frame);
        if (returnVal == 0) {
            File file = fileChooser.getSelectedFile();
            return file;
        } else {
            return null;
        }
    }

    public String[] showTabSeparatedOptions() {
        JTextField field1 = new JTextField("1");
        JTextField field2 = new JTextField("1");
        JTextField field3 = new JTextField("2");
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Header Lines:"));
        panel.add(field1);
        panel.add(new JLabel("Event Code Column(s):"));
        panel.add(field2);
        panel.add(new JLabel("Tag Column(s):"));
        panel.add(field3);
        String[] tabSeparatedOptions = new String[0];
        boolean validInput = false;

        int result;
        for (result = 0; !validInput; validInput = this.validateTabSeparatedOptions(result, field1.getText(), field2.getText(), field3.getText())) {
            result = JOptionPane.showConfirmDialog((Component) null, panel, "Tab Separated Options", 2, -1);
        }

        if (result == 0) {
            tabSeparatedOptions = new String[]{field1.getText(), field2.getText(), field3.getText()};
            return tabSeparatedOptions;
        } else {
            return tabSeparatedOptions;
        }
    }

    public boolean validateTabSeparatedOptions(int result, String headerLines, String eventCodeColumn, String TagColumn) {
        String message = new String();
        if (!headerLines.trim().matches("\\s*[0-9]+")) {
            message = message + "* header lines must be a single number greater than or equal to 0\n";
        }

        if (!eventCodeColumn.trim().matches("\\s*[1-9][0-9]*(\\s*,\\s*[1-9][0-9]*)*")) {
            message = message + "* event code column must be a single number greater than or equal to 1 or a comma-separted list of numbers\n";
        }

        if (!TagColumn.trim().matches("0") && !TagColumn.trim().matches("\\s*[1-9][0-9]*(\\s*,\\s*[1-9][0-9]*)*")) {
            message = message + "* tag column must be a single number greater than or equal to 0 or a comma-separted list of numbers\n";
        }

        if (result == 0 && !message.isEmpty()) {
            JOptionPane.showMessageDialog((Component) null, "Error(s):\n" + message);
            return false;
        } else {
            return true;
        }
    }

    /*** support methods for setListeners ***/
    private void addEvent() {
        TaggedEvent event = this.tagger.addNewEvent(new String(), new String());
        event.setAppView(this);
        this.clearSelected();
        this.selected.add(event.getEventLevelId());
        this.selectedEvents.add(event.getEventLevelId());
        event.setInEdit(true);
        event.setInFirstEdit(true);
        this.scrollToEvent(event);
        this.updateEventsPanel();
    }

    private File addExtensionToFile(File file, String extension) {
        return !file.getAbsolutePath().endsWith("." + extension) ? new File(file.getAbsolutePath() + "." + extension) : file;
    }

    private void addGroup() {
        Set<Integer> newSelectedGroups = this.tagger.addNewGroups(this.selected);
        if (newSelectedGroups.size() > 0) {
            this.clearSelected();
            this.selected.addAll(newSelectedGroups);
            this.selectedGroups.addAll(newSelectedGroups);
            this.updateEventsPanel();
        }
    }

//    private void addTag() {
//        AbstractTagModel newTag = this.tagger.addNewTag((AbstractTagModel) null, new String());
//        GuiTagModel newGuiTag = (GuiTagModel) newTag;
//        if (newGuiTag != null) {
//            newGuiTag.setFirstEdit(true);
//            this.updateTagsPanel();
//            this.scrollToTag(newTag);
//        }
//
//    }

    private void askBeforeClose() {
        String message = "Are you sure you want to exit?";
        this.createYesNoDialog(message);
    }

    public void clearSelected() {
        this.selected.clear();
        this.selectedEvents.clear();
        this.selectedGroups.clear();
    }

    private void collapseAll() {
        ScrollLayout layout = (ScrollLayout) this.tagsScrollPane.getLayout();
        layout.scrollTo(0);
        this.autoCollapseDepth = 1;
        this.autoCollapse = true;
        this.collapseLevel.getJTextArea().setText(Integer.toString(this.autoCollapseDepth));
        this.updateTagsPanel();
    }

    private int createYesNoDialog(String message) {
        YesNoDialog dialog = new YesNoDialog(frame, message);
        int option = dialog.showDialog();
        if (option == 0) {
            loader.setNotified(true);
            frame.dispose();
        }
        return option;
    }

    /**
     * Updates the tags panel with the information currently represented by the
     * tagger.
     */
    private void deselectAll() {
        this.clearSelected();
        this.updateEventsPanel();
    }

    private void deleteSelected() {
        Iterator var2 = this.selectedEvents.iterator();

        Integer ids;
        while (var2.hasNext()) {
            ids = (Integer) var2.next();
            TaggedEvent tEvent = this.tagger.getEventByGroupId(ids);
            this.tagger.removeEvent(tEvent);
        }

        var2 = this.selectedGroups.iterator();

        while (var2.hasNext()) {
            ids = (Integer) var2.next();
            this.tagger.removeGroup(ids);
        }

        this.updateEventsPanel();
    }

    private void expandAll() {
        ScrollLayout layout = (ScrollLayout) this.tagsScrollPane.getLayout();
        layout.scrollTo(0);
        this.autoCollapseDepth = this.tagger.getTagLevel();
        this.autoCollapse = true;
        this.collapseLevel.getJTextArea().setText(Integer.toString(this.autoCollapseDepth));
        this.updateTagsPanel();
    }

    /**
     * Hides the context menu on the screen.
     */
    public void hideContextMenu() {
        shield.remove(contextMenu);
        shield.setVisible(false);
    }

    private void hideRRTags() {
        Iterator var2 = this.tagger.getEventSet().iterator();

        while (var2.hasNext()) {
            TaggedEvent taggedEvent = (TaggedEvent) var2.next();
            taggedEvent.setShowInfo(false);
        }

        this.updateEventsPanel();
    }

    private void historyScroll(HistoryItem item) {
        switch (item.type.ordinal()) {
            case 1:
                this.scrollToTag(item.tagModel);
            case 2:
            case 6:
            case 7:
            default:
                break;
            case 3:
                this.scrollToEvent(item.event);
                break;
            case 4:
                this.scrollToEvent(item.event);
                break;
            case 5:
                this.scrollToEventGroup(item.event);
                break;
            case 8:
                this.scrollToTag(item.tagModel);
                break;
            case 9:
                this.scrollToEventTag((GuiTagModel) item.tagModel);
                break;
            case 10:
                this.scrollToEvent(item.event);
                break;
            case 11:
                this.scrollToEventTag((GuiTagModel) item.tagModel);
                break;
            case 12:
                this.scrollToEventTag((GuiTagModel) item.tagModel);
        }

    }


    private void showRRTags() {
        Iterator var2 = this.tagger.getEventSet().iterator();

        while (var2.hasNext()) {
            TaggedEvent taggedEvent = (TaggedEvent) var2.next();
            taggedEvent.setShowInfo(true);
        }

        this.updateEventsPanel();
    }

    private void hideNotification() {
        this.notification.setVisible(false);
    }

    private void proceed(MouseEvent e) {
        XButton button = (XButton) e.getSource();
        if (button.isEnabled()) {
            List<EventModel> missingReqTags = this.tagger.findMissingRequiredTags();
            boolean exit = true;
            if (this.tagger.isPrimary() && !missingReqTags.isEmpty()) {
                exit = this.showRequiredMissingDialog(missingReqTags);
            }

            if (exit) {
                this.loader.setSubmitted(true);
                this.loader.setNotified(true);
                this.frame.dispose();
            }
        }

    }

    private void resetSplitContainer() {
        int splitterPos = this.frame.getWidth() / 2;
        VerticalSplitLayout layout = (VerticalSplitLayout) this.splitContainer.getLayout();
        if (layout != null) {
            layout.setX(splitterPos);
        }

    }

    private void selectAll() {
        for (TaggedEvent taggedEvent : tagger.getEventSet()) {
            this.selected.add(taggedEvent.getEventLevelId());
            this.selectedEvents.add(taggedEvent.getEventLevelId());
//            // add groups
//            Set<Integer> keys = taggedEvent.getTagGroups().keySet();
//            Iterator var5 = keys.iterator();
//
//            while (var5.hasNext()) {
//                Integer key = (Integer) var5.next();
//                this.selected.add(key);
//            }
        }

        this.updateEventsPanel();
    }

    private void showNotification() {
        if (this.notification.getToggleDetailsButton().getText().equals("hide details")) {
            this.notification.getToggleDetailsButton().setText("show details");
            this.setTopHeight(this.notification, 10.0D, Unit.PX, 30.0D, Unit.PX);
            this.setLeftRight(this.notification, 305.0D, Unit.PX, 245.0D, Unit.PX);
        } else if (this.notification.getToggleDetailsButton().getText().equals("show details")) {
            this.notification.getToggleDetailsButton().setText("hide details");
            double detailsHeight = (double) ((float) this.notification.getDetails().getLineCount() * FontsAndColors.BASE_CONTENT_FONT.getSize2D() + 20.0F);
            this.setTopHeight(this.notification, 10.0D, Unit.PX, 30.0D + detailsHeight, Unit.PX);
        }

    }

    /**
     * Updates the search items displayed to match the current text in the
     * search bar.
     */
    private void updateSearch() {
        searchResults.removeAll();
        searchResults.revalidate();
        Set<GuiTagModel> tagModels = tagger.getSearchTags(searchTags.getJTextArea().getText());
        if (tagModels == null || tagModels.isEmpty()) {
            searchResultsScrollPane.setVisible(false);
            return;
        }
        for (GuiTagModel tag : tagModels) {
            searchResults.add(tag.getTagSearchView());
        }
        searchResults.repaint();
        searchResultsScrollPane.setVisible(true);
    }

    /*** support methods for addComponents ***/
    private void addOptionComponents() {
        if (this.isStandAloneVersion) {
            this.splitPaneLeft.add(this.addEvent, new Constraint("top:0 height:50 right:10 width:115"));
        } else {
            this.add(this.cancel, new Constraint("bottom:10 height:30 right:100 width:80"));
            this.add(this.ok, new Constraint("bottom:10 height:30 right:10 width:80"));
            this.add(this.help, new Constraint("bottom:10 height:30 left:10 width:150"));
            this.add(this.strategyGuide, new Constraint("bottom:10 height:30 left:160 width:160"));
        }
    }

    /*** support methods for updateEventsPanel. Add groups and other tags ***/
    private int addOtherTags(TaggedEvent taggedEvent, int top) {
        JLabel label = new JLabel("Other tags") {
            @Override
            public Font getFont() {
                return FontsAndColors.contentFont.deriveFont(Font.BOLD);
            }
        };
        label.setForeground(FontsAndColors.BLUE_DARK);
        eventsPanel.add(label, new Constraint("top:" + top + " height:20 left:5 right:105"));
        top += 23;

        for (Map.Entry<Integer, TaggerSet<AbstractTagModel>> tagGroup : taggedEvent.getTagGroups().entrySet()) {
            top = this.createGroupSpace(taggedEvent, tagGroup, top);
            for (AbstractTagModel tag : (TaggerSet<AbstractTagModel>) tagGroup.getValue()) {
                if (!((Integer) tagGroup.getKey() == taggedEvent.getEventLevelId() && this.tagger.isRRValue(tag) && this.tagger.isPrimary())) {
                    GuiTagModel guiTagModel = (GuiTagModel) tag;
                    guiTagModel.setAppView(this);
                    guiTagModel.updateMissing();
                    TagEventView tagEgtView = guiTagModel.getTagEventView((Integer) tagGroup.getKey());
                    GroupView groupView = taggedEvent.getGroupViewByKey((Integer) tagGroup.getKey());
                    if (groupView == null) {
                        taggedEvent.addTagEgtView(tag, tagEgtView);
                    } else {
                        groupView.addTagEgtView(tag, tagEgtView);
                    }

                    this.eventsPanel.add(tagEgtView, new Constraint("top:" + top + " height:26 left:30 right:40")); // show on GUI
                    if (guiTagModel.isMissing()) {
                        hasMissingTag++;
                        this.eventsPanel.add(tagEgtView.getMaskForMissingTag(), new Constraint("top:" + top + " height:26 left:30 right:40")); // add mask of correct part of the missing tag
                        this.eventsPanel.setLayer(tagEgtView.getMaskForMissingTag(), 1);
                    }
                    this.eventsPanel.add(tagEgtView.getDelete(), new Constraint("top:" + top + " height:26 width:30 right:0")); //add delete 'X' button associated with this tag
                    this.eventsPanel.setLayer(tagEgtView.getDelete(), 1);
                    top += 27;
                    if (guiTagModel.isInEdit()) {
                        TagEventEditView teev = guiTagModel.getTagEventEditView(taggedEvent);
                        teev.setAppView(this);
                        teev.update();
                        this.eventsPanel.add(teev, new Constraint("top:" + top + " height:" + 85 + " left:30 right:0"));
                        top += 85;
                    }
                }
            }
        }

        return top;
    }

    private int addEvents(TaggedEvent taggedEvent, int top) {
        taggedEvent.setAppView(this);
        EventView ev = taggedEvent.getEventView();
        ev.setGroupId(taggedEvent.getEventLevelId());
        if (this.selected.contains(taggedEvent.getEventLevelId())) {
            ev.setSelected(true);
        } else {
            ev.setSelected(false);
        }

        this.eventsPanel.add(ev, new Constraint("top:" + top + " height:30 left:0 width:" + (this.eventsPanel.getWidth() - 15)));
        ev.setCurrentPosition(top);
        top += 31;
        if (taggedEvent.isInEdit()) {
            EventEditView eev = taggedEvent.getEventEditView();
            eev.update();
            this.eventsPanel.add(eev, new Constraint("top:" + top + " height:" + 135));
            top += 135;
        }

        return top;
    }

    private int addRRTags(TaggedEvent taggedEvent, int top) {
        if (this.tagger.isPrimary() && taggedEvent.showInfo() && this.tagger.hasRRTags()) {
            top = this.addRequiredTags(taggedEvent, top);
            top = this.addRecommendedTags(taggedEvent, top);
            top = this.addSeparator(top);
        }

        return top;
    }

//    private int addEventTagSearchPanel(TaggedEvent taggedEvent, int top) {
//        EventEnterTagView evTagSearchView = taggedEvent.getEventEnterTagView();
//        evTagSearchView.setAppView(this);
//
//        this.eventsPanel.add(evTagSearchView.getjTextArea(), new Constraint("top:" + top + " height:26 left:37 right:200"));
//        top += 27;
//        this.eventsPanel.setLayer(evTagSearchView.getSearchResultsScrollPane(), 1);
//        this.eventsPanel.add(evTagSearchView.getSearchResultsScrollPane(), new Constraint("top:" + top + " left:37 right:0 height:200"));
//        top += 5;
//        return top;
//    }

    private int createGroupSpace(TaggedEvent taggedEvent, Map.Entry<Integer, TaggerSet<AbstractTagModel>> tagGroup, int top) {
        if ((Integer) tagGroup.getKey() != taggedEvent.getEventLevelId()) {
            Integer groupId = (Integer) tagGroup.getKey();
            GroupView groupView = new GroupView(this.tagger, this, groupId);
            taggedEvent.addGroupView(groupView);
            if (this.selected.contains(groupId)) {
                groupView.setSelected(true);
            }

            Integer numTagsInGroup = taggedEvent.getNumTagsInGroup(groupId);
            if (numTagsInGroup == 0) {
                this.eventsPanel.add(groupView, new Constraint("top:" + top + " height:27 left:0 width:30"));
                top += 27;
            } else {
                this.eventsPanel.add(groupView, new Constraint("top:" + top + " height:" + numTagsInGroup * 27 + " left:0 width:30"));
            }
        }

        return top;
    }

    private int addRequiredTags(TaggedEvent taggedEvent, int top) {
        int size;
        for (Iterator var4 = this.tagger.getRequiredTags().iterator(); var4.hasNext(); top += size) {
            AbstractTagModel tag = (AbstractTagModel) var4.next();
            RRTagView rrtv = taggedEvent.getRRTagView(tag);
            taggedEvent.addRRTagView(tag, rrtv);
            size = rrtv.getConstraintHeight();
            this.eventsPanel.add(rrtv, new Constraint("top:" + top + " height:" + size));

            // add delete tag 'x' icon to Category tags
            if (rrtv.getKey().getName().equals("Category")) {
                TaggerSet<AbstractTagModel> sets = rrtv.getValues();
                if (sets != null && !sets.isEmpty()) {
                    int y = top+rrtv.getTagSize()*2; // account for 2 lines: tag label (Event/Category) and prompt ("Click to add tag")
                    for (AbstractTagModel tagModel : sets) {
                        TagEventView tgEvtView = rrtv.getTagEgtViewByKey(tagModel);
                        this.eventsPanel.add(tgEvtView.getDelete(), new Constraint("top:" + y + " height:" + rrtv.getTagSize() + " width:30 right:0"));
                        this.eventsPanel.setLayer(tgEvtView.getDelete(), 1);
                        y += rrtv.getTagSize();
                    }
                }
            }
        }

        return top;
    }

    private int addRecommendedTags(TaggedEvent taggedEvent, int top) {
        int size;
        for (Iterator var4 = this.tagger.getRecommendedTags().iterator(); var4.hasNext(); top += size) {
            AbstractTagModel tag = (AbstractTagModel) var4.next();
            RRTagView rrtv = taggedEvent.getRRTagView(tag);
            taggedEvent.addRRTagView(tag, rrtv);
            size = rrtv.getConstraintHeight();
            this.eventsPanel.add(rrtv, new Constraint("top:" + top + " height:" + size));
        }

        return top;
    }

    private int addSeparator(int top) {
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.black);
        separator.setBackground(Color.black);
        this.eventsPanel.add(separator, new Constraint("top:" + top + " height:1 left:15 right:20"));
        top += 5;
        return top;
    }

    /**
     * Updates the set of groups selected for adding tags to include only groups
     * that exist in the EGT set.
     */
    private void pruneSelectedGroups() {
        Iterator<Integer> iter = selectedGroups.iterator();
        while (iter.hasNext()) {
            Integer groupId = iter.next();
            boolean found = false;
            for (TaggedEvent currentTaggedEvent : tagger.getEventSet()) {
                if (currentTaggedEvent.containsGroup(groupId)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                iter.remove();
            }
        }
    }

/************************************************************************/


    /**
     * Creates the menu buttons
     *
     * @param label The menu button label.
     * @return The button object with the specified label.
     */
    public static XButton createMenuButton(String label) {
        XButton button = new XButton(label) {
            @Override
            public Font getFont() {
                return FontsAndColors.buttonFont;
            }
        };
//        button.setNormalBackground(FontsAndColors.MENU_NORMAL_BG);
//        button.setNormalForeground(FontsAndColors.MENU_NORMAL_FG);
//        button.setHoverBackground(FontsAndColors.MENU_HOVER_BG);
//        button.setHoverForeground(FontsAndColors.MENU_HOVER_FG);
//        button.setPressedBackground(FontsAndColors.MENU_PRESSED_BG);
//        button.setPressedForeground(FontsAndColors.MENU_PRESSED_FG);

        return button;
    }

    public GuiTagModel getPreviousTag() {
        return this.previousTag;
    }

    public void setPreviousTag(GuiTagModel previousTag) {
        this.previousTag = previousTag;
    }

    /**
     * Cancels the search, causing any search items displayed to disappear.
     */
    public void cancelSearch() {
        searchTags.getJTextArea().setText(new String());
    }


    /**
     * Shows a file chooser to select a Tagger Data XML file.
     *
     * @param option The dialog option.
     * @return The file returned from the file chooser.
     */
    public int loadTaggerDataXMLDialog(int option) {
        boolean loadSuccess = true;
        File loadFile = showFileChooserDialog("Load Combined events + HED XML", "Load", "Load .xml file", "XML files",
                new String[]{"xml"});
        if (loadFile != null)
            loadSuccess = tagger.loadEventsAndHED(loadFile);
        return checkLoadSuccess(option, loadFile, loadSuccess);
    }


    private void refreshPanels() {
        this.autoCollapse = true;
        this.clearSelected();
        this.updateEventsPanel();
        this.updateTagsPanel();
    }

    /**
     * Repaints the events panel.
     */
    public void repaintEventsPanel() {
        eventsPanel.validate();
        eventsPanel.repaint();
    }

    /**
     * Repaints the events scroll pane.
     */
    public void repaintEventsScrollPane() {
        eventsScrollPane.repaint();
    }

    /**
     * Repaints the tags scroll pane.
     */
    public void repaintTagsScrollPane() {
        tagsScrollPane.repaint();
    }


    /**
     * Updates the notification at the top of the GUI the the given preview and
     * details
     *
     * @param preview Short message to display in notification
     * @param details Details to display when the notification is expanded
     */
    public void updateNotification(String preview, String details) {
        notification.setVisible(preview != null);
        notification.setPreviewText(preview);
        notification.setDetailsText(details);
    }

    public void scrollToEvent(TaggedEvent event) {
        int offset = 100;
        ScrollLayout layout = (ScrollLayout) this.eventsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        EventView eventView = event.getEventView();
        int y = Math.max(0, eventView.getY() - offset);
        layout.scrollTo(y);
        eventView.highlight();
    }


    public void scrollToEventGroup(TaggedEvent event) {
        int offset = event.getEventView().getHeight() + event.findTagCount() * 27;
        ScrollLayout layout = (ScrollLayout) this.eventsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        int y = Math.max(0, event.getEventView().getY() + offset);
        layout.scrollTo(y);
    }

    public void scrollToEventTag(GuiTagModel tag) {
        if (this.selected.size() > 0) {
            int offset = 100;
            int lastSelectedGroup = (Integer) Collections.max(this.selected);
            ScrollLayout layout = (ScrollLayout) this.eventsScrollPane.getLayout();
            this.updateNotification((String) null, (String) null);
            TaggedEvent event = this.tagger.getTaggedEventFromGroupId(lastSelectedGroup);
            if (event.isRRTagDescendant(tag)) {
                AbstractTagModel rrTag = event.findRRParentTag(tag);
                RRTagView rrTagView = event.getRRTagViewByKey(rrTag);
                TagEventView tagEgtView = rrTagView.getTagEgtViewByKey(tag);
                if (rrTagView != null) {
                    int y = Math.max(0, rrTagView.getY() - offset);
                    layout.scrollTo(y);
//                    if (tagEgtView != null) {
//                        tagEgtView.highlight();
//                    }
                }
            } else if (event.getEventLevelId() != lastSelectedGroup) {
                GroupView groupView = event.getGroupViewByKey(lastSelectedGroup);
                TagEventView tagEgtView = groupView.getTagEgtViewByKey(tag);
                if (groupView != null) {
                    int y = Math.max(0, groupView.getY() - offset);
                    layout.scrollTo(y);
//                    if (tagEgtView != null) {
//                        tagEgtView.highlight();
//                    }
                }
            } else {
                TagEventView tagEgtView = event.getTagEgtViewByKey(tag);
                if (tagEgtView != null) {
                    int y = Math.max(0, tagEgtView.getY() - offset);
                    layout.scrollTo(y);
//                    tagEgtView.highlight();
                }
            }
        }

    }


    public void scrollToTag(AbstractTagModel tag) {
        int offset = 100;
        ScrollLayout layout = (ScrollLayout) this.tagsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        AbstractTagModel takeValueTag = this.tagger.getTagModel(tag.getParentPath() + "/#");
        AbstractTagModel parentTag = this.tagger.getExtensionAllowedAncestor(tag.getPath());
        GuiTagModel gtm = (GuiTagModel) tag;
        this.expandToLevel(gtm.getDepth());
        TagView tagView = gtm.getTagView();
        int y = Math.max(0, tagView.getY() - offset);
        if (y == 0) {
            if (takeValueTag != null && takeValueTag.takesValue()) {
                gtm = (GuiTagModel) takeValueTag;
                this.expandToLevel(gtm.getDepth());
                tagView = gtm.getTagView();
                y = Math.max(0, tagView.getY() - offset);
            } else {
                if (parentTag == null) {
                    return;
                }

                gtm = (GuiTagModel) parentTag;
                this.expandToLevel(gtm.getDepth());
                tagView = gtm.getTagView();
                y = Math.max(0, tagView.getY() - offset);
            }
        }

        layout.scrollTo(y);
        tagView.highlight();
    }

    public void scrollToPreviousTag() {
        if (this.previousTag != null) {
            this.scrollToTag(this.previousTag);
        }

    }

    public void expandToLevel(int depth) {
        if (Integer.valueOf(this.collapseLevel.getJTextArea().getText()) < depth) {
            this.autoCollapseDepth = depth;
            this.autoCollapse = true;
            this.collapseLevel.getJTextArea().setText(Integer.toString(this.autoCollapseDepth));
            this.updateTagsPanel();
        }

    }

    public void scrollToLastSelectedGroup() {
        int offset = 100;
        Iterator<Integer> selectedGroupsIterator = this.selected.iterator();

        int lastSelectedGroup;
        for (lastSelectedGroup = 0; selectedGroupsIterator.hasNext(); lastSelectedGroup = (Integer) selectedGroupsIterator.next()) {
        }

        ScrollLayout layout = (ScrollLayout) this.eventsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        TaggedEvent event = this.tagger.getTaggedEventFromGroupId(lastSelectedGroup);
        int y;
        if (event.getEventLevelId() != lastSelectedGroup) {
            GroupView groupView = event.getGroupViewByKey(lastSelectedGroup);
            y = Math.max(0, groupView.getY() - offset);
            layout.scrollTo(y);
            groupView.highlight();
        } else {
            EventView groupView = event.getEventView();
            y = Math.max(0, groupView.getY() - offset);
            layout.scrollTo(y);
            groupView.highlight();
        }

    }

    public void scrollToNewGroup(TaggedEvent event, int groupId) {
        int offset = event.getEventView().getHeight() + event.findTagCount() * 27;
        ScrollLayout layout = (ScrollLayout) this.eventsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        GroupView groupView = event.getGroupViewByKey(Integer.valueOf(groupId));
        int y = Math.max(0, groupView.getY() - offset);
        layout.scrollTo(y);
        groupView.highlight();
    }

    public void scrollToTagAddIn(AbstractTagModel tag) {
        int offset = 100;
        ScrollLayout layout = (ScrollLayout) this.tagsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        if (tag != null) {
            GuiTagModel gtm = (GuiTagModel) tag;
            this.expandToLevel(gtm.getDepth());
            AddValueView addValueView = gtm.getAddValueView();
            int y = Math.max(0, addValueView.getY() - offset);
            layout.scrollTo(y);
        }

    }

    public void scrollToTagEdit(AbstractTagModel tag) {
        int offset = 100;
        ScrollLayout layout = (ScrollLayout) this.tagsScrollPane.getLayout();
        this.updateNotification((String) null, (String) null);
        if (tag != null) {
            GuiTagModel gtm = (GuiTagModel) tag;
            this.expandToLevel(gtm.getDepth());
            TagEditView tagEditView = gtm.getTagEditView();
            int y = Math.max(0, tagEditView.getY() - offset);
            layout.scrollTo(y);
        }

    }

    /**
     * Shows a dialog for the user to enter basic event information (code and
     * label) and has the tagger create this event.
     */
    public void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog(frame);
        String[] eventFields = dialog.showDialog();
        if (eventFields != null) {
            TaggedEvent event = tagger.addNewEvent(eventFields[0], eventFields[1]);
            updateEventsPanel();
            if (event == null) {
                showTaggerMessageDialog(MessageConstants.ADD_EVENT_ERROR, "Ok", null, null);
            } else {
                ScrollLayout eventScrollLayout = (ScrollLayout) eventsScrollPane.getLayout();
                eventScrollLayout.scrollTo(event.getEventView().getCurrentPosition());
            }
        }

    }

    public boolean isSelected(int id) {
        return this.selected.contains(id);
    }

    public void removeSelectedGroup(int id) {
        this.selected.remove(id);
        this.selectedGroups.remove(id);
    }

    public void removeSelectedEvent(int id) {
        this.selected.remove(id);
        this.selectedEvents.remove(id);
    }

    public void addSelectedGroup(int id) {
        this.selected.add(id);
        this.selectedGroups.add(id);
    }

    public Set<Integer> getSelected() {
        return this.selected;
    }

    public void addSelectedEvent(int id) {
        this.selected.add(id);
        this.selectedEvents.add(id);
    }

    /**
     * Delete all tags of CTAGGER
     */
    public void clearAllTags() {
        int option = showTaggerMessageDialog("Are you sure you want to clear all tags?", "Yes", "No", null);
        if (option == 0) {
            tagger.clearAllTags();
            updateEventsPanel();
        }
    }

    /**
     * Copy tags of selected event to other events
     */
    public void copyTag() {
        if (selectedEvents.size() != 1) {
            showTaggerMessageDialog("Please select only one event to copy tags from", "Ok", null, null);
            return;
        }
        else
            new CopyToDialog(frame, tagger, selectedEvents.iterator().next());
    }

    public void getTagSummaryOfSelected() {
        if (selectedEvents.size() == 0) {
            showTaggerMessageDialog("Select events to be included in the summary first", "Ok", null, null);
            return;
        }
        else if (selectedEvents.size() == 1) {
            showTaggerMessageDialog("Please select more than one events for this report", "Ok", null, null);
            return;
        }
        HashMap<String, ArrayList<String>> report = tagger.extractTags(selectedEvents);
        System.out.println(report);
        HashMap<String, ArrayList<String>> fullEventNameUniqueTags = new HashMap<>();
        Set<String> eventIDs = report.keySet();
        for (TaggedEvent event : tagger.getEventSet()) {
            if (eventIDs.contains(""+event.getEventLevelId())) {
                fullEventNameUniqueTags.put(event.getCode(), report.get(""+event.getEventLevelId()));
            }
        }
        TagSummaryDialog dialog = new TagSummaryDialog(frame, report.get("shared"), fullEventNameUniqueTags);
        dialog.showDialog();
    }
    /**
     * Shows a message dialog with the given message and options for the user to
     * choose.
     *
     * @param message Message to display to user
     * @param opt0    Option a user can choose
     * @param opt1    Option a user can choose (may be null)
     * @param opt2    Option a user can choose (may be null)
     * @return The option the user chose (0, 1, or 2), or -1 if no option was
     * chosen
     */
    public int showTaggerMessageDialog(String message, String opt0, String opt1, String opt2) {
        TaggerMessageDialog dialog = new TaggerMessageDialog(frame, message, opt0, opt1, opt2);
        return dialog.showDialog();
    }

    /**
     * Shows a dialog to handle toggling a tag when ancestor tags are present.
     * If the user chooses to replace these ancestor tags, it removes the
     * ancestor tags in the tagger and toggles the tag again.
     *
     * @param message
     */
    public void showAncestorDialog(ToggleTagMessage message) {
        TagDisplayDialog dialog = new TagDisplayDialog(frame, message.ancestors, MessageConstants.ANCESTOR,
                MessageConstants.REPLACE_TAGS_Q, true, "Replace", "Warning");
        boolean replace = dialog.showDialog();
        if (replace) {
            // Remove ancestor tags
            for (EventModel ancestor : message.ancestors) {
                Set<Integer> tagIds = new HashSet<Integer>();
                tagIds.add(ancestor.getGroupId());
                tagger.unassociate(ancestor.getTagModel(), tagIds);
            }
            // Toggle tag again
            tagger.toggleTag(message.tagModel, message.groupIds);
        }
    }

    /**
     * Shows a context menu with the given options on the screen. The menu
     * appears where the mouse was clicked. Uses the default width of 100.
     *
     * @param map Map of option names and actions
     */
    public void showContextMenu(Map<String, ContextMenuAction> map) {
        this.showContextMenu(map, 100);
    }

    private void undoAction() {
        HistoryItem item = null;
        item = this.tagger.undo();
        this.updateEventsPanel();
        this.updateTagsPanel();
        String hoverText = this.tagger.getUndoMessage();
        this.hoverMessage.setText(hoverText);
        if (item != null) {
            this.historyScroll(item);
        }

    }

    /**
     * Shows a context menu with the given options and width on the screen. The
     * menu appears where the mouse was clicked.
     *
     * @param map   Map of option names and actions
     * @param width Width on the screen
     */
    public void showContextMenu(Map<String, ContextMenuAction> map, int width) {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        shield.setVisible(true);
        Point shieldPoint = shield.getLocationOnScreen();
        contextMenu = new ContextMenu(this, map);
        shield.add(contextMenu);
        int contextMenuHeight = contextMenu.getPreferredSize().height;
        int contextMenuWidth = (int) (ConstraintLayout.scale * width);
        int x = mousePoint.x - shieldPoint.x;
        int y = mousePoint.y - shieldPoint.y;
        if (y + contextMenuHeight > shield.getHeight()) {
            y -= contextMenuHeight;
        }
        if (x + contextMenuWidth > shield.getWidth()) {
            x -= contextMenuWidth;
        }
        contextMenu.setLocation(x, y);
        contextMenu.setSize(contextMenuWidth, contextMenuHeight);
    }

    /**
     * Shows a dialog to handle toggling a tag when descendant tags are present.
     * It displays all of the descendant tags present in the selected groups and
     * identifies which event and group they are in.
     *
     * @param message The message shown in the descendant dialog.
     */
    public void showDescendantDialog(ToggleTagMessage message) {
        TagDisplayDialog dialog = new TagDisplayDialog(frame, message.descendants, MessageConstants.DESCENDANT, null,
                false, "Ok", "Warning");
        dialog.showDialog();
    }

    /**
     * Shows a file chooser dialog with the given message.
     *
     * @param message
     *            The message shown in the file chooser dialog.
     * @return The File chosen, or null if no file was chosen.
     */

    /**
     * Shows a dialog with a message containing required tags that are missing
     * from events.
     *
     * @param missingTags The missing required tags.
     * @return True if the user chooses to exit anyway, and false if the user
     * chooses cancel.
     */
    public boolean showRequiredMissingDialog(List<EventModel> missingTags) {
        TagDisplayDialog dialog = new TagDisplayDialog(frame, missingTags, MessageConstants.MISSING_REQUIRED,
                MessageConstants.EXIT_Q, true, "Ok", "Warning");
        return dialog.showDialog();
    }

    public AbstractTagModel showTagChooserDialog(AbstractTagModel baseTag) {
        TaggerSet<AbstractTagModel> tags = this.tagger.getSubHierarchy(baseTag.getPath());
        TagChooserDialog dialog = new TagChooserDialog(this.frame, this, this.tagger, tags);
        AbstractTagModel result = dialog.showDialog();
        this.updateTagsPanel();
        this.updateEventsPanel();
        return result;
    }

    /**
     * Shows a dialog to handle toggling a tag when unique tag values are
     * present. It displays all of the unique tags present in the selected
     * groups and identifies which event and group they are in.
     *
     * @param message The message that shows up in the dialog.
     */
    public void showUniqueDialog(ToggleTagMessage message) {
        String text = MessageConstants.UNIQUE + message.uniqueKey.getPath() + ":";
        TagDisplayDialog dialog = new TagDisplayDialog(frame, message.uniqueValues, text, null, false, "Ok",
                "Warning");
        dialog.showDialog();
    }

    /**
     * Converts a comma separated string of numbers into a integer array
     *
     * @param str comma separated string of numbers
     * @return a integer array
     */
    private int[] StringToIntArray(String str) {
        String[] strArray = str.split(",");
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i].trim());
        }
        return intArray;
    }

    public class HistoryButton extends XButton {
        String hoverText;
        boolean undo;

        public HistoryButton(Icon icon, boolean undo) {
            super(icon);
            this.undo = undo;
        }

        public HistoryButton(String textArg, boolean undo) {
            super(textArg);
            this.undo = undo;
        }

        public Font getFont() {
            return FontsAndColors.buttonFont;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (undo) {
                TaggerView.this.undoAction();
            } else {
                TaggerView.this.redoAction();
            }

        }

//        public void mouseEntered(MouseEvent e) {
//            super.mouseEntered(e);
//            this.hoverText = this.undo ? TaggerView.this.tagger.getUndoMessage() : TaggerView.this.tagger.getRedoMessage();
//            TaggerView.this.hoverMessage.setText(this.hoverText);
//            Point point = this.getLocation();
//            int top = point.y + 50;
//            int right = this.getWidth() - point.x - 120;
//            TaggerView.this.setTopHeight(TaggerView.this.hoverMessage, (double) top, Unit.PX, 25.0D, Unit.PX);
//            TaggerView.this.setRightWidth(TaggerView.this.hoverMessage, (double) right, Unit.PX, 120.0D, Unit.PX);
//            TaggerView.this.hoverMessage.setVisible(true);
//        }
//
//        public void mouseExited(MouseEvent e) {
//            super.mouseExited(e);
//            TaggerView.this.hoverMessage.setVisible(false);
//        }
    }


    public class addGroupMenuListener implements ActionListener {
        public addGroupMenuListener() {
        }

        public void actionPerformed(ActionEvent e) {
            TaggerView.this.addGroup();
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public EventEnterTagView getEventEnterTagView() {
        return eventEnterTagView;
    }
}
