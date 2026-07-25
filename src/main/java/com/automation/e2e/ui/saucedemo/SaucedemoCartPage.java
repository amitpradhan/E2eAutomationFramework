package com.automation.e2e.ui.saucedemo;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SaucedemoCartPage {

    private final Page page;
    private final Locator cartItemTitle;
    private final Locator checkoutButton;

    public SaucedemoCartPage(Page page) {
        this.page = page;
        this.cartItemTitle = page.locator("a[href*='/products/'], .cart-title");
        this.checkoutButton = page.locator("input[name='checkout'], button[name='checkout']");
    }

    public String getFirstCartItemName() {
        return UiActionsUtil.getElementText(cartItemTitle);
    }

    public boolean isCheckoutButtonVisible() {
        return checkoutButton.first().isVisible();
    }
}