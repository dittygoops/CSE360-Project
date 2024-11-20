package simpleDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

	private Connection connection = null;
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
				+ "email VARCHAR(255) UNIQUE, "
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
		String query = "SELECT COUNT(*) from groups where name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, checkGroup);
			ResultSet rs = statement.executeQuery(query);
			return rs.next() && rs.getInt(1) > 0;	
		} catch (SQLException e) {
			System.err.println("DB error checking if group exists" + e.getMessage());
		}
		return false;	
	}

	public boolean isGroupSpecial(String checkGroup) {
		String query = "SELECT specialFlag from groups where name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, checkGroup);
			ResultSet resSet = statement.executeQuery(query);
			if(resSet.next()) {
				return resSet.getBoolean("specialFlag");
			} else return false;
		} catch (SQLException e) {
			System.err.println("DB error checking if group is speical" + e.getMessage());
		}
		return false;
	}

	/**
	 * Add a user to the database
	 * 
	 * @param user
	 * @throws SQLException
	 */
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

	/**
	 * Register a user with username, password, and role
	 * 
	 * @param userName
	 * @param password
	 * @param role
	 * @throws SQLException
	 */
	public boolean register(String userName, String password, int userID) throws SQLException {
		String insertUser = "UPDATE cse360users SET userName = ?, password = ?, and otpFlag = ? WHERE id = ?";
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
			pstmt.setBoolean(31, user.getRoles()[2]);
			pstmt.setString(4, user.getUsername());
			pstmt.setString(4, user.getEmail());

			// update execution
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected == 0) {
				System.out.println("No user found with the username: " + user.getUsername() " and email : " + user.getEmail());
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
	 * store user information in the database
	 * 
	 * @param user
	 * @throws SQLException
	 */
	public void storeUser(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, email, password, firstName, middleName, lastName, preferredFirst, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, user.getEmail());
			pstmt.setString(3, user.getPassword());
			pstmt.setString(4, user.getFirstName());
			pstmt.setString(5, user.getMiddleName());
			pstmt.setString(6, user.getLastName());
			pstmt.setString(7, user.getPreferredFirst());
			pstmt.setString(8, user.getRoles());
			pstmt.executeUpdate();
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
		String sql = "SELECT userName, email, preferredFirst, adminFlag, teachFlag, studentFlag FROM cse360users";
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

	public int insertShellUser(boolean admin, boolean instruct, boolean stud) throws SQLException{
		String insertShell = "INSERT INTO cse360users (userName, email, password, firstName, middleName, lastName, preferredFirst, adminFlag, teachFlag, studFlag, otpFlag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertShell)) {
			pstmt.setString(0, "");
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
                        System.out.println("Inserted row ID: " + generatedId);
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
		String query = "SELECT user_id FROM otpTable WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String expiryTime = rs.getString("expiryTime");
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

	// Admin and instruction team roles are enhanced
	// with commands to back up and restore help system data
	// to admin/instructor named external file
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

	public void restoreMerge(String roles, String file) throws Exception {
		if (roles.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		boolean myFlag = false;

		try (BufferedReader reads = new BufferedReader(new FileReader(file))) {
			// counter keeps track of lines - every 7 we need to insert an article into the
			// table
			int counter = 0;
			// line is what the lines from the file will contain
			String line = "";
			String id = "";
			int tmpId = -1;
			String level = "";
			String group_id = "";
			String title = "";
			String short_description = "";
			String keywords = "";
			String body = "";
			String reference_links = "";

			// read the file line by line
			while ((line = reads.readLine()) != null) {
				switch ((counter % 8)) {

					case 0 -> {
						if (counter == 0) {
							id = line;
							tmpId = Integer.parseInt(id);
							String possQuery = "Select count(*) from articles where id = ?";
							try (PreparedStatement pstmt2 = connection.prepareStatement(possQuery)) {
								pstmt2.setInt(1, tmpId);
								try (ResultSet rs = pstmt2.executeQuery()) {
									if (rs.next()) {
										// If the count is greater than 0, the user exists
										if (rs.getInt(1) == 0)
											myFlag = true;
									}
								}
							}
							break;
						}
						// ignore the id since it will auto generate upon table entry
						String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
						System.out.println("Inserting article: " + id);
						if (myFlag) {
							try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

								// pstmt.setString(1, id);
								pstmt.setString(1, level);
								pstmt.setString(2, group_id);
								pstmt.setString(3, title);
								pstmt.setString(4, short_description);
								pstmt.setString(5, keywords);
								pstmt.setString(6, body);
								pstmt.setString(7, reference_links);
								pstmt.setInt(8, tmpId);

								pstmt.executeUpdate();
								System.out.println("An article has been added successfully to the system!");
								myFlag = false;
							}
						}

						if (counter > 0) {
							id = line;
							tmpId = Integer.parseInt(id);
							String possQuery = "Select count(*) from articles where id = ?";
							try (PreparedStatement pstmt2 = connection.prepareStatement(possQuery)) {
								pstmt2.setInt(1, tmpId);
								try (ResultSet rs = pstmt2.executeQuery()) {
									if (rs.next()) {
										// If the count is greater than 0, the user exists
										if (rs.getInt(1) == 0)
											myFlag = true;
									}
								}
							}
						}
						break;
					}

					case 1 -> {
						level = line;
						break;
					}
					case 2 -> {
						group_id = line;
						break;
					}
					case 3 -> {
						title = line;
						break;
					}
					case 4 -> {
						short_description = line;
						break;
					}
					case 5 -> {
						keywords = line;
						break;
					}
					case 6 -> {
						body = line;
						break;
					}

					case 7 -> {
						reference_links = line;
						break;
					}
					/*
					 * case 8 -> {
					 * reference_links = line;
					 * }
					 */
					default -> {
						System.out.println("Something went wrong. Try again later.");
					}
				}
				counter++;
			}

			if (counter > 0 && myFlag) {
				String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

					// pstmt.setString(1, id);
					pstmt.setString(1, level);
					pstmt.setString(2, title);
					pstmt.setString(3, group_id);
					pstmt.setString(4, short_description);
					pstmt.setString(5, keywords);
					pstmt.setString(6, body);
					pstmt.setString(7, reference_links);
					tmpId = Integer.parseInt(id);
					pstmt.setInt(8, tmpId);

					pstmt.executeUpdate();
					System.out.println("An article has been added successfully to the system!");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void restore(String roles, String file) throws Exception {
		if (roles.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		if (!isDatabaseEmpty()) {
			String sql = "TRUNCATE TABLE articles";

			try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
				pstmt.executeUpdate();
				System.out.println("Successfully cleared out all articles");
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		try (BufferedReader reads = new BufferedReader(new FileReader(file))) {
			// counter keeps track of lines - every 7 we need to insert an article into the
			// table
			int counter = 0;
			// line is what the lines from the file will contain
			String line = "";
			String id = "";
			int tmpId = -1;
			String level = "";
			String group_id = "";
			String title = "";
			String short_description = "";
			String keywords = "";
			String body = "";
			String reference_links = "";

			// read the file line by line
			while ((line = reads.readLine()) != null) {
				switch ((counter % 8)) {

					case 0 -> {
						if (counter == 0) {
							id = line;
							tmpId = Integer.parseInt(id);
							break;
						}
						// ignore the id since it will auto generate upon table entry
						String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
						System.out.println("Inserting article: " + id);
						try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

							// pstmt.setString(1, id);
							pstmt.setString(1, level);
							pstmt.setString(2, group_id);
							pstmt.setString(3, title);
							pstmt.setString(4, short_description);
							pstmt.setString(5, keywords);
							pstmt.setString(6, body);
							pstmt.setString(7, reference_links);
							pstmt.setInt(8, tmpId);

							pstmt.executeUpdate();
							System.out.println("An article has been added successfully to the system!");
						}
						if (counter > 0) {
							id = line;
							tmpId = Integer.parseInt(id);
						}
						break;
					}

					case 1 -> {
						level = line;
						break;
					}
					case 2 -> {
						group_id = line;
						break;
					}
					case 3 -> {
						title = line;
						break;
					}
					case 4 -> {
						short_description = line;
						break;
					}
					case 5 -> {
						keywords = line;
						break;
					}
					case 6 -> {
						body = line;
						break;
					}

					case 7 -> {
						reference_links = line;
						break;
					}
					/*
					 * case 8 -> {
					 * reference_links = line;
					 * }
					 */
					default -> {
						System.out.println("Something went wrong. Try again later.");
					}
				}
				counter++;
			}

			if (counter > 0) {
				String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {

					// pstmt.setString(1, id);
					pstmt.setString(1, level);
					pstmt.setString(2, title);
					pstmt.setString(3, group_id);
					pstmt.setString(4, short_description);
					pstmt.setString(5, keywords);
					pstmt.setString(6, body);
					pstmt.setString(7, reference_links);
					tmpId = Integer.parseInt(id);
					pstmt.setInt(8, tmpId);

					pstmt.executeUpdate();
					System.out.println("An article has been added successfully to the system!");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
		for(String curGroup : groups) {
			if(groupExist(curGroup)) {
				if(isGroupSpecial(curGroup)) {
					System.out.println("You are unable to create an article for the following group: " + curGroup);
					System.out.println("As an admin, you are unable to make articles for Special Access Groups");
					break;
				}
			}
		}

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
		String encryptedBody = encryptionHelper.encrypt(body);

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

		System.out.println("Enter group ID (e.g. CSE360, CSE360-01, CSE360-02): ");
		String groupId = scanner.nextLine();

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

		String updateArticle = "UPDATE articles SET level = ?, group_id = ?, title = ?, short_description = ?, keywords = ?, body = ?, reference_links = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateArticle)) {
			pstmt.setString(1, level);
			pstmt.setString(2, groupId);
			pstmt.setString(3, title);
			pstmt.setString(4, shortDescription);
			pstmt.setString(5, keywords);
			pstmt.setString(6, body);
			pstmt.setString(7, referenceLinks);
			pstmt.setInt(8, id);
			pstmt.executeUpdate();
		}
	}

	/**
	 * View all articles in the database
	 * 
	 * @param role
	 * @throws SQLException
	 */
	public void viewAllArticles(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		System.out.println("All articles:");
		String query = "SELECT id, authors, short_description FROM articles";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				int id = rs.getInt(1);
				String authors = rs.getString(2);
				String shortDescription = rs.getString(3);	

				System.out.println("ID: " + id);
				System.out.println("Authors: " + authors);
				System.out.println("Short Description: " + shortDescription);	
			}
		} catch(SQLException e) {
			System.err.println("DB issue trying to view all articles");
		}
	}

	public void viewGroupedArticles(String role, String group) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		String query = "SELECT articles.id, articles.short_description, articles.authors FROM articles "
		+ "JOIN articleGroups on articleGroups.article_id = articles.id "
		+ "JOIN groups on articleGroups.group_name = groups.name "
		+ "WHERE groups.name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, group);
			ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					String authors = rs.getString(3);
					String shortDescription = rs.getString(2);
				

					System.out.println("Article ID: " + id);
					System.out.println("Authors: " + authors);	
					System.out.println("Short Description: " + shortDescription);	
				}
			
		} catch (SQLException e) {
				System.err.println("DB issue while viewing Grouped articles: " + e.getMessage());
			}
	}

	public void viewArticle(String role, String articleId) throws SQLException {
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
					String groupId = rs.getString("group_id");
					String title = rs.getString("title");
					String shortDescription = rs.getString("short_description");
					String keywords = rs.getString("keywords");
					String encryptedBody = rs.getString("body");
					String decryptedBody = encryptionHelper.decrypt(encryptedBody);
					String referenceLinks = rs.getString("reference_links");

					System.out.println("ID: " + id);
					System.out.println("Level: " + level);
					System.out.println("Group ID: " + groupId);
					System.out.println("Title: " + title);
					System.out.println("Short Description: " + shortDescription);
					System.out.println("Keywords: " + keywords);
					System.out.println("Body: " + decryptedBody);
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
	public boolean deleteArticle(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return false;
		}

		System.out.println("Enter article ID: ");
		int id = Integer.parseInt(scanner.nextLine());

		String deleteArticle = "DELETE FROM articles WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
			pstmt.setInt(1, id);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		}
	}

}
