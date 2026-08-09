package com.automation.e2e.db;

import com.automation.e2e.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DbUtil {

    private static final Logger log = LogManager.getLogger(DbUtil.class);

    /**
     * Establishes a database connection dynamically supporting MySQL, PostgreSQL, SQL Server, and Oracle.
     */
    public static Connection getConnection() throws SQLException {
        String jdbcUrl = System.getProperty("db.url", ConfigReader.getProperty("db.url"));
        String username = System.getProperty("db.user", ConfigReader.getProperty("db.user"));
        String password = System.getProperty("db.password", ConfigReader.getProperty("db.password"));

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            throw new RuntimeException("[DB ERROR] Database JDBC URL is not configured in config.properties or system properties!");
        }

        // Automatically load appropriate JDBC Driver based on URL pattern or config
        registerDriver(jdbcUrl);

        log.info("Establishing database connection to URL: {}", jdbcUrl);
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * Auto-detects and registers the JDBC driver class based on the connection string prefix.
     */
    private static void registerDriver(String jdbcUrl) {
        try {
            if (jdbcUrl.contains("h2")) {
                Class.forName("org.h2.Driver");
            } else if (jdbcUrl.contains("mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (jdbcUrl.contains("postgresql")) {
                Class.forName("org.postgresql.Driver");
            } else if (jdbcUrl.contains("sqlserver")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } else if (jdbcUrl.contains("oracle")) {
                Class.forName("oracle.jdbc.OracleDriver");
            }
        } catch (ClassNotFoundException e) {
            log.error("Failed to load JDBC driver class for URL pattern: {}", jdbcUrl, e);
            throw new RuntimeException("Missing JDBC Driver dependency in pom.xml", e);
        }
    }

    /**
     * Executes a count query universally across any database vendor.
     */
    public static int getRowCount(String tableName, String condition) {
        String query = String.format("SELECT COUNT(*) FROM %s WHERE %s", tableName, condition);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Failed to fetch row count for table {}: {}", tableName, e.getMessage());
            throw new RuntimeException(e);
        }
        return 0;
    }
}