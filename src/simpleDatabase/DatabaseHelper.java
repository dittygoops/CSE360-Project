package simpleDatabase;
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
 * @author Abhave Abhilash and Aditya Gupta
 * @version 1.0
 * @since 10/9/2024
 */
class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/firstDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 

	private Scanner scanner = new Scanner(System.in);
	//	PreparedStatement pstmt
	
	/**
	 * Blank constructor
	 */
	public DatabaseHelper() {}

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	/**
	 * create and initiate our user and otp tables
	 * @throws SQLException
	 */
	private void createTables() throws SQLException {
		String dropUsers = "DROP TABLE IF EXISTS cse360users";
		statement.execute(dropUsers);
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
				+ "role VARCHAR(3), "
				+ "otpFlag BOOLEAN DEFAULT FALSE) ";
		statement.execute(userTable);
		
		String dropOtp = "DROP TABLE IF EXISTS otpTable";
		statement.execute(dropOtp);		

		// create otp table, with id, otp, expiry time and user role(s)
		String otpTable = "CREATE TABLE IF NOT EXISTS otpTable ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "otp VARCHAR(255), "
				+ "expiryTime TIMESTAMP), " 
				+ "role VARCHAR(3)";
		statement.execute(otpTable);

		String articlesTable = "CREATE TABLE IF NOT EXISTS articles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "level VARCHAR(20), "     // level (beginner, intermediate, advanced, expert)
                + "group_id VARCHAR(50), "  				// group_id (e.g. CSE360, CSE360-01, CSE360-02)
                + "title VARCHAR(255) NOT NULL, " 			// title
                + "short_description CLOB, "				// short_description/abstract
                + "keywords ARRAY, "						// keywords
                + "body CLOB, "								// body
                + "reference_links ARRAY"					// reference_links
                + ")";
			statement.execute(articlesTable);
		
	}

	/**
	 * Check if the database is empty
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

	/**
	 * Register a user with username, password, and role
	 * @param userName
	 * @param password
	 * @param role
	 * @throws SQLException
	 */
	public void register(String userName, String password, String role) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			pstmt.setString(3, role);
			pstmt.executeUpdate();
		}
	}

	/**
	 * login user with username and password
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
	                String username = rs.getString("userName");
	                String email = rs.getString("email");
	                String firstName = rs.getString("firstName");
	                String middleName = rs.getString("middleName");
	                String lastName = rs.getString("lastName");
	                String preferredFirst = rs.getString("preferredFirst");
	                String roles = rs.getString("role");
	                
	                boolean otpFlag = false; // default value
	                LocalDateTime otpExpiration = LocalDateTime.now(); // default value

	                // Constructing and returning the User object
	                return new User(username, password, email, firstName, middleName, lastName,
	                        preferredFirst, roles, otpFlag, otpExpiration);
	            } else {
	                return null; // User not found
	            }
	        }
	    }
	}
	
	/**
	 * update database row with data from user object
	 * @param user
	 * @throws SQLException
	 */
	public void updateUser(User user) throws SQLException {
	    String query = "UPDATE cse360users SET email = ?, password = ?, firstName = ?, middleName = ?, lastName = ?, preferredFirst = ?, role = ? WHERE userName = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getEmail());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getFirstName());
	        pstmt.setString(4, user.getMiddleName());
	        pstmt.setString(5, user.getLastName());
	        pstmt.setString(6, user.getPreferredFirst());
	        pstmt.setString(7, user.getRoles());
	        pstmt.setString(8, user.getUsername());

	        // update execution
	        int rowsAffected = pstmt.executeUpdate();
	        if (rowsAffected == 0) {
	            System.out.println("No user found with the username: " + user.getUsername());
	        } else {
	            System.out.println("User updated successfully.");
	        }
	    }
	}

	/**
	 * find user by username and email 
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
	                String roles = rs.getString("role");
	                
	                boolean otpFlag = rs.getBoolean("otpFlag"); // Get actual otpFlag value from DB
	                LocalDateTime otpExpiration = LocalDateTime.now(); // You can replace this with actual expiration time if you have it in DB

	                // Constructing and returning the User object
	                return new User(username, "", userEmail, firstName, middleName, lastName,
	                        preferredFirst, roles, otpFlag, otpExpiration);
	            } else {
	                return null; // User not found
	            }
	        }
	    }
	}


	/**
	 * store user information in the database
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
	 * check if user in database
	 * @param userName
	 * @return boolean that represents if user exists
	 */
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/**
	 * display list of all users
	 * @throws SQLException
	 */
	public void displayUsersByAdmin() throws SQLException{
		String sql = "SELECT * FROM cse360users"; 
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql); 

		while(rs.next()) { 
			// Retrieve by column name 
			int id  = rs.getInt("id"); 
			String  email = rs.getString("email"); 
			String password = rs.getString("password"); 
			String role = rs.getString("role");  

			// Display values 
			System.out.print("ID: " + id); 
			System.out.print(", Age: " + email); 
			System.out.print(", First: " + password); 
			System.out.println(", Last: " + role); 
		} 
	}
	
	/**
	 * display user by name
	 * @throws SQLException
	 */
	public void displayUsersByUser() throws SQLException{
		String sql = "SELECT * FROM cse360users"; 
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql); 

		while(rs.next()) { 
			// Retrieve by column name 
			int id  = rs.getInt("id"); 
			String  email = rs.getString("email"); 
			String password = rs.getString("password"); 
			String role = rs.getString("role");  

			// Display values 
			System.out.print("ID: " + id); 
			System.out.print(", Age: " + email); 
			System.out.print(", First: " + password); 
			System.out.println(", Last: " + role); 
		} 
	}

	/**
	 * Create OTP
	 */
	public String createOTP() {
		String otp = "";
		for (int i = 0; i < 6; i++) {
			otp += (int) (Math.random() * 10);
		}
		String expiryTime = LocalDateTime.now().plusMinutes(5).toString();
		try {
			insertOTP(otp, expiryTime);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return otp;
	}

	/**
	 * insert otp to table with roles
	 * @param otp
	 * @param expiryTime
	 * @throws SQLException
	 */
	public void insertOTP(String otp, String expiryTime) throws SQLException {
		String insertOTP = "INSERT INTO otpTable (otp, expiryTime) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertOTP)) {
			pstmt.setString(1, otp);
			pstmt.setString(2, expiryTime);
			pstmt.executeUpdate();
		}
	}

	/**
	 * retrieves otp from the database and if the otp is expired, it returns false else it is found in database
	 * and the otp is not expired, it returns true
	 * @param otp
	 * @return boolean representing the verification of OTP
	 * @throws SQLException
	 */
	public Boolean verifyOTP(String otp) throws SQLException {
		String query = "SELECT * FROM otpTable WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String expiryTime = rs.getString("expiryTime");
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
			        LocalDateTime expiry = LocalDateTime.parse(expiryTime, formatter);
					if (LocalDateTime.now().isBefore(expiry)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * delete otp from table
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
	 *  check if the otp is expired by checking the otp table for the expiry time
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
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

	// Admin and instruction team roles are enhanced
	// with commands to back up and restore help system data
	// to admin/instructor named external file

	// restore command -->
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
	// 2. merge backup with current help articles (when id matches, don't add backup)

	// i dont understand the mechanism to support multiple groups

	/**
	 * Create a new article in the database
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
		
		System.out.println("Enter group ID (e.g. CSE360, CSE360-01, CSE360-02): ");
		String groupId = scanner.nextLine();
		
		System.out.println("Enter article title: ");
		String title = scanner.nextLine();
		
		System.out.println("Enter short description: ");
		String shortDescription = scanner.nextLine();
		
		System.out.println("Enter keywords (comma separated): ");
		String[] keywords = scanner.nextLine().split(",");
		
		System.out.println("Enter article body: ");
		String body = scanner.nextLine();
		
		System.out.println("Enter reference links (comma separated): ");
		String[] referenceLinks = scanner.nextLine().split(",");

		String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
			pstmt.setString(1, level);
			pstmt.setString(2, groupId);
			pstmt.setString(3, title);
			pstmt.setString(4, shortDescription);
			pstmt.setArray(5, connection.createArrayOf("VARCHAR", keywords));
			pstmt.setString(6, body);
			pstmt.setArray(7, connection.createArrayOf("VARCHAR", referenceLinks));
			pstmt.executeUpdate();
		}
	}

	/**
	 * Update an existing article in the database
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
		String[] keywords = scanner.nextLine().split(",");
		
		System.out.println("Enter article body: ");
		String body = scanner.nextLine();
		
		System.out.println("Enter reference links (comma separated): ");
		String[] referenceLinks = scanner.nextLine().split(",");

		String updateArticle = "UPDATE articles SET level = ?, group_id = ?, title = ?, short_description = ?, keywords = ?, body = ?, reference_links = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateArticle)) {
			pstmt.setString(1, level);
			pstmt.setString(2, groupId);
			pstmt.setString(3, title);
			pstmt.setString(4, shortDescription);
			pstmt.setArray(5, connection.createArrayOf("VARCHAR", keywords));
			pstmt.setString(6, body);
			pstmt.setArray(7, connection.createArrayOf("VARCHAR", referenceLinks));
			pstmt.setInt(8, id);
			pstmt.executeUpdate();
		}
	}

	public void viewArticles(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		String query = "SELECT * FROM articles";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				int id = rs.getInt("id");
				String level = rs.getString("level");
				String groupId = rs.getString("group_id");
				String title = rs.getString("title");
				String shortDescription = rs.getString("short_description");
				String[] keywords = (String[]) rs.getArray("keywords").getArray();
				String body = rs.getString("body");
				String[] referenceLinks = (String[]) rs.getArray("reference_links").getArray();

				System.out.println("ID: " + id);
				System.out.println("Level: " + level);
				System.out.println("Group ID: " + groupId);
				System.out.println("Title: " + title);
				System.out.println("Short Description: " + shortDescription);
				System.out.println("Keywords: " + String.join(", ", keywords));
				System.out.println("Body: " + body);
				System.out.println("Reference Links: " + String.join(", ", referenceLinks));
			}
		}
	}
	
	public void viewArticles(String role, String group) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		String query = "SELECT * FROM articles WHERE group_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, group);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt("id");
					String level = rs.getString("level");
					String groupId = rs.getString("group_id");
					String title = rs.getString("title");
					String shortDescription = rs.getString("short_description");
					String[] keywords = (String[]) rs.getArray("keywords").getArray();
					String body = rs.getString("body");
					String[] referenceLinks = (String[]) rs.getArray("reference_links").getArray();

					System.out.println("ID: " + id);
					System.out.println("Level: " + level);
					System.out.println("Group ID: " + groupId);
					System.out.println("Title: " + title);
					System.out.println("Short Description: " + shortDescription);
					System.out.println("Keywords: " + String.join(", ", keywords));
					System.out.println("Body: " + body);
					System.out.println("Reference Links: " + String.join(", ", referenceLinks));
				}
			}
		}
	}

	public void deleteArticle(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		System.out.println("Enter article ID: ");
		int id = Integer.parseInt(scanner.nextLine());

		String deleteArticle = "DELETE FROM articles WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
		}
	}
}
