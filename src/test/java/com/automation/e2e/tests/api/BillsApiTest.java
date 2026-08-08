package com.automation.e2e.tests.api;

import com.automation.e2e.api.services.BillsApi;
import com.automation.e2e.base.Base;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BillsApiTest extends Base {

    private BillsApi billsApi;

    @BeforeMethod
    public void setupApiServices() {
        // Initialize the service object with the thread-safe API context
        billsApi = new BillsApi(getApiContext());
    }

//    @Test(priority = 1, description = "Verify GET /v1/bills endpoint execution")
//    public void testGetBillsEndpoint() {
//        // 1. Single execution step
//        APIResponse response = billsApi.getAllBills();
//
//        // 2. Assertions
//        Assert.assertEquals(response.status(), 200, "Expected HTTP status 200 OK");
//        Assert.assertTrue(response.text().contains("id"), "Expected response payload to contain entity identifiers");
//    }


    @Test(priority = 1, description = "Verify GET /v1/bills endpoint execution and print all bills/billers")
    public void testGetBillsEndpoint() throws Exception {
        // 1. Single execution step
        APIResponse response = billsApi.getAllBills();

        // 2. Assertions
        Assert.assertEquals(response.status(), 200, "Expected HTTP status 200 OK");

        String responseBody = response.text();
        Assert.assertNotNull(responseBody, "Response body cannot be null");

        // 3. Parse JSON response and log all billers/bills
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseBody);

        JsonNode billsArray = rootNode.isArray() ? rootNode : rootNode.path("data");

        log.info("==================== FETCHED BILLS LIST ====================");
        if (billsArray.isArray() && billsArray.size() > 0) {
            for (JsonNode bill : billsArray) {
                String billId = bill.path("id").asText("N/A");
                String billerId = bill.path("billerId").asText("N/A");
                String amount = bill.path("amount").asText("N/A");
                String status = bill.path("status").asText("N/A");

                String billDetails = String.format("Bill ID: %s | Biller ID: %s | Amount: %s | Status: %s",
                        billId, billerId, amount, status);

                // Logs to console, log4j file appenders, and ExtentReports listener stream
                log.info(billDetails);
            }
        } else {
            log.warn("No bills found in the response body or structure is nested differently: {}", responseBody);
        }
        log.info("============================================================");

        Assert.assertTrue(responseBody.contains("id"), "Expected response payload to contain entity identifiers");
    }
    @Test(priority = 2, description = "Verify POST /v1/bills endpoint execution using a JSON payload file")
    public void testCreateBillEndpoint() {
        APIResponse response = billsApi.createBill("payloads/create_bill.json");

        // Print the error message from the server to debug the 400 Bad Request
        System.out.println("SERVER RESPONSE BODY: " + response.text());

        Assert.assertTrue(response.status() == 201 || response.status() == 200, "Expected HTTP status 201 or 200, but got " + response.status());
    }
}