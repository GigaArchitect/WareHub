package nasar.mustafa.warehub.controllers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

public class SalesAndPurchaseReportsController implements Initializable {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private TableView<ReportRecord> reportTable;
    @FXML private TableColumn<ReportRecord, LocalDate> dateColumn;
    @FXML private TableColumn<ReportRecord, Double> salesColumn;
    @FXML private TableColumn<ReportRecord, Double> purchasesColumn;
    @FXML private TableColumn<ReportRecord, Double> profitColumn;
    @FXML private BarChart<String, Number> reportChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label netProfitLabel;

    private Connection connection;
    private final ObservableList<ReportRecord> reportData = FXCollections.observableArrayList();

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadInitialData();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDatePickers();
        setupTableColumns();
        setupReportTypes();
        reportTable.setItems(reportData);
    }

    private void setupDatePickers() {
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        salesColumn.setCellValueFactory(new PropertyValueFactory<>("sales"));
        purchasesColumn.setCellValueFactory(new PropertyValueFactory<>("purchases"));
        profitColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));
    }

    private void setupReportTypes() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "يومي", "شهري", "سنوي"
        ));
        reportTypeComboBox.setValue("يومي");
    }

    private void loadInitialData() {
        if (connection == null) return;
        onGenerateReport();
    }

    @FXML
    private void onGenerateReport() {
        reportData.clear();
        reportChart.getData().clear();

        String groupBy = switch (reportTypeComboBox.getValue()) {
            case "شهري" -> "strftime('%Y-%m', ";
            case "سنوي" -> "strftime('%Y', ";
            default -> "DATE("; // يومي
        };

        String datePattern = switch (reportTypeComboBox.getValue()) {
            case "شهري" -> "%Y-%m";
            case "سنوي" -> "%Y";
            default -> "%Y-%m-%d"; // يومي
        };

        String salesQuery = String.format("""
        SELECT strftime('%s', s.sale_date) as date, COALESCE(SUM(si.total_price), 0) as total
        FROM sales s
        JOIN sales_items si ON s.id = si.sale_id
        WHERE DATE(s.sale_date) BETWEEN DATE(?) AND DATE(?)
        GROUP BY strftime('%s', s.sale_date)
        ORDER BY date
        """, datePattern, datePattern);

        String purchasesQuery = String.format("""
        SELECT strftime('%s', p.purchase_date) as date, COALESCE(SUM(pi.total_cost), 0) as total
        FROM purchases p
        JOIN purchase_items pi ON p.id = pi.purchase_id
        WHERE DATE(p.purchase_date) BETWEEN DATE(?) AND DATE(?)
        GROUP BY strftime('%s', p.purchase_date)
        ORDER BY date
        """, datePattern, datePattern);

        try {
            var salesSeries = new XYChart.Series<String, Number>();
            salesSeries.setName("المبيعات");

            var purchasesSeries = new XYChart.Series<String, Number>();
            purchasesSeries.setName("المشتريات");

            double totalSales = 0;
            double totalPurchases = 0;

            var salesData = new HashMap<String, Double>();
            var purchasesData = new HashMap<String, Double>();

            try (PreparedStatement stmt = connection.prepareStatement(salesQuery)) {
                stmt.setString(1, fromDatePicker.getValue().toString());
                stmt.setString(2, toDatePicker.getValue().toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String date = rs.getString("date");
                    double total = rs.getDouble("total");
                    salesData.put(date, total);
                    totalSales += total;
                }
            }

            try (PreparedStatement stmt = connection.prepareStatement(purchasesQuery)) {
                stmt.setString(1, fromDatePicker.getValue().toString());
                stmt.setString(2, toDatePicker.getValue().toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String date = rs.getString("date");
                    double total = rs.getDouble("total");
                    purchasesData.put(date, total);
                    totalPurchases += total;
                }
            }

            Set<String> allDates = new TreeSet<>();
            allDates.addAll(salesData.keySet());
            allDates.addAll(purchasesData.keySet());

            for (String dateStr : allDates) {
                double sales = salesData.getOrDefault(dateStr, 0.0);
                double purchases = purchasesData.getOrDefault(dateStr, 0.0);

                LocalDate date = switch (reportTypeComboBox.getValue()) {
                    case "شهري" -> LocalDate.parse(dateStr + "-01");
                    case "سنوي" -> LocalDate.parse(dateStr + "-01-01");
                    default -> LocalDate.parse(dateStr); // يومي
                };

                reportData.add(new ReportRecord(date, sales, purchases));
                salesSeries.getData().add(new XYChart.Data<>(dateStr, sales));
                purchasesSeries.getData().add(new XYChart.Data<>(dateStr, purchases));
            }

            reportChart.getData().addAll(salesSeries, purchasesSeries);

            double netProfit = totalSales - totalPurchases;
            totalSalesLabel.setText(String.format("%,.2f", totalSales));
            totalPurchasesLabel.setText(String.format("%,.2f", totalPurchases));
            netProfitLabel.setText(String.format("%,.2f", netProfit));

        } catch (SQLException e) {
            showAlert("خطأ", "فشل في تحميل البيانات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onExportReport() {
        showAlert("تنبيه", "سيتم تنفيذ التصدير قريباً", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class ReportRecord {
        private final LocalDate date;
        private final double sales;
        private final double purchases;
        private final double profit;

        public ReportRecord(LocalDate date, double sales, double purchases) {
            this.date = date;
            this.sales = sales;
            this.purchases = purchases;
            this.profit = sales - purchases;
        }

        public LocalDate getDate() { return date; }
        public double getSales() { return sales; }
        public double getPurchases() { return purchases; }
        public double getProfit() { return profit; }
    }
}
