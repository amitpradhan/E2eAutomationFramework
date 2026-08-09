package com.automation.e2e.ui.pages.gk;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage extends BasePage {

    private final Locator basicElementsCard;
    private final Locator businessFlowsCard;

    public DashboardPage(Page page) {
        super(page);
        // FIX: Using case-insensitive tag-scoped pseudo-selectors to handle any CSS transformations
        this.basicElementsCard = page.locator("*:has-text('Basic Elements'), a:has-text('Basic Elements')").first();
        this.businessFlowsCard = page.locator("*:has-text('Business Flows'), *:has-text('Business Flow'), a:has-text('Business')").first();
    }

    public ScenariosFormPage clickBasicElementsScenario() {
        UiActionsUtil.click(basicElementsCard);
        waitForNetworkSettle();
        return new ScenariosFormPage(page);
    }

    public BusinessWorkflowPage clickBusinessFlowsScenario() {
        UiActionsUtil.click(businessFlowsCard);
        waitForNetworkSettle();
        return new BusinessWorkflowPage(page);
    }
}