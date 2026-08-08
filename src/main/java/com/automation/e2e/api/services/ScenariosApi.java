//package com.automation.e2e.api.services;
//
//import com.automation.e2e.api.ApiClient;
//import com.automation.e2e.api.ApiEndpoints;
//import com.microsoft.playwright.APIRequestContext;
//import com.microsoft.playwright.APIResponse;
//
//import java.util.Map;
//
///**
// * Service class for the Scenarios API.
// * Encapsulates app routing, context passing, and endpoint constants.
// */
//public class ScenariosApi {
//
//    private static final String APP_NAME = "practiseapi";
//    private final APIRequestContext apiContext;
//
//    // The context is passed once during initialization
//    public ScenariosApi(APIRequestContext apiContext) {
//        this.apiContext = apiContext;
//    }
//
//    /**
//     * Executes a GET request to fetch all scenarios.
//     */
//    public APIResponse getAllScenarios() {
//        return ApiClient.executeGet(apiContext, APP_NAME, ApiEndpoints., null);
//    }
//
//    /**
//     * Executes a GET request with query parameters (e.g., filtering).
//     */
//    public APIResponse getScenariosWithFilters(Map<String, String> queryParams) {
//        return ApiClient.executeGet(apiContext, APP_NAME, ApiEndpoints.SCENARIOS, queryParams);
//    }
//
//    /**
//     * Executes a POST request using a JSON file payload to create a scenario.
//     */
//    public APIResponse createScenario(String jsonFilePath) {
//        return ApiClient.executePostFromJsonFile(apiContext, APP_NAME, ApiEndpoints.SCENARIOS, jsonFilePath);
//    }
//}