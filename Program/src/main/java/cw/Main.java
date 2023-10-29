package cw;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL resURL = getClass().getResource("/fxml/mainView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resURL);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Выбор CAPTCHA");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> Platform.exit());

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}