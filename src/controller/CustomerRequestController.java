package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.HashMap;

public class CustomerRequestController {

    private Connection conn;

    public CustomerRequestController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 1. LOAD CUSTOMERS (For Dropdown) ---
    public HashMap<String, Integer> getCustomerMap() {
        HashMap<String, Integer> map = new HashMap<>();
        String sql = "SELECT customerId, name FROM Customer";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getInt("customerId"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // --- 2. LOAD REQUESTS (Table Data) ---
    public void loadRequests(DefaultTableModel model) {
        model.setRowCount(0);
        // Joins Customer table to show Names instead of IDs
        String sql = "SELECT r.requestId, c.name, r.productName, r.quantity, r.expectedDate, r.priority, r.status " +
                "FROM CustomerRequest r " +
                "JOIN Customer c ON r.customerId = c.customerId " +
                "ORDER BY r.requestDate DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("requestId"),
                        rs.getString("name"),
                        rs.getString("productName"),
                        rs.getDouble("quantity"),
                        rs.getDate("expectedDate"),
                        rs.getString("priority"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 3. APPROVE REQUEST (Automated Order Creation) ---
    public boolean approveRequest(int requestId) {
        // SQL Queries
        String fetchSql = "SELECT * FROM CustomerRequest WHERE requestId = ?";
        String insertOrderSql = "INSERT INTO Orders (customerId, requestId, orderDate, expectedDate, orderStatus, totalAmount) VALUES (?, ?, NOW(), ?, 'Pending', 0)";
        String insertItemSql = "INSERT INTO OrderItem (orderId, productName, quantity, price, subTotal) VALUES (?, ?, ?, 0, 0)";
        String updateReqSql = "UPDATE CustomerRequest SET status = 'Approved' WHERE requestId = ?";

        PreparedStatement pstFetch = null, pstOrder = null, pstItem = null, pstUpdate = null;
        ResultSet rs = null, rsKeys = null;

        try {
            conn.setAutoCommit(false); // START TRANSACTION

            // A. Fetch the Request Data
            pstFetch = conn.prepareStatement(fetchSql);
            pstFetch.setInt(1, requestId);
            rs = pstFetch.executeQuery();

            if (!rs.next()) return false;

            int custId = rs.getInt("customerId");
            String prodName = rs.getString("productName");
            double qty = rs.getDouble("quantity");
            Date expDate = rs.getDate("expectedDate");

            // B. Create the Order
            pstOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            pstOrder.setInt(1, custId);
            pstOrder.setInt(2, requestId);
            pstOrder.setDate(3, expDate);
            pstOrder.executeUpdate();

            // Get the new Order ID
            rsKeys = pstOrder.getGeneratedKeys();
            int newOrderId = 0;
            if (rsKeys.next()) newOrderId = rsKeys.getInt(1);

            // C. Create the Order Item
            pstItem = conn.prepareStatement(insertItemSql);
            pstItem.setInt(1, newOrderId);
            pstItem.setString(2, prodName);
            pstItem.setDouble(3, qty);
            pstItem.executeUpdate();

            // D. Mark Request as Approved
            pstUpdate = conn.prepareStatement(updateReqSql);
            pstUpdate.setInt(1, requestId);
            pstUpdate.executeUpdate();

            conn.commit(); // COMMIT TRANSACTION
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {} // Undo changes if error
            JOptionPane.showMessageDialog(null, "Approval Failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    // --- 4. REJECT REQUEST ---
    public boolean rejectRequest(int requestId) {
        try {
            PreparedStatement pst = conn.prepareStatement("UPDATE CustomerRequest SET status = 'Rejected' WHERE requestId = ?");
            pst.setInt(1, requestId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 5. ADD REQUEST (For Manual Entry) ---
    public boolean addRequest(String custName, String prod, double qty, java.util.Date date, String priority) {
        try {
            // Convert Name to ID
            int custId = getCustomerMap().get(custName);

            String sql = "INSERT INTO CustomerRequest (customerId, productName, quantity, expectedDate, priority, status, requestDate) VALUES (?, ?, ?, ?, ?, 'Pending', NOW())";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, custId);
            pst.setString(2, prod);
            pst.setDouble(3, qty);
            pst.setDate(4, new java.sql.Date(date.getTime()));
            pst.setString(5, priority);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return false;
        }
    }
}