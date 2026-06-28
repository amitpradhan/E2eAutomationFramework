# Framework Execution & Environment Switching Guide

This document serves as the operational playbook for managing, configuring, and executing the End-to-End (E2E) Test Automation Framework across multiple target environments (**SIT**, **UAT**, and **LOCAL**) without using hardcoded `.properties` files.

---

## 1. Environment Architecture

The framework handles environmental parameters using an in-memory routing strategy driven by Java Virtual Machine (VM) System Properties (`-Denv`).

When a test cycle is initiated, the initialization block reads the flag, resolves all backend API endpoints, maps the application URL, and selects the correct database string. If no flag is provided, the framework falls back to the safety of the `LOCAL` configuration profile.

---

## 2. Environment Configuration Specifications (`ConfigReader.java`)

The environment management utility is maintained under `src/main/java/com/automation/e2e/utils/ConfigReader.java`. It processes configuration profiles completely in-memory utilizing a conditional routing architecture based on the passed runtime flag.

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
    * **Database Pipeline:** Local host instance connection path (`local_billing_db`) utilizing system administrator default parameters (`SA`)

---

## 3. Local Execution Instructions

### A. Running via IntelliJ IDEA VM Options
To run specific environments locally inside your Integrated Development Environment (IDE):

1. In the top-right toolbar, click the **Run Configurations** dropdown (next to the green Run/Debug icons) and choose **Edit Configurations...**
2. Select your target TestNG XML or Test class file configuration from the left pane.
3. Locate the **VM options** field. *(If the field is not visible, click **Modify options** or use the shortcut **Alt+M**, and verify **Add VM options** is checked).*
4. Input your environment property argument exactly as required:
    * For SIT: `-Denv=sit`
    * For UAT: `-Denv=uat`
    * For LOCAL: `-Denv=local`
5. Click **Apply**, then click **OK**. Click the green **Run** or **Debug** arrow to launch.

### B. Running via Maven Command Line Terminal
Open any standard console shell at your root directory path and execute the suite by joining the target environment flag onto the regular Maven test lifecycle syntax:

```bash
# Run execution loops against System Integration Testing parameters
mvn test -DsuiteXmlFile=testng.xml -Denv=sit

# Run execution loops against User Acceptance Testing endpoints
mvn test -DsuiteXmlFile=testng.xml -Denv=uat

# Run execution loops against your local sandbox database engine setup
mvn test -DsuiteXmlFile=testng.xml -Denv=local