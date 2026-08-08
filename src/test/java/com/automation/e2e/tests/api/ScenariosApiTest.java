//package com.automation.e2e.tests.api;
//
//import com.automation.e2e.api.services.ScenariosApi;
//import com.automation.e2e.base.Base;
//import com.microsoft.playwright.APIResponse;
//import org.testng.Assert;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//public class ScenariosApiTest extends Base {
//
//    private ScenariosApi scenariosApi;
//
//    @BeforeMethod
//    public void setupApiServices() {
//        // Initialize the service object with the thread-safe API context
//        scenariosApi = new ScenariosApi(getApiContext());
//    }
//
//    @Test(priority = 1, description = "Verify GET /scenarios endpoint execution")
//    public void testGetScenariosEndpoint() {
//        // 1. Single execution step
//        APIResponse response = scenariosApi.getAllScenarios();
//
//        // 2. Assertions
//        Assert.assertEquals(response.status(), 200, "Expected HTTP status 200 OK");
//        Assert.assertTrue(response.text().contains("id"), "Expected response payload to contain entity identifiers");
//    }
//
//    @Test(priority = 2, description = "Verify POST /scenarios endpoint execution using a JSON payload file")
//    public void testCreateScenarioEndpoint() {
//        // 1. Single execution step with payload path
//        APIResponse response = scenariosApi.createScenario("payloads/create_scenario.json");
//
//        // 2. Assertions
//        Assert.assertEquals(response.status(), 201, "Expected HTTP status 201 Created");
//        Assert.assertTrue(response.text().contains("Test Automation Scenario"), "Expected response to confirm created title");
//    }
//}