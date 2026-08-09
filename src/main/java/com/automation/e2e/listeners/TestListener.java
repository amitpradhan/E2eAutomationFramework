package com.automation.e2e.listeners;

import com.automation.e2e.base.Base;
import com.automation.e2e.reports.ExtentReportManager;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
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

    public static ExtentTest getTest() {
        return testThread.get();
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Starting Execution Context: {}", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        String packageName = result.getTestClass().getRealClass().getPackageName();
        String appName = packageName.contains(".")
                ? packageName.substring(packageName.lastIndexOf('.') + 1).toUpperCase()
                : "GENERAL";

        ExtentTest test = extent.createTest(className + " :: " + methodName, result.getMethod().getDescription());
        test.assignCategory("APP: " + appName);

        Object currentClass = result.getInstance();
        if (currentClass instanceof Base) {
            String activeEnv = ((Base) currentClass).getActiveEnvironment().toUpperCase();
            test.assignCategory("ENV: " + activeEnv);
            log.info("[TEST START] [{}] [{}] {}.{}()", activeEnv, appName, className, methodName);
        }

        testThread.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("[TEST PASSED] {}", result.getMethod().getMethodName());

        // Optionally attach a success screenshot of the final page
        attachScreenshotFromTestResult(result, "Final Page State");

        testThread.get().log(Status.PASS, "Test Executed Successfully.");
        ExtentReportManager.flushReport();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("[TEST FAILED] {}", result.getMethod().getMethodName(), result.getThrowable());

        // Attach screenshot on failure
        attachScreenshotFromTestResult(result, "Failure State Screenshot");

        testThread.get().log(Status.FAIL, result.getThrowable());
        ExtentReportManager.flushReport();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("[TEST SKIPPED] {}", result.getMethod().getMethodName());
        testThread.get().log(Status.SKIP, "Test Execution Skipped: " + result.getThrowable());
        ExtentReportManager.flushReport();
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Finished Execution Context: {}", context.getName());
        ExtentReportManager.flushReport();
    }

    private void attachScreenshotFromTestResult(ITestResult result, String title) {
        Object currentClass = result.getInstance();
        if (currentClass instanceof Base) {
            Base baseTest = (Base) currentClass;
            String base64Image = baseTest.captureScreenshotAsBase64();
            if (!base64Image.isEmpty()) {
                testThread.get().info(title,
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image).build());
            }
        }
    }
}