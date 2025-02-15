package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.control.SearchableComboBox;

import java.sql.Connection;
import java.sql.ResultSet;

public class ShowVehicleExpensesController {
    @FXML
    SearchableComboBox selectCarsCombo;

    @FXML
    TableView<CarExpensesRow> expensesTableView;

    @FXML
    TableColumn<CarExpensesRow, String> expenseName;

    @FXML
    TableColumn<CarExpensesRow, Double> expensePrice;

    @FXML
    TableColumn<CarExpensesRow, Integer> expenseId;

    @FXML
    TableColumn<CarExpensesRow, String> expenseDate;

    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected void initialize() {
        expenseName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expenseName"));
        expensePrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expensePrice"));
        expenseDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expenseDate"));
        expenseId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expenseId"));
    }

    protected void populateItemsCombo() {
        try {
            ResultSet rs = connection.prepareStatement("SELECT plate_number FROM vehicles").executeQuery();
            while (rs.next()) {
                selectCarsCombo.getItems().add(rs.getString("plate_number"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching vehicles from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }

    @FXML
    protected void showExpenses() {
        if (connection == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connection is not set!");
            alert.showAndWait();
            return;
        }

        try {
            ResultSet rs = connection.prepareStatement("SELECT * FROM vehicle_expenses WHERE vehicle_id = (SELECT id FROM vehicles WHERE plate_number = '" + selectCarsCombo.getValue() + "')").executeQuery();
            expensesTableView.getItems().clear();
            while (rs.next()) {
                expensesTableView.getItems().add(new CarExpensesRow(rs.getString("expense_name"), rs.getDouble("amount"), rs.getInt("id"), rs.getString("date")));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching expenses from the database: " + e.getMessage());
            alert.showAndWait();
        }
    }


    public static class CarExpensesRow{
        private String expenseName;
        private double expensePrice;
        private int expenseId;
        private String expenseDate;

        public CarExpensesRow(String expenseName, double expensePrice, int expenseId, String expenseDate) {
            this.expenseName = expenseName;
            this.expensePrice = expensePrice;
            this.expenseId = expenseId;
            this.expenseDate = expenseDate;
        }

        public String getExpenseName() {
            return expenseName;
        }

        public double getExpensePrice() {
            return expensePrice;
        }

        public String getExpenseDate() {
            return expenseDate;
        }

        public int getExpenseId() {
            return expenseId;
        }
    }
}

