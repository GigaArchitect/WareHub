package nasar.mustafa.warehub.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SalesAndPurchaseReportsController implements Initializable {

    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private ComboBox<String> itemComboBox;
    
    @FXML
    private BarChart<String, Number> comparisonChart;
    
    @FXML
    private CategoryAxis xAxis;
    
    @FXML
    private NumberAxis yAxis;
    
    @FXML
    private TableView<TransactionRecord> transactionsTable;
    
    @FXML
    private TableColumn<TransactionRecord, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<TransactionRecord, String> typeColumn;
    
    @FXML
    private TableColumn<TransactionRecord, String> itemColumn;
    
    @FXML
    private TableColumn<TransactionRecord, Double> quantityColumn;
    
    @FXML
    private TableColumn<TransactionRecord, Double> priceColumn;
    
    @FXML
    private TableColumn<TransactionRecord, Double> totalColumn;
    
    @FXML
    private Label totalSalesLabel;
    
    @FXML
    private Label totalPurchasesLabel;
    
    @FXML
    private Label netProfitLabel;
    
    private ObservableList<TransactionRecord> transactionsData = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize date pickers
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
        
        // Initialize table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Set table data
        transactionsTable.setItems(transactionsData);
        
        // Load items
        loadItems();
        
        // Configure chart
        xAxis.setLabel("التاريخ");
        yAxis.setLabel("القيمة");
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
        transactionsData.clear();
        comparisonChart.getData().clear();
        
        // TODO: Fetch sales and purchase data from database based on filters
        // For now, add sample data
        transactionsData.addAll(
            new TransactionRecord(LocalDate.now().minusDays(10), "شراء", "صنف 1", 20, 70, 1400),
            new TransactionRecord(LocalDate.now().minusDays(8), "بيع", "صنف 1", 5, 100, 500),
            new TransactionRecord(LocalDate.now().minusDays(5), "شراء", "صنف 2", 10, 120, 1200),
            new TransactionRecord(LocalDate.now().minusDays(3), "بيع", "صنف 1", 8, 100, 800),
            new TransactionRecord(LocalDate.now().minusDays(1), "بيع", "صنف 2", 4, 180, 720)
        );
        
        // Calculate totals
        double totalSales = transactionsData.stream()
                .filter(record -> record.getType().equals("بيع"))
                .mapToDouble(TransactionRecord::getTotal)
                .sum();
                
        double totalPurchases = transactionsData.stream()
                .filter(record -> record.getType().equals("شراء"))
                .mapToDouble(TransactionRecord::getTotal)
                .sum();
                
        double netProfit = totalSales - totalPurchases;
        
        // Update labels
        totalSalesLabel.setText(String.format("%.2f", totalSales));
        totalPurchasesLabel.setText(String.format("%.2f", totalPurchases));
        netProfitLabel.setText(String.format("%.2f", netProfit));
        
        // Update chart
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("المبيعات");
        
        XYChart.Series<String, Number> purchasesSeries = new XYChart.Series<>();
        purchasesSeries.setName("المشتريات");
        
        // Group by date for chart
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        transactionsData.stream()
                .filter(record -> record.getType().equals("بيع"))
                .forEach(record -> 
                    salesSeries.getData().add(new XYChart.Data<>(record.getDate().format(formatter), record.getTotal()))
                );
                
        transactionsData.stream()
                .filter(record -> record.getType().equals("شراء"))
                .forEach(record -> 
                    purchasesSeries.getData().add(new XYChart.Data<>(record.getDate().format(formatter), record.getTotal()))
                );
        
        comparisonChart.getData().addAll(salesSeries, purchasesSeries);
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
    
    // Model class for transaction records
    public static class TransactionRecord {
        private final LocalDate date;
        private final String type;
        private final String item;
        private final double quantity;
        private final double price;
        private final double total;
        
        public TransactionRecord(LocalDate date, String type, String item, 
                               double quantity, double price, double total) {
            this.date = date;
            this.type = type;
            this.item = item;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }
        
        public LocalDate getDate() { return date; }
        public String getType() { return type; }
        public String getItem() { return item; }
        public double getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return total; }
    }
}
