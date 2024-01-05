package for_testing;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class TestUtils {
    public static int argbToHash(int argb) {
        return Integer.rotateLeft(argb, 8);
    }

    public static boolean findDifferentColor(Color color, Image image) {
        final int WIDTH = (int) image.getWidth();
        final int HEIGHT = (int) image.getHeight();
        final int COLOR_HASH = color.hashCode();

        final var pr = image.getPixelReader();
        boolean found = false;
        for (int i = 0; i < WIDTH * HEIGHT && !found; i++) {
            var x = i % WIDTH;
            var y = i / WIDTH;
            int hash = argbToHash(pr.getArgb(x, y));
            if (hash != COLOR_HASH)
                found = true;
        }
        return found;
    }

    public static boolean findThisColor(Color color, Image image) {
        final int WIDTH = (int) image.getWidth();
        final int HEIGHT = (int) image.getHeight();
        final int COLOR_HASH = color.hashCode();

        final var pr = image.getPixelReader();
        boolean found = false;
        for (int i = 0; i < WIDTH * HEIGHT && !found; i++) {
            var x = i % WIDTH;
            var y = i / WIDTH;
            int hash = argbToHash(pr.getArgb(x, y));
            if (hash == COLOR_HASH)
                found = true;
        }
        return found;
    }
}
