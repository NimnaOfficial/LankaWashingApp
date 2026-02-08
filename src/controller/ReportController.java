package controller;

import db.DatabaseConnection;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportController {

    private Connection conn;

    public ReportController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateAndOpenReport(String reportSelection) {
        String reportFile = "";

        // --- MAP SELECTION TO FILENAME ---
        switch (reportSelection) {
            // Admin Reports
            case "User Details Report":
                reportFile = "users_report.jasper";
                break;
            case "Role Permissions Report":
                reportFile = "roles_report.jasper";
                break;

            // HR Reports (NEW!)
            case "Employee List":
                reportFile = "employees_report.jasper";
                break;
            case "Monthly Payroll":
                reportFile = "payroll_report.jasper";
                break;

            default:
                JOptionPane.showMessageDialog(null, "Invalid Report Selection");
                return;
        }

        try {
            // 1. Load Report
            String reportPath = "/reports/" + reportFile;
            InputStream is = getClass().getResourceAsStream(reportPath);

            if (is == null) {
                // Fallback for testing outside JAR
                is = new java.io.FileInputStream("src/reports/" + reportFile);
            }

            JasperReport jr = (JasperReport) JRLoader.loadObject(is);

            // 2. Parameters (Empty for now)
            Map<String, Object> params = new HashMap<>();

            // 3. Fill Report
            JasperPrint jp = JasperFillManager.fillReport(jr, params, conn);

            // 4. Output to C:\reports\
            String outputDir = "C:\\reports\\Admin\\";
            new File(outputDir).mkdirs();

            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String outputFileName = outputDir + reportSelection.replace(" ", "_") + "_" + timeStamp + ".pdf";

            // 5. Export & Open
            JasperExportManager.exportReportToPdfFile(jp, outputFileName);

            File pdfFile = new File(outputFileName);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(null, "Report Saved: " + outputFileName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating report: " + ex.getMessage());
        }
    }
}