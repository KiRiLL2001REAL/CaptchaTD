package cw.controllers;

import javafx.fxml.FXML;

public interface ICaptchaController {
    @FXML void initialize() throws Exception;
    @FXML void checkAnswer();
    @FXML void refreshCaptchaWindow();

    void resetFields();
}
