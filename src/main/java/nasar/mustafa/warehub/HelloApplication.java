package nasar.mustafa.warehub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Logged.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("WareHub");
        stage.setMaximized(true);
        stage.setScene(scene);
        LoggedController loggedController = fxmlLoader.getController();
        loggedController.setStage(stage);
        Image icon = new Image(getClass().getResourceAsStream("WareHub.png"));
        stage.getIcons().add(icon);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        launch();
    }
}