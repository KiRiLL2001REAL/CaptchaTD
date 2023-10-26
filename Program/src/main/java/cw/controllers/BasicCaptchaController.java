package cw.controllers;

import cw.logic.CaptchaLogic;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

abstract public class BasicCaptchaController {
    @FXML protected Button refreshButton;
    @FXML protected Button checkButton;
    @FXML protected Label flagLabel;

    CaptchaLogic captcha;
}
