package com.automation.e2e.base;

import com.automation.e2e.listeners.TestListener;
import com.automation.e2e.utils.ConfigReader;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import java.util.Base64;

@Listeners(TestListener.class)
public class Base {

    protected static final Logger log = LogManager.getLogger(Base.class);

    protected Playwright playwright;
    protected Browser browser;
    protected Page page;
    protected String activeEnvironment;

    @BeforeClass
    public void setupInfrastructure() {
        String systemEnv = System.getProperty("env");
        activeEnvironment = (systemEnv != null && !systemEnv.trim().isEmpty())
                ? systemEnv.toLowerCase().trim()
                : ConfigReader.getProperty("default.environment").toLowerCase().trim();

        log.info("Initializing Test Infrastructure | Environment: [{}]", activeEnvironment.toUpperCase());

        // CI/CD pipelines usually run headless
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", ConfigReader.getProperty("browser.headless")));
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
     * Captures full page screenshot and returns it as a Base64 string for report embedding.
     */
    public String captureScreenshotAsBase64() {
        try {
            if (page != null && !page.isClosed()) {
                byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
                return Base64.getEncoder().encodeToString(screenshotBytes);
            }
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
        }
        return "";
    }

    public Page getPage() {
        return page;
    }

    protected void navigateToApp(String appName) {
        String configKey = activeEnvironment + "." + appName + ".targetAppUrl";
        String targetUrl = ConfigReader.getProperty(configKey);

        if (targetUrl == null || targetUrl.isEmpty()) {
            configKey = "local." + appName + ".targetAppUrl";
            targetUrl = ConfigReader.getProperty(configKey);
        }

        log.info("Navigating to App [{}] | Env [{}] | URL: {}", appName.toUpperCase(), activeEnvironment.toUpperCase(), targetUrl);
        page.navigate(targetUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public String getActiveEnvironment() {
        return activeEnvironment;
    }

    @AfterClass(alwaysRun = true)
    public void teardownInfrastructure() {
        log.info("Tearing down browser context for environment: [{}]", activeEnvironment.toUpperCase());
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}