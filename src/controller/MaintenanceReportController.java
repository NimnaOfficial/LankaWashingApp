package controller;

import db.DatabaseConnection;
import net.sf.jasperreports.engine.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MaintenanceReportController {

    private Connection conn;

    public MaintenanceReportController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    // --- 1. PREVIEW LOGIC (Populates JTable) ---
    public void loadReportPreview(String reportName, DefaultTableModel model) {
        model.setRowCount(0); // Clear table
        model.setColumnCount(0); // Clear columns

        String sql = "";

        switch (reportName) {
            case "Maintenance Task History":
                // ✅ FIXED: Changed 'faultDetails' -> 'description', 't.description' -> 't.repairDetails', 'taskDate' -> 'date'
                sql = "SELECT t.taskId, m.machineType, f.description AS fault_desc, t.repairDetails, t.cost, t.date " +
                        "FROM task t " +
                        "JOIN faultlog f ON t.faultId = f.faultId " +
                        "JOIN machine m ON f.machineId = m.machineId " +
                        "ORDER BY t.date DESC LIMIT 50";
                break;

            case "Critical Faults Log":
                // ✅ FIXED: Changed 'faultDetails' -> 'description', 'priority' -> 'severity'
                sql = "SELECT f.faultId, m.machineType, f.description, f.reportedDate, f.severity " +
                        "FROM faultlog f " +
                        "JOIN machine m ON f.machineId = m.machineId " +
                        "WHERE f.severity = 'High' OR f.severity = 'Critical' " +
                        "ORDER BY f.reportedDate DESC";
                break;

            case "Technician Utilization":
                // ✅ FIXED: Removed 'specialization' (not in DB), added 'availabilityStatus'
                sql = "SELECT tech.name, tech.availabilityStatus, COUNT(t.taskId) AS tasks_completed, COALESCE(SUM(t.cost), 0) AS total_value " +
                        "FROM technician tech " +
                        "LEFT JOIN task t ON tech.technicianId = t.technicianId " +
                        "GROUP BY tech.technicianId";
                break;

            case "Machine Cost & ROI Analysis (Strategic)":
                // ✅ FIXED: Ensured column names match constraints
                sql = "SELECT m.machineId, m.machineType, " +
                        "COUNT(f.faultId) AS breakdown_count, " +
                        "COALESCE(SUM(t.cost), 0) AS total_repair_cost, " +
                        "CASE " +
                        "   WHEN COUNT(f.faultId) > 5 OR SUM(t.cost) > 50000 THEN 'REPLACE MACHINE' " +
                        "   ELSE 'Service Routine' " +
                        "END AS recommendation " +
                        "FROM machine m " +
                        "LEFT JOIN faultlog f ON m.machineId = f.machineId " +
                        "LEFT JOIN task t ON f.faultId = t.faultId " +
                        "GROUP BY m.machineId " +
                        "ORDER BY total_repair_cost DESC";
                break;
        }

        // Execute Query and Fill Table
        try {
            if (sql.isEmpty()) return;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Add Columns
            for (int i = 1; i <= colCount; i++) {
                model.addColumn(meta.getColumnLabel(i));
            }

            // Add Rows
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading preview: " + e.getMessage());
        }
    }

    // --- 2. PDF GENERATION LOGIC ---
    public void generateReport(String reportName) {
        String jasperFile = "";
        String outputName = "";

        switch (reportName) {
            case "Maintenance Task History":
                jasperFile = "maintenance_task_history.jasper";
                outputName = "Maintenance_Log";
                break;
            case "Critical Faults Log":
                jasperFile = "critical_faults.jasper";
                outputName = "Critical_Faults";
                break;
            case "Technician Utilization":
                jasperFile = "technician_utilization.jasper";
                outputName = "Technician_Performance";
                break;
            case "Machine Cost & ROI Analysis (Strategic)":
                jasperFile = "MachineCostReport.jasper";
                outputName = "Machine_ROI_Analysis";
                break;
            default:
                return;
        }

        generatePdf(jasperFile, outputName);
    }

    private void generatePdf(String fileName, String outputName) {
        try {
            InputStream reportStream = getClass().getResourceAsStream("/reports/" + fileName);
            if (reportStream == null) {
                JOptionPane.showMessageDialog(null, "Report file not found: " + fileName +
                        "\nMake sure the .jasper file is in src/reports/");
                return;
            }

            Map<String, Object> params = new HashMap<>();
            String logoPath = "C:/Users/SANDANIMNE/Desktop/EAD fnl/logo.png";
            if (new File(logoPath).exists()) params.put("logo", new ImageIcon(logoPath).getImage());

            JasperPrint jp = JasperFillManager.fillReport(reportStream, params, conn);

            if (jp.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No data to display.");
                return;
            }

            String outputDir = "C:\\reports\\Maintenance\\";
            new File(outputDir).mkdirs();

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputFile = outputDir + outputName + "_" + timestamp + ".pdf";

            JasperExportManager.exportReportToPdfFile(jp, outputFile);

            if (Desktop.isDesktopSupported() && new File(outputFile).exists()) {
                Desktop.getDesktop().open(new File(outputFile));
            } else {
                JOptionPane.showMessageDialog(null, "Saved: " + outputFile);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }
}