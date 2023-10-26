package cw.logic;

import cw.utils.imgenerator.CaptchaImageGenerator;
import javafx.scene.image.Image;

import java.util.Objects;

public class TextCaptchaLogic extends CaptchaLogic implements ICaptcha {
    private final Image mImage;
    private final String mExpression;
    private final String mAnswer;

    public TextCaptchaLogic(String expression, String answer, int fontSize, int width, int height) {
        mExpression = expression;
        mAnswer = answer;
        mImage = CaptchaImageGenerator.generateImage(mExpression, fontSize, width, height);
    }

    public Image[] getImages() {
        return new Image[] {mImage};
    }

    public boolean checkAnswer() {
        return Objects.equals(mUserAnswer, mAnswer);
    }
}
