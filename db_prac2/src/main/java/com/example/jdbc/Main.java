package com.example.jdbc;

import java.sql.*;
import java.util.Scanner;

public class Main {
    // MySQL connection info
    private static final String URL = "jdbc:mysql://localhost:3306/pos_db";
    private static final String USER = "plzcr"; // Enter your ID with privileges
    private static final String PASSWORD = "GhalGhal3690"; // Enter your PW of the ID

    public static void main(String[] args) {
        // JDBC connection and dealing with data
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("successfully connected to MySQL DB!");

            System.out.print("Enter your ID: ");
            Scanner scanner = new Scanner(System.in);
            String ID = scanner.nextLine();

            String buffer;
            int position_id, worker_id, store_id;

            /* get worker_name and position_name */
            String bufferSQL = "SELECT w.worker_id, w.worker_name, wp.position_id, wp.position_name\n" +
                    "FROM worker w\n" +
                    "JOIN worker_position wp ON w.position_id = wp.position_id\n" +
                    "WHERE w.worker_code = '" + ID + "';";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                if (resultSet.next()) { // resultSet이 비어있지 않으면 실행
                    buffer = resultSet.getString("position_name") + " " + resultSet.getString("worker_name");
                    position_id = resultSet.getInt("position_id");
                    worker_id = resultSet.getInt("worker_id");
                } else {
                    System.out.println("No matching record found for worker_code: " + ID);
                    return;
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Not a legal access");
                return;
            }

            /* logged-in */
            System.out.println("Welcome! " + buffer + "!");


            /* get store_id */
            bufferSQL = "SELECT store_id\n" +
                    "FROM work_for\n" +
                    "WHERE worker_id = " + worker_id + ";";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                if (resultSet.next()) { // resultSet이 비어있지 않으면 실행
                    store_id = resultSet.getInt("store_id");
                } else {
                    System.out.println("No matching record found for worker_code: " + ID);
                    return;
                }
            }

            int user_input;
            while (true) {
                /* choose among three */
                System.out.print("[1] check inventory\n" /* 재고 조회 */ +
                        "[2] sale\n" /* 매출 조회[업데이트] */ +
                        "[3] purchase/delivery\n" /* 매입/배송 조회[업데이트] */ +
                        "[4] check money\n" /* 금고 조회 */ +
                        "[5] exit\n" +
                        "Enter your command: ");
                user_input = scanner.nextInt();
                System.out.print("\n\n");

                switch (user_input){
                    case 1:
                        /* 현재 재고 조회 */
                        bufferSQL = "SELECT p.prod_id, p.prod_name, b.brand_name, i.inventory\n" +
                                "FROM product p\n" +
                                "JOIN prod_brand b ON p.brand_id = b.brand_id\n" +
                                "JOIN inventory i ON p.prod_id = i.prod_id\n" +
                                "WHERE i.store_id = " + store_id + ";";

                        /* print the inventory */
                        try (Statement statement = connection.createStatement();
                             ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                            System.out.printf("%-10s %-40s %-20s %-10s%n", "prod_id", "prod_name", "brand_name", "inventory");
                            System.out.println("--------------------------------------------------------------");

                            while (resultSet.next()) {
                                int prodId = resultSet.getInt("prod_id");
                                String prodName = resultSet.getString("prod_name");
                                String brandName = resultSet.getString("brand_name");
                                int inventory = resultSet.getInt("inventory");

                                System.out.printf("%-10d %-40s %-20s %-10d%n", prodId, prodName, brandName, inventory);
                            }
                        }
                        break;
                    case 2:
                        /* 매출 화면 */
                        System.out.print("[1] sell\n" /* 판매(결제) */ +
                                "[2] refund\n" /* 환불 */ +
                                "[3] return to the menu\n" +
                                "Enter your command: ");
                        user_input = scanner.nextInt();
                        System.out.print("\n\n");

                        switch (user_input){
                            case 1:
                                /* 판매(결제) */

                                /* 재고 조회 */
                                bufferSQL = "SELECT p.prod_id, p.prod_name, b.brand_name, i.inventory\n" +
                                        "FROM product p\n" +
                                        "JOIN prod_brand b ON p.brand_id = b.brand_id\n" +
                                        "JOIN inventory i ON p.prod_id = i.prod_id\n" +
                                        "WHERE i.store_id = " + store_id + ";";

                                /* print the inventory */
                                try (Statement statement = connection.createStatement();
                                     ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                                    System.out.printf("%-10s %-40s %-20s %-10s%n", "prod_id", "prod_name", "brand_name", "inventory");
                                    System.out.println("--------------------------------------------------------------");

                                    while (resultSet.next()) {
                                        int prodId = resultSet.getInt("prod_id");
                                        String prodName = resultSet.getString("prod_name");
                                        String brandName = resultSet.getString("brand_name");
                                        int inventory = resultSet.getInt("inventory");

                                        System.out.printf("%-10d %-40s %-20s %-10d%n", prodId, prodName, brandName, inventory);
                                    }
                                }

                                /* 상품 입력 */
                                System.out.print("Which product?: ");
                                int prod_id = scanner.nextInt();

                                /* 회원 입력 */
                                System.out.print("Is a member?: ");
                                user_input = scanner.nextInt();
                                System.out.print("\n\n");

                                /* Update the inventory */
                                bufferSQL = "UPDATE inventory\n" +
                                        "SET inventory = inventory - 1\n" +
                                        "WHERE store_id = " + store_id + " AND prod_id = " + prod_id + " AND inventory > 0;";

                                try (Connection conn1 = DriverManager.getConnection(URL, USER, PASSWORD);
                                     PreparedStatement pstmt = conn1.prepareStatement(bufferSQL)) {

                                    int rowsAffected = pstmt.executeUpdate();

                                    if (rowsAffected > 0) {
                                        /* money update */
                                        // SQL query to get retail_price from the product table
                                        String getRetailPriceQuery = "SELECT retail_price FROM product WHERE prod_id = " + prod_id;

                                        // SQL query to update the money table
                                        String updateMoneyQuery = "UPDATE money SET money = money + ? WHERE store_id = " + store_id;

                                        try (Connection conn2 = DriverManager.getConnection(URL, USER, PASSWORD);
                                             // Get the retail_price
                                             PreparedStatement getRetailPriceStmt = conn2.prepareStatement(getRetailPriceQuery);
                                             ResultSet rs = getRetailPriceStmt.executeQuery()) {

                                            // Check if the product exists
                                            if (rs.next()) {
                                                int retailPrice = rs.getInt("retail_price");

                                                // Prepare the update query
                                                try (PreparedStatement updateStmt = conn2.prepareStatement(updateMoneyQuery)) {
                                                    // Set the retail_price as the value to add to money
                                                    updateStmt.setInt(1, retailPrice);

                                                    // Execute the update query
                                                    rowsAffected = updateStmt.executeUpdate();
                                                    if (rowsAffected > 0) {
                                                        System.out.println("Money updated successfully.");
                                                    } else {
                                                        System.out.println("No rows updated.");
                                                    }
                                                }
                                            }

                                        }

                                        System.out.print("Payment succeeded. Inventory updated. Money updated");
                                        if (user_input != 0)
                                            System.out.print("Points updated.");
                                        System.out.print("\n");
                                    }
                                }

                                break;

                            case 2:
                                /* 환불 */

                                /* 판매한 상품 조회 */
                                String query = "SELECT sr.sale_id, s.sale_date_time, p.prod_name, sr.quantity " +
                                        "FROM sale_record sr " +
                                        "JOIN sale s ON sr.sale_id = s.sale_id " +
                                        "JOIN product p ON sr.prod_id = p.prod_id " +
                                        "WHERE s.store_id = " + store_id + ";";

                                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                                    // Execute the query
                                    try (ResultSet rs = pstmt.executeQuery()) {
                                        // Print the result as a table
                                        System.out.printf("%-10s %-20s %-40s %-10s%n", "sale_id", "sale_date_time", "prod_name", "quantity");
                                        System.out.println("----------------------------------------------------------------------");

                                        while (rs.next()) {
                                            int saleId = rs.getInt("sale_id");
                                            Timestamp saleDateTime = rs.getTimestamp("sale_date_time");
                                            String prodName = rs.getString("prod_name");
                                            int quantity = rs.getInt("quantity");

                                            System.out.printf("%-10d %-20s %-40s %-10d%n", saleId, saleDateTime, prodName, quantity);
                                        }
                                    }
                                }

                                /* sale_id 입력 */
                                System.out.print("Which sale_id?: ");
                                int sale_id = scanner.nextInt();

                                System.out.println("Refund of sale_id " + sale_id + " is completed. Points updated. Money updated.");

                                break;
                            case 3:
                                break;
                        }
                        break;
                    case 3:
                        /* 매입 */
                        if (position_id == 3) { // if the worker is a normal employee
                            System.out.println("You don't have the privileges");
                        }
                        else{
                            System.out.print("[1] purchase\n" /* 매입 */ +
                                    "[2] check whether the delivery is completed\n" /* 매입에 따른 배송 여부 확인 */ +
                                    "[3] recall\n" /* 매입환출 */ +
                                    "[4] return to the menu\n" +
                                    "Enter your command: ");
                            user_input = scanner.nextInt();
                            System.out.print("\n\n");

                            switch (user_input){
                                case 1:
                                    /* 매입 */

                                    /* 재고 조회 */
                                    bufferSQL = "SELECT p.prod_id, p.prod_name, b.brand_name, i.inventory\n" +
                                            "FROM product p\n" +
                                            "JOIN prod_brand b ON p.brand_id = b.brand_id\n" +
                                            "JOIN inventory i ON p.prod_id = i.prod_id\n" +
                                            "WHERE i.store_id = " + store_id + ";";

                                    /* print the inventory */
                                    try (Statement statement = connection.createStatement();
                                         ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                                        System.out.printf("%-10s %-40s %-20s %-10s%n", "prod_id", "prod_name", "brand_name", "inventory");
                                        System.out.println("--------------------------------------------------------------");

                                        while (resultSet.next()) {
                                            int prodId = resultSet.getInt("prod_id");
                                            String prodName = resultSet.getString("prod_name");
                                            String brandName = resultSet.getString("brand_name");
                                            int inventory = resultSet.getInt("inventory");

                                            System.out.printf("%-10d %-40s %-20s %-10d%n", prodId, prodName, brandName, inventory);
                                        }
                                    }

                                    /* 상품 입력 */
                                    System.out.print("Which product?: ");
                                    int prod_id = scanner.nextInt();
                                    System.out.print("\n\n");

                                    /* Update the inventory */
                                    bufferSQL = "UPDATE inventory\n" +
                                            "SET inventory = inventory - 1\n" +
                                            "WHERE store_id = " + store_id + " AND prod_id = " + prod_id + " AND inventory > 0;";

                                    try (Connection conn1 = DriverManager.getConnection(URL, USER, PASSWORD);
                                         PreparedStatement pstmt = conn1.prepareStatement(bufferSQL)) {

                                        int rowsAffected = pstmt.executeUpdate();

                                        if (rowsAffected > 0) {
                                            /* money update */
                                            // SQL query to get retail_price from the product table
                                            String getWholesalePriceQuery = "SELECT wholesale_price FROM product WHERE prod_id = " + prod_id;

                                            // SQL query to update the money table
                                            String updateMoneyQuery = "UPDATE money SET money = money - ? WHERE store_id = " + store_id;

                                            try (Connection conn2 = DriverManager.getConnection(URL, USER, PASSWORD);
                                                 // Get the wholesale_price
                                                 PreparedStatement getWholesalePriceStmt = conn2.prepareStatement(getWholesalePriceQuery);
                                                 ResultSet rs = getWholesalePriceStmt.executeQuery()) {

                                                // Check if the product exists
                                                if (rs.next()) {
                                                    int wholesalePrice = rs.getInt("wholesale_price");

                                                    // Prepare the update query
                                                    try (PreparedStatement updateStmt = conn2.prepareStatement(updateMoneyQuery)) {
                                                        // Set the retail_price as the value to add to money
                                                        updateStmt.setInt(1, wholesalePrice);

                                                        // Execute the update query
                                                        rowsAffected = updateStmt.executeUpdate();
                                                        if (rowsAffected > 0) {
                                                            System.out.println("Successful purchasing. Inventory updated. Money updated. Delivery updated.");
                                                        } else {
                                                            System.out.println("No rows updated.");
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    break;
                                case 2:
                                    /* 매입에 따른 배송 여부 확인 */

                                    /* 매입한 상품 조회 */
                                    // SQL query
                                    String query1 = "SELECT pr.pur_id, p.pur_date_time, prod.prod_name, pr.quantity, " +
                                            "IF(d.is_completed, 'true', 'false') AS is_completed " +
                                            "FROM purchase_record pr " +
                                            "JOIN purchase p ON pr.pur_id = p.pur_id " +
                                            "JOIN product prod ON pr.prod_id = prod.prod_id " +
                                            "LEFT JOIN delivery d ON p.pur_id = d.pur_id " +
                                            "WHERE p.store_id = " + store_id;

                                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                                         PreparedStatement pstmt = conn.prepareStatement(query1)) {

                                        // Execute the query
                                        try (ResultSet rs = pstmt.executeQuery()) {
                                            // Print the result as a table
                                            System.out.printf("%-10s %-20s %-40s %-10s %-10s%n", "pur_id", "pur_date_time", "prod_name", "quantity", "is_completed");
                                            System.out.println("------------------------------------------------------------------------------------------");

                                            while (rs.next()) {
                                                int purId = rs.getInt("pur_id");
                                                Timestamp purDateTime = rs.getTimestamp("pur_date_time");
                                                String prodName = rs.getString("prod_name");
                                                int quantity = rs.getInt("quantity");
                                                String isCompleted = rs.getString("is_completed");

                                                System.out.printf("%-10d %-20s %-40s %-10d %-10s%n", purId, purDateTime, prodName, quantity, isCompleted);
                                            }
                                        }
                                    }

                                    /* 업데이트 여부 입력 */
                                    System.out.print("Update or not?: ");
                                    user_input = scanner.nextInt();

                                    /* pur_id 입력 */
                                    System.out.print("Which pur_id?: ");
                                    int pur_id_1 = scanner.nextInt();

                                    System.out.print("\n\n");
                                    if (user_input != 0){
                                        /* update is_completed */
                                        // SQL query
                                        String updateQuery = "UPDATE delivery SET is_completed = TRUE WHERE pur_id = " + pur_id_1;

                                        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                                             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

                                            // Execute the update
                                            int rowsUpdated = pstmt.executeUpdate();

                                            // Print the result
                                            if (rowsUpdated > 0) {
                                                System.out.println("Successfully updated the delivery record.");
                                            }
                                        }
                                    }

                                    break;
                                case 3:
                                    /* 매입환출(recall) */

                                    /* 매입한 상품 조회 */
                                    String query = "SELECT pr.pur_id, pc.pur_date_time, p.prod_name, pr.quantity " +
                                            "FROM purchase_record pr " +
                                            "JOIN purchase pc ON pr.pur_id = pc.pur_id " +
                                            "JOIN product p ON pr.prod_id = p.prod_id " +
                                            "WHERE pc.store_id = " + store_id + ";";

                                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                                         PreparedStatement pstmt = conn.prepareStatement(query)) {

                                        // Execute the query
                                        try (ResultSet rs = pstmt.executeQuery()) {
                                            // Print the result as a table
                                            System.out.printf("%-10s %-20s %-40s %-10s%n", "pur_id", "pur_date_time", "prod_name", "quantity");
                                            System.out.println("----------------------------------------------------------------------");

                                            while (rs.next()) {
                                                int purId = rs.getInt("pur_id");
                                                Timestamp purDateTime = rs.getTimestamp("pur_date_time");
                                                String prodName = rs.getString("prod_name");
                                                int quantity = rs.getInt("quantity");

                                                System.out.printf("%-10d %-20s %-40s %-10d%n", purId, purDateTime, prodName, quantity);
                                            }
                                        }
                                    }

                                    /* pur_id 입력 */
                                    System.out.print("Which pur_id?: ");
                                    int pur_id_2 = scanner.nextInt();

                                    System.out.println("Recall of pur_id " + pur_id_2 + " is completed. Money updated.");

                                    break;
                                case 4:
                                    break;
                            }
                        }
                        break;
                    case 4:
                        if (position_id == 1) {
                            /* 금고 조회 */
                            bufferSQL = "SELECT money\n" +
                                    "FROM money\n" +
                                    "WHERE store_id = " + store_id + ";";

                            try (Statement statement = connection.createStatement();
                                 ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                                if (resultSet.next()) { // resultSet이 비어있지 않으면 실행
                                    System.out.println("available money: " + resultSet.getInt("money"));
                                } else {
                                    System.out.println("Error occurred");
                                    return;
                                }
                            }
                        }
                        else{
                            System.out.println("You don't have the privileges");
                        }
                        break;
                    case 5:
                        return;
                }
                System.out.print("\n\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
