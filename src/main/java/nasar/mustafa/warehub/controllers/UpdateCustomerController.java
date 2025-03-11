package nasar.mustafa.warehub.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.ibatis.jdbc.Null;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.ResultSet;

public class UpdateCustomerController {
    protected Connection connection;

    public void setConnection (Connection connection) {
        this.connection = connection;
        populateComboBox();
    }

    @FXML
    SearchableComboBox<String> combo;

    @FXML
    TextField name;

    @FXML
    TextField address;

    @FXML
    TextField phone;

    @FXML
    TextField email;


    public void populateComboBox() {
        try {
            ResultSet st = connection.createStatement().executeQuery("SELECT name FROM customers");
            while (st.next()){
                combo.getItems().add(st.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error populating combo box: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void populateTextFields() {
        String customerName = combo.getValue();
        try {
            ResultSet st = connection.createStatement().executeQuery("SELECT * FROM customers WHERE name = '" + customerName + "'");
            st.next();
            name.setText(st.getString("name"));
            address.setText(st.getString("address"));
            phone.setText(st.getString("phone"));
            email.setText(st.getString("email"));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error populating text fields: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void updateCustomer() {
        String customerName = combo.getValue();
        String newName = name.getText();
        String newAddress = address.getText();
        String newPhone = phone.getText();
        String newEmail = email.getText();

        try {
            connection.setAutoCommit(false);
            connection.createStatement().executeUpdate("UPDATE customers SET name = '" + newName + "', address = '" + newAddress + "', phone = '" + newPhone + "', email = '" + newEmail + "' WHERE name = '" + customerName + "'");
            connection.commit();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Updated Customer Successfully");
            alert.showAndWait();
            ObservableList newCombo = combo.getItems();
            newCombo.remove(customerName);
            newCombo.add(newName);
            combo.setItems(newCombo);
            SingleSelectionModel <String> selectionModel = combo.getSelectionModel();
            selectionModel.select(newName);
            combo.setSelectionModel(selectionModel);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Updating Customer: " + e.getMessage());
            alert.showAndWait();
        }
    }

}
