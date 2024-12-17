package me.group.cceproject.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.sql.*;
import java.util.*;

import static me.group.cceproject.controllers.OrderMenuController.staticOrderItems;

public class SuperAdminMainController {

    @FXML
    private AnchorPane mainform;

    @FXML
    private TableView<productData> inventory_tableView;

    @FXML
    private TableColumn<productData, String> inventory_col_productID;

    @FXML
    private TableColumn<productData, String> inventory_col_productName;


    @FXML
    private TableColumn<productData, String> inventory_col_stock;

    @FXML
    private TableColumn<productData, String> inventory_col_price;

    @FXML
    private TableColumn<productData, String> inventory_col_status;

    @FXML
    private TableColumn<productData, String> inventory_col_date;
    @FXML
    private AnchorPane PizzaPane;

    @FXML
    private ComboBox<String> inventory_status;
    @FXML
    private TextField inventory_stock;
    @FXML
    private ImageView inventory_imageView;

    private Alert alert;

    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    @FXML
    private TextField inventory_productID;
    @FXML
    private TextField inventory_price;
    private Image image;
    private TableView<OrderSummary> orderTableView;

    private ObservableList<productData> cardListData = FXCollections.observableArrayList();
    @FXML
    private TextField inventory_productName;
    private static final String ORDER_FILE = "orders.txt";

    private final Queue<OrderSummary> orderQueue = new LinkedList<>();
    private final Map<String, Stack<OrderItem>> orderStacks = new HashMap<>();
    private final Map<String, Double> productPrices = new HashMap<>();

    @FXML
    public void initialize() {


        inventoryStatusList();
        inventoryShowData();


    }
    @FXML
    private void inventoryUpdateButtonClicked() {
        // Prompt the user to enter the product ID
        String productID = showInputDialog("Enter Product ID:", "Update Product");
        if (productID == null || productID.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Product ID cannot be empty.");
            return;
        }

        String checkProdID = "SELECT product_id FROM products WHERE product_id = ?";
        try (Connection connect = Database.connectDB();
             PreparedStatement checkStmt = connect.prepareStatement(checkProdID)) {

            checkStmt.setString(1, productID.trim());
            try (ResultSet result = checkStmt.executeQuery()) {
                if (!result.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error Message",
                            "Product ID " + productID.trim() + " does not exist.");
                } else {
                    // Proceed to update product details
                    updateProductInDatabase(connect, productID.trim());
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the product.");
        }
    }

    private void updateProductInDatabase(Connection connect, String productID) {
        // Ask the user for the details in a custom dialog
        ProductUpdateDialog dialog = new ProductUpdateDialog(productID);
        Optional<ProductDetails> result = dialog.showAndWait();

        if (result.isPresent()) {
            ProductDetails productDetails = result.get();

            String newStock = productDetails.getStock();
            String newPrice = productDetails.getPrice();
            String newStatus = productDetails.getStatus();
            String newImage = productDetails.getImage();

            // Update the product details in the database
            String updateData = "UPDATE products SET stock = ?, price = ?, status = ?, image = ?, date = ? WHERE product_id = ?";
            try (PreparedStatement updateStmt = connect.prepareStatement(updateData)) {
                // Set the new values or retain the current values
                updateStmt.setInt(1, newStock != null && !newStock.isEmpty() ? Integer.parseInt(newStock) : getCurrentStock(productID));
                updateStmt.setDouble(2, newPrice != null && !newPrice.isEmpty() ? Double.parseDouble(newPrice) : getCurrentPrice(productID));
                updateStmt.setString(3, newStatus != null && !newStatus.isEmpty() ? newStatus : getCurrentStatus(productID));
                updateStmt.setString(4, newImage != null && !newImage.isEmpty() ? newImage.replace("\\", "\\\\") : getCurrentImage(productID));
                updateStmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                updateStmt.setString(6, productID);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                    inventoryShowData();
                    inventoryClearBtn();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No product was updated. Please try again.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the product.");
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error", ex.getMessage());
            }
        }
    }

    // Helper method to display an input dialog and return the user's input
    private String showInputDialog(String prompt, String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);  // Set the title
        dialog.setHeaderText(prompt);  // Set the prompt
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // Product update dialog class


    public static class ProductUpdateDialog extends Dialog<ProductDetails> {
        private TextField stockField = new TextField();
        private TextField priceField = new TextField();
        private ComboBox<String> statusComboBox = new ComboBox<>();
        private TextField imageField = new TextField();
        private CheckBox updateImageCheckBox = new CheckBox("Update Image");
        private Stage primaryStage;

        public ProductUpdateDialog(String productID) {
            this.primaryStage = primaryStage;  // Passing the main stage for file dialog use

            setTitle("Update Product Details");
            setHeaderText("Enter new details for Product ID: " + productID);

            // Set default values from the current database (optional)
            stockField.setText(String.valueOf(getCurrentStock(productID)));
            priceField.setText(String.valueOf(getCurrentPrice(productID)));
            statusComboBox.getItems().addAll("Available", "Not Available");
            statusComboBox.setValue(getCurrentStatus(productID));  // Set current status
            imageField.setText(getCurrentImage(productID));  // Set current image path

            // Add placeholders to stock and image fields
            stockField.setPromptText("Enter stock (e.g., 10)");
            imageField.setPromptText("Enter image path (e.g., /images/product.jpg)");

            // Image update checkbox and disable the image field initially
            updateImageCheckBox.setSelected(false);
            imageField.setDisable(true);

            // Add listener to checkbox to enable/disable the image field and file chooser
            updateImageCheckBox.setOnAction(e -> {
                if (updateImageCheckBox.isSelected()) {
                    imageField.setDisable(false);  // Enable the image field if checked
                } else {
                    imageField.setDisable(true);  // Disable the image field if unchecked
                    imageField.clear();  // Clear the image field if user does not want to update the image
                }
            });

            // Add file chooser to select image path
            imageField.setOnMouseClicked(event -> openFileChooser(event));

            // Create the dialog layout
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    new Label("Stock:"), stockField,
                    new Label("Price:"), priceField,
                    new Label("Status:"), statusComboBox,
                    updateImageCheckBox, // Checkbox to update image
                    new Label("Image Path:"), imageField
            );

            getDialogPane().setContent(vbox);

            // Add buttons
            ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);

            // Convert the user's input to a ProductDetails object on submit
            setResultConverter(dialogButton -> {
                if (dialogButton == updateButton) {
                    return new ProductDetails(
                            stockField.getText(),
                            priceField.getText(),
                            statusComboBox.getValue(),
                            imageField.getText()
                    );
                }
                return null;
            });
        }

        // Method to open a file chooser dialog
        private void openFileChooser(MouseEvent event) {
            if (updateImageCheckBox.isSelected()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
                fileChooser.setTitle("Select an Image");

                // Show file chooser and capture the selected file
                java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    imageField.setText(selectedFile.getAbsolutePath());
                }
            }
        }
    }



    // Helper class to store the product details
    public static class ProductDetails {
        private String stock;
        private String price;
        private String status;
        private String image;

        public ProductDetails(String stock, String price, String status, String image) {
            this.stock = stock;
            this.price = price;
            this.status = status;
            this.image = image;
        }

        public String getStock() {
            return stock;
        }

        public String getPrice() {
            return price;
        }

        public String getStatus() {
            return status;
        }

        public String getImage() {
            return image;
        }
    }

    // Helper methods for getting current values from the database
    private static int getCurrentStock(String productID) {
        // Fetch the current stock from the database
        // Placeholder code for database query, replace with your actual code
        return 0;  // Return the current stock value from your database
    }

    private static double getCurrentPrice(String productID) {
        // Fetch the current price from the database
        // Placeholder code for database query, replace with your actual code
        return 0.0;  // Return the current price value from your database
    }

    private static String getCurrentStatus(String productID) {
        // Fetch the current status from the database
        // Placeholder code for database query, replace with your actual code
        return "Available";  // Return the current status value from your database
    }

    private static String getCurrentImage(String productID) {
        // Fetch the current image path from the database
        // Placeholder code for database query, replace with your actual code
        return "default_image.jpg";  // Return the current image path from your database
    }

//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void inventoryDeleteBtn() {
        // Database connection setup
        Connection connect = null;
        try {
            // Replace with your actual database URL, username, and password
            String url = "jdbc:mysql://localhost:3306/your_database_name";
            String username = "your_database_user";
            String password = "your_database_password";
            connect = DriverManager.getConnection(url, username, password);

            if (inventory_productID.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Please provide a valid Product ID to delete.");
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to DELETE Product ID: " + inventory_productID.getText().trim() + "?");

                Optional<ButtonType> option = alert.showAndWait();

                if (option.isPresent() && option.get() == ButtonType.OK) {
                    String deleteData = "DELETE FROM products WHERE prod_id = ?";
                    try (PreparedStatement prepare = connect.prepareStatement(deleteData)) {
                        prepare.setString(1, inventory_productID.getText().trim());

                        int rowsAffected = prepare.executeUpdate();
                        if (rowsAffected > 0) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Product successfully deleted!");
                            // Update the table view and clear fields
                            inventoryShowData();
                            inventoryClearBtn();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error Message", "No product was deleted. Please check the Product ID.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while deleting the product.");
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Delete operation was cancelled.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect to the database.");
        } finally {
            // Ensure the connection is closed
            if (connect != null) {
                try {
                    connect.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void inventoryClearBtn() {



// Replace the original line with the following
        inventory_productID.setText("");
        inventory_productName.setText("");
        inventory_stock.setText("");
        inventory_price.setText("");
        inventory_status.getSelectionModel().clearSelection();
        data.path = "";
        data.id = 0;
        inventory_imageView.setImage(null);

    }

    @FXML
    private void importImageClicked(MouseEvent event) {

        // Assuming you use a FileChooser to select an image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Set the image in the ImageView
            Image image = new Image(selectedFile.toURI().toString());
            inventory_imageView.setImage(image);

            // Set the path for validation
            data.path = selectedFile.getAbsolutePath();

        }
    }


    // MERGE ALL DATAS
    public void inventoryShowData() {
        // Fetch data from the database and populate it into an ObservableList
        ObservableList<productData> inventoryListData = inventoryDataList();

        if (inventoryListData == null) {
            System.out.println("No data available for inventory.");
            return;
        }

        // Set up the TableView columns to correspond with the productData fields
        inventory_col_productID.setCellValueFactory(new PropertyValueFactory<>("productId"));
        inventory_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventory_col_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        inventory_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventory_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Bind the data to the TableView
        inventory_tableView.setItems(inventoryListData);
    }

    private ObservableList<productData> inventoryDataList() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT product_id, product_name, stock, price, status, image, date FROM products";

        try (Connection connect = Database.connectDB();
             PreparedStatement prepare = connect.prepareStatement(sql);
             ResultSet result = prepare.executeQuery()) {

            while (result.next()) {
                // Map data from ResultSet to productData object
                productData prodData = new productData(
                        result.getString("product_id"),
                        result.getString("product_name"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("status"),
                        result.getString("image"),
                        result.getDate("date")
                );

                // Add productData to list
                listData.add(prodData);
            }
        } catch (SQLException e) {
            // Use a logging framework for production systems
            System.err.println("Error fetching inventory data: " + e.getMessage());
        }

        return listData;
    }


    // TO SHOW DATA ON OUR TABLE
    private ObservableList<productData> inventoryListData;



    public void inventorySelectData() {

        productData prodData = inventory_tableView.getSelectionModel().getSelectedItem();
        int num = inventory_tableView.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) {
            return;
        }

        // Ensure inventory_productID is correctly initialized as noted above
        inventory_productID.setText(prodData.getProductId());
        inventory_productName.setText(prodData.getProductName());
        inventory_stock.setText(String.valueOf(prodData.getStock()));
        inventory_price.setText(String.valueOf(prodData.getPrice()));

        data.path = prodData.getImage();

        String path = "File:" + prodData.getImage();
        data.date = String.valueOf(prodData.getDate());
//        data.id = prodData.getId();

        image = new Image(path, 120, 127, false, true);
        inventory_imageView.setImage(image);
    }


    private String[] statusList = {"Available", "Unavailable"};


    public void inventoryStatusList() {

        List<String> statusL = new ArrayList<>();

        for (String data : statusList) {
            statusL.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(statusL);
        inventory_status.setItems(listData);

    }

    // Parse order details from the file
    private void parseOrderDetails(String line, Stack<OrderItem> currentStack) {
        if (line.startsWith("Bundle Code:")) {
            currentStack.push(new OrderItem("", 0, "", 0, "", 0, "", line.substring(12).trim()));
        } else if (line.startsWith("Pizza Name:") && !currentStack.isEmpty()) {
            currentStack.peek().setPizzaName(line.substring(11).trim());
        } else if (line.startsWith("Drink Name:") && !currentStack.isEmpty()) {
            currentStack.peek().setDrinkName(line.substring(11).trim());
        } else if (line.startsWith("Price:") && !currentStack.isEmpty()) {
            currentStack.peek().setTotalPrice(line.substring(6).trim());
        } else if (line.startsWith("Quantity:") && !currentStack.isEmpty()) {
            currentStack.peek().setPizzaQuantity(Integer.parseInt(line.substring(9).trim()));
        }
    }


    @FXML
    public void inventoryAddBtn() {
        try {
            // Validate if input fields are properly filled
            if (!isInputValid()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill all required fields.");
                return;
            }

            // Check if the product ID already exists in the database
            String checkProdID = "SELECT product_id FROM products WHERE product_id = ?";
            try (Connection connect = Database.connectDB();
                 PreparedStatement checkStmt = connect.prepareStatement(checkProdID)) {

                if (connect == null) {
                    throw new SQLException("Failed to connect to the database.");
                }

                checkStmt.setString(1, inventory_productID.getText().trim());
                try (ResultSet result = checkStmt.executeQuery()) {
                    if (result.next()) {
                        showAlert(Alert.AlertType.ERROR, "Duplicate Error",
                                "Product ID " + inventory_productID.getText().trim() + " is already taken.");
                        return;
                    }
                }
            }

            // If Product ID does not exist, add the product
            addProductToDatabase();

            addProductToPizzaPane();

            inventoryShowData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An SQL error occurred: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }
    private void addProductToPizzaPane() {
        try {
            // Load the product card FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/views/productCard.fxml"));
            Parent productCardParent = loader.load();

            // Assuming you have a ProductCardController that has a method to set product details
            ProductCardController productCardController = loader.getController();
            productCardController.setProductDetails(inventory_productID.getText().trim(),

                    inventory_productName.getText().trim(),
                    inventory_price.getText().trim(),
                    inventory_imageView.getImage());

            // Add the new product card to the pizza pane (assuming pizzaPane is the container in OrderMenu.fxml)
            PizzaPane.getChildren().add(productCardParent);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load product card.");
        }
    }
    public class ProductCardController {
        @FXML
        private Text productID;
        @FXML
        private Text productName;
        @FXML
        private Text price;
        @FXML
        private ImageView productImage;

        public void setProductDetails(String id, String name, String price, Image image) {
            this.productID.setText(id);
            this.productName.setText(name);
            this.price.setText(price);
            this.productImage.setImage(image);
        }
    }

    private void addProductToDatabase() {
        String insertData = "INSERT INTO products (product_id, product_name, stock, price, status, image, date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connect = Database.connectDB();
             PreparedStatement insertStmt = connect.prepareStatement(insertData)) {

            if (connect == null) {
                throw new SQLException("Failed to connect to the database.");
            }

            // Set parameters for the query
            insertStmt.setString(1, inventory_productID.getText().trim());
            insertStmt.setString(2, inventory_productName.getText().trim());
            insertStmt.setInt(3, parseIntegerField(inventory_stock.getText().trim(), "Stock"));
            insertStmt.setDouble(4, parseDoubleField(inventory_price.getText().trim(), "Price"));
            insertStmt.setString(5, inventory_status.getSelectionModel().getSelectedItem());

            if (data.path == null || data.path.isEmpty()) {
                throw new IllegalArgumentException("Product image path is not set.");
            }

            insertStmt.setString(6, data.path.replace("\\", "\\\\"));
            insertStmt.setDate(7, new java.sql.Date(System.currentTimeMillis()));

            // Execute the query
            insertStmt.executeUpdate();

            // Notify success
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");

            // Refresh inventory data and clear input fields
            inventoryShowData();
            inventoryClearBtn();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An SQL error occurred while adding the product: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Input Error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private boolean isInputValid() {
        System.out.println("Product ID: " + inventory_productID.getText().trim());
        System.out.println("Product Name: " + inventory_productName.getText().trim());
        System.out.println("Stock: " + inventory_stock.getText().trim());
        System.out.println("Price: " + inventory_price.getText().trim());
        System.out.println("Image Path: " + data.path);
        System.out.println("Image: " + inventory_imageView.getImage());

        return !inventory_productID.getText().trim().isEmpty() &&
                !inventory_productName.getText().trim().isEmpty() &&
                !inventory_stock.getText().trim().isEmpty() &&
                !inventory_price.getText().trim().isEmpty() &&
                inventory_status.getSelectionModel().getSelectedItem() != null &&
                data.path != null &&
                inventory_imageView.getImage() != null;
    }


    private int parseIntegerField(String input, String fieldName) {
        try {
            int value = Integer.parseInt(input);
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " must be a positive number.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer.");
        }
    }

    private double parseDoubleField(String input, String fieldName) {
        try {
            double value = Double.parseDouble(input);
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " must be a positive number.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid decimal number.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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