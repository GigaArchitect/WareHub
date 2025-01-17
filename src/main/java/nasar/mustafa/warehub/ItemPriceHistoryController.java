package nasar.mustafa.warehub;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import java.sql.Connection;

public class ItemPriceHistoryController {
    Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @FXML
    ComboBox<String> itemsCombo;

    public ItemPriceHistoryController(){
        // fetch all items from the database and add them to the itemsCombo


    }



}
