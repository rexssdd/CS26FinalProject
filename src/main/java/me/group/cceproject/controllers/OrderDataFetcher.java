package me.group.cceproject.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.group.cceproject.controllers.OrderSummary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderDataFetcher {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pizzaordering";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public ObservableList<OrderSummary> fetchOrdersFromDatabase() {
        ObservableList<OrderSummary> orderList = FXCollections.observableArrayList();

        // Update the SQL query to fetch the order_type as well
        String query = "SELECT order_number, order_type, totalprice, status FROM orders";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Iterate over the ResultSet to get the order details
            while (resultSet.next()) {
                String orderNumber = resultSet.getString("order_number");
                String totalPrice = resultSet.getString("totalprice");
                String status = resultSet.getString("status");
                String orderType = resultSet.getString("order_type");  // Fetch order_type

                // Create the OrderSummary object and set the values
                OrderSummary orderSummary = new OrderSummary(orderNumber);
                orderSummary.setOrderTotal(totalPrice);
                orderSummary.setOrderStatus(status);
                orderSummary.setOrderType(orderType);  // Set the fetched order type
                orderList.add(orderSummary);

                System.out.println(orderNumber);
                System.out.println(totalPrice);
                System.out.println(status);
                System.out.println(orderType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderList;
    }
}
