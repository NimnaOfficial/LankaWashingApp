package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;

public class TechnicianController {

    private Connection conn;

    public TechnicianController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    // SECTION A: MAINTENANCE MANAGER METHODS (Existing CRUD)
    // ==========================================================

    // 1. ADD TECHNICIAN
    public boolean addTechnician(String name, String status) {
        String sql = "INSERT INTO Technician (name, availabilityStatus) VALUES (?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, status);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Technician: " + e.getMessage());
            return false;
        }
    }

    // 2. UPDATE TECHNICIAN
    public boolean updateTechnician(int id, String name, String status) {
        String sql = "UPDATE Technician SET name=?, availabilityStatus=? WHERE technicianId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, status);
            pst.setInt(3, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating: " + e.getMessage());
            return false;
        }
    }

    // 3. DELETE TECHNICIAN
    public boolean deleteTechnician(int id) {
        String sql = "DELETE FROM Technician WHERE technicianId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Cannot delete: Technician may have assigned tasks.");
            return false;
        }
    }

    // 4. LOAD ALL TECHNICIANS (For Manager View)
    public void loadTechnicians(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM Technician";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("technicianId"),
                        rs.getString("name"),
                        rs.getString("availabilityStatus")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    // SECTION B: TECHNICIAN DASHBOARD METHODS (New Features)
    // ==========================================================

    /**
     * Helper: Links the Logged-in 'User' to the 'Technician' table by Name.
     * Since 'User' table and 'Technician' table are separate, we match them by Name.
     */
    public int getTechnicianIdByName(String name) {
        String sql = "SELECT technicianId FROM Technician WHERE name = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("technicianId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Not found
    }

    // 5. LOAD SPECIFIC TASKS (For Technician Dashboard)
    public void loadTechnicianTasks(int techId, DefaultTableModel model) {
        model.setRowCount(0);
        // Joins Task, FaultLog, and Machine to give the technician full details
        String sql = "SELECT t.taskId, m.machineType, f.description, t.date, t.cost " +
                "FROM Task t " +
                "JOIN FaultLog f ON t.faultId = f.faultId " +
                "JOIN Machine m ON f.machineId = m.machineId " +
                "WHERE t.technicianId = ? AND (t.repairDetails IS NULL OR t.repairDetails != 'Completed')";

        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, techId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("taskId"),
                        rs.getString("machineType"),
                        rs.getString("description"),
                        rs.getDate("date"),
                        rs.getDouble("cost")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 6. GET CURRENT STATUS
    public String getTechnicianStatus(int techId) {
        String sql = "SELECT availabilityStatus FROM Technician WHERE technicianId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, techId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("availabilityStatus");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // 7. TOGGLE STATUS (Busy <-> Available)
    public void updateTechnicianStatus(int techId, String status) {
        String sql = "UPDATE Technician SET availabilityStatus = ? WHERE technicianId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, status);
            pst.setInt(2, techId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 8. COMPLETE TASK
    public void completeTask(int taskId) {
        String sql = "UPDATE Task SET repairDetails = 'Completed' WHERE taskId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, taskId);
            pst.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error completing task: " + e.getMessage());
        }
    }
}