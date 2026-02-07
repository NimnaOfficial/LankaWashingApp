package controller;

import db.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ProductionProcessController {

    private Connection conn;

    public ProductionProcessController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 1. GET ACTIVE BATCHES ---
    public HashMap<String, Integer> getActiveBatches() {
        HashMap<String, Integer> map = new HashMap<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT batchId, productName FROM ProductionBatchRecord WHERE status != 'Completed'");
            while (rs.next()) map.put(rs.getInt("batchId") + " - " + rs.getString("productName"), rs.getInt("batchId"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // --- 2. SPECIFIC RESOURCE LOADERS ---
    public HashMap<String, Integer> getDyeingResources() {
        return getResourcesByQuery("SELECT resourceId, resourceName FROM Resource WHERE category = 'Chemical'");
    }

    public HashMap<String, Integer> getWashingResources() {
        return getResourcesByQuery("SELECT resourceId, resourceName FROM Resource WHERE category IN ('Chemical', 'Consumable')");
    }

    public HashMap<String, Integer> getDryingResources() {
        return getResourcesByQuery("SELECT resourceId, resourceName FROM Resource WHERE category = 'Fuel'");
    }

    private HashMap<String, Integer> getResourcesByQuery(String sql) {
        HashMap<String, Integer> map = new HashMap<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) map.put(rs.getString("resourceName"), rs.getInt("resourceId"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // --- 3. SAVE PROCESS LOGIC ---
    private boolean saveGenericProcess(String table, String colQty, String colType, int batchId, int resId, double qty, String typeVal, String duration) {
        try {
            conn.setAutoCommit(false);

            // A. Check Stock & Price
            PreparedStatement pstCheck = conn.prepareStatement("SELECT currentQty, unitPrice, resourceName FROM Resource WHERE resourceId=?");
            pstCheck.setInt(1, resId);
            ResultSet rs = pstCheck.executeQuery();
            if (!rs.next()) return false;

            double stock = rs.getDouble("currentQty");
            double price = rs.getDouble("unitPrice");
            String resName = rs.getString("resourceName");

            // B. Validation
            if (stock < qty) {
                JOptionPane.showMessageDialog(null, "⚠️ Stock Low for " + resName + "!\nAvailable: " + stock + "\nRequired: " + qty);
                conn.rollback();
                return false;
            }

            if (price <= 0) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "⚠️ Warning: The Unit Price for '" + resName + "' is 0.00.\nThe calculated cost will be 0.\nContinue?",
                        "Zero Price Warning", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.NO_OPTION) {
                    conn.rollback();
                    return false;
                }
            }

            double cost = qty * price;

            // C. Insert Record
            String sqlLog = "INSERT INTO " + table + " (batchId, resourceId, " + colQty + ", " + colType + ", duration, cost) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstLog = conn.prepareStatement(sqlLog);
            pstLog.setInt(1, batchId);
            pstLog.setInt(2, resId);
            pstLog.setDouble(3, qty);
            // FIX: MySQL JDBC will allow setting a numeric string into a Double column
            pstLog.setString(4, typeVal);
            pstLog.setString(5, duration);
            pstLog.setDouble(6, cost);
            pstLog.executeUpdate();

            // D. Deduct Stock
            PreparedStatement pstUpd = conn.prepareStatement("UPDATE Resource SET currentQty = currentQty - ? WHERE resourceId = ?");
            pstUpd.setDouble(1, qty);
            pstUpd.setInt(2, resId);
            pstUpd.executeUpdate();

            conn.commit(); conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveDyeProcess(int batchId, int resId, String chemName, double volume, String duration) {
        return saveGenericProcess("DyeProcess", "dyeVolume", "chemicalType", batchId, resId, volume, chemName, duration);
    }

    public boolean saveWashProcess(int batchId, int resId, double level, String duration) {
        // ✅ FIXED: Changed "Normal" to "40" (a valid number for waterTemp)
        // Since the database column `waterTemp` is a DOUBLE, it rejects text like "Normal".
        return saveGenericProcess("WashingProcess", "waterLevel", "waterTemp", batchId, resId, level, "40", duration);
    }

    public boolean saveDryProcess(int batchId, int resId, double wood, String type, String duration) {
        return saveGenericProcess("DryingProcess", "woodUsage", "dryingType", batchId, resId, wood, type, duration);
    }

    // --- 4. MONITORING ---
    public void loadProcessHistory(DefaultTableModel model, String filter) {
        model.setRowCount(0);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs;
            if (filter.equals("All") || filter.equals("Dyeing")) {
                rs = stmt.executeQuery("SELECT batchId, 'Dyeing', chemicalType, dyeVolume, COALESCE(cost, 0.0), duration FROM DyeProcess");
                while(rs.next()) model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5), rs.getString(6) });
            }
            if (filter.equals("All") || filter.equals("Washing")) {
                rs = stmt.executeQuery("SELECT batchId, 'Washing', waterTemp, waterLevel, COALESCE(cost, 0.0), duration FROM WashingProcess");
                while(rs.next()) model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5), rs.getString(6) });
            }
            if (filter.equals("All") || filter.equals("Drying")) {
                rs = stmt.executeQuery("SELECT batchId, 'Drying', dryingType, woodUsage, COALESCE(cost, 0.0), duration FROM DryingProcess");
                while(rs.next()) model.addRow(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5), rs.getString(6) });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}