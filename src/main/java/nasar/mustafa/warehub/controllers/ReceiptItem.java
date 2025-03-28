package nasar.mustafa.warehub.controllers;

public class ReceiptItem {
    private String itemName;
    private int quantity;
    private double price;
    private double totalPrice;
    private int serial;

    public ReceiptItem(String itemName, int quantity, double price, double totalPrice, int serial) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.serial = serial;
    }

    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotalPrice() { return totalPrice; }
    public int getSerial() { return serial; }
    public double getTotal() { return price * quantity; }
}
