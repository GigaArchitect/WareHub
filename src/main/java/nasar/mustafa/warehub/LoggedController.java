package nasar.mustafa.warehub;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;

public class LoggedController {
    @FXML
    protected Pane contentPane;

    protected Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    protected void onAboutClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("About.fxml"));
        Node aboutPane = loader.load();

        AboutController aboutController = loader.getController();



        contentPane.getChildren().setAll(aboutPane);

        aboutController.setMessage(stage.getWidth(), stage.getHeight());
    }
}
