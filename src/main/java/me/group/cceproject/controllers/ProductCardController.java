package me.group.cceproject.controllers;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.awt.event.MouseEvent;


public class ProductCardController {
    @FXML
    private AnchorPane productpane;

    @FXML
    private Label prod_name;

    @FXML
    private Label prod_price;
    @FXML
    private Label Stock;

    @FXML
    private ImageView prod_imageView;

    @FXML
    private Spinner<Integer> prod_spinner;
    @FXML
    private String productId; // The product ID should be passed to this controller


    @FXML
    private Button prod_addBtn;

    private ProdData productData;
    private Image image;

    private String prodID;
    private String type;
    private String prod_date;
    private String prod_image;
    private String stock;

    private SpinnerValueFactory<Integer> spin;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private double totalP;
    private double pr;

    private Alert alert;

    public void initialize() {
//        printAllItems();
//        getStockFromDatabase(productId);
        prod_name.setAlignment(Pos.CENTER);  // Set alignment to center
        prod_price.setAlignment(Pos.CENTER);
        Stock.setAlignment(Pos.CENTER);
    }

    private void getStockFromDatabase(String productId) {
        if (productId == null || productId.isEmpty()) {
            System.out.println("Invalid product ID: " + productId);
            return;  // Exit the method if productId is null or empty
        }

        new Thread(() -> {
            try {
                // Establish connection
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

                // Query to fetch the stock for the given productId
                String query = "SELECT stock FROM products WHERE product_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, productId);  // Pass the productId as a parameter
                ResultSet rs = pstmt.executeQuery();

                // Check if the result set contains data
                if (rs.next()) {
                    int stock = rs.getInt("stock");  // Get the stock value
                    System.out.println("Stock fetched: " + stock);  // Log the stock value for debugging

                    // Update the label on the JavaFX application thread
                    Platform.runLater(() -> {
                        if (Stock != null) {  // Check if Stock label is not null
                            Stock.setText("Stock: " + stock);  // Set stock value to the label
                        }
                    });
                } else {
                    System.out.println("No stock found for productId: " + productId);
                    Platform.runLater(() -> {
                        if (Stock != null) {
                            Stock.setText("Stock: Not Available");  // Set a fallback message if no stock is found
                        }
                    });
                }

                connection.close();  // Close connection after use
            } catch (SQLException e) {
                e.printStackTrace();  // Log any SQL exceptions
                Platform.runLater(() -> {
                    if (Stock != null) {
                        Stock.setText("Error fetching stock");  // Show error message on the label
                    }
                });
            }
        }).start();  // Start the thread to query the database asynchronously
    }




    private void printAllItems() {
        try {
            // Establish connection (modify the connection details)
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

            // Query to fetch all products
            String query = "SELECT * FROM products";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            // Loop through the result set to get each product's details
            while (rs.next()) {
                // Assuming the table has columns: product_id, product_name, price, stock, image
                String productId = rs.getString("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                String image = rs.getString("image");

                // Print each product's details
//                System.out.println("Product ID: " + productId);
//                System.out.println("Product Name: " + productName);
//                System.out.println("Price: " + price);
//                System.out.println("Stock: " + stock);
//                System.out.println("Image: " + image);
//                System.out.println("-------------");

                // Update the label to show stock for each product
                // Assuming you have a Stock label for each product, or a generic Stock label
                Platform.runLater(() -> {
                    if (Stock != null) {  // Check if the Stock label is not null
                        Stock.setText(" " + stock);  // Set stock value on the label
                    }
                });
            }

            connection.close(); // Close connection after use
        } catch (SQLException e) {
            e.printStackTrace(); // Log any SQL exceptions
        }
    }



    // You can set the productId dynamically from another part of your application
    public void setProductId(String productId) {
        this.productId = productId;
    }


    public void setData(ProdData prodData) {
        // Set the product data to the controller
        this.productData = prodData;

        // Set product image
        prod_image = prodData.getImage();
        prodID = prodData.getProductId();
        prod_name.setText(prodData.getProductName());
        prod_price.setText("$" + String.valueOf(prodData.getPrice()));
        Stock.setText(String.valueOf(prodData.getStock()));
        String imagepath = "File:" + prodData.getImage();
        image = new Image(imagepath, 110, 90, false, true);
        prod_imageView.setImage(image);

        // Store the price for later use when adding to cart
        pr = prodData.getPrice();
    }


    private void loadPizzaAddons(String pizzaName, String pizzaPrice, String imagePath, String productId, javafx.scene.input.MouseEvent event) {
        try {
            // Load the MealAddons.fxml scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/PizzaAddons.fxml"));
            Parent mealAddonsRoot = loader.load();

            // Get the controller for MealAddons and pass product details
            PizzaAddonsController pizzaAddonsController = loader.getController();
            pizzaAddonsController.setMealDetails(pizzaName, pizzaPrice, imagePath, productId);

            // Set the scene in the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene mealAddonsScene = new Scene(mealAddonsRoot);
            stage.setScene(mealAddonsScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", null, "Error loading PizzaAddons screen.");
        }
    }


    private int qty;
    public void ProductPaneClicked(javafx.scene.input.MouseEvent event) {
       try{ Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

        // Query to fetch all products
        String query = "SELECT * FROM products";
        PreparedStatement pstmt = connection.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        if (connection == null) {
            showAlert(Alert.AlertType.ERROR, "Error Message", null, "Failed to connect to the database.");
            return;
        }   if (productData != null) {
            // Get the necessary details from the ProdData object
            String productId = productData.getProductId();
            String productName = productData.getProductName();
            double price = productData.getPrice();
            String imagepath = productData.getImage();

            // Call the method to load the pizza addons screen with the selected product details
            loadPizzaAddons(productName, String.valueOf(price), imagepath, productId, event);
        }else {
            showAlert(Alert.AlertType.ERROR, "Error", null, "No product data found.");
        }


        try {
            int stock = getProductStock(prodID);
            if (stock <= 0) {
                String productId = "someProductId"; // You need to define or retrieve the productId
                updateProductStatus("Unavailable", 0, productId);
                showAlert(Alert.AlertType.ERROR, "Error Message", null, "This product is out of stock.");
                return;
            }

//            if (!isProductAvailable(productId)) {
//                showAlert(Alert.AlertType.ERROR, "Error Message", null, "   This product is not available.");
//                return;
//            }

//            qty = prod_spinner.getValue();
//            if (qty <= 0) {
//                showAlert(Alert.AlertType.ERROR, "Error Message", null, "Please select a valid quantity.");
//                return;
//            }

            if (stock < qty) {
                showAlert(Alert.AlertType.ERROR, "Error Message", null, "Not enough stock available.");
                return;
            }

//            addToOrderMenu();
//            updateProductStock(productId, qty);
//            showAlert(Alert.AlertType.INFORMATION, "Information Message", null, "Successfully Added!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Message", null, "An error occurred: " + e.getMessage());
        }
       }
       catch (SQLException e) {
           e.printStackTrace();
       }
    }

    // Fetch product stock
    private int getProductStock(String productId) throws Exception {
        // SQL query to get the stock of a product by product_id
        String query = "SELECT stock FROM products WHERE product_id = ?";

        // Establish connection (modify the connection details)
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set the productId as the parameter for the query
            pstmt.setString(1, productId);

            // Execute the query and retrieve the result
            try (ResultSet rs = pstmt.executeQuery()) {
                // If the product exists, return the stock value
                if (rs.next()) {
                    return rs.getInt("stock");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log any SQL exceptions
            throw new Exception("Error fetching product stock", e); // Throw an exception in case of an error
        }

        // Return 0 if the product was not found or there was an issue
        return 0;
    }


    // Check if the product is available
    private boolean isProductAvailable(String productId) throws Exception {
        String query = "SELECT status FROM products WHERE product_id = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set the productId as the parameter for the query
            stmt.setString(1, productId);

            // Execute the query and retrieve the result
            try (ResultSet rs = stmt.executeQuery()) {
                // If the product exists, check if its status is 'Available'
                if (rs.next()) {
                    return "Available".equalsIgnoreCase(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log any SQL exceptions
            throw new Exception("Error checking product availability", e); // Throw an exception if there is an error
        }

        // Return false if the product doesn't exist or if there was an error
        return false;
    }


    // Update product status
    private void updateProductStatus(String status, int stock, String productId) throws Exception {
        String query = "UPDATE products SET status = ?, stock = ? WHERE product_id = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            stmt.setString(1, status);
            stmt.setInt(2, stock);
            stmt.setString(3, productId); // Use productId parameter here

            // Execute the update
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Log any SQL exceptions
            throw new Exception("Error updating product status", e); // Rethrow with a descriptive message
        }

    }


    // Add product to order menu
    private void addToOrderMenu() throws Exception {

        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");
//             String query = "INSERT INTO customer (customer_id, prod_id, prod_name, type, quantity, price, date, image, em_username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        try (PreparedStatement stmt = connect.prepareStatement(query)) {
//            stmt.setString(1, String.valueOf(data.cID));
//            stmt.setString(2, prodID);
//            stmt.setString(3, prod_name.getText());
//            stmt.setString(4, type);
//            stmt.setInt(5, qty);
//            stmt.setDouble(6, qty * pr);
//            stmt.setDate(7, new java.sql.Date(new Date().getTime()));
//            stmt.setString(8, prod_image);
//            stmt.setString(9, data.username);
//            stmt.executeUpdate();
//        }
    }

    // Update product stock
    private void updateProductStock(String productId, int newStock) throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

        String query = "UPDATE products SET stock = ? WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newStock); // Set the new stock value
            stmt.setString(2, productId); // Set the product_id parameter
            stmt.executeUpdate(); // Execute the update
        }
    }


    // Show alert dialog
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
