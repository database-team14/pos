package com.example.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
                System.out.println("[1] check inventory\n" /* 재고 조회 */ +
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
                        /* 매출 */
                        System.out.println("[1] sell\n" /* 판매(결제) */ +
                                "[2] \n" /* 판매 상품 조회 */ +
                                "[3] refund\n" /* 매입/배송 조회[업데이트] */ +
                                "[4] return to the menu\n" +
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
                                    System.out.print("\n\n");
                                }
                                break;
                            case 2:
                                /* 판매 상품 조회 */
                                break;
                            case 3:
                                if (position_id == 3) { // if the worker is a normal employee
                                    System.out.println("You don't have privileges");
                                }
                                else{
                                    System.out.println("[1] check inventory\n" /* 재고 조회 */ +
                                            "[2] sale\n" /* 매출 조회[업데이트] */ +
                                            "[3] purchase/delivery\n" /* 매입/배송 조회[업데이트] */ +
                                            "[4] exit\n" +
                                            "Enter your command: ");
                                }
                                break;
                            case 4:
                                break;
                        }
                        break;
                    case 3:
                        /* 매입 */
                        if (position_id == 3) { // if the worker is a normal employee
                            System.out.println("You don't have the privileges");
                        }
                        else{
                            System.out.println("[1] purchase\n" /* 매입 */ +
                                    "[2] check whether the delivery is completed\n" /* 매입에 따른 배송 내역 확인 */ +
                                    "[3] recall\n" /*  */ +
                                    "[4] exit\n" +
                                    "Enter your command: ");
                            user_input = scanner.nextInt();
                            System.out.print("\n\n");

                            switch (user_input){
                                case 1:
                                    /*  */
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
                                    if (position_id == 3) { // if the worker is a normal employee
                                        System.out.println("You don't have privileges");
                                    }
                                    else{
                                        System.out.println("[1] check inventory\n" /* 재고 조회 */ +
                                                "[2] sale\n" /* 매출 조회[업데이트] */ +
                                                "[3] purchase/delivery\n" /* 매입/배송 조회[업데이트] */ +
                                                "[4] exit\n" +
                                                "Enter your command: ");
                                    }
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
