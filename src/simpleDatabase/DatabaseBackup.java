package simpleDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.h2.util.json.JSONArray;
import org.h2.util.json.JSONObject;

public class DatabaseBackup {
    private final Connection connection;
    private static final String BACKUP_DIRECTORY = "database_backups";

    public DatabaseBackup(Connection connection) {
        this.connection = connection;
        createBackupDirectory();
    }

    private void createBackupDirectory() {
        File directory = new File(BACKUP_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void backupAllData() throws SQLException, IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        backupToSQL(timestamp);
        backupToJSON(timestamp);
        System.out.println("Complete backup finished successfully!");
    }

    private void backupToSQL(String timestamp) throws SQLException, IOException {
        String fileName = BACKUP_DIRECTORY + "/complete_backup_" + timestamp + ".sql";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("-- Complete database backup created at " + timestamp);
            writer.println("-- Note: Tables should be restored in the correct order due to foreign key constraints\n");

            // 1. First backup groups (no foreign key dependencies)
            writer.println("-- Groups Table Backup");
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM groups");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    writer.println(String.format(
                        "INSERT INTO groups (name, description, is_special) VALUES ('%s', %s, %b);",
                        escape(rs.getString("name")),
                        rs.getString("description") != null ? "'" + escape(rs.getString("description")) + "'" : "NULL",
                        rs.getBoolean("is_special")
                    ));
                }
            }
            writer.println();

            // 2. Backup articles
            writer.println("-- Articles Table Backup");
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM articles");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("INSERT INTO articles (id, level, authors, title, short_description, keywords, body, reference_links) VALUES (")
                       .append(rs.getInt("id")).append(", ")
                       .append(quote(rs.getString("level"))).append(", ")
                       .append(quote(rs.getString("authors"))).append(", ")
                       .append(quote(rs.getString("title"))).append(", ")
                       .append(quote(rs.getString("short_description"))).append(", ")
                       .append(quote(rs.getString("keywords"))).append(", ")
                       .append(quote(rs.getString("body"))).append(", ")
                       .append(quote(rs.getString("reference_links"))).append(");");
                    writer.println(sql.toString());
                }
            }
            writer.println();

            // 3. Backup article_groups (junction table)
            writer.println("-- Article-Groups Associations Backup");
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM article_groups");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    writer.println(String.format(
                        "INSERT INTO article_groups (article_id, group_name) VALUES (%d, '%s');",
                        rs.getInt("article_id"),
                        escape(rs.getString("group_name"))
                    ));
                }
            }
            writer.println();

            // 4. Backup group_permissions
            writer.println("-- Group Permissions Backup");
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM group_permissions");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    writer.println(String.format(
                        "INSERT INTO group_permissions (user_id, group_name, access_role) VALUES (%d, '%s', '%s');",
                        rs.getInt("user_id"),
                        escape(rs.getString("group_name")),
                        escape(rs.getString("access_role"))
                    ));
                }
            }
        }
    }

    private void backupToJSON(String timestamp) throws SQLException, IOException {
        String fileName = BACKUP_DIRECTORY + "/complete_backup_" + timestamp + ".json";
        // create a backup JSON object
        org.h2.util.json.JSONObject backup = new org.h2.util.json.JSONObject();
     

        // 1. Backup groups
        JSONArray groupsArray = new JSONArray();
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM groups");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                JSONObject group = new JSONObject();
                group.put("name", rs.getString("name"));
                group.put("description", rs.getString("description"));
                group.put("is_special", rs.getBoolean("is_special"));
                groupsArray.add(group);
            }
        }
        backup.put("groups", groupsArray);

        // 2. Backup articles with their groups
        JSONArray articlesArray = new JSONArray();
        try (PreparedStatement pstmt = connection.prepareStatement("""
                SELECT a.*, 
                       GROUP_CONCAT(ag.group_name) as group_names
                FROM articles a
                LEFT JOIN article_groups ag ON a.id = ag.article_id
                GROUP BY a.id
                """);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                JSONObject article = new JSONObject();
                article.put("id", rs.getInt("id"));
                article.put("level", rs.getString("level"));
                article.put("authors", rs.getString("authors"));
                article.put("title", rs.getString("title"));
                article.put("short_description", rs.getString("short_description"));
                article.put("keywords", rs.getString("keywords"));
                article.put("body", rs.getString("body"));
                article.put("reference_links", rs.getString("reference_links"));
                
                // Add groups as array
                String groupNames = rs.getString("group_names");
                if (groupNames != null) {
                    article.put("groups", Arrays.asList(groupNames.split(",")));
                } else {
                    article.put("groups", new ArrayList<>());
                }
                articlesArray.add(article);
            }
        }
        backup.put("articles", articlesArray);

        // 3. Backup group permissions
        JSONArray permissionsArray = new JSONArray();
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM group_permissions");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                JSONObject permission = new JSONObject();
                permission.put("user_id", rs.getInt("user_id"));
                permission.put("group_name", rs.getString("group_name"));
                permission.put("access_role", rs.getString("access_role"));
                permissionsArray.add(permission);
            }
        }
        backup.put("group_permissions", permissionsArray);

        // Write the complete JSON backup
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(backup.toJSONString());
        }
    }

    private String escape(String str) {
        if (str == null) return "NULL";
        return str.replace("'", "''")
                 .replace("\\", "\\\\");
    }

    private String quote(String str) {
        if (str == null) return "NULL";
        return "'" + escape(str) + "'";
    }

    public void restoreFromSQL(String backupFile) throws SQLException, IOException {
        connection.setAutoCommit(false);
        try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("--")) continue;
                
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(line);
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}