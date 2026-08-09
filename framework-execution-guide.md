Here is your completely updated, properly formatted Markdown documentation. All ASCII diagrams, code blocks, section headers, and lists have been fixed so they render cleanly in Markdown previews and Git repositories.

Markdown
# Framework Execution, Architecture & Operational Guide

This document serves as the operational playbook for managing, configuring, and executing the End-to-End (E2E) Test Automation Framework across multiple target environments (**SIT**, **UAT**, and **LOCAL**) and database layers without hardcoding credentials or `.properties` parameters.

---

## 1. System Architecture Overview

## 1. System Architecture Overview

The framework employs a tri-layer automation model (UI + REST API + Database) built with **Playwright Java**, **TestNG**, **Jackson**, **Log4j2**, **Dotenv**, and **Testcontainers / H2 Database**.

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
```

---

## 2. Environment Architecture & Dynamic Configuration (`ConfigReader.java`)

The environment management utility is maintained under `src/main/java/com/automation/e2e/utils/ConfigReader.java`. It processes configuration profiles completely in-memory utilizing an in-memory routing strategy driven by Java Virtual Machine (VM) System Properties (`-Denv`).

When a test cycle is initiated, the initialization block reads the flag, resolves all backend API endpoints, maps the application URL, and selects the correct database string. If no flag is provided, the framework falls back to the safety of the `LOCAL` configuration profile.

To prevent hardcoding sensitive credentials or environment parameters, configuration resolution follows a strict order of precedence:

[JVM -D Args] ---> [Dotenv (.env)] ---> [OS Env Vars (CI/CD)] ---> [config.properties]


### Supported Environment Profiles

* **SIT (System Integration Testing)**
    * **Application Target:** UI Scenario Playbook
    * **API Base Endpoint:** Specialized Workers Worker Instance
    * **Database Pipeline:** Isolated SIT MS SQL Server instance (`sit_billing_db`)
* **UAT (User Acceptance Testing)**
    * **Application Target:** Dedicated UAT Sandbox Scenario Webpage
    * **API Base Endpoint:** UAT worker distribution node
    * **Database Pipeline:** Isolated UAT MS SQL Server instance (`uat_billing_db`)
* **LOCAL (Default Local Sandbox)**
    * **Application Target:** UI Scenario Playbook
    * **API Base Endpoint:** Default Practice Worker Instance
    * **Database Pipeline:** Local host connection path (`local_billing_db`) or H2 in-memory engine

---

## 3. Secret Management via Dotenv (`.env`)

Developer passwords and sensitive keys are stored locally in a `.env` file that is strictly excluded from version control via `.gitignore`.

### Local `.env` Setup
1. Copy `.env.example` to `.env` in the project root directory.
2. Populate local variables:
```env
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
To eliminate hardcoded SQL statements and inline string constants in test methods:

SqlCatalog.java (src/main/java/com/automation/e2e/db/SqlCatalog.java): Centralized catalog housing all DDL (table creations) and DML (INSERT, MERGE, SELECT) queries.

TestUser.java Enum (src/main/java/com/automation/e2e/enums/TestUser.java): Strongly typed domain enum providing seed data (username, email, status) for database setup and assertion validation.

6. Local Execution Instructions
A. Running via IntelliJ IDEA VM Options
To run specific environments locally inside your Integrated Development Environment (IDE):

In the top-right toolbar, click the Run Configurations dropdown (next to the green Run/Debug icons) and choose Edit Configurations...

Select your target TestNG XML or Test class file configuration from the left pane.

Locate the VM options field. (If the field is not visible, click Modify options or use the shortcut Alt+M, and verify Add VM options is checked).

Input your environment property arguments exactly as required:

For SIT (UI/API): -Denv=sit

For UAT (UI/API): -Denv=uat

For LOCAL (H2 Database): -Denv=local -Ddb.vendor=h2

For LOCAL (Testcontainers MySQL): -Denv=local -Ddb.vendor=mysql

Click Apply, then click OK. Click the green Run or Debug arrow to launch.

B. Running via Maven Command Line Terminal
Open any standard console shell at your root directory path and execute the suite by joining the target environment flag onto the regular Maven test lifecycle syntax:

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