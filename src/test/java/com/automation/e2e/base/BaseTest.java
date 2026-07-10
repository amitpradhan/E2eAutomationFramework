package com.automation.e2e.base;

import com.automation.e2e.utils.ConfigReader;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected Page page;
    protected String targetAppUrl;

    @BeforeClass
    public void setupTestInfrastructure() {
        // 1. Resolve configuration values from your properties file
        targetAppUrl = ConfigReader.getTargetAppUrl();
        boolean isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("browser.headless"));
        String browserChannel = ConfigReader.getProperty("browser.channel");
        double pageLoadTimeout = Double.parseDouble(ConfigReader.getProperty("timeout.page.load"));

        // 2. Initialize Playwright Context Engine
        playwright = Playwright.create();

        // 3. Configure Browser launch parameters using your custom config values
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setChannel(browserChannel);

        browser = playwright.chromium().launch(launchOptions);
        page = browser.newPage();

        // 4. Apply your global page load timeout parameters safely
        page.setDefaultNavigationTimeout(pageLoadTimeout);

        // 5. Navigate to the base environment URL instantly and wait for network stability
        page.navigate(targetAppUrl);

        // FIX: Added explicit network idle wait state to ensure client-side rendering
        // settles completely before any test methods execute.
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @AfterClass(alwaysRun = true)
    public void teardownTestInfrastructure() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}