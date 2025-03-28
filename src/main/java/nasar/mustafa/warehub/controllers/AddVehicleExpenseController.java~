package nasar.mustafa.warehub.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static nasar.mustafa.warehub.ApplicationEntry.convertArabicNumerals;

public class AddVehicleExpenseController {
    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadVehicles();
    }

    @FXML
    private SearchableComboBox<String> vehicleComboBox;

    @FXML
    private TableView<Expense> expenseTable;

    @FXML
    private TableColumn<Expense, String> expenseNameColumn;

    @FXML
    private TableColumn<Expense, Double> expensePriceColumn;

    @FXML
    private TextField expenseNameField;

    @FXML
    private TextField expensePriceField;

    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        expenseNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        expensePriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        expenseTable.setItems(expenseList);
    }

    private void loadVehicles() {
        if (connection == null) {
            showAlert(Alert.AlertType.ERROR, "Connection is not set!");
            return;
        }

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT plate_number FROM vehicles");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                vehicleComboBox.getItems().add(resultSet.getString("plate_number"));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void addRow(ActionEvent event) {
        String expenseName = expenseNameField.getText();
        String expensePriceText = expensePriceField.getText();

        if (expenseName == null || expenseName.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Expense name is empty or invalid");
            return;
        }

        double expensePrice;
        try {
            expensePrice = Double.parseDouble(convertArabicNumerals(expensePriceText));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Expense price is invalid");
            return;
        }

        Expense expense = new Expense(expenseName, expensePrice);
        expenseList.add(expense);

        expenseNameField.clear();
        expensePriceField.clear();
    }

    @FXML
    protected void addExpense(ActionEvent event) {
        if (vehicleComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a vehicle.");
            return;
        }

        String selectedVehicle = vehicleComboBox.getValue();

        try {
            connection.setAutoCommit(false); // Start transaction

            for (Expense expense : expenseList) {
                String sql = "INSERT INTO vehicle_expenses (vehicle_id, expense_name, amount) VALUES ((SELECT id FROM vehicles WHERE plate_number = ?), ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, selectedVehicle);
                    statement.setString(2, expense.getName());
                    statement.setDouble(3, expense.getPrice());
                    statement.executeUpdate();
                }
            }

            connection.commit(); // Commit transaction
            showAlert(Alert.AlertType.INFORMATION, "Expenses added successfully.");
            expenseList.clear(); // Clear the table
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                showAlert(Alert.AlertType.ERROR, "Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            showAlert(Alert.AlertType.ERROR, "Error adding expenses: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Failed to reset auto-commit: " + ex.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }

    public static class Expense {
        private String name;
        private double price;

        public Expense(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
