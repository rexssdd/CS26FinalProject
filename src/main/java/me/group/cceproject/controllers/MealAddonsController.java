package me.group.cceproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;  // Import TextField for quantity input
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MealAddonsController {

    // Drink Panes
    @FXML
    private Pane CokePane;
    @FXML
    private Pane SpritePane;
    @FXML
    private Pane MtnDewPane;
    @FXML
    private Pane CheesePane;
    @FXML
    private Pane MeatPane;
    @FXML
    private Pane PepperoniPane;
    @FXML
    private Pane NoThanksPane;

    // Size Buttons
    @FXML
    private Button SmallButton;
    @FXML
    private Button MediumButton;
    @FXML
    private Button LargeButton;

    @FXML
    private ImageView PizzaPicture;
    @FXML
    private Text PizzaName;
    @FXML
    private Text PizzaPrice;
    @FXML
    private Button AddtoOrderButton;
    @FXML
    private Button CancelButton;

    @FXML
    private TextField drinksQtyNumber;
    @FXML
    private TextField pizzaQtyNumber;
    // TextField for quantity selection
    @FXML
    private TextField QuantityNumber;

    private String foodCode;
    private String selectedSize = "Small";  // Default size
    private String selectedDrink = "Coke";  // Default drink

    // Styles for drink panes
    private final String selectedPaneStyle = "-fx-border-color: #DB383D; -fx-border-width: 2; -fx-border-radius: 5;";
    private final String defaultPaneStyle = ""; // Default style for deselected panes

    // Styles for size buttons
    private final String selectedButtonStyle = "-fx-background-color: #DB383D; -fx-border-color: #DB383D; -fx-border-radius: 5; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String defaultButtonStyle = "-fx-background-color: #FFFFFF; -fx-border-color: #DB383D; -fx-border-radius: 5; -fx-text-fill: black; -fx-font-weight: normal;";

    @FXML
    public void initialize() {
        // Set CokePane and NoThanksPane as default selected panes
        CokePane.setStyle(selectedPaneStyle);
        NoThanksPane.setStyle(selectedPaneStyle);

        // Set SmallButton as the default selected button
        SmallButton.setStyle(selectedButtonStyle);
    }

    public void setMealDetails(String pizzaName, String pizzaPrice, String imagePath, String foodCode) {
        this.foodCode = foodCode;
        PizzaName.setText(pizzaName);
        PizzaPrice.setText(pizzaPrice);
        try {
            String resourcePath = "/me/group/cceproject/images/" + imagePath;
            Image image = new Image(getClass().getResource(resourcePath).toExternalForm());
            PizzaPicture.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// btns for quantity
    @FXML
   private void pizzaAddClicked(MouseEvent event) {
    try{
        int quantity = Integer.parseInt(pizzaQtyNumber.getText().trim());
        if (quantity < 100) {
            quantity++;
            pizzaQtyNumber.setText(String.valueOf(quantity));
        }
    }catch (Exception e){
        e.printStackTrace();
    }
    }
    @FXML
    private void pizzaMinusClicked(MouseEvent event) {
        try {
            int quantity = Integer.parseInt(pizzaQtyNumber.getText().trim());
            if (quantity > 0) {
                quantity--;
                pizzaQtyNumber.setText(String.valueOf(quantity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // drinks qty
    @FXML
    private void drinksAddClicked(MouseEvent event) {
        try {
            int quantity = Integer.parseInt(drinksQtyNumber.getText().trim());
            if (quantity < 100) {
                quantity++;
                drinksQtyNumber.setText(String.valueOf(quantity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     @FXML
     private void drinksMinusClicked (MouseEvent event){
        try{
            int quantity = Integer.parseInt(drinksQtyNumber.getText().trim());
            if (quantity > 0) {
                quantity--;
                drinksQtyNumber.setText(String.valueOf(quantity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
     }

     //addons
    @FXML
    private void addonsAddClicked(MouseEvent event) {
        try {
            int quantity = Integer.parseInt(QuantityNumber.getText().trim());
            if (quantity < 100) {
                quantity++;
                QuantityNumber.setText(String.valueOf(quantity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void addonsMinusClicked(MouseEvent event) {
        try {
            int quantity = Integer.parseInt(QuantityNumber.getText().trim());
            if (quantity > 0) {
                quantity--;
                QuantityNumber.setText(String.valueOf(quantity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //
    @FXML
    private void CokeClicked(MouseEvent event) {
        selectDrinkPane(CokePane);
        selectedDrink = "Coke";  // Update selected drink
    }

    @FXML
    private void SpriteClicked(MouseEvent event) {
        selectDrinkPane(SpritePane);
        selectedDrink = "Sprite";  // Update selected drink
    }

    @FXML
    private void MtnDewClicked(MouseEvent event) {
        selectDrinkPane(MtnDewPane);
        selectedDrink = "Mountain Dew";  // Update selected drink
    }

    @FXML
    private void CFClicked(MouseEvent event) {
        selectAddonPane(CheesePane);
    }

    @FXML
    private void CPClicked(MouseEvent event) {
        selectAddonPane(MeatPane);
    }

    @FXML
    private void RVClicked(MouseEvent event) {
        selectAddonPane(PepperoniPane);
    }

    @FXML
    private void NTClicked(MouseEvent event) {
        selectAddonPane(NoThanksPane);
    }

    @FXML
    private void SmallButtonClicked(MouseEvent event) {
        selectSize(SmallButton);
        selectedSize = "Small";  // Update selected size
    }

    @FXML
    private void MediumButtonClicked(MouseEvent event) {
        selectSize(MediumButton);
        selectedSize = "Medium";  // Update selected size
    }

    @FXML
    private void LargeButtonClicked(MouseEvent event) {
        selectSize(LargeButton);
        selectedSize = "Large";  // Update selected size
    }

    private void selectDrinkPane(Pane selectedDrinkPane) {
        CokePane.setStyle(defaultPaneStyle);
        SpritePane.setStyle(defaultPaneStyle);
        MtnDewPane.setStyle(defaultPaneStyle);
        selectedDrinkPane.setStyle(selectedPaneStyle);
    }

    private void selectAddonPane(Pane selectedAddonPane) {
        CheesePane.setStyle(defaultPaneStyle);
        MeatPane.setStyle(defaultPaneStyle);
        PepperoniPane.setStyle(defaultPaneStyle);
        NoThanksPane.setStyle(defaultPaneStyle);
        selectedAddonPane.setStyle(selectedPaneStyle);
    }

    private void selectSize(Button selectedButton) {
        SmallButton.setStyle(defaultButtonStyle);
        MediumButton.setStyle(defaultButtonStyle);
        LargeButton.setStyle(defaultButtonStyle);
        selectedButton.setStyle(selectedButtonStyle);
    }

    @FXML
    private void AddtoOrderClicked(MouseEvent event) {
        try {
            // Retrieve selected addon and calculate final price
            String selectedAddon = getSelectedAddon();
            double basePrice = extractPrice(PizzaPrice.getText());
            double finalPrice = calculateFinalPrice(basePrice, selectedSize, selectedAddon);

            // Get inputs for pizza, drink, and addon details
            String completepizzaName = PizzaName.getText();
            String selectedDrink = this.selectedDrink;
            int drinkQty = Integer.parseInt(drinksQtyNumber.getText().trim());
            String addon = selectedAddon;
            int addonQty = Integer.parseInt(QuantityNumber.getText().trim());
            String formattedPrice = String.format("₱ %.2f", finalPrice);
            int quantity = Integer.parseInt(pizzaQtyNumber.getText().trim());


            if (quantity < 1 || drinkQty < 1 || addonQty < 1) {
                return; // Exit if any quantity is invalid
            }

            OrderMenuController.addOrderItem(
                    completepizzaName,
                    quantity,
                    selectedDrink,
                    drinkQty,
                    addon,
                    addonQty,
                    formattedPrice,
                    foodCode
            );

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
        }
    }


    private String getSelectedAddon() {
        if (CheesePane.getStyle().contains("DB383D")) return "Cheese";
        if (MeatPane.getStyle().contains("DB383D")) return "Meat";
        if (PepperoniPane.getStyle().contains("DB383D")) return "Pepperoni";
        return "No Thanks";
    }

    private double extractPrice(String priceStr) {
        return Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
    }

    private double calculateFinalPrice(double basePrice, String size, String addon) {
        double finalPrice = basePrice;

        switch (size) {
            case "Medium":
                finalPrice += 20;
                break;
            case "Large":
                finalPrice += 40;
                break;
        }

        switch (addon) {
            case "Cheese":
                finalPrice += 39;
                break;
            case "Meat":
                finalPrice += 49;
                break;
            case "Pepperoni":
                finalPrice += 59;
                break;
        }

        return finalPrice;
    }

    @FXML
    private void CancelClicked(MouseEvent event) {
        try {
            // Load back the OrderMenu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/OrderMenu.fxml"));
            Parent orderMenuRoot = loader.load();

            // Get the controller and set the order type
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
}
