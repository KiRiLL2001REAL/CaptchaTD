module cw.program {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires ini4j;


    opens cw to javafx.fxml;
    exports cw;

    opens cw.controllers to javafx.fxml;
}