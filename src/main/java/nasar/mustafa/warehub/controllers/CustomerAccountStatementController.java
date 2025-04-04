package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javafx.event.ActionEvent;

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
    private TableColumn<AccountStatementRow, Integer> idColumn;
    
    @FXML
    private TextField paidAmountField;

    @FXML
    private TextField transactionVolumeField;

    @FXML
    private BorderPane contentPane;

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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        accountStatementTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && accountStatementTable.getSelectionModel().getSelectedItem() != null) {
                AccountStatementRow selectedRow = accountStatementTable.getSelectionModel().getSelectedItem();
                if ("بيع".equals(selectedRow.getDescription())) {
                    openReceiptWindow(selectedRow.getId());
                }
            }
        });
    }

    private void openReceiptWindow(int saleId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddCustomerReceipt.fxml"));
            Parent root = loader.load();

            AddCustomerReceiptController controller = loader.getController();
            controller.setConnection(connection);
            controller.loadReceiptData(saleId);
	    controller.disableViewButtons();

            Stage stage = new Stage();
            stage.setTitle("Receipt Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Error opening receipt window: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
                "SELECT s.sale_date AS date, s.id AS id , 'بيع' AS description, s.total_price AS amount, " +
                            "(SELECT SUM(total_price) FROM sales WHERE customer_id = c.id) AS balance " +
                            "FROM sales s INNER JOIN customers c ON s.customer_id = c.id WHERE c.name = ? " +
                            "UNION ALL " +
                "SELECT p.payment_date AS date, p.id AS id , 'دفع' AS description, -p.amount AS amount, " +
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
		int id = rs.getInt("id");

                if (description.equals("بيع")) {
                    transactionVolume += amount;
                } else if (description.equals("دفع")) {
                    paidAmount += -amount;
                }

                accountStatementTable.getItems().add(new AccountStatementRow(date, description, amount, transactionVolume - paidAmount, id));
            }

            paidAmountField.setText(String.format("%,.2f", paidAmount));
            transactionVolumeField.setText(String.format("%,.2f", transactionVolume));

        } catch (SQLException e) {
            showAlert("Error", "Error fetching account statement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportPDF(ActionEvent event) {
        if (accountStatementTable.getItems().isEmpty()) {
            showAlert("No account statements to export", "Empty"  ,Alert.AlertType.WARNING);
            return;
        }

        String selectedCustomer = customerComboBox.getValue();
        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            showAlert("Please select a customer", "No Customer" ,Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(accountStatementTable.getScene().getWindow());

        if (file != null) {
            try {
                // Load the report template
                JasperReport jasperReport = JasperCompileManager.compileReport(
                        getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/CustomerAccountStatement.jrxml"));

                // Create data source from account statement items
                List<Map<String, Object>> dataList = new ArrayList<>();
                for (AccountStatementRow item : accountStatementTable.getItems()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", item.getDate());
                    map.put("describe", item.getDescription());
                    map.put("amount", item.getAmount());
                    map.put("balance", item.getBalance());
                    dataList.add(map);
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

                // Get total values from the fields
                String totalPaidText = paidAmountField.getText().replace(",", "");
                double totalPaid = Double.parseDouble(totalPaidText);

                String transVolumeText = transactionVolumeField.getText().replace(",", "");
                double transVolume = Double.parseDouble(transVolumeText);

                // Set parameters
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("CustomerName", selectedCustomer);
                parameters.put("TotalValuePaid", totalPaid);
                parameters.put("TransVolume", transVolume);

                // Fill and export the report
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());

                showAlert("PDF exported successfully", "Done" ,Alert.AlertType.INFORMATION);
            } catch (JRException e) {
                showAlert("Error exporting PDF: ", e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                showAlert("Invalid number format: ", e.getMessage(), Alert.AlertType.ERROR);
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

    public static class AccountStatementRow {
        private final String date;
        private final String description;
        private final double amount;
        private final double balance;
	private final int id;

        public AccountStatementRow(String date, String description, double amount, double balance, int id) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.balance = balance;
	    this.id = id;
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

        public int getId() {
            return id;
        }

    }
}
