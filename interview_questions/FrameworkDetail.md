Markdown
## Understanding Our Framework Architecture (Simple Language Guide)

This framework is built like a **modular industrial assembly line**. Instead of putting all automation logic into one big script, every responsibility is split into specialized, reusable components.

---

### 1. Simple Overview of the Framework Layers

1. **UI Layer (Playwright):** Interacts with the web browser (clicking buttons, filling forms, verifying screens).
2. **API Layer (Playwright API Context):** Communicates directly with backend web services to fetch or create data rapidly without using the UI.
3. **Database Layer (JDBC / Testcontainers / H2):** Connects to databases to verify that data created via UI or API is correctly stored in backend tables.

---

### 2. Framework Components Explained in Detail with Examples

#### A. Base Setup (`Base.java`)
* **What it does:** The "brain" or engine supervisor of the framework. It handles setup before tests run and teardown after tests complete.
* **Why we use it:** To open browser instances, start databases, and ensure browser contexts are closed even if a test crashes.
* **Real Example:** Before running a test suite, `Base.java` checks if you requested an H2 database or MySQL container, starts it up, and opens a fresh Playwright browser page.

#### B. Dynamic Configuration Reader (`ConfigReader.java` & Dotenv `.env`)
* **What it does:** Centralized configuration routing that fetches environment URLs, ports, and database credentials without hardcoding them in code.
* **Why we use it:** To allow running the exact same test against SIT, UAT, or Local environments seamlessly, while keeping developer passwords safe.
* **Real Example:** If you pass `-Denv=sit`, `ConfigReader` fetches `https://sit.billing.com`. If you have a local `.env` file, it retrieves your secret DB password (`TC_MYSQL_PASSWORD=MyPass123!`).

#### C. Database Factory & Driver Utility (`DbContainerFactory.java` & `DbUtil.java`)
* **What it does:** A factory that spins up database environments on demand.
* **Why we use it:**
    * If you have Docker running, it creates a real container (MySQL, Postgres, SQL Server, Oracle).
    * If you don't have Docker, it runs a lightweight H2 in-memory database right inside Java.
* **Real Example:** Calling `DbContainerFactory.startContainer("h2")` spins up an in-memory MySQL-compatible database in milliseconds without needing Docker Desktop installed.

#### D. Central Query Catalog (`SqlCatalog.java`)
* **What it does:** A repository housing all SQL query strings.
* **Why we use it:** Keeps SQL queries out of test scripts. If a database column name changes, you update it in one central file instead of updating 50 test classes.
* **Real Example:**
  ```java
  public static final String SELECT_SYNCED_BILL = "SELECT bill_id, amount FROM synced_bills WHERE bill_id = ?";
E. Strongly-Typed Test Data Enums (TestUser.java)
What it does: Defines test user roles and domain data as structured Java Enums.

Why we use it: Avoids typos in raw strings like "amit@megamind360.com" across multiple files.

Real Example: Calling TestUser.AMIT.getEmail() returns "amit@megamind360.com" with auto-complete support in IntelliJ.

#### F. Reporting & Execution Listeners (`TestListener.java` & ExtentReports)
* **What it does:** Listens to test events (`Start`, `Pass`, `Fail`, `Skip`) and generates rich HTML execution reports.
* **Why we use it:** When a UI test fails, the listener automatically captures a browser screenshot and attaches it directly to the HTML report.

---

# Operational Framework Guide

---

## 1. System Architecture Overview

```text
                           +-----------------------------------+
                           |        TestNG Suite Runner        |
                           |   (testng.xml / testng-db.xml)    |
                           +-----------------+-----------------+
                                             |
                                  +----------v----------+
                                  |      Base.java      |
                                  |  (Lifecycle Hooks)  |
                                  +----------+----------+
                                             |
          +----------------------------------+----------------------------------+
          |                                  |                                  |
+---------v---------+              +---------v---------+              +---------v---------+
|     UI Layer      |              |    API Layer      |              |  Database Layer   |
| (Playwright Pages/|              | (Playwright API   |              |  (DbUtil / H2 /   |
|   Page Objects)   |              |   RequestCtx)     |              |  Testcontainers)  |
+-------------------+              +-------------------+              +---------+---------+
                                                                                |
                                                                       +--------v--------+
                                                                       |   SqlCatalog /  |
                                                                       | TestUser Enums  |
                                                                       +-----------------+
2. Environment Architecture & Dynamic Configuration (ConfigReader.java)
The environment management utility is maintained under src/main/java/com/automation/e2e/utils/ConfigReader.java. It processes configuration profiles completely in-memory utilizing an in-memory routing strategy driven by Java Virtual Machine (VM) System Properties (-Denv).

Configuration resolution follows a strict order of precedence:

[JVM -D Args] ---> [Dotenv (.env)] ---> [OS Env Vars (CI/CD)] ---> [config.properties]
Configuration resolution follows a strict order of precedence:

[JVM -D Args] ---> [Dotenv (.env)] ---> [OS Env Vars (CI/CD)] ---> [config.properties]
Supported Environment Profiles
SIT (System Integration Testing)

Application Target: UI Scenario Playbook

API Base Endpoint: Specialized Workers Worker Instance

Database Pipeline: Isolated SIT MS SQL Server instance (sit_billing_db)

UAT (User Acceptance Testing)

Application Target: Dedicated UAT Sandbox Scenario Webpage

API Base Endpoint: UAT worker distribution node

Database Pipeline: Isolated UAT MS SQL Server instance (uat_billing_db)

LOCAL (Default Local Sandbox)

Application Target: UI Scenario Playbook

API Base Endpoint: Default Practice Worker Instance

Database Pipeline: Local host connection path (local_billing_db) or H2 in-memory engine

3. Secret Management via Dotenv (.env)
   Developer passwords and sensitive keys are stored locally in a .env file that is strictly excluded from version control via .gitignore.

Local .env Setup
Copy .env.example to .env in the project root directory.

Populate local variables:

Code snippet
TC_MYSQL_PASSWORD=LocalSecurePass123!
TC_MSSQL_PASSWORD=StrongPassword123!
TC_POSTGRES_PASSWORD=postgres
TC_ORACLE_PASSWORD=oracle
4. Database Utility & Multi-Vendor Engine (DbUtil.java & DbContainerFactory.java)
   The database layer provides transparent, vendor-agnostic execution across H2, MySQL, PostgreSQL, Microsoft SQL Server, and Oracle.

                                  +-----------------------+
                                  | DbContainerFactory    |
                                  +-----------+-----------+
                                              |
                   +--------------------------+--------------------------+
                   |                                                     |
        +----------v----------+                               +----------v----------+
        |   In-Memory Mode    |                               | Testcontainers Mode |
        |  (db.vendor = h2)   |                               | (db.vendor = mysql  |
        |  (No Docker needed) |                               |   postgres|oracle)  |
        +----------+----------+                               +----------+----------+
                   |                                                     |
                   +--------------------------+--------------------------+
                                              |
                                     +--------v--------+
                                     |    DbUtil.java  |
                                     |  (Connection &  |
                                     |  Driver Router) |
                                     +-----------------+
Supported Database Engines
h2 (Local Default): Lightweight, zero-Docker in-memory engine emulating MySQL syntax dialect (MODE=MySQL).

mysql: Spawns a Dockerized mysql:8.0.36 container via Testcontainers.

postgres / postgresql: Spawns a Dockerized postgres:16-alpine container.

sqlserver / mssql: Spawns a Dockerized mssql/server:2022-latest container.

oracle: Spawns a Dockerized oracle-free:23-slim-faststart container.

5. Query & Data Abstraction Layer (SqlCatalog.java & TestUser.java)
   SqlCatalog.java (src/main/java/com/automation/e2e/db/SqlCatalog.java): Centralized catalog housing all DDL (table creations) and DML (INSERT, MERGE, SELECT) queries.

TestUser.java Enum (src/main/java/com/automation/e2e/enums/TestUser.java): Strongly typed domain enum providing seed data (username, email, status) for database setup and assertion validation.

6. Local Execution Instructions
   A. Running via IntelliJ IDEA VM Options
   Go to Run/Debug Configurations -> Select your TestNG run target.

In VM options (Alt+M if hidden), input your runtime parameters:

For SIT (UI/API): -Denv=sit

For UAT (UI/API): -Denv=uat

For LOCAL (H2 Database): -Denv=local -Ddb.vendor=h2

For LOCAL (Testcontainers MySQL): -Denv=local -Ddb.vendor=mysql

Click Apply, then Run.

B. Running via Maven Command Line Terminal
Note for Windows PowerShell: Always enclose -D arguments in double quotes to prevent PowerShell from misinterpreting dots (.).

PowerShell
# Run execution loops against System Integration Testing parameters
mvn test "-DsuiteXmlFile=testng.xml" "-Denv=sit"

# Run execution loops against User Acceptance Testing endpoints
mvn test "-DsuiteXmlFile=testng.xml" "-Denv=uat"

# Run execution loops against local sandbox database setup (H2 Default)
mvn test "-DsuiteXmlFile=testng-db.xml" "-Denv=local"

# Run execution loops against Dockerized Testcontainers MySQL
mvn test "-DsuiteXmlFile=testng-db.xml" "-Ddb.vendor=mysql"
7. Continuous Integration & CI/CD Pipeline (.github/workflows/e2e-pipeline.yml)
   The framework includes a GitHub Actions workflow that executes tests automatically on every push or pull_request, and supports manual execution (workflow_dispatch).

Workflow Capabilities
Java 21 & Maven Caching: Pre-configured JDK runtime with dependency caching.

Playwright Browser Cache: Stores Playwright binaries (~/.cache/ms-playwright) to reduce pipeline execution time.

Dynamic Database Selection: Automatically defaults to h2 for automated commits, while offering a choice between h2, mysql, postgres, or sqlserver on manual trigger.

Artifact Reporting: Automatically archives Extent Reports and Surefire test results (target/surefire-reports/) for 14 days after every pipeline run.