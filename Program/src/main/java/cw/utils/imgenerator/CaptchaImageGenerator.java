package cw.utils.imgenerator;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static cw.utils.imgenerator.AdditionalMath.mapValue;
import static cw.utils.imgenerator.ImageUtils.*;

public class CaptchaImageGenerator {

    private static WritableImage randomBulgePitchEffect(
            Image image,
            double minX, double maxX,
            double minY, double maxY,
            double minS, double maxS,
            double minR, double maxR
    ) {
        double rX = mapValue(Math.random(), 0, 1, minX, maxX);
        double rY = mapValue(Math.random(), 0, 1, minY, maxY);
        double rS = mapValue(Math.random(), 0, 1, minS, maxS);
        double rR = mapValue(Math.random(), 0, 1, minR, maxR);
        return ImageUtils.bulgePitchEffect(image, rX, rY, rS, rR);
    }

    public static Image generateImage(
            String expression,
            int fontSize,
            int width,
            int height
    ) {
        final Color BACKGROUND_COLOR = Color.WHITE;
        final int DRAW_PASSES = 2;
        final int ELEMENT_COUNT_ORIGIN = 1;
        final int ELEMENT_COUNT_BOUND = 5;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gContext = canvas.getGraphicsContext2D();

        gContext.setFill(BACKGROUND_COLOR);
        gContext.fillRect(0, 0, width, height);

        // draw background
        ArrayList<PrimitiveType> primitives = new ArrayList<>(Arrays.asList(PrimitiveType.values()));
        Collections.shuffle(primitives);
        for (int i = 0; i < DRAW_PASSES; i++) {
            for (var primitiveType : primitives) {
                int count = ThreadLocalRandom.current().nextInt(ELEMENT_COUNT_ORIGIN, ELEMENT_COUNT_BOUND);
                ArrayList<Color> colors = generateUniqueRandomColors(count, BACKGROUND_COLOR);
                drawPrimitive(gContext, primitiveType, colors);
            }
        }

        // drawing expression
        var RTO = ImageTextUtils.makeSpecialTextObject(
                expression,
                "Times New Roman",
                fontSize,
                0, 20);
        double tX = (width  - RTO.getWidth())  / 2;
        double tY = (height - RTO.getHeight()) / 2;

        Canvas textCanvas = new Canvas(width, height);
        GraphicsContext textGraphicsContext = textCanvas.getGraphicsContext2D();
        PixelWriter pwT = textGraphicsContext.getPixelWriter();

        ImageTextUtils.drawRTO(
                pwT, RTO,
                tX, tY,
                0.07, 0.15);

        // matching text and background together
        WritableImage wImage = new WritableImage(width, height);
        WritableImage wText = new WritableImage(width, height);
        canvas.snapshot(new SnapshotParameters(), wImage);
        textCanvas.snapshot(new SnapshotParameters(), wText);
        // The rule is:
        //   if the color of the wText pixel isn't white, then the pixel color of
        //   the wImage becomes inverted
        PixelWriter pwI = wImage.getPixelWriter();
        PixelReader prI = wImage.getPixelReader();
        PixelReader prT = wText.getPixelReader();
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                var colorT = prT.getColor(j, i);
                if (!colorT.equals(Color.WHITE)) {
                    var colorI = prI.getColor(j, i);
                    var colorI_inv = colorI.invert();
                    if (colorT.equals(Color.BLACK)) {
                        pwI.setColor(j, i, colorI_inv);
                        continue;
                    }
                    Color blended = ImageUtils.mix(colorI_inv, colorI, colorT.getRed());
                    pwI.setColor(j, i, blended);
                }
            }

        var effected = randomBulgePitchEffect(
                wImage,
                0, width - 1,
                0, height - 1,
                -0.4, -0.1,
                100, Math.min(width, height));

        // borders
        var pwE = effected.getPixelWriter();
        for (int i = 0; i < height; i++) {
            pwE.setColor(0, i, Color.BLACK);
            pwE.setColor(width - 1, i, Color.BLACK);
        }
        for (int j = 0; j < width; j++) {
            pwE.setColor(j, 0, Color.BLACK);
            pwE.setColor(j, height - 1, Color.BLACK);
        }

        return effected;
    }
}
