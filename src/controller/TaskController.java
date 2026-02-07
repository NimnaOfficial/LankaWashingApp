package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.LinkedHashMap;

public class TaskController {

    private Connection conn;

    public TaskController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 1. GET FAULTS FOR DROPDOWN ---
    public LinkedHashMap<String, Integer> getFaultMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        // Join to show Machine Type + Fault Description
        String sql = "SELECT f.faultId, f.description, m.machineType, f.severity " +
                "FROM FaultLog f " +
                "JOIN Machine m ON f.machineId = m.machineId " +
                "ORDER BY f.faultId DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String label = rs.getInt("faultId") + " - " + rs.getString("machineType") +
                        " (" + rs.getString("description") + ")";
                map.put(label, rs.getInt("faultId"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // --- 2. GET TECHNICIANS FOR DROPDOWN ---
    public LinkedHashMap<String, Integer> getTechnicianMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT technicianId, name, availabilityStatus FROM Technician";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String label = rs.getString("name") + " (" + rs.getString("availabilityStatus") + ")";
                map.put(label, rs.getInt("technicianId"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // 3. ADD TASK (Now includes 'parts')
    public boolean addTask(int faultId, int techId, String details, String parts, double cost, String date) {
        String sql = "INSERT INTO Task (faultId, technicianId, repairDetails, partsUsed, cost, date) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, faultId);
            pst.setInt(2, techId);
            pst.setString(3, details);
            pst.setString(4, parts); // ✅ Added Parts
            pst.setDouble(5, cost);
            pst.setString(6, date);

            // Auto-update technician to Busy (Optional but good for logic)
            updateTechStatus(techId, "Busy");

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Task: " + e.getMessage());
            return false;
        }
    }

    // 4. UPDATE TASK
    public boolean updateTask(int id, int faultId, int techId, String details, String parts, double cost, String date) {
        String sql = "UPDATE Task SET faultId=?, technicianId=?, repairDetails=?, partsUsed=?, cost=?, date=? WHERE taskId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, faultId);
            pst.setInt(2, techId);
            pst.setString(3, details);
            pst.setString(4, parts); // ✅ Added Parts
            pst.setDouble(5, cost);
            pst.setString(6, date);
            pst.setInt(7, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating: " + e.getMessage());
            return false;
        }
    }

    // 5. DELETE TASK
    public boolean deleteTask(int id) {
        String sql = "DELETE FROM Task WHERE taskId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // 6. LOAD TASKS
    public void loadTasks(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT t.taskId, t.faultId, f.description, m.machineType, t.technicianId, tech.name, " +
                "t.repairDetails, t.partsUsed, t.cost, t.date " +
                "FROM Task t " +
                "LEFT JOIN FaultLog f ON t.faultId = f.faultId " +
                "LEFT JOIN Machine m ON f.machineId = m.machineId " +
                "LEFT JOIN Technician tech ON t.technicianId = tech.technicianId " +
                "ORDER BY t.date DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String faultLabel = rs.getInt("faultId") + " - " + rs.getString("machineType");

                model.addRow(new Object[]{
                        rs.getInt("taskId"),
                        faultLabel,
                        rs.getString("name"),
                        rs.getString("repairDetails"),
                        rs.getString("partsUsed"), // ✅ Added Column
                        rs.getDouble("cost"),
                        rs.getDate("date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateTechStatus(int id, String status) {
        try {
            PreparedStatement pst = conn.prepareStatement("UPDATE Technician SET availabilityStatus=? WHERE technicianId=?");
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {}
    }
}