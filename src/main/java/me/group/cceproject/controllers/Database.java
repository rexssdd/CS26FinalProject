package me.group.cceproject.controllers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Database {
    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());

    public static Connection connectDB() {
        String url = "jdbc:mysql://localhost:3306/pizzaordering";
        String user = "root";
        String password = "";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to the database", e);
//            showAlert("Error", "Failed to connect to the database: " + e.getMessage());
            return null;
        }

    }
}


