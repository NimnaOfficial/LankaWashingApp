package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;

public class MachineController {

    private Connection conn;

    public MachineController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. ADD MACHINE
    public boolean addMachine(String type, String lastService, String status) {
        String sql = "INSERT INTO Machine (machineType, lastService, status) VALUES (?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, type);
            pst.setString(2, lastService);
            pst.setString(3, status);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Machine: " + e.getMessage());
            return false;
        }
    }

    // 2. UPDATE MACHINE
    public boolean updateMachine(int id, String type, String lastService, String status) {
        String sql = "UPDATE Machine SET machineType=?, lastService=?, status=? WHERE machineId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, type);
            pst.setString(2, lastService);
            pst.setString(3, status);
            pst.setInt(4, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating: " + e.getMessage());
            return false;
        }
    }

    // 3. DELETE MACHINE
    public boolean deleteMachine(int id) {
        String sql = "DELETE FROM Machine WHERE machineId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Cannot Delete: Machine is linked to Maintenance Tasks.");
            return false;
        }
    }

    // 4. LOAD MACHINES
    public void loadMachines(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM Machine ORDER BY machineId ASC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("machineId"),
                        rs.getString("machineType"),
                        rs.getDate("lastService"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}