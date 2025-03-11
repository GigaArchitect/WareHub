package nasar.mustafa.warehub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.ResultSet;

public class DeleteCustomerController {
    protected Connection connection;

    public void setConnection (Connection connection){
        this.connection = connection;
        populateComboBox();
    }

    @FXML
    SearchableComboBox<String> combo;

    public void populateComboBox(){
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

    public void deleteCustomer(ActionEvent event){
        String customerName = combo.getValue();
        try {
            connection.setAutoCommit(false);
            connection.createStatement().executeUpdate("DELETE FROM customers WHERE name = '" + customerName + "'");
            connection.commit();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deleted Customer Successfully");
            alert.showAndWait();
            combo.getItems().remove(customerName);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error Deleting Customer: " + e.getMessage());
            alert.showAndWait();
        }
    }

}
