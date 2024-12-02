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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.PolicyNode;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import static com.mysql.cj.conf.PropertyKey.logger;
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
        if (!isInputValid()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all required fields.");
            return;
        }

        String checkProdID = "SELECT prod_id FROM products WHERE prod_id = ?";
        try (Connection connect = Database.connectDB();
             PreparedStatement checkStmt = connect.prepareStatement(checkProdID)) {

            checkStmt.setString(1, inventory_productID.getText().trim());
            try (ResultSet result = checkStmt.executeQuery()) {
                if (!result.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error Message",
                            "Product ID " + inventory_productID.getText().trim() + " does not exist.");
                } else {
                    updateProductInDatabase(connect);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the product.");
//            logger.log(Level.SEVERE, "Database error while updating product", e);
        }
    }

    private void updateProductInDatabase(Connection connect) {
        String updateData = "UPDATE products SET product_name = ?, stock = ?, price = ?, status = ?, image = ?, date = ? "
                + "WHERE prod_id = ?";
        try (PreparedStatement updateStmt = connect.prepareStatement(updateData)) {
            updateStmt.setString(1, inventory_productName.getText().trim());
            updateStmt.setInt(2, parseIntegerField(inventory_stock.getText().trim(), "Stock"));
            updateStmt.setDouble(3, parseDoubleField(inventory_price.getText().trim(), "Price"));
            updateStmt.setString(4, inventory_status.getSelectionModel().getSelectedItem());
            updateStmt.setString(5, data.path.replace("\\", "\\\\"));
            updateStmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
            updateStmt.setString(7, inventory_productID.getText().trim());

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
//            logger.log(Level.SEVERE, "Database error while updating product", e);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Input Error", ex.getMessage());
        }
    }


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