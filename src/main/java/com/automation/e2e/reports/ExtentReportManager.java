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
            String reportDir = ConfigReader.getProperty("report.output.directory"); // e.g., "target/reports/"
            String reportName = ConfigReader.getProperty("report.file.name");

            // 1. Ensure output directory exists before creating reporter
            File directory = new File(reportDir);
            if (!directory.exists()) {
                directory.mkdirs(); // Creates target/reports/ directory tree dynamically
            }

            String reportPath = reportDir + File.separator + reportName;

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle(ConfigReader.getProperty("report.document.title"));
            sparkReporter.config().setReportName("Automated Test Execution Summary Report");
            sparkReporter.config().setTheme(Theme.DARK);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // Dynamic System Metadata
            String activeEnv = System.getProperty("env", ConfigReader.getProperty("default.environment")).toUpperCase();
            extent.setSystemInfo("Target Environment", activeEnv);
            extent.setSystemInfo("Browser Engine", ConfigReader.getProperty("browser.channel").toUpperCase());
            extent.setSystemInfo("Operating System", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));

            // 2. JVM Shutdown Hook guarantees disk-write on process exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (extent != null) {
                    extent.flush();
                }
            }));
        }
        return extent;
    }

    public synchronized static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}