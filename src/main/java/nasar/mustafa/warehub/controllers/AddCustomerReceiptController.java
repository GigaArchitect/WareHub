package nasar.mustafa.warehub.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.SearchableComboBox;

import java.sql.*;

public class AddCustomerReceiptController {
    private Connection connection;
    private int serialCounter = 1;
    private ObservableList<ReceiptItem> receiptItems = FXCollections.observableArrayList();

    @FXML
    private SearchableComboBox<String> customerCombo;

    @FXML
    private SearchableComboBox<String> itemCombo;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField totalField;

    @FXML
    private TableView<ReceiptItem> receiptTable;

    @FXML
    private TableColumn<ReceiptItem, Double> dateColumn;

    @FXML
    private TableColumn<ReceiptItem, Integer> quantityColumn;

    @FXML
    private TableColumn<ReceiptItem, Double> priceColumn;

    @FXML
    private TableColumn<ReceiptItem, String> itemColumn;

    @FXML
    private TableColumn<ReceiptItem, Integer> serialColumn;

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        receiptTable.setItems(receiptItems);
    }

    @FXML
    public void addItem(ActionEvent event) {
        try {
            String selectedItem = itemCombo.getValue();
            if (selectedItem == null || selectedItem.isEmpty()) {
                showAlert("Please select an item", Alert.AlertType.WARNING);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Please enter a valid quantity", Alert.AlertType.WARNING);
                return;
            }

            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT ip.price_sell, ip.stock_quantity FROM item_prices ip " +
                            "INNER JOIN items i ON ip.item_id = i.id " +
                            "WHERE i.name = ? ORDER BY ip.effective_date DESC LIMIT 1"
            );
            stmt.setString(1, selectedItem);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int stockQuantity = rs.getInt("stock_quantity");
                if (quantity > stockQuantity) {
                    showAlert("Insufficient stock. Available: " + stockQuantity, Alert.AlertType.WARNING);
                    return;
                }

                double price = rs.getDouble("price_sell");
                ReceiptItem item = new ReceiptItem(selectedItem, quantity, price, serialCounter++);
                receiptItems.add(item);
                updateTotal();
                clearInputs();
            }
        } catch (SQLException e) {
            showAlert("Error adding item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void deleteItem(ActionEvent event) {
        ReceiptItem selectedItem = receiptTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            receiptItems.remove(selectedItem);
            updateTotal();
        }
    }

    @FXML
    public void saveReceipt(ActionEvent event) {
        if (receiptItems.isEmpty()) {
            showAlert("Cannot save empty receipt", Alert.AlertType.WARNING);
            return;
        }

        String selectedCustomer = customerCombo.getValue();
        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            showAlert("Please select a customer", Alert.AlertType.WARNING);
            return;
        }

        try {
            connection.setAutoCommit(false);

            // Get customer ID
            PreparedStatement customerStmt = connection.prepareStatement(
                    "SELECT id FROM customers WHERE name = ?"
            );
            customerStmt.setString(1, selectedCustomer);
            ResultSet customerRs = customerStmt.executeQuery();

            if (!customerRs.next()) {
                throw new SQLException("Customer not found");
            }
            int customerId = customerRs.getInt("id");

            // Create sale record
            PreparedStatement saleStmt = connection.prepareStatement(
                    "INSERT INTO sales (customer_id, total_price) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            saleStmt.setInt(1, customerId);
            String totalText = totalField.getText().replace(",", "");
            saleStmt.setDouble(2, Double.parseDouble(totalText));
            saleStmt.executeUpdate();

            ResultSet generatedKeys = saleStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Failed to create sale record");
            }
            int saleId = generatedKeys.getInt(1);

            // Insert sale items and update stock
            for (ReceiptItem item : receiptItems) {
                // Get item ID
                PreparedStatement itemStmt = connection.prepareStatement(
                        "SELECT id FROM items WHERE name = ?"
                );
                itemStmt.setString(1, item.getItemName());
                ResultSet itemRs = itemStmt.executeQuery();
                if (!itemRs.next()) {
                    throw new SQLException("Item not found: " + item.getItemName());
                }
                int itemId = itemRs.getInt("id");

                // Insert sale item
                PreparedStatement saleItemStmt = connection.prepareStatement(
                        "INSERT INTO sales_items (sale_id, item_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)"
                );
                saleItemStmt.setInt(1, saleId);
                saleItemStmt.setInt(2, itemId);
                saleItemStmt.setInt(3, item.getQuantity());
                saleItemStmt.setDouble(4, item.getPrice());
                saleItemStmt.setDouble(5, item.getTotal());
                saleItemStmt.executeUpdate();

                // Update stock quantity
                PreparedStatement updateStockStmt = connection.prepareStatement(
                        "UPDATE item_prices SET stock_quantity = stock_quantity - ? " +
                                "WHERE item_id = ? AND effective_date = (SELECT MAX(effective_date) FROM item_prices WHERE item_id = ?)"
                );
                updateStockStmt.setInt(1, item.getQuantity());
                updateStockStmt.setInt(2, itemId);
                updateStockStmt.setInt(3, itemId);
                updateStockStmt.executeUpdate();
            }

            connection.commit();
            showAlert("Receipt saved successfully", Alert.AlertType.INFORMATION);
            clearAll();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            showAlert("Error saving receipt: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateTotal() {
        double total = receiptItems.stream()
                .mapToDouble(ReceiptItem::getTotal)
                .sum();
        String formattedTotal = String.format("%,.2f", total);
        totalField.setText(formattedTotal);
    }

    private void clearInputs() {
        itemCombo.setValue(null);
        quantityField.clear();
    }

    private void clearAll() {
        clearInputs();
        customerCombo.setValue(null);
        receiptItems.clear();
        totalField.clear();
        serialCounter = 1;
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.CLOSE);
        alert.showAndWait();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateComboBoxes();
    }

    private void populateComboBoxes() {
        try {
            ResultSet customers = connection.createStatement()
                    .executeQuery("SELECT name FROM customers ORDER BY name");
            while (customers.next()) {
                customerCombo.getItems().add(customers.getString("name"));
            }

            ResultSet items = connection.createStatement()
                    .executeQuery("SELECT name FROM items ORDER BY name");
            while (items.next()) {
                itemCombo.getItems().add(items.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Error populating combo boxes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
