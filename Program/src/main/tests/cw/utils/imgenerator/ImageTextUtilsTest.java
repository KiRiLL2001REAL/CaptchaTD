package cw.utils.imgenerator;

import cw.Main;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.atomic.AtomicReference;

import static for_testing.TestUtils.findDifferentColor;
import static for_testing.TestUtils.findThisColor;
import static org.junit.jupiter.api.Assertions.*;

public class ImageTextUtilsTest extends ApplicationTest {

    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class);
    }

    @Test // unit
    public void TestMakeSpecialTextObject() {
        var rtoRef = new AtomicReference<ImageTextUtils.RotatedTextObject>();
        interact(() -> rtoRef.set(ImageTextUtils.makeSpecialTextObject("123", "Times New Roman", 16, 15, 30)));

        assertNotNull(rtoRef, "Returned object is null");
        assertTrue(rtoRef.get().getWidth() > 0, "Width <= 0");
        assertTrue(rtoRef.get().getHeight() > 0, "Height <= 0");
    }

    @Test // unit
    public void TestDrawRTO() {
        var snapshotRef = new AtomicReference<WritableImage>();
        var canvasRef = new AtomicReference<Canvas>();
        GraphicsContext gc;

        var rtoRef = new AtomicReference<ImageTextUtils.RotatedTextObject>();
        interact(() -> rtoRef.set(ImageTextUtils.makeSpecialTextObject("123", "Times New Roman", 16, 15, 30)));

        double deviation = 16;
        int width = (int) (rtoRef.get().getWidth() + deviation * 2);
        int height = (int) (rtoRef.get().getHeight() + deviation * 2);

        canvasRef.set(new Canvas(width, height));
        gc = canvasRef.get().getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));
        assertFalse(findDifferentColor(Color.WHITE, snapshotRef.get()),
                "There is color, different from WHITE");

        ImageTextUtils.drawRTO(gc.getPixelWriter(), rtoRef.get(), deviation, deviation, deviation / width, deviation / height);

        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));
        assertTrue(findThisColor(Color.BLACK, snapshotRef.get()),
                "Text was not rasterized");
    }
}