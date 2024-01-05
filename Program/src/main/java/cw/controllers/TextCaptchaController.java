package cw.controllers;

import cw.logic.TextCaptchaLogic;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;


public class TextCaptchaController extends BasicCaptchaController implements ICaptchaController {

    @FXML
    protected ImageView captchaView;
    @FXML
    protected TextField editText;

    void updateImage() {
        Image img = ((TextCaptchaLogic)(captcha)).getImages()[0];
        if (captchaView.getFitWidth() != img.getWidth() || captchaView.getFitHeight() != img.getHeight()) {
            double oldWidth = captchaView.getFitWidth();
            double oldHeight = captchaView.getFitHeight();
            captchaView.setFitWidth(img.getWidth());
            captchaView.setFitHeight(img.getHeight());
            double offsetX = (oldWidth - captchaView.getFitWidth()) / 2;
            double offsetY = (oldHeight - captchaView.getFitHeight()) / 2;

            captchaView.setLayoutX(captchaView.getLayoutX() + offsetX);
            captchaView.setLayoutY(captchaView.getLayoutY() + offsetY);
        }
        captchaView.setImage(img);
    }

    @FXML
    public void initialize() {
        refreshCaptchaWindow();

        checkButton.setOnAction(actionEvent -> checkAnswer());
        editText.setOnKeyPressed(this::editKeyPressed);
        refreshButton.setOnAction(actionEvent -> refreshCaptchaWindow());
    }

    @FXML
    public void checkAnswer() {
        if (editText.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Некорректный ввод данных");
            alert.setContentText("Поле ответа незаполненно");
            alert.show();
            return;
        }
        captcha.setUserAnswer(editText.getText());
        boolean correct = ((TextCaptchaLogic)(captcha)).checkAnswer();
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
    void editKeyPressed(KeyEvent event) {
        if (event.getEventType().equals(KeyEvent.KEY_PRESSED) && event.getCode().equals(KeyCode.ENTER)) {
            checkAnswer();
        }
    }

    public void resetFields() {
        flagLabel.setTextFill(Color.GRAY);
        flagLabel.setText("---");
        editText.setText("");
    }

    @FXML
    public void refreshCaptchaWindow() {
        resetFields();
        String alph = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder expr = new StringBuilder();
        int cnt = 5 + (int)(Math.random() * 100) % 5;
        for (int i = 0; i < cnt; i++) {
            int idx = (int)(Math.random() * 100) % alph.length();
            expr.append(alph.charAt(idx));
        }
        captcha = new TextCaptchaLogic(expr.toString(), expr.toString(), 72, 500, 400);
        updateImage();
    }
}