package me.group.cceproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class LoginPanelController {

    @FXML
    private TextField Username; // FXML ID for the TextField
    @FXML
    private PasswordField Password; // FXML ID for the PasswordField

    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        System.out.println(alert.toString());
        alert.showAndWait();
    }

    public void loginclicked(MouseEvent event) {
        String username = Username.getText(); // Get the text from the Username field
        String password = Password.getText(); // Get the text from the Password field

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Establish the database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pizzaordering", "root", "");

            // Prepare the SQL query to validate credentials and get the admin type
            String sql = "SELECT type FROM admin WHERE name = ? AND password = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Fetch the admin type from the result set
                String adminType = resultSet.getString("type");

                // Load appropriate FXML based on admin type
                if ("Admin".equalsIgnoreCase(adminType)) {
                    showAlert("Login Successful", "Welcome, Admin!");
                    loadFXML("/me/group/cceproject/AdminMain.fxml", event);
                } else if ("Super Admin".equalsIgnoreCase(adminType)) {
                    loadFXML("/me/group/cceproject/SuperAdminMain.fxml", event);
                    showAlert("Login Successful", "Welcome, SuperAdmin!");

                } else {
                    // Handle unknown admin type
                    showAlert("Login Failed", "Unknown admin type.");
                }
            } else {
                // Invalid username or password
                showAlert("Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while connecting to the database: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to load the FXML based on admin type
    private void loadFXML(String fxmlFile, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            System.out.println(fxmlFile);
            stage.show();
        } catch (IOException e) {
            System.out.println(e);
            showAlert("Error", "Failed to load the page: " + e.getMessage());
        }
    }

    // Super Admin Login page switch if needed (not directly related to login but if you need it)
    public void SuperAdminClicked(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/group/cceproject/SuperAdminLogin.fxml"));
            Parent loginPanelRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene loginPanelScene = new Scene(loginPanelRoot);
            stage.setScene(loginPanelScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading SuperAdminLogin.fxml: " + e.getMessage());
        }
    }
}
