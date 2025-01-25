package nasar.mustafa.warehub.tableViewModels;

public class ItemPriceHistoryRow {
    private int id;
    private String name;
    private double priceBuy;
    private double priceSell;
    private String date;

    public ItemPriceHistoryRow(int id, String name, double priceBuy, double priceSell, String date) {
        this.id = id;
        this.name = name;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPriceBuy() {
        return priceBuy;
    }

    public double getPriceSell() {
        return priceSell;
    }

    public String getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriceBuy(double priceBuy) {
        this.priceBuy = priceBuy;
    }

    public void setPriceSell(double priceSell) {
        this.priceSell = priceSell;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
