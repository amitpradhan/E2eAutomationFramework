package com.automation.e2e.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class BusinessWorkflowPage extends BasePage {

    private final Locator tableGridRows;
    private final Locator globalSearchInput;

    public BusinessWorkflowPage(Page page) {
        super(page);
        this.tableGridRows = page.locator("table tr, .user-list tr");
        this.globalSearchInput = page.locator("input[placeholder*='Search'], #search-box").first();
    }

    public BusinessWorkflowPage executeGridSearch(String dynamicTerm) {
        globalSearchInput.fill(dynamicTerm);
        page.keyboard().press("Enter");
        waitForNetworkSettle();
        return this;
    }

    public Locator findTargetRowByMatchingText(String uniqueMarker) {
        return tableGridRows.filter(new Locator.FilterOptions().setHasText(uniqueMarker));
    }

    public boolean isUserRecordVisibleInGrid(String uniqueMarker) {
        Locator matchingRow = findTargetRowByMatchingText(uniqueMarker);
        return matchingRow.first().isVisible();
    }

    public void clickVerifyKycForUser(String uniqueMarker) {
        Locator targetedRow = findTargetRowByMatchingText(uniqueMarker);
        // Scans localized row tree structure to find action buttons safely
        targetedRow.locator("button:has-text('Verify KYC'), button[data-action='kyc']").first().click();
        waitForNetworkSettle();
    }

    public void clickDeleteForUser(String uniqueMarker) {
        Locator targetedRow = findTargetRowByMatchingText(uniqueMarker);
        targetedRow.locator("button:has-text('Delete'), button[data-action='delete']").first().click();
        waitForNetworkSettle();
    }
}