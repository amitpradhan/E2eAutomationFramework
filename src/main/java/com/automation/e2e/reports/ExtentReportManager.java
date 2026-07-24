package com.automation.e2e.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.automation.e2e.utils.ConfigReader;

import java.io.File;

public class ExtentReportManager {

    private static ExtentReports extent;

    public synchronized static ExtentReports getInstance() {
        if (extent == null) {
            String reportDir = ConfigReader.getProperty("report.output.directory");
            String reportName = ConfigReader.getProperty("report.file.name");
            String reportPath = reportDir + File.separator + reportName;

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle(ConfigReader.getProperty("report.document.title"));
            sparkReporter.config().setReportName("Automated Test Execution Summary Report");
            sparkReporter.config().setTheme(Theme.DARK);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // Dynamic Environment & System Details
            String activeEnv = System.getProperty("env", ConfigReader.getProperty("default.environment")).toUpperCase();
            String browserChannel = ConfigReader.getProperty("browser.channel");

            extent.setSystemInfo("Target Environment", activeEnv);
            extent.setSystemInfo("Browser Engine", browserChannel.toUpperCase());
            extent.setSystemInfo("Operating System", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("Executed By", System.getProperty("user.name"));
        }
        return extent;
    }
}