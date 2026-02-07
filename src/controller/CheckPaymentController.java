package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class CheckPaymentController {

    private Connection conn;

    public CheckPaymentController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load data from the 'payments' table
    public void loadPayments(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("paymentId"),
                        rs.getString("orderId"),
                        rs.getDouble("amount"),
                        rs.getString("method"),
                        rs.getString("status"),
                        rs.getTimestamp("payment_date"),
                        rs.getString("transaction_ref")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ UPDATED METHOD: Updates Payment AND Linked Order
    public boolean updatePaymentStatus(int paymentId, String newStatus) {
        PreparedStatement pstUpdatePay = null;
        PreparedStatement pstGetOrder = null;
        PreparedStatement pstUpdateOrder = null;

        try {
            // 1. Start Transaction
            conn.setAutoCommit(false);

            // 2. Update Payment Status in 'payments' table
            String sqlPay = "UPDATE payments SET status = ? WHERE paymentId = ?";
            pstUpdatePay = conn.prepareStatement(sqlPay);
            pstUpdatePay.setString(1, newStatus);
            pstUpdatePay.setInt(2, paymentId);
            int rows = pstUpdatePay.executeUpdate();

            // 3. If Payment is 'Success', also update 'orders' table
            if (rows > 0 && "Success".equalsIgnoreCase(newStatus)) {

                // A. Get the Order Reference (e.g., "ORD-005")
                String sqlGet = "SELECT orderId FROM payments WHERE paymentId = ?";
                pstGetOrder = conn.prepareStatement(sqlGet);
                pstGetOrder.setInt(1, paymentId);
                ResultSet rs = pstGetOrder.executeQuery();

                if (rs.next()) {
                    String orderRef = rs.getString("orderId");

                    // B. Extract Numeric ID (Remove "ORD-")
                    if (orderRef != null && orderRef.startsWith("ORD-")) {
                        try {
                            String numPart = orderRef.replace("ORD-", "");
                            int orderId = Integer.parseInt(numPart);

                            // C. Update Order Status to 'Completed'
                            String sqlOrd = "UPDATE orders SET orderStatus = 'Completed' WHERE orderId = ?";
                            pstUpdateOrder = conn.prepareStatement(sqlOrd);
                            pstUpdateOrder.setInt(1, orderId);
                            pstUpdateOrder.executeUpdate();

                            System.out.println("✅ Order #" + orderId + " marked as Completed.");

                        } catch (NumberFormatException e) {
                            System.err.println("⚠️ Could not parse Order ID: " + orderRef);
                        }
                    }
                }
            }

            // 4. Commit Transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}