package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import nasar.mustafa.warehub.tableViewModels.ItemPriceHistoryRow;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.ResultSet;

public class ItemPriceHistoryController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected SearchableComboBox<String> itemsCombo;

    @FXML
    protected TableView<ItemPriceHistoryRow> tablePriceHistory;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Integer> idColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, String> nameColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Double> priceBuy;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Double> priceSell;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, String> dateColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceBuy.setCellValueFactory(new PropertyValueFactory<>("priceBuy"));
        priceSell.setCellValueFactory(new PropertyValueFactory<>("priceSell"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

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
            ResultSet rs = connection.prepareStatement("SELECT item_prices.id, items.name, item_prices.price_buy, item_prices.price_sell, " +
                    "effective_date FROM item_prices INNER JOIN items ON item_prices.item_id = " +
                    "items.id WHERE items.name = '" + selectedItem + "'").executeQuery();
            tablePriceHistory.getItems().clear();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double priceBuy = rs.getDouble("price_buy");
                double priceSell = rs.getDouble("price_sell");
                String date = rs.getString("effective_date");

                ItemPriceHistoryRow row = new ItemPriceHistoryRow(id, name, priceBuy, priceSell, date);
                tablePriceHistory.getItems().add(row);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching item price history from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
        tablePriceHistory.refresh();
    }
}
