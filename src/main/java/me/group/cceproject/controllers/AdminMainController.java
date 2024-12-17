package me.group.cceproject.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;


import static me.group.cceproject.controllers.OrderMenuController.staticOrderItems;

public class AdminMainController {

    @FXML
    private TextField inputOrderNumber;

    @FXML
    private TextField OrderNumberInput;

    @FXML
    private TextField AmountTextField;

    @FXML
    private Text TotalText;



    @FXML
    private Text orderTotalText;

    @FXML
    private Text ChangeText;

    @FXML
    private ComboBox<String> orderStatusComboBox;

    @FXML
    private TabPane MainTab;

    @FXML
    private TabPane InputTab;


    @FXML
    private Tab OrderQueueTab;

    @FXML
    private Tab OrdersTab;

    @FXML
    private Tab OrderDetails;

    @FXML
    private Tab OrderInput;

    @FXML
    private TextField QuantityField;
    @FXML
    private TableView<OrderSummary> orderTableView;
    @FXML
    private TableColumn<OrderSummary, String> orderNumberColumn;
    @FXML
    private TableColumn<OrderSummary, String> orderTotalColumn;
    @FXML
    private TableColumn<OrderSummary, String> orderStatusColumn;
    @FXML
    private TableColumn<OrderSummary, String> OrdersTypeColumn;
    @FXML
    private TableView<OrderItem> OrdersTable;
    @FXML
    private TableColumn<OrderItem, String> OrderNum;
    @FXML
    private TableColumn<OrderItem, String> PizzaName;
    @FXML
    private TableColumn<OrderItem, String> TotalPrice;
    @FXML
    private TableColumn<OrderItem, Integer> PizzaQuantity;
    @FXML
    private TableColumn<OrderItem, String> DrinksName;
    @FXML
    private TableColumn<OrderItem, Integer> DrinksQuantity;
    @FXML
    private TableColumn<OrderItem, String> AddOns;
    @FXML
    private TableColumn<OrderItem, Integer> AddOnsQty;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pizzaordering"; // Replace with your database URL
    private static final String DB_USER = "root"; // Replace with your database username
    private static final String DB_PASSWORD = ""; // Replace with your database password

    private static final String ORDER_FILE = "orders.txt";

    // Queue for order processing
    private Queue<OrderSummary> orderQueue = new LinkedList<>();

    // Map to store stacks of OrderItems for each order
    private Map<String, Stack<OrderItem>> orderStacks = new HashMap<>();

    @FXML
    public void initialize() {
        loadOrdersData();
        // Set up the order summary columns
//        orderNumberColumn.setCellValueFactory(cellData -> cellData.getValue().orderNumberProperty());
//        orderTotalColumn.setCellValueFactory(cellData -> cellData.getValue().orderTotalProperty());
//        orderStatusColumn.setCellValueFactory(cellData -> cellData.getValue().orderStatusProperty());
//        // Set up the order details columns

        OrderNum.setCellValueFactory(cellData -> cellData.getValue().foodCodeProperty());
        PizzaName.setCellValueFactory(cellData -> cellData.getValue().pizzaNameProperty());
        TotalPrice.setCellValueFactory(cellData -> cellData.getValue().pizzaPriceProperty());
        PizzaQuantity.setCellValueFactory(cellData -> cellData.getValue().pizzaquantityProperty().asObject());
        DrinksName.setCellValueFactory(cellData -> cellData.getValue().drinkNameProperty());
        DrinksQuantity.setCellValueFactory(cellData -> cellData.getValue().drinkquantityProperty().asObject());
        AddOns.setCellValueFactory(cellData -> cellData.getValue().addonsNameProperty());
        AddOnsQty.setCellValueFactory(cellData -> cellData.getValue().addonsquantityProperty().asObject());


        orderStatusComboBox.getItems().addAll("Pending", "In Progress", "Completed");
        OrderNumberInput.setOnAction(event -> handleOrderNumberInput());
        // Example product IDs and prices for demonstration
        productPrices.put("P001", 100.0);
        productPrices.put("P002", 150.0);
        productPrices.put("P003", 200.0);

        // Populate the ComboBox with product IDs
        inputOrderNumber.setOnAction(event -> loadOrderDetails());
        OrdersTable.getItems().addListener((ListChangeListener<OrderItem>) change -> updateTotalPrice());

        // Update the total price on startup
        updateTotalPrice();
        loadOrders();
    }
    public void loadOrdersData() {
        Task<ObservableList<OrderSummary>> task = new Task<ObservableList<OrderSummary>>() {
            @Override
            protected ObservableList<OrderSummary> call() throws Exception {
                OrderDataFetcher dataFetcher = new OrderDataFetcher();
                return dataFetcher.fetchOrdersFromDatabase();

            }
        };

        task.setOnSucceeded(event -> {
            // Once the task completes, update the TableView on the JavaFX Application Thread
            ObservableList<OrderSummary> orderList = task.getValue();
            orderNumberColumn.setCellValueFactory(cellData -> cellData.getValue().orderNumberProperty());
            orderTotalColumn.setCellValueFactory(cellData -> cellData.getValue().orderTotalProperty());
            orderStatusColumn.setCellValueFactory(cellData -> cellData.getValue().orderStatusProperty());
            OrdersTypeColumn.setCellValueFactory(cellData -> cellData.getValue().orderTypeProperty());
            // Populate the TableView with the data
            orderTableView.setItems(orderList);
        });

        task.setOnFailed(event -> {
            // Handle failure (e.g., show an alert)
            showAlert("Error", "Failed to fetch orders from the database.");
        });

        // Start the task in a separate thread
        Thread thread = new Thread(task);
        thread.setDaemon(true); // Daemon thread will not block JVM shutdown
        thread.start();
    }

    private void loadOrders() {
        ObservableList<OrderSummary> orders = FXCollections.observableArrayList();
        try {
            List<String> lines = Files.readAllLines(Paths.get(ORDER_FILE));
            OrderSummary currentOrder = null;
            Stack<OrderItem> currentStack = null;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                // When a new order starts
                if (line.matches("\\d{4}: Order Items")) {
                    if (currentOrder != null && currentStack != null) {
                        orders.add(currentOrder);
                        orderQueue.offer(currentOrder);  // Add to queue
                        orderStacks.put(currentOrder.getOrderNumber(), currentStack);
                    }
                    String orderNumber = line.split(":")[0];
                    currentOrder = new OrderSummary(orderNumber);
                    currentStack = new Stack<>();
                }
                // Parsing food code
                else if (line.startsWith("Bundle Code:")) {
                    OrderItem item = new OrderItem("", 0, "", 0, "", 0, "", line.substring(10).trim());
                    if (currentStack != null) {
                        currentStack.push(item);  // Add item to stack
                    }
                }
                // Parsing meal name (which could include addons or drinks)
                else if (line.startsWith("Pizza Name:") && !currentStack.isEmpty()) {
                    String pizzaName = line.substring(10).trim();

                    // Continue to append addons/drinks as long as the next lines aren't "Price:" or "Quantity:"
                    StringBuilder fullPizzaName = new StringBuilder(pizzaName);

                    // Look ahead to check if the next lines are addons/drinks
                    for (int j = i + 1; j < lines.size(); j++) {
                        String nextLine = lines.get(j).trim();

                        // Stop appending if we encounter a price, quantity, or total indicator
                        if (nextLine.startsWith("Price:") || nextLine.startsWith("Quantity:") || nextLine.startsWith("Total Price:")) {
                            i = j - 1;  // Move i to where parsing left off
                            break;
                        }
                        // Otherwise, it's an addon or drink
                        fullPizzaName.append("\n").append(nextLine.trim());
                    }
                    currentStack.peek().setPizzaName(fullPizzaName.toString().trim());
                } else if (line.startsWith("Quantity:") && !currentStack.isEmpty()) {
                    currentStack.peek().setPizzaQuantity(Integer.parseInt(line.substring(9).trim()));
                }
                else if (line.startsWith("Drink Name:") && !currentStack.isEmpty()) {
                    String drinkName = line.substring(11).trim();

                    StringBuilder fullDrinkName = new StringBuilder(drinkName);
                    for (int j = i + 1; j < lines.size(); j++) {
                        String nextLine = lines.get(j).trim();

                        if (nextLine.startsWith("Price:") || nextLine.startsWith("Quantity:") || nextLine.startsWith("Total Price:")) {
                            i = j - 1;
                            break;
                        }
                        fullDrinkName.append("\n").append(nextLine.trim());
                    }

                    // Set the full meal name in the current order item

                    currentStack.peek().setDrinkName(fullDrinkName.toString().trim());

                }
            else if (line.startsWith("DrinksName:")&& !currentStack.isEmpty()) {
                    currentStack.peek().setDrinkName(line.substring(11).trim());
                }
                else if(line.startsWith("DrinksQuantity:")&& !currentStack.isEmpty()) {
                    currentStack.peek().setDrinkQuantity(Integer.parseInt(line.substring(12).trim()));
                }else if (line.startsWith("AddonsName:")&& !currentStack.isEmpty()){
                    currentStack.peek().setAddonsName(line.substring(13).trim());
                }
                else if(line.startsWith("AddonsQty:")&& !currentStack.isEmpty()) {
                    currentStack.peek().setAddonsQuantity(Integer.parseInt(line.substring(14).trim()));
                }

                // Parsing price
                else if (line.startsWith("Price:") && !currentStack.isEmpty()) {
                    currentStack.peek().setTotalPrice(line.substring(6).trim());
                }
                // Parsing quantity

                // Parsing total price for the order
                else if (line.startsWith("Total Price:") && currentOrder != null) {
                currentOrder.setPizzatotal(line.substring(12).trim());
                }
                // Parsing status
                else if (line.startsWith("Status:") && currentOrder != null) {
                    currentOrder.setOrderStatus(line.substring(7).trim());
                }
            }

            // Add the last order if it exists
            if (currentOrder != null && currentStack != null) {
                orders.add(currentOrder);
                orderQueue.offer(currentOrder);
                orderStacks.put(currentOrder.getOrderNumber(), currentStack);
            }

            orderTableView.setItems(orders);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private void loadOrderDetails() {
        String orderNumberInput = inputOrderNumber.getText().trim();  // Get the entered order number
        if (orderNumberInput.isEmpty()) {
            showAlert("Error", "Please enter an order number");
            return;
        }

        // Loop through the orders in the orderTableView to find the matching order
        for (OrderSummary order : orderTableView.getItems()) {
            if (order.getOrderNumber().equals(orderNumberInput)) {
                // Update the orderTotalText with the total price of the selected order
                orderTotalText.setText(String.format("₱%.2f", Double.parseDouble(order.getOrderTotal())));

                // Update the orderStatusComboBox with the current status of the order
                orderStatusComboBox.setValue(order.getOrderStatus());
                return;  // Exit after updating the UI with the found order's details
            }
        }

        // If no matching order is found
        showAlert("Information", "Order number " + orderNumberInput + " not found.");
    }

    // This method will be called when the "Update Status" button is clicked
    @FXML
    private void UpdateStatusClicked(MouseEvent event) {
        String orderNumberInput = inputOrderNumber.getText();
        String newStatus = orderStatusComboBox.getValue();

        if (orderNumberInput.isEmpty() || newStatus == null) {
            showAlert("Error", "Order number or new status not selected");
            return;
        }

        for (OrderSummary order : orderTableView.getItems()) {
            if (order.getOrderNumber().equals(orderNumberInput)) {
                order.setOrderStatus(newStatus);  // Update order status in the table view

                // Update the status in the database
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String updateQuery = "UPDATE orders SET status = ? WHERE order_number = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, newStatus);  // Set the new status
                        updateStmt.setString(2, orderNumberInput);  // Set the order number
                        int rowsUpdated = updateStmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            System.out.println("Order status updated successfully in the database.");
                        } else {
                            System.out.println("No order found with that order number.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to update order status in the database.");
                }

                // Additional logic for removing orders from queue and stack if completed
                if (newStatus.equals("Completed")) {
                    orderQueue.remove(order);  // Remove from queue
                    orderStacks.remove(order.getOrderNumber());  // Remove from stack
                    orderTableView.getItems().remove(order);  // Remove from table view
                }

                orderTableView.refresh();
                saveOrdersToFile();
                return;
            }
        }

        showAlert("Error", "Order number " + orderNumberInput + " not found");
    }


    private void saveOrdersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_FILE))) {
            for (OrderSummary order : orderTableView.getItems()) {
                writer.write(order.getOrderNumber() + ": Order Items\n");

                Stack<OrderItem> orderStack = orderStacks.get(order.getOrderNumber());
                if (orderStack != null) {
                    // Create temporary stack to preserve original order
                    Stack<OrderItem> tempStack = new Stack<>();
                    Stack<OrderItem> originalStack = new Stack<>();

                    // Copy items to temp stack (reverses order)
                    while (!orderStack.isEmpty()) {
                        tempStack.push(orderStack.pop());
                    }

                    // Write items and restore original stack
                    while (!tempStack.isEmpty()) {
                        OrderItem item = tempStack.pop();
                        writer.write("  Food Code: " + item.getFoodCode() + "\n");
                        writer.write("  Pizza Name: " + item.getPizzaName() + "\n");
                        writer.write("  Quantity: " + item.getPizzaQuantity() + "\n");
                        writer.write("  Drink Name:" + item.getDrinkName() + "\n");
                        writer.write("  Drink Qty:" + item.getDrinkQuantity() + "\n");
                        writer.write("  Addons:"+ item.getAddonsName() + "\n");
                        writer.write("  AddonsQty:"+ item.getAddonsQuantity() + "\n");
                        writer.write("  Price: " + item.getTotalPrice() + "\n");

                        orderStack.push(item);
                        originalStack.push(item);
                    }

                    // Restore original stack order
                    orderStack.clear();
                    while (!originalStack.isEmpty()) {
                        orderStack.push(originalStack.pop());
                    }
                }

                writer.write("  Total Price: " + order.getOrderTotal() + "\n");
                writer.write("  Status: " + order.getOrderStatus() + "\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save orders: " + e.getMessage());
        }
    }


    private List<OrderItem> fetchOrderItems(String orderNumber) {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT oi.quantity, oi.drinks, oi.drinksquantity, oi.addons, oi.AddonsQuantity, oi.price, " +
                "p.product_name AS pizzaName " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE o.order_number = ?";

        try (Connection conn = Database.connectDB(); // Get connection from Database class
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, orderNumber); // Set the orderNumber parameter in the query
            ResultSet rs = stmt.executeQuery();

            // Iterate through the result set and populate the list of OrderItem objects
            while (rs.next()) {
                // Format the price as a string
                String formattedPrice = String.format("%.2f", rs.getDouble("price"));

                // Construct the OrderItem object
                OrderItem item = new OrderItem(
                        rs.getString("pizzaName"),       // pizzaName
                        rs.getInt("quantity"),          // pizzaQuantity
                        rs.getString("drinks"),         // drinkName
                        rs.getInt("drinksQuantity"),    // drinkQuantity
                        rs.getString("addons"),         // addonsName
                        rs.getInt("AddonsQuantity"),    // addonsQuantity
                        formattedPrice,                 // totalPrice (formatted)
                        ""                              // foodCode (optional; use default or leave empty)
                );

                orderItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace to debug any SQL issues
        }
        return orderItems; // Return the list of OrderItem objects
    }


    @FXML
    private void handleOrderNumberInput() {
        String orderNumber = OrderNumberInput.getText().trim();

        if (orderNumber.isEmpty()) {
            showAlert("Error", "Please enter an order number");
            return;
        }

        List<OrderItem> orderItems = fetchOrderItems(orderNumber);
        if (!orderItems.isEmpty()) {
            OrdersTable.setItems(FXCollections.observableArrayList(orderItems));
            updateTotalPrice(); // Ensure it calculates correctly
        } else {
            showAlert("Information", "No items found for order number: " + orderNumber);
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Map<String, Double> productPrices = new HashMap<>();

    @FXML
    private void QueueClicked(MouseEvent event) {
        MainTab.getSelectionModel().select(OrderQueueTab);
        InputTab.getSelectionModel().select(OrderDetails);
    }

    @FXML
    private void OrdersClicked(MouseEvent event) {
        MainTab.getSelectionModel().select(OrdersTab);
        InputTab.getSelectionModel().select(OrderInput);
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (OrderItem item : OrdersTable.getItems()) {
            // Get the numeric value of the meal price, removing any non-numeric characters (like ₱)
            String priceStr = item.getTotalPrice().replaceAll("[^\\d.]", "");
            double itemPrice = Double.parseDouble(priceStr);

            // Calculate the total for this item (price * quantity)
            total += itemPrice * item.getPizzaQuantity();
        }

        // Update the TotalText to show the new total value
        TotalText.setText(String.format("₱%.2f", total));
    }



    @FXML
    private void PayClicked(MouseEvent event) {
        // Get the total amount from TotalText
        String totalTextValue = TotalText.getText().replaceAll("[^\\d.]", "");  // Remove the ₱ symbol and get the number
        double totalAmount = Double.parseDouble(totalTextValue);

        // Get the amount from the AmountTextField (money given by the customer)
        String amountGivenText = AmountTextField.getText();
        if (amountGivenText.isEmpty()) {
            showAlert("Error", "Please enter the amount given by the customer");
            return;
        }

        double amountGiven = Double.parseDouble(amountGivenText);

        // Calculate the change
        if (amountGiven < totalAmount) {
            showAlert("Error", "Amount given is less than the total. Please enter a valid amount.");
            return;
        }

        double changeAmount = amountGiven - totalAmount;

        // Update the ChangeText with the calculated change
        ChangeText.setText(String.format("₱%.2f", changeAmount));
    }


    @FXML
    private void ReceiptClicked(MouseEvent event) {
        String orderNumber = OrderNumberInput.getText().trim();  // Get the order number
        if (orderNumber.isEmpty()) {
            showAlert("Error", "Please enter an order number");
            return;
        }

        Stack<OrderItem> orderStack = orderStacks.get(orderNumber);  // Get the items for this order
        if (orderStack == null || orderStack.isEmpty()) {
            showAlert("Error", "No items found for order number: " + orderNumber);
            return;
        }

        // Get the order type (Dine In or Take Out) from the OrderMenuController
        String orderType = OrderMenuController.getOrderType();

        // Prepare the receipt content
        StringBuilder receiptContent = new StringBuilder();
        receiptContent.append("Pizzify \nOrder Receipt\n");
        receiptContent.append("Order Number: ").append(orderNumber).append("\n");
        receiptContent.append("Order Type: ").append(orderType).append("\n");  // Include order type
        receiptContent.append("======================================\n");

        // Loop through the order items and add to receipt
        double totalPrice = 0.0;
        for (OrderItem item : orderStack) {
            String pizzaName = item.getPizzaName();
            int quantity = item.getPizzaQuantity();
            String drinkName = item.getDrinkName();
            int dqty = item.getDrinkQuantity();
            String addons = item.getAddonsName();
            int aqty = item.getAddonsQuantity();
            double price = Double.parseDouble(item.getTotalPrice().replaceAll("[^\\d.]", ""));

            double itemTotal = price * quantity;
            totalPrice += itemTotal;

            receiptContent.append(String.format("%-20s %-20s %.2f %-20s %.2f  %5d x ₱%.2f = ₱%.2f\n", pizzaName , drinkName, dqty,addons,aqty, quantity, price, itemTotal));
        }

        receiptContent.append("======================================\n");
        receiptContent.append(String.format("Total: ₱%.2f\n", totalPrice));

        // Get the amount paid by the customer
        String amountPaidText = AmountTextField.getText();
        if (amountPaidText.isEmpty()) {
            showAlert("Error", "Please enter the amount paid by the customer.");
            return;
        }
        double amountPaid = Double.parseDouble(amountPaidText);

        // Get the change (this should already be calculated when the Pay button is clicked)
        String changeText = ChangeText.getText().replaceAll("[^\\d.]", "");  // Remove any currency symbols
        double changeAmount = Double.parseDouble(changeText);

        // Add the amount paid and change to the receipt
        receiptContent.append(String.format("Amount Paid: ₱%.2f\n", amountPaid));
        receiptContent.append(String.format("Change: ₱%.2f\n", changeAmount));

        // Write receipt to a file named after the order number
        String fileName = "Receipt_" + orderNumber + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(receiptContent.toString());
            showAlert("Success", "Receipt saved as " + fileName);
        } catch (IOException e) {
            showAlert("Error", "Failed to save receipt: " + e.getMessage());
        }
    }

    @FXML
    private void SignOutClicked(MouseEvent event) throws IOException {
        if (staticOrderItems != null) {
            staticOrderItems.clear();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/StartMenu.fxml"));
        Parent mainRoot = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene mainScene = new Scene(mainRoot);
        stage.setScene(mainScene);
        stage.show();
    }
}
