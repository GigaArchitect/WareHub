package nasar.mustafa.warehub.controllers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PurchaseReportsController implements Initializable {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> supplierComboBox;
    @FXML private ComboBox<String> itemComboBox;
    @FXML private TableView<PurchaseRecord> purchasesTable;
    @FXML private TableColumn<PurchaseRecord, LocalDate> dateColumn;
    @FXML private TableColumn<PurchaseRecord, String> invoiceNumberColumn;
    @FXML private TableColumn<PurchaseRecord, String> supplierColumn;
    @FXML private TableColumn<PurchaseRecord, String> itemColumn;
    @FXML private TableColumn<PurchaseRecord, Double> quantityColumn;
    @FXML private TableColumn<PurchaseRecord, Double> priceColumn;
    @FXML private TableColumn<PurchaseRecord, Double> totalColumn;
    @FXML private Label totalPurchasesLabel;

    private Connection connection;
    private FXCollections FXCollections;
    private final ObservableList<PurchaseRecord> purchasesData = FXCollections.observableArrayList();

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadInitialData();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDatePickers();
        setupTableColumns();
        purchasesTable.setItems(purchasesData);
    }

    private void setupDatePickers() {
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        invoiceNumberColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadInitialData() {
        loadSuppliers();
        loadItems();
    }

    private void loadSuppliers() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM suppliers ORDER BY name")) {

            ObservableList<String> suppliers = FXCollections.observableArrayList("الكل");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(rs.getString("name"));
            }

            supplierComboBox.setItems(suppliers);
            supplierComboBox.setValue("الكل");

        } catch (SQLException e) {
            showAlert("خطأ", "فشل في تحميل الموردين: " + e.getMessage(), Alert.AlertType.ERROR);
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
        purchasesData.clear();
        StringBuilder query = new StringBuilder("""
            SELECT p.id, p.purchase_date, s.name AS supplier_name,
                   i.name AS item_name, pi.quantity, pi.unit_price, pi.total_cost
            FROM purchases p
            JOIN suppliers s ON p.supplier_id = s.id
            JOIN purchase_items pi ON p.id = pi.purchase_id
            JOIN items i ON pi.item_id = i.id
            WHERE DATE(p.purchase_date) BETWEEN DATE(?) AND DATE(?)
        """);

        if (!supplierComboBox.getValue().equals("الكل")) {
            query.append(" AND s.name = ?");
        }
        if (!itemComboBox.getValue().equals("الكل")) {
            query.append(" AND i.name = ?");
        }
        query.append(" ORDER BY p.purchase_date DESC");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, fromDatePicker.getValue().toString());
            stmt.setString(paramIndex++, toDatePicker.getValue().toString());

            if (!supplierComboBox.getValue().equals("الكل")) {
                stmt.setString(paramIndex++, supplierComboBox.getValue());
            }
            if (!itemComboBox.getValue().equals("الكل")) {
                stmt.setString(paramIndex, itemComboBox.getValue());
            }

            ResultSet rs = stmt.executeQuery();
            double totalAmount = 0;

            while (rs.next()) {
                double total = rs.getDouble("total_cost");
                totalAmount += total;

                purchasesData.add(new PurchaseRecord(
                        String.format("PUR-%04d", rs.getInt("id")),
                        LocalDate.parse(rs.getString("purchase_date").split(" ")[0]),
                        rs.getString("supplier_name"),
                        rs.getString("item_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price"),
                        total
                ));
            }

            totalPurchasesLabel.setText(String.format("%,.2f", totalAmount));

        } catch (SQLException e) {
            showAlert("خطأ", "فشل في تحميل البيانات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportReport() {
        showAlert("تنبيه", "سيتم تنفيذ التصدير قريباً", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class PurchaseRecord {
        private final String invoiceNumber;
        private final LocalDate date;
        private final String supplier;
        private final String item;
        private final double quantity;
        private final double price;
        private final double total;

        public PurchaseRecord(String invoiceNumber, LocalDate date, String supplier,
                              String item, double quantity, double price, double total) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.supplier = supplier;
            this.item = item;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }

        // Getters
        public String getInvoiceNumber() { return invoiceNumber; }
        public LocalDate getDate() { return date; }
        public String getSupplier() { return supplier; }
        public String getItem() { return item; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}
