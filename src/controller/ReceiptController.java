package controller;

import db.DatabaseConnection;
import utils.GoogleDriveService; // Import your new class
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Random;

public class ReceiptController {

    private Connection conn;
    private GoogleDriveService driveService; // Add the service instance

    public ReceiptController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Initialize Drive Service once when Controller starts
            driveService = new GoogleDriveService();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateRef() {
        return "RCPT-" + (10000 + new Random().nextInt(90000));
    }

    // --- (Keep getStockTransactions and loadReceiptHistory same as before) ---
    public HashMap<String, String[]> getStockTransactions() {
        HashMap<String, String[]> map = new HashMap<>();
        String sql = "SELECT st.transactionId, r.resourceName, st.quantity, (st.quantity * r.unitPrice) AS totalCost " +
                "FROM stock_transaction st " +
                "JOIN resource r ON st.resourceId = r.resourceId " +
                "LEFT JOIN receipt_log rl ON st.transactionId = rl.transactionId " +
                "WHERE st.transactionType = 'IN' " +
                "AND rl.receiptId IS NULL";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("transactionId"));
                String name = rs.getString("resourceName");
                double qty = rs.getDouble("quantity");
                double cost = rs.getDouble("totalCost");
                String label = "Trans #" + id + " | " + name + " (Qty: " + qty + ")";
                map.put(label, new String[]{id, String.valueOf(cost)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void loadReceiptHistory(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM receipt_log ORDER BY receiptDate DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("refNo"),
                        rs.getString("stockDetails"),
                        rs.getDate("receiptDate"),
                        rs.getDouble("cost"),
                        rs.getString("driveLink")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // -----------------------------------------------------------------------

    // ✅ FIXED: Uses GoogleDriveService to upload real file
    public boolean processReceipt(String ref, int transId, String details, java.util.Date date, double cost, File file) {

        String driveLink = "No File Uploaded"; // Default value

        // 1. Upload to Google Drive if a file is provided
        if (file != null && file.exists()) {
            System.out.println("Starting upload for: " + file.getName());

            // Call your new utility class
            String uploadedLink = driveService.uploadFile(file);

            if (uploadedLink != null) {
                driveLink = uploadedLink; // Use the REAL link from Google
            } else {
                System.err.println("Upload failed, saving without link.");
            }
        }

        // 2. Insert into Database
        String sql = "INSERT INTO receipt_log (refNo, transactionId, stockDetails, receiptDate, cost, driveLink) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, ref);
            pst.setInt(2, transId);
            pst.setString(3, details);
            pst.setDate(4, new java.sql.Date(date.getTime()));
            pst.setDouble(5, cost);
            pst.setString(6, driveLink); // Saves the valid link (e.g., https://drive.google.com/...)

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}