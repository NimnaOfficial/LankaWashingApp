package controller;

import db.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.*;

public class EmployeeController {

    private Connection conn;

    public EmployeeController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // PART 1: METHODS FOR EMPLOYEE MANAGEMENT
    // ==========================================

    // 1. ADD EMPLOYEE
    public boolean addEmployee(String name, String nic, String phone, String addr,
                               String desig, String dept, double salary) {
        String sql = "INSERT INTO Employee (name, nic, phone, address, designation, department, basicSalary) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, nic);
            pst.setString(3, phone);
            pst.setString(4, addr);
            pst.setString(5, desig);
            pst.setString(6, dept);
            pst.setDouble(7, salary);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Adding: " + e.getMessage());
            return false;
        }
    }

    // 2. UPDATE EMPLOYEE (Full Update)
    public boolean updateEmployee(int id, String name, String nic, String phone, String addr,
                                  String desig, String dept, double salary, String status) {
        String sql = "UPDATE Employee SET name=?, nic=?, phone=?, address=?, designation=?, department=?, basicSalary=?, status=? WHERE empId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, nic);
            pst.setString(3, phone);
            pst.setString(4, addr);
            pst.setString(5, desig);
            pst.setString(6, dept);
            pst.setDouble(7, salary);
            pst.setString(8, status);
            pst.setInt(9, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Updating: " + e.getMessage());
            return false;
        }
    }

    // 3. DELETE EMPLOYEE
    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM Employee WHERE empId=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Deleting: " + e.getMessage());
            return false;
        }
    }

    // 4. LOAD ALL EMPLOYEES (For Employee Mgmt Panel)
    public void loadEmployees(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM Employee";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("empId"),
                        rs.getString("name"),
                        rs.getString("nic"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("designation"),
                        rs.getString("department"),
                        rs.getDouble("basicSalary"),
                        rs.getString("status")
                });
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // PART 2: METHODS FOR SALARY STRUCTURE PANEL
    // ==========================================

    // 5. LOAD SALARIES ONLY (For Salary Panel)
    public void loadEmployeeSalaries(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT empId, name, designation, basicSalary FROM Employee";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("empId"),
                        rs.getString("name"),
                        rs.getString("designation"),
                        rs.getDouble("basicSalary")
                });
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // 6. UPDATE BASIC SALARY ONLY (For Salary Panel)
    public boolean updateBasicSalary(int empId, double newSalary) {
        String sql = "UPDATE Employee SET basicSalary = ? WHERE empId = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setDouble(1, newSalary);
            pst.setInt(2, empId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // ... inside EmployeeController class ...

    // 7. NEW: Get Real Dashboard Statistics
    public java.util.Map<String, String> getDashboardStats() {
        java.util.Map<String, String> stats = new java.util.HashMap<>();

        // Defaults
        stats.put("active", "0");
        stats.put("depts", "0");
        stats.put("payroll", "-");

        try {
            // 1. Count Active Employees
            Statement stmt = conn.createStatement();
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM Employee WHERE status='Active'");
            if (rs1.next()) {
                stats.put("active", String.valueOf(rs1.getInt(1)));
            }

            // 2. Count Active Departments (Replacing 'Pending Leave' since we don't have a Leave table)
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(DISTINCT department) FROM Employee");
            if (rs2.next()) {
                stats.put("depts", String.valueOf(rs2.getInt(1)));
            }

            // 3. Calculate Next Payroll Date (Last day of current month)
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate lastDay = today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
            stats.put("payroll", lastDay.getMonth().toString().substring(0,3) + " " + lastDay.getDayOfMonth());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}