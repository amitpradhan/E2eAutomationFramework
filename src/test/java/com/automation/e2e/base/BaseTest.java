package com.automation.e2e.base;

import com.automation.e2e.db.DbUtil;
import com.automation.e2e.utils.ConfigReader;
import com.microsoft.playwright.*;
import org.testng.annotations.*;
import java.sql.SQLException;

public class BaseTest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeSuite
    public void setupGlobalInfrastructure() throws SQLException {
        // Fetch properties from memory map fields directly
        String dbUrl = ConfigReader.getDbUrl();
        String dbUser = ConfigReader.getDbUser();
        String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "your_fallback_pwd";

        DbUtil.initConnection(dbUrl, dbUser, dbPassword);

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }
    // Helper wrapper to post logs directly to Log4j2 and Extent Reports simultaneously
//    protected void logStep(String message) {
//        log.info(message); // Prints to console and logs/automation_run.log
//        if (extentTest.get() != null) {
//            extentTest.get().info(message); // Appends steps to ExtentReport.html
//        }
//    }
    // ... rest of your @BeforeMethod, @AfterMethod, and @AfterSuite blocks ...
}