package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import nasar.mustafa.warehub.tableViewModels.ItemPriceHistoryRow;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.ResultSet;

public class ItemDeleteController {
    protected Connection connection;
    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected SearchableComboBox<String> itemsCombo;

    protected void populateItemsCombo() {
        try {
            ResultSet rs = connection.prepareStatement("SELECT name FROM items").executeQuery();
            while (rs.next()) {
                itemsCombo.getItems().add(rs.getString("name"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching items from " +
                    "the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        String selectedItem = itemsCombo.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an item to delete", ButtonType.CLOSE);
            alert.showAndWait();
            return;
        }
        try {
            connection.prepareStatement("DELETE FROM items WHERE name = '" + selectedItem + "'").execute();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Item deleted successfully", ButtonType.CLOSE);
            alert.showAndWait();
            itemsCombo.getItems().remove(selectedItem);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error deleting item", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }
}
