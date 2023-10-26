package cw.utils.imgenerator;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Random;

import static cw.utils.imgenerator.AdditionalMath.mapValue;

public class ImageUtils {
    /**
     * Makes a color interpolated by 4 specified pixels and a base point in range {@code [0, 1)}.
     * @param pixels array of {@code Color} objects. Pixels should be clockwise ordered
     *               from top-left to bottom-left one.
     * @param dx offset by x-axis.
     * @param dy offset by y-axis.
     */
    public static Color bilinearInterpolateColor(Color[] pixels, double dx, double dy) {
        double[][] channelsOfPixel = new double[4][3];
        for (int i = 0; i < 4; i++) {
            channelsOfPixel[i][0] = pixels[i].getRed();
            channelsOfPixel[i][1] = pixels[i].getGreen();
            channelsOfPixel[i][2] = pixels[i].getBlue();
        }
        double[] res = new double[3];
        for (int i = 0; i < 3; i++)
             res[i] = channelsOfPixel[0][i] * (1d - dx) * (1d - dy) +
                     channelsOfPixel[1][i] * dx * (1d - dy) +
                     channelsOfPixel[2][i] * dx * dy +
                     channelsOfPixel[3][i] * (1d - dx) * dy;
        return new Color(res[0], res[1], res[2], 1);
    }

    public static Color[] fillPixelArrayForBilinearInterpolation(
            PixelReader pr,
            double basePointX, double basePointY,
            int imageWidth, int imageHeight
    ) {
        int bottomRightX = (int) Math.floor(basePointX) + 1;
        int bottomRightY = (int) Math.floor(basePointY) + 1;
        //  Почему не topLeft? - Дело в том, что во всех блоках if встречаются конструкции
        // типа topLeft + 1. Использование bottomRight чуточку ускоряет работу программы.
        /* TODO Можно оптимизировать кол-во вызовов стороннего кода через использование метода getArgb() */
        Color[] pixels = new Color[4];
        pixels[0] = pr.getColor(bottomRightX - 1, bottomRightY - 1);
        if (bottomRightX < imageWidth)
            pixels[1] = pr.getColor(bottomRightX, bottomRightY - 1);
        else pixels[1] = pixels[0];

        if (bottomRightY < imageHeight)
            pixels[3] = pr.getColor(bottomRightX - 1, bottomRightY);
        else pixels[3] = pixels[0];

        if (bottomRightX < imageWidth)
            if (bottomRightY < imageHeight)
                pixels[2] = pr.getColor(bottomRightX, bottomRightY);
            else
                pixels[2] = pixels[1];
        else
            if (bottomRightY < imageHeight)
                pixels[2] = pixels[3];
            else
                pixels[2] = pixels[0];

        return pixels;
    }

    /**
     * Makes bulge or pitch effect, relying on the value of {@code strength} param.
     * @param image picture to handle.
     * @param cX the center of the effect on the {@code X} axis.
     * @param cY the center of the effect on the {@code Y} axis.
     * @param strength value in range {@code [-1, 1]}.
     *                 <p><b>WARNING</b> value outside this range may cause unexpected behavior.
     * @param radius the distance in pixels, beyond which the effect will not be applied.
     * @return {@link WritableImage} with applied effect.
     */
    public static WritableImage bulgePitchEffect(Image image, double cX, double cY, double strength, double radius) {
        var pr = image.getPixelReader();

        var width = image.getWidth();
        var height = image.getHeight();
        var wImage = new WritableImage((int)width, (int)height);

        var pw = wImage.getPixelWriter();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                double dist = AdditionalMath.euclideanDistance(new double[]{j, i}, new double[]{cX, cY});
                if (dist >= radius || dist == 0) {
                    pw.setColor(j, i, pr.getColor(j, i));
                    continue;
                }

                double percent = dist / radius;
                double parentDist = dist;
                if (strength > 0) {
                    double arg = AdditionalMath.smoothStep(0d, radius / dist, percent);
                    parentDist *= AdditionalMath.mix(1d, arg, strength * 0.75d);
                }
                else if (strength < 0) {
                    double arg = Math.pow(percent, 1d + strength * 0.75d) * radius / dist;
                    parentDist *= AdditionalMath.mix(1d, arg, 1d - percent);
                }

                double angle = Math.atan2(i - cY, j - cX);
                double fromX = cX + parentDist * Math.cos(angle);
                double fromY = cY + parentDist * Math.sin(angle);
                if (fromX < 0 || fromY < 0 || fromX >= width || fromY >= height)
                    continue;

                int topLeftX = (int) Math.floor(fromX);
                int topLeftY = (int) Math.floor(fromY);

                double dx = fromX - topLeftX;
                double dy = fromY - topLeftY;

                Color[] pixels = fillPixelArrayForBilinearInterpolation(pr, fromX, fromY, (int)width, (int)height);

                pw.setColor(j, i, bilinearInterpolateColor(pixels, dx, dy));
            }
        }

        return wImage;
    }

    /**
     * Make {@link WritableImage} array of chars from String {@code expr}.
     * <p><b>WARNING</b> the {@code '\n'} character producing {@code null} {@link WritableImage},
     * so you need to manually handle the line feed.
     * @param expr string to convert.
     * @param fontName name of font.
     * @param fontSize size of font.
     */
    public static WritableImage[] rasterizeTextByLetters(String expr, String fontName, int fontSize) {
        Font font = new Font(fontName, fontSize);
        Text text;

        int size = expr.length();
        WritableImage[] result = new WritableImage[size];
        for (int i = 0; i < size; i++) {
            char c = expr.charAt(i);
            if (c == '\n') {
                result[i] = null;
                continue;
            }

            text = new Text(String.valueOf(c));
            text.setFont(font);
            double tWidth = text.getLayoutBounds().getWidth();
            double tHeight = text.getLayoutBounds().getHeight();
            double tOffsetY = text.getBaselineOffset();

            Canvas canvas = new Canvas((int)tWidth, (int)tHeight);
            GraphicsContext gContext = canvas.getGraphicsContext2D();
            gContext.setFill(Color.BLACK);
            gContext.setFont(font);
            gContext.fillText(String.valueOf(c), 0, tOffsetY);

            result[i] = new WritableImage((int)tWidth, (int)tHeight);
            canvas.snapshot(new SnapshotParameters(), result[i]);
        }

        return result;
    }

    public enum PrimitiveType {
        none,
        strokeRect, strokeArc, strokeLine, strokeOval, strokeRoundRect,
        fillRect,   fillArc,               fillOval,   fillRoundRect
    }

    public static void drawPrimitive(double cWidth, double cHeight, GraphicsContext gContext,
                                     PrimitiveType primitive, int count)
    {
        if (primitive == PrimitiveType.none)
            return;

        final double minSizeX = cWidth * 0.05d;
        final double minSizeY = cHeight * 0.05d;

        gContext.save();

        double[] arg = new double[6];
        for (int i = 0; i < count; i++) {
            gContext.setStroke(generateRandomColor(false));
            gContext.setFill(generateRandomColor(false));
            gContext.setLineWidth((int)(Math.random() * cWidth * 0.02) + 1);

            arg[0] = mapValue(Math.random(), 0, 1,
                    -cWidth * 0.1d, cWidth * 1.1d);  // x
            arg[1] = mapValue(Math.random(), 0, 1,
                    -cHeight * 0.1d, cHeight * 1.1d); // y
            arg[2] = mapValue(Math.random(), 0, 1,
                    minSizeX, cWidth * 0.5d);          // w
            arg[3] = mapValue(Math.random(), 0, 1,
                    minSizeY, cHeight * 0.5d);         // h
            switch (primitive) {
                case strokeRect -> gContext.strokeRect(arg[0], arg[1], arg[2], arg[3]);
                case fillRect -> gContext.fillRect(arg[0], arg[1], arg[2], arg[3]);
                case strokeOval -> gContext.strokeOval(arg[0], arg[1], arg[2], arg[3]);
                case fillOval -> gContext.fillOval(arg[0], arg[1], arg[2], arg[3]);
                case strokeLine -> {
                    arg[2] = mapValue(Math.random(), 0, 1,
                            -cWidth * 0.1d, cWidth * 1.1d);  // x2
                    arg[3] = mapValue(Math.random(), 0, 1,
                            -cHeight * 0.1d, cHeight * 1.1d); // y2
                    gContext.strokeLine(arg[0], arg[1], arg[2], arg[3]);
                }
                case strokeArc, fillArc -> {
                    arg[4] = Math.random() * 360;
                    arg[5] = Math.random() * 360;
                    int r = (int) (Math.random() * 3);
                    final ArcType[] a = new ArcType[3];
                    a[0] = ArcType.OPEN;
                    a[1] = ArcType.CHORD;
                    a[2] = ArcType.ROUND;
                    if (primitive == PrimitiveType.strokeArc)
                        gContext.strokeArc(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5], a[r]);
                    else  gContext.fillArc(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5], a[r]);
                }
                case strokeRoundRect, fillRoundRect -> {
                    arg[4] = arg[5] = minSizeX * mapValue(Math.random(), 0, 1,
                            0.2, 5);
                    if (primitive == PrimitiveType.strokeRoundRect)
                        gContext.strokeRoundRect(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5]);
                    else  gContext.fillRoundRect(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5]);
                }
            }
        }

        gContext.restore();
    }

    private static final double MIN_COLOR_FACTOR = 0.1d;
    private static final double MAX_COLOR_FACTOR = 0.9d;

    public static Color generateRandomColor(boolean hasTransparency) {
        Random rng = new Random();
        int c = rng.nextInt();
        float r = (float)mapValue(c & 255, 0, 255,
                MIN_COLOR_FACTOR, MAX_COLOR_FACTOR);
        float g = (float)mapValue((c >>> 8) & 255, 0, 255,
                MIN_COLOR_FACTOR, MAX_COLOR_FACTOR);
        float b = (float)mapValue((c >>> 16) & 255, 0, 255,
                MIN_COLOR_FACTOR, MAX_COLOR_FACTOR);
        if (hasTransparency) {
            float o = (float) mapValue((c >>> 24) & 255, 0, 255,
                    0, 1);
            return new Color(r, g, b, o);
        }

        return new Color(r, g, b, 1);
    }

    /**
     * Mix colors with specified factor.
     * @param A primary color to blend.
     * @param B secondary color to blend.
     * @param factor value in range {@code [0d, 1d]}. If the value equals 0, then the resulting color is A. If it
     *               equals 1, then result is B. I.e. {@code factor} variable shows how much color A will overlapped
     *               by color B.
     *               <p><b>WARNING!</b> Value outside of range {@code [0d, 1d]} may cause unexpected behavior.
     * @return mixed color.
     */
    public static Color mix(Color A, Color B, double factor) {
        double factor_inv = 1.d - factor;

        int rgbaA = A.hashCode();
        int rgbaB = B.hashCode();
        int rA = (rgbaA >> 24) & 0xFF;
        int gA = (rgbaA >> 16) & 0xFF;
        int bA = (rgbaA >> 8 ) & 0xFF;
        int rB = (rgbaB >> 24) & 0xFF;
        int gB = (rgbaB >> 16) & 0xFF;
        int bB = (rgbaB >> 8 ) & 0xFF;

        int rC = (int)((double)(rA) * factor_inv + (double)(rB) * factor);
        int gC = (int)((double)(gA) * factor_inv + (double)(gB) * factor);
        int bC = (int)((double)(bA) * factor_inv + (double)(bB) * factor);

        return Color.rgb(rC,gC,bC,1);
    }
}