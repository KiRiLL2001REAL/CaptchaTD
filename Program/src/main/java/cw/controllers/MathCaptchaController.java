package cw.controllers;

import cw.logic.TextCaptchaLogic;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class MathCaptchaController extends TextCaptchaController {
    private static final KeyCode[] allowedKeyCodes = new KeyCode[] {
            KeyCode.DIGIT0,
            KeyCode.DIGIT1,
            KeyCode.DIGIT2,
            KeyCode.DIGIT3,
            KeyCode.DIGIT4,
            KeyCode.DIGIT5,
            KeyCode.DIGIT6,
            KeyCode.DIGIT7,
            KeyCode.DIGIT8,
            KeyCode.DIGIT9,
            KeyCode.MINUS
    };

    private String oldText = "";

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        editText.setOnKeyTyped(this::checkAllowedKey);
    }

    @Override
    @FXML
    public void refreshCaptchaWindow() {
        resetFields();
        int a = (int)(Math.random() * 100);
        int b = (int)(Math.random() * 100);
        int sign = (int)(Math.random() * 10) % 2;
        int result = (sign == 0) ? a + b : a - b;
        StringBuilder expression = new StringBuilder();
        expression.append(a);
        switch (sign) {
            case 0 -> expression.append(" + ");
            case 1 -> expression.append(" - ");
        }
        expression.append(b);
        String resultExpression = String.valueOf(result);
        captcha = new TextCaptchaLogic(expression.toString(), resultExpression, 120, 500, 400);
        updateImage();
    }

    @Override
    @FXML
    void editKeyPressed(KeyEvent event) {
        super.editKeyPressed(event);
        oldText = editText.getText();
    }

    @FXML
    void checkAllowedKey(KeyEvent event) {
        // разрешены только цифры и минус
        String aText = editText.getText();
        if (event.getEventType().equals(KeyEvent.KEY_TYPED) && aText.length() > 0) {
            boolean illegalCharacterEntry = false;
            for (int i = 0; i < aText.length(); i++) {
                boolean allowed = false;
                var code = editText.getText().charAt(i);
                for (KeyCode key : allowedKeyCodes) {
                    if (code == key.getCode()) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    illegalCharacterEntry = true;
                    break;
                }
            }
            if (illegalCharacterEntry) {
                editText.setText(oldText);
                editText.positionCaret(oldText.length());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Некорректный ввод данных");
                alert.setContentText("Разрешен ввод следующих символов: \"-0123456789\"");
                alert.show();
            }
        }
    }

    @FXML
    @Override
    public void checkAnswer() {
        String text = editText.getText();
        if (text.length() == 0) {
            super.checkAnswer(); // вывод окна о пустом поле ввода
            return;
        }
        try {
            Integer.parseInt(text);
            super.checkAnswer();
        }
        catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Некорректный ввод данных");
            alert.setContentText("Введённое значение не является числом");
            alert.show();
        }
    }
}
