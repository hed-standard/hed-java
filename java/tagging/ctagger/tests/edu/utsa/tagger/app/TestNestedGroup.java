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
import static org.junit.Assert.*;

public class TestNestedGroup {
    private Tagger testTagger;
    private TaggerLoader loader;
    private String hedXML;
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
        factory = new GuiModelFactory();
        loader = new TaggerLoader(hedXML, "[{\"code\" : \"1\", \"tags\" : []}]", TaggerLoader.USE_JSON, "Test Nested Group", 3, factory);
        testTagger = new Tagger(factory, loader);
        tagAncestor = factory.createAbstractTagModel(testTagger);
        tagAncestor.setPath("/a/b/c");
        tagAncestor2 = factory.createAbstractTagModel(testTagger);
        tagAncestor2.setPath("/a");
        tag = (GuiTagModel) factory.createAbstractTagModel(testTagger);
        tag.setPath("/a/b/c/d");
        tagDescendant = factory.createAbstractTagModel(testTagger);
        tagDescendant.setPath("/a/b/c/d/e/f");
        GuiEventModel eventModel = new GuiEventModel(testTagger);
        eventModel.setCode("test");
        taggedEvent = new TaggedEvent(eventModel,testTagger);
        taggedEvent.setEventLevelId(0); // add eventLevelID to taggedEvent group list
        testTagger.addEventBase(taggedEvent);
        loader.setTagger(testTagger);
    }

    @Test
    public void testEventLevel() {
        // group hierarchy should contain eventLevelID as root
        assertEquals(taggedEvent.getTagGroupHierarchy().getRoot().getGroupId(), taggedEvent.getEventLevelId());

        taggedEvent.addTag(tagAncestor);
        // there should be only one groupID which is the eventLevelID
        assertEquals(taggedEvent.getEventLevelId(), (int) taggedEvent.getTagGroups().firstKey());
        // there should be one tag which is tagAncestor
        assertEquals(1, taggedEvent.getTagGroups().get(taggedEvent.getEventLevelId()).size());
        assertTrue(taggedEvent.getTagGroups().containsValue(tagAncestor));

        taggedEvent.addTag(tagAncestor2);
        // there should be 2 tags
        assertEquals(2, taggedEvent.getTagGroups().get(taggedEvent.getEventLevelId()).size());
        assertTrue(taggedEvent.getTagGroups().containsValue(tagAncestor2));

    }

    @Test
    public void testGroupLevel() {
        int eventLevelID = taggedEvent.getEventLevelId();
        GroupTree groupHierarchy = taggedEvent.getTagGroupHierarchy();

        // group hierarchy should contain eventLevelID as root
        assertEquals(taggedEvent.getTagGroupHierarchy().getRoot().getGroupId(), taggedEvent.getEventLevelId());

        // eventLevel
        //  - groupOneID -> tagAncestor
        int groupOneID = testTagger.addNewGroup(taggedEvent);
        // groupHierarchy root now has children which is groupOneID and eventLevelID should be parent of groupOneID
        assertTrue(taggedEvent.getTagGroupHierarchy().find(taggedEvent.getEventLevelId()).hasChildren());
        assertFalse(taggedEvent.getTagGroupHierarchy().find(groupOneID).hasChildren());
        assertEquals(taggedEvent.getTagGroupHierarchy().find(taggedEvent.getEventLevelId()).getChildren().get(0).getGroupId(), groupOneID);
        assertEquals(taggedEvent.getTagGroupHierarchy().find(groupOneID).getParentId(), taggedEvent.getEventLevelId());

        taggedEvent.addTagToGroup(groupOneID, tagAncestor);
        // there should be one tag which is tagAncestor under groupOneID not eventLevelID
        assertEquals(0, taggedEvent.getTagGroups().get(taggedEvent.getEventLevelId()).size());
        assertEquals(1, taggedEvent.getTagGroups().get(groupOneID).size());
//        assertTrue(taggedEvent.getTagGroups().containsValue(tagAncestor));
        assertTrue(taggedEvent.getTagGroups().get(groupOneID).contains(tagAncestor));
        assertFalse(taggedEvent.getTagGroups().get(eventLevelID).contains(tagAncestor));


        // eventLevel
        //  - groupOneID -> tagAncestor
        //      - groupTwoID -> tagAncestor2
        int groupTwoID = groupOneID + 1;
        taggedEvent.addGroup(groupOneID, groupTwoID);
        // groupHierarchy root now has children which are groupOneID and groupTwoID. groupOneID should be parent of groupTwoID
        assertTrue(groupHierarchy.find(groupOneID).hasChildren());
        assertEquals(taggedEvent.getTagGroupHierarchy().find(eventLevelID).getChildren().get(0).getGroupId(), groupOneID);
        assertTrue(groupHierarchy.find(eventLevelID).getChildren().size()==1);
        assertTrue(groupHierarchy.find(groupOneID).getChildren().size()==1);
        assertEquals(groupHierarchy.find(groupOneID).getChildren().get(0).getGroupId(), groupTwoID);
        assertNotEquals(taggedEvent.getTagGroupHierarchy().find(eventLevelID).getChildren().get(0).getGroupId(), groupTwoID);
        assertEquals(groupHierarchy.find(groupOneID).getParentId(), eventLevelID);
        assertEquals(groupHierarchy.find(groupTwoID).getParentId(), groupOneID);

        taggedEvent.addTagToGroup(groupTwoID, tagAncestor2);
        // there should be one tag which is tagAncestor2 under groupTwoID
        assertEquals(1, taggedEvent.getTagGroups().get(groupOneID).size());
        assertEquals(1, taggedEvent.getTagGroups().get(groupTwoID).size());
        assertTrue(taggedEvent.getTagGroups().get(groupTwoID).contains(tagAncestor2));
        assertFalse(taggedEvent.getTagGroups().get(groupOneID).contains(tagAncestor2));

        // eventLevel -> tag
        //  - groupOneID -> tagAncestor
        //      - groupTwoID -> tagAncestor2
        taggedEvent.addTagToGroup(eventLevelID, tag);
        // there should be one tag which is tagAncestor2 under groupTwoID
        assertEquals(1, taggedEvent.getTagGroups().get(eventLevelID).size());
        assertTrue(taggedEvent.getTagGroups().get(eventLevelID).contains(tag));

        // eventLevel -> tag
        //  - groupOneID -> tagAncestor
        //      - groupTwoID -> tagAncestor2
        //  - groupThreeID ->tagDescendant
        int groupThreeID = groupTwoID + 1;
        taggedEvent.addGroup(groupThreeID);
        taggedEvent.addTagToGroup(groupThreeID, tagDescendant);
        try {
            String[] result = loader.getXMLAndEvents();
            String json = result[1];
            System.out.println(json); // should be similar to [ { "code" : "test", "tags" : [ "/a/b/c/d", [ "/a/b/c", [ "/a" ] ], [ "/a/b/c/d/e/f" ] ] } ]
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }
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
