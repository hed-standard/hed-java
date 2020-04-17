package edu.utsa.tagger.app;

import edu.utsa.tagger.*;
import edu.utsa.tagger.gui.GuiEventModel;
import edu.utsa.tagger.gui.GuiModelFactory;
import edu.utsa.tagger.gui.GuiTagModel;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class TestNestedGroup {
    private Tagger testTagger;
    private String hedXML;
    private String hedRR;
    private String eventsOld;
    private IFactory factory;

    private TaggedEvent taggedEvent;
    private AbstractTagModel tagAncestor;
    private AbstractTagModel tagAncestor2;
    private AbstractTagModel tag;
    private AbstractTagModel tagDescendant;
    TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroups;
    GroupTree groups;

    @Before
    public void setUp() {
        hedXML = TestUtilities.getResourceAsString(TestUtilities.HedFileName);
        hedRR = TestUtilities.getResourceAsString(TestUtilities.HedFileName);
        eventsOld = TestUtilities.getResourceAsString(TestUtilities.JsonEventsArrays);
        factory = new GuiModelFactory();
        TaggerLoader loader = new TaggerLoader(hedXML, eventsOld, TaggerLoader.USE_JSON, "Test Nested Group", 3, factory);
        testTagger = new Tagger(factory, loader);
        tagAncestor = factory.createAbstractTagModel(testTagger);
        tagAncestor.setPath("/a/b/c");
        tagAncestor2 = factory.createAbstractTagModel(testTagger);
        tagAncestor2.setPath("/a");
        tag = (GuiTagModel) factory.createAbstractTagModel(testTagger);
        tag.setPath("/a/b/c/d");
        tagDescendant = factory.createAbstractTagModel(testTagger);
        tagDescendant.setPath("/a/b/c/d/e/f");
        ArrayList<AbstractTagModel> attributes = new ArrayList<>();
        attributes.add(tagDescendant);
        attributes.add(tagAncestor2);
        ((GuiTagModel)tag).setAttributes(attributes);
        TaggerSet<AbstractTagModel> set1 = new TaggerSet<>();
        set1.add(tag);
        set1.add(tagAncestor);
        TaggerSet<AbstractTagModel> set2 = (TaggerSet<AbstractTagModel>)set1.clone();
        TaggerSet<AbstractTagModel> set3 = (TaggerSet<AbstractTagModel>)set1.clone();
        TaggerSet<AbstractTagModel> set4 = (TaggerSet<AbstractTagModel>)set1.clone();
        tagGroups = new TreeMap<>();
        tagGroups.put(1, set1);
        tagGroups.put(2, set2);
        tagGroups.put(3, set3);
        tagGroups.put(4, set4);
        tagGroups.put(5, set4);
        tagGroups.put(6, set4);
        tagGroups.put(7, set4);
        groups = new GroupTree(1);
        groups.add(1, 2);
        groups.add(1, 3);
        groups.add(2, 4);
        groups.add(2, 5);
        groups.add(4,6);
        groups.add(3,7);
        System.out.println(groups);
        GuiEventModel eventModel = new GuiEventModel(testTagger);
        eventModel.setCode("test");
        taggedEvent = new TaggedEvent(eventModel,testTagger);
        taggedEvent.setTagGroups(tagGroups);
        taggedEvent.setTagGroupHierarchy(groups);
        testTagger.addEventBase(taggedEvent);
    }

    @Test
    public void testSearch() {
        System.out.println(groups.find(4));
        System.out.println(groups.find(8));
    }

    @Test
    public void testToJson() {
        System.out.println(testTagger.getJSONString());
    }


}
