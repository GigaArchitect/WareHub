package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentNoReceiptController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    SearchableComboBox<String> itemsCombo;

    @FXML
    TextField payment;

    @FXML
    Label to_pay;

    @FXML
    public void initialize() {
        itemsCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                updateDebtLabel(newValue);
            }
        });
    }

    private void updateDebtLabel(String customerName) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT SUM(total_price) - COALESCE((SELECT SUM(amount) FROM payments WHERE customer_id = c.id), 0) AS debt " +
                            "FROM sales s " +
                            "INNER JOIN customers c ON s.customer_id = c.id " +
                            "WHERE c.name = ?"
            );
            stmt.setString(1, customerName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double debt = rs.getDouble("debt");
                to_pay.setText(String.valueOf(debt));
            } else {
                to_pay.setText("0.0");
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching customer debt", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    public void populateItemsCombo() {
        try {
            ResultSet rs = connection.prepareStatement("SELECT name FROM customers").executeQuery();
            while (rs.next()) {
                itemsCombo.getItems().add(rs.getString("name"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching items from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    @FXML
    public void onPayment() {
        try{
            connection.setAutoCommit(false);
            Double paymentValue = Double.parseDouble(payment.getText());
            String customer = itemsCombo.getValue();
            ResultSet customerId = connection.prepareStatement("SELECT id FROM customers WHERE name = '" + customer + "'").executeQuery();
            PreparedStatement st  = connection.prepareStatement("INSERT INTO payments (customer_id, amount) VALUES (?, ?)");

            customerId.next();

            st.setString(1, customerId.getString("id"));
            st.setDouble(2, paymentValue);

            st.executeUpdate();

            connection.commit();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Payment Inserted", ButtonType.OK);
            alert.showAndWait();
            updateDebtLabel(customer);

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error in Rolling back Insert payment from the database\n" + e.getMessage(), ButtonType.CLOSE);
                alert.showAndWait();
            }
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Inserting payment to the database\n" + e.getMessage(), ButtonType.CLOSE);
            alert.showAndWait();
        }
    }
}
