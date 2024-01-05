package cw.utils.imgenerator;

import cw.Main;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import org.junit.Before;
import org.junit.Test;
import javafx.scene.paint.Color;
import org.testfx.framework.junit.ApplicationTest;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static for_testing.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ImageUtilsTest extends ApplicationTest {

    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class);
    }

    @Test // unit
    public void testBilinearInterpolateColor() {
        Color[] pixels = new Color[4];
        pixels[0] = new Color(0.04, 0.08, 0.92, 1.0); // ul
        pixels[1] = new Color(0.56, 0.29, 0.33, 1.0); // ur
        pixels[2] = new Color(0.09, 0.56, 0.06, 1.0); // br
        pixels[3] = new Color(0.77, 0.23, 0.77, 1.0); // bl
        double dx = 0.2;
        double dy = 0.7;

        Color actual = ImageUtils.bilinearInterpolateColor(pixels, dx, dy);
        Color expected = new Color(0.4870, 0.2438, 0.6802, 1.0);
        assertEquals(expected, actual);
    }

    @Test // unit
    public void testGenerateRandomColor() {
        Color color = ImageUtils.generateRandomColor();
        assertNotNull(color, "Object is null");
    }

    @Test // unit
    public void testGenUniqueRandColorsNegativeCount() {
        var randThread = ThreadLocalRandom.current();
        var colorToAvoid = ImageUtils.generateRandomColor();
        int count = -Math.abs(randThread.nextInt());
        ArrayList<Color> colors = ImageUtils.generateUniqueRandomColors(count, colorToAvoid);
        assertNotNull(colors, "Object is null");
        assertTrue(colors.isEmpty(), "List of colors is not empty, meanwhile count is negative");
    }

    @Test // unit
    public void testGenUniqueRandColorsNormal() {
        var randThread = ThreadLocalRandom.current();
        var colorToAvoid = ImageUtils.generateRandomColor();
        int count = Math.abs(randThread.nextInt()) % 1024;
        ArrayList<Color> colors = ImageUtils.generateUniqueRandomColors(count, colorToAvoid);
        assertNotNull(colors, "Object is null");
        assertEquals(count, colors.size(), "Number of generated colors didn't match with passed count");
        assertEquals(-1, colors.indexOf(colorToAvoid), "Avoided color is in resulting list");

        boolean duplicate = false;
        var hashes = new HashSet<Integer>();
        for (var color : colors) {
            int hash = color.hashCode();
            if (hashes.contains(hash)) {
                duplicate = true;
                break;
            }
            hashes.add(hash);
        }
        assertFalse(duplicate, "Found duplicate color");
    }

    @Test // unit
    public void testGenUniqueRandColorsLargeCount() {
        var randThread = ThreadLocalRandom.current();
        var colorToAvoid = ImageUtils.generateRandomColor();
        int count;
        do {
            count = Math.abs(randThread.nextInt());
        } while (count < 1024);
        int finalCount = count;
        assertThrows(IllegalArgumentException.class, () -> ImageUtils.generateUniqueRandomColors(finalCount, colorToAvoid));
    }

    @Test // unit
    public void testMix() {
        Color c1 = new Color(1.0, 0.0, 0.2, 1.0);
        Color c2 = new Color(0.0, 1.0, 0.8, 1.0);
        double factor = 0.5;

        Color actual = ImageUtils.mix(c1, c2, factor);
        Color expected = new Color(0.5, 0.5, 0.5, 1.0);
        assertEquals(expected, actual);
    }

    @Test // unit
    public void testDrawPrimitives() {
        var snapshotRef = new AtomicReference<WritableImage>();
        var canvasRef = new AtomicReference<Canvas>();
        GraphicsContext gc;
        final Color BACKGROUND_COLOR = ImageUtils.generateRandomColor();

        // проверка правильности конвертации цвета argb в хеш (тест эквивалентности Color::hashCode() и argbToHash())

        canvasRef.set(new Canvas(1, 1));
        gc = canvasRef.get().getGraphicsContext2D();

        Color color = ImageUtils.generateRandomColor();
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, 1, 1);
        gc.setFill(color);
        gc.fillRect(0, 0, 1, 1);
        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));
        int argb = snapshotRef.get().getPixelReader().getArgb(0, 0); // цвет забирается из снапшота в формате argb

        assertEquals(color.hashCode(), argbToHash(argb),
                "Check Color::hashCode() and fix argbToHash function.");

        // Зальём изображение указанным цветом, и убедимся, что заливка выполнена

        final int WIDTH, HEIGHT;    WIDTH = HEIGHT = 128;
        canvasRef.set(new Canvas(WIDTH, HEIGHT));
        gc = canvasRef.get().getGraphicsContext2D();

        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));

        assertFalse(findDifferentColor(BACKGROUND_COLOR, snapshotRef.get()),
                "There is color, different from background");

        // Сгенерируем случайный набор различных цветов в количестве доступных примитивов, не схожих с цветом фона.
        // Нарисуем несколько примитивов каждого цвета, и убедимся, что что-либо выводится.

        int count = ImageUtils.PrimitiveType.values().length;
        ArrayList<Color> colors = ImageUtils.generateUniqueRandomColors(count, BACKGROUND_COLOR);
        ArrayList<ImageUtils.PrimitiveType> primitives =
                new ArrayList<>(Arrays.asList(ImageUtils.PrimitiveType.values()));
        Collections.shuffle(primitives);
        for (int i = 0; i < count; i++) {
            var primitiveType = primitives.get(i);
            if (primitiveType == ImageUtils.PrimitiveType.none) // пропускаем, если тип 'none'
                continue;
            // рисуем примитив
            ImageUtils.drawPrimitive(gc, primitiveType, List.of(colors.get(i), colors.get(i)));
        }

        // проверка того, что примитивы нарисованы
        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));
        int foundedColors = 0;
        for (int i = 0; i < count; i++) {
            var primitiveType = primitives.get(i);
            if (primitiveType == ImageUtils.PrimitiveType.none) // пропускаем, если тип 'none'
                continue;
            if (findThisColor(colors.get(i), snapshotRef.get()))
                foundedColors++;
        }

        assertTrue(foundedColors > 0,
                "No primitives are visible. May be it is false-positive detection?");
    }
}