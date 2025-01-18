package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.ResultSet;

public class ItemPriceHistoryController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected ComboBox<String> itemsCombo;

    @FXML
    TableView tablePriceHistory;

    protected void populateItemsCombo() {
        try {
            ResultSet rs = connection.prepareStatement("SELECT name FROM items").executeQuery();
            while (rs.next()) {
                itemsCombo.getItems().add(rs.getString("name"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching items from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    @FXML
    protected void onItemSelect() {
        String selectedItem = itemsCombo.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        try {
            ResultSet rs = connection.prepareStatement("SELECT price FROM item_price_history WHERE item_name = '" + selectedItem + "'").executeQuery();
            while (rs.next()) {
                // Add the price to the table
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching item price history from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }
}
