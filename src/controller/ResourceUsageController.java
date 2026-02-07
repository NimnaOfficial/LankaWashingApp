package controller;

import db.DatabaseConnection;
import javax.swing.JOptionPane;
import java.sql.*;

public class ResourceUsageController {

    private Connection conn;

    public ResourceUsageController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * CORE METHOD: Handles safe resource consumption by operators.
     * @param batchId - The production batch (e.g., 101)
     * @param resourceId - The ID of resource being used (e.g., Red Dye ID)
     * @param qtyRequired - How much the operator wants to use
     * @param processType - "Dye", "Wash", or "Dry"
     * @return true if successful, false if blocked (low stock)
     */
    public boolean recordUsage(int batchId, int resourceId, double qtyRequired, String processType) {

        PreparedStatement pstCheck = null, pstUpdate = null, pstLog = null;
        ResultSet rs = null;

        try {
            conn.setAutoCommit(false); // START TRANSACTION (Safety Lock)

            // 1. CHECK STOCK & PRICE
            String checkSql = "SELECT currentQty, unitPrice, resourceName FROM Resource WHERE resourceId = ?";
            pstCheck = conn.prepareStatement(checkSql);
            pstCheck.setInt(1, resourceId);
            rs = pstCheck.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Error: Resource ID " + resourceId + " not found!");
                return false;
            }

            double currentStock = rs.getDouble("currentQty");
            double price = rs.getDouble("unitPrice");
            String resName = rs.getString("resourceName");

            // 2. VALIDATION (The "Stop" Logic)
            if (currentStock < qtyRequired) {
                JOptionPane.showMessageDialog(null,
                        "⚠️ INSUFFICIENT STOCK!\n\n" +
                                "Resource: " + resName + "\n" +
                                "Available: " + currentStock + "\n" +
                                "Required: " + qtyRequired + "\n\n" +
                                "Process cannot continue until stock is replenished.");
                conn.rollback();
                return false;
            }

            // 3. CALCULATE COST & DEDUCT STOCK
            double usageCost = qtyRequired * price;
            double newStock = currentStock - qtyRequired;

            String updateSql = "UPDATE Resource SET currentQty = ? WHERE resourceId = ?";
            pstUpdate = conn.prepareStatement(updateSql);
            pstUpdate.setDouble(1, newStock);
            pstUpdate.setInt(2, resourceId);
            pstUpdate.executeUpdate();

            // 4. SAVE TO SPECIFIC PROCESS TABLE
            String insertSql = "";
            if (processType.equalsIgnoreCase("Dye")) {
                insertSql = "INSERT INTO DyeProcess (batchId, resourceId, dyeVolume, cost, chemicalType, duration) VALUES (?, ?, ?, ?, ?, ?)";
                // Note: You need to pass chemicalName/Duration from UI. Simplified here for logic demo.
                pstLog = conn.prepareStatement(insertSql);
                pstLog.setInt(1, batchId);
                pstLog.setInt(2, resourceId);
                pstLog.setDouble(3, qtyRequired);
                pstLog.setDouble(4, usageCost);
                pstLog.setString(5, resName); // Using Resource Name as Chemical Type
                pstLog.setString(6, "2 Hours"); // Placeholder
            }
            else if (processType.equalsIgnoreCase("Dry")) {
                insertSql = "INSERT INTO DryingProcess (batchId, resourceId, woodUsage, cost, dryingType, duration) VALUES (?, ?, ?, ?, 'Oven', '3 Hours')";
                pstLog = conn.prepareStatement(insertSql);
                pstLog.setInt(1, batchId);
                pstLog.setInt(2, resourceId);
                pstLog.setDouble(3, qtyRequired);
                pstLog.setDouble(4, usageCost);
            }
            // ... Add Washing logic similarly

            if (pstLog != null) pstLog.executeUpdate();

            // 5. COMMIT (Save Everything)
            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(null, "System Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    /**
     * INVENTORY MANAGER METHOD: Updates Price when buying new stock
     */
    public void addStock(int resourceId, double qtyAdded, double newUnitPrice) {
        try {
            // Simple logic: Overwrite old price with new market price
            // Advanced logic: Calculate Weighted Average Cost (optional)
            String sql = "UPDATE Resource SET currentQty = currentQty + ?, unitPrice = ? WHERE resourceId = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setDouble(1, qtyAdded);
            pst.setDouble(2, newUnitPrice);
            pst.setInt(3, resourceId);
            pst.executeUpdate();

            // Log Transaction
            String logSql = "INSERT INTO Stock_Transaction (resourceId, transactionType, quantity, transactionDate) VALUES (?, 'IN', ?, NOW())";
            PreparedStatement pstLog = conn.prepareStatement(logSql);
            pstLog.setInt(1, resourceId);
            pstLog.setDouble(2, qtyAdded);
            pstLog.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}