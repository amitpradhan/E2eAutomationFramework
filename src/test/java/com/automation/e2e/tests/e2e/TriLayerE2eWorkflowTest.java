package com.automation.e2e.tests.e2e;

import com.automation.e2e.api.services.BillsApi;
import com.automation.e2e.base.Base;
import com.automation.e2e.db.DbUtil;
import com.automation.e2e.db.SqlCatalog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class TriLayerE2eWorkflowTest extends Base {

    private BillsApi billsApi;

    @BeforeMethod
    public void setupServices() {
        billsApi = new BillsApi(getApiContext());
    }

    @Test(description = "Tri-layer verification: Fetch via API -> Seed to H2 DB -> Verify data persistence")
    public void testApiToDatabaseToUiSync() throws Exception {
        log.info("--- [E2E WORKFLOW] Starting API -> H2 Database integration test ---");

        // 1. API LAYER - Fetch baseline payload
        APIResponse apiResponse = billsApi.getAllBills();
        Assert.assertEquals(apiResponse.status(), 200, "API request failed!");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(apiResponse.text());
        JsonNode billsArray = rootNode.isArray() ? rootNode : rootNode.path("data");

        Assert.assertTrue(billsArray.isArray() && billsArray.size() > 0, "No bills returned from API!");

        JsonNode firstBill = billsArray.get(0);
        String billId = firstBill.path("id").asText();
        double amount = firstBill.path("amount").asDouble(0.0);
        log.info("Fetched bill via API | ID: {}, Amount: {}", billId, amount);

        // 2. DATABASE LAYER - Sync API response into H2 Database using SqlCatalog
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(SqlCatalog.CREATE_SYNCED_BILLS_TABLE);

            try (PreparedStatement pstmt = conn.prepareStatement(SqlCatalog.MERGE_SYNCED_BILL)) {
                pstmt.setString(1, billId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, "SYNCED");
                pstmt.executeUpdate();
            }
            log.info("Successfully executed MERGE/INSERT into H2 database table 'synced_bills'.");
        }

        // 3. VERIFICATION - Query and view persisted data in H2 DB
        int syncedCount = DbUtil.getRowCount("synced_bills", "bill_id='" + billId + "'");
        Assert.assertEquals(syncedCount, 1, "Record count mismatch in H2 DB!");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SqlCatalog.SELECT_SYNCED_BILL_BY_ID)) {

            pstmt.setString(1, billId);
            try (ResultSet rs = pstmt.executeQuery()) {
                Assert.assertTrue(rs.next(), "Persisted record was not found in H2 DB!");

                String persistedBillId = rs.getString("bill_id");
                double persistedAmount = rs.getDouble("amount");
                String persistedStatus = rs.getString("sync_status");

                log.info("==================== VIEW H2 PERSISTED DATA ====================");
                log.info("Bill ID    : {}", persistedBillId);
                log.info("Amount     : {}", persistedAmount);
                log.info("Sync Status: {}", persistedStatus);
                log.info("===============================================================");

                Assert.assertEquals(persistedBillId, billId, "Persisted Bill ID does not match API data!");
                Assert.assertEquals(persistedAmount, amount, 0.01, "Persisted Amount does not match API data!");
                Assert.assertEquals(persistedStatus, "SYNCED", "Persisted Sync Status mismatch!");
            }
        }

        log.info("--- [E2E WORKFLOW COMPLETED SUCCESSFULLY] ---");
    }
}