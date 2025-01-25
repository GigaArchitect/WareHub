package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.SearchableComboBox;
import nasar.mustafa.warehub.ApplicationEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ItemUpdateController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected SearchableComboBox<String> itemsCombo;

    @FXML
    protected TextField priceSellField;

    @FXML
    protected TextField priceBuyField;

    @FXML
    protected TextField quantityField;

    @FXML
    protected void initialize() {
        itemsCombo.setOnAction(event -> onItemSelect());
    }

    @FXML
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
        try {
            String selectedItem = itemsCombo.getValue();
            String query = "SELECT price_sell, price_buy, stock_quantity " +
                    "FROM item_prices " +
                    "INNER JOIN items ON item_prices.item_id = items.id " +
                    "WHERE items.name = ? " +
                    "ORDER BY item_prices.effective_date DESC LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, selectedItem);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                double priceSell = rs.getDouble("price_sell");
                double priceBuy = rs.getDouble("price_buy");
                int quantity = rs.getInt("stock_quantity");
                priceSellField.setText(String.valueOf(priceSell));
                priceBuyField.setText(String.valueOf(priceBuy));
                quantityField.setText(String.valueOf(quantity));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching item details from the database", ButtonType.CLOSE);
            alert.showAndWait();
            System.out.println(e.getMessage());
        }
    }

    @FXML
    protected void onUpdatePress() {
        try {
            System.out.println(connection.getAutoCommit());
            String selectedItem = itemsCombo.getValue();
            String query = "SELECT price_sell, price_buy, stock_quantity " +
                    "FROM item_prices " +
                    "INNER JOIN items ON item_prices.item_id = items.id " +
                    "WHERE items.name = ? " +
                    "ORDER BY item_prices.effective_date DESC LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, selectedItem);
            ResultSet rs = statement.executeQuery();

            double currentSellPrice = 0;
            double currentBuyPrice = 0;
            int currentQuantity = 0;

            if (rs.next()) {
                currentSellPrice = rs.getDouble("price_sell");
                currentBuyPrice = rs.getDouble("price_buy");
                currentQuantity = rs.getInt("stock_quantity");
            }

            double newSellPrice = Double.parseDouble(ApplicationEntry.convertArabicNumerals(priceSellField.getText()));
            double newBuyPrice = Double.parseDouble(ApplicationEntry.convertArabicNumerals(priceBuyField.getText()));
            int newQuantity = Integer.parseInt(ApplicationEntry.convertArabicNumerals(quantityField.getText()));

            if (newSellPrice != currentSellPrice || newBuyPrice != currentBuyPrice) {
                PreparedStatement updatePrices = connection.prepareStatement(
                        "INSERT INTO item_prices (item_id, price_sell, price_buy, stock_quantity) " +
                                "SELECT id, ?, ?, ? FROM items WHERE name = ?"
                );
                updatePrices.setDouble(1, newSellPrice);
                updatePrices.setDouble(2, newBuyPrice);
                updatePrices.setInt(3, newQuantity);
                updatePrices.setString(4, selectedItem);
                updatePrices.executeUpdate();
            } else if (newQuantity != currentQuantity) {
                PreparedStatement updateQuantity = connection.prepareStatement(
                        "UPDATE item_prices SET stock_quantity = ? WHERE item_id = (SELECT id FROM items WHERE name = ?) " +
                                "AND effective_date = (SELECT MAX(effective_date) FROM item_prices WHERE item_id = (SELECT id FROM items WHERE name = ?))"
                );
                updateQuantity.setInt(1, newQuantity);
                updateQuantity.setString(2, selectedItem);
                updateQuantity.setString(3, selectedItem);
                updateQuantity.executeUpdate();
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Item updated successfully", ButtonType.CLOSE);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error updating item details", ButtonType.CLOSE);
            alert.showAndWait();
            System.out.println(e.getMessage());
        }
    }
}
