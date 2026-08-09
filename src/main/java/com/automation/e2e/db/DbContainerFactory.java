package com.automation.e2e.db;

import com.automation.e2e.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class DbContainerFactory {

    private static final Logger log = LogManager.getLogger(DbContainerFactory.class);
    private static JdbcDatabaseContainer<?> activeContainer;

    /**
     * Handles DB initialization dynamically.
     * Supported keys: "h2", "mysql", "postgres", "sqlserver", "oracle"
     */
    @SuppressWarnings("resource")
    public static JdbcDatabaseContainer<?> startContainer(String dbType) {
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type key cannot be null or empty!");
        }

        String normalizedType = dbType.toLowerCase().trim();
        log.info("--- [DB FACTORY] Requesting database initialization for type: [{}] ---", normalizedType);

        String dbName = getPropertyOrDefault("tc.db.name", "e2e_testdb");

        // 1. In-Memory H2 Mode (No Docker Required)
        if ("h2".equals(normalizedType)) {
            String h2Mode = getPropertyOrDefault("h2.compatibility.mode", "MySQL");
            String h2Url = String.format("jdbc:h2:mem:%s;MODE=%s;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE", dbName, h2Mode);

            System.setProperty("db.url", h2Url);
            System.setProperty("db.user", "sa");
            System.setProperty("db.password", "");

            log.info("Successfully initialized H2 In-Memory Database (Mode: {}). JDBC URL: {}", h2Mode, h2Url);
            return null; // No container reference needed for in-memory H2
        }

        // 2. Testcontainers Modes (Docker Required)
        switch (normalizedType) {
            case "mysql":
                String mysqlImage = getPropertyOrDefault("tc.mysql.image", "mysql:8.0.36");
                String mysqlUser = getPropertyOrDefault("tc.mysql.username", "root");
                String mysqlPass = getPropertyOrDefault("tc.mysql.password", "rootpassword");

                MySQLContainer<?> mysql = new MySQLContainer<>(mysqlImage)
                        .withDatabaseName(dbName)
                        .withUsername(mysqlUser)
                        .withPassword(mysqlPass);
                activeContainer = mysql;
                break;

            case "postgres":
            case "postgresql":
                String pgImage = getPropertyOrDefault("tc.postgres.image", "postgres:16-alpine");
                String pgUser = getPropertyOrDefault("tc.postgres.username", "postgres");
                String pgPass = getPropertyOrDefault("tc.postgres.password", "postgres");

                PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(pgImage)
                        .withDatabaseName(dbName)
                        .withUsername(pgUser)
                        .withPassword(pgPass);
                activeContainer = postgres;
                break;

            case "sqlserver":
            case "mssql":
                String mssqlImage = getPropertyOrDefault("tc.mssql.image", "mcr.microsoft.com/mssql/server:2022-latest");
                String mssqlPass = getPropertyOrDefault("tc.mssql.password", "StrongPassword123!");

                MSSQLServerContainer<?> mssql = new MSSQLServerContainer<>(mssqlImage)
                        .acceptLicense()
                        .withPassword(mssqlPass);
                activeContainer = mssql;
                break;

            case "oracle":
                String oracleImage = getPropertyOrDefault("tc.oracle.image", "gvenzl/oracle-free:23-slim-faststart");
                String oracleUser = getPropertyOrDefault("tc.oracle.username", "system");
                String oraclePass = getPropertyOrDefault("tc.oracle.password", "oracle");

                OracleContainer oracle = new OracleContainer(oracleImage)
                        .withDatabaseName(dbName)
                        .withUsername(oracleUser)
                        .withPassword(oraclePass);
                activeContainer = oracle;
                break;

            default:
                throw new UnsupportedOperationException("Unsupported database key: '" + dbType
                        + "'. Use 'h2', 'mysql', 'postgres', 'sqlserver', or 'oracle'.");
        }

        // Start container instance
        activeContainer.start();

        // Inject dynamic runtime parameters into system properties for DbUtil
        System.setProperty("db.url", activeContainer.getJdbcUrl());
        System.setProperty("db.user", activeContainer.getUsername());
        System.setProperty("db.password", activeContainer.getPassword());

        log.info("Successfully started [{}] Testcontainer. JDBC URL: {}", normalizedType.toUpperCase(), activeContainer.getJdbcUrl());
        return activeContainer;
    }

    public static void stopContainer() {
        if (activeContainer != null && activeContainer.isRunning()) {
            activeContainer.stop();
            log.info("--- [DB FACTORY] Active database container stopped successfully ---");
        }
    }

    private static String getPropertyOrDefault(String key, String defaultValue) {
        String value = ConfigReader.getProperty(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
}