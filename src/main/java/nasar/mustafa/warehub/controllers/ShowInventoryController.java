package nasar.mustafa.warehub.controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.SearchableComboBox;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;
import javafx.event.ActionEvent;

public class ShowInventoryController implements Initializable {

    @FXML
    private SearchableComboBox<String> itemsCombo;

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<InventoryItem, String> codeColumn;

    @FXML
    private TableColumn<InventoryItem, String> nameColumn;

    @FXML
    private TableColumn<InventoryItem, Double> quantityColumn;

    @FXML
    private TableColumn<InventoryItem, Double> purchasePriceColumn;

    @FXML
    private TableColumn<InventoryItem, Double> sellingPriceColumn;

    @FXML
    private TableColumn<InventoryItem, Double> totalValueColumn;

    @FXML
    private Label itemCountLabel;

    @FXML
    private Label totalValueLabel;

    private ObservableList<InventoryItem> allInventoryItems = FXCollections.observableArrayList();
    private ObservableList<InventoryItem> filteredInventoryItems = FXCollections.observableArrayList();

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadInventoryData();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

        itemsCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterInventory(newVal);
        });
    }

    private void loadInventoryData() {
        allInventoryItems.clear();
        String query = """
            SELECT i.id AS item_id, i.name AS item_name,
                   ip.price_buy, ip.price_sell, ip.stock_quantity,
                   (ip.price_buy * ip.stock_quantity) AS total_value
            FROM items i
            JOIN item_prices ip ON i.id = ip.item_id
            WHERE ip.effective_date = (
                SELECT MAX(effective_date)
                FROM item_prices
                WHERE item_id = i.id AND (end_date IS NULL OR end_date > CURRENT_TIMESTAMP)
            )
            ORDER BY i.name
            """;
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                allInventoryItems.add(new InventoryItem(
                        "ITM-" + rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("stock_quantity"),
                        rs.getDouble("price_buy"),
                        rs.getDouble("price_sell"),
                        rs.getDouble("total_value")
                ));
            }
            filteredInventoryItems.addAll(allInventoryItems);
            inventoryTable.setItems(filteredInventoryItems);

            // Populate combo box with product names
            itemsCombo.getItems().clear();
            itemsCombo.getItems().addAll(
                    allInventoryItems.stream()
                            .map(InventoryItem::getItemName)
                            .collect(Collectors.toSet())
            );
            updateTotals();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load inventory: " + e.getMessage());
        }
    }

    private void filterInventory(String selectedItem) {
        filteredInventoryItems.clear();
        if (selectedItem == null || selectedItem.isBlank()) {
            filteredInventoryItems.addAll(allInventoryItems);
        } else {
            for (InventoryItem item : allInventoryItems) {
                if (item.getItemName().equalsIgnoreCase(selectedItem)) {
                    filteredInventoryItems.add(item);
                }
            }
        }
        updateTotals();
    }

    private void updateTotals() {
        itemCountLabel.setText(String.valueOf(filteredInventoryItems.size()));
        double totalValue = filteredInventoryItems.stream()
                .mapToDouble(InventoryItem::getTotalValue)
                .sum();
        totalValueLabel.setText(String.format("%.2f", totalValue));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onExportPDF(ActionEvent event) {
        if (inventoryTable.getItems().isEmpty()) {
            showAlert("No inventory items to export", String.valueOf(Alert.AlertType.WARNING));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(inventoryTable.getScene().getWindow());

        if (file != null) {
            try {
                JasperReport jasperReport = JasperCompileManager.compileReport(
                        getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/ShowInventory.jrxml"));

                List<Map<String, Object>> dataList = new ArrayList<>();
                for (InventoryItem item : inventoryTable.getItems()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", item.getItemCode());
                    map.put("name", item.getItemName());
                    map.put("quantity", item.getQuantity());
                    map.put("purchase_price", item.getPurchasePrice());
                    map.put("selling_price", item.getSalePrice());
                    map.put("total_value", item.getTotalValue());
                    dataList.add(map);
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

                String totalValueText = totalValueLabel.getText().replace(",", "");
                double totalValue = Double.parseDouble(totalValueText);

                String itemCountText = itemCountLabel.getText();
                int itemCount = Integer.parseInt(itemCountText);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("TotalValues", totalValue);
                parameters.put("NoItems", itemCount);

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("PDF Exported Successfully");
                alert.showAndWait();

            } catch (JRException e) {
                showAlert("Error exporting PDF: " + e.getMessage(), String.valueOf(Alert.AlertType.ERROR));
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                showAlert("Invalid number format: " + e.getMessage(), String.valueOf(Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        }
    }

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
