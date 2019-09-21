package edu.utsa.tagger.app;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TaggerLoader;
import edu.utsa.tagger.TaggerSet;
import edu.utsa.tagger.TestSetup;
import edu.utsa.tagger.app.TestUtilities;
import edu.utsa.tagger.gui.AddValueView;
import edu.utsa.tagger.gui.GuiModelFactory;
import edu.utsa.tagger.gui.GuiTagModel;
import edu.utsa.tagger.gui.TaggerView;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.SortedSet;

import static org.junit.Assert.assertTrue;

public class TestAddValueView {
    private TestSetup setup;
    private String[] possibleUnits = {"cm","m"};
    private AddValueView view;
    @Before
    public void setUp() {
        setup = new TestSetup();
        SortedSet<AbstractTagModel> tagModels = setup.getTestTagger().getTagSet();
        GuiTagModel tagModel = (GuiTagModel)tagModels.first();
        view = tagModel.getAddValueView();

    }

    @Test
    public void testValidateNumericValue() {
        int max = 100000, min = -100000;
        String randNum = "" + ((Math.random() * ((max - min) + 1)) + min);
        String[] possibleValues = {randNum,"abcd","a1b","2a4"};

    }
    /* Test all events */
    @Test
    public void testOKButton() {
        /*
        What do we want when an Ok button is pressed in AddValue View?
        1. Validate input depending on the attribute of the tag
            - If isNumeric tag, validate unit
            -
        2. Accept input value and save it to the event
         */
    }

    @Test
    public void testShowDialogJSONInput() throws IOException {
        String hedXML = TestUtilities.getResourceAsString(TestUtilities.HedFileName);
        String events = TestUtilities.getResourceAsString(TestUtilities.JsonEventsArrays);

        String[] result = TaggerLoader.load(hedXML, events, TaggerLoader.USE_JSON,
                "Tagger Test - JSON + XML", 2, new GuiModelFactory());
        System.out.println(result[1]);
        assertTrue(result != null);
    }
}
