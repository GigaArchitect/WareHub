package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.control.SearchableComboBox;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;
import javafx.event.ActionEvent;

import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.ResultSet;

public class ShowVehicleExpensesController {
    @FXML
    SearchableComboBox selectCarsCombo;

    @FXML
    TableView<CarExpensesRow> expensesTableView;

    @FXML
    TableColumn<CarExpensesRow, String> expenseName;

    @FXML
    TableColumn<CarExpensesRow, Double> expensePrice;

    @FXML
    TableColumn<CarExpensesRow, Integer> expenseId;

    @FXML
    TableColumn<CarExpensesRow, String> expenseDate;

    @FXML
    private TextField totalExpensesField;

    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected void initialize() {
        expenseName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expenseName"));
        expensePrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expensePrice"));
        expenseDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expenseDate"));
        expenseId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expenseId"));
    }

    protected void populateItemsCombo() {
        try {
            ResultSet rs = connection.prepareStatement("SELECT plate_number FROM vehicles").executeQuery();
            while (rs.next()) {
                selectCarsCombo.getItems().add(rs.getString("plate_number"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching vehicles from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    @FXML
    protected void showExpenses() {
        if (connection == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connection is not set!");
            alert.showAndWait();
            return;
        }

        try {
            String query = "SELECT * FROM vehicle_expenses WHERE vehicle_id = (SELECT id FROM vehicles WHERE plate_number = '" + selectCarsCombo.getValue() + "')";
            ResultSet rs = connection.prepareStatement(query).executeQuery();

            expensesTableView.getItems().clear();
            double totalExpenses = 0.0;

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                totalExpenses += amount;
                expensesTableView.getItems().add(new CarExpensesRow(
                        rs.getString("expense_name"),
                        amount,
                        rs.getInt("id"),
                        rs.getString("date")
                ));
            }

            totalExpensesField.setText(String.format("%,.2f", totalExpenses));

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching expenses from the database: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onExportPDF(ActionEvent event) {
        if (expensesTableView.getItems().isEmpty()) {
            showAlert("No expenses to export", Alert.AlertType.WARNING);
            return;
        }

        String selectedVehicle = (String) selectCarsCombo.getValue();
        if (selectedVehicle == null || selectedVehicle.isEmpty()) {
            showAlert("Please select a vehicle", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(expensesTableView.getScene().getWindow());

        if (file != null) {
            try {
                // Load the report template
                JasperReport jasperReport = JasperCompileManager.compileReport(
                        getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/ShowVehicleExpenses.jrxml"));

                // Create data source from expenses items
                List<Map<String, Object>> dataList = new ArrayList<>();
                for (CarExpensesRow item : expensesTableView.getItems()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", item.getExpenseId());
                    map.put("expense_name", item.getExpenseName());
                    map.put("amount", item.getExpensePrice());
                    map.put("date", item.getExpenseDate()); // Ensure date is in yyyy-MM-dd format
                    dataList.add(map);
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

                // Get total value from the totalExpensesField
                String totalText = totalExpensesField.getText().replace(",", "");
                double totalValue = Double.parseDouble(totalText);

                // Set parameters
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("TotalValue", totalValue);

                // Fill and export the report
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());

                showAlert("PDF exported successfully", Alert.AlertType.INFORMATION);
            } catch (JRException e) {
                showAlert("Error exporting PDF: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                showAlert("Invalid date format: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String noExpensesToExport, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, noExpensesToExport);
        alert.showAndWait();
    }


    public static class CarExpensesRow{
        private String expenseName;
        private double expensePrice;
        private int expenseId;
        private String expenseDate;

        public CarExpensesRow(String expenseName, double expensePrice, int expenseId, String expenseDate) {
            this.expenseName = expenseName;
            this.expensePrice = expensePrice;
            this.expenseId = expenseId;
            this.expenseDate = expenseDate;
        }

        public String getExpenseName() {
            return expenseName;
        }

        public double getExpensePrice() {
            return expensePrice;
        }

        public String getExpenseDate() {
            return expenseDate;
        }

        public int getExpenseId() {
            return expenseId;
        }
    }
}

