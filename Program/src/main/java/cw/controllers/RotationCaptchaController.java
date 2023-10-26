package cw.controllers;

import cw.logic.RotationCaptchaLogic;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;

public class RotationCaptchaController extends BasicCaptchaController implements ICaptchaController {
    public static final String IMG_FOLDER = "/images/rotation";

    @FXML protected AnchorPane anchorPane;
    @FXML protected ImageView patternView;
    @FXML protected ImageView rotatableView;
    @FXML protected Label hintPatternLabel;
    @FXML protected Label hintRotatableLabel;
    @FXML protected Slider rotationSlider;

    protected int countImages;

    @FXML
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
        if (countImages < 2) {
            throw new Exception("Too few files in \"" + IMG_FOLDER + "\". There are only 0.."
                    + (countImages - 1) + " pngs found. At least 2 required.");
        }
        this.countImages = countImages;

        refreshCaptchaWindow();

        checkButton.setOnAction(actionEvent -> checkAnswer());
        rotationSlider.setOnKeyPressed(this::handleKey);
        rotationSlider.setOnMouseDragged(mouseEvent -> handleDrag());
        rotationSlider.setOnMousePressed(mouseEvent -> handleDrag());
        refreshButton.setOnAction(actionEvent -> refreshCaptchaWindow());
    }

    void makeRoundImage(ImageView iv) {
        Rectangle clip = new Rectangle();
        clip.setWidth(iv.getFitWidth());
        clip.setHeight(iv.getFitHeight());
        clip.setArcWidth(iv.getFitWidth());
        clip.setArcHeight(iv.getFitHeight());
        clip.setStroke(Color.BLACK);

        iv.setClip(clip);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage img = iv.snapshot(parameters, null);

        iv.setClip(null);
        iv.setImage(img);
    }

    void rotateImageView(double newAngle) {
        rotatableView.setRotate(newAngle);
    }

    @FXML
    public void checkAnswer() {
        double yourAnswer = rotationSlider.getValue() + ((RotationCaptchaLogic)(captcha)).getAngleAdditionalValue();
        while (yourAnswer > 360d)
            yourAnswer -= 360;
        captcha.setUserAnswer(yourAnswer);
        boolean correct = ((RotationCaptchaLogic)(captcha)).checkAnswer();
        if (correct) {
            flagLabel.setText("Верно");
            flagLabel.setTextFill(Color.GREEN);
        }
        else {
            flagLabel.setText("Не верно");
            flagLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    void handleDrag() {
        rotateImageView(rotationSlider.getValue() + ((RotationCaptchaLogic)(captcha)).getAngleAdditionalValue());
    }

    @FXML
    void handleKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.UP) || event.getCode().equals(KeyCode.DOWN))
            handleDrag();
    }

    @FXML
    public void refreshCaptchaWindow() {
        int patternIdx = (int) (Math.random() * 10000000) % countImages;
        int rotatableIdx = patternIdx;
        while (rotatableIdx == patternIdx)
            rotatableIdx = (int) (Math.random() * 10000000) % countImages;

        URL patternURL = getClass().getResource(IMG_FOLDER + "/" + patternIdx + ".png");
        URL rotatableURL = getClass().getResource(IMG_FOLDER + "/" + rotatableIdx + ".png");
        assert patternURL != null;
        assert rotatableURL != null;

        double patternAngle = Math.random() * 360d;
        double additionalRotationAngle = Math.random() * 360d;

        captcha = new RotationCaptchaLogic(patternURL, patternAngle, rotatableURL, additionalRotationAngle, 10);


        Image[] imgs = ((RotationCaptchaLogic)(captcha)).getImages();
        patternView.setImage(imgs[0]);
        rotatableView.setImage(imgs[1]);

        double width = anchorPane.getPrefWidth();
        double height = anchorPane.getPrefHeight();
        final int limiter = 3;
        double sizeOfImages = width / limiter;

        patternView.setFitHeight(sizeOfImages);
        patternView.setFitWidth(sizeOfImages);
        rotatableView.setFitHeight(sizeOfImages);
        rotatableView.setFitWidth(sizeOfImages);

        makeRoundImage(patternView);
        makeRoundImage(rotatableView);

        patternView.setRotate(patternAngle);
        rotatableView.setRotate(additionalRotationAngle);

        patternView.setLayoutX(width / (limiter + 1) - patternView.getFitWidth() / 2);
        rotatableView.setLayoutX(width / (limiter + 1) * limiter - rotatableView.getFitWidth() / 2 - 50);

        patternView.setLayoutY((height - patternView.getFitHeight()) / 2);
        rotatableView.setLayoutY((height - rotatableView.getFitHeight()) / 2);
        hintPatternLabel.setLayoutY(patternView.getLayoutY() - sizeOfImages * 0.2d);
        hintRotatableLabel.setLayoutY(rotatableView.getLayoutY() - sizeOfImages * 0.2d);

        patternView.setEffect(new DropShadow(20, Color.GRAY));
        rotatableView.setEffect(new DropShadow(20, Color.GRAY));

        resetFields();
    }

    @Override
    public void resetFields() {
        flagLabel.setTextFill(Color.GRAY);
        flagLabel.setText("---");
        rotationSlider.setValue(0d);
    }
}
