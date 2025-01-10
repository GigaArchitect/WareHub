package nasar.mustafa.warehub;

import javafx.application.Platform;
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
    protected void setMessage(double x, double y) {
        about.setVisible(false);
        about.setText("This Program Is Written By Mustafa Nasser\nPhone : +201289031133\nGitHub : GigaArchitect");
        about.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 16px;");
        about.setTextAlignment(TextAlignment.CENTER);
        Platform.runLater(()->{
            about.setLayoutX(x / 2);
            about.setLayoutY(y / 2);
            about.setLayoutX(about.getLayoutX() - about.getWidth() / 2);
            about.setLayoutY(about.getLayoutY() - about.getHeight() / 2);
            about.setVisible(true);
        });
    }
}