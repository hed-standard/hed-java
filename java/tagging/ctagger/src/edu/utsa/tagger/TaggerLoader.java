//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.utsa.tagger;

import edu.utsa.tagger.gui.GuiModelFactory;
import java.io.IOException;

public class TaggerLoader {
    public static final int HED_EXTENDED = 128;
    public static final int FIRST_FIELD = 64;
    public static final int PRIMARY_FIELD = 32;
    public static final int STAND_ALONE = 16;
    public static final int EXTEND_ANYWHERE = 8;
    public static final int EXTENSIONS_ALLOWED = 4;
    public static final int PRESERVE_PREFIX = 2;
    public static final int USE_JSON = 1;
    private boolean back;
    private String events;
    private int flags;
    private boolean fMapLoaded;
    private boolean hedExtended;
    private String fMapPath;
    private boolean fMapSaved;
    private int initialDepth;
    private boolean notified;
    private boolean startOver;
    private boolean submitted;
    private Tagger tagger;
    private String tags;
    private String title;

    public static void load(int flags, String frameTitle, int initialDepth, IFactory factory) {
        new TaggerLoader(flags, frameTitle, initialDepth, factory);
    }

    public static String load(String xmlData, int flags, String frameTitle, int initialDepth) {
        return load(xmlData, flags, frameTitle, initialDepth, new GuiModelFactory());
    }

    /**
     * Creates a Loader that launches the Tagger GUI with the given parameters.
     *
     * @param xmlData
     *            An XML string in the TaggerData format containing the events
     *            and the HED hierarchy
     * @param flags
     *            Options for running Tagger
     * @param frameTitle
     *            Title of Tagger GUI window
     * @param initialDepth
     *            Initial depth to display tags
     * @param factory
     *            Factory used to create the GUI
     * @return String containing the TaggerData XML. If the user presses "Done,"
     *         this includes the changes the user made in the GUI. If the user
     *         presses "Cancel," this is equal to the String passed in as a
     *         parameter (with no changes included).
     */
    public static String load(String xmlData, int flags, String frameTitle, int initialDepth, IFactory factory) {
        TaggerLoader loader = new TaggerLoader(xmlData, flags, frameTitle, initialDepth, factory);
        loader.waitForSubmitted();
        return loader.isSubmitted() ? loader.tagger.getXmlDataString() : xmlData;
    }

    public static String[] load(String tags, String events, int flags, String frameTitle, int initialDepth) throws IOException {
        return load(tags, events, flags, frameTitle, initialDepth, new GuiModelFactory());
    }

    /**
     * Creates a Loader that launches the Tagger GUI with the given parameters.
     *
     * @param tags
     *            HED XML String
     * @param events
     *            Events JSON String
     * @param flags
     *            Options for running Tagger
     * @param frameTitle
     *            Title of Tagger GUI window
     * @param initialDepth
     *            Initial depth to display tags
     * @param factory
     *            Factory used to create the GUI
     * @return String array containing the HED XML (index 0) and the events JSON
     *         (index 1). If the user presses "Done," these include the changes
     *         the user made in the GUI. If the user presses "Cancel," these are
     *         equal to the Strings passed in as parameters (with no changes
     *         included).
     * @throws IOException
     */
    public static String[] load(String tags, String events, int flags, String frameTitle, int initialDepth, IFactory factory) throws IOException {
        TaggerLoader loader = new TaggerLoader(tags, events, flags, frameTitle, initialDepth, factory);
        String[] returnString = new String[]{tags, events};
        loader.waitForSubmitted();
        if (loader.isSubmitted()) {
            returnString[0] = loader.tagger.hedToString();
            returnString[1] = loader.checkFlags(1) ? loader.tagger.getJSONString() : loader.tagger.createTSVString();
        }

        return returnString;
    }

    public static String[] load(TaggerLoader loader, String tags, String events) throws IOException {
        String[] returnString = new String[]{loader.tagger.hedToString(), loader.checkFlags(1) ? loader.tagger.getJSONString() : loader.tagger.createTSVString()};
        return returnString;
    }

    public TaggerLoader(int flags, String frameTitle, int initialDepth, IFactory factory) {
        this.events = "";
        this.fMapPath = "";
        this.tags = "";
        this.title = "";
        this.initialDepth = initialDepth;
        this.title = frameTitle;
        this.flags = flags;
        this.tagger = new Tagger(factory, this);
        factory.createTaggerView(this, this.tagger, frameTitle);
    }

    public TaggerLoader(String xmlData, int flags, String frameTitle, int initialDepth, IFactory factory) {
        this.events = "";
        this.fMapPath = "";
        this.tags = "";
        this.title = "";
        this.initialDepth = initialDepth;
        this.title = frameTitle;
        this.flags = flags;
        this.tagger = new Tagger(xmlData, factory, this);
        factory.createTaggerView(this, this.tagger, frameTitle);
    }

    public TaggerLoader(String tags, String events, int flags, String frameTitle, int initialDepth) {
        this(tags, events, flags, frameTitle, initialDepth, new GuiModelFactory());
    }

    public TaggerLoader(Tagger tagger, String tags, String events, int flags, String frameTitle, int initialDepth) {
        this(tagger, tags, events, flags, frameTitle, initialDepth, new GuiModelFactory());
    }

    public TaggerLoader(String tags, String events, int flags, String frameTitle, int initialDepth, IFactory factory) {
        this.events = "";
        this.fMapPath = "";
        this.tags = "";
        this.title = "";
        this.tags = tags;
        this.events = events;
        this.initialDepth = initialDepth;
        this.title = frameTitle;
        this.flags = flags;
        this.tagger = new Tagger(tags, events, factory, this);
        factory.createTaggerView(this, this.tagger, frameTitle);
    }

    public TaggerLoader(Tagger tagger, String tags, String events, int flags, String frameTitle, int initialDepth, IFactory factory) {
        this.events = "";
        this.fMapPath = "";
        this.tags = "";
        this.title = "";
        this.tags = tags;
        this.events = events;
        this.initialDepth = initialDepth;
        this.title = frameTitle;
        this.flags = flags;
        this.tagger = tagger;
        factory.createTaggerView(this);
    }

    public synchronized boolean checkFlags(int flags) {
        return (this.flags & flags) == flags;
    }

    public synchronized boolean fMapLoaded() {
        return this.fMapLoaded;
    }

    public synchronized boolean fMapSaved() {
        return this.fMapSaved;
    }

    public synchronized String getFMapPath() {
        return this.fMapPath;
    }

    public synchronized int getInitialDepth() {
        return this.initialDepth;
    }

    public synchronized boolean getHEDExtended() {
        return this.hedExtended;
    }

    public synchronized String getTitle() {
        return this.title;
    }

    public synchronized String[] getXMLAndEvents() throws IOException {
        return load(this, this.tags, this.events);
    }

    public synchronized boolean isBack() {
        return this.back;
    }

    public synchronized boolean isNotified() {
        return this.notified;
    }

    public synchronized Tagger getTagger() {
        return this.tagger;
    }

    public synchronized boolean isStartOver() {
        return this.startOver;
    }

    public synchronized boolean isSubmitted() {
        return this.submitted;
    }

    public synchronized void setBack(boolean back) {
        this.back = back;
    }

    public synchronized void setFMapLoaded(boolean fMapLoaded) {
        this.fMapLoaded = fMapLoaded;
    }

    public synchronized void setTagger(Tagger tagger) {
        this.tagger = tagger;
    }

    public synchronized void setFMapPath(String fMapPath) {
        this.fMapPath = fMapPath;
    }

    public synchronized void setFMapSaved(boolean fMapSaved) {
        this.fMapSaved = fMapSaved;
    }

    public synchronized void setHEDExtended(boolean hedExtended) {
        this.hedExtended = hedExtended;
    }

    public synchronized void setNotified(boolean notified) {
        this.notified = notified;
        this.notify();
    }

    public synchronized void setStartOver(boolean startOver) {
        this.startOver = startOver;
    }

    public synchronized void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public synchronized void waitForSubmitted() {
        while(true) {
            try {
                if (!this.notified) {
                    this.wait();
                    continue;
                }
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            return;
        }
    }
}
