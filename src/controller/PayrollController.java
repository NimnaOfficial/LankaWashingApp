package controller;

import db.DatabaseConnection;
import java.sql.*;
import javax.swing.JOptionPane;

public class PayrollController {

    private Connection conn;

    public PayrollController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. Get Basic Salary (from Employee table)
    public double getBasicSalary(int empId) {
        String sql = "SELECT basicSalary FROM Employee WHERE empId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, empId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDouble("basicSalary");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // 2. Process & Save Payroll
    public boolean savePayroll(int empId, String month, double basic, int otHours,
                               double allowances, double deductions) {

        // --- AUTO-CALCULATIONS ---
        // 1. Calculate OT Earnings (Standard Formula: Basic / 240 hours * 1.5 rate * hours)
        double hourlyRate = basic / 240;
        double otEarnings = Math.round(hourlyRate * 1.5 * otHours);

        // 2. Calculate Net Salary
        double grossIncome = basic + otEarnings + allowances;
        double netSalary = grossIncome - deductions;

        // 3. Status
        String status = "Paid";
        int structureId = 0; // Default since we aren't using that module

        // --- DATABASE SAVE ---
        String sql = "INSERT INTO Payroll (employeeId, structureId, month, basicSalary, otHours, otEarnings, allowances, deductions, netSalary, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, empId);
            pst.setInt(2, structureId);
            pst.setString(3, month);
            pst.setDouble(4, basic);
            pst.setInt(5, otHours);
            pst.setDouble(6, otEarnings);
            pst.setDouble(7, allowances);
            pst.setDouble(8, deductions);
            pst.setDouble(9, netSalary);
            pst.setString(10, status);

            int result = pst.executeUpdate();
            if(result > 0) {
                String msg = String.format(
                        "Payroll Processed Successfully!\n\n" +
                                "Basic Salary:  %.2f\n" +
                                "OT Earnings:   %.2f (for %d hrs)\n" +
                                "Allowances:    %.2f\n" +
                                "Deductions:   -%.2f\n" +
                                "-----------------------\n" +
                                "NET SALARY:    %.2f",
                        basic, otEarnings, otHours, allowances, deductions, netSalary);

                JOptionPane.showMessageDialog(null, msg);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
        return false;
    }
    // ... existing code ...

    // 3. NEW: Load History into Table
    public void loadPayrollHistory(javax.swing.table.DefaultTableModel model) {
        model.setRowCount(0); // Clear old data
        String sql = "SELECT * FROM Payroll ORDER BY payrollId DESC"; // Newest first

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("payrollId"),
                        rs.getInt("employeeId"),
                        rs.getString("month"),
                        rs.getDouble("basicSalary"),
                        rs.getInt("otHours"),
                        rs.getDouble("otEarnings"),
                        rs.getDouble("netSalary"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
