package nasar.mustafa.warehub.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class LoggedController {
    @FXML
    protected BorderPane rootPane;

    protected Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    private Connection connection;

    public void setConnection(Connection connection){
        this.connection = connection;
    }

    @FXML
    protected void onAboutClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/About.fxml"));
        Node aboutPane = loader.load();
        AboutController aboutController = loader.getController();
        aboutController.setMessage();
        rootPane.setCenter(aboutPane);
    }

    @FXML
    protected void onAddItemButtonClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddItem.fxml"));
        Node addPane = loader.load();
        AddItemController addController = loader.getController();
        addController.setConnection(connection);
        rootPane.setCenter(addPane);
    }

    @FXML
    protected void onItemPriceHistoryClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/ItemPriceHistory.fxml"));
        Node itemPriceHistoryPane = loader.load();
        ItemPriceHistoryController itemPriceHistoryController = loader.getController();
        itemPriceHistoryController.setConnection(connection);
        rootPane.setCenter(itemPriceHistoryPane);
    }

    @FXML
    protected void onItemDeleteClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/ItemDelete.fxml"));
        Node itemDeletePane = loader.load();
        ItemDeleteController itemDeleteController = loader.getController();
        itemDeleteController.setConnection(connection);
        rootPane.setCenter(itemDeletePane);
    }

    @FXML
    protected void onItemUpdateClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/ItemUpdate.fxml"));
        Node itemUpdatePane = loader.load();
        ItemUpdateController itemUpdateController = loader.getController();
        itemUpdateController.setConnection(connection);
        rootPane.setCenter(itemUpdatePane);
    }

    @FXML
    protected void onAddVehicleClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddVehicle.fxml"));
        Node addVehiclePane = loader.load();
        AddVehicleController addVehicleController = loader.getController();
        addVehicleController.setConnection(connection);
        rootPane.setCenter(addVehiclePane);
    }

    @FXML
    protected void onVehicleAddExpenseClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddVehicleExpense.fxml"));
        Node vehicleAddExpensePane = loader.load();
        AddVehicleExpenseController vehicleAddExpenseController = loader.getController();
        vehicleAddExpenseController.setConnection(connection);
        rootPane.setCenter(vehicleAddExpensePane);
    }

    @FXML
    protected void onVehicleShowExpensesClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/ShowVehicleExpenses.fxml"));
        Node showVehicleExpensesPane = loader.load();
        ShowVehicleExpensesController showVehicleExpensesController = loader.getController();
        showVehicleExpensesController.setConnection(connection);
        rootPane.setCenter(showVehicleExpensesPane);
    }

    @FXML
    protected void onPaymentNoReceiptClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/PaymentNoReceipt.fxml"));
        Node showPaymentNoReceiptPane = loader.load();
        PaymentNoReceiptController paymentNoReceiptController = loader.getController();
        paymentNoReceiptController.setConnection(connection);
        rootPane.setCenter(showPaymentNoReceiptPane);
    }

    @FXML
    protected void onAddSupplierClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddSupplier.fxml"));
        Node showAddSupplierPane = loader.load();
        AddSupplier addSupplier = loader.getController();
        addSupplier.setConnection(connection);
        rootPane.setCenter(showAddSupplierPane);
    }

    @FXML
    protected void onAddCustomerClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddCustomer.fxml"));
        Node showAddCustomerPane = loader.load();
        AddCustomerController addCustomer = loader.getController();
        addCustomer.setConnection(connection);
        rootPane.setCenter(showAddCustomerPane);
    }

    @FXML
    protected void onDeleteCustomerClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/DeleteCustomer.fxml"));
        Node showDeleteCustomerPane = loader.load();
        DeleteCustomerController deleteCustomer = loader.getController();
        deleteCustomer.setConnection(connection);
        rootPane.setCenter(showDeleteCustomerPane);
    }

    @FXML
    protected void onUpdateCustomerClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/UpdateCustomer.fxml"));
        Node showUpdateCustomerPane = loader.load();
        UpdateCustomerController updateCustomer = loader.getController();
        updateCustomer.setConnection(connection);
        rootPane.setCenter(showUpdateCustomerPane);
    }

    @FXML
    protected void onAddCustomerReceiptClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/AddCustomerReceipt.fxml"));
        Node showAddCustomerReceiptPane = loader.load();
        AddCustomerReceiptController addCustomerReceipt = loader.getController();
        addCustomerReceipt.setConnection(connection);
        rootPane.setCenter(showAddCustomerReceiptPane);
    }

    @FXML
    protected void onCustomerAccountStatementClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/CustomerAccountStatement.fxml"));
        Node showCustomerAccountStatementPane = loader.load();
        CustomerAccountStatementController customerAccountStatement = loader.getController();
        customerAccountStatement.setConnection(connection);
        rootPane.setCenter(showCustomerAccountStatementPane);
    }

    @FXML
    protected void onSalesReportsClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/SalesReports.fxml"));
        Node salesReportsPane = loader.load();
        SalesReportsController salesReportsController = loader.getController();
        salesReportsController.setConnection(connection);
        rootPane.setCenter(salesReportsPane);
    }

    @FXML void onPurchaseOperationClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/nasar/mustafa/warehub/PurchaseOperation.fxml"));
        Node purchaseOperationPane = loader.load();
        PurchaseOperationController purchaseOperationController = loader.getController();
        purchaseOperationController.setConnection(connection);
        rootPane.setCenter(purchaseOperationPane);
    }
}
