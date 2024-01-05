package cw.utils.imgenerator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static cw.utils.imgenerator.AdditionalMath.mapValue;

public class ImageUtils {
    /**
     * Makes a color interpolated by 4 specified pixels and a base point in range {@code [0, 1)}.
     * @param pixels array of {@code Color} objects. Pixels should be clockwise ordered
     *               from top-left to bottom-left one.
     * @param dx offset by x-axis.
     * @param dy offset by y-axis.
     */
    public static Color bilinearInterpolateColor(
            Color[] pixels,
            double dx,
            double dy
    ) {
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
        else
            pixels[1] = pixels[0];

        if (bottomRightY < imageHeight)
            pixels[3] = pr.getColor(bottomRightX - 1, bottomRightY);
        else
            pixels[3] = pixels[0];

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
    public static WritableImage bulgePitchEffect(
            Image image,
            double cX,
            double cY,
            double strength,
            double radius
    ) {
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

    public enum PrimitiveType {
        none,
        fillRect,   fillArc,               fillOval,   fillRoundRect,
        strokeRect, strokeArc, strokeLine, strokeOval, strokeRoundRect
    }

    public static void drawPrimitive(
            final GraphicsContext gContext,
            final PrimitiveType primitive,
            final List<Color> colors
    ) {
        if (primitive == PrimitiveType.none)
            return;

        final double areaWidth = gContext.getCanvas().getWidth();
        final double areaHeight = gContext.getCanvas().getHeight();
        final double minSizeX = areaWidth * 0.05d;
        final double minSizeY = areaHeight * 0.05d;

        gContext.save();

        double[] arg = new double[6];
        for (Color color : colors) {
            gContext.setStroke(color);
            gContext.setFill(color);
            gContext.setLineWidth((int) (Math.random() * Math.max(areaWidth, areaHeight) * 0.02) + 1);

            arg[0] = mapValue(Math.random(), 0, 1,
                    areaWidth * -0.1d, areaWidth * 1.1d);  // x
            arg[1] = mapValue(Math.random(), 0, 1,
                    areaHeight * -0.1d, areaHeight * 1.1d); // y
            arg[2] = mapValue(Math.random(), 0, 1,
                    minSizeX, areaWidth * 0.5d);          // w
            arg[3] = mapValue(Math.random(), 0, 1,
                    minSizeY, areaHeight * 0.5d);         // h
            switch (primitive) {
                case strokeRect -> gContext.strokeRect(arg[0], arg[1], arg[2], arg[3]);
                case fillRect -> gContext.fillRect(arg[0], arg[1], arg[2], arg[3]);
                case strokeOval -> gContext.strokeOval(arg[0], arg[1], arg[2], arg[3]);
                case fillOval -> gContext.fillOval(arg[0], arg[1], arg[2], arg[3]);
                case strokeLine -> {
                    arg[2] = mapValue(Math.random(), 0, 1,
                            -areaWidth * 0.1d, areaWidth * 1.1d);  // x2
                    arg[3] = mapValue(Math.random(), 0, 1,
                            -areaHeight * 0.1d, areaHeight * 1.1d); // y2
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
                    else gContext.fillArc(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5], a[r]);
                }
                case strokeRoundRect, fillRoundRect -> {
                    arg[4] = arg[5] = minSizeX * mapValue(Math.random(), 0, 1,
                            0.2, 5);
                    if (primitive == PrimitiveType.strokeRoundRect)
                        gContext.strokeRoundRect(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5]);
                    else gContext.fillRoundRect(arg[0], arg[1], arg[2], arg[3], arg[4], arg[5]);
                }
                default -> {/* nope */}
            }
        }

        gContext.restore();
    }

    private static final int MIN_COLOR_VALUE =  54;
    private static final int MAX_COLOR_VALUE = 230;

    public static Color generateRandomColor() {
        int randVal = ThreadLocalRandom.current().nextInt();
        int r = Math.min(MAX_COLOR_VALUE, Math.max(MIN_COLOR_VALUE, (randVal      ) & 255));
        int g = Math.min(MAX_COLOR_VALUE, Math.max(MIN_COLOR_VALUE, (randVal >>  8) & 255));
        int b = Math.min(MAX_COLOR_VALUE, Math.max(MIN_COLOR_VALUE, (randVal >> 16) & 255));
        return Color.rgb(r, g, b, 1);
    }

    /***
     * Utility method, that generates different colors.
     * @param count required count. The value must be less than <code>2^10</code>.
     *              <p> Non-positive value will produce an empty {@link ArrayList}<{@link Color}>.
     * @param colorToAvoid color, that not to be generated.
     * @return {@link ArrayList}<{@link Color}> with different values.
     */
    public static ArrayList<Color> generateUniqueRandomColors(
            int count,
            Color colorToAvoid
    ) throws IllegalArgumentException {
        if (count > 1024)
            throw new IllegalArgumentException(String.format("Parameter 'count' is %d (not less than 2^10)", count));
        count = Math.max(0, count);

        ArrayList<Integer> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            list.add(i);
        Collections.shuffle(list);

        var storedHashes = new HashSet<Integer>(count + 1);
        storedHashes.add(colorToAvoid.hashCode());

        var result = new ArrayList<Color>();
        for (int i = 0; i < count; i++) {
            boolean unique = false;
            Color newColor;
            do {
                newColor = generateRandomColor();
                int newHash = newColor.hashCode();
                if (!storedHashes.contains(newHash)) {
                    storedHashes.add(newHash);
                    unique = true;
                }
            } while (!unique);
            result.add(newColor);
        }

        return result;
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

        double rC = A.getRed()   * factor_inv + B.getRed()   * factor;
        double gC = A.getGreen() * factor_inv + B.getGreen() * factor;
        double bC = A.getBlue()  * factor_inv + B.getBlue()  * factor;

        return new Color(rC, gC, bC,1);
    }
}