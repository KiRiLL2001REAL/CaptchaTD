package cw.controllers;

import cw.logic.PuzzleCaptchaLogic;
import cw.controls.CustomImageView;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

import java.net.URL;

public class PuzzleCaptchaController extends BasicCaptchaController implements ICaptchaController {
    public static final String IMG_FOLDER = "/images/puzzle";

    @FXML protected GridPane gridPane;
    @FXML protected AnchorPane mainAnchorPane;
    CustomImageView[] mImageViews;

    protected int countImages;

    @Override
    public void initialize() throws Exception {
        int countImages = 0;
        boolean countCalculated = false;
        while (!countCalculated) {
            URL url = getClass().getResource(IMG_FOLDER + "/" + countImages + ".png");
            if (url == null) {
                countCalculated = true;
                continue;
            }
            countImages++;
        }
        if (countImages < 1) {
            throw new Exception("Too few files in \"" + IMG_FOLDER + "\". There are only 0.."
                    + (countImages - 1) + " pngs found. At least 1 required.");
        }
        this.countImages = countImages;

        refreshCaptchaWindow();

        checkButton.setOnAction(actionEvent -> checkAnswer());
        refreshButton.setOnAction(actionEvent -> refreshCaptchaWindow());
    }

    @Override
    public void checkAnswer() {
        if (((PuzzleCaptchaLogic)(captcha)).checkAnswer()) {
            flagLabel.setText("Верно");
            flagLabel.setTextFill(Color.GREEN);
        }
        else {
            flagLabel.setText("Не верно");
            flagLabel.setTextFill(Color.RED);
        }
    }

    @Override
    public void refreshCaptchaWindow() {
        GridPane mGridPane = new GridPane();

        int numRows = 2 + (int)(Math.random() * 10) % 2;
        int numCols = 2 + (int)(Math.random() * 10) % 2;
        if (numCols == numRows) {
            if (numCols == 2) {
                int r = (int) (Math.random() * 10) % 2;
                if (r == 0) numRows++;
                else numCols++;
            }
        }

        if (numRows == 3) { mGridPane.setPrefHeight(402); mGridPane.setMaxHeight(402); }
        else              { mGridPane.setPrefHeight(400); mGridPane.setMaxHeight(400); }
        if (numCols == 3) { mGridPane.setPrefWidth(402); mGridPane.setMaxWidth(402); }
        else              { mGridPane.setPrefWidth(400); mGridPane.setMaxWidth(400); }

        mGridPane.setLayoutX((mainAnchorPane.getPrefWidth()  - mGridPane.getPrefWidth() ) / 2);
        mGridPane.setLayoutY((mainAnchorPane.getPrefHeight() - mGridPane.getPrefHeight()) / 2);
        mGridPane.setGridLinesVisible(true);

        double percentWidth = 100.d / numCols;
        double percentHeight = 100.d / numRows;
        for (int i = 0; i < numRows; i++) {
            var rc = new RowConstraints();
            rc.setPercentHeight(percentHeight);
            rc.setValignment(VPos.CENTER);
            mGridPane.getRowConstraints().add(rc);
        }
        for (int i = 0; i < numCols; i++) {
            var cc = new ColumnConstraints();
            cc.setPercentWidth(percentWidth);
            cc.setHalignment(HPos.CENTER);
            mGridPane.getColumnConstraints().add(cc);
        }

        int imIdx = (int)(Math.random() * 1000) % countImages;
        captcha = new PuzzleCaptchaLogic(
                numRows, numCols,
                getClass().getResource(IMG_FOLDER + "/" + imIdx + ".png"));


        final double imWidth =  mGridPane.getPrefWidth()  * 0.01 * percentWidth;
        final double imHeight = mGridPane.getPrefHeight() * 0.01 * percentHeight;
        mImageViews = new CustomImageView[numRows * numCols];
        int idx = 0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                CustomImageView cImgView = new CustomImageView(idx);
                cImgView.setImage(((PuzzleCaptchaLogic)(captcha)).getImage(i, j));
                cImgView.resize(imWidth, imHeight);
                cImgView.setFitWidth(imWidth - 2);
                cImgView.setFitHeight(imHeight - 2);
                cImgView.setOnMouseClicked(event -> {
                    var source = (CustomImageView) event.getSource();
                    ((PuzzleCaptchaLogic)(captcha)).updateIndex(source.index);
                    source.setImage(((PuzzleCaptchaLogic)(captcha)).getImage(source.index));
                });

                mImageViews[idx] = cImgView;
                mGridPane.add(mImageViews[idx], j, i);
                idx++;
            }
        }

        mainAnchorPane.getChildren().removeAll(gridPane);
        mainAnchorPane.getChildren().add(mGridPane);
        gridPane = mGridPane;

        resetFields();
    }

    @Override
    public void resetFields() {
        flagLabel.setTextFill(Color.GRAY);
        flagLabel.setText("---");
    }
}
