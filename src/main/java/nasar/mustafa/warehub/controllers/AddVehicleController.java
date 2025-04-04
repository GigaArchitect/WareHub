package nasar.mustafa.warehub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddVehicleController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @FXML
    private TextField plateNumberField;

    @FXML
    private TextField ownerNameField;

    @FXML
    private TextField vehicleTypeField;

    @FXML
    protected void addVehicle(ActionEvent event) {
        if (connection == null) {
            System.out.println("Connection is not set!");
            return;
        }


        try {
            System.out.println(connection.getAutoCommit());
            if (plateNumberField.getText() == null || plateNumberField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Plate number is empty or invalid");
                return;
            }

            if (ownerNameField.getText() == null || ownerNameField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Owner name is empty or invalid");
                return;
            }

            if (vehicleTypeField.getText() == null || vehicleTypeField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Vehicle type is empty or invalid");
                return;
            }

            // Execute the INSERT statement
            PreparedStatement command = connection.prepareStatement(
                    "INSERT INTO vehicles (plate_number, owner_name, vehicle_type) VALUES (?, ?, ?);"
            );
            command.setString(1, plateNumberField.getText().trim());
            command.setString(2, ownerNameField.getText().trim());
            command.setString(3, vehicleTypeField.getText().trim());
            command.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Vehicle added successfully");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error adding vehicle to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }
}
