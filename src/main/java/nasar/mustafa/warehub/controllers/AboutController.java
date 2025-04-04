package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;


public class AboutController {
    @FXML
    protected MenuBar Bar;
    @FXML
    protected Label about;
    @FXML
    private Pane aboutPane;
    @FXML
    protected void setMessage() {
        about.setText("WareHub\nThis Program Is Written By Mustafa Nasser\nPhone : +201289031133");
        about.setStyle("-fx-font-size: 24px;");
        about.setTextAlignment(TextAlignment.CENTER);
    }
}
