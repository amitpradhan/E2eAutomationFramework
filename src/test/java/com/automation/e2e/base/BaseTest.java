package com.automation.e2e.base;

import com.automation.e2e.db.DbUtil;
import com.automation.e2e.utils.ConfigReader;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;
import java.lang.reflect.Method;
import java.sql.SQLException;

public class BaseTest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    // Core Reporting Engines
    protected static ExtentReports extent;
    // FIX: Must be marked as protected so child test classes can access it
    protected static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    protected final Logger log = LogManager.getLogger(this.getClass());

    @BeforeSuite
    public void setupGlobalInfrastructure() {
        log.info("Initializing Extent Reporting Engine...");
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/ExtentReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);

        log.info("Loading Dynamic Environment Properties...");
        String dbUrl = ConfigReader.getDbUrl();
        String dbUser = ConfigReader.getDbUser();
        String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "SA_Password_Local";

        // Wrap in a try-catch to allow execution even without local SQL running
        try {
            log.info("Attempting to connect to target Environment database...");
            DbUtil.initConnection(dbUrl, dbUser, dbPassword);
            log.info("DATABASE STATUS: Connected successfully.");
        } catch (Exception databaseException) {
            log.warn("DATABASE STATUS: Connection failed or skipped. Error: " + databaseException.getMessage());
            log.warn("Framework will continue running UI and API layers without an active DB pipeline.");
        }

        log.info("Launching Headless Playwright Browser Context...");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @BeforeMethod
    public void setupTestContext(Method method) {
        ExtentTest test = extent.createTest(method.getName() + " [" + ConfigReader.getEnv().toUpperCase() + "]");
        extentTest.set(test);

        log.info("Starting Thread-Isolated Test Execution View: " + method.getName());
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterMethod
    public void evaluateTestOutcome(ITestResult result) {
        if (result.getStatus() == ITestResult.SUCCESS) {
            extentTest.get().pass("Test workflow executed and passed completely.");
            log.info("SUCCESS: Test Script completed flawlessly -> " + result.getName());
        } else if (result.getStatus() == ITestResult.FAILURE) {
            extentTest.get().fail(result.getThrowable());
            log.error("FAILURE: Exception raised on loop context -> " + result.getName(), result.getThrowable());

            try {
                byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                String base64Screenshot = java.util.Base64.getEncoder().encodeToString(screenshotBytes);
                extentTest.get().addScreenCaptureFromBase64String(base64Screenshot, "Failure Diagnostic State");
            } catch (Exception e) {
                log.warn("Unable to append view screenshot to Extent Report: " + e.getMessage());
            }
        } else if (result.getStatus() == ITestResult.SKIP) {
            extentTest.get().skip("Test logic skipped during setup initialization.");
            log.warn("SKIPPED: Target profile initialization deferred -> " + result.getName());
        }
        context.close();
    }

    @AfterSuite
    public void teardownGlobalInfrastructure() throws SQLException {
        log.info("Flushing Execution Report matrices to reports/ directory...");
        extent.flush();

        log.info("Severing Database Connection pipelines cleanly...");
        DbUtil.closeConnection();

        log.info("Shutting down core Playwright Browser processes...");
        browser.close();
        playwright.close();
    }

    // Helper wrapper to post logs directly to Log4j2 and Extent Reports simultaneously
    protected void logStep(String message) {
        log.info(message);
        if (extentTest.get() != null) {
            extentTest.get().info(message);
        }
    }
}