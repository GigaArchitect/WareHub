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
    protected TextField itemNameField;

    @FXML
    protected RadioButton updateLatestPriceRadio;

    @FXML
    protected RadioButton addNewPriceRadio;

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
            String query = "SELECT price_sell, price_buy, stock_quantity, name " +
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
                String itemName = rs.getString("name");
                priceSellField.setText(String.valueOf(priceSell));
                priceBuyField.setText(String.valueOf(priceBuy));
                quantityField.setText(String.valueOf(quantity));
                itemNameField.setText(itemName);
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
            if (!updateLatestPriceRadio.isSelected() && !addNewPriceRadio.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an option to update or add a new price.", ButtonType.CLOSE);
                alert.showAndWait();
                return;
            }

            connection.setAutoCommit(false); // Start transaction

            String selectedItem = itemsCombo.getValue();
            String newItemName = itemNameField.getText();
            String query = "SELECT price_sell, price_buy, stock_quantity, name " +
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

            if (updateLatestPriceRadio.isSelected()) {
                if (newSellPrice != currentSellPrice || newBuyPrice != currentBuyPrice || newQuantity != currentQuantity) {
                    PreparedStatement updateQuantity = connection.prepareStatement(
                            "UPDATE item_prices SET price_sell = ?, price_buy = ?, stock_quantity = ? WHERE item_id = (SELECT id FROM items WHERE name = ?) " +
                                    "AND effective_date = (SELECT MAX(effective_date) FROM item_prices WHERE item_id = (SELECT id FROM items WHERE name = ?))"
                    );
                    updateQuantity.setDouble(1, newSellPrice);
                    updateQuantity.setDouble(2, newBuyPrice);
                    updateQuantity.setInt(3, newQuantity);
                    updateQuantity.setString(4, selectedItem);
                    updateQuantity.setString(5, selectedItem);
                    updateQuantity.executeUpdate();
                }
            } else if (addNewPriceRadio.isSelected()) {
                PreparedStatement updatePrices = connection.prepareStatement(
                        "INSERT INTO item_prices (item_id, price_sell, price_buy, stock_quantity) " +
                                "SELECT id, ?, ?, ? FROM items WHERE name = ?"
                );
                updatePrices.setDouble(1, newSellPrice);
                updatePrices.setDouble(2, newBuyPrice);
                updatePrices.setInt(3, newQuantity);
                updatePrices.setString(4, selectedItem);
                updatePrices.executeUpdate();
            }

            if (!newItemName.equals(selectedItem)) {
                PreparedStatement updateName = connection.prepareStatement(
                        "UPDATE items SET name = ? WHERE name = ?"
                );
                updateName.setString(1, newItemName);
                updateName.setString(2, selectedItem);
                updateName.executeUpdate();
            }

            connection.commit(); // Commit transaction
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Item updated successfully", ButtonType.CLOSE);
            alert.showAndWait();
        } catch (Exception e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (Exception rollbackException) {
                System.out.println("Error rolling back transaction: " + rollbackException.getMessage());
            }
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error updating item details", ButtonType.CLOSE);
            alert.showAndWait();
            System.out.println(e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit mode
            } catch (Exception finalException) {
                System.out.println("Error resetting auto-commit mode: " + finalException.getMessage());
            }
        }
    }
}
