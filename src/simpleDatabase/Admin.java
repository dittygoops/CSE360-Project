package simpleDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class Admin extends User {
    private Connection connection;
    /**
     * Constructor for creating a new Admin instance.
     *
     * @param connection      The database connection
     * @param username        The admin's unique identifier
     * @param password        The admin's password
     * @param email           The admin's email address
     * @param firstName       The admin's first name
     * @param middleName      The admin's middle name (optional)
     * @param lastName        The admin's last name
     * @param prefName        The admin's preferred name
     * @param roles           The roles assigned to the admin
     * @param otpFlag         Indicates if a one-time password is required
     * @param otpExpiration    The expiration time for the one-time password
     */
    public Admin(Connection connection, String username, String password, String email, String firstName, String middleName, String lastName, String prefName, String roles) {
        super(connection, username, password, email, firstName, middleName, lastName, prefName, roles);
    }

    public int firstAdmin(String userName, String password) throws SQLException{
		String insertShell = "INSERT INTO cse360users (userName, email, password, firstName, middleName, lastName, preferredFirst, adminFlag, teachFlag, studFlag, otpFlag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertShell)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, "");
			pstmt.setString(3, password);
			pstmt.setString(4, "");
			pstmt.setString(5, "");
			pstmt.setString(6, "");
			pstmt.setString(7, "");
			pstmt.setBoolean(8, true);
			pstmt.setBoolean(9, false);
			pstmt.setBoolean(10, false);
			pstmt.setBoolean(11, true);
			int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 1) {
                System.out.println("Shell user inserted successfully.");
                return 1;
            } else {
                System.out.println("Shell user insertion failed.");
            }
		} catch (SQLException e) {
			System.err.println("Database error during shell user insertion: " + e.getMessage());
		}
		return -1;
    }

    // insert shell user -> idek what this does
    public int insertShellUser(boolean admin, boolean instruct, boolean stud) throws SQLException{
		String insertShell = "INSERT INTO cse360users (userName, email, password, firstName, middleName, lastName, preferredFirst, adminFlag, teachFlag, studFlag, otpFlag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertShell, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, "");
			pstmt.setString(2, "");
			pstmt.setString(3, "");
			pstmt.setString(4, "");
			pstmt.setString(5, "");
			pstmt.setString(6, "");
			pstmt.setString(7, "");
			pstmt.setBoolean(8, admin);
			pstmt.setBoolean(9, instruct);
			pstmt.setBoolean(10, stud);
			pstmt.setBoolean(11, false);
			int rowsAffected = pstmt.executeUpdate();

			if(rowsAffected > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1); // Retrieve the ID
                        System.out.println("Inserted user ID: " + generatedId);
						return generatedId;
					} else System.out.println("Shell user insertion failed");
				} catch (SQLException f) {
					System.err.println("Databse error during shell user insertion: " + f.getMessage());
				}
			} System.out.println("DB failed shell user insertion");
			return -1;
		} catch (SQLException e) {
			System.err.println("Database error during shell user insertion: " + e.getMessage());
		}
		return -1;
	}


    // update UserRoles
    public void updateUserRoles(User user) throws SQLException {
		String query = "UPDATE cse360users SET role = ? WHERE userName = ? AND email = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getRoles());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getEmail());

			
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected == 0) {
				System.out.println("No user found with the username: " + user.getUsername() + " and email : " + user.getEmail());
			} else {
				System.out.println("User updated successfully.");
			}
		} 
	}

    // update User
    public void updateUser(User user) throws SQLException {
        String query = "UPDATE cse360users SET userName = ?, email = ?, firstName = ?, middleName = ?, lastName = ?, preferredFirst = ? WHERE userName = ? AND email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getMiddleName());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getPreferredName());
            pstmt.setString(7, user.getUsername());
            pstmt.setString(8, user.getEmail());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No user found with the username: " + user.getUsername() + " and email : " + user.getEmail());
            } else {
                System.out.println("User updated successfully.");
            }
        }

    }

    // find User
    public User findUser(String userName, String email) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, email);
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

    // display all users
    public void displayAllUsers() throws SQLException {
        String query = "SELECT * FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                // Extracting values from the result set
                String dbuserName = rs.getString("userName");
                String dbemail = rs.getString("email");
                String dbfirstName = rs.getString("firstName");
                String dbmiddleName = rs.getString("middleName");
                String dbLastName = rs.getString("lastName");
                String preferredFirst = rs.getString("preferredFirst");
                String dbRoles = rs.getString("roles");
                System.out.println("Username: " + dbuserName + ", Email: " + dbemail + ", First Name: " + dbfirstName + ", Middle Name: " + dbmiddleName + ", Last Name: " + dbLastName + ", Preferred First: " + preferredFirst + ", Roles: " + dbRoles);
            }
        }
    }

    // delete user
    public void deleteUser(String userName, String email) throws SQLException {
        String query = "DELETE FROM cse360users WHERE userName = ? AND email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, email);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No user found with the username: " + userName + " and email : " + email);
            } else {
                System.out.println("User deleted successfully.");
            }
        }
    }
    

}