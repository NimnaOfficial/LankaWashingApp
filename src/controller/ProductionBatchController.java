package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;

public class ProductionBatchController {

    private Connection conn;

    public ProductionBatchController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ FIXED: Calculates Live Cost from Process Tables
    public void loadBatches(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT pbr.batchId, pbr.orderId, pbr.productId, pbr.productName, pbr.startDate, pbr.endDate, pbr.status, " +
                "(" +
                "  COALESCE((SELECT SUM(cost) FROM dyeprocess WHERE batchId = pbr.batchId), 0) + " +
                "  COALESCE((SELECT SUM(cost) FROM washingprocess WHERE batchId = pbr.batchId), 0) + " +
                "  COALESCE((SELECT SUM(cost) FROM dryingprocess WHERE batchId = pbr.batchId), 0) " +
                ") AS liveTotalCost " +
                "FROM productionbatchrecord pbr ORDER BY pbr.batchId DESC";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int ordId = rs.getInt("orderId");
                String ordStr = (ordId == 0) ? "Manual" : "Ord #" + ordId;

                model.addRow(new Object[]{
                        rs.getInt("batchId"),
                        ordStr,
                        rs.getString("productName"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getDouble("liveTotalCost"), // ✅ Shows calculated cost
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ FIXED: Removed 'cost' parameter (it's calculated, not entered)
    public boolean addBatch(String prodId, String name, String sDate, String eDate, String status) {
        String sql = "INSERT INTO productionbatchrecord (productId, productName, startDate, endDate, status) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, prodId);
            pst.setString(2, name);
            pst.setString(3, sDate);
            if(eDate.isEmpty()) pst.setNull(4, Types.DATE); else pst.setString(4, eDate);
            pst.setString(5, status);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ FIXED: Removed 'cost' parameter
    public boolean updateBatch(int id, String prodId, String name, String sDate, String eDate, String status) {
        String sql = "UPDATE productionbatchrecord SET productId=?, productName=?, startDate=?, endDate=?, status=? WHERE batchId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, prodId);
            pst.setString(2, name);
            pst.setString(3, sDate);
            if(eDate.isEmpty()) pst.setNull(4, Types.DATE); else pst.setString(4, eDate);
            pst.setString(5, status);
            pst.setInt(6, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteBatch(int id) {
        try {
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM dyeprocess WHERE batchId=" + id);
            stmt.executeUpdate("DELETE FROM washingprocess WHERE batchId=" + id);
            stmt.executeUpdate("DELETE FROM dryingprocess WHERE batchId=" + id);

            int rows = stmt.executeUpdate("DELETE FROM productionbatchrecord WHERE batchId=" + id);

            if(rows > 0) {
                conn.commit();
                JOptionPane.showMessageDialog(null, "Batch and related logs deleted.");
            } else {
                conn.rollback();
            }
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error Deleting Batch: " + e.getMessage());
        }
    }
}