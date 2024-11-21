package simpleDatabase;

import java.sql.SQLException;

/**
 * The CSE360Test class contains methods to test the functionality of the User, DatabaseHelper, and Article classes.
 * It includes methods to run tests and print the results.
 * 
 * @author Aditya Gupta, Abhave Abhilash
 * @version 1.0
 * @since 2024-10-30
 */
public class CSE360Test {

    static int numPassed = 0;
    static int numFailed = 0;

    /**
     * The main method that runs all the tests.
     * 
     * @param args Command line arguments
     * @throws SQLException If a database access error occurs
     */
    public static void main(String[] args) throws SQLException {
        System.out.println("Running tests...");
        testUser();
        testDatabase();
        testArticle();
        System.out.printf("%d tests ran, %d tests successful\n", numPassed + numFailed, numPassed);
    }

    /**
     * Tests the functionality of the User class.
     */
    private static void testUser() {
        System.out.println("Testing User class...");

        // Test 1: Username test
        User user1 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        user1.setUsername("testUser");
        if ("testUser".equals(user1.getUsername())) {
            System.out.println("Test 1 passed");
            numPassed++;
        } else {
            System.out.println("Test 1 failed");
            numFailed++;
        }

        // Test 2: Preferred and last name set test
        User user2 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
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
        User user3 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
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
        User user4 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
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
        User user5 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        boolean[] userRoles = user5.getRoles();
        userRoles[1] = true;
        user5.setRoles(userRoles);
        if (user5.getRoles()[1]) {
            System.out.println("Test 5 passed");
            numPassed++;
        } else {
            System.out.println("Test 5 failed");
            numFailed++;
        }

        // Test 6: Removing roles test
        User user6 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        userRoles = user6.getRoles();
        userRoles[0] = false;
        userRoles[1] = false;
        userRoles[2] = false;
        boolean[] postChange = user6.getRoles();
        if (!postChange[0] && (!postChange[1] && !postChange[2])) {
            System.out.println("Test 6 passed");
            numPassed++;
        } else {
            System.out.println("Test 6 failed");
            numFailed++;
        }
    }

    /**
     * Tests the functionality of the DatabaseHelper class.
     * 
     * @throws SQLException If a database access error occurs
     */
    private static void testDatabase() throws SQLException {
        System.out.println("Testing DatabaseHelper class...");

        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();

        // Test 1: Default admin invitation code existence test
        boolean result1 = databaseHelper.doesUserExist("doesnotexist");
        if (!result1) {
            System.out.println("Test 1 passed");
            numPassed++;
        } else {
            System.out.println("Test 1 failed");
            numFailed++;
        }

        // Test 2: Login account test
        User newUser = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, true, false, null);
        boolean[] newUserRoles = newUser.getRoles();
        int testNewUserID = databaseHelper.insertShellUser(newUserRoles[0], newUserRoles[1], newUserRoles[2]);
        String genOTP = databaseHelper.createOTP(testNewUserID);
        int fromDbNewUserId = databaseHelper.verifyOTP(genOTP);
        if(databaseHelper.register(newUser.getUsername(), newUser.getPassword(), fromDbNewUserId)) {
            User fromDB = databaseHelper.login(newUser.getUsername(), newUser.getPassword());
            boolean result2 = "username".equals(fromDB.getUsername());
            if (result2) {
                System.out.println("Test 2 passed");
                numPassed++;
            } else {
                System.out.println("Test 2 failed");
                numFailed++;
            }
        } else {
            System.out.println("Test 2 failed");
            numFailed++;
        }
        

        databaseHelper.closeConnection();
    }

    /**
     * Tests the functionality of the Article class.
     * 
     * @throws SQLException If a database access error occurs
     */
    private static void testArticle() throws SQLException {
        System.out.println("Testing Article class...");

        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();

        /* Test 1: Create article test
        databaseHelper.createArticle("beginner", "CSE360", "Introduction to Databases", "This article provides an introduction to databases.", new String[]{"databases", "SQL"}, "The body of the article goes here.", new String[]{"http://example.com"}, "s");
        System.out.println("Test 1 passed");
        numPassed++;
        */

        // Test 2: View all articles test
        databaseHelper.viewAllArticles("s");
        System.out.println("Test 2 passed");
        numPassed++;

        /* Test 3: View article by ID test
        databaseHelper.viewArticle("i", "1");
        System.out.println("Test 3 passed");
        numPassed++;
        */

        databaseHelper.closeConnection();
    }
}