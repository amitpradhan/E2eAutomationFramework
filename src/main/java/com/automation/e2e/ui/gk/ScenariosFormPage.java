package com.automation.e2e.ui.gk;

import com.automation.e2e.utils.UiActionsUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ScenariosFormPage extends BasePage {

    private final Locator inputTextField;
    private final Locator selectionDropdown;
    private final Locator termsCheckbox;
    private final Locator submissionButton;
    private final Locator statusConfirmationMessage;

    public ScenariosFormPage(Page page) {
        super(page);
        this.inputTextField = page.locator("input[type='text'], #text-input").first();
        this.selectionDropdown = page.locator("select").first();
        this.termsCheckbox = page.locator("input[type='checkbox']").first();
        this.submissionButton = page.locator("button:has-text('Submit')").first();
        this.statusConfirmationMessage = page.locator(".alert, #output-status").first();
    }

    public ScenariosFormPage fillTextInput(String data) {
        UiActionsUtil.enterText(inputTextField, data);
        return this;
    }

    public ScenariosFormPage chooseDropdownOptionByValue(String valueAttribute) {
        UiActionsUtil.selectDropdownByValue(selectionDropdown, valueAttribute);
        return this;
    }

    public ScenariosFormPage toggleCheckbox(boolean checkState) {
        UiActionsUtil.setCheckboxState(termsCheckbox, checkState);
        return this;
    }

    public void clickSubmit() {
        UiActionsUtil.click(submissionButton);
        waitForNetworkSettle();
    }

    public String getConfirmationText() {
        return UiActionsUtil.getElementText(statusConfirmationMessage);
    }
}