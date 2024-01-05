package cw.utils.imgenerator;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ImageTextUtils {
    /**
     * Object containing information about offset of rasterized letter.
     */
    private record OffsetInfo(double offsetX, double offsetY) {}

    public static class RotatedTextObject {
        private final List<WritableImage> images;
        private final List<Integer> imgInRow;
        private final List<OffsetInfo> additionalInfo;
        private double width;
        private double height;

        RotatedTextObject() {
            images = new ArrayList<>();
            imgInRow = new ArrayList<>();
            additionalInfo = new ArrayList<>();
            width = height = 0;
        }

        public double getWidth() { return width; }
        public double getHeight() { return height; }
    }

    /**
     * Makes {@link RotatedTextObject}.
     * @param expr string, which will be rasterized.
     * @param fontName font name.
     * @param fontSize font size.
     * @param constantAngle rotation angle in degrees.
     * @param maxDeviationAngle the angle within which the image will be additionally rotated.
     */
    public static RotatedTextObject makeSpecialTextObject(
            String expr,
            String fontName, int fontSize,
            double constantAngle, double maxDeviationAngle
    ) {
        final double gradToRad = Math.PI / 180d;
        Font font = new Font(fontName, fontSize);

        RotatedTextObject RTO = new RotatedTextObject();
        RTO.imgInRow.add(0);
        int imgInRow = 0;
        int rowIndex = 0;
        double currentTextWidth = 0;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '\n') {    // когда встречаем символ перевода строки
                // устанавливаем количество символов в строке
                RTO.imgInRow.set(rowIndex, imgInRow);
                // переходим на следующую строку
                rowIndex++;
                imgInRow = 0;
                RTO.imgInRow.add(0);
                // обновляем ширину объекта
                if (RTO.width < currentTextWidth)
                    RTO.width = currentTextWidth;
                currentTextWidth = 0d;
                // обновляем высоту объекта
                RTO.height += RTO.images.get(i - 1).getHeight() + RTO.additionalInfo.get(i - 1).offsetY * 2;

                continue;
            }
            imgInRow++;

            // Make javafx Text object, and getting properties from it
            Text text = new Text(String.valueOf(c));
            text.setFont(font);
            double tWidth = text.getLayoutBounds().getWidth();
            double tHeight = text.getLayoutBounds().getHeight();
            double tMaxSize = Math.max(tWidth, tHeight);
            double tOffsetY = text.getBaselineOffset();

            // Drawing javafx Text object on canvas
            Canvas canvas = new Canvas((int)tMaxSize, (int)tMaxSize);
            GraphicsContext gContext = canvas.getGraphicsContext2D();
            gContext.setFill(Color.BLACK);
            gContext.setFont(font);
            gContext.fillText(String.valueOf(c), (tMaxSize - tWidth) * 0.5d, tMaxSize - tHeight + tOffsetY);

            WritableImage wImage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
            canvas.snapshot(new SnapshotParameters(), wImage);
            var iR = wImage.getPixelReader();

            WritableImage wRotated = new WritableImage((int)wImage.getWidth(), (int)wImage.getHeight());
            var rW = wRotated.getPixelWriter();
            var w = wRotated.getWidth();
            var h = wRotated.getHeight();
            var cX = (int)(w / 2d);
            var cY = (int)(h / 2d);
            // fill with white color
            for (int ii = 0; ii < h; ii++)
                for (int jj = 0; jj < w; jj++)
                    rW.setColor(jj, ii, Color.WHITE);
            // rotating an image
            double deviatedAngle = AdditionalMath.mapValue(
                    Math.random(), 0, 1, -maxDeviationAngle, maxDeviationAngle);
            for (int ii = 0; ii < h; ii++)
                for (int jj = 0; jj < w; jj++) {
                    double a = Math.atan2(ii - cY, jj - cX) + (constantAngle + deviatedAngle) * gradToRad;
                    double dist = AdditionalMath.euclideanDistance(new double[]{jj, ii}, new double[]{cX, cY});
                    double fromX = cX + dist * Math.cos(a);
                    double fromY = cY + dist * Math.sin(a);
                    if (fromX < 0 || fromY < 0 || fromX >= w || fromY >= h)
                        continue;

                    int topLeftX = (int) Math.floor(fromX);
                    int topLeftY = (int) Math.floor(fromY);

                    double dx = fromX - topLeftX;
                    double dy = fromY - topLeftY;

                    Color[] pixels = ImageUtils.fillPixelArrayForBilinearInterpolation(iR, fromX, fromY, (int)w, (int)h);
                    rW.setColor(jj, ii, ImageUtils.bilinearInterpolateColor(pixels, dx, dy));
                }

            RTO.images.add(wRotated);
            double offsetX = (tWidth - tMaxSize) * 0.5d;
            double offsetY = tHeight - tMaxSize;
            RTO.additionalInfo.add(new OffsetInfo(offsetX, offsetY));
            currentTextWidth += wRotated.getWidth() + offsetX * 2d;
        }

        RTO.imgInRow.set(rowIndex, imgInRow);
        if (RTO.width < currentTextWidth)
            RTO.width = currentTextWidth;
        int lastImgIdx = RTO.images.size() - 1;
        RTO.height += RTO.images.get(lastImgIdx).getHeight() + RTO.additionalInfo.get(lastImgIdx).offsetY * 2;

        return RTO;
    }

    /**
     * Draw rasterized text {@link RotatedTextObject} in specified {@link PixelWriter}.
     * @param pW {@link PixelWriter} of canvas, where RTO should be drawn.
     * @param RTO {@link RotatedTextObject}.
     * @param startX start position by x-axis.
     * @param startY start position by y-axis.
     * @param maxDeviationX x-axis deviation in percent.
     * @param maxDeviationY y-axis deviation in percent.
     */
    public static void drawRTO(
            PixelWriter pW,
            RotatedTextObject RTO,
            double startX, double startY,
            double maxDeviationX, double maxDeviationY
    ) throws IllegalArgumentException {
        if (maxDeviationX < 0 || maxDeviationX > 1 || maxDeviationY < 0 || maxDeviationY > 1)
            throw new IllegalArgumentException("Percent should be in range [0, 1].");

        int image_ptr = 0;
        double globalOffsetX = startX;
        double globalOffsetY = startY;
        for (int row = 0; row < RTO.imgInRow.size(); row++) {
            double maxImageHeight = 0;
            for (int column = 0; column < RTO.imgInRow.get(row); column++) {
                WritableImage image = RTO.images.get(image_ptr);
                if (maxImageHeight < image.getHeight())
                    maxImageHeight = image.getHeight();

                PixelReader pr = image.getPixelReader();
                double localOffsetX = RTO.additionalInfo.get(image_ptr).offsetX();
                double localOffsetY = RTO.additionalInfo.get(image_ptr).offsetY();

                { // add deviation factor
                    double devX = image.getWidth() * maxDeviationX;
                    double devY = image.getHeight() * maxDeviationY;
                    double r = Math.random();
                    localOffsetX += AdditionalMath.mapValue(r, 0, 1, -devX, devX);
                    r = Math.random();
                    localOffsetY += AdditionalMath.mapValue(r, 0, 1, -devY, devY);
                }

                for (int i = 0; i < image.getHeight(); i++)
                    for (int j = 0; j < image.getWidth(); j++) {
                        Color color = pr.getColor(j, i);
                        if (color.equals(Color.WHITE))
                            continue;
                        pW.setColor((int)(globalOffsetX + j + localOffsetX), (int)(globalOffsetY + i + localOffsetY), color);
                    }

                globalOffsetX += image.getWidth() + localOffsetX * 2d;
                image_ptr++;
            }
            globalOffsetY += maxImageHeight;
            globalOffsetX = startX;
        }
    }
}
