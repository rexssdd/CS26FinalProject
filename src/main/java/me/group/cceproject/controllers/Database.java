package me.group.cceproject.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
/**
 *
 * @author WINDOWS 10
 */
public class Database {

    public static Connection connectDB() {

        try {

            Class.forName("com.mysql.jdbc.Driver");

            Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/cafe", "root", ""); // LINK YOUR DATABASE
            return connect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

