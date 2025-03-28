package nasar.mustafa.warehub.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import nasar.mustafa.warehub.tableViewModels.ItemPriceHistoryRow;
import org.controlsfx.control.SearchableComboBox;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javafx.event.ActionEvent;

import java.io.File;
import java.util.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ItemPriceHistoryController {
    protected Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        populateItemsCombo();
    }

    @FXML
    protected SearchableComboBox<String> itemsCombo;

    @FXML
    protected TableView<ItemPriceHistoryRow> tablePriceHistory;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Integer> idColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, String> nameColumn;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Double> priceBuy;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, Double> priceSell;

    @FXML
    protected TableColumn<ItemPriceHistoryRow, String> dateColumn;

    @FXML
    protected DatePicker fromDatePicker;

    @FXML
    protected DatePicker toDatePicker;

    @FXML
    protected LineChart<String, Number> lineChart;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceBuy.setCellValueFactory(new PropertyValueFactory<>("priceBuy"));
        priceSell.setCellValueFactory(new PropertyValueFactory<>("priceSell"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    protected void populateItemsCombo() {
        try {
            ResultSet rs = connection.prepareStatement("SELECT name FROM items").executeQuery();
            while (rs.next()) {
                itemsCombo.getItems().add(rs.getString("name"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching items from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
    }
    private void generatePDFReport(File file) throws JRException {
        JasperReport jasperReport = JasperCompileManager.compileReport(
                getClass().getResourceAsStream("/nasar/mustafa/warehub/Jasper/ItemPriceHistory.jrxml"));

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (ItemPriceHistoryRow item : tablePriceHistory.getItems()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("name", item.getName());
            map.put("سعر الشراء", item.getPriceBuy());
            map.put("سعر بيع", item.getPriceSell());
            map.put("date", item.getDate());
            dataList.add(map);
        }
        JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Price History for " + itemsCombo.getValue());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());
    }

    @FXML
    private void onExportPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tablePriceHistory.getScene().getWindow());

        if (file != null) {
            try {
                generatePDFReport(file);
                showAlert("Success", "PDF report generated successfully.", Alert.AlertType.INFORMATION);
            } catch (JRException e) {
                showAlert("Error", "Failed to generate PDF report: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void onItemSelect() {
        String selectedItem = itemsCombo.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        if (fromDate == null || toDate == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select both from and to dates.", ButtonType.CLOSE);
            alert.showAndWait();
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fromDateString = fromDate.format(formatter);
        String toDateString = toDate.format(formatter);

        try {
            ResultSet rs = connection.prepareStatement("SELECT item_prices.id, items.name, item_prices.price_buy, item_prices.price_sell, " +
                    "effective_date FROM item_prices INNER JOIN items ON item_prices.item_id = " +
                    "items.id WHERE items.name = '" + selectedItem + "' AND effective_date BETWEEN '" + fromDateString + "' AND '" + toDateString + "'").executeQuery();
            tablePriceHistory.getItems().clear();
            lineChart.getData().clear();

            XYChart.Series<String, Number> buySeries = new XYChart.Series<>();
            buySeries.setName("سعر الشراء");
            XYChart.Series<String, Number> sellSeries = new XYChart.Series<>();
            sellSeries.setName("سعر البيع");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double priceBuy = rs.getDouble("price_buy");
                double priceSell = rs.getDouble("price_sell");
                String date = rs.getString("effective_date");

                ItemPriceHistoryRow row = new ItemPriceHistoryRow(id, name, priceBuy, priceSell, date);
                tablePriceHistory.getItems().add(row);

                buySeries.getData().add(new XYChart.Data<>(date, priceBuy));
                sellSeries.getData().add(new XYChart.Data<>(date, priceSell));
            }

            lineChart.getData().addAll(buySeries, sellSeries);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error fetching item price history from the database", ButtonType.CLOSE);
            alert.showAndWait();
        }
        tablePriceHistory.refresh();
    }
}
