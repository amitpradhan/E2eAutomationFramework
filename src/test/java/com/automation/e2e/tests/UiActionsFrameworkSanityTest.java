package com.automation.e2e.tests;

import com.automation.e2e.base.Base;
import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Page;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UiActionsFrameworkSanityTest extends Base {

    private String mockSandboxHtmlPath;

    @BeforeClass
    public void generateLocalSandboxEnvironment() throws Exception {
        // Build a dynamic HTML mock dashboard file to safely run the UI action utility validations locally
        String sandboxHtmlContent = "<html>" +
                "<head><title>Sanity Workspace Sandbox</title></head>" +
                "<body>" +
                "  <h1 id='title-header'>Core Automation Workspace</h1>" +
                "  " +
                "  <!-- Form Elements -->" +
                "  <input type='text' id='username-field' placeholder='Enter user token'/>" +
                "  <select id='country-select'>" +
                "    <option value='ind'>India</option>" +
                "    <option value='usa'>United States</option>" +
                "  </select>" +
                "  <input type='checkbox' id='terms-chk'/>" +
                "  <input type='radio' name='gender' value='male' id='rad-m'/>" +
                "  <input type='radio' name='gender' value='female' id='rad-f'/>" +
                "  <button id='action-btn' onclick='document.getElementById(\"title-header\").innerText=\"Action Verified\"'>Submit Data</button>" +
                "  " +
                "  <!-- Dialogs & Popups -->" +
                "  <button id='alert-btn' onclick='alert(\"Native Browser Notification\")'>Trigger Alert</button>" +
                "  <button id='prompt-btn' onclick='let name=prompt(\"Enter Key:\"); if(name) document.getElementById(\"title-header\").innerText=name;'>Trigger Prompt</button>" +
                "  <a id='popup-link' href='https://gauravkhurana.com' target='_blank'>Launch Profile</a>" +
                "  " +
                "  <!-- File Management & Frames -->" +
                "  <input type='file' id='file-upload-input'/>" +
                "  <a id='download-link' href='data:text/plain;charset=utf-8,Framework%20Download%20Verification' download='sanity_report.txt'>Download Log</a>" +
                "  <iframe id='security-frame' srcdoc='<html><body><input id=\"inner-frame-field\" value=\"Initial\"/></body></html>'></iframe>" +
                "</body>" +
                "</html>";

        // Write file inside compilation target block folder structures
        Files.createDirectories(Paths.get("target"));
        File mockFile = new File("target/UiActionsSandbox.html");
        Files.writeString(mockFile.toPath(), sandboxHtmlContent);
        mockSandboxHtmlPath = "file://" + mockFile.getAbsolutePath();
    }

    @Test(priority = 1, description = "Verify basic element interaction operations like text entries, selections, checkboxes and clicks")
    public void testStandardInteractiveFormElements() {
        page.navigate(mockSandboxHtmlPath);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        // Verify element text extraction
        String initialHeader = UiActionsUtil.getElementText(page.locator("#title-header"));
        Assert.assertEquals(initialHeader, "Core Automation Workspace");

        // Verify filling text inputs
        UiActionsUtil.enterText(page.locator("#username-field"), "Amit_Automation_Worker");

        // Verify dropdown item selection by value
        UiActionsUtil.selectDropdownByValue(page.locator("#country-select"), "usa");

        // Verify checkbox switching actions
        UiActionsUtil.setCheckboxState(page.locator("#terms-chk"), true);

        // Verify grouped radio selection iterations
        UiActionsUtil.selectRadioButtonByValue(page.locator("input[name='gender']"), "female");

        // Verify click execution behaviors
        UiActionsUtil.click(page.locator("#action-btn"));
        Assert.assertEquals(UiActionsUtil.getElementText(page.locator("#title-header")), "Action Verified");
    }

    @Test(priority = 2, dependsOnMethods = {"testStandardInteractiveFormElements"},
            description = "Verify native browser Javascript dialog interceptors and tab switching management hooks")
    public void testWindowsAlertsAndTabContexts() {
        // 1. Verify native alert dialog auto-acceptance (Cleanly attaches -> accepts -> detaches)
        UiActionsUtil.handleAlertAutoAccept(page);
        UiActionsUtil.click(page.locator("#alert-btn"));

        // 2. Verify JavaScript input prompt handling (Cleanly attaches -> inputs text -> detaches)
        UiActionsUtil.handlePromptWithText(page, "Prompt Intercept Success");
        UiActionsUtil.click(page.locator("#prompt-btn"));
        Assert.assertEquals(UiActionsUtil.getElementText(page.locator("#title-header")), "Prompt Intercept Success");

        // 3. Verify dynamic window popups collection captures
        Page popupWindow = UiActionsUtil.clickAndGetPopup(page.locator("#popup-link"));
        Assert.assertNotNull(popupWindow, "Failed to capture the spawned browser popup frame context!");

        // Return focus back to the workspace tab
        UiActionsUtil.switchToTabByTitleOrUrl(page, "Sanity Workspace Sandbox");
        popupWindow.close();
    }

    @Test(priority = 3, dependsOnMethods = {"testStandardInteractiveFormElements"},
            description = "Verify isolated sub-frame document object traversal models along with multi-file uploads and downloads")
    public void testFramesAndFileManagementOperations() throws Exception {
        // Verify cross-document iframe execution workflows
        UiActionsUtil.interactInsideFrame(page, "#security-frame", "#inner-frame-field", "fill", "Frame Mutation Completed");

        // Verify local file injection uploads
        File tempUploadSample = new File("target/upload_token.txt");
        Files.writeString(tempUploadSample.toPath(), "Content Data Payload");
        UiActionsUtil.uploadFiles(page.locator("#file-upload-input"), tempUploadSample.getAbsolutePath());

        // Verify asynchronous local path asset generation downloads
        String downloadedFileLocation = UiActionsUtil.clickAndDownloadFile(
                page.locator("#download-link"),
                "target/downloads/",
                "verified_sanity_output.txt"
        );

        Assert.assertTrue(Files.exists(Paths.get(downloadedFileLocation)), "Target download asset file is missing from target path directory destination!");
        System.out.println("[SUCCESS] Framework core UI action utility validation suite completed flawlessly.");
    }
}