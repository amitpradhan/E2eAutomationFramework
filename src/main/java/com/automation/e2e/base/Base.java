package com.automation.e2e.base;

import com.automation.e2e.db.DbContainerFactory;
import com.automation.e2e.listeners.TestListener;
import com.automation.e2e.utils.ConfigReader;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;

import java.util.Arrays;
import java.util.Base64;

@Listeners(TestListener.class)
public class Base {

    protected static final Logger log = LogManager.getLogger(Base.class);

    // Global engine instances across the suite
    private static Playwright playwright;
    private static Browser browser;

    // ThreadLocal wrappers to isolate context & page per parallel execution thread
    private static final ThreadLocal<BrowserContext> threadContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> threadPage = new ThreadLocal<>();
    private static final ThreadLocal<String> threadEnvironment = new ThreadLocal<>();

    // Protected fields maintained for backward compatibility with existing test classes
    protected Page page;
    protected BrowserContext context;
    protected String activeEnvironment;

    @BeforeSuite(alwaysRun = true)
    public void globalSetupHook() {
        // 1. Resolve target DB vendor (defaults to h2 or config property)
        String targetDbVendor = System.getProperty("db.vendor", ConfigReader.getProperty("db.vendor"));
        if (targetDbVendor == null || targetDbVendor.trim().isEmpty()) {
            targetDbVendor = "h2";
        }

        log.info("--- [GLOBAL HOOK] Initializing Database Infrastructure for Vendor: [{}] ---", targetDbVendor.toUpperCase());
        try {
            DbContainerFactory.startContainer(targetDbVendor);
        } catch (Exception e) {
            log.error("Failed to initialize database infrastructure for vendor [{}]. Error: {}", targetDbVendor, e.getMessage());
            throw new RuntimeException("Database Container initialization failed", e);
        }

        // 2. Global Playwright & Browser Process Initialization
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", ConfigReader.getProperty("browser.headless")));
        String browserChannel = ConfigReader.getProperty("browser.channel");

        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless);

        if (browserChannel != null && !browserChannel.trim().isEmpty()) {
            launchOptions.setChannel(browserChannel);
        }

        if (!isHeadless) {
            launchOptions.setArgs(Arrays.asList("--start-maximized"));
        }

        browser = playwright.chromium().launch(launchOptions);
        log.info("--- [GLOBAL HOOK] Playwright Browser Engine Started Successfully ---");
    }

    @BeforeMethod(alwaysRun = true)
    public void setupInfrastructure() {
        // Resolve target runtime environment
        String systemEnv = System.getProperty("env");
        String activeEnv = (systemEnv != null && !systemEnv.trim().isEmpty())
                ? systemEnv.toLowerCase().trim()
                : ConfigReader.getProperty("default.environment").toLowerCase().trim();

        threadEnvironment.set(activeEnv);
        this.activeEnvironment = activeEnv;

        log.info("Initializing Thread Execution Context [Thread ID: {}] | Environment: [{}]",
                Thread.currentThread().getId(), activeEnv.toUpperCase());

        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", ConfigReader.getProperty("browser.headless")));
        double pageLoadTimeout = Double.parseDouble(ConfigReader.getProperty("timeout.page.load"));

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

        if (!isHeadless) {
            contextOptions.setViewportSize(null);
        } else {
            contextOptions.setViewportSize(1920, 1080);
        }

        // Instantiate thread-isolated BrowserContext and Page
        BrowserContext localContext = browser.newContext(contextOptions);
        Page localPage = localContext.newPage();
        localPage.setDefaultNavigationTimeout(pageLoadTimeout);

        // Store in ThreadLocal
        threadContext.set(localContext);
        threadPage.set(localPage);

        // Assign to instance fields for existing test classes
        this.context = localContext;
        this.page = localPage;
    }

    @AfterMethod(alwaysRun = true)
    public void teardownInfrastructure() {
        log.info("Tearing down Thread Execution Context [Thread ID: {}]", Thread.currentThread().getId());
        try {
            if (getPage() != null && !getPage().isClosed()) {
                getPage().close();
            }
        } catch (Exception e) {
            log.warn("Issue closing page on thread teardown: {}", e.getMessage());
        } finally {
            threadPage.remove();
            this.page = null;
        }

        try {
            if (getContext() != null) {
                getContext().close();
            }
        } catch (Exception e) {
            log.warn("Issue closing browser context on thread teardown: {}", e.getMessage());
        } finally {
            threadContext.remove();
            threadEnvironment.remove();
            this.context = null;
            this.activeEnvironment = null;
        }
    }

    @AfterSuite(alwaysRun = true)
    public void globalTeardownHook() {
        log.info("--- [GLOBAL HOOK] Shutting down Browser, Playwright Engine, and Database Instances ---");

        try {
            if (browser != null && browser.isConnected()) {
                browser.close();
                log.info("Playwright browser process closed successfully.");
            }
        } catch (Exception e) {
            log.warn("Issue closing browser during suite teardown: {}", e.getMessage());
        }

        try {
            if (playwright != null) {
                playwright.close();
                log.info("Playwright engine instance closed successfully.");
            }
        } catch (Exception e) {
            log.warn("Issue closing Playwright engine during suite teardown: {}", e.getMessage());
        }

        try {
            DbContainerFactory.stopContainer();
        } catch (Exception e) {
            log.warn("Issue stopping database container during suite teardown: {}", e.getMessage());
        }
    }

    // =========================================================================
    // GETTERS & UTILITY METHODS
    // =========================================================================

    public Page getPage() {
        return threadPage.get() != null ? threadPage.get() : page;
    }

    public BrowserContext getContext() {
        return threadContext.get() != null ? threadContext.get() : context;
    }

    public String getActiveEnvironment() {
        return threadEnvironment.get() != null ? threadEnvironment.get() : activeEnvironment;
    }

    public APIRequestContext getApiContext() {
        return getContext().request();
    }

    public String captureScreenshotAsBase64() {
        try {
            Page currentPage = getPage();
            if (currentPage != null && !currentPage.isClosed()) {
                byte[] screenshotBytes = currentPage.screenshot(new Page.ScreenshotOptions().setFullPage(false));
                return Base64.getEncoder().encodeToString(screenshotBytes);
            }
        } catch (Exception e) {
            log.error("Failed to capture screenshot on thread [{}]: {}", Thread.currentThread().getId(), e.getMessage());
        }
        return "";
    }

    protected void navigateToApp(String appName) {
        String activeEnv = getActiveEnvironment();
        String configKey = activeEnv + "." + appName + ".targetAppUrl";
        String targetUrl = ConfigReader.getProperty(configKey);

        if (targetUrl == null || targetUrl.isEmpty()) {
            configKey = "local." + appName + ".targetAppUrl";
            targetUrl = ConfigReader.getProperty(configKey);
        }

        log.info("Navigating to App [{}] | Env [{}] | URL: {}", appName.toUpperCase(), activeEnv.toUpperCase(), targetUrl);
        getPage().navigate(targetUrl);
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    public String getBaseApiUrl(String appName) {
        String activeEnv = getActiveEnvironment();
        String configKey = activeEnv + "." + appName + ".targetApiUrl";
        String targetUrl = ConfigReader.getProperty(configKey);

        if (targetUrl == null || targetUrl.isEmpty()) {
            configKey = "local." + appName + ".targetApiUrl";
            targetUrl = ConfigReader.getProperty(configKey);
        }
        return targetUrl;
    }
}