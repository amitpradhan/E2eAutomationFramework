package com.automation.e2e.tests;

import com.automation.e2e.base.Base;
import com.automation.e2e.listeners.TestListener;
import com.automation.e2e.ui.pages.gk.DashboardPage;
import com.automation.e2e.ui.pages.gk.ScenariosFormPage;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FormFieldsWorkflowTest extends Base {

    private DashboardPage dashboardPage;
    private ScenariosFormPage formPage;

    @Test(priority = 1)
    public void testFormFieldsInteractionWorkflow() {
        navigateToApp("gk");
        logPageScreenshot("1. Dashboard Landing Page");

        dashboardPage = new DashboardPage(page);
        formPage = dashboardPage.clickBasicElementsScenario();
        logPageScreenshot("2. Form Fields Scenario Page");

        formPage.fillTextInput("Amit")
                .chooseDropdownOptionByValue("option2")
                .toggleCheckbox(true);
        logPageScreenshot("3. Filled Form Data");

        formPage.clickSubmit();
        logPageScreenshot("4. Form Submission Output");

        Assert.assertNotNull(formPage.getConfirmationText(), "Form submission failed!");
    }

    /**
     * Helper method to capture and attach intermediate page step screenshots to Extent Report
     */
    private void logPageScreenshot(String stepName) {
        String base64Img = captureScreenshotAsBase64();
        if (!base64Img.isEmpty() && TestListener.getTest() != null) {
            TestListener.getTest().log(
                    Status.INFO,
                    stepName,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Img).build()
            );
        }
    }
}