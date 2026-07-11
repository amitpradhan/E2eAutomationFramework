package com.automation.e2e.utils;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Global UI Interactions Utility class for the E2E Automation Framework.
 * Centralizes all low-level Playwright actions with built-in explicit visibility waits
 * to ensure test stability and prevent synchronization flake across the application.
 */
public class UiActionsUtil {

    /**
     * Internal helper method to enforce that an element is visible in the DOM
     * before the framework attempts any interaction loop.
     *
     * @param locator The Playwright Locator wrapper targeting the element.
     */
    private static void waitForReadyState(Locator locator) {
        locator.first().waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
    }

    /**
     * Waits for the element to be visible and executes a standard click action.
     *
     * @param locator The Playwright Locator targeting the element to be clicked.
     */
    public static void click(Locator locator) {
        waitForReadyState(locator);
        locator.first().click();
    }

    /**
     * Dynamically waits for an input element, clears any existing text values,
     * and fills it with the provided string sequence.
     *
     * @param locator The Playwright Locator targeting the text input field.
     * @param value   The text string to populate into the input field.
     */
    public static void enterText(Locator locator, String value) {
        waitForReadyState(locator);
        locator.first().clear();
        locator.first().fill(value);
    }

    /**
     * Selects an option from a standard HTML select dropdown menu using the 'value' attribute.
     *
     * @param locator The Playwright Locator targeting the HTML select element.
     * @param value   The raw 'value' attribute string of the option to select.
     */
    public static void selectDropdownByValue(Locator locator, String value) {
        waitForReadyState(locator);
        locator.first().selectOption(new SelectOption().setValue(value));
    }

    /**
     * Selects an option from a standard HTML select dropdown menu using its visible label text.
     *
     * @param locator   The Playwright Locator targeting the HTML select element.
     * @param labelText The exact visible text of the option as seen in the UI.
     */
    public static void selectDropdownByVisibleText(Locator locator, String labelText) {
        waitForReadyState(locator);
        locator.first().selectOption(new SelectOption().setLabel(labelText));
    }

    /**
     * Synchronizes and forces a checkbox or radio button element to match the intended state.
     *
     * @param locator         The Playwright Locator targeting the checkbox or radio button.
     * @param shouldBeChecked True to check the element, false to uncheck it.
     */
    public static void setCheckboxState(Locator locator, boolean shouldBeChecked) {
        waitForReadyState(locator);
        if (shouldBeChecked) {
            locator.first().check();
        } else {
            locator.first().uncheck();
        }
    }

    /**
     * Iterates through a grouped collection of radio buttons and clicks the specific item
     * whose 'value' attribute matches the target selection metric.
     *
     * @param radioGroupLocator The Playwright Locator targeting the entire collection of radio elements.
     * @param targetValue       The 'value' attribute string representing the desired selection.
     * @throws RuntimeException If no radio button matches the specified value string.
     */
    public static void selectRadioButtonByValue(Locator radioGroupLocator, String targetValue) {
        int count = radioGroupLocator.count();
        for (int i = 0; i < count; i++) {
            Locator currentRadio = radioGroupLocator.nth(i);
            String valueAttribute = currentRadio.getAttribute("value");
            if (valueAttribute != null && valueAttribute.equalsIgnoreCase(targetValue)) {
                currentRadio.check();
                return;
            }
        }
        throw new RuntimeException("Target radio option matching '" + targetValue + "' not found.");
    }

    /**
     * Performs a realistic mouse hover movement over the designated target element.
     * Commonly used to trigger hover menus, tooltip viewports, or hidden CSS overlays.
     *
     * @param locator The Playwright Locator targeting the element to hover over.
     */
    public static void hover(Locator locator) {
        waitForReadyState(locator);
        locator.first().hover();
    }

    /**
     * Extracts and sanitizes the inner text content block from an active DOM element.
     *
     * @param locator The Playwright Locator targeting the element.
     * @return A trimmed String containing the element's clean text payload.
     */
    public static String getElementText(Locator locator) {
        waitForReadyState(locator);
        return locator.first().textContent().trim();
    }

    /**
     * Attaches an asynchronous, global alert intercept handler to auto-accept JavaScript dialogs
     * (Alerts, Confirms) immediately when they trigger in the browser browser context.
     *
     * @param page The current active Playwright Page instance where the dialog is expected.
     */
    public static void handleAlertAutoAccept(Page page) {
        // We declare an array placeholder to bypass Java's closure requirement for effective unbinding
        java.util.function.Consumer<com.microsoft.playwright.Dialog>[] handlerWrapper = new java.util.function.Consumer[1];

        handlerWrapper[0] = dialog -> {
            try {
                System.out.println("[ALERT DETECTED] Type: " + dialog.type() + " | Message: " + dialog.message());
                dialog.accept();
            } finally {
                // Instantly remove this specific listener so it doesn't leak into subsequent steps
                page.offDialog(handlerWrapper[0]);
            }
        };

        page.onDialog(handlerWrapper[0]);
    }

    /**
     * Attaches a global prompt intercept handler that automatically types a designated response
     * into a JavaScript prompt dialog before selecting accept.
     *
     * @param page               The current active Playwright Page instance.
     * @param promptResponseText The text payload string to inject into the dialog prompt field.
     */
    public static void handlePromptWithText(Page page, String promptResponseText) {
        java.util.function.Consumer<com.microsoft.playwright.Dialog>[] handlerWrapper = new java.util.function.Consumer[1];

        handlerWrapper[0] = dialog -> {
            try {
                System.out.println("[PROMPT DETECTED] Message: " + dialog.message());
                dialog.accept(promptResponseText);
            } finally {
                // Instantly remove this specific handler from the page context event registry
                page.offDialog(handlerWrapper[0]);
            }
        };

        page.onDialog(handlerWrapper[0]);
    }

    /**
     * Handles clicking an element that causes the browser engine to spin up a concurrent,
     * asynchronous popup window or secondary tab container.
     *
     * @param triggerLocator The Locator targeting the button/link that spawns the new window.
     * @return A fresh, isolated Playwright Page context handler referencing the newly spawned tab.
     */
    public static Page clickAndGetPopup(Locator triggerLocator) {
        waitForReadyState(triggerLocator);
        return triggerLocator.page().waitForPopup(triggerLocator.first()::click);
    }

    /**
     * Scans all active browser contexts to locate and switch window focus to a tab
     * whose active Page Title or URL string matches the search parameter token.
     *
     * @param currentPage            The current page reference to pull context scope parameters from.
     * @param titleOrUrlSearchToken The keyword string token to match against window titles or URLs.
     * @return The target focused Playwright Page instance.
     * @throws RuntimeException If no page window matches the structural search token.
     */
    public static Page switchToTabByTitleOrUrl(Page currentPage, String titleOrUrlSearchToken) {
        List<Page> allPages = currentPage.context().pages();
        for (Page openPage : allPages) {
            if (openPage.title().contains(titleOrUrlSearchToken) || openPage.url().contains(titleOrUrlSearchToken)) {
                openPage.bringToFront();
                return openPage;
            }
        }
        throw new RuntimeException("Target browser tab matching '" + titleOrUrlSearchToken + "' not found.");
    }

    /**
     * Locates a hyperlink anchor tag text matching the text search parameters and triggers a click.
     *
     * @param page            The current active Playwright Page instance.
     * @param visibleLinkText The visible text of the link to click.
     */
    public static void clickLinkByText(Page page, String visibleLinkText) {
        // Playwright handles fuzzy/substring matching by default when options are omitted
        Locator linkLocator = page.locator("a").getByText(visibleLinkText);
        linkLocator.first().waitFor();
        linkLocator.first().click();
    }

    /**
     * Bypasses the parent document context to directly execute filling or clicking steps
     * inside an isolated HTML iFrame document tree structure.
     *
     * @param page          The current active Playwright Page instance.
     * @param frameSelector The selector identifying the iframe (e.g., "#my-iframe").
     * @param innerSelector The locator path string inside the iframe targeting the actual element.
     * @param actionType    The exact action keyword to process ("click", "fill", or "enterText").
     * @param value         The string data payload to supply if executing an text entry fill action.
     * @return A FrameLocator reference to allow further custom interaction loops.
     */
    public static FrameLocator interactInsideFrame(Page page, String frameSelector, String innerSelector, String actionType, String value) {
        FrameLocator frame = page.frameLocator(frameSelector);
        Locator innerElement = frame.locator(innerSelector).first();
        innerElement.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

        if (actionType.equalsIgnoreCase("click")) {
            innerElement.click();
        } else if (actionType.equalsIgnoreCase("fill") || actionType.equalsIgnoreCase("enterText")) {
            innerElement.clear();
            innerElement.fill(value);
        }
        return frame;
    }

    /**
     * Injects one or more local file assets into an HTML input upload element (`input[type='file']`).
     *
     * @param uploadInputLocator The Playwright Locator targeting the upload input field element.
     * @param relativeFilePaths  Varargs string paths leading to the files (e.g. "src/test/resources/data.xlsx").
     */
    public static void uploadFiles(Locator uploadInputLocator, String... relativeFilePaths) {
        uploadInputLocator.first().waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        Path[] filePaths = new Path[relativeFilePaths.length];
        for (int i = 0; i < relativeFilePaths.length; i++) {
            filePaths[i] = Paths.get(relativeFilePaths[i]);
        }
        uploadInputLocator.first().setInputFiles(filePaths);
    }

    /**
     * Listens for an automated browser download stream triggered by a target click action,
     * waits for the download payload to finalize, and saves it into the local project directory.
     *
     * @param clickTriggerLocator The Locator targeting the button/link that initiates the download stream.
     * @param destinationFolder   The local target directory path where the file should be generated.
     * @param customFileName      Optional personalized string to rename the downloaded asset. Pass null to keep suggested values.
     * @return The absolute file system path string pointing directly to the saved download asset.
     */
    public static String clickAndDownloadFile(Locator clickTriggerLocator, String destinationFolder, String customFileName) {
        clickTriggerLocator.first().waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        Download download = clickTriggerLocator.page().waitForDownload(clickTriggerLocator.first()::click);
        String resolvedName = (customFileName == null || customFileName.isEmpty()) ? download.suggestedFilename() : customFileName;
        Path targetPath = Paths.get(destinationFolder, resolvedName);
        download.saveAs(targetPath);
        return targetPath.toAbsolutePath().toString();
    }
}