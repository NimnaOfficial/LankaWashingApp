package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class InvoiceController {
    private Connection conn;

    public InvoiceController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadInvoices(DefaultTableModel model) {
        model.setRowCount(0);

        // ✅ FIXED: Added 'po_reference' to the SELECT statement
        String sql = "SELECT id, supplier_id, po_reference, invoice_number, invoice_date, total_amount, status, file_link FROM invoices ORDER BY created_at DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("po_reference"),   // Now this will work!
                        rs.getString("invoice_number"),
                        rs.getDate("invoice_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getString("file_link")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE invoices SET status = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newStatus);
            pst.setInt(2, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}