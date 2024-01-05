package cw.utils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HelpConfigurationTest {
    private static HelpConfiguration helpConf = null;

    @BeforeClass
    public static void setUpClass() {
        helpConf = new HelpConfiguration(
                "TESTtitle123",
                new ArrayList<>(Arrays.asList("TESTstring1", "TESTstring2", "TESTstring3")),
                new ArrayList<>(Arrays.asList("TESTpath1", "TESTpath2")),
                new ArrayList<>(Arrays.asList(0, 1, 4)),
                new ArrayList<>(Arrays.asList(2, 3))
        );
    }

    @Test // unit
    public void testGetTitle() {
        assertEquals("Title mismatch",
                "TESTtitle123", helpConf.getTitle());
    }

    @Test // unit
    public void testGetStrings() {
        assertEquals("Strings and its indices arrays length mismatch",
                helpConf.getStringsIndices().size(), helpConf.getStrings().size());
    }

    @Test // unit
    public void testGetImages() {
        assertEquals("Images paths and its indices arrays length mismatch",
                helpConf.getImagesIndices().size(), helpConf.getImages().size());
    }

    @Test // integration (with system I/O)
    public void testLoadFromFile() {
        var helpConf = new HelpConfiguration();
        try {
            helpConf.loadFromFile("/help/config/testHelp.ini");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Failed to open resource test file");
        }

        assertEquals("Expected title mismatch",
                "Test: \"Тест - test\"", helpConf.getTitle());
        assertEquals("Strings count mismatch",
                2, helpConf.getStrings().size());
        assertEquals("Strings and its indices arrays length mismatch",
                helpConf.getStringsIndices().size(), helpConf.getStrings().size());
        assertEquals("Images paths count mismatch",
                2, helpConf.getImages().size());
        assertEquals("Images paths and its indices arrays length mismatch",
                helpConf.getImagesIndices().size(), helpConf.getImages().size());

        assertArrayEquals("String content mismatch",
                helpConf.strings.toArray(),
                new String[]{
                        "Тест \"test\": тест.\n\n\nTest тест test тест (1) test тест test.",
                        "Test тест test тест test \"тест\", test\nтест test тест test (2). Тест test тест test."});

        assertArrayEquals("Images paths content mismatch",
                helpConf.images.toArray(),
                new String[]{
                        "/path/to/image/in/resource/folder/1.png",
                        "/path/to/image/in/resource/folder/2.png"});
    }
}