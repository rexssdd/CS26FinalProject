package me.group.cceproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;

public class CreateAccountController {

    @FXML
    private TextField Username; // FXML ID for the Username field
    @FXML
    private PasswordField Password; // FXML ID for the PasswordField
    @FXML
    private ComboBox<String> Role; // FXML ID for the Role ComboBox

    public void initialize() {
        // Populate the ComboBox with roles
        Role.getItems().addAll("Admin", "Super Admin");
    }

    // Load SuperAdmin FXML
    private void SuperAdminLoad(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/SuperAdminMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading SuperAdminMain.fxml: " + e.getMessage());
        }
    }

    // Load Admin FXML
    private void AdminLoad(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/AdminMain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading AdminMain.fxml: " + e.getMessage());
        }
    }

    public void CreateAccountClicked(MouseEvent event) {
        String username = Username.getText();
        String password = Password.getText();
        String role = Role.getValue();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Validation Error", "All fields are required.");
            return;
        }

        // Save the account details to the database
        String url = "jdbc:mysql://localhost:3306/pizzaordering"; // Your database URL
        String dbUsername = "root"; // Database username
        String dbPassword = ""; // Database password

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO admin (Name, password, Type) VALUES (?, ?, ?)")) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role);

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                showAlert("Account Created", "Account successfully created for: " + username);

//                // Navigate to the appropriate screen based on the role
//                if ("Super Admin".equalsIgnoreCase(role)) {
//                    SuperAdminLoad(event);
//                } else if ("Admin".equalsIgnoreCase(role)) {
//                    AdminLoad(event);
//                }
            } else {
                showAlert("Account Creation Failed", "Failed to create the account. Please try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while saving the account: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void LoginClicked(MouseEvent event) {
        // Logic for navigating to the login panel
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/LoginPanel.fxml"));
            Parent loginPanelRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene loginPanelScene = new Scene(loginPanelRoot);
            stage.setScene(loginPanelScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading LoginPanel.fxml: " + e.getMessage());
        }
    }
}
