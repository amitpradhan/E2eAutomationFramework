package com.automation.e2e.tests.db;

import com.automation.e2e.base.Base;
import com.automation.e2e.db.DbUtil;
import com.automation.e2e.db.SqlCatalog;
import com.automation.e2e.enums.TestUser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseIntegrationTest extends Base {

    @Test(description = "Verify table creation, record insertion, and queries using SqlCatalog")
    public void testDatabaseCrudOperations() throws Exception {
        log.info("Starting database verification test on configured DB engine...");

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create table using SqlCatalog
            stmt.execute(SqlCatalog.CREATE_USERS_TABLE);
            log.info("Successfully executed table creation query from SqlCatalog.");

            // Insert records using SqlCatalog
            try (PreparedStatement pstmt = conn.prepareStatement(SqlCatalog.INSERT_USER)) {
                for (TestUser user : TestUser.values()) {
                    pstmt.setString(1, user.getUsername());
                    pstmt.setString(2, user.getEmail());
                    pstmt.setString(3, user.getStatus());
                    pstmt.executeUpdate();
                }
            }
            log.info("Successfully seeded user records into 'users' table using TestUser enum.");

            // Verify total row count
            int totalActiveUsers = DbUtil.getRowCount("users", "status='ACTIVE'");
            Assert.assertEquals(totalActiveUsers, 1, "Expected exactly 1 ACTIVE user in database");

            // Query using SqlCatalog
            try (PreparedStatement pstmt = conn.prepareStatement(SqlCatalog.SELECT_USER_BY_USERNAME)) {
                pstmt.setString(1, "Amit");
                try (ResultSet rs = pstmt.executeQuery()) {
                    Assert.assertTrue(rs.next(), "Expected user record 'Amit' to exist");
                    Assert.assertEquals(rs.getString("email"), "amit@megamind360.com", "Email mismatch");
                }
            }
            log.info("Successfully verified database record values.");
        }
    }
}