package simpleDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private String username;       // The user's unique identifier
    private String password;       // The user's password
    private String email;          // The user's email address
    private String firstName;      // The user's first name
    private String middleName;     // The user's middle name (optional)
    private String lastName;       // The user's last name
    private String preferedName;   // The user's preferred name
    private String roles;           // Indicates if a one-time password is required
    private Connection connection;

    /**
     * Constructor for creating a new User instance.
     * @param username The user's unique identifier
     * @param password The user's password
     * @param email The user's email address
     * @param firstName The user's first name
     * @param middleName The user's middle name (optional)
     * @param lastName The user's last name
     * @param prefName The user's preferred name
     * @param roles The roles assigned to the user
     */
    public User(Connection connection, String username, String password, String email, String firstName, String middleName, String lastName, String prefName, String roles) {
        this.connection = connection;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.preferedName = prefName;
        this.roles = roles;
    }

    // overloaded user constructor
    public User(String username, String email, String firstName, String middleName, String lastName, String prefName, String roles) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.preferedName = prefName;
        this.roles = roles;
    }

    // Getter methods
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getRoles() {
        return roles;
    }

    public String getPreferredName() {
        return preferedName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }


    // Setter methods
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPreferredName(String prefName) {
        this.preferedName = prefName;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
    
    // register User 
    public boolean register(String userName, String password, int userID) throws SQLException {
		String insertUser = "UPDATE cse360users SET userName = ?, password = ?, otpFlag = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			pstmt.setBoolean(3, true);
			pstmt.setInt(4, userID);
			int rowsAffected = pstmt.executeUpdate();
			return (rowsAffected > 0); 
			
		} catch (SQLException e) {
			System.err.println("DB issue while registering user: " + e.getMessage());
		}
		return false;
	}

    // login User
    public User login(String userName, String password) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// Extracting values from the result set
                    String dbuserName = rs.getString("userName");
					String dbemail = rs.getString("email");
					String dbfirstName = rs.getString("firstName");
					String dbmiddleName = rs.getString("middleName");
                    String dbLastName = rs.getString("lastName");
					String preferredFirst = rs.getString("preferredFirst");
                    String dbRoles = rs.getString("roles");
                    return new User(dbuserName, dbemail, dbfirstName, dbmiddleName, dbLastName, preferredFirst, dbRoles);

				} else {
					return null; // User not found
				}
			}
		}
	}


    /**
     * Method to retrieve the full name of the user, combining first, middle (if present), and last names.
     *
     * @return The full name of the user
     */
    public String getFullName() {
        return firstName + " " + (middleName != null && !middleName.isEmpty() ? middleName + " " : "") + lastName;
    }
    
    /**
     * Method to log out the user. 
     * (Logic for logging out should be implemented.)
     */
    public void logout() {
        // Logic for logging out
    }

    /**
     * Method to complete the account setup process.
     * (Logic for account setup should be implemented.)
     */
    public void finishAccountSetup() {
        // Logic to complete the account setup process
    }

    /**
     * Method to select a role for the current session.
     * 
     * @param role The role to be selected
     */
    public void selectRole(String role) {
        // Logic to select a role for the current session
    }
}