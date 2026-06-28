package com.automation.e2e.ui;

import com.microsoft.playwright.Page;

public class BasePage {
    protected Page page;

    /**
     * Constructor to initialize and share the Playwright Page context
     * across all inheriting Page Object classes.
     *
     * @param page The active Playwright Page instance from the test context.
     */
    public BasePage(Page page) {
        this.page = page;
    }

    /**
     * Shared Utility: Returns the current page title.
     * Accessible by any sub-page class extending BasePage.
     */
    public String getPageTitle() {
        return page.title().trim();
    }

    /**
     * Shared Utility: Returns the current page URL.
     */
    public String getPageUrl() {
        return page.url().trim();
    }
}