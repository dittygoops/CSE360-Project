package simpleDatabase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
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
public class DatabaseHelper {

	// JDBC driver name and database URL
	private final EncryptionHelper encryptionHelper = new EncryptionHelper();
	private Connection connection;
	private Statement statement;
	private Scanner scanner = new Scanner(System.in);

	/**
	 * Constructor for the DatabaseHelper class
	 */
	public DatabaseHelper() {
		try {
			connection = new ConnectionManager().connectToDatabase();
			statement = connection.createStatement();
		} catch (SQLException e) {
			System.err.println("Database connection error: " + e.getMessage());
		}
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

	// check groups table for the String group parameter
	/**
	 * Check if the group is special
	 * 
	 * @param checkGroup
	 * @return boolean that represents if group is special
	 */
	public boolean ifGroupExists(String checkGroup) {
		String query = "SELECT COUNT(*) FROM groups WHERE name = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, checkGroup);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("DB error checking if group exists: " + e.getMessage());
		}
		return false;
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
		if(role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}
		String query = "SELECT * FROM articles";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(query);
			 BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			
			while (rs.next()) {
				// Write each field on a separate line
				writer.write(rs.getString("id"));
				writer.newLine();
				writer.write(rs.getString("level"));
				writer.newLine(); 
				writer.write(rs.getString("authors"));
				writer.newLine();
				writer.write(rs.getString("title"));
				writer.newLine();
				writer.write(rs.getString("short_description"));
				writer.newLine();
				writer.write(rs.getString("keywords")); 
				writer.newLine();
				writer.write(rs.getString("body"));
				writer.newLine();
				writer.write(rs.getString("reference_links"));
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing to backup file: " + e.getMessage());
			throw new Exception("Backup failed");
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

	/**
	 * Create a new special group in the database
	 * 
	 * @param name
	 * @return true if the group was created successfully, false otherwise
	 * @throws SQLException
	 */
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

			pstmt.setString(2, groupId);
			pstmt.setString(3, title);
			pstmt.setString(4, shortDescription);
			pstmt.setArray(5, connection.createArrayOf("VARCHAR", keywords));
			pstmt.setString(6, encryptedBody);
			pstmt.setArray(7, connection.createArrayOf("VARCHAR", referenceLinks));
			pstmt.setInt(8, tempId);
			
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
				return;
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
	public void viewAllArticles(String role) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		System.out.println("All articles:");
		String query = "SELECT id, authors, short_description, title FROM articles";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				int id = rs.getInt(1);
				String authors = rs.getString(2);
				String shortDescription = rs.getString(3);
				String title = rs.getString(4);

				System.out.println("ID: " + id);
				System.out.println("Authors: " + authors);
				System.out.println("Title: " + title);
				System.out.println("Short Description: " + shortDescription);	
			}
		} catch(SQLException e) {
			System.err.println("DB issue trying to view all articles");
		}
	}

	/**
	 * View all articles in the database for a given group
	 * 
	 * @param role
	 * @param group
	 * @throws SQLException
	 */
	public void viewGroupedArticles(String role, String group) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		String query = "SELECT articles.id, articles.short_description, articles.authors, articles.title FROM articles "
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
	 * View all articles in the database for a given level
	 * 
	 * @param role
	 * @param contentLevel
	 * @throws SQLException
	 */
	public void viewContentArticles(String role, String contentLevel) throws SQLException {
		if (role.equals("s")) {
			System.out.println("Invalid role");
			return;
		}

		String query = "SELECT articles.id, articles.short_description, articles.authors, articles.title FROM articles "
		+ "JOIN articleGroups on articleGroups.article_id = articles.id "
		+ "JOIN groups on articleGroups.group_name = groups.name "
		+ "WHERE articles.level = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, contentLevel);
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
				
				for (int i = 0; i < articles.size(); i++) {
					System.out.println(i + 1 + "\nTitle: " + articles.get(i).getTitle() + "\nAbstract: " + articles.get(i).getShortDescription());
				}

				System.out.println("Which article would you like to view?");

				int articleIndex = Integer.parseInt(scanner.nextLine()) - 1;

				System.out.println(articles.get(articleIndex));
			}
		} catch (SQLException e) {
			System.err.println("Database error while searching for articles: " + e.getMessage());
		}
	}
}
