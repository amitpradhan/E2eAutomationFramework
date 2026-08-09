package com.automation.e2e.ui.pages.gk;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public class BasePage {
    protected Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    public String getPageTitle() {
        return page.title().trim();
    }

    public String getPageUrl() {
        return page.url().trim();
    }

    /**
     * Shared wrapper to guarantee client framework network hydration completes.
     */
    public void waitForNetworkSettle() {
        page.waitForLoadState(LoadState.LOAD);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}