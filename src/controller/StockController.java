package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.HashMap;

public class StockController {

    private Connection conn;

    public StockController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. GET RESOURCES (For Dropdown)
    public HashMap<String, Integer> getResourceMap() {
        HashMap<String, Integer> map = new HashMap<>();
        String sql = "SELECT resourceId, resourceName FROM Resource";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                map.put(rs.getString("resourceName"), rs.getInt("resourceId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // 2. GET CURRENT QTY
    public double getCurrentQty(int resourceId) {
        String sql = "SELECT currentQty FROM Resource WHERE resourceId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, resourceId);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) return rs.getDouble("currentQty");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // 3. PROCESS TRANSACTION (IN/OUT)
    public boolean processTransaction(int resourceId, String type, double qty, double unitPrice) {
        try {
            conn.setAutoCommit(false);

            // Validation: Cannot withdraw more than available
            if (type.equals("OUT")) {
                double current = getCurrentQty(resourceId);
                if (current < qty) {
                    JOptionPane.showMessageDialog(null, "Error: Insufficient Stock! Current: " + current);
                    return false;
                }
            }

            // Log Transaction (✅ Added 'status' = 'Completed')
            String sqlLog = "INSERT INTO Stock_Transaction (resourceId, transactionType, quantity, status, transactionDate) VALUES (?, ?, ?, 'Pending', NOW())";
            PreparedStatement pstLog = conn.prepareStatement(sqlLog);
            pstLog.setInt(1, resourceId);
            pstLog.setString(2, type);
            pstLog.setDouble(3, qty);
            pstLog.executeUpdate();

            // Update Resource Table (Qty & Price)
            String sqlUpdate;
            PreparedStatement pstUpd;

            if (type.equals("IN")) {
                // For IN: Update Quantity AND Unit Price (Latest Market Price)
                sqlUpdate = "UPDATE Resource SET currentQty = currentQty + ?, unitPrice = ? WHERE resourceId = ?";
                pstUpd = conn.prepareStatement(sqlUpdate);
                pstUpd.setDouble(1, qty);
                pstUpd.setDouble(2, unitPrice);
                pstUpd.setInt(3, resourceId);
            } else {
                // For OUT: Just deduct Quantity
                sqlUpdate = "UPDATE Resource SET currentQty = currentQty - ? WHERE resourceId = ?";
                pstUpd = conn.prepareStatement(sqlUpdate);
                pstUpd.setDouble(1, qty);
                pstUpd.setInt(2, resourceId);
            }
            pstUpd.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    // 4. LOAD STOCK HISTORY (✅ Added Status Column)
    public void loadHistory(DefaultTableModel model) {
        model.setRowCount(0);
        // Fetches 'status' from database
        String sql = "SELECT t.transactionId, t.transactionDate, r.resourceName, t.transactionType, t.quantity, t.status, r.unitPrice " +
                "FROM Stock_Transaction t JOIN Resource r ON t.resourceId = r.resourceId " +
                "ORDER BY t.transactionDate DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                double qty = rs.getDouble("quantity");
                double price = rs.getDouble("unitPrice");
                double total = qty * price; // Estimated Value

                model.addRow(new Object[]{
                        rs.getInt("transactionId"),
                        rs.getString("transactionDate"),
                        rs.getString("resourceName"),
                        rs.getString("transactionType"),
                        qty,
                        rs.getString("status"), // ✅ Load Status from DB
                        String.format("%.2f", total)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. LOAD OPERATOR USAGE & COST (Tab 2)
    public void loadOperatorUsage(DefaultTableModel model) {
        model.setRowCount(0);
        String sql =
                "SELECT 'Dyeing' as Process, d.batchId, r.resourceName, d.dyeVolume as Qty, d.cost " +
                        "FROM DyeProcess d JOIN Resource r ON d.resourceId = r.resourceId " +
                        "UNION ALL " +
                        "SELECT 'Washing', w.batchId, r.resourceName, w.waterLevel, w.cost " +
                        "FROM WashingProcess w JOIN Resource r ON w.resourceId = r.resourceId " +
                        "UNION ALL " +
                        "SELECT 'Drying', dr.batchId, r.resourceName, dr.woodUsage, dr.cost " +
                        "FROM DryingProcess dr JOIN Resource r ON dr.resourceId = r.resourceId " +
                        "ORDER BY batchId DESC";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                double cost = rs.getDouble("cost");
                model.addRow(new Object[]{
                        rs.getString("Process"),
                        rs.getInt("batchId"),
                        rs.getString("resourceName"),
                        rs.getDouble("Qty"),
                        String.format("%.2f", cost)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 6. PLACE PURCHASE ORDER (Fixes the "Delivery vs PO" issue)
    public boolean placeOrder(int resourceId, double qty, double unitPrice, String expectedDate) {
        try {
            conn.setAutoCommit(false);

            // Step 1: Get Supplier ID and Resource Name based on the selected Resource ID
            int supplierId = -1;
            String resourceName = "";

            String sqlGetInfo = "SELECT supplierId, resourceName FROM Resource WHERE resourceId = ?";
            PreparedStatement pstInfo = conn.prepareStatement(sqlGetInfo);
            pstInfo.setInt(1, resourceId);
            ResultSet rs = pstInfo.executeQuery();

            if (rs.next()) {
                supplierId = rs.getInt("supplierId");
                resourceName = rs.getString("resourceName");
            } else {
                return false; // Resource not found
            }

            // Step 2: Insert into purchaseorder table (The "Header")
            String sqlPO = "INSERT INTO purchaseorder (supplierId, orderDate, expectedDate, status) VALUES (?, NOW(), ?, 'Pending')";
            PreparedStatement pstPO = conn.prepareStatement(sqlPO, Statement.RETURN_GENERATED_KEYS);
            pstPO.setInt(1, supplierId);
            pstPO.setString(2, expectedDate); // Format: YYYY-MM-DD
            pstPO.executeUpdate();

            // Get the generated PO ID
            ResultSet generatedKeys = pstPO.getGeneratedKeys();
            int newPOId = 0;
            if (generatedKeys.next()) {
                newPOId = generatedKeys.getInt(1);
            }

            // Step 3: Insert into purchaseorderitem table (The "Items")
            String sqlItem = "INSERT INTO purchaseorderitem (purchaseOrderId, resourceName, unitPrice, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement pstItem = conn.prepareStatement(sqlItem);
            pstItem.setInt(1, newPOId);
            pstItem.setString(2, resourceName);
            pstItem.setDouble(3, unitPrice);
            pstItem.setDouble(4, qty);
            pstItem.executeUpdate();

            // Note: We do NOT update the 'Resource' qty here because the goods haven't arrived yet.

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }
}