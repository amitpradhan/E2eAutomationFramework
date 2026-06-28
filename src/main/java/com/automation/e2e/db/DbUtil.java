package com.automation.e2e.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbUtil {
    private static Connection connection;

    /**
     * Initializes the Master JDBC connection to the target environment database.
     * Ensure your connection URL is properly authenticated.
     */
    public static void initConnection(String url, String user, String password) throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    /**
     * Basic Validation Check: Fetches the total row count matching a condition.
     * Essential for validating that data has successfully populated a target table.
     */
    public static int getRowCount(String tableName, String condition) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + (condition != null ? " WHERE " + condition : "");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Dynamic Data Check: Executes a custom query and returns results as a List of Maps.
     * Each row is mapped as column name (Key) to column cell value (Value).
     */
    public static List<Map<String, Object>> executeQuery(String query) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * Payload Blob Retrieval: Extracts a raw string block (JSON or XML structural block)
     * out of a specified text data record column.
     */
    public static String fetchPayloadFromDb(String query, String columnName) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getString(columnName);
            }
        }
        throw new RuntimeException("No database record matched execution query: " + query);
    }

    /**
     * Safely closes the active JDBC connection pipeline.
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}