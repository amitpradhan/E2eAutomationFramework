package com.automation.e2e.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
            properties = new Properties();
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties file from resources!", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Resolves the target app URL dynamically.
     * Looks for a System property 'env' first (Maven CLI), otherwise falls back to 'default.environment'.
     */
    public static String getTargetAppUrl() {
        String activeEnv = System.getProperty("env");
        if (activeEnv == null || activeEnv.isEmpty()) {
            activeEnv = getProperty("default.environment");
        }
        return getProperty(activeEnv + ".targetAppUrl");
    }
}