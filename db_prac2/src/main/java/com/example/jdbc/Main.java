package com.example.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    // UI

    // MySQL connection info
    private static final String URL = "jdbc:mysql://localhost:3306/pos_prac";
    private static final String USER = "plzcr"; // Enter your ID with privileges
    private static final String PASSWORD = "GhalGhal3690"; // Enter your PW of the ID

    public static void main(String[] args) {
        // JDBC connection and dealing with data
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("successfully connected to MySQL DB!");

            // university DB from professor (not complete ver)
            // examples of accessing data
            String selectSQL = "SELECT * FROM advisor ORDER BY s_ID";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(selectSQL)) {
                while (resultSet.next()) {
                    System.out.println("s_ID: " + resultSet.getInt("s_ID") +
                            ", i_ID: " + resultSet.getString("i_id"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
