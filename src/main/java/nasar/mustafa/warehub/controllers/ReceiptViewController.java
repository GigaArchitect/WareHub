package nasar.mustafa.warehub.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import nasar.mustafa.warehub.controllers.ReceiptItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceiptViewController {
    @FXML
    private TableView<ReceiptItem> receiptTable;
    @FXML
    private TableColumn<ReceiptItem, String> itemNameColumn;
    @FXML
    private TableColumn<ReceiptItem, Integer> quantityColumn;
    @FXML
    private TableColumn<ReceiptItem, Integer> idColumn;
    @FXML
    private TableColumn<ReceiptItem, Double> unitPriceColumn;
    @FXML
    private TableColumn<ReceiptItem, Double> totalPriceColumn;

    private Connection connection;
    private int saleId;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    @FXML
    public void initialize() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void loadReceiptData() {
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
                int id = rs.getInt("id");
                receiptItems.add(new ReceiptItem(itemName, quantity, unitPrice, totalPrice, id));
            }
            receiptTable.setItems(receiptItems);
        } catch (SQLException e) {
            showAlert("Error", "Error loading receipt data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
