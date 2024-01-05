package cw.utils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
        File tempFile = null;
        try {
            String content = """
                # Конфигурационный файл капчи-паззла
                                
                [title]
                name = Справка: "Captcha - пазл"
                                
                [content]
                # индексы в строго возрастающей последовательности, начальный индекс = 0
                stringsIndices = 0, 2
                imagesIndices = 1, 3
                element0 = Тип "капчи": поведенческая.\\n\\n\\nНеобходимо нажимая на плитки (1) добиться восстановления исходного\\nизображения.\\nNote: Каждая плитка меняется либо вертикально, либо горизонтально,\\nпри этом ротация индексов сгенерирована случайным образом.\\nИзначально все плитки перепутаны.
                element1 = /help/images/helpPuzzleCaptcha.png
                element2 = После восстановления изображения нажмите кнопку "Проверить", и\\nрезультат отобразится в поле (2). Ниже приведён пример решения.
                element3 = /help/images/helpPuzzleCaptcha1.png
                """;
            tempFile = File.createTempFile("CaptchaTD_" + UUID.randomUUID(), ".ini.tmp");
            try (var writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                writer.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create temporary file");
        }

        var helpConf = new HelpConfiguration();
        try {
            helpConf.loadFromFile(tempFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to parse date");
        }

        assertEquals("Expected title mismatch",
                "Справка: \"Captcha - пазл\"", helpConf.getTitle());
        assertEquals("Strings count mismatch",
                2, helpConf.getStrings().size());
        assertEquals("Strings and its indices arrays length mismatch",
                helpConf.getStringsIndices().size(), helpConf.getStrings().size());
        assertEquals("Images paths count mismatch",
                2, helpConf.getImages().size());
        assertEquals("Images paths and its indices arrays length mismatch",
                helpConf.getImagesIndices().size(), helpConf.getImages().size());
    }
}