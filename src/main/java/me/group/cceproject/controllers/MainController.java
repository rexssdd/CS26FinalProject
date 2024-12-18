package me.group.cceproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class MainController {
    private String orderType; // Holds the current order type

    @FXML
    private void dineinClick(MouseEvent event) {
        orderType = "Dine In"; // Set the order type
        loadOrderMenu(orderType, event); // Pass it to the next screen if needed
    }

    @FXML
    private void takeoutClick(MouseEvent event) {
        orderType = "Take Out"; // Set the order type
        loadOrderMenu(orderType, event); // Pass it to the next screen if needed
    }
    @FXML
    private void logoclicked(MouseEvent event){
        try {
            // Load the FXML file for the login panel
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/CreateAccount.fxml"));
            Parent loginPanelRoot = loader.load();

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new scene for the login panel
            Scene loginPanelScene = new Scene(loginPanelRoot);

            // Set the new scene and show it
            stage.setScene(loginPanelScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading LoginPanel.fxml: " + e.getMessage());
        }


    }
    private void loadOrderMenu(String orderType, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/OrderMenu.fxml"));
            Parent orderMenuRoot = loader.load();

            OrderMenuController orderMenuController = loader.getController();
            orderMenuController.setOrderType(orderType);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene orderMenuScene = new Scene(orderMenuRoot);
            stage.setScene(orderMenuScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading OrderMenu.fxml: " + e.getMessage());
        }
    }
}