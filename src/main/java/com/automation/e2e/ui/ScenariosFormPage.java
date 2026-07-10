package com.automation.e2e.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

public class ScenariosFormPage extends BasePage {

    // Form elements locators mapping standard practice rules
    private final Locator inputTextField;
    private final Locator selectionDropdown;
    private final Locator termsCheckbox;
    private final Locator submissionButton;
    private final Locator statusConfirmationMessage;

    public ScenariosFormPage(Page page) {
        super(page);
        this.inputTextField = page.locator("input[type='text'], input#username, #text-input").first();
        this.selectionDropdown = page.locator("select, select#dropdown, #select-options").first();
        this.termsCheckbox = page.locator("input[type='checkbox'], #checkbox-input").first();
        this.submissionButton = page.locator("button[type='submit'], button:has-text('Submit')").first();
        this.statusConfirmationMessage = page.locator(".alert, .success-message, #output-status").first();
    }

    public ScenariosFormPage fillTextInput(String data) {
        inputTextField.fill(data);
        return this;
    }

    public ScenariosFormPage chooseDropdownOptionByValue(String valueAttribute) {
        selectionDropdown.selectOption(new SelectOption().setValue(valueAttribute));
        return this;
    }

    public ScenariosFormPage chooseDropdownOptionByLabel(String visibleText) {
        selectionDropdown.selectOption(new SelectOption().setLabel(visibleText));
        return this;
    }

    public ScenariosFormPage toggleCheckbox(boolean checkState) {
        if (checkState) {
            termsCheckbox.check();
        } else {
            termsCheckbox.uncheck();
        }
        return this;
    }

    public void clickSubmit() {
        submissionButton.click();
        waitForNetworkSettle();
    }

    public String getConfirmationText() {
        return statusConfirmationMessage.textContent().trim();
    }
}