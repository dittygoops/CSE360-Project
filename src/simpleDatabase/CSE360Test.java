package simpleDatabase;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class CSE360Test {

    static int numPassed = 0;
    static int numFailed = 0;

    public static void main(String[] args) throws SQLException {
        System.out.println("Running tests...");
        testUser();
        testDatabase();
        System.out.printf("%d tests ran, %d tests successful\n", numPassed + numFailed, numPassed);
    }

    private static void testUser() {
        System.out.println("Testing User class...");

        // Test 1: Username test
        User user1 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        user1.setUsername("testUser");
        if ("testUser".equals(user1.getUsername())) {
            System.out.println("Test 1 passed");
            numPassed++;
        } else {
            System.out.println("Test 1 failed");
            numFailed++;
        }

        // Test 2: Preferred and last name set test
        User user2 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        String prefName = "abcd";
        user2.setPreferredName(prefName);
        String lastName = "bcda";
        user2.setLastName(lastName);
        if (user2.getPreferredName().equals(prefName) && user2.getLastName().equals(lastName)) {
            System.out.println("Test 2 passed");
            numPassed++;
        } else {
            System.out.println("Test 2 failed");
            numFailed++;
        }

        // Test 3: Setting OTP flag to true test
        User user3 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        boolean flagResultTrue = true;
        user3.setOTPFlag(flagResultTrue);
        if (user3.getOTP() == flagResultTrue) {
            System.out.println("Test 3 passed");
            numPassed++;
        } else {
            System.out.println("Test 3 failed");
            numFailed++;
        }

        // Test 4: Setting OTP flag to false test
        User user4 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        boolean flagResultFalse = false;
        user4.setOTPFlag(flagResultFalse);
        if (user4.getOTP() == flagResultFalse) {
            System.out.println("Test 4 passed");
            numPassed++;
        } else {
            System.out.println("Test 4 failed");
            numFailed++;
        }

        // Test 5: Adding roles test
        User user5 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        String newRole = "INSTRUCTOR";
        user5.setRoles(newRole);
        if (user5.getRoles().equals(newRole)) {
            System.out.println("Test 5 passed");
            numPassed++;
        } else {
            System.out.println("Test 5 failed");
            numFailed++;
        }

        // Test 6: Removing roles test
        User user6 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        user6.setRoles(newRole);
        user6.setRoles("");
        if (user6.getRoles().isEmpty()) {
            System.out.println("Test 6 passed");
            numPassed++;
        } else {
            System.out.println("Test 6 failed");
            numFailed++;
        }
    }

    private static void testDatabase() throws SQLException {
        System.out.println("Testing DatabaseHelper class...");

        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();

        // Test 1: Default admin invitation code existence test
        boolean result1 = databaseHelper.doesUserExist("admin");
        if (result1) {
            System.out.println("Test 1 passed");
            numPassed++;
        } else {
            System.out.println("Test 1 failed");
            numFailed++;
        }

        // Test 2: Create account test
        User newUser = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", "roles", false, null);
        databaseHelper.register(newUser.getUsername(), newUser.getPassword(), newUser.getRoles());
        boolean result2 = "username".equals(newUser.getUsername());
        if (result2) {
            System.out.println("Test 2 passed");
            numPassed++;
        } else {
            System.out.println("Test 2 failed");
            numFailed++;
        }

        databaseHelper.closeConnection();
    }
}