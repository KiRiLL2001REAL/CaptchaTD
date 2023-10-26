package cw.logic;

import javafx.scene.image.Image;

public interface ICaptcha {
    Image[] getImages();
    boolean checkAnswer();
}
