package me.group.cceproject.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderItem {

    private final SimpleStringProperty pizzaName;
    private final SimpleIntegerProperty pizzaQuantity;
    private final SimpleStringProperty drinkName;
    private final SimpleIntegerProperty drinkQuantity;
    private final SimpleStringProperty addonsName;
    private final SimpleIntegerProperty addonsQuantity;
    private final SimpleStringProperty totalPrice;
    private final SimpleStringProperty foodCode;


    public OrderItem(String pizzaName, int pizzaQuantity,String drinkName,int drinkQuantity,String addonsName,int addonsQuantity, String totalPrice, String foodCode) {
        this.pizzaName = new SimpleStringProperty(pizzaName);
        this.pizzaQuantity = new SimpleIntegerProperty(pizzaQuantity);
        this.drinkName = new SimpleStringProperty(drinkName);
        this.drinkQuantity = new SimpleIntegerProperty(drinkQuantity);
        this.addonsName = new SimpleStringProperty(addonsName);
        this.addonsQuantity = new SimpleIntegerProperty(addonsQuantity);
        this.totalPrice = new SimpleStringProperty(totalPrice);
        this.foodCode = new SimpleStringProperty(foodCode);
    }

    // Getters
    public String getPizzaName() { return pizzaName.get(); }
    public String getTotalPrice() { return totalPrice.get(); }
    public String getFoodCode() { return foodCode.get(); }
    public int getPizzaQuantity() { return pizzaQuantity.get(); }
public int getDrinkQuantity() { return drinkQuantity.get(); }
    public int getAddonsQuantity() { return addonsQuantity.get(); }
    public String getAddonsName() { return addonsName.get(); }
    public String getDrinkName() { return drinkName.get(); }
    // Setters
    public void setPizzaName(String pizzaName) {
        this.pizzaName.set(pizzaName);
    }

    public void setTotalPrice(String pizzaPrice) {
        this.totalPrice.set(pizzaPrice);
    }

    public void setFoodCode(String foodCode) {
        this.foodCode.set(foodCode);
    }

    public void setPizzaQuantity(int quantity) {
            this.pizzaQuantity.set(quantity);
    }
    public void setDrinkQuantity(int quantity) {
        this.drinkQuantity.set(quantity);
    }
    public void setAddonsQuantity(int quantity) {
        this.addonsQuantity.set(quantity);
    }
    public void setAddonsName(String addonsName) {
        this.addonsName.set(addonsName);
    }
    public void setDrinkName(String drinkName) {
        this.drinkName.set(drinkName);
    }
    // Property getters
    public SimpleStringProperty pizzaNameProperty() { return pizzaName; }
    public SimpleStringProperty pizzaPriceProperty() { return totalPrice; }
    public SimpleStringProperty foodCodeProperty() { return foodCode; }
    public SimpleIntegerProperty pizzaquantityProperty() { return pizzaQuantity; }
    public SimpleIntegerProperty drinkquantityProperty() { return drinkQuantity; }
    public SimpleIntegerProperty addonsquantityProperty() { return addonsQuantity; }
    public SimpleStringProperty addonsNameProperty() { return addonsName; }
    public SimpleStringProperty drinkNameProperty() { return drinkName; }
}