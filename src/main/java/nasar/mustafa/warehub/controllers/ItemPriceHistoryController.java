package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import nasar.mustafa.warehub.tableViewModels.ItemPriceHistoryRow;

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
    protected TableView<ItemPriceHistoryRow> tablePriceHistory;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Integer> idColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, String> priceTypeColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Double> priceColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, String> dateColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        priceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("priceType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

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
    protected void onItemSelect() {
        String selectedItem = itemsCombo.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        try {
            ResultSet rs = connection.prepareStatement("SELECT item_prices.id, price_type, price, " +
                    "effective_date FROM item_prices INNER JOIN items ON item_prices.item_id = " +
                    "items.id WHERE items.name = '" + selectedItem + "'").executeQuery();
            tablePriceHistory.getItems().clear();
            while (rs.next()) {
                int id = rs.getInt("id");
                String priceType = rs.getString("price_type");
                double price = rs.getDouble("price");
                String date = rs.getString("effective_date");

                ItemPriceHistoryRow row = new ItemPriceHistoryRow(id, priceType, price, date);
                tablePriceHistory.getItems().add(row);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching item price history" +
                    " from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
        tablePriceHistory.refresh();
    }
}
