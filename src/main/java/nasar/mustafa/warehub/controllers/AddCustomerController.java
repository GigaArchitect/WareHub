package nasar.mustafa.warehub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.SQLException;

public class AddCustomerController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @FXML
    TextField name;

    @FXML
    TextField phone;

    @FXML
    TextField address;

    @FXML
    TextField email;

    @FXML
    protected void addCustomer(ActionEvent event) throws SQLException {
        String customerName = name.getText();
        String cutomerPhone = phone.getText();
        String customerAddress = address.getText();
        String customerEmail = email.getText();

        if (customerName.isEmpty() || customerAddress.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Customer Must Hava a name and an address");
            alert.showAndWait();
            return;
        }

        try {
            connection.setAutoCommit(false);
            connection.prepareStatement("INSERT INTO customers (name, phone, address, email) VALUES ('"
                    + customerName + "', '" + cutomerPhone + "', '" + customerAddress +
                    "', '" + customerEmail + "')").executeUpdate();
            connection.commit();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Cutomer Added Successfully");
            alert.showAndWait();
            name.clear();
            phone.clear();
            email.clear();
            address.clear();

        } catch (SQLException e) {
            connection.rollback();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Adding Customer: " + e.getMessage());
            alert.showAndWait();
            throw new RuntimeException(e);
        }

    }
}
