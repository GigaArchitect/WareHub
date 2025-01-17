package nasar.mustafa.warehub;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static nasar.mustafa.warehub.ApplicationEntry.convertArabicNumerals;

public class AddItemController {
    private Connection connection;

    public void setConnection(Connection connection){
        this.connection = connection;
    }

    @FXML
    TextField ItemName;

    @FXML
    TextField ItemPriceSell;

    @FXML
    TextField ItemPriceBuy;

    @FXML
    TextField Quantity;

    @FXML
    protected void addItem(ActionEvent event) {
        if (connection == null){
            System.out.println("Connection is not set !");
            return;
        }
        if (ItemName.getText() == null || ItemName.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "أسم الصنف فارغ أو غير صحيح",
                    ButtonType.CANCEL);
            alert.showAndWait();
            return;
        }
        if (ItemPriceBuy.getText() == null || ItemPriceBuy.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "تأكد من صحة الأسعار", ButtonType.CANCEL);
            alert.showAndWait();
            return;
        }
        if (ItemPriceSell.getText() == null || ItemPriceSell.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "تأكد من صحة الأسعار", ButtonType.CANCEL);
            alert.showAndWait();
            return;
        }
        if (Quantity.getText() == null || Quantity.getText().trim().isEmpty()){
            Quantity.setText("0");
        }
        try {
            connection.setAutoCommit(false);
            PreparedStatement command = connection.prepareStatement("INSERT INTO items(name, stock_quantity) " +
                    "VALUES(?, ?);");
            command.setString(1, ItemName.getText());
            command.setInt(2, Integer.parseInt(convertArabicNumerals(Quantity.getText())));
            command.executeUpdate();

            ResultSet generatedKeys = command.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("No ID obtained for the newly inserted item.");
            }
            int itemId = generatedKeys.getInt(1);

            try {
                double purchasePrice = Double.parseDouble(convertArabicNumerals(ItemPriceBuy.getText()));
                double sellingPrice = Double.parseDouble(convertArabicNumerals(ItemPriceSell.getText()));

                PreparedStatement purchasePriceCommand = connection.prepareStatement(
                        "INSERT INTO item_prices(item_id, price_type, price) VALUES(?, 'purchase', ?);"
                );
                purchasePriceCommand.setInt(1, itemId);
                purchasePriceCommand.setDouble(2, purchasePrice);
                purchasePriceCommand.executeUpdate();

                PreparedStatement sellingPriceCommand = connection.prepareStatement(
                        "INSERT INTO item_prices(item_id, price_type, price) VALUES(?, 'selling', ?);"
                );
                sellingPriceCommand.setInt(1, itemId);
                sellingPriceCommand.setDouble(2, sellingPrice);
                sellingPriceCommand.executeUpdate();

                connection.commit();
            } catch (NumberFormatException e) {
                connection.rollback();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid number format. Transaction rolled back.",
                        ButtonType.CLOSE);
                alert.showAndWait();
                return;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Transaction failed and was rolled back.",
                        ButtonType.CLOSE);
                alert.showAndWait();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Failed to rollback transaction: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw new RuntimeException("Failed to execute transaction: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to restore auto-commit state: " + ex.getMessage(), ex);
            }
        }
    }
}
