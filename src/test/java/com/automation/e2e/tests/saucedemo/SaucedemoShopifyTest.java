package com.automation.e2e.tests.saucedemo;

import com.automation.e2e.base.Base;
import com.automation.e2e.listeners.TestListener;
import com.automation.e2e.ui.saucedemo.SaucedemoCartPage;
import com.automation.e2e.ui.saucedemo.SaucedemoHomePage;
import com.automation.e2e.ui.saucedemo.SaucedemoProductPage;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SaucedemoShopifyTest extends Base {

    private SaucedemoHomePage homePage;
    private SaucedemoProductPage productPage;
    private SaucedemoCartPage cartPage;

    @Test(priority = 1, description = "Verify browsing products and adding items to cart on SauceDemo Shopify")
    public void testAddProductToCartWorkflow() {
        // 1. Navigate dynamically using environment resolver (-Denv=local / sit / uat)
        navigateToApp("saucedemo");
        logStepScreenshot("1. Home Page Loaded");

        homePage = new SaucedemoHomePage(page);

        // 2. Select Product by element ID ("product-1" -> Grey Jacket)
        productPage = homePage.selectProductById("product-1");
        String selectedTitle = productPage.getProductTitle();
        logStepScreenshot("2. Product Detail Page - " + selectedTitle);

        // 3. Add to Cart
        productPage.clickAddToCart();
        logStepScreenshot("3. Product Added to Cart State");

        // 4. View Cart & Validate
        cartPage = productPage.navigateToCart();
        logStepScreenshot("4. Shopping Cart Page View");

        Assert.assertTrue(cartPage.isCheckoutButtonVisible(), "Checkout button is not visible inside shopping cart!");
    }

    /**
     * Attaches an intermediate Base64 step screenshot directly into Extent Reports
     */
    private void logStepScreenshot(String stepDescription) {
        String base64Img = captureScreenshotAsBase64();
        if (!base64Img.isEmpty() && TestListener.getTest() != null) {
            TestListener.getTest().log(
                    Status.INFO,
                    stepDescription,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Img).build()
            );
        }
    }
}