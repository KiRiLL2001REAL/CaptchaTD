package cw.logic;

abstract public class CaptchaLogic {
    protected Object mUserAnswer;

    public void setUserAnswer(Object answer) {
        mUserAnswer = answer;
    }
}
