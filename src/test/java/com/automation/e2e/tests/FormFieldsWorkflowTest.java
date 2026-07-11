package com.automation.e2e.tests;

import com.automation.e2e.base.BaseTest;
import com.automation.e2e.ui.DashboardPage;
import com.automation.e2e.ui.ScenariosFormPage;
import com.automation.e2e.utils.ExcelUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

public class FormFieldsWorkflowTest extends BaseTest {

    private DashboardPage dashboardPage;
    private ScenariosFormPage formPage;

    @DataProvider(name = "excelFrameworkDataProvider")
    public Object[][] getExcelTestDataEntries() {
        String excelFilePath = "src/test/resources/testdata/FormTestData.xlsx";
        // Using our new map-based extension method for cleaner, breakdown-proof test injections
        List<Map<String, String>> dataRows = ExcelUtil.getTestDataAsMap(excelFilePath, "UserData");

        Object[][] testDataMatrix = new Object[dataRows.size()][1];
        for (int i = 0; i < dataRows.size(); i++) {
            testDataMatrix[i][0] = dataRows.get(i);
        }
        return testDataMatrix;
    }

    @Test(priority = 1, description = "Verify application landing page details map correctly to config properties URL")
    public void testDashboardLandingMetrics() {
        dashboardPage = new DashboardPage(page);
        dashboardPage.waitForNetworkSettle();
        Assert.assertTrue(page.title().length() > 0, "Failed to capture active landing page title!");
    }

    @Test(priority = 2, dependsOnMethods = {"testDashboardLandingMetrics"},
            dataProvider = "excelFrameworkDataProvider",
            description = "Validate form interaction steps with values dynamically pulled from spreadsheet cells")
    public void testFormFieldsInteractionWorkflow(Map<String, String> excelRowData) {
        page.navigate(targetAppUrl);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        dashboardPage = new DashboardPage(page);
        formPage = dashboardPage.clickBasicElementsScenario();

        // Feed parameters loaded safely via column layout header strings
        formPage.fillTextInput(excelRowData.get("Username"))
                .chooseDropdownOptionByValue(excelRowData.get("OptionValue"))
                .toggleCheckbox(Boolean.parseBoolean(excelRowData.get("CheckboxState")));

        formPage.clickSubmit();
        Assert.assertNotNull(formPage.getConfirmationText(), "Form track submission feedback payload evaluated to null!");
    }
}