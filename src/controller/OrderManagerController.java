package controller;

import db.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Date;
import java.util.LinkedHashMap;

public class OrderManagerController {

    private Connection conn;

    public OrderManagerController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 1. GET TOTAL COST FOR A SPECIFIC BATCH (Helper) ---
    public double getBatchCost(int batchId) {
        double totalCost = 0.0;
        try {
            // Sum costs from all 3 process tables for this batch
            String costSql = "SELECT " +
                    "(SELECT COALESCE(SUM(cost), 0) FROM dyeprocess WHERE batchId = ?) + " +
                    "(SELECT COALESCE(SUM(cost), 0) FROM washingprocess WHERE batchId = ?) + " +
                    "(SELECT COALESCE(SUM(cost), 0) FROM dryingprocess WHERE batchId = ?) AS grand_total";

            PreparedStatement pst = conn.prepareStatement(costSql);
            pst.setInt(1, batchId);
            pst.setInt(2, batchId);
            pst.setInt(3, batchId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                totalCost = rs.getDouble("grand_total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalCost;
    }

    // --- 2. GET COST BY ORDER ID (Wrapper) ---
    public double getOrderTotalCost(int orderId) {
        int batchId = getAssignedBatchId(orderId);
        if (batchId != -1) return getBatchCost(batchId);
        return 0.0;
    }

    // --- 3. LOAD ORDERS ---
    public void loadOrders(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT o.orderId, c.name AS customerName, o.orderDate, o.expectedDate, " +
                "o.orderStatus, o.totalAmount, o.requestId, cr.description " +
                "FROM Orders o " +
                "JOIN Customer c ON o.customerId = c.customerId " +
                "LEFT JOIN CustomerRequest cr ON o.requestId = cr.requestId " +
                "ORDER BY o.orderDate DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("orderId"),
                        rs.getString("customerName"),
                        rs.getDate("orderDate"),
                        rs.getDate("expectedDate"),
                        rs.getString("orderStatus"),
                        rs.getDouble("totalAmount"), // This will now be correct
                        rs.getInt("requestId"),
                        rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 4. UPDATE ORDER (Fixed Logic) ---
    public boolean updateOrder(int orderId, String status, Date expectedDate, int targetBatchId) {
        // Note: Removed 'double total' parameter because we calculate it fresh
        String sqlUpdateOrder = "UPDATE Orders SET orderStatus = ?, expectedDate = ? WHERE orderId = ?";
        String sqlUpdateCost = "UPDATE Orders SET totalAmount = ? WHERE orderId = ?";

        try {
            conn.setAutoCommit(false);

            // A. Update Status & Date
            PreparedStatement pst = conn.prepareStatement(sqlUpdateOrder);
            pst.setString(1, status);
            if (expectedDate != null) pst.setDate(2, new java.sql.Date(expectedDate.getTime()));
            else pst.setNull(2, Types.DATE);
            pst.setInt(3, orderId);
            pst.executeUpdate();

            // B. HANDLE BATCH ASSIGNMENT
            int finalBatchId = -1;

            if (targetBatchId != -1) {
                // Explicit Assignment
                assignOrderToBatch(orderId, targetBatchId);
                finalBatchId = targetBatchId;
            } else if (status.equalsIgnoreCase("Processing")) {
                // Auto-Create if needed
                createBatchFromOrder(orderId);
                finalBatchId = getAssignedBatchId(orderId);
            } else {
                // Keep existing batch if any
                finalBatchId = getAssignedBatchId(orderId);
            }

            // C. RECALCULATE & SAVE COST (The Fix)
            // Even if the user didn't type it, we fetch the real cost from the batch now
            double freshCost = 0.0;
            if (finalBatchId != -1) {
                freshCost = getBatchCost(finalBatchId);
            }

            PreparedStatement pstCost = conn.prepareStatement(sqlUpdateCost);
            pstCost.setDouble(1, freshCost);
            pstCost.setInt(2, orderId);
            pstCost.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return false;
        }
    }

    // Helper: Link existing batch
    private void assignOrderToBatch(int orderId, int batchId) throws SQLException {
        // 1. Clear old links
        PreparedStatement pstUnlink = conn.prepareStatement("UPDATE productionbatchrecord SET orderId = NULL WHERE orderId = ?");
        pstUnlink.setInt(1, orderId);
        pstUnlink.executeUpdate();

        // 2. Set new link
        PreparedStatement pstLink = conn.prepareStatement("UPDATE productionbatchrecord SET orderId = ? WHERE batchId = ?");
        pstLink.setInt(1, orderId);
        pstLink.setInt(2, batchId);
        int rows = pstLink.executeUpdate();

        if (rows > 0) {
            JOptionPane.showMessageDialog(null, "✅ Batch #" + batchId + " Assigned Successfully!");
        }
    }

    // Helper: Auto-Create
    private void createBatchFromOrder(int orderId) throws SQLException {
        PreparedStatement pstCheck = conn.prepareStatement("SELECT batchId FROM productionbatchrecord WHERE orderId = ?");
        pstCheck.setInt(1, orderId);
        if (pstCheck.executeQuery().next()) return;

        String prodName = "Custom Order";
        PreparedStatement pstName = conn.prepareStatement("SELECT productName FROM OrderItem WHERE orderId = ? LIMIT 1");
        pstName.setInt(1, orderId);
        ResultSet rsName = pstName.executeQuery();
        if (rsName.next()) prodName = rsName.getString("productName");

        String insertSql = "INSERT INTO productionbatchrecord (productName, startDate, status, orderId) VALUES (?, NOW(), 'In Progress', ?)";
        PreparedStatement pstInsert = conn.prepareStatement(insertSql);
        pstInsert.setString(1, prodName);
        pstInsert.setInt(2, orderId);
        pstInsert.executeUpdate();

        JOptionPane.showMessageDialog(null, "✅ New Production Batch Created!");
    }

    public LinkedHashMap<String, Integer> getAvailableBatches(int currentOrderId) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("✨ Auto-Create / No Change", -1);

        String sql = "SELECT batchId, productName, orderId FROM productionbatchrecord " +
                "WHERE (orderId IS NULL AND status != 'Completed') OR orderId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, currentOrderId);
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                int bId = rs.getInt("batchId");
                String name = rs.getString("productName");
                int linkedOrder = rs.getInt("orderId");
                String label = (linkedOrder == currentOrderId && linkedOrder != 0) ?
                        "✅ Current: Batch #" + bId + " - " + name :
                        "Batch #" + bId + " - " + name;
                map.put(label, bId);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public int getAssignedBatchId(int orderId) {
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT batchId FROM productionbatchrecord WHERE orderId = ?");
            pst.setInt(1, orderId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("batchId");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public String getOrderDetails(int orderId) {
        StringBuilder details = new StringBuilder();
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT productName, quantity FROM OrderItem WHERE orderId = ?");
            pst.setInt(1, orderId);
            ResultSet rs = pst.executeQuery();
            while(rs.next()) details.append("• ").append(rs.getString("productName")).append(" (Qty: ").append(rs.getDouble("quantity")).append(")\n");
        } catch (SQLException e) { e.printStackTrace(); }
        return details.toString();
    }

    public boolean deleteOrder(int orderId) {
        try {
            // Unlink batch first
            PreparedStatement pstUnlink = conn.prepareStatement("UPDATE productionbatchrecord SET orderId = NULL WHERE orderId = ?");
            pstUnlink.setInt(1, orderId);
            pstUnlink.executeUpdate();

            PreparedStatement pst = conn.prepareStatement("DELETE FROM Orders WHERE orderId = ?");
            pst.setInt(1, orderId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Cannot delete: " + e.getMessage());
            return false;
        }
    }
}