package nasar.mustafa.warehub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddSupplier {
    protected Connection connection;

    public void setConnection(Connection connection){
        this.connection = connection;
    }

    @FXML
    TextField name;

    @FXML
    TextField phone;

    @FXML
    TextField address;

    @FXML
    protected void AddSupplier(ActionEvent event){
        String SupplierName = name.getText();
        String SupplierPhone = phone.getText();
        String SupplierAddress = address.getText();

        try {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO suppliers (name, phone, address) VALUES (?, ?, ?)");
            statement.setString(1, SupplierName);
            statement.setString(2,SupplierPhone);
            statement.setString(3, SupplierAddress);
            statement.executeUpdate();
            connection.commit();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Added Supplier Successfully");
            alert.showAndWait();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Adding Supplier: " + e.getMessage());
            alert.showAndWait();
        }

    }

}
