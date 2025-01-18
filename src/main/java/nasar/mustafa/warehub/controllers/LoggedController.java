package nasar.mustafa.warehub.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class LoggedController {
    @FXML
    protected Pane contentPane;

    protected Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    private Connection connection;

    public void setConnection(Connection connection){
        this.connection = connection;
    }

    @FXML
    protected void onAboutClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/About.fxml"));
        Node aboutPane = loader.load();
        AboutController aboutController = loader.getController();
        contentPane.getChildren().setAll(aboutPane);
        aboutController.setMessage(stage.getWidth(), stage.getHeight());
    }

    @FXML
    protected void onAddItemButtonClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddItem.fxml"));
        Node addPane = loader.load();
        AddItemController addController = loader.getController();
        addController.setConnection(connection);
        contentPane.getChildren().setAll(addPane);
    }

    @FXML
    protected void onItemPriceHistoryClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/ItemPriceHistory.fxml"));
        Node itemPriceHistoryPane = loader.load();
        ItemPriceHistoryController itemPriceHistoryController = loader.getController();
        itemPriceHistoryController.setConnection(connection);
        contentPane.getChildren().setAll(itemPriceHistoryPane);
    }
}
