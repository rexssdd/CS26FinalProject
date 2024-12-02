package me.group.cceproject.controllers;

public class ProdData {
    private String productId;
    private String productName;
    private double price;
    private int stock;
    private String image;

    public ProdData(String productId, String productName, double price, int stock, String image) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
        this.image = image;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getImage() {
        return image;
    }
}