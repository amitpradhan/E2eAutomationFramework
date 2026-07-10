package com.automation.e2e.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage extends BasePage {

    private final Locator basicElementsCard;
    private final Locator businessFlowsCard;

    public DashboardPage(Page page) {
        super(page);
        // Using explicit, standard text locator binding strategies
        this.basicElementsCard = page.locator("text=Basic Elements").first();
        this.businessFlowsCard = page.locator("text=Business Flows").first();
    }

    public ScenariosFormPage clickBasicElementsScenario() {
        // Explicitly wait until the text is bound to the DOM and visible
        basicElementsCard.waitFor();
        basicElementsCard.click();
        waitForNetworkSettle();
        return new ScenariosFormPage(page);
    }

    public BusinessWorkflowPage clickBusinessFlowsScenario() {
        businessFlowsCard.waitFor();
        businessFlowsCard.click();
        waitForNetworkSettle();
        return new BusinessWorkflowPage(page);
    }
}