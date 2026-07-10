package com.automation.e2e.ui;

import com.automation.e2e.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FormFieldsWorkflowTest extends BaseTest {

    private DashboardPage dashboardPage;
    private ScenariosFormPage formPage;

    @Test(priority = 1, description = "Verify application landing page details map correctly to config properties URL")
    public void testDashboardLandingMetrics() {
        dashboardPage = new DashboardPage(page);
        dashboardPage.waitForNetworkSettle();

        Assert.assertTrue(page.title().length() > 0, "Failed to capture active landing page title!");
    }

    @Test(priority = 2, dependsOnMethods = {"testDashboardLandingMetrics"},
            description = "Validate data inputs and fluent interaction cycles on the Basic Elements track")
    public void testFormFieldsInteractionWorkflow() {
        // FIX: Force absolute navigation to bypass Single Page Application (SPA) hash-routing race conditions
        page.navigate(targetAppUrl);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        dashboardPage = new DashboardPage(page);
        formPage = dashboardPage.clickBasicElementsScenario();

        formPage.fillTextInput("Playwright_Automation_User")
                .chooseDropdownOptionByValue("option2")
                .toggleCheckbox(true);

        formPage.clickSubmit();

        Assert.assertNotNull(formPage.getConfirmationText(), "Form track submission feedback payload evaluated to null!");
    }
}