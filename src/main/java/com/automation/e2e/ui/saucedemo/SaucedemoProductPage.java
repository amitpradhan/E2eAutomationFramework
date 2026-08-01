package com.automation.e2e.ui.saucedemo;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public class SaucedemoProductPage {

    private final Page page;
    private final Locator addToCartButton;
    private final Locator productTitle;
    private final Locator productPrice;
    private final Locator viewCartLink;
    private final Locator homeLogoLink;

    public SaucedemoProductPage(Page page) {
        this.page = page;
        this.addToCartButton = page.locator("input[type='submit'][name='add'], button[name='add'], input[value*='Add']");
        this.productTitle = page.locator("h1, .product-title");

        // FIX: Added ':visible' filter to ignore hidden desktop/mobile price wrapper elements
        this.productPrice = page.locator("h2:visible, .price:visible, span.money:visible, #product-price:visible");

        // Target visible cart links to skip hidden responsive menu links
        this.viewCartLink = page.locator("a[href*='/cart']:visible");
        this.homeLogoLink = page.locator("a[href='/'], a#logo, .site-header__logo a");
    }

    public String getProductTitle() {
        return UiActionsUtil.getElementText(productTitle);
    }

    public double getProductPrice() {
        String rawPrice = UiActionsUtil.getElementText(productPrice);
        return Double.parseDouble(rawPrice.replaceAll("[^0-9.]", ""));
    }

    public SaucedemoProductPage clickAddToCart() {
        UiActionsUtil.click(addToCartButton);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public SaucedemoHomePage returnToHomePage() {
        UiActionsUtil.click(homeLogoLink);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return new SaucedemoHomePage(page);
    }

    public SaucedemoCartPage navigateToCart() {
        UiActionsUtil.click(viewCartLink);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return new SaucedemoCartPage(page);
    }
}