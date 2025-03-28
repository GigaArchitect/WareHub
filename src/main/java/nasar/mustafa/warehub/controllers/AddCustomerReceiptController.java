package nasar.mustafa.warehub.controllers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.SearchableComboBox;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.util.*;


import java.sql.*;

public class AddCustomerReceiptController {
    private Connection connection;
    private int serialCounter = 1;
    private ObservableList<ReceiptItem> receiptItems = FXCollections.observableArrayList();

    @FXML
    private SearchableComboBox<String> customerCombo;

    @FXML
    private SearchableComboBox<String> itemCombo;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField totalField;

    @FXML
    private TableView<ReceiptItem> receiptTable;

    @FXML
    private TableColumn<ReceiptItem, Double> dateColumn;

    @FXML
    private TableColumn<ReceiptItem, Integer> quantityColumn;

    @FXML
    private TableColumn<ReceiptItem, Double> priceColumn;

    @FXML
    private TableColumn<ReceiptItem, String> itemColumn;

    @FXML
    private TableColumn<ReceiptItem, Integer> serialColumn;

    @FXML
    private Label quantity_found;

    @FXML
    private Button addItemButton;
    
    @FXML
    private Button deleteItemButton;
    
    @FXML
    private Button registerReceiptButton;

    @FXML
    private ComboBox<String> available_prices;

    public void setCustomerCombo(String value){
	this.customerCombo.setValue(value);
    }

    public void disableViewButtons(){
	this.addItemButton.setDisable(true);
	this.deleteItemButton.setDisable(true);
	this.registerReceiptButton.setDisable(true);
    }

    public void setTotalValueField(String value){
	totalField.setText(value);
    }

    @FXML
    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        receiptTable.setItems(receiptItems);

        itemCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                populateAvailablePrices(newValue);
            }
        });

        available_prices.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                updateQuantityFound(newValue);
            }
        });
    }

    private void populateAvailablePrices(String itemName) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT ip.price_sell, ip.stock_quantity FROM item_prices ip " +
                            "INNER JOIN items i ON ip.item_id = i.id " +
                            "WHERE i.name = ? ORDER BY ip.effective_date DESC"
            );
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();

            available_prices.getItems().clear();
            while (rs.next()) {
                double priceSell = rs.getDouble("price_sell");
                int stockQuantity = rs.getInt("stock_quantity");
                available_prices.getItems().add(String.valueOf(priceSell));
            }
        } catch (SQLException e) {
            showAlert("Error fetching available prices: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateQuantityFound(String selectedPrice) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT ip.stock_quantity FROM item_prices ip " +
                            "INNER JOIN items i ON ip.item_id = i.id " +
                            "WHERE ip.price_sell = ? ORDER BY ip.effective_date DESC LIMIT 1"
            );
            stmt.setDouble(1, Double.parseDouble(selectedPrice));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int stockQuantity = rs.getInt("stock_quantity");
                quantity_found.setText(String.valueOf(stockQuantity));
            } else {
                quantity_found.setText("---");
            }
        } catch (SQLException e) {
            showAlert("Error fetching stock quantity: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void addItem(ActionEvent event) {
        try {
            String selectedItem = itemCombo.getValue();
            if (selectedItem == null || selectedItem.isEmpty()) {
                showAlert("Please select an item", Alert.AlertType.WARNING);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert("Please enter a valid quantity", Alert.AlertType.WARNING);
                return;
            }

            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT ip.price_sell, ip.price_buy, ip.stock_quantity FROM item_prices ip " +
                            "INNER JOIN items i ON ip.item_id = i.id " +
                            "WHERE i.name = ? ORDER BY ip.effective_date DESC LIMIT 1"
            );
            stmt.setString(1, selectedItem);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int stockQuantity = rs.getInt("stock_quantity");
                if (quantity > stockQuantity) {
                    showAlert("Insufficient stock. Available: " + stockQuantity, Alert.AlertType.WARNING);
                    return;
                }

                double priceSell = rs.getDouble("price_sell");
                double priceBuy = rs.getDouble("price_buy");
                ReceiptItem item = new ReceiptItem(selectedItem, quantity, priceSell, priceBuy, serialCounter++);
                receiptItems.add(item);
                updateTotal();
                clearInputs();
            }
        } catch (SQLException e) {
            showAlert("Error adding item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void deleteItem(ActionEvent event) {
        ReceiptItem selectedItem = receiptTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            receiptItems.remove(selectedItem);
            updateTotal();
        }
    }

    @FXML
    public void saveReceipt(ActionEvent event) {
        if (receiptItems.isEmpty()) {
            showAlert("Cannot save empty receipt", Alert.AlertType.WARNING);
            return;
        }

        String selectedCustomer = customerCombo.getValue();
        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            showAlert("Please select a customer", Alert.AlertType.WARNING);
            return;
        }

        try {
            connection.setAutoCommit(false);

            PreparedStatement customerStmt = connection.prepareStatement(
                    "SELECT id FROM customers WHERE name = ?"
            );
            customerStmt.setString(1, selectedCustomer);
            ResultSet customerRs = customerStmt.executeQuery();

            if (!customerRs.next()) {
                throw new SQLException("Customer not found");
            }
            int customerId = customerRs.getInt("id");

            // Create sale record
            PreparedStatement saleStmt = connection.prepareStatement(
                    "INSERT INTO sales (customer_id, total_price) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            saleStmt.setInt(1, customerId);
            String totalText = totalField.getText().replace(",", "");
            saleStmt.setDouble(2, Double.parseDouble(totalText));
            saleStmt.executeUpdate();

            ResultSet generatedKeys = saleStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Failed to create sale record");
            }
            int saleId = generatedKeys.getInt(1);

            for (ReceiptItem item : receiptItems) {
                PreparedStatement itemStmt = connection.prepareStatement(
                        "SELECT id FROM items WHERE name = ?"
                );
                itemStmt.setString(1, item.getItemName());
                ResultSet itemRs = itemStmt.executeQuery();
                if (!itemRs.next()) {
                    throw new SQLException("Item not found: " + item.getItemName());
                }
                int itemId = itemRs.getInt("id");

                PreparedStatement saleItemStmt = connection.prepareStatement(
                        "INSERT INTO sales_items (sale_id, item_id, quantity, unit_price, unit_price_buy, total_price) VALUES (?, ?, ?, ?, ?, ?)"
                );
                saleItemStmt.setInt(1, saleId);
                saleItemStmt.setInt(2, itemId);
                saleItemStmt.setInt(3, item.getQuantity());
                saleItemStmt.setDouble(4, item.getPrice());
                saleItemStmt.setDouble(6, item.getTotal());
                saleItemStmt.executeUpdate();

                PreparedStatement updateStockStmt = connection.prepareStatement(
                        "UPDATE item_prices SET stock_quantity = stock_quantity - ? " +
                                "WHERE item_id = ? AND effective_date = (SELECT MAX(effective_date) FROM item_prices WHERE item_id = ?)"
                );
                updateStockStmt.setInt(1, item.getQuantity());
                updateStockStmt.setInt(2, itemId);
                updateStockStmt.setInt(3, itemId);
                updateStockStmt.executeUpdate();
            }

            connection.commit();
            showAlert("Receipt saved successfully", Alert.AlertType.INFORMATION);
            clearAll();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            showAlert("Error saving receipt: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void loadReceiptData(int saleId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT i.name, i.id ,si.quantity, si.unit_price, (si.quantity * si.unit_price) AS total_price " +
                            "FROM sales_items si " +
                            "INNER JOIN items i ON si.item_id = i.id " +
                            "WHERE si.sale_id = ?"
            );
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<ReceiptItem> receiptItems = FXCollections.observableArrayList();
            while (rs.next()) {
                String itemName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getDouble("unit_price");
                double totalPrice = rs.getDouble("total_price");
                int itemId = rs.getInt("id");
                receiptItems.add(new ReceiptItem(itemName, quantity, unitPrice, totalPrice, itemId));
            }
            receiptTable.setItems(receiptItems);
	    this.receiptItems = receiptItems;

	    // form info
	    PreparedStatement formInfo = connection.prepareStatement("SELECT sales.total_price, customers.name FROM sales "+
								     "INNER JOIN customers ON sales.customer_id = customers.id WHERE sales.id = ?");
	    formInfo.setInt(1, saleId);
	    rs = formInfo.executeQuery();
	    setTotalValueField(rs.getString("total_price"));
	    setCustomerCombo(rs.getString("name"));

        } catch (SQLException e) {
            showAlert("Error loading receipt data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportPDF(ActionEvent event) {
        if (receiptItems.isEmpty()) {
            showAlert("No items to export", Alert.AlertType.WARNING);
            return;
        }

        String selectedCustomer = customerCombo.getValue();
        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            showAlert("Please select a customer", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(receiptTable.getScene().getWindow());

        if (file != null) {
            try {
                JasperReport jasperReport = JasperCompileManager.compileReport(
                        getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/AddCustomerRecipet.jrxml"));

                List<Map<String, Object>> dataList = new ArrayList<>();
                for (ReceiptItem item : receiptItems) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", item.getSerial());
                    map.put("product_name", item.getItemName());
                    map.put("unit_price", item.getPrice());
                    map.put("quantity", item.getQuantity());
                    map.put("date", java.sql.Date.valueOf(LocalDate.now()));
                    dataList.add(map);
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

                String totalText = totalField.getText().replace(",", "");
                double totalValue = Double.parseDouble(totalText);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("CustomerName", selectedCustomer);
                parameters.put("TotalValue", totalValue);

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());

                showAlert("PDF exported successfully", Alert.AlertType.INFORMATION);
            } catch (JRException e) {
                showAlert("Error exporting PDF: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void updateTotal() {
        double total = receiptItems.stream()
                .mapToDouble(ReceiptItem::getTotal)
                .sum();
        String formattedTotal = String.format("%,.2f", total);
        totalField.setText(formattedTotal);
    }

    private void clearInputs() {
        itemCombo.setValue(null);
        quantityField.clear();
    }

    private void clearAll() {
        clearInputs();
        customerCombo.setValue(null);
        receiptItems.clear();
        totalField.clear();
        serialCounter = 1;
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.CLOSE);
        alert.showAndWait();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateComboBoxes();
    }

    private void populateComboBoxes() {
        try {
            ResultSet customers = connection.createStatement()
                    .executeQuery("SELECT name FROM customers ORDER BY name");
            while (customers.next()) {
                customerCombo.getItems().add(customers.getString("name"));
            }

            ResultSet items = connection.createStatement()
                    .executeQuery("SELECT name FROM items ORDER BY name");
            while (items.next()) {
                itemCombo.getItems().add(items.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Error populating combo boxes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
