package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.HashMap;

public class ResourceController {

    private Connection conn;

    public ResourceController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Integer> getSupplierMap() {
        HashMap<String, Integer> map = new HashMap<>();
        String sql = "SELECT supplierId, company FROM supplier";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                map.put(rs.getString("company"), rs.getInt("supplierId"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public boolean addResource(String name, String category, String unit, double minLevel, int supplierId, double unitPrice) {
        String sql = "INSERT INTO resource (resourceName, category, unit, currentQty, minLevel, supplierId, unitPrice) VALUES (?, ?, ?, 0.0, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setString(3, unit);
            pst.setDouble(4, minLevel);
            pst.setInt(5, supplierId);
            pst.setDouble(6, unitPrice);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Resource: " + e.getMessage());
            return false;
        }
    }

    public boolean updateResource(int id, String name, String category, String unit, double minLevel, int supplierId, double unitPrice) {
        String sql = "UPDATE resource SET resourceName=?, category=?, unit=?, minLevel=?, supplierId=?, unitPrice=? WHERE resourceId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setString(3, unit);
            pst.setDouble(4, minLevel);
            pst.setInt(5, supplierId);
            pst.setDouble(6, unitPrice);
            pst.setInt(7, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteResource(int id) {
        String sql = "DELETE FROM resource WHERE resourceId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Cannot Delete: This material is used in production logs.");
            return false;
        }
    }

    // --- VIEW MATERIALS (With Safe Null Handling) ---
    public void loadResources(DefaultTableModel model) {
        model.setRowCount(0);

        // Uses COALESCE to treat NULLs as 0.0, preventing "empty" cells
        String sql = "SELECT r.resourceId, r.resourceName, r.category, r.unit, " +
                "COALESCE(r.currentQty, 0.0) as currentQty, r.minLevel, " +
                "COALESCE(r.unitPrice, 0.0) as unitPrice, s.company " +
                "FROM resource r LEFT JOIN supplier s ON r.supplierId = s.supplierId";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                double qty = rs.getDouble("currentQty");
                double price = rs.getDouble("unitPrice");
                double totalValue = qty * price;

                model.addRow(new Object[]{
                        rs.getInt("resourceId"),
                        rs.getString("resourceName"),
                        rs.getString("category"),
                        rs.getString("unit"),
                        qty,
                        rs.getDouble("minLevel"),
                        rs.getString("company"),
                        price,
                        totalValue
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}