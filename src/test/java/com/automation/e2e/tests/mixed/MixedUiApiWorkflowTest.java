package com.automation.e2e.tests.mixed;

import com.automation.e2e.api.services.BillsApi;
import com.automation.e2e.base.Base;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MixedUiApiWorkflowTest extends Base {

    @Test(description = "Mix UI and API: Create a record via API and verify it appears in the UI grid")
    public void testApiDataCreationVerifiedInUi() {

        // ==========================================
        // 1. API PHASE: Create data via Service Object
        // ==========================================
        BillsApi billsApi = new BillsApi(getApiContext());

        // Single call executing the POST request from file
        APIResponse apiResponse = billsApi.createBill("payloads/create_scenario.json");
        Assert.assertEquals(apiResponse.status(), 201, "API failed to create test data!");

        // ==========================================
        // 2. UI PHASE: Verify in front-end
        // ==========================================
        navigateToApp("practiseapi");

        // Wait for grid to load and take a screenshot for Extent Reports
        captureScreenshotAsBase64();

        // Dynamically verify the title defined in our JSON file rendered in the UI
        boolean isRecordVisibleInUi = getPage().locator("tr:has-text('Test Automation Scenario')").isVisible();

        Assert.assertTrue(isRecordVisibleInUi, "Record created via API was not found in the UI Grid!");
    }
}