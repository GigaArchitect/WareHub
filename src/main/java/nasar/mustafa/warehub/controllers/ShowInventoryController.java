package nasar.mustafa.warehub.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ShowInventoryController implements Initializable {

    @FXML
    private TextField searchField;
    
    @FXML
    private TableView<InventoryItem> inventoryTable;
    
    @FXML
    private TableColumn<InventoryItem, String> itemCodeColumn;
    
    @FXML
    private TableColumn<InventoryItem, String> itemNameColumn;
    
    @FXML
    private TableColumn<InventoryItem, Double> quantityColumn;
    
    @FXML
    private TableColumn<InventoryItem, Double> purchasePriceColumn;
    
    @FXML
    private TableColumn<InventoryItem, Double> salePriceColumn;
    
    @FXML
    private TableColumn<InventoryItem, Double> totalValueColumn;
    
    @FXML
    private Label totalItemsLabel;
    
    @FXML
    private Label totalValueLabel;
    
    private ObservableList<InventoryItem> allInventoryItems = FXCollections.observableArrayList();
    private ObservableList<InventoryItem> filteredInventoryItems = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        salePriceColumn.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        
        // Load inventory data
        loadInventoryData();
        
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterInventory(newValue);
        });
    }
    
    private void loadInventoryData() {
        // TODO: Load from database
        // For now, add sample data
        allInventoryItems.addAll(
            new InventoryItem("ITM001", "صنف 1", 50, 70, 100, 3500),
            new InventoryItem("ITM002", "صنف 2", 25, 120, 180, 3000),
            new InventoryItem("ITM003", "صنف 3", 100, 30, 50, 3000),
            new InventoryItem("ITM004", "صنف 4", 15, 200, 280, 3000)
        );
        
        filteredInventoryItems.addAll(allInventoryItems);
        inventoryTable.setItems(filteredInventoryItems);
        
        updateTotals();
    }
    
    private void filterInventory(String searchText) {
        filteredInventoryItems.clear();
        
        if (searchText == null || searchText.isEmpty()) {
            filteredInventoryItems.addAll(allInventoryItems);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            
            for (InventoryItem item : allInventoryItems) {
                if (item.getItemCode().toLowerCase().contains(lowerCaseFilter) ||
                    item.getItemName().toLowerCase().contains(lowerCaseFilter)) {
                    filteredInventoryItems.add(item);
                }
            }
        }
        
        updateTotals();
    }
    
    private void updateTotals() {
        int totalItems = filteredInventoryItems.size();
        double totalValue = filteredInventoryItems.stream()
                .mapToDouble(InventoryItem::getTotalValue)
                .sum();
                
        totalItemsLabel.setText(String.valueOf(totalItems));
        totalValueLabel.setText(String.format("%.2f", totalValue));
    }
    
    @FXML
    private void onExportInventory() {
        // TODO: Implement export functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("تصدير المخزون");
        alert.setHeaderText(null);
        alert.setContentText("سيتم تصدير تقرير المخزون قريباً");
        alert.showAndWait();
    }
    
    @FXML
    private void onPrintInventory() {
        // TODO: Implement print functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("طباعة المخزون");
        alert.setHeaderText(null);
        alert.setContentText("سيتم طباعة تقرير المخزون قريباً");
        alert.showAndWait();
    }
    
    // Model class for inventory items
    public static class InventoryItem {
        private final String itemCode;
        private final String itemName;
        private final double quantity;
        private final double purchasePrice;
        private final double salePrice;
        private final double totalValue;
        
        public InventoryItem(String itemCode, String itemName, double quantity, 
                           double purchasePrice, double salePrice, double totalValue) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.quantity = quantity;
            this.purchasePrice = purchasePrice;
            this.salePrice = salePrice;
            this.totalValue = totalValue;
        }
        
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public double getQuantity() { return quantity; }
        public double getPurchasePrice() { return purchasePrice; }
        public double getSalePrice() { return salePrice; }
        public double getTotalValue() { return totalValue; }
    }
}
