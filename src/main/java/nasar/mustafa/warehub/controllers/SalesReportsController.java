package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SalesReportsController {

    @FXML
    private TableView<SaleReportRow> salesTable;

    @FXML
    private TableColumn<SaleReportRow, String> customerNameColumn;

    @FXML
    private TableColumn<SaleReportRow, String> saleDateColumn;

    @FXML
    private TableColumn<SaleReportRow, Double> totalPriceColumn;

    @FXML
    private TextField totalSaleValueField;

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadSalesData();
    }

    @FXML
    public void initialize() {
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        saleDateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
    }

    @FXML
    private void handleRefresh() {
        loadSalesData();
    }

    private void loadSalesData() {
        if (connection == null) {
            showAlert("Database Error", "No database connection", Alert.AlertType.ERROR);
            return;
        }

        String query = "SELECT c.name AS customerName, s.sale_date AS saleDate, s.total_price AS totalPrice " +
                "FROM sales s " +
                "INNER JOIN customers c ON s.customer_id = c.id " +
                "ORDER BY s.sale_date";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            salesTable.getItems().clear();
            double totalSaleValue = 0;

            while (rs.next()) {
                String customerName = rs.getString("customerName");
                String saleDate = rs.getString("saleDate");
                double totalPrice = rs.getDouble("totalPrice");

                salesTable.getItems().add(new SaleReportRow(customerName, saleDate, totalPrice));
                totalSaleValue += totalPrice;
            }

            totalSaleValueField.setText(String.format("%,.2f", totalSaleValue));

        } catch (SQLException e) {
            showAlert("Database Error", "Error loading sales data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class SaleReportRow {
        private final String customerName;
        private final String saleDate;
        private final double totalPrice;

        public SaleReportRow(String customerName, String saleDate, double totalPrice) {
            this.customerName = customerName;
            this.saleDate = saleDate;
            this.totalPrice = totalPrice;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getSaleDate() {
            return saleDate;
        }

        public double getTotalPrice() {
            return totalPrice;
        }
    }
}
