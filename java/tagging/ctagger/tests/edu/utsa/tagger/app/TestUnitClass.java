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
    private String[] possibleUnits = {"cm","m"};
    @Before
    public void setUp() {
        setup = new TestSetup();
    }

    @Test
    public void testGetUnitModifiers() {
        Tagger tagger = setup.getTestTagger();
        tagger.loadHED(setup.getHedXML()); // Tagger.createUnitModifierHashMapFromXml is called
        // modifiers as of HED7.1.1
        String[] unitModifiers = {"deca", "hecto", "kilo", "mega", "giga", "tera", "peta", "exa", "zetta", "yotta", "deci", "centi", "milli", "micro", "nano", "pico", "femto","atto","zepto","yocto"};
        String[] unitSymbolModifiers = {"da", "h", "k", "M", "G", "T", "P", "E", "Z", "Y", "d", "c", "m", "u", "n", "p", "f","a","z","y"};
        assertEquals(Arrays.asList(unitModifiers), tagger.getUnitModifiers().get("unit"));
        assertEquals(Arrays.asList(unitSymbolModifiers), tagger.getUnitModifiers().get("symbol"));
    }

    @Test
    public void testGetUnitClasses() {
        Tagger tagger = setup.getTestTagger();
        tagger.loadHED(setup.getHedXML()); // Tagger.createUnitModifierHashMapFromXml is called
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
