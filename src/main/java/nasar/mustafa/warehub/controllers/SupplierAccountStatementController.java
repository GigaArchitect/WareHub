package nasar.mustafa.warehub.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.controlsfx.control.SearchableComboBox;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SupplierAccountStatementController {

    @FXML
    private TableView<AccountStatement> accountStatementTable;
    @FXML
    private TableColumn<AccountStatement, Integer> idColumn;
    @FXML
    private TableColumn<AccountStatement, String> dateColumn;
    @FXML
    private TableColumn<AccountStatement, String> descriptionColumn;
    @FXML
    private TableColumn<AccountStatement, Double> amountColumn;
    @FXML
    private TableColumn<AccountStatement, Double> balanceColumn;
    @FXML
    private SearchableComboBox<String> suppliersComboBox;
    @FXML
    private TextField transactionVolumeField;
    @FXML
    private TextField paidAmountField;
    @FXML
    private Button aggregateButton;
    @FXML
    private Button printButton;

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadSuppliers();
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        accountStatementTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && accountStatementTable.getSelectionModel().getSelectedItem() != null) {
                AccountStatement selectedRow = accountStatementTable.getSelectionModel().getSelectedItem();
                if ("شراء".equals(selectedRow.getDescription())) {
                    openPurchaseOperationWindow(selectedRow.getId());
                }
            }
        });
    }

    private void openPurchaseOperationWindow(int purchaseId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/PurchaseOperation.fxml"));

            if (loader.getLocation() == null) {
                throw new IOException("Cannot find PurchaseOperation.fxml at: /nasar/mustafa/warehub/PurchaseOperation.fxml");
            }

            Parent root = loader.load();
            PurchaseOperationController controller = loader.getController();

            if (controller == null) {
                throw new IOException("Failed to get PurchaseOperationController");
            }

            controller.setConnection(connection);
            controller.loadPurchaseData(purchaseId);
            controller.disableEditingControls();

            Stage stage = new Stage();
            stage.setTitle("Purchase Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Error opening purchase window: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // For debugging
        }
    }

    private void loadSuppliers() {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT name FROM suppliers");
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> suppliers = FXCollections.observableArrayList();
            while (rs.next()) {
                suppliers.add(rs.getString("name"));
            }
            suppliersComboBox.setItems(suppliers);
        } catch (SQLException e) {
            showAlert("Error", "Error loading suppliers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAggregate() {
        String selectedSupplier = suppliersComboBox.getValue();
        if (selectedSupplier == null || selectedSupplier.isEmpty()) {
            showAlert("Input Error", "Please select a supplier", Alert.AlertType.WARNING);
            return;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT p.purchase_date AS date, p.id AS id, 'شراء' AS description, p.total_cost AS amount, " +
                            "(SELECT SUM(total_cost) FROM purchases WHERE supplier_id = s.id) AS balance " +
                            "FROM purchases p INNER JOIN suppliers s ON p.supplier_id = s.id WHERE s.name = ? " +
                            "UNION ALL " +
                            "SELECT d.deposit_date AS date, d.id AS id, 'توريد' AS description, d.amount AS amount, " +
                            "(SELECT SUM(amount) FROM deposits WHERE supplier_id = s.id) AS balance " +
                            "FROM deposits d INNER JOIN suppliers s ON d.supplier_id = s.id WHERE s.name = ? " +
                            "ORDER BY date"
            );
            stmt.setString(1, selectedSupplier);
            stmt.setString(2, selectedSupplier);
            ResultSet rs = stmt.executeQuery();

            accountStatementTable.getItems().clear();
            double paidAmount = 0;
            double transactionVolume = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");

                if (description.equals("شراء")) {
                    transactionVolume += amount;
                } else if (description.equals("توريد")) {
                    paidAmount += amount;
                }

                accountStatementTable.getItems().add(new AccountStatement(id, date, description, amount, transactionVolume - paidAmount));
            }

            paidAmountField.setText(String.format("%,.2f", paidAmount));
            transactionVolumeField.setText(String.format("%,.2f", transactionVolume));

        } catch (SQLException e) {
            showAlert("Error", "Error fetching account statement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportPDF() {
        if (accountStatementTable.getItems().isEmpty()) {
            showAlert("No account statements to export", "Empty", Alert.AlertType.WARNING);
            return;
        }

        String selectedSupplier = suppliersComboBox.getValue();
        if (selectedSupplier == null || selectedSupplier.isEmpty()) {
            showAlert("Please select a supplier", "No Supplier", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(accountStatementTable.getScene().getWindow());

        if (file != null) {
            try {
                JasperReport jasperReport = JasperCompileManager.compileReport(
                        getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/SupplierAccountStatement.jrxml"));

                List<Map<String, Object>> dataList = new ArrayList<>();
                for (AccountStatement item : accountStatementTable.getItems()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", item.getDate());
                    map.put("describe", item.getDescription());  // Changed to "describe"
                    map.put("amount", item.getAmount());
                    map.put("balance", item.getBalance());
                    dataList.add(map);
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

                // Parse numbers safely
                double totalPaid = 0;
                double transVolume = 0;
                try {
                    String totalPaidText = paidAmountField.getText().replaceAll("[^\\d.]", "");
                    totalPaid = Double.parseDouble(totalPaidText);

                    String transVolumeText = transactionVolumeField.getText().replaceAll("[^\\d.]", "");
                    transVolume = Double.parseDouble(transVolumeText);
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid number format in fields", Alert.AlertType.ERROR);
                    return;
                }

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("SupplierName", selectedSupplier);
                parameters.put("TotalValuePaid", totalPaid);
                parameters.put("TransVolume", transVolume);
                parameters.put("REPORT_LOCALE", new Locale("ar"));

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());

                showAlert("PDF exported successfully", "Done", Alert.AlertType.INFORMATION);
            } catch (JRException e) {
                showAlert("Error exporting PDF: ", e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class AccountStatement {
        private final int id;
        private final String date;
        private final String description;
        private final double amount;
        private final double balance;

        public AccountStatement(int id, String date, String description, double amount, double balance) {
            this.id = id;
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.balance = balance;
        }

        public int getId() {
            return id;
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
