package me.group.cceproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EndController {

    private static final String ORDER_FILE = "orders.txt";
    private static String paymentType;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pizzaordering";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML
    Text Reminder;

    @FXML
    private Text OrderNumber;

    @FXML
    public void initialize() {
        if (OrderNumber != null) {
            updateOrderNumber();
        } else {
            System.err.println("OrderNumber Text is null. Check your FXML file.");
        }
        updateStockFromOrderItems();

    }

    private void updateOrderNumber() {
        int orderNumber = getNextOrderNumber();
        OrderNumber.setText(String.format("%04d", orderNumber));
    }

    private int getNextOrderNumber() {
        try {
            if (!Files.exists(Paths.get(ORDER_FILE))) {
                Files.createFile(Paths.get(ORDER_FILE));
                return 1;
            }

            List<String> lines = Files.readAllLines(Paths.get(ORDER_FILE));
            if (lines.isEmpty()) {
                return 1;
            }

            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i).trim();
                if (line.matches("\\d{4}: Order Items")) {
                    return Integer.parseInt(line.split(":")[0]) + 1;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }
    private void updateStockFromOrderItems() {
        try {
            // Establish connection (modify the connection details)
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

            // Query to fetch all order items (get product_id and quantity from order_items)
            String query = "SELECT product_id, quantity FROM order_items";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            // Process each order item
            while (rs.next()) {
                // Get the product_id (String) and quantity from order_items
                String productId = rs.getString("product_id");
                int orderedQuantity = rs.getInt("quantity");

                // Step 1: Get the current stock for the product
                String getStockQuery = "SELECT stock FROM products WHERE product_id = ?";
                PreparedStatement getStockStmt = connection.prepareStatement(getStockQuery);
                getStockStmt.setString(1, productId); // Set the product_id as a String
                ResultSet stockRs = getStockStmt.executeQuery();

                // Step 2: Calculate the new stock
                int currentStock = 0;
                if (stockRs.next()) {
                    currentStock = stockRs.getInt("stock");
                } else {
                    System.out.println("No product found with Product ID: " + productId);
                    continue;  // Skip to the next iteration if no product is found
                }

                // Calculate the new stock after subtracting the ordered quantity
                int newStock = currentStock - orderedQuantity;

                // Step 3: Update the stock in the products table
                String updateQuery = "UPDATE products SET stock = ? WHERE product_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setInt(1, newStock);  // Set the new stock value
                updateStmt.setString(2, productId);  // Set the product_id as a String

                // Execute the update
                int rowsUpdated = updateStmt.executeUpdate();

                // Check if the update was successful
                if (rowsUpdated > 0) {
                    System.out.println("Stock updated successfully for Product ID: " + productId);
                    System.out.println("New stock: " + newStock);
                } else {
                    System.out.println("Failed to update stock for Product ID: " + productId);
                }
            }

            connection.close(); // Close connection after use
        } catch (SQLException e) {
            e.printStackTrace(); // Log any SQL exceptions
        }
    }


    private void printAllItemsa() {
        try {
            // Establish connection (modify the connection details)
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

            // Query to fetch all order items
            String query = "SELECT * FROM order_items";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            // Print the result of the query
            System.out.println("Order Items List:");
            while (rs.next()) {
                // Assuming the table has columns: id, order_id, product_id, quantity, drinks, drinksquantity, addons, AddonsQuantity, price
                int id = rs.getInt("id");
                int orderId = rs.getInt("order_id");
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                String drinks = rs.getString("drinks");
                int drinksQuantity = rs.getInt("drinksquantity");
                String addons = rs.getString("addons");
                int addonsQuantity = rs.getInt("AddonsQuantity");
                double price = rs.getDouble("price");

                // Print each order item's details
                System.out.println("Order Item ID: " + id);
                System.out.println("Order ID: " + orderId);
                System.out.println("Product ID: " + productId);
                System.out.println("Quantity: " + quantity);
                System.out.println("Drinks: " + drinks);
                System.out.println("Drinks Quantity: " + drinksQuantity);
                System.out.println("Addons: " + addons);
                System.out.println("Addons Quantity: " + addonsQuantity);
                System.out.println("Price: " + price);
                System.out.println("-------------");
            }

            connection.close(); // Close connection after use
        } catch (SQLException e) {
            e.printStackTrace(); // Log any SQL exceptions
        }
    }


    private int getProductIDByName(String productName) {
        String query = "SELECT id FROM products WHERE product_name = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void saveOrder(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            System.err.println("No order items to save.");
            return;
        }

        int orderNumber = Integer.parseInt(OrderNumber.getText());
        String orderType = OrderMenuController.getOrderType();
        double totalPrice = orderItems.stream()
                .mapToDouble(item -> {
                    try {
                        return Double.parseDouble(item.getTotalPrice().replaceAll("[^\\d.]", "")) * item.getPizzaQuantity();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return 0.0;
                    }
                })
                .sum();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_FILE, true))) {
            writer.write(String.format("%04d: Order Items", orderNumber));
            writer.newLine();
            writer.write("  Order Type: " + orderType);
            writer.newLine();
            for (OrderItem item : orderItems) {
                writer.write("  Bundle Code: " + item.getFoodCode());
                writer.newLine();
                writer.write("  Pizza Name: " + item.getPizzaName());
                writer.newLine();
                writer.write("  Quantity: " + item.getPizzaQuantity());
                writer.newLine();
                writer.write("  Drink Name: " + item.getDrinkName());
                writer.newLine();
                writer.write("  Quantity: " + item.getDrinkQuantity());
                writer.newLine();
                writer.write("  Add-Ons: " + item.getAddonsName());
                writer.newLine();
                writer.write("Add-Ons Quantity: " + item.getAddonsQuantity());

                writer.write("  Price: " + item.getTotalPrice());
                writer.newLine();
                System.out.println(""+ item.getPizzaName());
                System.out.println(""+ item.getPizzaQuantity());
                System.out.println(""+ item.getDrinkName());
                System.out.println(""+ item.getDrinkQuantity());
                System.out.println(""+ item.getAddonsName());
                System.out.println(""+ item.getAddonsQuantity());
                System.out.println(""+ item.getTotalPrice());
            }
            writer.write("  Total Price: " + String.format("%.2f", totalPrice));
            writer.newLine();
            writer.write("  Status: Pending");
            writer.newLine();
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Insert into orders table with status set to 'Pending'
            String orderQuery = "INSERT INTO orders (order_number, order_type, status, totalprice) VALUES (?, ?, 'Pending', ?)";
            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                // Set the parameters for the query
                orderStmt.setString(1, String.format("%04d", orderNumber)); // Formatted order number
                orderStmt.setString(2, orderType); // Order type (e.g., online, pickup)
                orderStmt.setDouble(3, totalPrice); // Total price

                // Execute the update
                orderStmt.executeUpdate();

                // Retrieve the generated keys (order ID)
                ResultSet rs = orderStmt.getGeneratedKeys();
                int orderId = 0;
                if (rs.next()) {
                    orderId = rs.getInt(1); // Get the generated order ID
                }

        // Insert into order_items table
                String itemsQuery = "INSERT INTO order_items (order_id, product_id, quantity, drinks, drinksquantity, addons, AddonsQuantity, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement itemsStmt = connection.prepareStatement(itemsQuery)) {
                    for (OrderItem item : orderItems) {
                        int productId = getProductIDByName(item.getPizzaName());
                        if (productId == -1) {
                            System.err.println("Product not found: " + item.getPizzaName());
                            continue; // Skip invalid products
                        }

                        itemsStmt.setInt(1, orderId);
                        itemsStmt.setInt(2, productId);
                        itemsStmt.setInt(3, item.getPizzaQuantity());
                        itemsStmt.setString(4, item.getDrinkName());
                        itemsStmt.setInt(5, item.getDrinkQuantity());
                        itemsStmt.setString(6, item.getAddonsName());
                        itemsStmt.setInt(7, item.getAddonsQuantity());

                        try {
                            itemsStmt.setDouble(8, Double.parseDouble(item.getTotalPrice().replaceAll("[^\\d.]", "")));
                        } catch (NumberFormatException e) {
                            itemsStmt.setDouble(8, 0.0);
                            e.printStackTrace();
                        }

                        itemsStmt.addBatch();

                        // Update product stock after order insertion
                        String stockQuery = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
                        try (PreparedStatement stockStmt = connection.prepareStatement(stockQuery)) {
                            stockStmt.setInt(1, item.getPizzaQuantity());  // Decrease stock by ordered quantity
                            stockStmt.setInt(2, productId);
                            stockStmt.setInt(3, item.getPizzaQuantity());  // Ensure stock doesn't go negative

                            int updatedRows = stockStmt.executeUpdate();
                            if (updatedRows == 0) {
                                System.err.println("Failed to update stock for product: " + item.getPizzaName() + ". Insufficient stock.");
                            }
                        }
                    }
                    int[] results = itemsStmt.executeBatch();
                    System.out.println("Order items inserted: " + results.length);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
        updatePaymentTypeLabel();
    }

    private void updatePaymentTypeLabel() {
        if ("PayCounter".equals(paymentType)) {
            Reminder.setText("Pay for your order\nat the cashier");
        } else {
            Reminder.setText("Pay using your card\nat the cashier");
        }
    }

    private List<OrderItem> getCurrentOrderItems() {
        return OrderMenuController.staticOrderItems;
    }

    @FXML
    private void CreateOrderClicked(MouseEvent event) {
        List<OrderItem> orderItems = getCurrentOrderItems();
        saveOrder(orderItems);
        OrderMenuController.staticOrderItems.clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/StartMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
