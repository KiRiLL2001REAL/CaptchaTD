package cw.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HelpConfigurationTest {
    private HelpConfiguration helpConf = null;

    @Before
    public void initData() {
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
        assertEquals("TESTtitle123", helpConf.getTitle());
    }

    @Test // unit
    public void testGetStrings() {
        assertEquals(helpConf.getStringsIndices().size(), helpConf.getStrings().size(),
                "Strings and its indices arrays length mismatch");
    }

    @Test // unit
    public void testGetImages() {
        assertEquals(helpConf.getImagesIndices().size(), helpConf.getImages().size(),
                "Images paths and its indices arrays length mismatch");
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
            var fw = new FileWriter(tempFile);
            var bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
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

        assertEquals("Справка: \"Captcha - пазл\"", helpConf.getTitle(),
                "Expected title mismatch");
        assertEquals(2, helpConf.getStrings().size(),
                "Strings count mismatch");
        assertEquals(helpConf.getStringsIndices().size(), helpConf.getStrings().size(),
                "Strings and its indices arrays length mismatch");
        assertEquals(2, helpConf.getImages().size(),
                "Images paths count mismatch");
        assertEquals(helpConf.getImagesIndices().size(), helpConf.getImages().size(),
                "Images paths and its indices arrays length mismatch");
    }
}