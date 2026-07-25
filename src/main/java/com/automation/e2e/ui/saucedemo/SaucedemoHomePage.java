package com.automation.e2e.ui.saucedemo;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SaucedemoHomePage {

    private final Page page;
    private final Locator firstProductCard;
    private final Locator searchInput;

    public SaucedemoHomePage(Page page) {
        this.page = page;
        // Targets product links on sauce-demo.myshopify.com
        // (UiActionsUtil handles calling .first() automatically)
        this.firstProductCard = page.locator("a[href*='/products/']");
        this.searchInput = page.locator("input[name='q']");
    }

    public SaucedemoProductPage selectFirstFeaturedProduct() {
        UiActionsUtil.click(firstProductCard);
        return new SaucedemoProductPage(page);
    }

    public SaucedemoHomePage searchProduct(String query) {
        UiActionsUtil.enterText(searchInput, query);
        page.keyboard().press("Enter");
        return this;
    }
}