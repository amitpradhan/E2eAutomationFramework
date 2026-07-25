package com.automation.e2e.ui.saucedemo;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SaucedemoProductPage {

    private final Page page;
    private final Locator addToCartButton;
    private final Locator productTitle;
    private final Locator viewCartLink;

    public SaucedemoProductPage(Page page) {
        this.page = page;
        // Standard Shopify 'Add to Cart' form inputs/buttons
        this.addToCartButton = page.locator("input[type='submit'][name='add'], button[name='add'], input[value*='Add']");
        this.productTitle = page.locator("h1, .product-title");
        this.viewCartLink = page.locator("a[href*='/cart']");
    }

    public String getProductTitle() {
        return UiActionsUtil.getElementText(productTitle);
    }

    public SaucedemoProductPage clickAddToCart() {
        UiActionsUtil.click(addToCartButton);
        return this;
    }

    public SaucedemoCartPage navigateToCart() {
        UiActionsUtil.click(viewCartLink);
        return new SaucedemoCartPage(page);
    }
}