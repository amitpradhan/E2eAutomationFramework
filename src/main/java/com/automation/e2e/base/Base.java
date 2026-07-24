package com.automation.e2e.base;

import com.automation.e2e.utils.ConfigReader;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class Base {
    protected Playwright playwright;
    protected Browser browser;
    protected Page page;
    protected String activeEnvironment;

    @BeforeClass
    public void setupTestInfrastructure() {
        // Resolve target execution environment from System properties (-Denv=sit) or fallback to config default
        String systemEnv = System.getProperty("env");
        activeEnvironment = (systemEnv != null && !systemEnv.trim().isEmpty())
                ? systemEnv.toLowerCase().trim()
                : ConfigReader.getProperty("default.environment").toLowerCase().trim();

        boolean isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("browser.headless"));
        String browserChannel = ConfigReader.getProperty("browser.channel");
        double pageLoadTimeout = Double.parseDouble(ConfigReader.getProperty("timeout.page.load"));

        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setChannel(browserChannel);

        browser = playwright.chromium().launch(launchOptions);
        page = browser.newPage();
        page.setDefaultNavigationTimeout(pageLoadTimeout);
    }

    /**
     * Resolves the target app URL dynamically based on app prefix and active environment.
     * Example: navigateToApp("saucedemo") -> fetches "sit.saucedemo.targetAppUrl" if -Denv=sit
     */
    protected void navigateToApp(String appName) {
        String configKey = activeEnvironment + "." + appName + ".targetAppUrl";
        String targetUrl = ConfigReader.getProperty(configKey);

        if (targetUrl == null || targetUrl.isEmpty()) {
            // Fallback lookup if environment key is omitted
            configKey = "local." + appName + ".targetAppUrl";
            targetUrl = ConfigReader.getProperty(configKey);
        }

        page.navigate(targetUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public String getActiveEnvironment() {
        return activeEnvironment;
    }

    @AfterClass(alwaysRun = true)
    public void teardownTestInfrastructure() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}