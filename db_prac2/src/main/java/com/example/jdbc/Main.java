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

            System.out.println("Enter your ID: ");
            Scanner scanner = new Scanner(System.in);
            String ID = scanner.nextLine();

            String buffer, worker_id, store_id;
            int position_id;

            /* get worker_name and position_name */
            String bufferSQL = "SELECT w.worker_id, wp.position_id, wp.position_name\n" +
                    "FROM worker w\n" +
                    "JOIN worker_position wp ON w.position_id = wp.position_id\n" +
                    "WHERE w.worker_code = " + ID + ";";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                buffer = resultSet.getString("worker_position") + " " + resultSet.getString("worker_name");
                position_id = resultSet.getInt("position_id");
                worker_id = resultSet.getString("worker_id");
            } catch (Exception e){
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
                store_id = resultSet.getString("store_id");
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

                                    int i = 0;
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

                                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                                     PreparedStatement pstmt = conn.prepareStatement(bufferSQL)) {

                                    int rowsAffected = pstmt.executeUpdate();

                                    if (rowsAffected > 0) {
                                        /* money update */

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
                                    "[2] check whether the delivery is completed\n" /* 매입에 따른 배송 내역 확인 */ +
                                    "[3] recall\n" /*  */ +
                                    "[4] exit\n" +
                                    "Enter your command: ");
                            user_input = scanner.nextInt();
                            System.out.print("\n\n");

                            switch (user_input){
                                case 1:
                                    /* 매입 */
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
                                        System.out.print("\n\n");
                                    }
                                    break;
                                case 2:
                                    /* 판매 상품 조회 */
                                    break;
                                case 3:
                                    /* 매입환출(recall) */
                                    System.out.print("[1] check inventory\n" /* 재고 조회 */ +
                                                "[2] sale\n" /* 매출 조회[업데이트] */ +
                                                "[3] purchase/delivery\n" /* 매입/배송 조회[업데이트] */ +
                                                "[4] exit\n" +
                                                "Enter your command: ");
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
                                System.out.println("available money: " + resultSet.getInt("money"));
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


            // should be deleted
            /*
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(bufferSQL)) {
                while (resultSet.next()) {
                    System.out.println("s_ID: " + resultSet.getInt("s_ID") +
                            ", i_ID: " + resultSet.getString("i_id"));
                }
            }
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
