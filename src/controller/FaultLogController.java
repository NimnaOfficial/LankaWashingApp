package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class FaultLogController {

    private Connection conn;

    public FaultLogController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 1. GET MACHINES FOR DROPDOWN ---
    // Returns: "101 - Dyeing Machine A" -> 101
    public HashMap<String, Integer> getMachineMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT machineId, machineType FROM Machine";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String label = rs.getInt("machineId") + " - " + rs.getString("machineType");
                map.put(label, rs.getInt("machineId"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // 2. ADD FAULT
    public boolean addFault(int machineId, String date, String severity, String desc) {
        String sql = "INSERT INTO FaultLog (machineId, reportedDate, severity, description) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, machineId);
            pst.setString(2, date);
            pst.setString(3, severity);
            pst.setString(4, desc);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Fault: " + e.getMessage());
            return false;
        }
    }

    // 3. UPDATE FAULT
    public boolean updateFault(int id, int machineId, String date, String severity, String desc) {
        String sql = "UPDATE FaultLog SET machineId=?, reportedDate=?, severity=?, description=? WHERE faultId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, machineId);
            pst.setString(2, date);
            pst.setString(3, severity);
            pst.setString(4, desc);
            pst.setInt(5, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating: " + e.getMessage());
            return false;
        }
    }

    // 4. DELETE FAULT
    public boolean deleteFault(int id) {
        String sql = "DELETE FROM FaultLog WHERE faultId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // 5. LOAD FAULTS (With Machine Names)
    public void loadFaults(DefaultTableModel model) {
        model.setRowCount(0);
        // Join with Machine table to show names instead of IDs
        String sql = "SELECT f.faultId, f.machineId, m.machineType, f.reportedDate, f.severity, f.description " +
                "FROM FaultLog f " +
                "LEFT JOIN Machine m ON f.machineId = m.machineId " +
                "ORDER BY f.reportedDate DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String machineLabel = rs.getInt("machineId") + " - " + rs.getString("machineType");

                model.addRow(new Object[]{
                        rs.getInt("faultId"),
                        machineLabel, // Display "101 - Dyeing Machine"
                        rs.getDate("reportedDate"),
                        rs.getString("severity"),
                        rs.getString("description")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}