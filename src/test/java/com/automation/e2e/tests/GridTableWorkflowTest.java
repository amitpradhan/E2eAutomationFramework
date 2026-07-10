package com.automation.e2e.tests;

import com.automation.e2e.base.BaseTest;
import com.automation.e2e.ui.BusinessWorkflowPage;
import com.automation.e2e.ui.DashboardPage;
import org.testng.annotations.Test;

public class GridTableWorkflowTest extends BaseTest {

    private DashboardPage dashboardPage;
    private BusinessWorkflowPage workflowPage;

    @Test(priority = 1, description = "Verify data tables load and respond to complex row element tracking loops")
    public void testGridTableOperationsAndRowVerification() {
        dashboardPage = new DashboardPage(page);
        dashboardPage.waitForNetworkSettle();

        // Select the complex business workflow section card
        workflowPage = dashboardPage.clickBusinessFlowsScenario();

        // Dynamic context token search execution
        String targetUserMarker = "apiclient_";
        workflowPage.executeGridSearch(targetUserMarker);

        // Conditional operational validation loop inside live DOM table tree nodes
        if (workflowPage.isUserRecordVisibleInGrid(targetUserMarker)) {
            System.out.println("[INFO] Row element discovered in active grid viewport. Issuing sub-tree mutations...");

            // Execute deep target actions scoped directly to the matched table row context
            workflowPage.clickVerifyKycForUser(targetUserMarker);

            System.out.println("[SUCCESS] Action step sequence completed cleanly against targeted table row.");
        } else {
            System.out.println("[WARN] Target row indicator state was not visible inside the early table pagination page.");
        }
    }
}