package com.automation.e2e.ui.saucedemo;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SaucedemoCartPage {

    private final Page page;
    private final Locator cartTotalAmount;
    private final Locator checkoutButton;

    public SaucedemoCartPage(Page page) {
        this.page = page;
        // Total price node on sauce-demo.myshopify.com/cart
        this.cartTotalAmount = page.locator("h2.subtotal, h2:has-text('£'), .cart__subtotal, .total h3").first();

        // Flexible visible selector for checkout input/button/anchor
        this.checkoutButton = page.locator("input[name='checkout']:visible, button[name='checkout']:visible, a[href*='checkout']:visible, input[value*='Check']:visible");
    }

    /**
     * Extracts numerical cart total value (e.g., "£115.00" -> 115.00)
     */
    public double getCartTotalAmount() {
        String rawTotal = UiActionsUtil.getElementText(cartTotalAmount);
        return Double.parseDouble(rawTotal.replaceAll("[^0-9.]", ""));
    }

    public boolean isCheckoutButtonVisible() {
        return checkoutButton.first().isVisible();
    }
}