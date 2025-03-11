package nasar.mustafa.warehub.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PurchaseReportsController implements Initializable {

    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private ComboBox<String> supplierComboBox;
    
    @FXML
    private ComboBox<String> itemComboBox;
    
    @FXML
    private TableView<PurchaseRecord> purchasesTable;
    
    @FXML
    private TableColumn<PurchaseRecord, String> invoiceNumberColumn;
    
    @FXML
    private TableColumn<PurchaseRecord, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<PurchaseRecord, String> supplierColumn;
    
    @FXML
    private TableColumn<PurchaseRecord, String> itemColumn;
    
    @FXML
    private TableColumn<PurchaseRecord, Double> quantityColumn;
    
    @FXML
    private TableColumn<PurchaseRecord, Double> priceColumn;
    
    @FXML
    private TableColumn<PurchaseRecord, Double> totalColumn;
    
    @FXML
    private Label totalPurchasesLabel;
    
    private ObservableList<PurchaseRecord> purchasesData = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize date pickers
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
        
        // Initialize table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        invoiceNumberColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Set table data
        purchasesTable.setItems(purchasesData);
        
        // Load suppliers and items
        loadSuppliers();
        loadItems();
    }
    
    private void loadSuppliers() {
        // TODO: Load from database
        ObservableList<String> suppliers = FXCollections.observableArrayList(
            "الكل", "مورد 1", "مورد 2", "مورد 3"
        );
        supplierComboBox.setItems(suppliers);
        supplierComboBox.setValue("الكل");
    }
    
    private void loadItems() {
        // TODO: Load from database
        ObservableList<String> items = FXCollections.observableArrayList(
            "الكل", "صنف 1", "صنف 2", "صنف 3"
        );
        itemComboBox.setItems(items);
        itemComboBox.setValue("الكل");
    }
    
    @FXML
    private void onGenerateReport() {
        // Clear previous data
        purchasesData.clear();
        
        // TODO: Fetch purchase data from database based on filters
        // For now, add sample data
        purchasesData.addAll(
            new PurchaseRecord("PUR-001", LocalDate.now().minusDays(7), "مورد 1", "صنف 1", 10, 80, 800),
            new PurchaseRecord("PUR-002", LocalDate.now().minusDays(4), "مورد 2", "صنف 2", 5, 150, 750),
            new PurchaseRecord("PUR-003", LocalDate.now().minusDays(2), "مورد 1", "صنف 3", 20, 40, 800)
        );
        
        // Update total
        double total = purchasesData.stream().mapToDouble(PurchaseRecord::getTotal).sum();
        totalPurchasesLabel.setText(String.format("%.2f", total));
    }
    
    @FXML
    private void onExportReport() {
        // TODO: Implement export functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("تصدير التقرير");
        alert.setHeaderText(null);
        alert.setContentText("سيتم تصدير التقرير قريباً");
        alert.showAndWait();
    }
    
    // Model class for purchase records
    public static class PurchaseRecord {
        private final String invoiceNumber;
        private final LocalDate date;
        private final String supplier;
        private final String item;
        private final double quantity;
        private final double price;
        private final double total;
        
        public PurchaseRecord(String invoiceNumber, LocalDate date, String supplier, String item, 
                            double quantity, double price, double total) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.supplier = supplier;
            this.item = item;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }
        
        public String getInvoiceNumber() { return invoiceNumber; }
        public LocalDate getDate() { return date; }
        public String getSupplier() { return supplier; }
        public String getItem() { return item; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}
