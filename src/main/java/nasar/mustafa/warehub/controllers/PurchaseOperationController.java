package nasar.mustafa.warehub.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.SearchableComboBox;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class PurchaseOperationController implements Initializable {
    @FXML private SearchableComboBox<Supplier> supplierComboBox;
    @FXML private SearchableComboBox<Item> itemComboBox;
    @FXML private TextField quantityField;
    @FXML private TableColumn<PurchaseItem, String> itemNameColumn;
    @FXML private TableColumn<PurchaseItem, Integer> quantityColumn;
    @FXML private TableColumn<PurchaseItem, Double> priceColumn;
    @FXML private TableColumn<PurchaseItem, Double> totalColumn;
    @FXML private TextField totalAmountField;

    private Connection connection;
    private final ObservableList<PurchaseItem> purchaseItems = FXCollections.observableArrayList();
    private double totalAmount = 0.0;

    @FXML private TableView<PurchaseItem> purchaseItemsTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        purchaseItemsTable.setItems(purchaseItems);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadSuppliers();
        loadItems();
    }

    private void loadSuppliers() {
        String query = "SELECT id, name FROM suppliers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
            while (rs.next()) {
                suppliers.add(new Supplier(rs.getInt("id"), rs.getString("name")));
            }
            supplierComboBox.setItems(suppliers);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading suppliers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadItems() {
        String query = "SELECT id, name FROM items";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ObservableList<Item> items = FXCollections.observableArrayList();
            while (rs.next()) {
                items.add(new Item(rs.getInt("id"), rs.getString("name")));
            }
            itemComboBox.setItems(items);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading items: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private double getLatestItemPrice(int itemId) {
        String query = "SELECT price_buy FROM item_prices WHERE item_id = ? ORDER BY effective_date DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price_buy");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to fetch item price: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return 0.0;
    }

    @FXML
    private void onAddItemToPurchase() {
        if (!validateItemInput()) return;

        Item selectedItem = itemComboBox.getValue();
        int quantity = Integer.parseInt(quantityField.getText());
        double price = getLatestItemPrice(selectedItem.id);
        double total = quantity * price;

        purchaseItems.add(new PurchaseItem(selectedItem.id, selectedItem.name, quantity, price, total));
        totalAmount += total;
        totalAmountField.setText(String.format("%,.2f", totalAmount));

        clearItemInputs();
    }

@FXML
private void onSavePurchase() {
    if (!validatePurchase()) return;

    try {
        connection.setAutoCommit(false);

        String purchaseQuery = "INSERT INTO purchases (supplier_id, total_cost) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(purchaseQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, supplierComboBox.getValue().id);
            pstmt.setDouble(2, totalAmount);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int purchaseId = rs.getInt(1);
                savePurchaseItems(purchaseId);

                // Update item prices
                updateItemPrices();

                connection.commit();
                showAlert("Success", "Purchase saved successfully", Alert.AlertType.INFORMATION);
                clearForm();
            }
        }
    } catch (SQLException e) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        showAlert("Error", "Failed to save purchase: " + e.getMessage(), Alert.AlertType.ERROR);
    } finally {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

    @FXML
    private void onDeleteItem() {
        PurchaseItem selectedItem = purchaseItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Error", "Please select an item to delete", Alert.AlertType.ERROR);
            return;
        }

        totalAmount -= selectedItem.getTotal();
        purchaseItems.remove(selectedItem);
        totalAmountField.setText(String.format("%,.2f", totalAmount));
    }

    private void updateItemPrices() throws SQLException {
        String query = "UPDATE item_prices SET stock_quantity = ? WHERE item_id = ? AND end_date IS NULL";
        try (PreparedStatement updateStmt = connection.prepareStatement(query)) {
            for (PurchaseItem item : purchaseItems) {
                int newQuantity = getExistingQuantity(item.itemId) + item.quantity;
                updateStmt.setInt(1, newQuantity);
                updateStmt.setInt(2, item.itemId);
                updateStmt.executeUpdate();
            }
        }
    }

    private int getExistingQuantity(int itemId) throws SQLException {
        String query = "SELECT stock_quantity FROM item_prices WHERE item_id = ? AND end_date IS NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("stock_quantity");
            }
        }
        return 0;
    }

    private void savePurchaseItems(int purchaseId) throws SQLException {
        String query = "INSERT INTO purchase_items (purchase_id, item_id, quantity, unit_price, total_cost) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (PurchaseItem item : purchaseItems) {
                pstmt.setInt(1, purchaseId);
                pstmt.setInt(2, item.itemId);
                pstmt.setInt(3, item.quantity);
                pstmt.setDouble(4, item.price);
                pstmt.setDouble(5, item.total);
                pstmt.executeUpdate();
            }
        }
    }

    private boolean validateItemInput() {
        if (itemComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select an item", Alert.AlertType.ERROR);
            return false;
        }
        try {
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid number for quantity", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validatePurchase() {
        if (supplierComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select a supplier", Alert.AlertType.ERROR);
            return false;
        }
        if (purchaseItems.isEmpty()) {
            showAlert("Validation Error", "Please add at least one item", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void clearItemInputs() {
        itemComboBox.setValue(null);
        quantityField.clear();
    }

    private void clearForm() {
        supplierComboBox.setValue(null);
        clearItemInputs();
        purchaseItems.clear();
        totalAmount = 0.0;
        totalAmountField.setText(String.format("%,.2f", 0.0));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static class Supplier {
        private final int id;
        private final String name;

        public Supplier(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class Item {
        private final int id;
        private final String name;

        public Item(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class PurchaseItem {
        private final int itemId;
        private final String itemName;
        private final int quantity;
        private final double price;
        private final double total;

        public PurchaseItem(int itemId, String itemName, int quantity, double price, double total) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }

        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}
