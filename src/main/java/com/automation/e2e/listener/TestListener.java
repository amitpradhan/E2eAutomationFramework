package com.automation.e2e.listener;

import com.automation.e2e.base.Base;
import com.automation.e2e.reports.ExtentReportManager;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);
    private static final ExtentReports extent = ExtentReportManager.getInstance();
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        log.info("==========================================================================");
        log.info("Starting Execution Suite: " + context.getName());
        log.info("==========================================================================");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        // Extract Application Name from package structure (e.g., com.automation.e2e.tests.gk -> APP: GK)
        String packageName = result.getTestClass().getRealClass().getPackageName();
        String appName = packageName.contains(".")
                ? packageName.substring(packageName.lastIndexOf('.') + 1).toUpperCase()
                : "GENERAL";

        ExtentTest test = extent.createTest(className + " :: " + methodName, result.getMethod().getDescription());

        // Tag reporting categories
        test.assignCategory("APP: " + appName);

        Object currentClass = result.getInstance();
        if (currentClass instanceof Base) {
            String activeEnv = ((Base) currentClass).getActiveEnvironment().toUpperCase();
            test.assignCategory("ENV: " + activeEnv);
            log.info("[TEST START] [{}] [{}] {}.{}()", activeEnv, appName, className, methodName);
        } else {
            log.info("[TEST START] [{}] {}.{}()", appName, className, methodName);
        }

        testThread.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("[TEST PASSED] " + result.getMethod().getMethodName());
        testThread.get().log(Status.PASS, "Test Executed Successfully.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("[TEST FAILED] " + result.getMethod().getMethodName(), result.getThrowable());
        testThread.get().log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("[TEST SKIPPED] " + result.getMethod().getMethodName());
        testThread.get().log(Status.SKIP, "Test Execution Skipped: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("==========================================================================");
        log.info("Execution Suite Finished: " + context.getName());
        log.info("==========================================================================");
        extent.flush();
    }
}