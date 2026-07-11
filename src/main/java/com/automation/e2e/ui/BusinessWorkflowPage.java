package com.automation.e2e.ui;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class BusinessWorkflowPage extends BasePage {

    private final Locator tableGridRows;
    private final Locator globalSearchInput;

    public BusinessWorkflowPage(Page page) {
        super(page);
        this.tableGridRows = page.locator("table tr, .user-list tr");
        this.globalSearchInput = page.locator("input[placeholder*='Search']").first();
    }

    public BusinessWorkflowPage executeGridSearch(String dynamicTerm) {
        UiActionsUtil.enterText(globalSearchInput, dynamicTerm);
        // Retaining your custom keyboard engine call fixed earlier
        page.keyboard().press("Enter");
        waitForNetworkSettle();
        return this;
    }

    public Locator findTargetRowByMatchingText(String uniqueMarker) {
        return tableGridRows.filter(new Locator.FilterOptions().setHasText(uniqueMarker));
    }

    public boolean isUserRecordVisibleInGrid(String uniqueMarker) {
        return findTargetRowByMatchingText(uniqueMarker).first().isVisible();
    }

    public void clickVerifyKycForUser(String uniqueMarker) {
        Locator targetedRow = findTargetRowByMatchingText(uniqueMarker);
        Locator actionButton = targetedRow.locator("button:has-text('Verify KYC')").first();

        // Use central utility to execute the dynamic row contextual click
        UiActionsUtil.click(actionButton);
        waitForNetworkSettle();
    }
}