package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SupplierPaymentNoReceiptController {

    @FXML
    private SearchableComboBox<String> supplierComboBox;

    @FXML
    private TextField amountField;

    @FXML
    private Label balanceLabel;

    @FXML
    private Button payButton;

    private Connection connection;

    @FXML
    public void initialize() {
        setupListeners();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadSuppliers();
    }

    private void setupListeners() {
        supplierComboBox.setOnAction(e -> updateBalance());
        payButton.setOnAction(e -> handlePayment());

        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldValue);
            }
        });
    }

    private void loadSuppliers() {
        String query = "SELECT name FROM suppliers ORDER BY name";
        ObservableList<String> suppliers = FXCollections.observableArrayList();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(rs.getString("name"));
            }
            supplierComboBox.setItems(suppliers);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load suppliers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateBalance() {
        String selectedSupplier = supplierComboBox.getValue();
        if (selectedSupplier == null) return;

        String query = """
            SELECT 
                (SELECT COALESCE(SUM(total_cost), 0) FROM purchases p 
                 INNER JOIN suppliers s ON p.supplier_id = s.id 
                 WHERE s.name = ?) -
                (SELECT COALESCE(SUM(amount), 0) FROM deposits d 
                 INNER JOIN suppliers s ON d.supplier_id = s.id 
                 WHERE s.name = ?) as balance
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, selectedSupplier);
            stmt.setString(2, selectedSupplier);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                balanceLabel.setText(String.format("%,.2f", balance));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to calculate balance: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePayment() {
        String selectedSupplier = supplierComboBox.getValue();
        if (selectedSupplier == null) {
            showAlert("Error", "Please select a supplier", Alert.AlertType.WARNING);
            return;
        }

        String amountText = amountField.getText();
        if (amountText.isEmpty()) {
            showAlert("Error", "Please enter an amount", Alert.AlertType.WARNING);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount format", Alert.AlertType.WARNING);
            return;
        }

        try {
            int supplierId;
            String supplierQuery = "SELECT id FROM suppliers WHERE name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(supplierQuery)) {
                stmt.setString(1, selectedSupplier);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    showAlert("Error", "Supplier not found", Alert.AlertType.ERROR);
                    return;
                }
                supplierId = rs.getInt("id");
            }

            String depositQuery = "INSERT INTO deposits (supplier_id, amount) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(depositQuery)) {
                stmt.setInt(1, supplierId);
                stmt.setDouble(2, amount);
                stmt.executeUpdate();
            }

            showAlert("Success", "Payment recorded successfully", Alert.AlertType.INFORMATION);
            amountField.clear();
            updateBalance();

        } catch (SQLException e) {
            showAlert("Error", "Failed to record payment: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
