package me.group.cceproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartController {
    @FXML
    private ScrollPane cartScrollPane;

    @FXML
    private Text totalPriceText;

    @FXML
    private VBox cartItemsContainer;

    @FXML
    public void initialize() {
        cartItemsContainer = new VBox(15);
        cartItemsContainer.setPadding(new Insets(20));
        cartItemsContainer.setStyle("-fx-background-color: white;");
        cartScrollPane.setContent(cartItemsContainer);
        cartScrollPane.setFitToWidth(true);

        loadCartItems();
    }

    private void loadCartItems() {
        if (OrderMenuController.staticOrderItems != null) {
            cartItemsContainer.getChildren().clear();
            double totalPrice = 0.0;

            for (OrderItem item : OrderMenuController.staticOrderItems) {
                HBox itemContainer = createCartItemContainer(item);
                cartItemsContainer.getChildren().add(itemContainer);

                String priceStr = item.getTotalPrice().replaceAll("[^\\d.]", "");
                totalPrice += Double.parseDouble(priceStr);
            }

            totalPriceText.setText(String.format("₱ %.2f", totalPrice));
        }
    }

    private HBox createCartItemContainer(OrderItem item) {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-border-color: #E5E5E5; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");

        // Food Image
        ImageView foodImage = new ImageView();
        try {
            String imagePath = "/me/group/cceproject/images/" + item.getProductId() + ".png";  // Changed from item.getOrderNumber() to item.getProductId()
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            foodImage.setImage(image);
            foodImage.setFitWidth(80);
            foodImage.setFitHeight(80);
            foodImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        // Food name
        Text nameText = new Text(item.getPizzaName());
        nameText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Center section with name and quantity controls
        VBox centerSection = new VBox(10);
        centerSection.setAlignment(Pos.CENTER_LEFT);

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER_LEFT);

        Text quantityLabel = new Text("Quantity:");
        quantityLabel.setStyle("-fx-font-size: 14px;");

        HBox controlsBox = new HBox(5);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        Button decreaseBtn = new Button("-");
        Text quantityText = new Text(String.valueOf(item.getPizzaQuantity()));
        Button increaseBtn = new Button("+");

        // Style the quantity controls
        String buttonStyle = "-fx-background-color: white; -fx-border-color: #DB383D; " +
                "-fx-text-fill: #DB383D; -fx-border-radius: 5; " +
                "-fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;";
        decreaseBtn.setStyle(buttonStyle);
        increaseBtn.setStyle(buttonStyle);
        quantityText.setStyle("-fx-font-size: 14px;");

        // Add controls to the controls box
        controlsBox.getChildren().addAll(decreaseBtn, quantityText, increaseBtn);

        // Add label and controls to quantity box
        quantityBox.getChildren().addAll(quantityLabel, controlsBox);

        centerSection.getChildren().addAll(nameText, quantityBox);

        // Price
        Text priceText = new Text(item.getTotalPrice());
        priceText.setStyle("-fx-fill: #DB383D; -fx-font-size: 16px;");

        // Remove button
        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-background-color: red; -fx-text-fill: #FFFFFF; " +
                "-fx-border-color: #DB383D; -fx-border-radius: 5; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");

        // Add spacing between price and remove button
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add all components to container
        container.getChildren().addAll(foodImage, centerSection, spacer, priceText, removeBtn);

        // Event handlers
        decreaseBtn.setOnAction(e -> updateQuantity(quantityText, -1, item));
        increaseBtn.setOnAction(e -> updateQuantity(quantityText, 1, item));
        removeBtn.setOnAction(e -> removeItem(container, item));

        return container;
    }

    private List<OrderItem> fetchOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();

        // JDBC connection setup (replace with your own credentials)
        String url = "jdbc:mysql://localhost:3306/yourdb"; // Your database URL
        String username = "root";  // Your database username
        String password = "password";  // Your database password

        // SQL query to get the order items, drinks, and addons for a specific order
        String query = "SELECT oi.product_id, oi.quantity, p.product_name, p.price, " +
                "d.drink_name, d.quantity AS drink_qty, " +
                "a.addon_name, a.quantity AS addon_qty " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "LEFT JOIN drinks d ON oi.order_item_id = d.order_item_id " +
                "LEFT JOIN addons a ON oi.order_item_id = a.order_item_id " +
                "WHERE oi.order_id = ?";

        try (Connection connection = DriverManager.getConnection('url', 'username', 'password');
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, orderId);  // Set the orderId parameter
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Create lists for addons and their quantities
                List<String> addonNames = new ArrayList<>();
                List<Integer> addonQuantities = new ArrayList<>();
                if (rs.getString("addon_name") != null) {
                    addonNames.add(rs.getString("addon_name"));
                    addonQuantities.add(rs.getInt("addon_qty"));
                }

                // Create the OrderItem from the result set
                OrderItem item = new OrderItem(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("drink_name"),
                        rs.getInt("drink_qty"),
                        addonNames,
                        addonQuantities
                );
                orderItems.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderItems;
    }

    private String getFoodCode(String pizzaName) {
        // Map meal names to their corresponding codes
        switch (pizzaName) {
            case "Hawaian Pizza":
                return "hawaian";
            case "Margherita Pizza":
                return "Special";
            case "Deluxe Pizza":
                return "Deluxe";
            // Add more cases as needed
            default:
                return "default-food"; // Default image
        }
    }

    private void updateQuantity(Text quantityText, int change, OrderItem item) {
        int currentQuantity = Integer.parseInt(quantityText.getText());
        int newQuantity = Math.max(1, currentQuantity + change);

        // Update both the display and the OrderItem
        quantityText.setText(String.valueOf(newQuantity));
        item.setPizzaQuantity(newQuantity);

        // Update total price
        updateTotalPrice();
    }

    private void removeItem(HBox container, OrderItem item) {
        cartItemsContainer.getChildren().remove(container);
        OrderMenuController.staticOrderItems.remove(item);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (OrderItem item : OrderMenuController.staticOrderItems) {
            String priceStr = item.getTotalPrice().replaceAll("[^\\d.]", "");
            double itemPrice = Double.parseDouble(priceStr);
            // Multiply by quantity
            total += itemPrice * item.getPizzaQuantity();
        }
        totalPriceText.setText(String.format("₱ %.2f", total));
    }

    @FXML
    private void backToOrderMenu(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/OrderMenu.fxml"));
            Parent orderMenuRoot = loader.load();

            OrderMenuController controller = loader.getController();
            controller.setOrderType(OrderMenuController.getOrderType());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene orderMenuScene = new Scene(orderMenuRoot);
            stage.setScene(orderMenuScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error returning to order menu: " + e.getMessage());
        }
    }

    @FXML
    private void proceedToCheckout(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/PayOrder.fxml"));
            Parent payOrderRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene payOrderScene = new Scene(payOrderRoot);
            stage.setScene(payOrderScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error returning to pay order menu: " + e.getMessage());
        }
    }
}