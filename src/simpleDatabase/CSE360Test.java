package simpleDatabase;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CSE360Test {
    static int numPassed = 0;
    static int numFailed = 0;

    @Test
    public void testUsername() {
        User user1 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        user1.setUsername("testUser");
        assertEquals("testUser", user1.getUsername(), "Test 1 failed");
        numPassed++;
    }

    @Test
    public void testPreferredAndLastName() {
        User user2 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        String prefName = "abcd";
        user2.setPreferredName(prefName);
        String lastName = "bcda";
        user2.setLastName(lastName);
        assertEquals(prefName, user2.getPreferredName(), "Test 2 failed");
        assertEquals(lastName, user2.getLastName(), "Test 2 failed");
        numPassed++;
    }

    @Test
    public void testSettingOTPFlagTrue() {
        User user3 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        boolean flagResultTrue = true;
        user3.setOTPFlag(flagResultTrue);
        assertTrue(user3.getOTP(), "Test 3 failed");
        numPassed++;
    }

    @Test
    public void testSettingOTPFlagFalse() {
        User user4 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        boolean flagResultFalse = false;
        user4.setOTPFlag(flagResultFalse);
        assertFalse(user4.getOTP(), "Test 4 failed");
        numPassed++;
    }

    @Test
    public void testAddingRoles() {
        User user5 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        boolean[] userRoles = user5.getRoles();
        userRoles[1] = true;
        user5.setRoles(userRoles);
        assertTrue(user5.getRoles()[1], "Test 5 failed");
        numPassed++;
    }

    @Test
    public void testRemovingRoles() {
        User user6 = new User("username", "password", "email", "firstName", "middleName", "lastName", "prefName", false, false, false, false, null);
        boolean[] userRoles = user6.getRoles();
        userRoles[0] = false;
        userRoles[1] = false;
        userRoles[2] = false;
        user6.setRoles(userRoles);
        boolean[] postChange = user6.getRoles();
        assertFalse(postChange[0], "Test 6 failed");
        assertFalse(postChange[1], "Test 6 failed");
        assertFalse(postChange[2], "Test 6 failed");
        numPassed++;
    }

    @Test
    public void testDefaultAdminInvitationCodeExistence() throws SQLException {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();
        boolean result1 = databaseHelper.doesUserExist("doesnotexist");
        assertFalse(result1, "Test 1 failed");
        numPassed++;
        databaseHelper.closeConnection();
    }

    @Test
    public void testViewAllArticles() throws SQLException {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();
        databaseHelper.viewAllArticles("s");
        System.out.println("Test 1 passed");
        numPassed++;
        databaseHelper.closeConnection();
    }
    
    @Test
    public void testAddArticle() throws SQLException {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();

        // Create a mock user
        User user = new User("testUser", "password", "email", "firstName", "middleName", "lastName", "prefName", false, true, false, false, null);
        databaseHelper.register(user.getUsername(), user.getPassword(), databaseHelper.insertShellUser(false, true, false));

<<<<<<< HEAD
        // Add an article
        databaseHelper.createArticle("t");
=======
        /*
        // Test 2: View all articles test
        databaseHelper.viewAllArticles("s");
        System.out.println("Test 2 passed");
        numPassed++;
        */
>>>>>>> 45eb321935656fd7c007e6158c368e35de020664

        // Verify the article was added
        String query = "SELECT COUNT(*) AS count FROM articles";
        try (Statement stmt = databaseHelper.connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                assertTrue(rs.getInt("count") > 0, "Test add article failed");
            }
        }

        numPassed++;
        databaseHelper.closeConnection();
    }
    
    @Test
    public void testSearchArticle() throws SQLException {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();

        // Add an article to search for
        User user = new User("testUser", "password", "email", "firstName", "middleName", "lastName", "prefName", false, true, false, false, null);
        databaseHelper.register(user.getUsername(), user.getPassword(), databaseHelper.insertShellUser(false, true, false));
        databaseHelper.createArticle("t");

        // Search for the article
        String level = "beginner";
        String group = "CSE360";
        String search = "test";

        // Verify the search results
        String query = "SELECT COUNT(*) AS count FROM articles WHERE level = ? AND title LIKE ?";
        try (PreparedStatement pstmt = databaseHelper.connection.prepareStatement(query)) {
            pstmt.setString(1, level);
            pstmt.setString(2, "%" + search + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertTrue(rs.getInt("count") > 0, "Test search article failed");
                }
            }
        }

        numPassed++;
        databaseHelper.closeConnection();
    }
}