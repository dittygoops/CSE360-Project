package simpleDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {
    private static final String JDBC_DRIVER = "org.h2.Driver";
	private static final String DB_URL = "jdbc:h2:~/firstDatabase";

    static final String USER = "sa";
	static final String PASS = "";

    public static Statement statement = null;

    public Connection connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables(); // Create the necessary tables if they don't exist
            return connection;
        }  catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
        return null;
    }

    private static void createTables() throws SQLException {
		String userTable = """
		CREATE TABLE IF NOT EXISTS cse360users (
				id INT AUTO_INCREMENT PRIMARY KEY,
				userName VARCHAR(255),
				email VARCHAR(255) UNIQUE,
				password VARCHAR(255),
				firstName VARCHAR(255),
				lastName VARCHAR(255),
				middleName VARCHAR(255),
				preferredFirst VARCHAR(255),
				role VARCHAR(3)
			)
		""";
		statement.execute(userTable);

		// create otp table, with id, otp, expiry time and user role(s)
		String otpTable = """
			CREATE TABLE IF NOT EXISTS otpTable ("
				otp VARCHAR(255) PRIMARY KEY,
				otp_role_assocation VARCHAR(3)
				expiryTime TIMESTAMP,
				created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_expiry (expiry_time)
				)
				""";
		statement.execute(otpTable);

		String articlesTable = "CREATE TABLE IF NOT EXISTS articles ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "level VARCHAR(20), " // level (beginner, intermediate, advanced, expert)
				+ "authors VARCHAR(100), " // authors
				+ "title VARCHAR(255) NOT NULL, " // title
				+ "short_description CLOB, " // short_description/abstract
				+ "keywords VARCHAR(255), " // keywords
				+ "body CLOB, " // body
				+ "reference_links VARCHAR(255)" // reference_links
				+ ")";
		statement.execute(articlesTable);

		String groupsTable = """
            CREATE TABLE IF NOT EXISTS groups (
                name VARCHAR(50) PRIMARY KEY,
                description VARCHAR(255),
                is_special BOOLEAN DEFAULT FALSE
            )
        """;
        statement.execute(groupsTable);

		// Article-Groups junction table
        String articleGroupsTable = """
            CREATE TABLE IF NOT EXISTS article_groups (
                article_id INT,
                group_name VARCHAR(50),
                PRIMARY KEY (article_id, group_name),
                FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
                FOREIGN KEY (group_name) REFERENCES groups(name) ON DELETE CASCADE
            )
        """;
        statement.execute(articleGroupsTable);

		// Group permissions table with improved structure
        String groupPermissionsTable = """
            CREATE TABLE IF NOT EXISTS group_permissions (
                user_id INT,
                group_name VARCHAR(50),
                access_role ENUM('viewer', 'editor', 'admin') NOT NULL,
                PRIMARY KEY (user_id, group_name),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (group_name) REFERENCES groups(name) ON DELETE CASCADE
            )
        """;
        statement.execute(groupPermissionsTable);
	}
}
