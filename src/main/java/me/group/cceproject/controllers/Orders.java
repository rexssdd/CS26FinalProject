package me.group.cceproject.controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Orders {
    private final StringProperty drinkName = new SimpleStringProperty();
    private final IntegerProperty drinkquantity = new SimpleIntegerProperty();
    private final StringProperty addonsName = new SimpleStringProperty();

    public int getDrinkquantity() {
        return drinkquantity.get();
    }

    public void setDrinkquantity(int quantity) {
        drinkquantity.set(quantity);
    }

    public IntegerProperty drinkquantityProperty() {
        return drinkquantity;
    }

    public String getAddonsName() {
        return addonsName.get();
    }

    public void setAddonsName(String name) {
        addonsName.set(name);
    }

    public StringProperty addonsNameProperty() {
        return addonsName;
    }
}
