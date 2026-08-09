package com.automation.e2e.enums;

public enum TestUser {

    AMIT("Amit", "amit@megamind360.com", "ACTIVE"),
    AUTOMATION_TESTER("AutomationTester", "tester@megamind360.com", "INACTIVE");

    private final String username;
    private final String email;
    private final String status;

    TestUser(String username, String email, String status) {
        this.username = username;
        this.email = email;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}