package controller;

import db.DatabaseConnection;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class UserController {

    private Connection conn;

    public UserController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Failed!");
        }
    }

    // =========================================================================
    // 1. ADD USER (With Automatic Technician Profile Creation)
    // =========================================================================
    public boolean addUser(String name, String email, String phone, String password, int roleId, String status) {
        String sqlUser = "INSERT INTO User (name, email, phone, password, roleId, status) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            // [A] Start Transaction: Turn off auto-save
            conn.setAutoCommit(false);

            // [B] Insert into User Table
            PreparedStatement pst = conn.prepareStatement(sqlUser);
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, phone);
            pst.setString(4, password);
            pst.setInt(5, roleId);
            pst.setString(6, status);
            int rows = pst.executeUpdate();

            // [C] If User added successfully, check if they are a Technician (Role ID 6)
            if (rows > 0) {
                if (roleId == 6) {
                    // Check if profile already exists to prevent duplicates
                    if (!checkTechnicianExists(name)) {
                        String sqlTech = "INSERT INTO Technician (name, availabilityStatus) VALUES (?, 'Available')";
                        PreparedStatement pstTech = conn.prepareStatement(sqlTech);
                        pstTech.setString(1, name);
                        pstTech.executeUpdate();
                    }
                }

                // [D] Commit Transaction: Save everything
                conn.commit();
                return true;
            } else {
                conn.rollback(); // Undo if User insert failed
                return false;
            }

        } catch (SQLException e) {
            // [E] Rollback on Error: Undo everything if something crashes
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }

            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(null, "Error: Email already exists!");
            } else {
                JOptionPane.showMessageDialog(null, "Error Adding User: " + e.getMessage());
            }
            return false;
        } finally {
            // Reset to default mode
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    // Helper method to check if a technician name already exists
    private boolean checkTechnicianExists(String name) {
        String sql = "SELECT technicianId FROM Technician WHERE name = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // 2. UPDATE USER
    // =========================================================================
    public boolean updateUser(int id, String name, String email, String phone, int roleId, String status) {
        // Note: Password is NOT updated here to prevent accidental resets.
        String sql = "UPDATE User SET name=?, email=?, phone=?, roleId=?, status=? WHERE userId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, phone);
            pst.setInt(4, roleId);
            pst.setString(5, status);
            pst.setInt(6, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating User: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 3. DELETE USER
    // =========================================================================
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM User WHERE userId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Deleting User: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 4. LOAD USERS (For View Table)
    // =========================================================================
    public void loadUsers(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing data

        // Joins User and Role tables to show 'Technician' instead of '6'
        String sql = "SELECT u.userId, u.name, u.email, u.phone, r.roleName, u.status, u.roleId " +
                "FROM User u " +
                "JOIN Role r ON u.roleId = r.roleId " +
                "ORDER BY u.userId ASC";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("roleName"),
                        rs.getString("status"),
                        rs.getInt("roleId") // Hidden column used for dropdown selection
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error Loading Users: " + e.getMessage());
        }
    }
}