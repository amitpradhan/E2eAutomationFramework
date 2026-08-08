package com.automation.e2e.api;

import com.automation.e2e.base.Base;
import com.automation.e2e.listeners.TestListener;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient extends Base {

    private static final Logger log = LogManager.getLogger(ApiClient.class);

    /**
     * Generic GET Request resolver using app name and relative endpoint.
     */
    public static APIResponse executeGet(APIRequestContext apiContext, String appName, String relativePath, Map<String, String> queryParams) {
        String baseUrl = getInstanceBaseApiUrl(appName);
        String fullUrl = baseUrl + relativePath;

        RequestOptions options = RequestOptions.create();
        if (queryParams != null) {
            queryParams.forEach(options::setQueryParam);
        }

        logRequest("GET", fullUrl, null);
        APIResponse response = apiContext.get(fullUrl, options);
        logResponse(response);
        return response;
    }

    /**
     * Generic POST Request resolver that reads JSON payload directly from file path.
     */
    public static APIResponse executePostFromJsonFile(APIRequestContext apiContext, String appName, String relativePath, String jsonFilePath) {
        String baseUrl = getInstanceBaseApiUrl(appName);
        String fullUrl = baseUrl + relativePath;

        String jsonPayload = loadJsonFile(jsonFilePath);

        RequestOptions options = RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(jsonPayload);

        logRequest("POST", fullUrl, jsonPayload);
        APIResponse response = apiContext.post(fullUrl, options);
        logResponse(response);
        return response;
    }

    /**
     * Helper to read JSON files from test resources directory.
     */
    private static String loadJsonFile(String relativePath) {
        try {
            File file = new File("src/test/resources/" + relativePath);
            if (!file.exists()) {
                file = new File(relativePath); // Fallback to absolute/relative check
            }
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to load JSON payload file: {}", relativePath, e);
            throw new RuntimeException("Could not read JSON file from path: " + relativePath, e);
        }
    }

    private static String getInstanceBaseApiUrl(String appName) {
        Base baseInstance = new Base();
        String activeEnv = System.getProperty("env", "local").toLowerCase().trim();
        String configKey = activeEnv + "." + appName + ".targetApiUrl";
        String url = com.automation.e2e.utils.ConfigReader.getProperty(configKey);
        if (url == null || url.isEmpty()) {
            url = com.automation.e2e.utils.ConfigReader.getProperty("local." + appName + ".targetApiUrl");
        }
        return url;
    }

    private static void logRequest(String method, String endpoint, Object payload) {
        String reqLog = String.format("API Request: [%s] %s", method, endpoint);
        log.info(reqLog);
        if (TestListener.getTest() != null) {
            TestListener.getTest().log(Status.INFO, reqLog);
            if (payload != null) {
                TestListener.getTest().log(Status.INFO, "Payload: <pre>" + payload.toString() + "</pre>");
            }
        }
    }

    private static void logResponse(APIResponse response) {
        String resLog = String.format("API Response: Status [%d] %s", response.status(), response.statusText());
        log.info(resLog);
        if (TestListener.getTest() != null) {
            Status status = (response.status() >= 200 && response.status() < 300) ? Status.PASS : Status.WARNING;
            TestListener.getTest().log(status, resLog);
            try {
                String responseBody = new String(response.body());
                if (!responseBody.isEmpty()) {
                    TestListener.getTest().log(Status.INFO, "Response Body: <pre>" + responseBody + "</pre>");
                }
            } catch (Exception e) {
                log.warn("Could not parse response body for logging.");
            }
        }
    }


    /**
     * Generic GET Request resolver using app name and relative endpoint.
     */
    public static APIResponse executeGet(APIRequestContext apiContext, String appName, String relativePath, Map<String, String> headers, Map<String, String> queryParams) {
        String baseUrl = getInstanceBaseApiUrl(appName);
        String fullUrl = baseUrl + relativePath;

        RequestOptions options = RequestOptions.create();

        // Inject Custom Headers (e.g., API Keys, Bearer Tokens)
        if (headers != null) {
            headers.forEach(options::setHeader);
        }
        if (queryParams != null) {
            queryParams.forEach(options::setQueryParam);
        }

        logRequest("GET", fullUrl, null);
        APIResponse response = apiContext.get(fullUrl, options);
        logResponse(response);
        return response;
    }

    /**
     * Generic POST Request resolver that reads JSON payload directly from file path.
     */
    public static APIResponse executePostFromJsonFile(APIRequestContext apiContext, String appName, String relativePath, Map<String, String> headers, String jsonFilePath) {
        String baseUrl = getInstanceBaseApiUrl(appName);
        String fullUrl = baseUrl + relativePath;

        String jsonPayload = loadJsonFile(jsonFilePath);

        RequestOptions options = RequestOptions.create()
                // THIS LINE IS CRITICAL FOR 400 ERRORS
                .setHeader("Content-Type", "application/json")
                .setData(jsonPayload);

        if (headers != null) {
            headers.forEach(options::setHeader);
        }

        // Inject Custom Headers
        if (headers != null) {
            headers.forEach(options::setHeader);
        }

        logRequest("POST", fullUrl, jsonPayload);
        APIResponse response = apiContext.post(fullUrl, options);
        logResponse(response);
        return response;
    }


}