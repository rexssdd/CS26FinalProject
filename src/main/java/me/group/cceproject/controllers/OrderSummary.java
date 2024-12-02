package me.group.cceproject.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrderSummary {

    private final StringProperty orderNumber;
    private final StringProperty orderTotal;
    private final StringProperty orderStatus;
    private final StringProperty pizzatotal;
    private final StringProperty orderType;  // New property for order type

    // Constructor
    public OrderSummary(String orderNumber) {
        this.orderNumber = new SimpleStringProperty(orderNumber);
        this.orderTotal = new SimpleStringProperty();
        this.orderStatus = new SimpleStringProperty();
        this.pizzatotal = new SimpleStringProperty();
        this.orderType = new SimpleStringProperty();  // Initialize the order type property
    }

    // Getter for orderNumber
    public String getOrderNumber() {
        return orderNumber.get();
    }

    // Getter for pizzatotal
    public String getPizzatotal() {
        return pizzatotal.get();
    }

    // Setter for orderNumber
    public void setOrderNumber(String orderNumber) {
        this.orderNumber.set(orderNumber);
    }

    // Property for orderNumber
    public StringProperty orderNumberProperty() {
        return orderNumber;
    }

    // Getter for orderTotal
    public String getOrderTotal() {
        return orderTotal.get();
    }

    // Setter for orderTotal
    public void setOrderTotal(String orderTotal) {
        this.orderTotal.set(orderTotal);
    }

    // Property for orderTotal
    public StringProperty orderTotalProperty() {
        return orderTotal;
    }

    // Getter for orderStatus
    public String getOrderStatus() {
        return orderStatus.get();
    }

    // Setter for orderStatus
    public void setOrderStatus(String orderStatus) {
        this.orderStatus.set(orderStatus);
    }

    // Property for orderStatus
    public StringProperty orderStatusProperty() {
        return orderStatus;
    }

    // Getter for orderType
    public String getOrderType() {
        return orderType.get();
    }

    // Setter for orderType
    public void setOrderType(String orderType) {
        this.orderType.set(orderType);
    }

    // Property for orderType
    public StringProperty orderTypeProperty() {
        return orderType;
    }

    // Setter for pizzatotal
    public void setPizzatotal(String pizzatotal) {
        this.pizzatotal.set(pizzatotal);
    }

    // Property for pizzatotal
    public StringProperty pizzatotalProperty() {
        return pizzatotal;
    }
}
