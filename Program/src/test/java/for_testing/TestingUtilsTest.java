package for_testing;

import cw.Main;
import cw.utils.imgenerator.ImageUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.atomic.AtomicReference;

import static for_testing.TestingUtils.argbToHash;
import static org.junit.Assert.assertEquals;

public class TestingUtilsTest extends ApplicationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class);
    }

    @Test // unit (для cw.utils.imgenerator.testDrawPrimitives)
    public void testColorRgbaEqualsItsHash() {
        final Color BACKGROUND_COLOR = ImageUtils.generateRandomColor();
        AtomicReference<WritableImage> snapshotRef = new AtomicReference<>();
        var canvas = new Canvas(1, 1);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color color = ImageUtils.generateRandomColor();
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, 1, 1);
        gc.setFill(color);
        gc.fillRect(0, 0, 1, 1);
        interact(() -> snapshotRef.set(canvas.snapshot(new SnapshotParameters(), null)));
        int argb = snapshotRef.get().getPixelReader().getArgb(0, 0); // цвет забирается из снапшота в формате argb

        assertEquals("Check Color::hashCode() and fix argbToHash function.",
                color.hashCode(), argbToHash(argb));
    }
}