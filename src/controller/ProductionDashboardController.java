package controller;

import db.DatabaseConnection;
import java.sql.*;

public class ProductionDashboardController {

    private Connection conn;

    public ProductionDashboardController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Card 1: Count batches that are NOT completed
    public String getActiveBatchCount() {
        try {
            String sql = "SELECT COUNT(*) FROM ProductionBatchRecord WHERE status != 'Completed'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return "0";
    }

    // Card 2: Count Total Activities (Dye + Wash + Dry)
    // Since we split the tables, we sum the counts of all 3
    public String getDailyActivityCount() {
        int total = 0;
        try {
            Statement stmt = conn.createStatement();

            // Count Dyeing
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM DyeProcess");
            if(rs1.next()) total += rs1.getInt(1);

            // Count Washing
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM WashingProcess");
            if(rs2.next()) total += rs2.getInt(1);

            // Count Drying
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM DryingProcess");
            if(rs3.next()) total += rs3.getInt(1);

        } catch (SQLException e) { e.printStackTrace(); }
        return String.valueOf(total);
    }

    // Card 3: Count Customer Requests
    public String getPendingRequestCount() {
        try {
            // Assuming you have a CustomerRequest table, if not, returns 0
            String sql = "SELECT COUNT(*) FROM CustomerRequest WHERE status = 'Pending'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (SQLException e) {
            return "0"; // Fail gracefully if table doesn't exist yet
        }
        return "0";
    }
}