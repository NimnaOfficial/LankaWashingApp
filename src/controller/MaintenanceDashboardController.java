package controller;

import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MaintenanceDashboardController {

    private Connection conn;

    public MaintenanceDashboardController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. Count Machines needing repair (Status = 'Maintenance' or 'Offline')
    public String getCriticalMachineCount() {
        return getCount("SELECT COUNT(*) FROM Machine WHERE status IN ('Maintenance', 'Offline')");
    }

    // 2. Count Available Technicians
    public String getAvailableTechnicianCount() {
        return getCount("SELECT COUNT(*) FROM Technician WHERE availabilityStatus = 'Available'");
    }

    // 3. Count Pending Maintenance Tasks
    public String getPendingTaskCount() {
        // Assuming we look for tasks logged recently or specific logic
        // For now, let's count total tasks for the month
        return getCount("SELECT COUNT(*) FROM Task WHERE MONTH(date) = MONTH(CURRENT_DATE())");
    }

    private String getCount(String sql) {
        try {
            if (conn == null) return "0";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }
}