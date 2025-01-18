package nasar.mustafa.warehub.tableViewModels;

public class ItemPriceHistoryRow {
    private int id;
    private String priceType;
    private double price;
    private String date;

    public ItemPriceHistoryRow(int id, String priceType, double price, String date) {
        this.id = id;
        this.priceType = priceType;
        this.price = price;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getPriceType() {
        return priceType;
    }

    public double getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
