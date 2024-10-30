package simpleDatabase;

import java.sql.SQLException;

public class Test {

    public static void main(String[] args) {
        DatabaseHelper dbHelper = new DatabaseHelper();

        try {
            // Connect to the database
            dbHelper.connectToDatabase();

            // Insert first user
            System.out.println("Inserting first user...");
            dbHelper.register("john.doe@example.com", "password123", "ADM");
            System.out.println("First user inserted: john.doe@example.com");

            // Retrieve and print the first user
            System.out.println("Retrieving first user...");
            if (dbHelper.login("john.doe@example.com", "password123", "ADM")) {
                System.out.println("Login successful for user: john.doe@example.com");
            } else {
                System.out.println("Login failed for user: john.doe@example.com");
            }

            // Insert second user
            System.out.println("Inserting second user...");
            dbHelper.register("jane.smith@example.com", "password456", "USR");
            System.out.println("Second user inserted: jane.smith@example.com");

            // Retrieve and print all users
            System.out.println("Retrieving all users...");
            dbHelper.displayUsersByAdmin();

            // Check if database is empty
            System.out.println("Checking if database is empty...");
            boolean isEmpty = dbHelper.isDatabaseEmpty();
            System.out.println("Is the database empty? " + isEmpty);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the database connection
            dbHelper.closeConnection();
        }
    }
}
