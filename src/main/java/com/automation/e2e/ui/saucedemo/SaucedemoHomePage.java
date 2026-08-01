package com.automation.e2e.ui.saucedemo;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SaucedemoHomePage {

    private final Page page;

    public SaucedemoHomePage(Page page) {
        this.page = page;
    }

    /**
     * Selects a specific product using its element ID (e.g., "product-1", "product-2")
     */
    public SaucedemoProductPage selectProductById(String productId) {
        Locator productLocator = page.locator("#" + productId);
        UiActionsUtil.click(productLocator);
        return new SaucedemoProductPage(page);
    }

    /**
     * Fallback to select a product by its URL path string (e.g., "grey-jacket")
     */
    public SaucedemoProductPage selectProductBySlug(String productSlug) {
        Locator productLocator = page.locator("a[href*='" + productSlug + "']");
        UiActionsUtil.click(productLocator);
        return new SaucedemoProductPage(page);
    }
}