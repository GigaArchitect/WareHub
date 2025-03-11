package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerAccountStatementController {
    @FXML
    private SearchableComboBox<String> customerComboBox;

    @FXML
    private TableView<AccountStatementRow> accountStatementTable;

    @FXML
    private TableColumn<AccountStatementRow, String> dateColumn;

    @FXML
    private TableColumn<AccountStatementRow, String> descriptionColumn;

    @FXML
    private TableColumn<AccountStatementRow, Double> amountColumn;

    @FXML
    private TableColumn<AccountStatementRow, Double> balanceColumn;

    @FXML
    private TextField paidAmountField;

    @FXML
    private TextField transactionVolumeField;

    @FXML
    private BorderPane contentPane; // Assuming you have a Pane to print

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateCustomerComboBox();
    }

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
    }

    private void populateCustomerComboBox() {
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT name FROM customers ORDER BY name");
            while (rs.next()) {
                customerComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Error", "Error fetching customers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAggregate() {
        String selectedCustomer = customerComboBox.getValue();
        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            showAlert("Input Error", "Please select a customer", Alert.AlertType.WARNING);
            return;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT s.sale_date AS date, 'بيع' AS description, s.total_price AS amount, " +
                            "(SELECT SUM(total_price) FROM sales WHERE customer_id = c.id) AS balance " +
                            "FROM sales s INNER JOIN customers c ON s.customer_id = c.id WHERE c.name = ? " +
                            "UNION ALL " +
                            "SELECT p.payment_date AS date, 'دفع' AS description, -p.amount AS amount, " +
                            "(SELECT SUM(amount) FROM payments WHERE customer_id = c.id) AS balance " +
                            "FROM payments p INNER JOIN customers c ON p.customer_id = c.id WHERE c.name = ? " +
                            "ORDER BY date"
            );
            stmt.setString(1, selectedCustomer);
            stmt.setString(2, selectedCustomer);
            ResultSet rs = stmt.executeQuery();

            accountStatementTable.getItems().clear();
            double paidAmount = 0;
            double transactionVolume = 0;

            while (rs.next()) {
                String date = rs.getString("date");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");

                if (description.equals("بيع")) {
                    transactionVolume += amount;
                } else if (description.equals("دفع")) {
                    paidAmount += -amount;
                }

                accountStatementTable.getItems().add(new AccountStatementRow(date, description, amount, transactionVolume - paidAmount));
            }

            paidAmountField.setText(String.format("%,.2f", paidAmount));
            transactionVolumeField.setText(String.format("%,.2f", transactionVolume));

        } catch (SQLException e) {
            showAlert("Error", "Error fetching account statement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(contentPane.getScene().getWindow())) {
            boolean success = job.printPage(contentPane);
            if (success) {
                job.endJob();
            } else {
                showAlert("Print Error", "Failed to print the page", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class AccountStatementRow {
        private final String date;
        private final String description;
        private final double amount;
        private final double balance;

        public AccountStatementRow(String date, String description, double amount, double balance) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.balance = balance;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }

        public double getBalance() {
            return balance;
        }
    }
}
