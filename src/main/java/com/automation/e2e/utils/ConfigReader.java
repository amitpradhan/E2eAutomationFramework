package com.automation.e2e.utils;

public class ConfigReader {

    private static final String ENV;
    private static final String APP_URL;
    private static final String API_BASE_URL;
    private static final String DB_URL;
    private static final String DB_USER;

    static {
        // 1. Read 'env' VM argument or fallback to 'local'
        String environment = System.getProperty("env");
        if (environment == null || environment.trim().isEmpty()) {
            environment = "local";
        }
        ENV = environment.toLowerCase().trim();

        // 2. Assign environment variables directly in-memory based on target switch state
        switch (ENV) {
            case "sit":
                APP_URL = "https://gauravkhurana.com/practise-api/ui/index.html#/scenarios";
                API_BASE_URL = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
                DB_URL = "jdbc:sqlserver://sit-db-server:1433;databaseName=sit_billing_db;encrypt=true;trustServerCertificate=true;";
                DB_USER = "sit_automation_user";
                break;

            case "uat":
                APP_URL = "https://uat-gauravkhurana.com/practise-api/ui/index.html#/scenarios";
                API_BASE_URL = "https://uat-billpay-api.workers.dev";
                DB_URL = "jdbc:sqlserver://uat-db-server:1433;databaseName=uat_billing_db;encrypt=true;trustServerCertificate=true;";
                DB_USER = "uat_automation_user";
                break;

            case "local":
            default:
                APP_URL = "https://gauravkhurana.com/practise-api/ui/index.html#/scenarios";
                API_BASE_URL = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
                DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=local_billing_db;encrypt=true;trustServerCertificate=true;";
                DB_USER = "SA";
                break;
        }
        System.out.println(">>> SUCCESS: Active Target Environment Configured Profile: [" + ENV.toUpperCase() + " IN-MEMORY] <<<");
    }

    public static String getEnv() { return ENV; }
    public static String getAppUrl() { return APP_URL; }
    public static String getApiBaseUrl() { return API_BASE_URL; }
    public static String getDbUrl() { return DB_URL; }
    public static String getDbUser() { return DB_USER; }
}