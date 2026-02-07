package controller;

import db.DatabaseConnection;
import model.Supplier;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierController {

    private Connection conn;

    public SupplierController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database connection failed.");
            e.printStackTrace();
        }
    }

    public boolean addSupplier(String company, String name, String phone, String email) {
        // 1. Fixed SQL: We hard-code default/empty values for the required Bank/Remarks columns
        //    bankName='', accountNo=0, branch='', remarks='Pending'
        String sql = "INSERT INTO supplier (company, name, phone, email, bankName, accountNo, branch, remarks, password) " +
                "VALUES (?, ?, ?, ?, '', 0, '', 'Pending', '123')";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            // 2. We only set the 4 parameters you actually have
            pst.setString(1, company);
            pst.setString(2, name);
            pst.setString(3, phone);
            pst.setString(4, email);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding Supplier: " + e.getMessage());
            return false;
        }
    }

    // --- USE CASE 2: UPDATE SUPPLIER ---
    public boolean updateSupplier(int id, String company, String name, String phone, String email) {
        String sql = "UPDATE Supplier SET company=?, name=?, phone=?, email=? WHERE supplierId=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, company);
            pst.setString(2, name);
            pst.setString(3, phone);
            pst.setString(4, email);
            pst.setInt(5, id);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating Supplier: " + e.getMessage());
            return false;
        }
    }

    // --- USE CASE 3: DELETE SUPPLIER ---
    public boolean deleteSupplier(int id) {
        String sql = "DELETE FROM Supplier WHERE supplierId=?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Cannot delete supplier.\nThis supplier is linked to other records."
            );
            return false;
        }
    }

    // --- USE CASE 4: GET ALL SUPPLIERS ---
    public List<Supplier> getAllSuppliers() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Supplier ORDER BY supplierId ASC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Supplier s = new Supplier(
                        rs.getInt("supplierId"),
                        rs.getString("company"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email")
                );
                list.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
