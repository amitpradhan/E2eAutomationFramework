package com.automation.e2e.api.services;

import com.automation.e2e.api.ApiClient;
import com.automation.e2e.api.ApiEndpoints;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for the Bills API.
 * Encapsulates app routing, context passing, headers, and endpoint constants.
 */
public class BillsApi {

    private static final String APP_NAME = "practiseapi";
    private final APIRequestContext apiContext;
    private final Map<String, String> defaultHeaders;

    public BillsApi(APIRequestContext apiContext) {
        this.apiContext = apiContext;

        // The practise-api backend requires this specific API Key
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put("X-API-Key", "demo-api-key-123");
    }

    public APIResponse getAllBills() {
        return ApiClient.executeGet(apiContext, APP_NAME, ApiEndpoints.BILLS, defaultHeaders, null);
    }

    public APIResponse createBill(String jsonFilePath) {
        return ApiClient.executePostFromJsonFile(apiContext, APP_NAME, ApiEndpoints.BILLS, defaultHeaders, jsonFilePath);
    }
}