package simpleDatabase;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/***
 * This class contains all functions that relate/interact with our H2 databases
 * 
 * @author Abhave Abhilash and Aditya Gupta
 * @version 1.0
 * @since 10/9/2024
 */
class DatabaseHelper {

	// JDBC driver name and database URL
	private final EncryptionHelper encryptionHelper = new EncryptionHelper();
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:~/firstDatabase";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	public Connection connection = null;
	private Statement statement = null;

	private Scanner scanner = new Scanner(System.in);
	// PreparedStatement pstmt

	/**
	 * Blank constructor
	 */
	public DatabaseHelper() {

	}

	/**
	 * Connect to the database
	 * 
	 * @throws SQLException
	 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement();
			createTables(); // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	/**
	 * create and initiate our user and otp tables
	 * 
	 * @throws SQLException
	 */
	private void createTables() throws SQLException {
		// String dropUsers = "DROP TABLE IF EXISTS cse360users";
		// statement.execute(dropUsers);
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("

				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255), "
				+ "email VARCHAR(255), "
				+ "password VARCHAR(255), "
				// first name
				+ "firstName VARCHAR(255), "
				// last name
				+ "lastName VARCHAR(255), "
				// middle name
				+ "middleName VARCHAR(255), "
				// preferred first
				+ "preferredFirst VARCHAR(255), "
				// user role sia
				+ "adminFlag BOOLEAN DEFAULT FALSE, "
				+ "teachFlag BOOLEAN DEFAULT FALSE, "
				+ "studFlag BOOLEAN DEFAULT FALSE, "
				+ "otpFlag BOOLEAN DEFAULT FALSE) ";
		statement.execute(userTable);

		// create otp table, with id, otp, expiry time and user role(s)
		String otpTable = "CREATE TABLE IF NOT EXISTS otpTable ("
				+ "otp VARCHAR(255) PRIMARY KEY, "
				+ "expiryTime TIMESTAMP, "
				+ "user_id INT, "
				+ "FOREIGN KEY(user_id) REFERENCES cse360users(id) ON DELETE CASCADE)";
		statement.execute(otpTable);

		String articlesTable = "CREATE TABLE IF NOT EXISTS articles ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "level VARCHAR(20), " // level (beginner, intermediate, advanced, expert)
				+ "authors VARCHAR(100), "
				+ "title VARCHAR(255) NOT NULL, " // title
				+ "short_description CLOB, " // short_description/abstract
				+ "keywords VARCHAR(255), " // keywords
				+ "body CLOB, " // body
				+ "reference_links VARCHAR(255)" // reference_links
				+ ")";
		statement.execute(articlesTable);

		String groups = "CREATE TABLE IF NOT EXISTS groups ("
			+ "name VARCHAR(255) PRIMARY KEY, "
			+ "specialFlag BOOLEAN DEFAULT FALSE)";
		statement.execute(groups);

		String articleGroupsTable = "CREATE TABLE IF NOT EXISTS articleGroups ("
			+ "group_name VARCHAR(255), "
			+ "article_id INT, " 
			+ "FOREIGN KEY(group_name) REFERENCES groups(name) ON DELETE CASCADE, "
			+ "FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE)";
		statement.execute(articleGroupsTable);

		String groupRightsTable = "CREATE TABLE IF NOT EXISTS groupRights ("
			+ "user_id INT, "
			+ "group_name VARCHAR(255), "
			+ "accessRole VARCHAR(1), "
			+ "adminRightsFlag BOOLEAN DEFAULT FALSE, "
			+ "viewRightsFlag BOOLEAN DEFAULT FALSE, "
			+ "FOREIGN KEY(user_id) REFERENCES cse360users(id) ON DELETE CASCADE, "
			+ "FOREIGN KEY(group_name) REFERENCES groups(name) ON DELETE CASCADE)";
		statement.execute(groupRightsTable);
	}

	/**
	 * Check if the database is empty
	 * 
	 * @return boolean that represents empty or not
	 * @throws SQLException
	 */
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	public boolean groupExist(String checkGroup) {
		String query = "SELECT COUNT(*) AS count FROM groups WHERE name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, checkGroup);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0; // Check if count > 0
			}
		} catch (SQLException e) {
			System.err.println("DB error checking if group exists: " + e.getMessage());
		}
		return false;	
	}
	

	public boolean isGroupSpecial(String checkGroup) {
		String query = "SELECT specialFlag from groups where name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, checkGroup);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getBoolean(1);
			}
		} catch (SQLException e) {
			System.err.println("DB error checking if group is speical: " + e.getMessage());
		}
		return false;
	}

	/*
	 * Add a user to the database
	 * 
	 * @param user
	 * @throws SQLException
	 
	public void addUser(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, email, password, firstName, middleName, lastName, preferredFirst, role, otpFlag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, user.getEmail());
			pstmt.setString(3, user.getPassword());
			pstmt.setString(4, user.getFirstName());
			pstmt.setString(5, user.getMiddleName());
			pstmt.setString(6, user.getLastName());
			pstmt.setString(7, user.getPreferredFirst());
			pstmt.setString(8, user.getRoles());
			pstmt.setBoolean(9, user.getOTP());
			pstmt.executeUpdate();
		}
	}
	
	*/

	/**
	 * Register a user with username, password, and role
	 * 
	 * @param userName
	 * @param password
	 * @param role
	 * @throws SQLException
	 */
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

	

	/**
	 * login user with username and password
	 * 
	 * @param userName
	 * @param password
	 * @return User object that is populated with columns from database
	 * @throws SQLException
	 */
	public User login(String userName, String password) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// Extracting values from the result set
					String email = rs.getString("email");
					String firstName = rs.getString("firstName");
					String middleName = rs.getString("middleName");
					String lastName = rs.getString("lastName");
					String preferredFirst = rs.getString("preferredFirst");
					boolean aFlag = rs.getBoolean("adminFlag");	
					boolean tFlag = rs.getBoolean("teachFlag");
					boolean sFlag = rs.getBoolean("studFlag");
					boolean otpFlag = rs.getBoolean("otpFlag");
					LocalDateTime otpExpiration = LocalDateTime.now(); // default value

					// Constructing and returning the User object
					return new User(userName, password, email, firstName, middleName, lastName,
							preferredFirst, aFlag, tFlag, sFlag, otpFlag, otpExpiration);
				} else {
					return null; // User not found
				}
			}
		}
	}

	/**
	 * update database row with data from user object
	 * 
	 * @param user
	 * @throws SQLException
	 */
	public void updateUserRoles(User user) throws SQLException {
		String query = "UPDATE cse360users SET adminFlag = ?, teachFlag = ?, studFlag = ? WHERE userName = ? and email = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setBoolean(1, user.getRoles()[0]);
			pstmt.setBoolean(2, user.getRoles()[1]);
			pstmt.setBoolean(3, user.getRoles()[2]);
			pstmt.setString(4, user.getUsername());
			pstmt.setString(5, user.getEmail());

			// update execution
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected == 0) {
				System.out.println("No user found with the username: " + user.getUsername() + " and email : " + user.getEmail());
			} else {
				System.out.println("User updated successfully.");
			}
		} 
	}

	public boolean updateUser(User user) throws SQLException {
		String query = "UPDATE cse360users SET firstName = ?, middleName = ?, lastName = ?, preferredFirst = ?, email = ?, otpFlag = ? WHERE userName = ? and password = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getFirstName());
			pstmt.setString(2, user.getMiddleName());
			pstmt.setString(3, user.getLastName());
			pstmt.setString(4, user.getPreferredFirst());
			pstmt.setString(5, user.getEmail());
			pstmt.setBoolean(6, user.getOTP());
			pstmt.setString(7, user.getUsername());
			pstmt.setString(8, user.getPassword());	
			// update execution
			int rowsAffected = pstmt.executeUpdate();
			return (rowsAffected > 0);
		} catch (SQLException e) {
			System.err.println("DB issue with updating user after setting up account: " + e.getMessage());
		}
		return false;
	}

	/**
	 * find user by username and email
	 * 
	 * @param userName
	 * @param email
	 * @return User object with data from database
	 * @throws SQLException
	 */
	public User findUser(String userName, String email) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// Extracting values from the result set
					String username = rs.getString("userName");
					String userEmail = rs.getString("email");
					String firstName = rs.getString("firstName");
					String middleName = rs.getString("middleName");
					String lastName = rs.getString("lastName");
					String preferredFirst = rs.getString("preferredFirst");
					boolean aFlag = rs.getBoolean("adminFlag");
					boolean tFlag = rs.getBoolean("teachFlag");
					boolean sFlag = rs.getBoolean("studFlag");

					boolean otpFlag = rs.getBoolean("otpFlag"); // Get actual otpFlag value from DB
					LocalDateTime otpExpiration = LocalDateTime.now(); // You can replace this with actual expiration
																		// time if you have it in DB

					// Constructing and returning the User object
					return new User(username, "", userEmail, firstName, middleName, lastName,
							preferredFirst, aFlag, tFlag, sFlag, otpFlag, otpExpiration);
				} else {
					return null; // User not found
				}
			}
		}
	}

	/**
	 * get User Roles by username
	 * 
	 * @param userName
	 */
	public boolean[] getUserRoles(String userName, String email) {
		String query = "SELECT adminFlag, teachFlag, studFlag FROM cse360users WHERE userName = ? and email = ?";
		boolean[] roles = new boolean[3];
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, email);
		
			ResultSet rs = pstmt.executeQuery(); 

			if (rs.next()) {
				roles[0] = rs.getBoolean(1);
				roles[1] = rs.getBoolean(2);
				roles[2] = rs.getBoolean(3);
				return roles;
			} else {
				System.err.println("User not found");
				return null;
			}
		} catch (SQLException e) {
			System.err.println("Database error while getting user roles: " + e.getMessage());
			return null;
		}
	}


	public boolean userExist(String userName, String email) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ? AND email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("DB issue validating user by userName and Email: " + e.getMessage());
			return false;
		}
	}

	/**
	 * check if user in database
	 * 
	 * @param userName
	 * @return boolean that represents if user exists
	 */
	public boolean doesUserExist(String userName) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Database error while checking user existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * check if user in database
	 * 
	 * @param userName
	 * @param password
	 * @return boolean that represents if user exists
	 */
	public boolean doesUserExistBoth(String userName, String password) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {

			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Database error while checking user existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * check if user in database
	 * 
	 * @param email
	 * @return boolean that represents if user exists
	 */
	public boolean doesUserExistEmail(String email) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {

			pstmt.setString(1, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Database error while checking user existence: " + e.getMessage());
			return false;
		}
	}

	/**
	 * display list of all users
	 * 
	 * @throws SQLException
	 */
	public void displayUsersByAdmin() throws SQLException {
		String sql = "SELECT userName, email, preferredFirst, adminFlag, teachFlag, studFlag FROM cse360users";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				// Retrieve by column name
				String username = rs.getString(1);
				String email = rs.getString(2);
				String preferredFirstName = rs.getString(3);
				boolean[] roles  = {rs.getBoolean(4), rs.getBoolean(5),rs.getBoolean(6)};
				

				// Display values
				System.out.print("Username: " + username);
				System.out.print(", Email: " + email);
				System.out.print(", Preferred First Name: " + preferredFirstName);
				System.out.print(", Roles: ");
				if(roles[0]) System.out.print("Admin ");
				if(roles[1]) System.out.print("Instructor ");
				if(roles[2]) System.out.print("Student");
				System.out.println();
			}
		} catch (SQLException e) {
			System.err.println("Database error while checking user existence: " + e.getMessage());
		}
	}

	/**
	 * display user by name
	 * 
	 * @throws SQLException
	 */
	public void displayUsersByUser() throws SQLException {
		String sql = "SELECT * FROM cse360users";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("id");
				String email = rs.getString("email");
				String password = rs.getString("password");
				String role = rs.getString("role");

				// Display values
				System.out.print("ID: " + id);
				System.out.print(", Age: " + email);
				System.out.print(", First: " + password);
				System.out.println(", Last: " + role);
			}
		}
	}

	// delete user by username
	/**
	 * delete user by username
	 * 
	 * @param userName
	 * @return boolean that represents if user was deleted
	 * @throws SQLException
	 */

	public boolean deleteUserAccount(String userName, String email) throws SQLException {
		String deleteQuery = "DELETE FROM cse360users WHERE userName = ? and email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, email);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Database error while deleting user: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Create OTP and store it in the database
	 */
	public String createOTP(int userID) {
		String otp = "";
		for (int i = 0; i < 6; i++) {
			otp += (int) (Math.random() * 10);
		}
		String expiryTime = LocalDateTime.now().plusMinutes(5).toString();
		try {
			insertOTP(otp, expiryTime, userID);
		} catch (SQLException e) {
			System.err.println("Database error while creating OTP: " + e.getMessage());
		}
		return otp;
	}

	public int getUserId(String userName, String email) {
		String query = "SELECT id FROM cse360users WHERE userName = ? AND email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, email);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) { // Check if a row is available
					return rs.getInt(1); // Retrieve the id
				} else return -1;
			}
		} catch (SQLException e) {
			System.err.println("DB issue validating user by userName and Email: " + e.getMessage());
		}
		return -1; // Return -1 if no user is found or an error occurs
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

			if(rowsAffected > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1); // Retrieve the ID
                        System.out.println("Inserted user ID: " + generatedId);
						return generatedId;
					} else System.out.println("First admin was not made properly. Please try again later");
				} catch (SQLException f) {
					System.err.println("Databse error during shell user insertion: " + f.getMessage());
				} 
			} else System.out.println("First admin was not made properly. Please try again later");
		} catch (SQLException e) {
			System.err.println("Database error during shell user insertion: " + e.getMessage());
		}
		return -1;
	}	

	/**
	 * Insert a shell user into the database
	 * 
	 * @param admin
	 * @param instruct
	 * @param stud
	 * @return the id of the inserted user
	 * @throws SQLException
	 */
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

	/**
	 * Create a random article id
	 * 
	 * @return the id of the article
	 */
	public int createArticleId() {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (int) (Math.random() * 10);
		}
		int res = Integer.parseInt(id);
		return res;
	}

	/**
	 * insert otp to table with roles
	 * 
	 * @param otp
	 * @param expiryTime
	 * @throws SQLException
	 */
	public void insertOTP(String otp, String expiryTime, int userID) throws SQLException {
		String insertOTP = "INSERT INTO otpTable (otp, expiryTime, user_id) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertOTP)) {
			pstmt.setString(1, otp);
			pstmt.setString(2, expiryTime);
			pstmt.setInt(3, userID);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("DB issue inserting OTP into table: " + e.getMessage());
		}
	}

	/**
	 * Returns user_id that is associated with otp 
	 * If an issue arises - returns -1
	 * 
	 * @param otp
	 * @return Integer that represents user_id associated with the otp
	 * @throws SQLException
	 */
	public int verifyOTP(String otp) throws SQLException {
		String query = "SELECT user_id, expiryTime FROM otpTable WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String expiryTime = rs.getString(2);
					if (expiryTime.contains(".")) {
						String[] parts = expiryTime.split("\\.");
						if (parts[1].length() < 6) {
							expiryTime = parts[0] + "." + String.format("%-6s", parts[1]).replace(' ', '0');
						} else if (parts[1].length() > 6) {
							expiryTime = parts[0] + "." + parts[1].substring(0, 6);
						}
					}
	
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
					LocalDateTime expiry = LocalDateTime.parse(expiryTime, formatter);
					if (LocalDateTime.now().isBefore(expiry)) {
						return rs.getInt(1);
					}
				}
			}
		}
		return -1;
	}

	/**
	 * This function is used after an OTP has been verified to return the roles
	 * associated with the invitation
	 * 
	 * @param otp
	 * @return string of roles given to this user by the admin who invited them to
	 *         the system
	 * @throws SQLException
	 */
	public String getRolesFromOTP(String otp) throws SQLException {
		String query = "SELECT * FROM otpTable WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String roles = rs.getString("role");
					return roles;
				}
			}
		}
		return "error";
	}

	/**
	 * delete otp from table
	 * 
	 * @param otp
	 * @throws SQLException
	 */
	public void deleteOTP(String otp) throws SQLException {
		String deleteOTP = "DELETE FROM otpTable WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteOTP)) {
			pstmt.setString(1, otp);
			pstmt.executeUpdate();
		}
	}

	/**
	 * check if the otp is expired by checking the otp table for the expiry time
	 * 
	 * @param otp
	 * @return boolean representing the expiration state of otp
	 * @throws SQLException
	 */
	public Boolean isOTPExpired(String otp) throws SQLException {
		String query = "SELECT * FROM otpTable WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String expiryTime = rs.getString("expiryTime");
					LocalDateTime expiry = LocalDateTime.parse(expiryTime);
					return LocalDateTime.now().isAfter(expiry);
				}
			}
		}
		return true;
	}

	/**
	 * close connections with databases
	 */
	public void closeConnection() {
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException se2) {
			System.err.println("Database error while closing connection: " + se2.getMessage());
		}
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	/**
	 * Backup the articles to a file
	 * 
	 * @param role
	 * @param file
	 * @throws Exception
	 */
	public void backup(String role, String file) throws Exception {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		// Check to see if there is anything to back up at all
		if (isDatabaseEmpty()) {
			System.out.println("There are no articles in the system to back up.");
			return;
		}

		// Get all entries in the table
		String sql = "SELECT * FROM articles";
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

			// Each article will have 6 lines in the text file, every 6 lines corresponds to
			// an entry
			while (rs.next()) {
				// For each entry - get all fields and store them in their encrypted states
				int id = rs.getInt("id");
				String idEnter = "" + id;
				String level = rs.getString("level");
				String group_id = rs.getString("group_id");
				String title = rs.getString("title");
				String short_description = rs.getString("short_description");
				String keywords = rs.getString("keywords");
				String body = rs.getString("body");
				String references = rs.getString("reference_links");

				// write each field on its own line
				writer.write(idEnter);
				writer.newLine();
				writer.write(level);
				writer.newLine();
				writer.write(group_id);
				writer.newLine();
				writer.write(title);
				writer.newLine();
				writer.write(short_description);
				writer.newLine();
				writer.write(keywords);
				writer.newLine();
				writer.write(body);
				writer.newLine();
				writer.write(references);
				writer.newLine();
			}
			System.out.println("Successfully backed up system");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void restore(String role){
		System.out.println("Here are your restoration options: ");
		System.out.println("1. Restore all articles");
		System.out.println("2. Restore a general group or Special Access Group of articles");
		System.out.println("Please enter the restoration option you would like to proceed with: ");
		String restoreOption = scanner.nextLine();
		String fileName = "";

		// swtich case method
		switch (restoreOption){
			case "1": {
				System.out.println("Please enter the name of the file you would like to restore from: ");
				fileName = scanner.nextLine();
				try {
					restoreAllArticles(role, fileName);
				} catch (Exception e) {
					System.err.println("Error during restoration: " + e.getMessage());
				}
				break;
			}
			case "2": {
				System.out.println("Please enter the name of the general group or Special Access Group of articles you would like to restore: ");
				String group = scanner.nextLine();
				System.out.println("Please enter the name of the file you would like to restore from: ");
				fileName = scanner.nextLine();
				restoreGroupArticles(role, fileName, group);
				break;
			}
			default: {
				System.out.println("Invalid Option. Try again later.");
				break;
			}
		}

	}

	public void restoreGroupArticles(String role, String fileName, String group){
		System.out.println("Restoring group articles");
	}

	// restore from user specified file
	public void restoreAllArticles(String role, String file) throws Exception {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		// check if the file exists
		File f = new File(file);
		if (!f.exists()) {
			System.out.println("File does not exist");
			return;
		}

		// clear the articles table
		String clearArticles = "DELETE FROM articles";
		statement.execute(clearArticles);


		// read each line of the file and insert into the article database
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				int id = Integer.parseInt(line);
				String level = reader.readLine();
				String authors = reader.readLine();
				String title = reader.readLine();
				String shortDescription = reader.readLine();
				String keywords = reader.readLine();
				String body = reader.readLine();
				String referenceLinks = reader.readLine();

				// insert into the database or update
				String insertArticle = "INSERT INTO articles (id, level, authors, title, short_description, keywords, body, reference_links) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
					pstmt.setInt(1, id);
					pstmt.setString(2, level.isEmpty() ? null : level);
					pstmt.setString(3, authors.isEmpty() ? null : authors);
					pstmt.setString(4, title.isEmpty() ? null : title);
					pstmt.setString(5, shortDescription.isEmpty() ? null : shortDescription);
					pstmt.setString(6, keywords.isEmpty() ? null : keywords);
					pstmt.setString(7, body.isEmpty() ? null : body);
					pstmt.setString(8, referenceLinks.isEmpty() ? null : referenceLinks);
					pstmt.executeUpdate();
				}
			}
			System.out.println("Successfully restored system");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// take all the articles in the system from the specific group through the database and backit up to the filename
	public void backupMethod(String role){
		System.out.println("Here are your backup options: ");
			System.out.println("1. Backup all articles");
			System.out.println("2. Backup a general group or Special Access Group of articles");
			System.out.println("Please enter the restoration option you would like to proceed with: ");
			String backUp = scanner.nextLine();
			String fileName = "";

		
			switch (backUp) {

				case "1": {

					System.out.println("Please enter the name of the file you would like to backup to: ");
					fileName = scanner.nextLine();
					try {
						backupAllArticles(role, fileName);
					} catch (Exception e) {
						System.err.println("Error during backup: " + e.getMessage());
					}
					break;
				}

				case "2": {

					System.out.println("Please enter the name of the general group or Special Access Group of articles you would like to backup: ");
					String group = scanner.nextLine();
					System.out.println("Please enter the name of the file you would like to backup to: ");
					fileName = scanner.nextLine();
					try {
						backupGroupArticles(role, fileName, group);
					} catch (SQLException | IOException e) {
						System.err.println("Error during backup: " + e.getMessage());
					}
					break;
				}

				default: {
					System.out.println("Invalid Option. Try again later.");
					break;
				}

			}
	}
	
	// backupAllArticles
	// 1. backup all help articles to an external file
	public void backupAllArticles(String role, String file) throws Exception {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		// Check to see if there is anything to back up at all
		if (isDatabaseEmpty()) {
			System.out.println("There are no articles in the system to back up.");
			return;
		}

		// Get all entries in the table
		String sql = "SELECT * FROM articles";
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

			// Each article will have 6 lines in the text file, every 6 lines corresponds to
			// an entry
			while (rs.next()) {
				// For each entry - get all fields and store them in their encrypted states
				int id = rs.getInt("id");
				String level = rs.getString("level");
				String authors = rs.getString("authors");
				String title = rs.getString("title");
				String shortDescription = rs.getString("short_description");
				String keywords = rs.getString("keywords");
				String body = rs.getString("body");
				String referenceLinks = rs.getString("reference_links");

				// write each field on its own line
				writer.write(String.valueOf(id));
				writer.newLine();
				writer.write(level);
				writer.newLine();
				writer.write(authors);
				writer.newLine();
				writer.write(title);
				writer.newLine();
				writer.write(shortDescription);
				writer.newLine();
				writer.write(keywords);
				writer.newLine();
				writer.write(body);
				writer.newLine();
				writer.write(referenceLinks);
				writer.newLine();
			}
			System.out.println("Successfully backed up system");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void backupGroupArticles(String role, String fileName, String articleGroup) throws SQLException, IOException {
        // SQL query to select articles from the specified group

		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
	

        String sqlQuery = "SELECT a.* FROM articleGroups ag JOIN articles a ON ag.article_id = a.id WHERE ag.group_name = ?";
        
        // Try-with-resources for database connection and prepared statement
        PreparedStatement pstmt = connection.prepareStatement(sqlQuery);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            
            // Set the group parameter
        pstmt.setString(1, articleGroup);
            
            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                int articleCount = 0;
                
                // Process each result
                while (rs.next()) {
                    // Write article details to file
					writer.write("group_name: " + articleGroup);
					writer.newLine();
					writer.write("ID: " + rs.getString("id"));
					writer.newLine();
					writer.write("Level: " + rs.getString("level"));
					writer.newLine();
					writer.write("Authors: " + rs.getString("authors"));
					writer.newLine();
                    writer.write("Title: " + rs.getString("title"));
                    writer.newLine();
					writer.write("Short Description: " + rs.getString("short_description"));
					writer.newLine();
					writer.write("Keywords: " + rs.getString("keywords"));
					writer.newLine();
                    writer.write("Body: " + rs.getString("body"));
                    writer.newLine();
					writer.write("Reference Links: " + rs.getString("reference_links"));
					writer.newLine();
                    writer.write("-".repeat(50)); // Article separator
                    writer.newLine();
                    
                    articleCount++;
                }
                
                // Check if any articles were found
                if (articleCount == 0) {
                    System.out.println("No articles found in group: " + articleGroup);
                } else {
                    System.out.println("Successfully backed up " + articleCount + 
                                       " articles from group '" + articleGroup + "' to " + fileName);
                }
				writer.close();
            }
    }
		

	// 1. remove all existing help articles
	public void deleteAllArticles(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
	

        String sqlQuery = "SELECT a.* FROM articleGroups ag JOIN articles a ON ag.article_id = a.id WHERE ag.group_name = ?";
        
        // Try-with-resources for database connection and prepared statement
        PreparedStatement pstmt = connection.prepareStatement(sqlQuery);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            
            // Set the group parameter
        pstmt.setString(1, articleGroup);
            
            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                int articleCount = 0;
                
                // Process each result
                while (rs.next()) {
                    // Write article details to file
					writer.write("group_name: " + articleGroup);
					writer.newLine();
					writer.write("ID: " + rs.getString("id"));
					writer.newLine();
					writer.write("Level: " + rs.getString("level"));
					writer.newLine();
					writer.write("Authors: " + rs.getString("authors"));
					writer.newLine();
                    writer.write("Title: " + rs.getString("title"));
                    writer.newLine();
					writer.write("Short Description: " + rs.getString("short_description"));
					writer.newLine();
					writer.write("Keywords: " + rs.getString("keywords"));
					writer.newLine();
                    writer.write("Body: " + rs.getString("body"));
                    writer.newLine();
					writer.write("Reference Links: " + rs.getString("reference_links"));
					writer.newLine();
                    writer.write("-".repeat(50)); // Article separator
                    writer.newLine();
                    
                    articleCount++;
                }
                
                // Check if any articles were found
                if (articleCount == 0) {
                    System.out.println("No articles found in group: " + articleGroup);
                } else {
                    System.out.println("Successfully backed up " + articleCount + 
                                       " articles from group '" + articleGroup + "' to " + fileName);
                }
				writer.close();
            }
    }
		

	// 1. remove all existing help articles
	public void deleteAllArticles(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		String deleteAllArticles = "DELETE FROM articles";
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(deleteAllArticles);
		}
	}

	/**
	 * Restore the articles from a file
	 * 
	 * @param roles
	 * @param file
	 * @throws Exception
	 */
	public void restoreMerge(String roles, String file) throws Exception {
		// if (roles.equals("s")) {
		// 	System.out.println("Invalid role");
		// 	return;
		// }
		// boolean myFlag = false;

		// try (BufferedReader reads = new BufferedReader(new FileReader(file))) {
		// 	// counter keeps track of lines - every 7 we need to insert an article into the
		// 	// table
		// 	int counter = 0;
		// 	// line is what the lines from the file will contain
		// 	String line = "";
		// 	String id = "";
		// 	int tmpId = -1;
		// 	String level = "";
		// 	String group_id = "";
		// 	String title = "";
		// 	String short_description = "";
		// 	String keywords = "";
		// 	String body = "";
		// 	String reference_links = "";

		// 	// read the file line by line
		// 	while ((line = reads.readLine()) != null) {
		// 		switch ((counter % 8)) {

		// 			case 0 -> {
		// 				if (counter == 0) {
		// 					id = line;
		// 					tmpId = Integer.parseInt(id);
		// 					String possQuery = "Select count(*) from articles where id = ?";
		// 					try (PreparedStatement pstmt2 = connection.prepareStatement(possQuery)) {
		// 						pstmt2.setInt(1, tmpId);
		// 						try (ResultSet rs = pstmt2.executeQuery()) {
		// 							if (rs.next()) {
		// 								// If the count is greater than 0, the user exists
		// 								if (rs.getInt(1) == 0)
		// 									myFlag = true;
		// 							}
		// 						}
		// 					}
		// 					break;
		// 				}
		// 				// ignore the id since it will auto generate upon table entry
		// 				String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		// 				System.out.println("Inserting article: " + id);
		// 				if (myFlag) {
		// 					try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

		// 						// pstmt.setString(1, id);
		// 						pstmt.setString(1, level);
		// 						pstmt.setString(2, group_id);
		// 						pstmt.setString(3, title);
		// 						pstmt.setString(4, short_description);
		// 						pstmt.setString(5, keywords);
		// 						pstmt.setString(6, body);
		// 						pstmt.setString(7, reference_links);
		// 						pstmt.setInt(8, tmpId);

		// 						pstmt.executeUpdate();
		// 						System.out.println("An article has been added successfully to the system!");
		// 						myFlag = false;
		// 					}
		// 				}

		// 				if (counter > 0) {
		// 					id = line;
		// 					tmpId = Integer.parseInt(id);
		// 					String possQuery = "Select count(*) from articles where id = ?";
		// 					try (PreparedStatement pstmt2 = connection.prepareStatement(possQuery)) {
		// 						pstmt2.setInt(1, tmpId);
		// 						try (ResultSet rs = pstmt2.executeQuery()) {
		// 							if (rs.next()) {
		// 								// If the count is greater than 0, the user exists
		// 								if (rs.getInt(1) == 0)
		// 									myFlag = true;
		// 							}
		// 						}
		// 					}
		// 				}
		// 				break;
		// 			}

		// 			case 1 -> {
		// 				level = line;
		// 				break;
		// 			}
		// 			case 2 -> {
		// 				group_id = line;
		// 				break;
		// 			}
		// 			case 3 -> {
		// 				title = line;
		// 				break;
		// 			}
		// 			case 4 -> {
		// 				short_description = line;
		// 				break;
		// 			}
		// 			case 5 -> {
		// 				keywords = line;
		// 				break;
		// 			}
		// 			case 6 -> {
		// 				body = line;
		// 				break;
		// 			}

		// 			case 7 -> {
		// 				reference_links = line;
		// 				break;
		// 			}
		// 			/*
		// 			 * case 8 -> {
		// 			 * reference_links = line;
		// 			 * }
		// 			 */
		// 			default -> {
		// 				System.out.println("Something went wrong. Try again later.");
		// 			}
		// 		}
		// 		counter++;
		// 	}

		// 	if (counter > 0 && myFlag) {
		// 		String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		// 		try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

		// 			// pstmt.setString(1, id);
		// 			pstmt.setString(1, level);
		// 			pstmt.setString(2, title);
		// 			pstmt.setString(3, group_id);
		// 			pstmt.setString(4, short_description);
		// 			pstmt.setString(5, keywords);
		// 			pstmt.setString(6, body);
		// 			pstmt.setString(7, reference_links);
		// 			tmpId = Integer.parseInt(id);
		// 			pstmt.setInt(8, tmpId);

		// 			pstmt.executeUpdate();
		// 			System.out.println("An article has been added successfully to the system!");
		// 		}
		// 	}

		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }
		System.out.println("Restoring from backup file...");
	}

	/**
	 * Restore the articles from a file
	 * 
	 * @param roles
	 * @param file
	 * @throws Exception
	 */
	public void restore(String roles, String file) throws Exception {
		// if (roles.equals("s")) {
		// 	System.out.println("Invalid role");
		// 	return;
		// }
		// if (!isDatabaseEmpty()) {
		// 	String sql = "TRUNCATE TABLE articles";

		// 	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
		// 		pstmt.executeUpdate();
		// 		System.out.println("Successfully cleared out all articles");
		// 	} catch (SQLException e) {
		// 		e.printStackTrace();
		// 	}
		// }

		// try (BufferedReader reads = new BufferedReader(new FileReader(file))) {
		// 	// counter keeps track of lines - every 7 we need to insert an article into the
		// 	// table
		// 	int counter = 0;
		// 	// line is what the lines from the file will contain
		// 	String line = "";
		// 	String id = "";
		// 	int tmpId = -1;
		// 	String level = "";
		// 	String group_id = "";
		// 	String title = "";
		// 	String short_description = "";
		// 	String keywords = "";
		// 	String body = "";
		// 	String reference_links = "";

		// 	// read the file line by line
		// 	while ((line = reads.readLine()) != null) {
		// 		switch ((counter % 8)) {

		// 			case 0 -> {
		// 				if (counter == 0) {
		// 					id = line;
		// 					tmpId = Integer.parseInt(id);
		// 					break;
		// 				}
		// 				// ignore the id since it will auto generate upon table entry
		// 				String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		// 				System.out.println("Inserting article: " + id);
		// 				try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

		// 					// pstmt.setString(1, id);
		// 					pstmt.setString(1, level);
		// 					pstmt.setString(2, group_id);
		// 					pstmt.setString(3, title);
		// 					pstmt.setString(4, short_description);
		// 					pstmt.setString(5, keywords);
		// 					pstmt.setString(6, body);
		// 					pstmt.setString(7, reference_links);
		// 					pstmt.setInt(8, tmpId);

		// 					pstmt.executeUpdate();
		// 					System.out.println("An article has been added successfully to the system!");
		// 				}
		// 				if (counter > 0) {
		// 					id = line;
		// 					tmpId = Integer.parseInt(id);
		// 				}
		// 				break;
		// 			}

		// 			case 1 -> {
		// 				level = line;
		// 				break;
		// 			}
		// 			case 2 -> {
		// 				group_id = line;
		// 				break;
		// 			}
		// 			case 3 -> {
		// 				title = line;
		// 				break;
		// 			}
		// 			case 4 -> {
		// 				short_description = line;
		// 				break;
		// 			}
		// 			case 5 -> {
		// 				keywords = line;
		// 				break;
		// 			}
		// 			case 6 -> {
		// 				body = line;
		// 				break;
		// 			}

		// 			case 7 -> {
		// 				reference_links = line;
		// 				break;
		// 			}
		// 			/*
		// 			 * case 8 -> {
		// 			 * reference_links = line;
		// 			 * }
		// 			 */
		// 			default -> {
		// 				System.out.println("Something went wrong. Try again later.");
		// 			}
		// 		}
		// 		counter++;
		// 	}

		// 	if (counter > 0) {
		// 		String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		// 		try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

		// 			// pstmt.setString(1, id);
		// 			pstmt.setString(1, level);
		// 			pstmt.setString(2, title);
		// 			pstmt.setString(3, group_id);
		// 			pstmt.setString(4, short_description);
		// 			pstmt.setString(5, keywords);
		// 			pstmt.setString(6, body);
		// 			pstmt.setString(7, reference_links);
		// 			tmpId = Integer.parseInt(id);
		// 			pstmt.setInt(8, tmpId);

		// 			pstmt.executeUpdate();
		// 			System.out.println("An article has been added successfully to the system!");
		// 		}
		// 	}

		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }
		System.out.println("Restoring from backup file...");
	}

	/**
	 * Create a new group in the database
	 * 
	 * @param groups
	 * @throws SQLException
	 */
	public void createGroups(String[] groups) throws SQLException {
		for(String curGroup : groups) {
			if(!groupExist(curGroup)) {
				String insertGroup = "INSERT INTO groups (name, specialFlag) VALUES (?, ?)";
				try(PreparedStatement pstmt = connection.prepareStatement(insertGroup)) {
					pstmt.setString(1, curGroup);
					pstmt.setBoolean(2, false);

					pstmt.executeUpdate();
					System.out.println("A new general article group: " + curGroup + " has been made.");
				} catch (SQLException e) {
					System.err.println("DB error while creating new general groups: " + e.getMessage());
				}
			}
		}
	}

	public void listAllGroups(boolean general) throws SQLException{
		String query = "SELECT name from groups where specialFlag = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setBoolean(1, general);
			try(ResultSet rs = pstmt.executeQuery()) {
				System.out.println("Here are the group names: ");
				while(rs.next()) {
					System.out.println(rs.getString(1));
				}	
			}
		} catch (SQLException e) {
			System.err.println("DB issue that lists all general or specific groups: " + e.getMessage());
		}
	}

	public boolean createSpecialGroup(String name) throws SQLException {
		if(groupExist(name) || isGroupSpecial(name)) {
			System.out.println("This group either already exits or is a special group already. Please try again later.");
			return false;
		}

		String insertGroup = "INSERT INTO groups (name, specialFlag) VALUES (?, ?)";
				try(PreparedStatement pstmt = connection.prepareStatement(insertGroup)) {
					pstmt.setString(1, name);
					pstmt.setBoolean(2, true);

					pstmt.executeUpdate();
					System.out.println("A new special access group: " + name + " has been made.");
					return true;
				} catch(SQLException e) {
					System.err.println("DB issue making SAG: " + e.getMessage());
				}
		return false;
	}

	/**
	 * Link an article to a group in the database
	 * 
	 * @param groupName
	 * @param articleID
	 * @throws SQLException
	 */
	public void linkArticleGroup(String groupName, int articleID) throws SQLException {
		if(!groupExist(groupName)) {
			System.out.println("Trying to link to a group that does not exist");
			return;
		}

		String insertQuery = "INSERT INTO articleGroups (group_name, article_id) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
			pstmt.setString(1, groupName);
			pstmt.setInt(2, articleID);

			pstmt.executeUpdate();
			System.out.println("Article linked successfully");
		} catch (SQLException e) {
			System.err.println("DB issue while linking article to group");
		}
	}

	/**
	 * Link a user to a group in the database
	 * 
	 * @param groupName
	 * @param userId
	 * @param roleFlag
	 * @param adminPerms
	 * @param viewPerms
	 * @throws SQLException
	 */
	public void linkUserGroup(String groupName, int userId, String roleFlag, boolean adminPerms, boolean viewPerms) throws SQLException {
		String linkQuery = "INSERT INTO groupRights (user_id, group_name, accessRole, adminRightsFlag, viewRightsFlag) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(linkQuery)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, groupName);
			pstmt.setString(3, roleFlag);
			pstmt.setBoolean(4, adminPerms);
			pstmt.setBoolean(5, viewPerms);
			pstmt.executeUpdate();
			System.out.println("User successfully linked to group: " + groupName);
		} catch (SQLException e) {
			System.err.println("DB issue linking user to group: " + e.getMessage());
			throw e; // Re-throw for transactional consistency
		}
	}

	/**
	 * Delete a user's access to a special group in the database
	 * 
	 * @param gName
	 * @param userId
	 * @throws SQLException
	 */
	public void delUserGroup(String gName, int userId) throws SQLException{
		String delQuery = "DELETE FROM groupRights WHERE group_name = ? AND user_id = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(delQuery)) {
			pstmt.setString(1, gName);
			pstmt.setInt(2, userId);

			pstmt.executeUpdate();	
		} catch (SQLException e) {
			System.err.println("DB issue with deleting a user's access to a speical group: " + e.getMessage());
		}
	}
	
	public void delEntireGroup(String gName) throws SQLException {
		String delQuery = "DELETE FROM groups where name = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(delQuery)) {
			pstmt.setString(1, gName);
			int rowsAffected = pstmt.executeUpdate();
			if(rowsAffected >= 1) System.out.println("A group was deleted");
			else System.out.println("There was no group to delete");
		} catch(SQLException e) {
			System.err.println("DB issue with trying to delete an entire group");
		}
	}
	

	/**
	 * Create a new article in the database
	 * 
	 * @param level
	 * @param groupId
	 * @param title
	 * @param shortDescription
	 * @param keywords
	 * @param body
	 * @param referenceLinks
	 * @throws SQLException
	 */
	public void createArticle(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		System.out.println("Enter article level (beginner, intermediate, advanced, expert): ");
		String level = scanner.nextLine();

		System.out.println("Enter the authors of this article (Please make sure there are no spaces and that they are comma separated) (e.g. Einstein,Oppenheimer,Suess): ");
		String authors = scanner.nextLine();

		System.out.println(
				"Enter group ID (Please make sure there are no spaces and that they are comma separated) (e.g. CSE360,CSE360-01,CSE360-02): ");
		String groupId = scanner.nextLine() + ",";

		String[] groups = groupId.trim().split(",");
		boolean failed = false;
		for(String curGroup : groups) {
			if(groupExist(curGroup) && isGroupSpecial(curGroup)) {
					System.out.println("You are unable to create an article for the following group: " + curGroup);
					System.out.println("As an admin or adding articles from outside the specific menu option, you are unable to make articles for Special Access Groups");
					failed = true;
					break;
			}
		}
		if(failed) return;

		createGroups(groups);

		System.out.println("Enter article title: ");
		String title = scanner.nextLine();

		System.out.println("Enter short description: ");
		String shortDescription = scanner.nextLine();

		System.out.println("Enter keywords (comma separated): ");
		String[] keywords = scanner.nextLine().split(",");

		System.out.println("Enter article body: ");
		String body = scanner.nextLine();

		// article encryption
		//String encryptedBody = encryptionHelper.encrypt(body);

		System.out.println("Enter reference links (comma separated): ");
		String[] referenceLinks = scanner.nextLine().split(",");

		int tempId = createArticleId();

		String insertArticle = "INSERT INTO articles (level, title, short_description, keywords, body, reference_links, id, authors) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
			pstmt.setString(1, level);
			pstmt.setString(2, title);
			pstmt.setString(3, shortDescription);
			pstmt.setArray(4, connection.createArrayOf("VARCHAR", keywords));
			pstmt.setString(5, body);
			pstmt.setArray(6, connection.createArrayOf("VARCHAR", referenceLinks));
			pstmt.setInt(7, tempId);
			pstmt.setString(8, authors);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("DB issue while inserting article into table");
		}

		//link each group to an article
		for(String curGroup : groups) linkArticleGroup(curGroup, tempId);
	}

	//Only to check if Instructors have Admin Rights for a SAG
	public boolean checkSpecialAdminAccess(int instructId, String groupName) throws SQLException {
		String checkSpecial = "SELECT adminRightsFlag FROM groupRights WHERE user_id = ? AND group_name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(checkSpecial)) {
			pstmt.setInt(1, instructId);
			pstmt.setString(2, groupName);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("DB issue: Unable to check admin access for the group: " + groupName + ". " + e.getMessage());
			throw e; // Re-throw for better error handling
		}
		return false;
	}
	
	/**
	 * Check if a user has view access to a special group in the database
	 * 
	 * @param userId
	 * @param groupName
	 * @return true if the user has view access, false otherwise
	 * @throws SQLException
	 */
	public boolean checkSpecialViewAccess(int userId, String groupName) throws SQLException {
		String checkSpecial = "SELECT viewRightsFlag from groupRights where user_id = ? and group_name = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(checkSpecial)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, groupName);

			try(ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					return rs.getBoolean(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("DB issue: unable to check if user has view access to a special group");
		}
		return false;
	}

	public void createInstructArticle(User curUser) throws SQLException {
		
		int curId = getUserId(curUser.getUsername(), curUser.getEmail());
		if (curId == -1) {
			System.out.println("User not found. Cannot create article.");
			return;
		}

		System.out.println("Enter article level (Beginner, Intermediate, Advanced, or Expert): ");
		String level = scanner.nextLine();
		System.out.println("Enter authors (comma-seprated with no spaces): ");
		String authors = scanner.nextLine();
		authors.concat(",");
		System.out.println("Enter group IDs (comma-seprated with no spaces): ");
		String groupId = scanner.nextLine();
		groupId.concat(",");
		String[] groups = groupId.split(",");

		boolean needEncryption = false;
		for (String group : groups) {
			if (groupExist(group)) {
				if (isGroupSpecial(group) && !checkSpecialAdminAccess(curId, group)) {
					System.out.println("No admin rights for special group: " + group);
					return;
				}
				needEncryption = true;
			} else {
				System.out.println("Group " + group + " does not exist.");
			}
		}

		createGroups(groups);

		System.out.println("Enter article title: ");
        String title = scanner.nextLine();
        System.out.println("Enter description: ");
        String shortDescription = scanner.nextLine();
        System.out.println("Enter keywords (comma-seprated with no spaces): ");
        String[] keywords = scanner.nextLine().split(",");
        System.out.println("Enter body: ");
        String body = scanner.nextLine();

        if (needEncryption) {
            body = encryptionHelper.encrypt(body);
        }

        System.out.println("Enter reference links (comma-seprated with no spaces): ");
        String[] referenceLinks = scanner.nextLine().split(",");

        int tempId = createArticleId();
        String insertArticle = "INSERT INTO articles (level, title, short_description, keywords, body, reference_links, id, authors) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
            pstmt.setString(1, level);
            pstmt.setString(2, title);
            pstmt.setString(3, shortDescription);
            pstmt.setArray(4, connection.createArrayOf("VARCHAR", keywords));
            pstmt.setString(5, body);
            pstmt.setArray(6, connection.createArrayOf("VARCHAR", referenceLinks));
            pstmt.setInt(7, tempId);
            pstmt.setString(8, authors);
            pstmt.executeUpdate();
        } catch(SQLException e) {
			System.err.println("DB issue: could not properly create article for instructor: " + e.getMessage());
		}

        // Link groups to article
        for (String group : groups) {
            linkArticleGroup(group, tempId);
        }
	}


	/**
	 * Update an existing article in the database
	 * 
	 * @param role
	 * @throws SQLException
	 */
	public void updateArticle(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		System.out.println("Enter article ID: ");
		int id = Integer.parseInt(scanner.nextLine());

		System.out.println("Enter article level (beginner, intermediate, advanced, expert): ");
		String level = scanner.nextLine();

		System.out.println("Enter author(s) (Please make sure there are no spaces and that they are comma separated) (e.g. Einstein,Oppenheimer,Suess)");
		String authors = scanner.nextLine();
		authors.concat(",");

		System.out.println(
				"Enter group ID (Please make sure there are no spaces and that they are comma separated) (e.g. CSE360,CSE360-01,CSE360-02): ");
		String groupId = scanner.nextLine() + ",";

		String[] groups = groupId.trim().split(",");
		boolean failed = false;
		for(String curGroup : groups) {
			if(groupExist(curGroup) && isGroupSpecial(curGroup)) {
					System.out.println("You are unable to update and assign an article to following Speical Access group: " + curGroup);
					System.out.println("Please do not attempt to promote existing articles to Special Access Groups in the future");
					failed = true;
					break;
			}
		}
		if(failed) return;

		createGroups(groups);

		System.out.println("Enter article title: ");
		String title = scanner.nextLine();

		System.out.println("Enter short description: ");
		String shortDescription = scanner.nextLine();

		System.out.println("Enter keywords (comma separated): ");
		String keywords = scanner.nextLine();

		System.out.println("Enter article body: ");
		String body = scanner.nextLine();

		System.out.println("Enter reference links (comma separated): ");
		String referenceLinks = scanner.nextLine();

		String updateArticle = "UPDATE articles SET level = ?, title = ?, short_description = ?, keywords = ?, body = ?, reference_links = ?, authors = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateArticle)) {
			pstmt.setString(1, level);
			pstmt.setString(2, title);
			pstmt.setString(3, shortDescription);
			pstmt.setString(4, keywords);
			pstmt.setString(5, body);
			pstmt.setString(6, referenceLinks);
			pstmt.setString(7, authors);
			pstmt.setInt(8, id);
			pstmt.executeUpdate();
		} catch(SQLException e) {
			System.err.println("DB issue while instructor updating articles");
		}
	}

	/**
	 * View all articles in the database
	 * 
	 * @param role
	 * @throws SQLException
	 */
	public void viewAllArticles(int userId) throws SQLException {	

		System.out.println("All articles you can view:");


		String query = "SELECT articles.id, articles.short_description, articles.authors, articles.title FROM groupRights JOIN groups on groupRights.group_name = groups.name JOIN articleGroups on groups.name = articleGroups.group_name JOIN articles on articleGroups.article_id = articles.id WHERE groupRights.user_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			try(ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
				int id = rs.getInt(1);
				String authors = rs.getString(3);
				String shortDescription = rs.getString(2);
				String title = rs.getString(4);

				System.out.println("ID: " + id);
				System.out.println("Authors: " + authors);
				System.out.println("Title: " + title);
				System.out.println("Short Description: " + shortDescription);	
				}
			}

		} catch(SQLException e) {
			System.err.println("DB issue trying to view all articles: " + e.getMessage());
		}
	}

	public void viewGroupedArticles(int uId, String group) throws SQLException {

		String query = "SELECT articles.id, articles.short_description, articles.authors, articles.title FROM groupRights "
		+ "JOIN groups on groupRights.group_name = groups.name " 
		+ "JOIN articleGroups on groups.name = articleGroups.group_name "
		+ "JOIN articles on articleGroups.article_id = articles.id "
		+ "WHERE groupRights.user_id = ? AND groups.name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, uId);
			pstmt.setString(2, group);
			ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					String authors = rs.getString(3);
					String shortDescription = rs.getString(2);
					String title = rs.getString(4);
				

					System.out.println("Article ID: " + id);
					System.out.println("Authors: " + authors);
					System.out.println("Title: " + title);
					System.out.println("Short Description: " + shortDescription);	
				}
			
		} catch (SQLException e) {
				System.err.println("DB issue while viewing Grouped articles: " + e.getMessage());
			}
	}

	public void viewContentArticles(int uId, String contentLevel) throws SQLException {	
		
		String query = "SELECT articles.id, articles.short_description, articles.authors, articles.title FROM groupRights "
		+ "JOIN groups on groupRights.group_name = groups.name " 
		+ "JOIN articleGroups on groups.name = articleGroups.group_name "
		+ "JOIN articles on articleGroups.article_id = articles.id "
		+ "WHERE groupRights.user_id = ? AND articles.level = ?";


		// String query = "SELECT articles.id, articles.short_description, articles.authors, articles.title FROM articles "
		// + "JOIN articleGroups on articleGroups.article_id = articles.id "
		// + "JOIN groups on articleGroups.group_name = groups.name "
		// + "WHERE articles.level = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, uId);
			pstmt.setString(2, contentLevel);
			ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					String authors = rs.getString(3);
					String shortDescription = rs.getString(2);
					String title = rs.getString(4);
				

					System.out.println("Article ID: " + id);
					System.out.println("Authors: " + authors);
					System.out.println("Title: " + title);
					System.out.println("Short Description: " + shortDescription);	
				}
			
		} catch (SQLException e) {
				System.err.println("DB issue while viewing Grouped articles: " + e.getMessage());
			}
	}
	

	/**
	 * Get all groups for one article
	 * 
	 * @param articleId
	 * @return an ArrayList of group names
	 */
	public ArrayList<String> getGroupsForAnArticle(int articleId) {
		ArrayList<String> tempList = new ArrayList<>();
		String query = "SELECT name from groups "
		+ "JOIN articleGroups on groups.name = articleGroups.group_name "
		+ "JOIN articles on articleGroups.article_id = articles.id "
		+ "WHERE articles.id = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, articleId);

			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				tempList.add(rs.getString(1));
			}
			
			return tempList;
		} catch(SQLException e) {
			System.err.println("DB issue while getting the groups for an article: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Check if a user has access to a list of groups
	 * 
	 * @param curUser
	 * @param groups
	 * @return true if the user has access, false otherwise
	 * @throws SQLException
	 */
	public boolean articleAuth(User curUser, ArrayList<String> groups) throws SQLException{
		int userId = getUserId(curUser.getUsername(), curUser.getEmail());
		for(int i = 0; i < groups.size(); i++) {
			String curGroup = groups.get(i);
			if(isGroupSpecial(curGroup)) {
				if(!checkSpecialViewAccess(userId, curGroup)) return false;
			}
		}
		return true;
	}

	/**
	 * Check if a user has deletion access to a list of groups
	 * 
	 * @param curUser
	 * @param groups
	 * @return true if the user has deletion access, false otherwise
	 * @throws SQLException
	 */
	public boolean articleDelAuth(User curUser, ArrayList<String> groups) throws SQLException{
		int userId = getUserId(curUser.getUsername(), curUser.getEmail());
		for(int i = 0; i < groups.size(); i++) {
			String curGroup = groups.get(i);
			if(isGroupSpecial(curGroup)) {
				if(!checkSpecialAdminAccess(userId, curGroup)) return false;
			}
		}
		return true;
	}

	/**
	 * Check if a user has encrypted access to a list of groups
	 * 
	 * @param curUser
	 * @param groups
	 * @return true if the user has encrypted access, false otherwise
	 * @throws SQLException
	 */
	public boolean articleEncrypted(User curUser, ArrayList<String> groups) throws SQLException{
		int userId = getUserId(curUser.getUsername(), curUser.getEmail());
		for(int i = 0; i < groups.size(); i++) {
			String curGroup = groups.get(i);
			if(isGroupSpecial(curGroup)) {
				if(checkSpecialViewAccess(userId, curGroup)) return true;
			}
		}
		return false;
	}	

	/**
	 * View an article from the database
	 * 
	 * @param role
	 * @param articleId
	 * @param encrypted
	 * @throws SQLException
	 */
	public void viewArticle(String role, String articleId, boolean encrypted) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		int id = Integer.parseInt(articleId);

		String query = "SELECT * FROM articles WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String level = rs.getString("level");
					String authors = rs.getString("authors");
					String title = rs.getString("title");
					String shortDescription = rs.getString("short_description");
					String keywords = rs.getString("keywords");
					String body = rs.getString("body");
					if(encrypted) body = encryptionHelper.decrypt(body);
					String referenceLinks = rs.getString("reference_links");

					System.out.println("ID: " + id);
					System.out.println("Level: " + level);
					System.out.println("Authors: " + authors);
					System.out.println("Title: " + title);
					System.out.println("Short Description: " + shortDescription);
					System.out.println("Keywords: " + keywords);
					System.out.println("Body: " + body);
					System.out.println("Reference Links: " + referenceLinks);
				}
			}
		}
	}

	/**
	 * Delete an article from the database
	 * 
	 * @param role
	 * @return boolean that represents if article was deleted
	 * @throws SQLException
	 */
	public boolean deleteArticle(User curUser) throws SQLException {

		System.out.println("Enter article ID: ");
		int id = Integer.parseInt(scanner.nextLine());

		ArrayList<String> temp = getGroupsForAnArticle(id);
		if(!articleDelAuth(curUser, temp)) {
			System.out.println("You are not authorized to delete this article.");
			return false;
		}

		String deleteArticle = "DELETE FROM articles WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
			pstmt.setInt(1, id);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		} catch(SQLException e) {
			System.err.println("DB issue while trying to delete an article");
		}
		return false;
	}

	/**
	 * List special users with varying admin and view permissions
	 * 
	 * @param roleFlag
	 * @param adminRights
	 * @param gName
	 * @throws SQLException
	 */
	public void listSpecUsers(String roleFlag, boolean adminRights, String gName) throws SQLException {
		String username, email, pref;
		
		if(adminRights) {
			String sql = "SELECT cse360users.username, cse360users.email, cse360users.preferredFirst from cse360users "
				+ "JOIN groupRights on cse360users.id = groupRights.user_id "
				+ "WHERE groupRights.group_name = ? AND groupRights.accessRole = ? AND groupRights.adminRightsFlag = ?";

				try(PreparedStatement pstmt = connection.prepareStatement(sql)) {
					pstmt.setString(1, gName);
					pstmt.setString(2, roleFlag);
					pstmt.setBoolean(3, true);

					try(ResultSet rs = pstmt.executeQuery()) {
						while(rs.next()) {
							username = rs.getString(1);
							email = rs.getString(2);
							pref = rs.getString(3);

							System.out.print("Username: " + username);
							System.out.print(", Email: " + email);
							System.out.println(", Preferred First Name: " + pref);	
						}
					}
				} catch (SQLException e) {
					System.err.println("DB issue displaying special users with varying admin permissions: " + e.getMessage());
				}
		} else {
			String sql = "SELECT cse360users.username, cse360users.email, cse360users.preferredFirst from cse360users "
				+ "JOIN groupRights on cse360users.id = groupRights.user_id "
				+ "WHERE groupRights.group_name = ? AND groupRights.accessRole = ? AND groupRights.viewRightsFlag = ?";

				try(PreparedStatement pstmt = connection.prepareStatement(sql)) {
					pstmt.setString(1, gName);
					pstmt.setString(2, roleFlag);
					pstmt.setBoolean(3, true);

					try(ResultSet rs = pstmt.executeQuery()) {
						while(rs.next()) {
							username = rs.getString(1);
							email = rs.getString(2);
							pref = rs.getString(3);

							System.out.print("Username: " + username);
							System.out.print(", Email: " + email);
							System.out.println(", Preferred First Name: " + pref);	
						}
					}
				} catch (SQLException e) {
					System.err.println("DB issue displaying special users with varying viewing permissions: " + e.getMessage());
				}	
		}
				

			
	}


	public void listAllGroupUsers(String gName) throws SQLException {
		String username, email, pref, accRole;
		boolean admin, view;
		
		
			String sql = "SELECT cse360users.username, " 
			+ "cse360users.email, "
			+ "cse360users.preferredFirst, " 
			+ "groupRights.accessRole, " 
			+ "groupRights.adminRightsFlag, " 
			+ "groupRights.viewRightsFlag " 
			+ "FROM cse360users " 
			+ "JOIN groupRights " 
			+ "ON cse360users.id = groupRights.user_id " 
			+ "WHERE groupRights.group_name = ?";

				try(PreparedStatement pstmt = connection.prepareStatement(sql)) {
					pstmt.setString(1, gName);

					try(ResultSet rs = pstmt.executeQuery()) {
						while(rs.next()) {
							username = rs.getString(1);
							email = rs.getString(2);
							pref = rs.getString(3);
							accRole = rs.getString(4);
							
								admin = rs.getBoolean(5);
								view = rs.getBoolean(6);
							

							System.out.print("Username: " + username);
							System.out.print(", Email: " + email);
							System.out.print(", Preferred First Name: " + pref);
							if(admin) System.out.print(", Admin Rights");
							if(view && !accRole.equals("a")) System.out.print(", View Rights");
							System.out.println();	
						}
					}
				} catch (SQLException e) {
					System.err.println("DB issue displaying all special users: " + e.getMessage());
				}
				

			
	}

	public boolean canDeleteAdmin(int userId) throws SQLException{
		String query = "SELECT DISTINCT(group_name) from groupRights "
		+ "WHERE user_id = ? AND adminRightsFlag = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setBoolean(2, true);

			try(ResultSet rs = pstmt.executeQuery()) {
				while(rs.next()) {
					if(!multAdminsToGroup(rs.getString(1))) {
						System.out.println("You cannot delete this user as they are the sole admin for the group " + rs.getString(1) + ".");
						return false;
					}
				}
				return true;
			}
		} catch(SQLException e) {
			System.err.println("DB issue with checking all groups that this user is an admin for: " + e.getMessage());
		}
		return false;
	}

	public boolean isUserAdminOfGroup(int userId, String gName) throws SQLException{
		String query = "SELECT accessRole FROM groupRights "
		+ "WHERE user_id = ? AND group_name = ? AND adminRightsFlag = ?";

		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, gName);
			pstmt.setBoolean(3, true);
			try(ResultSet rs = pstmt.executeQuery()) {
				return rs.next();	
			}
		} catch(SQLException e) {
			System.err.println("DB issue with determinig if user is admin of a group");
		}
		return false;
	}

	public boolean multAdminsToGroup(String gName) throws SQLException{
		String query = "SELECT COUNT(accessRole) FROM groupRights "
		+ "WHERE adminRightsFlag = ? AND group_name =?";

		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setBoolean(1, true);
			pstmt.setString(2, gName);

			try(ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					return rs.getInt(1) > 1;
				}
			}
		} catch (SQLException e) {
			System.err.println("DB issue with determining if a group has multipler users: ");
		}

		return false;
	}

	public void searchArticle(String role, String level, String group, String search) { 
		role = role.strip();
		level = level.strip();
		group = group.strip();
		search = search.strip();
		
		String query = "SELECT a.* FROM articles a "
					 + "JOIN articleGroups ag ON a.id = ag.article_id "
					 + "WHERE (? = 'ALL' OR a.level = ?) "
					 + "AND (? = 'ALL' OR ag.group_name LIKE ?) "
					 + "AND (a.title LIKE ? OR a.short_description LIKE ? OR a.keywords LIKE ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			pstmt.setString(1, level);
			pstmt.setString(2, level);
			pstmt.setString(3, group);
			pstmt.setString(4, group.equals("ALL") ? "%" : "%" + group + "%");
			pstmt.setString(5, "%" + search + "%");
			pstmt.setString(6, "%" + search + "%");
			pstmt.setString(7, "%" + search + "%");
			try (ResultSet rs = pstmt.executeQuery()) {
				List<Article> articles = new ArrayList<>();
				while (rs.next()) {
					int id = rs.getInt("id");
					String articleLevel = rs.getString("level");
					String authors = rs.getString("authors");
					String title = rs.getString("title");
					String shortDescription = rs.getString("short_description");
					String keywords = rs.getString("keywords");
					String body = rs.getString("body");
					String referenceLinks = rs.getString("reference_links");
					articles.add(new Article(id, articleLevel, authors, title, shortDescription, keywords, body, referenceLinks));
				}
				System.out.println("Search Level: " + level + "\t\tTotal Results: " + articles.size());
				
				for (int i = 0; i < articles.size(); i++) {
					System.out.println("Option: " + (i + 1) + "\nTitle: " + articles.get(i).getTitle() + "\nAbstract: " + articles.get(i).getShortDescription());
				}

				System.out.println("Which article would you like to view?");

				String choice = scanner.nextLine();

				if (!choice.matches("\\d+")) {
					System.out.println("Invalid input. Please enter a valid article number.");
					return;
				}

				int articleIndex = Integer.parseInt(choice) - 1;
				
				if(articles.size() == 0) {
					System.out.println("There are no articles matching your criteria. Please try again later.");
					return;
				}

				if (articleIndex < 0 || articleIndex >= articles.size()) {
					System.out.println("Invalid article selection. Please try again.");
					return;
				}

				System.out.println(articles.get(articleIndex));
			}
		} catch (SQLException e) {
			System.err.println("Database error while searching for articles: " + e.getMessage());
		}
	}
}