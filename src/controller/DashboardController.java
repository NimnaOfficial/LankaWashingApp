package controller;

import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardController {

    private Connection conn;

    public DashboardController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. Count Low Stock Items
    public String getLowStockCount() {
        return getCount("SELECT COUNT(*) FROM Resource WHERE currentQty <= minLevel");
    }

    // 2. Count Total Suppliers
    public String getTotalSuppliers() {
        return getCount("SELECT COUNT(*) FROM Supplier");
    }

    // 3. Count Total Raw Materials (Swapped from "Pending Orders" since that table doesn't exist yet)
    public String getTotalMaterials() {
        return getCount("SELECT COUNT(*) FROM Resource");
    }

    // Helper method to execute count queries
    private String getCount(String sql) {
        try {
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