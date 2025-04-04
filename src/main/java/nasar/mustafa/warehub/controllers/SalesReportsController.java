package nasar.mustafa.warehub.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.fonts.SimpleFontFace;
import net.sf.jasperreports.engine.fonts.SimpleFontFamily;
import net.sf.jasperreports.engine.fonts.SimpleFontSet;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SalesReportsController implements Initializable {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> itemComboBox;
    @FXML private TableView<SaleRecord> salesTable;
    @FXML private TableColumn<SaleRecord, LocalDate> dateColumn;
    @FXML private TableColumn<SaleRecord, String> customerColumn;
    @FXML private TableColumn<SaleRecord, String> itemColumn;
    @FXML private TableColumn<SaleRecord, Double> quantityColumn;
    @FXML private TableColumn<SaleRecord, Double> priceColumn;
    @FXML private TableColumn<SaleRecord, Double> totalColumn;
    @FXML private TableColumn<SaleRecord, String> invoiceNumberColumn;
    @FXML private Label totalSalesLabel;

    private Connection connection;
    private final ObservableList<SaleRecord> salesData = FXCollections.observableArrayList();
    private String sqlQuery;

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadInitialData();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDatePickers();
        setupTableColumns();
        salesTable.setItems(salesData);
    }

    private void setupDatePickers() {
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        invoiceNumberColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadInitialData() {
        loadCustomers();
        loadItems();
    }

    private void loadCustomers() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM customers ORDER BY name")) {

            ObservableList<String> customers = FXCollections.observableArrayList("الكل");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(rs.getString("name"));
            }

            customerComboBox.setItems(customers);
            customerComboBox.setValue("الكل");

        } catch (SQLException e) {
            showAlert("خطأ", "فشل في تحميل العملاء: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadItems() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM items ORDER BY name")) {

            ObservableList<String> items = FXCollections.observableArrayList("الكل");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(rs.getString("name"));
            }

            itemComboBox.setItems(items);
            itemComboBox.setValue("الكل");

        } catch (SQLException e) {
            showAlert("خطأ", "فشل في تحميل الأصناف: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onGenerateReport() {
        salesData.clear();
        StringBuilder query = new StringBuilder("""
        SELECT s.id, s.sale_date, c.name AS customer_name,
               i.name AS item_name, si.quantity, si.unit_price, si.total_price
        FROM sales s
        JOIN customers c ON s.customer_id = c.id
        JOIN sales_items si ON s.id = si.sale_id
        JOIN items i ON si.item_id = i.id
        WHERE DATE(s.sale_date) BETWEEN DATE('""")
                .append(fromDatePicker.getValue().toString())
                .append("') AND DATE('")
                .append(toDatePicker.getValue().toString())
                .append("')");

        if (!customerComboBox.getValue().equals("الكل")) {
            query.append(" AND c.name = '").append(customerComboBox.getValue()).append("'");
        }
        if (!itemComboBox.getValue().equals("الكل")) {
            query.append(" AND i.name = '").append(itemComboBox.getValue()).append("'");
        }
        query.append(" ORDER BY s.sale_date DESC");

        sqlQuery = query.toString();

        try (PreparedStatement stmt = connection.prepareStatement(sqlQuery)) {
            ResultSet rs = stmt.executeQuery();
            double totalAmount = 0;

            while (rs.next()) {
                double total = rs.getDouble("total_price");
                totalAmount += total;

                salesData.add(new SaleRecord(
                        String.format("SAL-%04d", rs.getInt("id")),
                        LocalDate.parse(rs.getString("sale_date").split(" ")[0]),
                        rs.getString("customer_name"),
                        rs.getString("item_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price"),
                        total
                ));
            }

            totalSalesLabel.setText(String.format("%,.2f", totalAmount));

        } catch (SQLException e) {
            showAlert("خطأ", "فشل في تحميل البيانات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void generatePDFReport(File file) throws JRException {
        JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/SalesReports.jrxml"));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("SQL_STAT", sqlQuery);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
        JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());
    }

    @FXML
    private void onExportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(salesTable.getScene().getWindow());

        if (file != null) {
            try {
                generatePDFReport(file);
                showAlert("Success", "PDF report generated successfully.", Alert.AlertType.INFORMATION);
            } catch (JRException e) {
                showAlert("Error", "Failed to generate PDF report: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class SaleRecord {
        private final String invoiceNumber;
        private final LocalDate date;
        private final String customer;
        private final String item;
        private final double quantity;
        private final double price;
        private final double total;

        public SaleRecord(String invoiceNumber, LocalDate date, String customer,
                          String item, double quantity, double price, double total) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.customer = customer;
            this.item = item;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }

        // Getters
        public String getInvoiceNumber() { return invoiceNumber; }
        public LocalDate getDate() { return date; }
        public String getCustomer() { return customer; }
        public String getItem() { return item; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}
