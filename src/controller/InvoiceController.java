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
        // Added total_amount to the SELECT query
        String sql = "SELECT id, po_reference, invoice_number, invoice_date, totvani@tech.comal_amount, status, file_link FROM invoices ORDER BY created_at DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),                // 0: ID
                        rs.getString("po_reference"),   // 1: PO Ref
                        rs.getString("invoice_number"), // 2: Invoice #
                        rs.getDate("invoice_date"),     // 3: Date
                        rs.getDouble("total_amount"),   // 4: ✅ Added Amount back
                        rs.getString("status"),         // 5: Status
                        rs.getString("file_link")       // 6: Link
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
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