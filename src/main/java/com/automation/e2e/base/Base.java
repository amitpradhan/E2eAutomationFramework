package com.automation.e2e.base;

import com.automation.e2e.listeners.TestListener;
import com.automation.e2e.utils.ConfigReader;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import java.util.Arrays;
import java.util.Base64;

@Listeners(TestListener.class)
public class Base {

    protected static final Logger log = LogManager.getLogger(Base.class);

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected String activeEnvironment;

    @BeforeClass
    public void setupInfrastructure() {
        String systemEnv = System.getProperty("env");
        activeEnvironment = (systemEnv != null && !systemEnv.trim().isEmpty())
                ? systemEnv.toLowerCase().trim()
                : ConfigReader.getProperty("default.environment").toLowerCase().trim();

        log.info("Initializing Test Infrastructure | Environment: [{}]", activeEnvironment.toUpperCase());

        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", ConfigReader.getProperty("browser.headless")));
        String browserChannel = ConfigReader.getProperty("browser.channel");
        double pageLoadTimeout = Double.parseDouble(ConfigReader.getProperty("timeout.page.load"));

        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setChannel(browserChannel);

        // Maximize browser window in headed mode
        if (!isHeadless) {
            launchOptions.setArgs(Arrays.asList("--start-maximized"));
        }

        browser = playwright.chromium().launch(launchOptions);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

        // Pass null viewport size when headed so browser uses full screen dimensions
        if (!isHeadless) {
            contextOptions.setViewportSize(null);
        } else {
            contextOptions.setViewportSize(1920, 1080);
        }

        context = browser.newContext(contextOptions);
        page = context.newPage();
        page.setDefaultNavigationTimeout(pageLoadTimeout);
    }

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

    public BrowserContext getContext() {
        return context;
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
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }


    /**
     * Fixture Getter for APIRequestContext.
     * Tied to the current BrowserContext, meaning API calls automatically
     * share cookies and authentication state with the UI Page.
     */
    public APIRequestContext getApiContext() {
        return getContext().request();
    }
    /**
     * Resolves the API Base URL from config.properties dynamically based on the active environment.
     */
    public String getBaseApiUrl(String appName) {
        String configKey = activeEnvironment + "." + appName + ".targetApiUrl";
        String targetUrl = ConfigReader.getProperty(configKey);

        if (targetUrl == null || targetUrl.isEmpty()) {
            configKey = "local." + appName + ".targetApiUrl";
            targetUrl = ConfigReader.getProperty(configKey);
        }
        return targetUrl;
    }

}