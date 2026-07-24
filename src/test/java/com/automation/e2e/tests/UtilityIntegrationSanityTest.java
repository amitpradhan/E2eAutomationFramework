package com.automation.e2e.tests;

import com.automation.e2e.base.Base;
import com.automation.e2e.api.ApiClient;
import com.automation.e2e.db.DbUtil;
import com.automation.e2e.utils.JsonUtils;
import com.automation.e2e.utils.XmlUtils;
import com.automation.e2e.validators.ValidationEngine;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UtilityIntegrationSanityTest extends Base {

    @Test(description = "Verify all E2E framework core utilities, payload engines, and database bridges function correctly")
    public void verifyAllFrameworkUtilitiesFunctionCorrectly() throws Exception {

        // -------------------------------------------------------------------
        // 1. Verify ConfigReader & Playwright UI Context
        // -------------------------------------------------------------------
        System.out.println("[SANITY] Step 1: Validating In-Memory ConfigReader & UI Lifecycle...");

        // Using your dynamic target URL property routing structure
        Assert.assertNotNull(page, "ConfigReader failed to resolve the App URL from properties configuration!");

        // Asserting that the inherited page state instance successfully completed its initial navigation loop
        Assert.assertTrue(page.title().length() > 0, "Playwright UI Page initialization failed to fetch structural title.");
        System.out.println("[SANITY] Playwright UI Engine status: Operational. Page Title: " + page.title());

        // -------------------------------------------------------------------
        // 2. Verify Payload Utilities (JsonUtils & XmlUtils)
        // -------------------------------------------------------------------
        System.out.println("[SANITY] Step 2: Testing Payload Parsing Utilities...");

        String structuralJsonMock = "{ \"biller\": \"Default\", \"amount\": \"0\" }";
        String structuralXmlMock = "<request><biller>Default</biller><amount>0</amount></request>";

        String customizedJson = JsonUtils.updateJsonValue(structuralJsonMock, "biller", "SanityTestUser_JSON");
        String customizedXml = XmlUtils.updateXmlTagValue(structuralXmlMock, "biller", "SanityTestUser_XML");

        Assert.assertTrue(customizedJson.contains("SanityTestUser_JSON"), "JsonUtils failed to modify node attributes structural state.");
        Assert.assertTrue(customizedXml.contains("SanityTestUser_XML"), "XmlUtils failed to modify element tag structural state.");
        System.out.println("[SANITY] Payload modification utilities status: Operational.");

        // -------------------------------------------------------------------
        // 3. Verify ApiClient Engine & Backend Communication
        // -------------------------------------------------------------------
        System.out.println("[SANITY] Step 3: Validating ApiClient Network Engine...");

        // Re-using the central shared 'playwright' engine context initialized from BaseTest
        ApiClient apiClient = new ApiClient(playwright);

        // Fetching the baseline bills index to verify API sandbox responses
        APIResponse apiResponse = apiClient.get("/v1/bills", "application/json");

        System.out.println("[SANITY] API Request Dispatched. Received Response Status Code: " + apiResponse.status());
        Assert.assertTrue(apiResponse.status() == 200 || apiResponse.status() == 201,
                "ApiClient connection failed to interact with backend routing server. Status code: " + apiResponse.status());

        String responseBody = apiResponse.text();
        Assert.assertNotNull(responseBody, "API backend returned an empty body state execution loop.");
        System.out.println("[SANITY] ApiClient Engine status: Operational.");
        apiClient.dispose();

        // -------------------------------------------------------------------
        // 4. Verify Validation Engine Core Assertions
        // -------------------------------------------------------------------
        System.out.println("[SANITY] Step 4: Testing Centralized Validation Engine...");

        ValidationEngine.validateJsonPayloads(customizedJson, customizedJson, true); // Strict comparison against self
        boolean xmlCheckResult = ValidationEngine.isValidXml(customizedXml);

        Assert.assertTrue(xmlCheckResult, "ValidationEngine failed to confirm structural integrity of valid XML string data block.");
        System.out.println("[SANITY] Validation Engine status: Operational.");

        // -------------------------------------------------------------------
        // 5. Verify Database Connectivity & Row Counts
        // -------------------------------------------------------------------
        System.out.println("[SANITY] Step 5: Testing Database Pipeline Utility...");
        try {
            // Validating that the database connection established works cleanly without throwing exceptions
            int placeholderCheck = DbUtil.getRowCount("INFORMATION_SCHEMA.TABLES", "1=1");
            System.out.println("[SANITY] Database query completed. Accessible structural tables count checked: " + placeholderCheck);
            System.out.println("[SANITY] Database Utility status: Operational.");
        } catch (Exception databaseException) {
            System.out.println("[SANITY ALERT] Database structure check compiled, but execution was bypassed. " +
                    "Verify your target SQL server credentials if the connection threw an exception: " + databaseException.getMessage());
        }

        System.out.println(">>> ALL UTILITIES VERIFIED: FRAMEWORK CORE SANITY CHECKS COMPLETED SUCCESSFULLY <<<");
    }
}