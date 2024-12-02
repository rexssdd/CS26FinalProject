package me.group.cceproject.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class OrderMenuController {
    @FXML
    private Button Cancel;
    @FXML
    private AnchorPane PizzaPane;

    @FXML
    private AnchorPane SpecialPizzaPane;

    private String orderType;

    @FXML
    private TableView<OrderItem> OrderTable;

    @FXML
    private TableColumn<OrderItem, String> PizzaNameTable;

    @FXML
    private TableColumn<OrderItem, Integer> PizzaQuantityTable;

    @FXML
    private TableColumn<OrderItem, String> DrinkNameTable;

    @FXML
    private TableColumn<OrderItem, Integer> DrinkQuantityTable;

    @FXML
    private TableColumn<OrderItem, String> AddonsNameTable;
    @FXML
    private TableColumn<OrderItem, Integer> AddonsQuantityTable;
    @FXML
    private TableColumn<OrderItem, String> TotalPrice;
    @FXML
    private FlowPane productContainer; // Parent container for product cards

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;


    @FXML
    private Text TotalPriceText;

    static ObservableList<OrderItem> staticOrderItems;

    private static OrderMenuController instance;

    @FXML
    private Text orderTypeLabel;
    static String staticOrderType;

    @FXML
    public void initialize() {
        loadProducts();
        // Set up the table columns
        PizzaNameTable.setCellValueFactory(new PropertyValueFactory<>("pizzaName"));
        PizzaQuantityTable.setCellValueFactory(new PropertyValueFactory<>("pizzaQuantity"));
        DrinkNameTable.setCellValueFactory(new PropertyValueFactory<>("drinkName"));
        DrinkQuantityTable.setCellValueFactory(new PropertyValueFactory<>("drinkQuantity"));
        AddonsNameTable.setCellValueFactory(new PropertyValueFactory<>("addonsName"));
        AddonsQuantityTable.setCellValueFactory(new PropertyValueFactory<>("addonsQuantity"));
        TotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        // Store instance for access from MealAddonsController

        instance = this;
        // Initialize the ObservableList if it's null
        if (staticOrderItems == null) {
            staticOrderItems = FXCollections.observableArrayList();
        }

        // Set the items to the table
        OrderTable.setItems(staticOrderItems);

        // Print current items for debugging
        System.out.println("Current items in table: " + staticOrderItems.size());
        for (OrderItem item : staticOrderItems) {
            System.out.println("Pizza: " + item.getPizzaName());
            System.out.println("Quantity: " + item.getPizzaQuantity());
            System.out.println("Drinks: " + item.getDrinkName());
            System.out.println("Quantity: " + item.getDrinkQuantity());
            System.out.println("Addons: " + item.getAddonsName());
            System.out.println("Quantity: " + item.getAddonsQuantity());
            System.out.println("Total: " + item.getTotalPrice());
        }

        // Add listener to update total price when items change
        staticOrderItems.addListener((ListChangeListener.Change<? extends OrderItem> change) -> {
            updateTotalPrice();
        });

        // Update total price initially
        updateTotalPrice();

        // Restore order type if it exists
        if (staticOrderType != null && orderTypeLabel != null) {
            orderTypeLabel.setText("Your Order ( " + staticOrderType + " ):");
        }
        // Set initial visibility


    }

    // Static method to add order item
    public static void addOrderItem(String pizzaName, int pizzaQuantity, String drinkName, int drinkQuantity, String addonsName, int addonsQuantity, String pizzaPrice, String foodCode, String OrderNumber) {
        System.out.println("Adding order item: " + pizzaName + " - " + pizzaQuantity + " - " + drinkName + " - " + drinkQuantity + " - " + addonsName + " - " + addonsQuantity + " - " + pizzaPrice + " - " + foodCode);
        if (staticOrderItems == null) {
            staticOrderItems = FXCollections.observableArrayList();
        }
        staticOrderItems.add(new OrderItem(pizzaName, pizzaQuantity, drinkName, drinkQuantity, addonsName, addonsQuantity, pizzaPrice, foodCode));
    }

    private double updateTotalPrice() {
        double total = 0.0;
        for (OrderItem item : staticOrderItems) {
            String priceStr = item.getTotalPrice().replaceAll("[^\\d.]", "");
            double itemPrice = Double.parseDouble(priceStr);
            total += itemPrice * item.getPizzaQuantity();
        }
        TotalPriceText.setText(String.format("₱ %.2f", total));
        return total;
    }
    // Method to get the pizza quantity of the selected order item
    public int getSelectedPizzaQuantity() {
        // Get the selected item from the table
        OrderItem selectedItem = OrderTable.getSelectionModel().getSelectedItem();

        // Check if an item is selected
        if (selectedItem != null) {
            // Return the pizza quantity
            return selectedItem.getPizzaQuantity();
        } else {
            // No item selected
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a pizza first.", null, null, null);
            return -1; // Indicating no item was selected
        }
    }


    public static OrderMenuController getInstance() {
        return instance;
    }

    public void setOrderType(String orderType) {
        staticOrderType = orderType; // Store in static variable
        updateOrderTypeLabel();
    }

    // Update the label in the UI to show whether it's Dine In or Take Out
    private void updateOrderTypeLabel() {
        if (orderTypeLabel != null) {
            orderTypeLabel.setText("Your Order (" + staticOrderType + "):");
        } else {
            System.err.println("Unable to set order type label. Label is null.");
        }
    }

    public static String getOrderType() {
        return staticOrderType;
    }

    @FXML
    public void RegularPizzaCategoryClicked(MouseEvent event) {
        PizzaPane.setVisible(true);
        SpecialPizzaPane.setVisible(false);
    }

    @FXML
    public void SpecialPizzaCategoryClicked(MouseEvent event) {
        PizzaPane.setVisible(false);
        SpecialPizzaPane.setVisible(true);
    }

    // Burger Category
    // Yumburger/B1
    @FXML
    public void B1Clicked(MouseEvent event) {
        String pizzaName = "Deluxe Pizza";
        String pizzaPrice = "₱ 199";
        String imagePath = "Deluxe.png";
        String foodCode = "Deluxe";

        loadMealAddons(pizzaName, pizzaPrice, imagePath, foodCode, event);
    }

    // Cheesy Burger/B2
    @FXML
    public void B2Clicked(MouseEvent event) {
        String pizzaName = "Hawaian Pizza";
        String pizzaPrice = "₱ 199";
        String imagePath = "hawaian.png";
        String foodCode = "hawaian";

        loadMealAddons(pizzaName, pizzaPrice, imagePath, foodCode, event);
    }

    // Whopper/B3
    @FXML
    public void B3Clicked(MouseEvent event) {
        String pizzaName = "Margherita Pizza";
        String pizzaPrice = "₱ 199";
        String imagePath = "Special.png";
        String foodCode = "Special";

        loadMealAddons(pizzaName, pizzaPrice, imagePath, foodCode, event);
    }

    // Chicken Wings Category
    // Spicy Chicken Wings/C1
    @FXML
    public void C1Clicked(MouseEvent event) {
        String pizzaName = "Pepperoni Pizza";
        String pizzaPrice = "₱ 159";
        String imagePath = "Pepperoni.png";
        String foodCode = "pepperoni";

        loadMealAddons(pizzaName, pizzaPrice, imagePath, foodCode, event);
    }

    private void loadMealAddons(String pizzaName, String pizzaPrice, String imagePath, String foodCode, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/PizzaAddons.fxml"));
            Parent mealAddonsRoot = loader.load();

            PizzaAddonsController pizzaAddonsController = loader.getController();
            // Pass the foodCode along with other details
            pizzaAddonsController.setMealDetails(pizzaName, pizzaPrice, imagePath, foodCode);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene mealAddonsScene = new Scene(mealAddonsRoot);
            stage.setScene(mealAddonsScene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading PizzaAddons.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void PayforOrderClicked(MouseEvent event) {
        if (staticOrderItems == null || staticOrderItems.isEmpty()) {
            showAlert("No Order", "Please add items to your order before proceeding to payment.");
            return;  // Do not proceed if no items have been added
        }


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


    @FXML
    private void ViewCartClicked(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/ShoppingCart.fxml"));
            Parent shopCartRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene shopCartScene = new Scene(shopCartRoot);
            stage.setScene(shopCartScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error going to shopping cart: " + e.getMessage());
        }
    }

    public void CancelClicked(MouseEvent event) throws IOException {

        if (staticOrderItems != null) {
            staticOrderItems.clear(); // This clears the table
        }
        // Load the main screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/Main.fxml"));
        Parent mainRoot = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene mainScene = new Scene(mainRoot);
        stage.setScene(mainScene);
        stage.show();
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showAlert(Alert.AlertType alertType,String title, String content, String noProductsFound, Object o, String s) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();

    }

//    private void loadProducts() {
//        // Example product data (can be replaced with database retrieval)
//        String[] productNames = {"Pepperoni Pizza", "Margherita Pizza", "Hawaiian Pizza"};
//        double[] productPrices = {199.0, 199.0, 199.0};
//        String[] productImages = {"pepperoni.png", "margherita.png", "hawaiian.png"};
//
//        // Clear the PizzaPane before loading products
//        PizzaPane.getChildren().clear();
//
//        // Loop to create product cards
//        for (int i = 0; i < productNames.length; i++) {
//            VBox productBox = createProductBox(productNames[i], productPrices[i], productImages[i]);
//            PizzaPane.getChildren().add(productBox); // Add product card to PizzaPane
//        }
//    }

//    private VBox createProductBox(String name, double price, String imagePath) {
//        VBox productBox = new VBox();
//        productBox.setSpacing(5.0);
//        productBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 10;");
//        productBox.setPrefWidth(200);
//
//        // Product Name
//        Label nameLabel = new Label(name);
//        nameLabel.setStyle("-fx-font-weight: bold;");
//
//        // Product Price
//        Label priceLabel = new Label("₱" + price);
//        priceLabel.setStyle("-fx-font-size: 14px;");
//
//        // Add Button
//        Button addButton = new Button("Add");
//        addButton.setOnAction(event -> {
//            // Add product to the order list
//            addOrderItem(name, 1, "", 0, "", 0, "₱" + price, name);
//        });
//
//        // Add components to productBox
//        productBox.getChildren().addAll(nameLabel, priceLabel, addButton);
//        return productBox;
//    }
public void loadProducts() {
    List<ProdData> products = fetchProductsFromDatabase();

    if (products.isEmpty()) {
        showAlert(Alert.AlertType.INFORMATION, "No Products Found", null, "There are no products available in the database.");
        return;
    }

    for (ProdData product : products) {
        try {
            // Load ProductCard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/ProductCard.fxml"));
            AnchorPane productCard = loader.load();

            // Get ProductCardController and set product data
            ProductCardController controller = loader.getController();
            controller.setData(product);

            // Add product card to the container
            productContainer.getChildren().add(productCard);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Loading Product", null, "An error occurred while loading products: " + e.getMessage());
        }
    }
}

    private void showAlert(Alert.AlertType alertType, String title, Object header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header == null ? null : header.toString());
        alert.setContentText(content);
        alert.showAndWait();
    }

    private List<ProdData> fetchProductsFromDatabase() {
        List<ProdData> productList = new ArrayList<>();

        try {
            connect = Database.connectDB(); // Establish connection to the database
            if (connect == null) {
                throw new Exception("Database connection failed.");
            }

            String query = "SELECT product_id, product_name, price, stock, image FROM products WHERE status = 'Available'";
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();

            while (result.next()) {
                // Create a ProdData object for each product
                ProdData product = new ProdData(
                        result.getString("product_id"),
                        result.getString("product_name"),
                        result.getDouble("price"),
                        result.getInt("stock"),
                        result.getString("image")
                );
                productList.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Fetching Products", null, "An error occurred: " + e.getMessage());
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return productList;
    }

    public static class Database {
        public static Connection connectDB() {
            String url = "jdbc:mysql://localhost:3306/pizzaordering";
            String user = "root";
            String password = "";

            try {
                return DriverManager.getConnection(url, user, password);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

