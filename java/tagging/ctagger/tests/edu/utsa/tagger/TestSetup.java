package edu.utsa.tagger;

import edu.utsa.tagger.app.TestUtilities;
import edu.utsa.tagger.gui.GuiModelFactory;
import edu.utsa.tagger.gui.TaggerView;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class TestSetup {
    private Tagger testTagger;
    private File testLoadXml;
    private String hedXML;
    private String hedRR;
    private String eventsOld;
    private IFactory factory;


    private TaggerView testTaggerView;
    private AbstractTagModel tagAncestor;
    private AbstractTagModel tagAncestor2;
    private AbstractTagModel tag;
    private AbstractTagModel tagDescendant;
    private TaggedEvent testEvent1;
    private TaggedEvent testEvent2;
    private TaggedEvent testEvent3;

    public TestSetup() {
        // Download latest version of HED schema (https://github.com/hed-standard/hed-specification)
        try {
            Process p = Runtime.getRuntime().exec("wget -nc -P /Users/dtyoung/Documents/hed-java/java/tagging/ctagger/tests/data/ https://raw.githubusercontent.com/hed-standard/hed-specification/master/hedxml/HEDLatest.xml");
        }
        catch(IOException e) {
            System.out.println("Exception while downloading latest version of HED Schema");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Use version of HED available in the system");
        }
        try {
            testLoadXml = TestUtilities.getResourceAsFile(TestUtilities.saveFileTest);
        }
        catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        hedXML = TestUtilities.getResourceAsString(TestUtilities.HedFileName);
        hedRR = TestUtilities.getResourceAsString(TestUtilities.HedFileName);
        eventsOld = TestUtilities.getResourceAsString(TestUtilities.JsonEventsArrays);
        factory = new GuiModelFactory();
        TaggerLoader loader = new TaggerLoader(hedXML, eventsOld, TaggerLoader.USE_JSON, "Tagger Test - JSON Events", 3, factory);
        testTagger = new Tagger(hedXML, eventsOld, factory, loader);
        TaggerSet<TaggedEvent> egtSet = testTagger.getEventSet();
        testEvent1 = egtSet.get(0);
        testEvent2 = egtSet.get(1);
        testEvent3 = egtSet.get(2);
        tagAncestor = factory.createAbstractTagModel(testTagger);
        tagAncestor.setPath("/a/b/c");
        tagAncestor2 = factory.createAbstractTagModel(testTagger);
        tagAncestor2.setPath("/a");
        tag = factory.createAbstractTagModel(testTagger);
        tag.setPath("/a/b/c/d");
        tagDescendant = factory.createAbstractTagModel(testTagger);
        tagDescendant.setPath("/a/b/c/d/e/f");
    }

    public Tagger getTestTagger() {
        return testTagger;
    }

    public void setTestTagger(Tagger testTagger) {
        this.testTagger = testTagger;
    }

    public File getTestLoadXml() {
        return testLoadXml;
    }

    public void setTestLoadXml(File testLoadXml) {
        this.testLoadXml = testLoadXml;
    }

    public String getHedXML() {
        return hedXML;
    }

    public void setHedXML(String hedXML) {
        this.hedXML = hedXML;
    }

    public String getHedRR() {
        return hedRR;
    }

    public void setHedRR(String hedRR) {
        this.hedRR = hedRR;
    }

    public String getEventsOld() {
        return eventsOld;
    }

    public void setEventsOld(String eventsOld) {
        this.eventsOld = eventsOld;
    }

    public IFactory getFactory() {
        return factory;
    }

    public void setFactory(IFactory factory) {
        this.factory = factory;
    }

    public AbstractTagModel getTagAncestor() {
        return tagAncestor;
    }

    public void setTagAncestor(AbstractTagModel tagAncestor) {
        this.tagAncestor = tagAncestor;
    }

    public AbstractTagModel getTagAncestor2() {
        return tagAncestor2;
    }

    public void setTagAncestor2(AbstractTagModel tagAncestor2) {
        this.tagAncestor2 = tagAncestor2;
    }

    public AbstractTagModel getTag() {
        return tag;
    }

    public void setTag(AbstractTagModel tag) {
        this.tag = tag;
    }

    public AbstractTagModel getTagDescendant() {
        return tagDescendant;
    }

    public void setTagDescendant(AbstractTagModel tagDescendant) {
        this.tagDescendant = tagDescendant;
    }

    public TaggedEvent getTestEvent1() {
        return testEvent1;
    }

    public void setTestEvent1(TaggedEvent testEvent1) {
        this.testEvent1 = testEvent1;
    }

    public TaggedEvent getTestEvent2() {
        return testEvent2;
    }

    public void setTestEvent2(TaggedEvent testEvent2) {
        this.testEvent2 = testEvent2;
    }

    public TaggedEvent getTestEvent3() {
        return testEvent3;
    }

    public void setTestEvent3(TaggedEvent testEvent3) {
        this.testEvent3 = testEvent3;
    }
    public TaggerView getTestTaggerView() {
        return testTaggerView;
    }

}
