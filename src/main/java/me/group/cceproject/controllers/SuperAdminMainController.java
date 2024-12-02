package me.group.cceproject.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.PolicyNode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static me.group.cceproject.controllers.OrderMenuController.staticOrderItems;

public class SuperAdminMainController {


    private AnchorPane main_form;
    @FXML
    private TextField inputOrderNumber, OrderNumberInput, AmountTextField, QuantityField;
    @FXML
    private Text orderTotalText, TotalText, ChangeText;
    @FXML
    private ComboBox<String> ProductStatusComboBox;
    @FXML
    private TableView<OrderSummary> products;
    @FXML
    private TableColumn<OrderSummary, String> productId;
    @FXML
    private TableColumn<OrderSummary, String> orderNumberColumn, orderTotalColumn, orderStatusColumn;
    @FXML
    private TableView<OrderItem> OrdersTable;
    @FXML
    private TableColumn<OrderItem, String> BundleId, PizzaName, TotalPrice, DrinkName, AddonsName;
    @FXML
    private TableColumn<OrderItem, Integer> PizzaQuantity, DrinkQuantity;
    @FXML
    private VBox productsContainer;


    @FXML
    private TableView<productData> inventory_tableView;

    @FXML
    private TableColumn<productData, String> inventory_col_productID;

    @FXML
    private TableColumn<productData, String> inventory_col_productName;

    @FXML
    private TableColumn<productData, String> inventory_col_type;

    @FXML
    private TableColumn<productData, String> inventory_col_stock;

    @FXML
    private TableColumn<productData, String> inventory_col_price;

    @FXML
    private TableColumn<productData, String> inventory_col_status;

    @FXML
    private TableColumn<productData, String> inventory_col_date;
    @FXML
    private ComboBox<String> inventory_type;

    @FXML
    private TextField inventory_stock;
    @FXML
    private ImageView inventory_imageView;

    private Alert alert;

    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private TextField inventory_productID;
    private Image image;
    private TableView<OrderSummary> orderTableView;

    private ObservableList<productData> cardListData = FXCollections.observableArrayList();

    private TextField inventory_productName;
    private static final String ORDER_FILE = "orders.txt";

    private final Queue<OrderSummary> orderQueue = new LinkedList<>();
    private final Map<String, Stack<OrderItem>> orderStacks = new HashMap<>();
    private final Map<String, Double> productPrices = new HashMap<>();

    @FXML
    public void initialize() {

        loadOrders();
        inventoryStatusList();
        inventoryShowData();
    }

    @FXML
    private ComboBox<String> inventory_status;

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Load orders from file
    private void loadOrders() {
        ObservableList<OrderSummary> orders = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDER_FILE))) {
            String line;
            OrderSummary currentOrder = null;
            Stack<OrderItem> currentStack = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.matches("\\d{4}: Order Items")) {
                    if (currentOrder != null && currentStack != null) {
                        orders.add(currentOrder);
                        orderQueue.offer(currentOrder);
                        orderStacks.put(currentOrder.getOrderNumber(), currentStack);
                    }
                    String orderNumber = line.split(":")[0];
                    currentOrder = new OrderSummary(orderNumber);
                    currentStack = new Stack<>();
                } else if (currentStack != null) {
                    parseOrderDetails(line, currentStack);
                } else if (line.startsWith("Total Price:") && currentOrder != null) {
                    currentOrder.setOrderTotal(line.substring(12).trim());
                } else if (line.startsWith("Status:") && currentOrder != null) {
                    currentOrder.setOrderStatus(line.substring(7).trim());
                }
            }
            if (currentOrder != null && currentStack != null) {
                orders.add(currentOrder);
                orderQueue.offer(currentOrder);
                orderStacks.put(currentOrder.getOrderNumber(), currentStack);
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load orders: " + e.getMessage());
        }
        orderTableView.setItems(orders);
    }

    public void inventoryDeleteBtn() {
        if (data.id == 0) {

            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();

        } else {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to DELETE Product ID: " + inventory_productID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get().equals(ButtonType.OK)) {
                String deleteData = "DELETE FROM products WHERE id = " + data.id;
                try {
                    prepare = connect.prepareStatement(deleteData);
                    prepare.executeUpdate();

                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("successfully Deleted!");
                    alert.showAndWait();

                    // TO UPDATE YOUR TABLE VIEW
                    inventoryShowData();
                    // TO CLEAR YOUR FIELDS
                    inventoryClearBtn();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Cancelled");
                alert.showAndWait();
            }
        }
    }

    public void inventoryClearBtn() {


        // Then in the constructor or initializer:
        inventory_productID = new TextField();
        inventory_productName = new TextField();  // Initialize in constructor
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

    // LETS MAKE A BEHAVIOR FOR IMPORT BTN FIRST
    public void inventoryImportBtn() {

        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Open Image File", "*png", "*jpg"));

        File file = openFile.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {

            data.path = file.getAbsolutePath();
            image = new Image(file.toURI().toString(), 120, 127, false, true);

            inventory_imageView.setImage(image);
        }
    }

    // MERGE ALL DATAS
    public ObservableList<productData> inventoryDataList() {

        ObservableList<productData> listData = FXCollections.observableArrayList();

        String sql = "SELECT * FROM products";

        connect = Database.connectDB();

        try {

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            productData prodData;

            while (result.next()) {

                prodData = new productData(result.getInt("id"),
                        result.getString("prod_id"),
                        result.getString("prod_name"),
                        result.getString("type"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("status"),
                        result.getString("image"),
                        result.getDate("date"));

                listData.add(prodData);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;
    }

    // TO SHOW DATA ON OUR TABLE
    private ObservableList<productData> inventoryListData;

    public void inventoryShowData() {
        inventoryListData = inventoryDataList();

        inventory_col_productID.setCellValueFactory(new PropertyValueFactory<>("productId"));
        inventory_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventory_col_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        inventory_col_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        inventory_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventory_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));

        inventory_tableView.setItems(inventoryListData);

    }

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
        data.id = prodData.getId();

        image = new Image(path, 120, 127, false, true);
        inventory_imageView.setImage(image);
    }


    private String[] statusList = {"Available", "Unavailable"};
    @FXML
    private TextField inventory_price;


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


    public void inventoryAddBtn() {

        if (inventory_productID.getText().isEmpty()
                || inventory_productName.getText().isEmpty()
                || inventory_stock.getText().isEmpty()
                || inventory_price.getText().isEmpty()
                || inventory_status.getSelectionModel().getSelectedItem() == null
                || data.path == null) {

            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();

        } else {

            // CHECK PRODUCT ID
            String checkProdID = "SELECT prod_id FROM product WHERE prod_id = '"
                    + inventory_productID.getText() + "'";

            connect = Database.connectDB();

            try {

                statement = connect.createStatement();
                result = statement.executeQuery(checkProdID);

                if (result.next()) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText(inventory_productID.getText() + " is already taken");
                    alert.showAndWait();
                } else {
                    String insertData = "INSERT INTO products "
                            + "(product_id, product_name, stock, price, status, image, date) "
                            + "VALUES(?,?,?,?,?,?,?)";

                    prepare = connect.prepareStatement(insertData);
                    prepare.setString(1, inventory_productID.getText());
                    prepare.setString(2, inventory_productName.getText());
                    prepare.setString(3, inventory_stock.getText());
                    prepare.setString(4, inventory_price.getText());
                    prepare.setString(5, (String) inventory_status.getSelectionModel().getSelectedItem());

                    String path = data.path;
                    path = path.replace("\\", "\\\\");

                    prepare.setString(7, path);

                    // TO GET CURRENT DATE
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                    prepare.setString(7, String.valueOf(sqlDate));

                    prepare.executeUpdate();

                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Added!");
                    alert.showAndWait();

                    inventoryShowData();
                    inventoryClearBtn();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



        // Update the status of an order
        // Ensure the controller class is properly defined as a JavaFX controller and the FXML annotation is in the right place
        @FXML
        private void UpdateStatusClicked(MouseEvent event){
            String orderNumberInput = inputOrderNumber.getText();
            String newStatus = ProductStatusComboBox.getValue();

            if (orderNumberInput.isEmpty() || newStatus == null) {
                showAlert("Error", "Order number or status not selected");
                return;
            }

        }


        }

