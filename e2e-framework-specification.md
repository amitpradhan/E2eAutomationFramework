# End-to-End (E2E) Test Automation Framework Specification

This comprehensive document details the architecture, directory layout, core component implementations, and validation layers of the advanced E2E Test Automation Framework. Built on **Java 21**, **Playwright Java**, **TestNG**, **ExtentReports**, and **Maven**, this framework integrates UI automation via the **Page Object Model (POM)** pattern, API interactions, dynamic database assertions, centralized thread-safe reporting with Base64 embedded screenshots, and multi-environment pipeline execution.

---

## 1. Architectural Design & Layout

The framework operates on a strict multi-tiered structure separating test workflows, ThreadLocal fixture engines, database access, UI wrappers, logging contexts, and reporting mechanisms.

### Complete Directory Layout

```text
E2eAutomationFramework/
│
├── .github/
│   └── workflows/
│       └── daily-e2e-run.yml          # GitHub Actions daily CI/CD scheduling engine
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/automation/e2e/
│   │   │       ├── api/               # API Engine & Endpoint Client Layers
│   │   │       │   └── ApiClient.java
│   │   │       │
│   │   │       ├── base/              # ThreadLocal Fixture Manager (@BeforeSuite, @BeforeMethod)
│   │   │       │   └── Base.java
│   │   │       │
│   │   │       ├── db/                # Database Helpers (Connections, Row Counts, Blob Fetching)
│   │   │       │   └── DbUtil.java
│   │   │       │
│   │   │       ├── listeners/         # TestNG Listener & ThreadLocal Extent Logging
│   │   │       │   └── TestListener.java
│   │   │       │
│   │   │       ├── reports/           # ExtentReports Spark setup with JVM Shutdown Hooks
│   │   │       │   └── ExtentReportManager.java
│   │   │       │
│   │   │       ├── ui/                # Page Object Model (POM) Layers
│   │   │       │   ├── saucedemo/     # SauceDemo Shopify Module
│   │   │       │   │   ├── SaucedemoHomePage.java
│   │   │       │   │   ├── SaucedemoProductPage.java
│   │   │       │   │   └── SaucedemoCartPage.java
│   │   │       │   └── gk/            # E2E Practice Workflows Module
│   │   │       │       ├── DashboardPage.java
│   │   │       │       └── ScenariosFormPage.java
│   │   │       │
│   │   │       ├── utils/             # Utilities, Property Readers, Excel, & UI Helpers
│   │   │       │   ├── ConfigReader.java
│   │   │       │   ├── ExcelUtil.java
│   │   │       │   ├── JsonXmlUtil.java
│   │   │       │   └── UiActionsUtil.java  # Centralized auto-waits & Playwright action wrappers
│   │   │       │
│   │   │       └── validators/        # Centralized Assert Engines (Deep JSON/XML Assertions)
│   │   │           └── ValidationEngine.java
│   │   │
│   │   └── resources/
│   │       ├── config.properties      # Centralized Environment URLs, DB Strings, & Options
│   │       └── log4j2.xml             # Log4j2 Appenders & File logging configurations
│   │
│   └── test/
│       ├── java/
│       │   └── com/automation/e2e/tests/
│       │       ├── saucedemo/         # SauceDemo E2E Regression Workflows
│       │       │   ├── SaucedemoShopifyTest.java
│       │       │   └── SaucedemoMultiItemCartTest.java
│       │       └── gk/                # UI Form Field Workflows
│       │           └── FormFieldsWorkflowTest.java
│       │
│       └── resources/                 # Test Data Sheets, Payload Templates & Mock Assets
│           ├── testdata/
│           │   └── TestData.xlsx
│           └── payloads/
│               ├── request_template.json
│               └── request_template.xml
│
├── testng.xml                         # Test Suite Runner and parallelization profiles
└── pom.xml                            # Dependencies (Playwright, TestNG, ExtentReports, POI, Jackson)