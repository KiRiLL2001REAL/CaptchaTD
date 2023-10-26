package cw.logic;

import cw.exceptions.PuzzleCaptchaIndexOutOfBoundsException;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.net.URL;
import java.util.Random;

public class PuzzleCaptchaLogic extends CaptchaLogic implements ICaptcha {
    private final int rows;
    private final int cols;
    private final int[][] indices;
    private final int[][][] nextIndices;
    private final Image[] subImages;

    public PuzzleCaptchaLogic(
            int rows, int cols,
            URL imageURL
    ) {
        this.rows = rows;
        this.cols = cols;

        indices = new int[rows][];
        nextIndices = new int[rows][][];
        for (int i = 0; i < rows; i++) {
            indices[i] = new int[cols];
            nextIndices[i] = new int[cols][];
            for (int j = 0; j < cols; j++) {
                // заполняем массивчик следующих индексов.
                if ((int)(Math.random() * 10) % 2 == 0) {
                    nextIndices[i][j] = new int[cols];
                    for (int k = 0; k < cols; k++)
                        nextIndices[i][j][k] = i * cols + k;
                }
                else {
                    nextIndices[i][j] = new int[rows];
                    final int size = rows * cols;
                    for (int k = 0; k < rows; k++)
                        nextIndices[i][j][k] = (i * cols + j + k * cols) % size;
                }

                shuffleArray(nextIndices[i][j]);
                // не должно быть такого, чтобы индекс subImage сразу был правильным
                if (nextIndices[i][j][0] == i * cols + j) {
                    int tmp = nextIndices[i][j][0];
                    int ptr = nextIndices[i][j].length - 1;
                    nextIndices[i][j][0] = nextIndices[i][j][ptr];
                    nextIndices[i][j][ptr] = tmp;
                }
                // заполняем индекс текущего элемента  первым значением из допустимого диапазона индексов
                indices[i][j] = nextIndices[i][j][0];
            }
        }

        Image mainImg = new Image(imageURL.toString());
        int subImgWidth  = (int)mainImg.getWidth()  / cols;
        int subImgHeight = (int)mainImg.getHeight() / rows;
        PixelReader reader = mainImg.getPixelReader();
        subImages = new WritableImage[rows * cols];
        int imgOffsetY = 0;
        for (int i = 0; i < rows; i++) {
            int imgOffsetX = 0;
            int offIdx = i * cols;
            for (int j = 0; j < cols; j++) {
                subImages[offIdx + j] = new WritableImage(reader, imgOffsetX, imgOffsetY, subImgWidth, subImgHeight);
                imgOffsetX += subImgWidth;
            }
            imgOffsetY += subImgHeight;
        }

    }

    // Implementing Fisher–Yates shuffle
    private static void shuffleArray(int[] arr) {
        Random rnd = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }
    }

    public void updateIndex(int row, int col) {
        try {
            checkCorrectnessOfIndices(row, col);
            synchronized (indices) {
                int idx_ptr = -1;
                int size = nextIndices[row][col].length;
                for (int i = 0; i < size; i++)
                    if (nextIndices[row][col][i] == indices[row][col])
                        idx_ptr = i;
                assert idx_ptr > 0;

                idx_ptr = (idx_ptr == size - 1) ? 0 : idx_ptr + 1;
                indices[row][col] = nextIndices[row][col][idx_ptr];
            }

        } catch (PuzzleCaptchaIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateIndex(int index) {
        updateIndex(index / cols, index % cols);
    }

    public Image getImage(int row, int col) {
        try {
            checkCorrectnessOfIndices(row, col);
            return subImages[indices[row][col]];
            //return images[row * cols + col];
        } catch (PuzzleCaptchaIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Image getImage(int index) {
        return getImage(index / cols, index % cols);
    }

    public void checkCorrectnessOfIndices(int row, int col) throws PuzzleCaptchaIndexOutOfBoundsException {
        if (row < 0 || row >= rows)
            throw new PuzzleCaptchaIndexOutOfBoundsException("row", row, rows);
        if (col < 0 || col >= cols)
            throw new PuzzleCaptchaIndexOutOfBoundsException("column", col, cols);
    }

    public boolean checkAnswer() {
        boolean isCorrect = true;
        int size = rows * cols;
        for (int i = 0; i < size; i++) {
            if (indices[i / cols][i % cols] != i) {
                isCorrect = false;
                break;
            }
        }
        return isCorrect;
    }

    @Deprecated
    public Image[] getImages() {
        return null;
    }
}
