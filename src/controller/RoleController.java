package controller;

import db.DatabaseConnection;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class RoleController {

    private Connection conn;

    public RoleController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 1. ADD ROLE
    public boolean addRole(String roleName, String permissions) {
        String sql = "INSERT INTO Role (roleName, permission) VALUES (?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, roleName);
            pst.setString(2, permissions);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Role: " + e.getMessage());
            return false;
        }
    }

    // 2. UPDATE ROLE
    public boolean updateRole(int roleId, String roleName, String permissions) {
        String sql = "UPDATE Role SET roleName=?, permission=? WHERE roleId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, roleName);
            pst.setString(2, permissions);
            pst.setInt(3, roleId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating Role: " + e.getMessage());
            return false;
        }
    }

    // 3. DELETE ROLE
    public boolean deleteRole(int roleId) {
        // Prevent deleting Admin (ID 1)
        if (roleId == 1) {
            JOptionPane.showMessageDialog(null, "Cannot delete the System Admin role!", "Security Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "DELETE FROM Role WHERE roleId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, roleId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Deleting Role (Check if users are assigned to it): " + e.getMessage());
            return false;
        }
    }

    // 4. LOAD ROLES
    public void loadRolesToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM Role";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("roleId"),
                        rs.getString("roleName"),
                        rs.getString("permission")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}