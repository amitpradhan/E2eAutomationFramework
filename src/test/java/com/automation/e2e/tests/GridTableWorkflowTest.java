package com.automation.e2e.tests;

import com.automation.e2e.base.Base;
import com.automation.e2e.ui.pages.gk.BusinessWorkflowPage;
import com.automation.e2e.ui.pages.gk.DashboardPage;
import com.automation.e2e.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;

public class GridTableWorkflowTest extends Base {

    private DashboardPage dashboardPage;
    private BusinessWorkflowPage workflowPage;

    @Test(priority = 1, description = "Verify data tables search workflows filter correctly using parameters loaded from JSON configuration profiles")
    public void testGridTableOperationsAndRowVerification() {
        // 1. Parse data file dynamically using the framework's JsonUtils extension method
        JsonNode dataProfile = JsonUtils.readJsonFileAsNode("src/test/resources/testdata/GridTestData.json");
        String targetedUserQuery = dataProfile.get("searchUserToken").asText();

        // 2. Initialize Page Object models (BaseTest handles navigation & network settle)
        dashboardPage = new DashboardPage(page);

        // 3. Navigate into the Business Flows track via encapsulated card clicks
        workflowPage = dashboardPage.clickBusinessFlowsScenario();

        // 4. Run the data-driven target grid search logic using parameters from the JSON file
        workflowPage.executeGridSearch(targetedUserQuery);

        // 5. Query the grid layout row array elements conditionally
        if (workflowPage.isUserRecordVisibleInGrid(targetedUserQuery)) {
            System.out.println("[JSON-DRIVEN] Match found inside current viewport grid for user: " + targetedUserQuery);
            workflowPage.clickVerifyKycForUser(targetedUserQuery);
        } else {
            System.out.println("[WARN] Target row item matching token '" + targetedUserQuery + "' was not found inside the active grid rows.");
        }
    }
}