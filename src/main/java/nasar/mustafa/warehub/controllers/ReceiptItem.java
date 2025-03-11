package nasar.mustafa.warehub.controllers;

public class ReceiptItem {
    private String itemName;
    private int quantity;
    private double price;
    private double total;
    private int serial;

    public ReceiptItem(String itemName, int quantity, double price, int serial) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.total = price * quantity;
        this.serial = serial;
    }

    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotal() { return total; }
    public int getSerial() { return serial; }
}
