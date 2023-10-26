package cw.logic;

import javafx.scene.image.Image;

import java.net.URL;

public class RotationCaptchaLogic extends CaptchaLogic implements ICaptcha {
    private final Image mImagePattern;
    private final Image mRotatableImage;
    private final double mAnglePattern;
    private final double mAngleRotatedAdditionalValue;
    private final double mThreshold;

    /**
     * All angles in degrees [0..360)
     */
    public RotationCaptchaLogic(
            URL patternURL, double patternAngle,
            URL rotatableURL, double additionalRotationAngle,
            double threshold
    ) {
        mImagePattern = new Image(patternURL.toString());
        mRotatableImage = new Image(rotatableURL.toString());

        while (patternAngle > 360d)
            patternAngle -= 360d;
        while (additionalRotationAngle > 360d)
            additionalRotationAngle -= 360d;

        mAnglePattern = patternAngle;
        mAngleRotatedAdditionalValue = additionalRotationAngle;

        mThreshold = Math.abs(threshold);
    }

    @Override
    public boolean checkAnswer() {
        double answer = (double)mUserAnswer;
        double diff = answer - mAnglePattern;
        return Math.abs(diff) < mThreshold;
    }

    public Image[] getImages() {
        return new Image[] {mImagePattern, mRotatableImage};
    }

    public double getAngleAdditionalValue() {
        return  mAngleRotatedAdditionalValue;
    }

}
