package com.automation.e2e.db;

public class SqlCatalog {

    // =========================================================================
    // TABLES DDL
    // =========================================================================
    public static final String CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE'" +
                    ")";

    public static final String CREATE_SYNCED_BILLS_TABLE =
            "CREATE TABLE IF NOT EXISTS synced_bills (" +
                    "bill_id VARCHAR(50) PRIMARY KEY, " +
                    "amount DECIMAL(10,2), " +
                    "sync_status VARCHAR(20)" +
                    ")";

    // =========================================================================
    // DML / INSERT & MERGE QUERIES
    // =========================================================================
    public static final String INSERT_USER =
            "INSERT INTO users (username, email, status) VALUES (?, ?, ?)";

    public static final String MERGE_SYNCED_BILL =
            "MERGE INTO synced_bills (bill_id, amount, sync_status) KEY(bill_id) VALUES (?, ?, ?)";

    // =========================================================================
    // SELECT QUERIES
    // =========================================================================
    public static final String SELECT_USER_BY_USERNAME =
            "SELECT username, email FROM users WHERE username = ?";

    public static final String SELECT_SYNCED_BILL_BY_ID =
            "SELECT bill_id, amount, sync_status FROM synced_bills WHERE bill_id = ?";
}