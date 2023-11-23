package cw.utils.imgenerator;

import org.junit.jupiter.api.Test;
import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {
    @Test
    void testBilinearInterpolateColor() {
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

    @Test
    void testMix() {
        Color c1 = new Color(1.0, 0.0, 0.2, 1.0);
        Color c2 = new Color(0.0, 1.0, 0.8, 1.0);
        double factor = 0.5;

        Color actual = ImageUtils.mix(c1, c2, factor);
        Color expected = new Color(0.5, 0.5, 0.5, 1.0);
        assertEquals(expected, actual);
    }
}