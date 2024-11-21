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
import java.util.ArrayList;
import java.util.List;
 
/***
 * This class contains all functions that relate/interact with our H2 databases
 * @author Abhave Abhilash and Aditya Gupta
 * @version 1.0
 * @since 10/9/2024
 */
class DatabaseHelper {
 
	// JDBC driver name and database URL 
	private final EncryptionHelper encryptionHelper = new EncryptionHelper();
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
 
	/**
	 * Connect to the database
	 * @throws SQLException
	 */
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
		//String dropUsers = "DROP TABLE IF EXISTS cse360users";
		//statement.execute(dropUsers);
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
 
		// create otp table, with id, otp, expiry time and user role(s)
		String otpTable = "CREATE TABLE IF NOT EXISTS otpTable ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "otp VARCHAR(255), "
				+ "expiryTime TIMESTAMP, " 
				+ "role VARCHAR(3))";
		statement.execute(otpTable);
 
		String articlesTable = "CREATE TABLE IF NOT EXISTS articles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "level VARCHAR(20), "     				// level (beginner, intermediate, advanced, expert)
                + "group_id VARCHAR(255), "  				// group_id (e.g. CSE360, CSE360-01, CSE360-02)
                + "title VARCHAR(255) NOT NULL, " 			// title
                + "short_description CLOB, "				// short_description/abstract
                + "keywords VARCHAR(255), "					// keywords
                + "body CLOB, "								// body
                + "reference_links VARCHAR(255)"			// reference_links
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
	 * Add a user to the database
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
	 * @param userName
	 * @param password
	 * @param role
	 * @throws SQLException
	 */
	public void register(String userName, String password, String role) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role, otpFlag) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, password);
			pstmt.setString(3, role);
			pstmt.setBoolean(4, true);
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
	                String email = rs.getString("email");
	                String firstName = rs.getString("firstName");
	                String middleName = rs.getString("middleName");
	                String lastName = rs.getString("lastName");
	                String preferredFirst = rs.getString("preferredFirst");
	                String roles = rs.getString("role");
 
	                boolean otpFlag = rs.getBoolean("otpFlag"); 
	                LocalDateTime otpExpiration = LocalDateTime.now(); // default value
 
	                // Constructing and returning the User object
	                return new User(userName, password, email, firstName, middleName, lastName,
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
		System.out.println("Here is the passed in users email: " + user.getEmail());
	    String query = "UPDATE cse360users SET firstName = ?, middleName = ?, lastName = ?, preferredFirst = ?, role = ?, otpFlag = ?, email = ? WHERE userName = ?";
 
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getFirstName());
	        pstmt.setString(2, user.getMiddleName());
	        pstmt.setString(3, user.getLastName());
	        pstmt.setString(4, user.getPreferredFirst());
	        pstmt.setString(5, user.getRoles());
	        pstmt.setBoolean(6, user.getOTP());
	        pstmt.setString(7, user.getEmail());
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
	 * get User Roles by username
	 * @param userName
	 */
	public String getUserRoles(String userName) {
		String query = "SELECT role FROM cse360users WHERE userName = ?";
		String roles = "";
		try (PreparedStatement pstmt = connection.prepareStatement(query);
			 ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				roles = rs.getString("role");	
			} else {
				System.err.println("User not found");
			}
		} catch (SQLException e) {
			System.err.println("Database error while getting user roles: " + e.getMessage());
			return "error";
		}
		return roles;
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
	 * @throws SQLException
	 */
	public void displayUsersByAdmin() throws SQLException{
		String sql = "SELECT * FROM cse360users"; 
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
 
			while(rs.next()) { 
				// Retrieve by column name 
				int id  = rs.getInt("id"); 
				String  email = rs.getString("email"); 
				String firstName = rs.getString("firstName"); 
				String role = rs.getString("role");  
				String username = rs.getString("userName");
	
				// Display values 
				System.out.print("ID: " + id); 
				System.out.print(", Username: " + username); 
				System.out.print(", First Name: " + firstName); 
				System.out.print(", Email: " + email);
				System.out.println(", Roles: " + role);
			} 
		}
	}
 
	/**
	 * display user by name
	 * @throws SQLException
	 */
	public void displayUsersByUser() throws SQLException{
		String sql = "SELECT * FROM cse360users"; 
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
 
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
	}
 
	// delete user by username
	/**
	 * delete user by username
	 * @param userName
	 * @return boolean that represents if user was deleted
	 * @throws SQLException
	 */

	public boolean deleteUserAccount(String userName) throws SQLException {
		String deleteQuery = "DELETE FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
			pstmt.setString(1, userName);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
 
	/**
	 * Create OTP and store it in the database
	 */
	public String createOTP(String roles) {
		String otp = "";
		for (int i = 0; i < 6; i++) {
			otp += (int) (Math.random() * 10);
		}
		String expiryTime = LocalDateTime.now().plusMinutes(5).toString();
		try {
			insertOTP(otp, expiryTime, roles);
		} catch (SQLException e) {
			System.err.println("Database error while creating OTP: " + e.getMessage());
		}
		return otp;
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
	 * @param otp
	 * @param expiryTime
	 * @throws SQLException
	 */
	public void insertOTP(String otp, String expiryTime, String roles) throws SQLException {
		String insertOTP = "INSERT INTO otpTable (otp, expiryTime, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertOTP)) {
			pstmt.setString(1, otp);
			pstmt.setString(2, expiryTime);
			pstmt.setString(3, roles);
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
	 * This function is used after an OTP has been verified to return the roles associated with the invitation
	 * @param otp
	 * @return string of roles given to this user by the admin who invited them to the system
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
			System.err.println("Database error while closing connection: " + se2.getMessage());
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
	public void backup(String role, String file) throws Exception{
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		//Check to see if there is anything to back up at all
		if(isDatabaseEmpty()) {
			System.out.println("There are no articles in the system to back up.");
			return;
		}
		
		//Get all entries in the table
		String sql = "SELECT * FROM articles"; 
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
		
			//Each article will have 6 lines in the text file, every 6 lines corresponds to an entry
			while(rs.next()) {
				//For each entry - get all fields and store them in their encrypted states
				int id = rs.getInt("id");
				String idEnter = "" + id;
				String level = rs.getString("level");
				String group_id = rs.getString("group_id");
				String title = rs.getString("title");
				String short_description = rs.getString("short_description");
				String keywords = rs.getString("keywords");
				String body = rs.getString("body");
				String references = rs.getString("reference_links");
				
				//write each field on its own line
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
		
		try(BufferedReader reads = new BufferedReader(new FileReader(file))) {
			//counter keeps track of lines - every 7 we need to insert an article into the table
			int counter = 0;
			//line is what the lines from the file will contain
			String line = "";
			String id = "";
			int tmpId = -1;
			String level = "";
			String group_id = "";
			String title = "";
			String short_description = "";
			String keywords = "";
			String body =  "";
			String reference_links = "";

			//read the file line by line
			while((line = reads.readLine()) != null) {
				switch((counter % 8)) {
				
					case 0 ->  {
						if(counter == 0) {
							id = line;
							tmpId = Integer.parseInt(id);
							String possQuery = "Select count(*) from articles where id = ?";
							try(PreparedStatement pstmt2 = connection.prepareStatement(possQuery)) {
								pstmt2.setInt(1, tmpId);
								try (ResultSet rs = pstmt2.executeQuery()){
									if (rs.next()) {
							            // If the count is greater than 0, the user exists
							            if( rs.getInt(1) == 0) myFlag = true;
							        }
								}
							}
							break;
						}
						//ignore the id since it will auto generate upon table entry
						String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
						System.out.println("Inserting article: " + id);
						if(myFlag) {
							try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
							
							//pstmt.setString(1,  id);
							pstmt.setString(1,  level);
							pstmt.setString(2, group_id);
							pstmt.setString(3, title);
							pstmt.setString(4, short_description);
							pstmt.setString(5,  keywords);
							pstmt.setString(6,  body);
							pstmt.setString(7,  reference_links);
							pstmt.setInt(8, tmpId);
							
							
							pstmt.executeUpdate();
							System.out.println("An article has been added successfully to the system!");
							myFlag = false;
						}
						}
						
						if(counter > 0) {
							id = line;
							tmpId = Integer.parseInt(id);
							String possQuery = "Select count(*) from articles where id = ?";
							try(PreparedStatement pstmt2 = connection.prepareStatement(possQuery)) {
								pstmt2.setInt(1, tmpId);
								try (ResultSet rs = pstmt2.executeQuery()){
									if (rs.next()) {
							            // If the count is greater than 0, the user exists
							            if( rs.getInt(1) == 0) myFlag = true;
							        }
								}
							}
						}
						break;
					}

					case 1 ->  {
						level = line;
						break;
					}
					case 2 ->  {
						group_id = line;
						break;
					}
					case 3 ->  {
						title = line;
						break;
					}
					case 4 ->  {
						short_description = line;
						break;
					}
					case 5 ->  {
						keywords = line;
						break;
					}
					case 6 ->  {
						body = line;
						break;
					}
					
					case 7 ->  {
						reference_links = line;
						break;
					}
					/*
					case 8 ->  {
						reference_links = line;
					}
						*/
					default ->  {
						System.out.println("Something went wrong. Try again later.");
					}
				}
				counter++;
			}
			
			if(counter > 0 && myFlag) {
				String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
					
					//pstmt.setString(1,  id);
					pstmt.setString(1,  level);
					pstmt.setString(2, title);
					pstmt.setString(3, group_id);
					pstmt.setString(4, short_description);
					pstmt.setString(5,  keywords);
					pstmt.setString(6,  body);
					pstmt.setString(7,  reference_links);
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
	
	public void restore(String roles, String file) throws Exception{
		if (roles.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		if(!isDatabaseEmpty()) {
			String sql = "TRUNCATE TABLE articles";
			
			try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
				pstmt.executeUpdate();
				System.out.println("Successfully cleared out all articles");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		try(BufferedReader reads = new BufferedReader(new FileReader(file))) {
			//counter keeps track of lines - every 7 we need to insert an article into the table
			int counter = 0;
			//line is what the lines from the file will contain
			String line = "";
			String id = "";
			int tmpId = -1;
			String level = "";
			String group_id = "";
			String title = "";
			String short_description = "";
			String keywords = "";
			String body =  "";
			String reference_links = "";

			//read the file line by line
			while((line = reads.readLine()) != null) {
				switch((counter % 8)) {
				
					case 0 ->  {
						if(counter == 0) {
							id = line;
							tmpId = Integer.parseInt(id);
							break;
						}
						//ignore the id since it will auto generate upon table entry
						String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
						System.out.println("Inserting article: " + id);
						try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
							
							//pstmt.setString(1,  id);
							pstmt.setString(1,  level);
							pstmt.setString(2, group_id);
							pstmt.setString(3, title);
							pstmt.setString(4, short_description);
							pstmt.setString(5,  keywords);
							pstmt.setString(6,  body);
							pstmt.setString(7,  reference_links);
							pstmt.setInt(8, tmpId);
							
							
							pstmt.executeUpdate();
							System.out.println("An article has been added successfully to the system!");
						}
						if(counter > 0) {
							id = line;
							tmpId = Integer.parseInt(id);
						}
						break;
					}

					case 1 ->  {
						level = line;
						break;
					}
					case 2 ->  {
						group_id = line;
						break;
					}
					case 3 ->  {
						title = line;
						break;
					}
					case 4 ->  {
						short_description = line;
						break;
					}
					case 5 ->  {
						keywords = line;
						break;
					}
					case 6 ->  {
						body = line;
						break;
					}
					
					case 7 ->  {
						reference_links = line;
						break;
					}
					/*
					case 8 ->  {
						reference_links = line;
					}
						*/
					default ->  {
						System.out.println("Something went wrong. Try again later.");
					}
				}
				counter++;
			}
			
			if(counter > 0) {
				String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
					
					//pstmt.setString(1,  id);
					pstmt.setString(1,  level);
					pstmt.setString(2, title);
					pstmt.setString(3, group_id);
					pstmt.setString(4, short_description);
					pstmt.setString(5,  keywords);
					pstmt.setString(6,  body);
					pstmt.setString(7,  reference_links);
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
		
		System.out.println("Enter group ID (Please make sure there are no spaces and that they are comma separated) (e.g. CSE360,CSE360-01,CSE360-02): ");
		String groupId = scanner.nextLine() + ",";
 
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
		
		String insertArticle = "INSERT INTO articles (level, group_id, title, short_description, keywords, body, reference_links, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
			pstmt.setString(1, level);
			pstmt.setString(2, groupId);
			pstmt.setString(3, title);
			pstmt.setString(4, shortDescription);
			pstmt.setArray(5, connection.createArrayOf("VARCHAR", keywords));
			pstmt.setString(6, encryptedBody);
			pstmt.setArray(7, connection.createArrayOf("VARCHAR", referenceLinks));
			pstmt.setInt(8, tempId);
			
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
	 * @param role
	 * @throws SQLException
	 */
	public void viewAllArticles(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
 
		System.out.println("All articles:");
		String query = "SELECT * FROM articles";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				int id = rs.getInt("id");
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
 
	public void viewGroupedArticles(String role, String group) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
 
		String query = "SELECT * FROM articles WHERE group_id LIKE ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, "%" + group + ",%");
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt("id");
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

	public void searchArticle(String role, String level, String group, String search) { 
		String query = "SELECT * FROM articles WHERE level = ? AND group_id LIKE ? AND (title LIKE ? OR short_description LIKE ? OR keywords LIKE ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			pstmt.setString(1, level);
			pstmt.setString(2, "%" + group + "%");
			pstmt.setString(3, "%" + search + "%");
			pstmt.setString(4, "%" + search + "%");
			pstmt.setString(5, "%" + search + "%");
			try (ResultSet rs = pstmt.executeQuery()) {
				List<Article> articles = new ArrayList<>();
				while (rs.next()) {
					int id = rs.getInt("id");
					String articleLevel = rs.getString("level");
					String groupId = rs.getString("group_id");
					String title = rs.getString("title");
					String shortDescription = rs.getString("short_description");
					String keywords = rs.getString("keywords");
					String encryptedBody = rs.getString("body");
					String decryptedBody = encryptionHelper.decrypt(encryptedBody);
					String referenceLinks = rs.getString("reference_links");

					Article article = new Article(id, articleLevel, groupId, title, shortDescription, keywords, decryptedBody, referenceLinks);
					articles.add(article);
				}

				System.out.println("Search Level: " + level + "\t\tTotal Results: " + articles.size());
				
				for (Article article: articles) {
					System.out.println(article);
				}
			}
		} catch (SQLException e) {
			System.err.println("Database error while searching for articles: " + e.getMessage());
		}
	}
}
