package controller;

import db.DatabaseConnection;
import utils.GoogleDriveService;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Random;

public class ReceiptController {

    private Connection conn;
    private GoogleDriveService driveService;

    public ReceiptController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            driveService = new GoogleDriveService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateRef() {
        return "RCPT-" + (10000 + new Random().nextInt(90000));
    }

    // 1. LOAD BOTH STOCK TRANSACTIONS AND WEB ORDERS
    public HashMap<String, String[]> getStockTransactions() {
        HashMap<String, String[]> map = new HashMap<>();

        // Logic:
        // 1. Get Normal Stock Transactions (ID is positive)
        // 2. Get Accepted Web PO Items (ID is negative, to differentiate)
        String sql =
                "SELECT st.transactionId AS id, r.resourceName, st.quantity, (st.quantity * r.unitPrice) AS totalCost, 'Stock' as source " +
                        "FROM stock_transaction st " +
                        "JOIN resource r ON st.resourceId = r.resourceId " +
                        "LEFT JOIN receipt_log rl ON st.transactionId = rl.transactionId " +
                        "WHERE st.transactionType = 'IN' AND rl.receiptId IS NULL " +

                        "UNION ALL " +

                        "SELECT -(poi.orderItemId) AS id, poi.resourceName, poi.quantity, (poi.quantity * poi.unitPrice) AS totalCost, 'Web Order' as source " +
                        "FROM purchaseorderitem poi " +
                        "JOIN purchaseorder po ON poi.purchaseOrderId = po.purchaseOrderId " +
                        "WHERE po.status = 'Accepted' " +
                        // Ensure we haven't already processed this specific PO Item (Optional check, but good for safety)
                        "AND NOT EXISTS (SELECT 1 FROM stock_transaction st2 WHERE st2.quantity = poi.quantity AND st2.transactionDate = po.orderDate)";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("resourceName");
                double qty = rs.getDouble("quantity");
                double cost = rs.getDouble("totalCost");
                String source = rs.getString("source");

                // Label shows source so you know which is which
                String label = String.format("[%s] %s | Qty: %.1f", source, name, qty);

                // Store ID and Cost. (Negative ID tells processReceipt it's a Web Order)
                map.put(label, new String[]{String.valueOf(id), String.valueOf(cost)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // 2. LOAD HISTORY (Unchanged)
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

    // 3. PROCESS RECEIPT (NOW HANDLES WEB ORDERS)
    public boolean processReceipt(String ref, int transId, String details, java.util.Date date, double cost, File file) {
        String driveLink = "No File Uploaded";

        // Upload File
        if (file != null && file.exists()) {
            System.out.println("Starting upload for: " + file.getName());
            String uploadedLink = driveService.uploadFile(file);
            if (uploadedLink != null) driveLink = uploadedLink;
        }

        try {
            int finalTransactionId = transId;

            // 🛑 CHECK: Is this a Web Order? (Negative ID)
            if (transId < 0) {
                // Yes! We must convert it to a Stock Transaction first.
                int poItemId = Math.abs(transId);
                finalTransactionId = convertPOToStockTransaction(poItemId);

                if (finalTransactionId == -1) return false; // Conversion failed
            }

            // Normal Receipt Insert
            String sql = "INSERT INTO receipt_log (refNo, transactionId, stockDetails, receiptDate, cost, driveLink) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, ref);
            pst.setInt(2, finalTransactionId);
            pst.setString(3, details);
            pst.setDate(4, new java.sql.Date(date.getTime()));
            pst.setDouble(5, cost);
            pst.setString(6, driveLink);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🚀 NEW HELPER: Converts Web Order -> Stock Transaction
    private int convertPOToStockTransaction(int poItemId) {
        try {
            // 1. Get PO Item Details
            String sqlGet = "SELECT resourceName, quantity FROM purchaseorderitem WHERE orderItemId = ?";
            PreparedStatement pstGet = conn.prepareStatement(sqlGet);
            pstGet.setInt(1, poItemId);
            ResultSet rs = pstGet.executeQuery();

            if (!rs.next()) return -1;

            String resName = rs.getString("resourceName");
            double qty = rs.getDouble("quantity");

            // 2. Get Resource ID from Name
            String sqlRes = "SELECT resourceId FROM resource WHERE resourceName = ?";
            PreparedStatement pstRes = conn.prepareStatement(sqlRes);
            pstRes.setString(1, resName);
            ResultSet rsRes = pstRes.executeQuery();

            if (!rsRes.next()) return -1; // Resource mismatch
            int resourceId = rsRes.getInt("resourceId");

            // 3. Insert into Stock Transaction
            String sqlInsert = "INSERT INTO stock_transaction (resourceId, transactionType, quantity, status, transactionDate) VALUES (?, 'IN', ?, 'Completed', NOW())";
            PreparedStatement pstIns = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            pstIns.setInt(1, resourceId);
            pstIns.setDouble(2, qty);
            pstIns.executeUpdate();

            // 4. Return the NEW Transaction ID
            ResultSet keys = pstIns.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}