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

public class SaucedemoMultiItemCartTest extends Base {

    private SaucedemoHomePage homePage;
    private SaucedemoProductPage productPage;
    private SaucedemoCartPage cartPage;

    @Test(priority = 1, description = "Validate cumulative item total price matching cart checkout amount for Grey and Noir jackets")
    public void testCartTotalCalculationForMultipleItems() {
        double expectedCalculatedTotal = 0.0;

        // 1. Navigate to target SauceDemo app URL
        navigateToApp("saucedemo");
        logStepScreenshot("1. Home Page Loaded");

        homePage = new SaucedemoHomePage(page);

        // 2. Add First Item: Grey Jacket (#product-1) - £55.00
        productPage = homePage.selectProductById("product-1");
        double greyJacketPrice = productPage.getProductPrice();
        expectedCalculatedTotal += greyJacketPrice;

        logStepScreenshot("2. Selected Grey Jacket - Price: £" + greyJacketPrice);
        productPage.clickAddToCart();

        // 3. Return to Home Page cleanly & Add Second Item: Noir Jacket (#product-2) - £60.00
        navigateToApp("saucedemo");
        homePage = new SaucedemoHomePage(page);

        productPage = homePage.selectProductById("product-2");
        double noirJacketPrice = productPage.getProductPrice();
        expectedCalculatedTotal += noirJacketPrice;

        logStepScreenshot("3. Selected Noir Jacket - Price: £" + noirJacketPrice);
        productPage.clickAddToCart();

        // 4. Navigate to Cart & Fetch Actual Total
        cartPage = productPage.navigateToCart();
        logStepScreenshot("4. Cart Summary Page");

        double actualCartTotal = cartPage.getCartTotalAmount();

        // 5. Assertions
        logStepScreenshot("5. Price Verification - Expected: £" + expectedCalculatedTotal + " | Actual Cart: £" + actualCartTotal);
        Assert.assertEquals(actualCartTotal, expectedCalculatedTotal, 0.01, "Cart total price does not match expected items sum!");
        Assert.assertTrue(cartPage.isCheckoutButtonVisible(), "Checkout action button is not visible!");
    }

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