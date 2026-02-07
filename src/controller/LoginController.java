package controller;

import db.DatabaseConnection;
import model.User; // IMPORT THE NEW MODEL
import java.sql.*;
import javax.swing.JOptionPane;

public class LoginController {

    public User authenticateUser(String email, String password) {
        User user = null;
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM User WHERE email = ? AND password = ? AND status = 'Active'";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("userId"),
                        rs.getInt("roleId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password"),
                        rs.getString("status")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Login Error: " + e.getMessage());
        }
        return user;
    }
}