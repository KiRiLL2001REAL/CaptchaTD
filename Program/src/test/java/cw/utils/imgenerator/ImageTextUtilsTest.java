package cw.utils.imgenerator;

import cw.Main;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.atomic.AtomicReference;

import static for_testing.TestingUtils.findDifferentColor;
import static for_testing.TestingUtils.findThisColor;
import static org.junit.Assert.*;

public class ImageTextUtilsTest extends ApplicationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class);
    }

    @Test // unit
    public void TestMakeSpecialTextObject() {
        var rtoRef = new AtomicReference<ImageTextUtils.RotatedTextObject>();
        interact(() -> rtoRef.set(ImageTextUtils.makeSpecialTextObject("123",
                "Times New Roman", 16, 15, 30)));

        assertNotNull("Returned object is null", rtoRef.get());
        assertTrue("Width <= 0", rtoRef.get().getWidth() > 0);
        assertTrue("Height <= 0", rtoRef.get().getHeight() > 0);
    }

    @Test // unit
    public void TestDrawRTO() {
        var snapshotRef = new AtomicReference<WritableImage>();
        var canvasRef = new AtomicReference<Canvas>();
        GraphicsContext gc;

        var rtoRef = new AtomicReference<ImageTextUtils.RotatedTextObject>();
        interact(() -> rtoRef.set(ImageTextUtils.makeSpecialTextObject("123",
                "Times New Roman", 16, 15, 30)));

        double deviation = 16;
        int width = (int) (rtoRef.get().getWidth() + deviation * 2);
        int height = (int) (rtoRef.get().getHeight() + deviation * 2);

        canvasRef.set(new Canvas(width, height));
        gc = canvasRef.get().getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));
        assertFalse("There is color, different from WHITE",
                findDifferentColor(Color.WHITE, snapshotRef.get()));

        ImageTextUtils.drawRTO(gc.getPixelWriter(), rtoRef.get(),
                deviation, deviation, deviation / width, deviation / height);
        interact(() -> snapshotRef.set(canvasRef.get().snapshot(new SnapshotParameters(), null)));

        assertTrue("Text was not rasterized",
                findThisColor(Color.BLACK, snapshotRef.get()));
    }
}