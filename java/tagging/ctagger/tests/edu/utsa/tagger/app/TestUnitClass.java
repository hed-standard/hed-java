package edu.utsa.tagger.app;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import edu.utsa.tagger.*;
import edu.utsa.tagger.gui.AddValueView;
import edu.utsa.tagger.gui.GuiModelFactory;
import edu.utsa.tagger.gui.GuiTagModel;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;

import static org.junit.Assert.*;

/**
 * As of HED 7.1.1
 */
public class TestUnitClass {
    private TestSetup setup;
    private Tagger tagger;
    private String[] possibleUnits = {"cm","m"};
    @Before
    public void setUp() {
        setup = new TestSetup();
        tagger = setup.getTestTagger();
        tagger.loadHED(setup.getHedXML()); // Tagger.createUnitModifierHashMapFromXml is called
    }

    @Test
    public void testGetUnitModifiers() {
        // modifiers as of HED7.1.1
        String[] unitModifiers = {"deca", "hecto", "kilo", "mega", "giga", "tera", "peta", "exa", "zetta", "yotta", "deci", "centi", "milli", "micro", "nano", "pico", "femto","atto","zepto","yocto"};
        String[] unitSymbolModifiers = {"da", "h", "k", "M", "G", "T", "P", "E", "Z", "Y", "d", "c", "m", "u", "n", "p", "f","a","z","y"};
        assertEquals(Arrays.asList(unitModifiers), tagger.getUnitModifiers().get("unit"));
        assertEquals(Arrays.asList(unitSymbolModifiers), tagger.getUnitModifiers().get("symbol"));
    }

    @Test
    public void testGetUnitClasses() {
        // Event/Duration --> time unitClass
        GuiTagModel durationTag = (GuiTagModel) tagger.getTagModel("Event/Duration/#");
        assertEquals("time", durationTag.getUnitClass());
        String[] units = {"second", "s", "day", "minute", "hour"};
        assertArrayEquals(durationTag.getUnits(), units);
        assertTrue(tagger.unitClasses.get("time").get(0).isSIUnit());
        assertTrue(tagger.unitClasses.get("time").get(1).isSIUnit());
        assertTrue(tagger.unitClasses.get("time").get(1).isUnitSymbol());

        // Item/2D shape/Clock face/# --> time unitClass
        GuiTagModel clockFaceTag = (GuiTagModel) tagger.getTagModel("Item/2D shape/Clock face/#");
        assertEquals("clockTime", clockFaceTag.getUnitClass());
        String[] units1 = {"hour:min", "hour:min:sec"};
        assertArrayEquals(clockFaceTag.getUnits(), units1);
        assertFalse(tagger.unitClasses.get("clockTime").get(0).isSIUnit());
        assertFalse(tagger.unitClasses.get("clockTime").get(1).isSIUnit());
        assertFalse(tagger.unitClasses.get("clockTime").get(1).isUnitSymbol());

        // Attribute/Temporal rate/# --> frequency unitClass
        GuiTagModel temporalRateTag = (GuiTagModel) tagger.getTagModel("Attribute/Temporal rate/#");
        assertEquals("frequency", temporalRateTag.getUnitClass());
        String[] units2 = {"hertz", "Hz"};
        assertArrayEquals(temporalRateTag.getUnits(), units2);

        // Attribute/Direction/Bottom/# --> angle unitClass
        GuiTagModel directionTag = (GuiTagModel) tagger.getTagModel("Attribute/Direction/Bottom/#");
        assertEquals("angle,physicalLength,pixels", directionTag.getUnitClass());
        String[] units3 = {"radian", "rad", "degree", "metre", "m", "foot", "mile", "pixel", "px"};
        assertArrayEquals(directionTag.getUnits(), units3);
    }

    @Test
    public void testInputValidation() {
        // Test GuiTagModel.validateInput()
        // Event/Duration --> time unitClass
        GuiTagModel durationTag = (GuiTagModel) tagger.getTagModel("Event/Duration/#");
        assertEquals(durationTag.validateInput("12", "ms"), "12 ms");
        assertNull(durationTag.validateInput("word", "s"));

        // Item/2D shape/Clock face/# --> time unitClass
        GuiTagModel clockFaceTag = (GuiTagModel) tagger.getTagModel("Item/2D shape/Clock face/#");
        assertNull(clockFaceTag.validateInput("24:54","hour:min"));
        assertNull(clockFaceTag.validateInput("word","hour:min"));
        assertNull(clockFaceTag.validateInput("30:04", "hour:min"));
        assertEquals(clockFaceTag.validateInput("23:54", "hour:min"), "23:54 hour:min");
        assertEquals(clockFaceTag.validateInput("03:54", "hour:min"), "03:54 hour:min");

        assertNull(clockFaceTag.validateInput("24:54","hour:min:sec"));
        assertNull(clockFaceTag.validateInput("word","hour:min:sec"));
        assertNull(clockFaceTag.validateInput("30:04", "hour:min:sec"));
        assertNull(clockFaceTag.validateInput("03:04:60", "hour:min:sec"));
        assertEquals(clockFaceTag.validateInput("23:54:04", "hour:min:sec"), "23:54:04 hour:min:sec");
        assertEquals(clockFaceTag.validateInput("03:54:59", "hour:min:sec"), "03:54:59 hour:min:sec");
    }
    /* Test all events */
    @Test
    public void testGUI() {
        // Test that both event tag search view and schema view show unit options correctly
        // Event/Duration --> time unitClass
        GuiTagModel durationTag = (GuiTagModel) tagger.getTagModel("Event/Duration/#");

        // Item/2D shape/Clock face/# --> time unitClass
        GuiTagModel clockFaceTag = (GuiTagModel) tagger.getTagModel("Item/2D shape/Clock face/#");

        // Attribute/Temporal rate/# --> frequency unitClass
        GuiTagModel temporalRateTag = (GuiTagModel) tagger.getTagModel("Attribute/Temporal rate/#");

        // Attribute/Direction/Bottom/# --> angle unitClass
        GuiTagModel directionTag = (GuiTagModel) tagger.getTagModel("Attribute/Direction/Bottom/#");
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
