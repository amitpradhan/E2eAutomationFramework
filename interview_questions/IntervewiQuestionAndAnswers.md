# Top 100 Automation Framework Interview Questions & Answers

This document contains a comprehensive bank of 100 technical interview questions covering Framework Architecture, Playwright Java, Testcontainers, TestNG, and CI/CD pipelines.

---

## Phase 1: Core Framework Architecture & Design Patterns (Q1–Q20)

**1. What design pattern does the framework use to manage browser lifecycle across multiple test classes?**  
The framework uses the **ThreadLocal Pattern** combined with the **Singleton/Factory Pattern** inside `Base.java`. ThreadLocal isolates `BrowserContext` and `Page` instances per execution thread, ensuring safe, conflict-free parallel execution.

**2. How does `ConfigReader.java` resolve configuration properties across environments?**  
It uses a 4-tier resolution hierarchy:
1. JVM System Properties (`-Denv`, `-Ddb.vendor`)
2. Local Dotenv (`.env`)
3. OS Environment Variables (CI/CD Pipeline Secrets)
4. Static `config.properties` file.

**3. Why use `ThreadLocal<Page>` instead of a global `static Page page` variable?**  
A simple `static Page page` variable causes thread collisions during parallel execution because multiple threads overwrite the same page object. `ThreadLocal<Page>` assigns a dedicated page instance to each thread.

**4. How is the Tri-Layer (UI, API, DB) architecture decoupled in this framework?**  
Each layer operates independently through specialized utilities:
* **UI Layer:** Playwright Page Objects handle DOM interactions.
* **API Layer:** Playwright `APIRequestContext` handles REST calls directly.
* **DB Layer:** `DbUtil` manages JDBC connections.  
  Tests combine these layers in test methods without hardcoded dependencies between them.

**5. What is the role of `SqlCatalog.java` in the database architecture?**  
`SqlCatalog.java` centralizes all DDL (table creations) and DML (`INSERT`, `UPDATE`, `SELECT`) statements into `public static final String` constants. This eliminates inline SQL strings in test code and makes schema changes easy to maintain.

**6. How does the framework handle test data domain objects?**  
It uses strongly typed **Java Enums** (e.g., `TestUser.java`) and POJOs to encapsulate domain entity attributes (such as `username`, `email`, and `status`), avoiding raw string literal duplication across test methods.

**7. How are dynamic database connections initialized in `DbUtil`?**  
`DbUtil` dynamically inspects the JDBC URL string, registers the appropriate database driver class at runtime using `Class.forName()`, and creates connections using `DriverManager.getConnection()`.

**8. What is the role of TestNG Listeners in this framework?**  
`TestListener.java` implements `ITestListener`. It hooks into test execution events (`onTestStart`, `onTestSuccess`, `onTestFailure`) to log execution status, update Extent Reports, and attach screenshots when UI assertions fail.

**9. How do you prevent sensitive credentials from being committed to Git repository?**  
By using the **Dotenv (`java-dotenv`)** library. Sensitive keys are stored in a local `.env` file that is ignored by Git via `.gitignore`. In CI/CD pipelines, credentials are injected as GitHub Secrets / OS Environment Variables.

**10. How does the framework handle cross-platform execution (e.g., Windows vs. Linux CI runners)?**
* Paths use forward slashes or standard Java file resolvers.
* CLI properties in documentation highlight Windows PowerShell quotation syntax (`"-Dproperty=value"`) to prevent symbol-parsing errors.
* Playwright binaries and Docker containers run natively across OS platforms.

**11. Why did we wrap `DbContainerFactory` to return `null` when `db.vendor=h2` is selected?**  
H2 operates entirely in-memory within the JVM process. Returning `null` bypasses Docker container instantiation while still setting system properties (`db.url`, `db.user`, `db.password`) expected by `DbUtil`.

**12. What happens if an invalid `db.vendor` flag is passed to the framework?**  
`DbContainerFactory` throws an `UnsupportedOperationException`, stopping execution with a clear error message indicating supported database keys (`h2`, `mysql`, `postgres`, `sqlserver`, `oracle`).

**13. How are Extent Reports configured for parallel test runs?**  
Extent Test nodes are stored inside a `ThreadLocal<ExtentTest>` wrapper. This ensures each parallel test logs steps exclusively to its own report branch.

**14. What is the advantage of using Maven Surefire profiles / XML suite properties?**  
It allows parameterization of test execution from the CLI (`mvn test "-DsuiteXmlFile=testng-db.xml"`), enabling targeted suite execution without re-compiling source code.

**15. How does `Base.java` ensure Playwright resources are cleaned up even when tests fail?**  
Teardown methods use `@AfterClass(alwaysRun = true)` and `@AfterSuite(alwaysRun = true)` annotations, wrapped in `try-catch-finally` blocks to guarantee cleanup regardless of assertion status.

**16. What design pattern is used by Playwright's `APIRequestContext`?**  
The **Builder Pattern** and **Fluent Interface Pattern**, which allow chaining headers, query parameters, and request bodies cleanly.

**17. How does the framework maintain backward compatibility for local developers without Docker installed?**  
By making `h2` the default database vendor in `config.properties`. Local tests execute against an in-memory DB without requiring Docker Desktop.

**18. What is the purpose of `.env.example` in the source repository?**  
It serves as a clean template containing key names without real values, giving new developers a clear guide on required environment variables.

**19. How do you pass runtime flags dynamically through IntelliJ run configurations?**  
By navigating to **Edit Configurations -> VM Options** and passing system properties using `-D` flags (e.g., `-Denv=sit -Ddb.vendor=h2`).

**20. What is the advantage of using Java 21 for this automation framework?**  
Java 21 brings optimized virtual threads (Project Loom), improved pattern matching, record classes for immutability, and better memory management, leading to faster execution times.

---

## Phase 2: Playwright Java Core & Advanced (Q21–Q45)

**21. What is the difference between `Browser`, `BrowserContext`, and `Page` in Playwright?**
* **`Browser`:** An instance of a browser process (Chromium, Firefox, or WebKit).
* **`BrowserContext`:** An isolated, in-memory session inside a Browser (equivalent to an Incognito profile).
* **`Page`:** A single tab or popup window within a `BrowserContext`.

**22. How does Playwright handle auto-waiting?**  
Playwright automatically performs actionability checks (visibility, stability, enablement, receiving events) before executing actions like `click()` or `fill()`. This eliminates the need for manual `Thread.sleep()` calls.

**23. How do you launch a browser in headful mode for local debugging in Playwright Java?**
```java
Playwright playwright = Playwright.create();
Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
```

**24. How do you handle multi-tab/popup windows in Playwright Java?**  
Use `BrowserContext.waitForPage()`:
```java
Page newPage = context.waitForPage(() -> {
    page.click("a[target='_blank']");
});
newPage.waitForLoadState();
```

**25. How do you capture network requests/responses using Playwright Java?**
```java
page.onRequest(request -> System.out.println(">> " + request.method() + " " + request.url()));
page.onResponse(response -> System.out.println("<< " + response.status() + " " + response.url()));
```

**26. What is the default timeout in Playwright for actions, and how do you customize it?**  
The default action timeout is **30 seconds** (0 means no timeout). Customize it globally or per action:
```java
page.setDefaultTimeout(10000); // Global 10s
page.click("button#submit", new Page.ClickOptions().setTimeout(5000)); // Action specific
```

**27. How do you emulate a mobile device in Playwright Java?**
```java
BrowserContext context = browser.newContext(new Browser.NewContextOptions()
    .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)...")
    .setViewportSize(390, 844)
    .setDeviceScaleFactor(3)
    .setIsMobile(true)
    .setHasTouch(true));
```

**28. How do you upload files using Playwright Java?**  
Use `setInputFiles()`:
```java
page.setInputFiles("input[type='file']", Paths.get("src/test/resources/sample.pdf"));
```

**29. How do you download files using Playwright Java?**
```java
Download download = page.waitForDownload(() -> {
    page.click("a#download-report");
});
download.saveAs(Paths.get("downloads/" + download.suggestedFilename()));
```

**30. What locator strategies are recommended by Playwright?**  
User-facing locators are preferred:
* `page.getByRole()`
* `page.getByText()`
* `page.getByLabel()`
* `page.getByTestId()`

**31. How do you handle Shadow DOM elements in Playwright?**  
Playwright pierces Shadow DOM boundaries by default. Standard CSS selectors work directly through shadow roots without custom piercing logic.

**32. How do you handle HTTP Authentication dialogs in Playwright?**  
Set credentials when creating the `BrowserContext`:
```java
BrowserContext context = browser.newContext(new Browser.NewContextOptions()
    .setHttpCredentials("admin", "Secret123"));
```

**33. How do you record a video of test execution in Playwright?**
```java
BrowserContext context = browser.newContext(new Browser.NewContextOptions()
    .setRecordVideoDir(Paths.get("videos/")));
```

**34. What is `storageState` in Playwright and how does it speed up execution?**  
`storageState` saves authenticated cookies and local storage to a JSON file. Subsequent test suites load this JSON into new contexts to skip login UI steps.

**35. How do you execute custom JavaScript inside the browser context?**
```java
String userAgent = (String) page.evaluate("navigator.userAgent");
page.evaluate("document.querySelector('header').style.display='none'");
```

**36. How do you perform API testing using Playwright Java?**
```java
APIRequestContext apiContext = playwright.request().newContext();
APIResponse response = apiContext.get("[https://api.example.com/bills](https://api.example.com/bills)");
int statusCode = response.status();
String responseBody = response.text();
```

**37. How do you bypass CORS errors or SSL certificate issues in Playwright?**
```java
BrowserContext context = browser.newContext(new Browser.NewContextOptions()
    .setIgnoreHTTPSErrors(true));
```

**38. How do you handle iframe elements in Playwright?**  
Use `frameLocator()`:
```java
FrameLocator frame = page.frameLocator("iframe#payment-frame");
frame.locator("input#card-number").fill("4111111111111111");
```

**39. How do you handle JavaScript `alert()`, `confirm()`, or `prompt()` dialogs?**  
Playwright automatically dismisses dialogs by default. To accept or inspect them:
```java
page.onDialog(dialog -> {
    System.out.println(dialog.message());
    dialog.accept("Input Text");
});
```

**40. What is the difference between `locator.click()` and `locator.dispatchEvent("click")`?**
* `click()` simulates a real user action (performs actionability checks, moves mouse, fires events).
* `dispatchEvent("click")` directly triggers the DOM click event bypassing actionability checks.

**41. How do you capture full-page screenshots in Playwright Java?**
```java
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("screenshot.png"))
    .setFullPage(true));
```

**42. How do you mock or block network requests in Playwright?**
```java
// Block images to speed up page loads
page.route("**/*.{png,jpg,jpeg}", route -> route.abort());

// Mock API response
page.route("**/api/v1/bills", route -> route.fulfill(new Route.FulfillOptions()
    .setStatus(200)
    .setBody("{\"status\":\"success\"}")));
```

**43. How do you perform drag-and-drop actions?**
```java
page.dragAndDrop("#source-element", "#target-element");
```

**44. What is the difference between `page.waitForSelector()` and `Locator.waitFor()`?**  
`page.waitForSelector()` is an older, lower-level API. `Locator.waitFor()` is preferred because it works seamlessly with Playwright's auto-retrying locator engine.

**45. How do you handle slow-loading elements without using static timeouts?**  
Use explicit assertion retries:
```java
assertThat(page.locator("#status")).hasText("Completed", new LocatorAssertions.HasTextOptions().setTimeout(15000));
```

---

## Phase 3: Testcontainers & Multi-Database Automation (Q46–Q70)

**46. What is Testcontainers and why use it for test automation?**  
Testcontainers is a Java library that uses Docker to instantiate lightweight, throwaway databases or services during test execution. It guarantees clean, isolated database instances without relying on shared, static external servers.

**47. How does `DbContainerFactory` spin up different database containers dynamically?**  
It receives a `dbType` string (`mysql`, `postgres`, `sqlserver`, `oracle`) and uses a Java `switch` block to instantiate the corresponding Testcontainers class (`MySQLContainer`, `PostgreSQLContainer`, `MSSQLServerContainer`, `OracleContainer`).

**48. Why is generic type parameterization (`MySQLContainer<?>`) necessary when initializing containers?**  
In Testcontainers, concrete classes inherit self-referential generic types (`MySQLContainer<SELF MySQLContainer<SELF extends>>`). Using wildcards (`MySQLContainer<?>`) prevents raw type compilation warnings and type assignment errors.

**49. How does `DbContainerFactory` pass connection details to `DbUtil`?**  
After starting the container (`activeContainer.start()`), it extracts dynamic parameters and sets JVM system properties:
```java
System.setProperty("db.url", activeContainer.getJdbcUrl());
System.setProperty("db.user", activeContainer.getUsername());
System.setProperty("db.password", activeContainer.getPassword());
```

**50. What is H2 database and why is it included alongside Testcontainers?**  
H2 is an in-memory Java database engine. It runs inside the JVM without Docker, allowing local developer runs without external dependencies.

**51. How does H2 emulate MySQL syntax in our framework?**  
Through connection string parameterization:
`jdbc:h2:mem:e2e_testdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`

**52. How does `DbContainerFactory` distinguish between H2 and Testcontainers?**  
When `dbType` is `"h2"`, `DbContainerFactory` configures H2 system properties and returns `null` instead of instantiating a Docker container.

**53. How do you prevent table lock issues when multiple test threads access H2?**  
Include `DB_CLOSE_DELAY=-1` in the connection string to keep the in-memory database alive across thread connections during the JVM test session.

**54. What causes `java.lang.IllegalStateException: Could not find a valid Docker environment`?**  
This occurs when Testcontainers cannot locate an active Docker daemon. Causes include Docker Desktop being turned off, missing WSL2 integration, or missing environment variables (`DOCKER_HOST`).

**55. How do you run Testcontainers on a machine with Podman instead of Docker Desktop?**  
Set the `DOCKER_HOST` environment variable to point to the Podman named pipe or socket:
`$env:DOCKER_HOST="npipe:////./pipe/podman-machine-default"`

**56. How do you handle schema creation and table initialization in Testcontainers?**
* Pass `.withInitScript("schema.sql")` during container creation, or
* Execute DDL queries from `SqlCatalog.java` during `@BeforeSuite` / test setup hooks.

**57. What is Ryuk in Testcontainers?**  
Ryuk is a background sidecar container spawned by Testcontainers (`quay.io/testcontainers/ryuk`). It monitors the test process and cleans up orphaned containers and volumes if the JVM crashes unexpectedly.

**58. How do you disable Ryuk if running in restricted CI environments?**  
Set the environment variable `TESTCONTAINERS_RYUK_DISABLED=true`.

**59. What is the difference between `@Container` annotation usage and manual lifecycle management?**
* **`@Container` with JUnit/TestNG extensions:** Automatically manages container start/stop tied to class/test life cycles.
* **Manual Lifecycle (`startContainer()` / `stopContainer()`):** Provides full programmatic control over container lifecycle across custom suite structures.

**60. How does `MERGE INTO` in H2 compare to MySQL's `INSERT INTO ... ON DUPLICATE KEY UPDATE`?**  
`MERGE INTO` is standard SQL supported natively by H2 (and Oracle) to perform upsert operations based on primary key matching.

**61. Why did we use `OracleContainer` from the `oracle-xe` module?**  
The `oracle-xe` Testcontainers module provides pre-configured, lightweight Oracle Express Edition instances without manual image authentication.

**62. How do you optimize Docker image pull times in CI/CD when using Testcontainers?**
* Use alpine or slim image tags (e.g., `postgres:16-alpine`, `oracle-free:23-slim-faststart`).
* Enable Docker layer caching in your CI runner pipeline.

**63. How do you inspect database state inside a running Testcontainer during debugging?**  
Log `activeContainer.getJdbcUrl()` and pause execution with a breakpoint, or bind fixed host ports using `.withExposedPorts()` to connect an external GUI tool (like DBeaver).

**64. How does `DbUtil` prevent database connection leaks?**  
`DbUtil` uses **try-with-resources** blocks for `Connection`, `PreparedStatement`, and `ResultSet`, ensuring JDBC resources close automatically even when exceptions occur.

**65. What JDBC driver is required for SQL Server Testcontainers?**  
The Microsoft JDBC Driver for SQL Server (`com.microsoft.sqlserver:mssql-jdbc`).

**66. How do you accept the Microsoft SQL Server EULA in Testcontainers?**  
Call `.acceptLicense()` on the `MSSQLServerContainer` builder instance.

**67. What is the difference between `getRowCount()` and executing `SELECT COUNT(*)` manually in `DbUtil`?**  
`DbUtil.getRowCount()` encapsulates boilerplate `PreparedStatement` setup, parameter binding, and `ResultSet` parsing into a single utility method call.

**68. Why is `Class.forName()` needed inside `DbUtil` driver registration?**  
In legacy/dynamic JDBC setups, `Class.forName()` explicitly loads the driver class into the JVM's `DriverManager`, ensuring correct driver registration before creating connections.

**69. How do you pass custom database configuration parameters to a Testcontainers instance?**  
Use `.withCommand()` or `.withUrlParam()` methods (e.g., `.withUrlParam("useSSL", "false")`).

**70. How do you execute containerized tests against multiple database vendors sequentially in TestNG?**  
Define separate test blocks in `testng-db.xml` or use a TestNG `@DataProvider` to pass database keys (`mysql`, `postgres`, `sqlserver`) into `DbContainerFactory.startContainer()`.

---

## Phase 4: TestNG, Assertions & Reporting (Q71–Q85)

**71. What is the difference between `@BeforeSuite`, `@BeforeClass`, and `@BeforeMethod` in TestNG?**
* `@BeforeSuite`: Runs once before all tests in the suite.
* `@BeforeClass`: Runs once before the first test method in the current class.
* `@BeforeMethod`: Runs before each test method.

**72. What is the difference between Hard Assertions and Soft Assertions in TestNG?**
* **Hard Assertion (`Assert.assertEquals`):** Instantly halts execution of the test method upon failure.
* **Soft Assertion (`SoftAssert`):** Logs the failure and continues executing the remaining steps until `assertAll()` is called.

**73. How do you configure parallel execution in `testng.xml`?**
```xml
<suite name="Automation Suite" parallel="methods" thread-count="4">
```

**74. How do you skip or disable a test in TestNG?**  
Set `enabled = false` on the `@Test` annotation: `@Test(enabled = false)`

**75. How do you set test dependency in TestNG?**
```java
@Test(dependsOnMethods = {"testLogin"})
public void testDashboard() { ... }
```

**76. How do you parameterize tests in TestNG using `testng.xml`?**
```xml
<test name="LoginTest">
    <parameter name="browser" value="firefox"/>
    <classes>
        <class name="com.automation.e2e.tests.LoginTest"/>
    </classes>
</test>
```
In Java:
```java
@Parameters({"browser"})
@BeforeMethod
public void setup(String browser) { ... }
```

**77. How does `@DataProvider` work in TestNG?**  
A `@DataProvider` returns a 2D array (`Object[][]`) or `Iterator<Object[]>` containing test data sets:
```java
@DataProvider(name = "userData")
public Object[][] getUserData() {
    return new Object[][] { {"user1", "pass1"}, {"user2", "pass2"} };
}

@Test(dataProvider = "userData")
public void testUsers(String user, String pass) { ... }
```

**78. What is the role of `ITestContext` in TestNG?**  
`ITestContext` stores metadata about the test execution context (such as suite name, output directory, passed/failed tests), allowing data sharing between execution hooks.

**79. How do you re-run failed tests automatically in TestNG?**  
Implement `IRetryAnalyzer`:
```java
public class RetryAnalyzer implements IRetryAnalyzer {
    private int count = 0;
    private static final int maxCount = 2;
    @Override
    public boolean retry(ITestResult result) {
        if (count < maxCount) {
            count++;
            return true;
        }
        return false;
    }
}
```

**80. How do you pass system properties to Maven Surefire plugin?**  
Pass `-D` flags on the command line, or configure `<systemPropertyVariables>` in `pom.xml`.

**81. How does ExtentReports attach screenshots on test failure in Playwright?**
1. Capture screenshot as a byte array or file path using `page.screenshot()`.
2. Convert to Base64 string.
3. Attach to Extent node: `ExtentTest.fail("Test Failed", MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());`

**82. What is the purpose of `alwaysRun = true` in `@AfterMethod` or `@AfterClass`?**  
It guarantees execution of teardown methods even if preceding setup hooks or test assertions fail.

**83. How do you group tests in TestNG?**
```java
@Test(groups = {"smoke", "regression"})
public void testLogin() { ... }
```
Execute in `testng.xml`:
```xml
<groups>
    <run>
        <include name="smoke"/>
    </run>
</groups>
```

**84. What is the difference between `SuiteRunner` and `TestRunner` in TestNG?**
* `SuiteRunner`: Represents the global `<suite>` tag execution.
* `TestRunner`: Represents an individual `<test>` tag block inside the suite.

**85. How do you control test execution order in TestNG without dependencies?**  
Use the `priority` attribute: `@Test(priority = 1)`

---

## Phase 5: CI/CD Pipeline & GitHub Actions Automation (Q86–Q100)

**86. What is the purpose of `.github/workflows/e2e-pipeline.yml`?**  
It defines the automated CI/CD pipeline configuration for GitHub Actions, including trigger rules (`push`, `pull_request`, `workflow_dispatch`), environment setup, test execution, and artifact archiving.

**87. What trigger events are configured in our GitHub Actions pipeline?**
* `push`: Triggers build on code pushed to `main`/`master`.
* `pull_request`: Triggers build when a PR is opened against `main`/`master`.
* `workflow_dispatch`: Enables manual pipeline execution from GitHub UI with customizable dropdown parameters.

**88. How does `workflow_dispatch` enable dynamic database vendor selection in CI?**  
By defining input options under `workflow_dispatch.inputs.db_vendor`, allowing users to select `h2`, `mysql`, `postgres`, or `sqlserver` before manually launching the pipeline.

**89. Why do we cache Maven dependencies in GitHub Actions (`actions/setup-java@v4`)?**  
Caching `~/.m2/repository` prevents downloading dependencies on every build, reducing pipeline run times.

**90. How do you cache Playwright browser binaries in GitHub Actions?**  
Use `actions/cache@v4` targeting `~/.cache/ms-playwright` keyed by the OS runner and `pom.xml` hash.

**91. Why is `npx playwright install --with-deps` necessary in Linux CI runners?**  
Linux runner images lack native browser rendering dependencies (like GTK, Fontconfig, and NSS libraries). `--with-deps` installs required OS-level system packages.

**92. How are secret passwords passed securely to the Maven execution step in GitHub Actions?**  
Secrets are defined under `secrets.TC_MYSQL_PASSWORD` and mapped to job environment variables:
```yaml
env:
  TC_MYSQL_PASSWORD: ${{ secrets.TC_MYSQL_PASSWORD }}
```

**93. What is the purpose of `if: always()` in GitHub Actions workflow steps?**  
It ensures that step executes regardless of whether previous steps succeeded or failed (essential for uploading test reports on test failures).

**94. How does `actions/upload-artifact@v4` preserve test reports?**  
It zips the specified output directories (`target/surefire-reports/`, `reports/`) and attaches them to the pipeline summary view as downloadable zip archives.

**95. How does Testcontainers work inside standard GitHub Actions `ubuntu-latest` runners?**  
`ubuntu-latest` runners come with Docker pre-installed. Testcontainers automatically detects `/var/run/docker.sock` and starts container instances without extra setup.

**96. What is the difference between `mvn test` and `mvn verify` in CI pipelines?**
* `mvn test`: Runs unit and component tests through Maven Surefire plugin.
* `mvn verify`: Runs integration tests through Maven Failsafe plugin and runs post-integration checks.

**97. How do you run tests in headless mode in CI/CD pipelines?**  
Playwright runs headless by default unless `.setHeadless(false)` is explicitly called. In CI pipelines, headless mode avoids requiring a virtual frame buffer (Xvfb).

**98. How do you debug a failing test inside a GitHub Actions pipeline runner?**
* Enable step debug logging by adding repository secret `ACTIONS_STEP_DEBUG = true`.
* Upload Playwright trace files (`trace.zip`) or screenshots as workflow build artifacts.
* Use SSH debugging tools like `mxschmitt/action-tmate` to inspect the live runner environment.

**99. How do you prevent pipeline builds from hanging indefinitely?**  
Set a step or job level timeout:
```yaml
jobs:
  test:
    timeout-minutes: 30
```

**100. How does the pipeline switch database execution dynamically between automatic PR pushes and manual runs?**  
Using bash parameter expansion:
`DB_VENDOR="${{ github.event.inputs.db_vendor || 'h2' }}"`  
This defaults to `h2` during automated PR commits (avoiding Docker overhead) while respecting manual dropdown choices during custom manual runs.
```