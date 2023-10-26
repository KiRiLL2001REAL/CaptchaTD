package cw.controllers;

import cw.utils.HelpConfiguration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainWindowController {
    @FXML private ImageView iv_math_status;
    @FXML private ImageView iv_puzzle_status;
    @FXML private ImageView iv_rotation_status;
    @FXML private ImageView iv_text_status;
    @FXML private Button button_math;
    @FXML private Button button_puzzle;
    @FXML private Button button_rotation;
    @FXML private Button button_text;
    @FXML private MenuItem mi_about;
    @FXML private MenuItem mi_help_math;
    @FXML private MenuItem mi_help_puzzle;
    @FXML private MenuItem mi_help_rotation;
    @FXML private MenuItem mi_help_text;

    @FXML
    protected void initialize() {
        mi_about.setOnAction(actionEvent -> openDialogAbout());

        mi_help_text.setOnAction(actionEvent -> openHelpText());
        mi_help_math.setOnAction(actionEvent -> openHelpMath());
        mi_help_puzzle.setOnAction(actionEvent -> openHelpPuzzle());
        mi_help_rotation.setOnAction(actionEvent -> openHelpRotation());

        button_text.setOnAction(actionEvent -> openTextCaptcha());
        button_math.setOnAction(actionEvent -> openMathCaptcha());
        button_puzzle.setOnAction(actionEvent -> openPuzzleCaptcha());
        button_rotation.setOnAction(actionEvent -> openRotationCaptcha());
    }

    @FXML
    void openDialogAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText(null);
        alert.setContentText("""
                Программный продукт разработан в рамках выполнения курсовой работы по ООП на тему "Реализация CAPTCHA".
                
                Данная программа демонстрирует такие виды CAPTCHA, как: графический, логический и поведенческий.
                
                Разработчик: студент группы ИПБ-20 Батурин К. А.""");
        alert.show();
    }

    void changeCaptchaStatus(ImageView iv, String text) {
        if (Objects.equals(text, "Верно"))
            iv.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/icon/confirm.png")).toString()));
        else if (Objects.equals(text, "Не верно"))
            iv.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/icon/cancel.png")).toString()));
    }

    @FXML
    void openTextCaptcha() {
        Stage stage = loadSpecifiedWindow("/fxml/captcha/textCaptchaView.fxml",
                "Captcha: искажённый набор символов");
        stage.setOnCloseRequest(event ->
                changeCaptchaStatus(iv_text_status,
                        ((Label)(stage).getScene().getRoot().lookup("#flagLabel")).getText()));
        stage.show();
    }

    @FXML
    void openMathCaptcha() {
        Stage stage = loadSpecifiedWindow("/fxml/captcha/mathCaptchaView.fxml",
                "Captcha: искажённый математический пример");
        stage.setOnCloseRequest(event ->
                changeCaptchaStatus(iv_math_status,
                        ((Label)(stage).getScene().getRoot().lookup("#flagLabel")).getText()));
        stage.show();
    }

    @FXML
    void openRotationCaptcha() {
        Stage stage = loadSpecifiedWindow("/fxml/captcha/rotationCaptchaView.fxml",
                "Captcha: поворот картинки");
        stage.setOnCloseRequest(event ->
                changeCaptchaStatus(iv_rotation_status,
                        ((Label)(stage).getScene().getRoot().lookup("#flagLabel")).getText()));
        stage.show();
    }

    @FXML
    void openPuzzleCaptcha() {
        Stage stage = loadSpecifiedWindow("/fxml/captcha/puzzleCaptchaView.fxml",
                "Captcha: пазл");
        stage.setOnCloseRequest(event ->
                changeCaptchaStatus(iv_puzzle_status,
                        ((Label)(stage).getScene().getRoot().lookup("#flagLabel")).getText()));
        stage.show();
    }

    Stage loadSpecifiedWindow(String fxmlFileName, String title) {
        try {
            URL resURL = getClass().getResource(fxmlFileName);
            FXMLLoader fxmlLoader = new FXMLLoader(resURL);

            Stage stage = new Stage();
            Scene scene = new Scene(fxmlLoader.load());

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);

            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode().equals(KeyCode.ESCAPE)) {
                    Window window = stage.getScene().getWindow();
                    window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
                }
            });
            if (scene.getRoot().lookup("#flagLabel") != null) {
                scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                    if (((Label) scene.getRoot().lookup("#flagLabel")).getText().equals("---")) {
                        event.consume();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Решите капчу, прежде чем закрыть окно.");
                        alert.show();
                    }
                });
            }
            return stage;
        } catch (IOException e) {
            System.out.println("Err: can't load " + fxmlFileName);
        }
        return null;
    }

    void fillContent(Pane pane, HelpConfiguration hc, Font font) {
        int count = hc.getStringsIndices().size() + hc.getImagesIndices().size();
        int textCounter = 0;
        int imageCounter = 0;
        for (int i = 0; i < count; i++) {
            if (hc.getImagesIndices().contains(i)) {
                ImageView imageView = new ImageView();
                imageView.setImage(new Image(Objects.requireNonNull(getClass()
                        .getResource(hc.getImages().get(imageCounter))).toExternalForm()));
                pane.getChildren().add(imageView);
                imageCounter++;
            }
            else {
                Label label = new Label();
                label.setFont(font);
                label.setText(hc.getStrings().get(textCounter));
                pane.getChildren().add(label);
                textCounter++;
            }
        }
    }

    void openHelpWindow(HelpConfiguration hc) {
        Stage stage = loadSpecifiedWindow("/fxml/aboutView.fxml", hc.getTitle());
        stage.show();
        VBox mVBox = (VBox)stage.getScene().lookup("#mVBox");
        Font font = new Font("Calibri", 16);
        fillContent(mVBox, hc, font);
    }

    @FXML
    void openHelpText() {
        openHelpWindow(new HelpConfiguration("/help/config/textCaptchaHelp.ini"));
    }

    @FXML
    void openHelpMath() {
        openHelpWindow(new HelpConfiguration("/help/config/mathCaptchaHelp.ini"));
    }

    @FXML
    void openHelpPuzzle() {
        openHelpWindow(new HelpConfiguration("/help/config/puzzleCaptchaHelp.ini"));
    }

    @FXML
    void openHelpRotation() {
        openHelpWindow(new HelpConfiguration("/help/config/rotationCaptchaHelp.ini"));
    }

}
