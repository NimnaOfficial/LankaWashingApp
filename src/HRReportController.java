package controller;

import db.DatabaseConnection;
import net.sf.jasperreports.engine.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HRReportController {

    private Connection conn;

    public HRReportController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Error: " + e.getMessage());
        }
    }

    public void handleReportSelection(String selection) {
        switch (selection) {
            case "Employee Master List":
                generateReport("employee_list.jasper", "Employee_Directory");
                break;

            case "Monthly Payroll Log":
                generateReport("payroll_report.jasper", "Monthly_Payroll_Log");
                break;

            case "OT Cost Analysis (Strategic)":
                // This corresponds to the SQL logic for checking Overtime Ratio vs. Hiring
                generateReport("OTAnalysisReport.jasper", "OT_Cost_Strategic_Analysis");
                break;

            default:
                JOptionPane.showMessageDialog(null, "Invalid Selection");
        }
    }

    private void generateReport(String fileName, String outputName) {
        try {
            // 1. Load Report
            InputStream reportStream = getClass().getResourceAsStream("/reports/" + fileName);

            if (reportStream == null) {
                JOptionPane.showMessageDialog(null,
                        "Report file not found: /reports/" + fileName + "\n\n" +
                                "Please ensure the .jasper file exists in 'src/reports'.");
                return;
            }

            // 2. Parameters (Logo, etc.)
            Map<String, Object> params = new HashMap<>();
            String logoPath = "C:/Users/SANDANIMNE/Desktop/EAD fnl/logo.png";
            if (new File(logoPath).exists()) {
                params.put("logo", new ImageIcon(logoPath).getImage());
            }

            // 3. Fill Report
            JasperPrint jp = JasperFillManager.fillReport(reportStream, params, conn);

            if (jp.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No data found for this report.");
                return;
            }

            // 4. Export
            String outputDir = "C:\\reports\\HR\\";
            new File(outputDir).mkdirs();

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputFile = outputDir + outputName + "_" + timestamp + ".pdf";

            JasperExportManager.exportReportToPdfFile(jp, outputFile);

            // 5. Open PDF
            if (Desktop.isDesktopSupported()) {
                File pdfFile = new File(outputFile);
                if (pdfFile.exists()) Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(null, "Report Saved: " + outputFile);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error Generating Report:\n" + ex.getMessage());
        }
    }
}