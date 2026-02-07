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

public class ProductionReportController {

    private Connection conn;

    public ProductionReportController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Error: " + e.getMessage());
        }
    }

    // 1. FULL BATCH HISTORY
    public void generateFullBatchReport() {
        generateReport("production_batch_report.jasper", new HashMap<>(), "Full_Batch_History");
    }

    // 2. RESOURCE CONSUMPTION
    public void generateResourceReport() {
        generateReport("resource_usage_report.jasper", new HashMap<>(), "Resource_Consumption_Report");
    }

    // 3. ✅ NEW: BATCH PROFITABILITY REPORT (Replaces Customer Report)
    public void generateBatchProfitReport() {
        // Ensure you have created 'BatchProfitReport.jrxml' and compiled it to .jasper
        generateReport("BatchProfitReport.jasper", new HashMap<>(), "Batch_Profitability_Analysis");
    }

    // 4. MONTHLY / DATE RANGE REPORT
    public void generateDateRangeReport(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(null, "Please select both Start and End dates!");
            return;
        }

        java.sql.Date sqlFrom = new java.sql.Date(fromDate.getTime());
        java.sql.Date sqlTo = new java.sql.Date(toDate.getTime());

        Map<String, Object> params = new HashMap<>();
        params.put("fromDate", sqlFrom);
        params.put("toDate", sqlTo);

        generateReport("monthly_production_report.jasper", params, "Monthly_Production_Analysis");
    }

    // --- GENERIC HELPER METHOD ---
    private void generateReport(String fileName, Map<String, Object> params, String outputName) {
        try {
            // 1. Load the compiled report (.jasper)
            InputStream reportStream = getClass().getResourceAsStream("/reports/" + fileName);

            if (reportStream == null) {
                JOptionPane.showMessageDialog(null,
                        "Report file not found: /reports/" + fileName + "\n\n" +
                                "Make sure the .jasper file is inside the 'src/reports' folder.");
                return;
            }

            // 2. Add Logo if exists (Optional)
            String logoPath = "C:/Users/SANDANIMNE/Desktop/EAD fnl/logo.png";
            if (new File(logoPath).exists()) {
                params.put("logo", new ImageIcon(logoPath).getImage());
            }

            // 3. Fill the report with data
            JasperPrint jp = JasperFillManager.fillReport(reportStream, params, conn);

            // 4. Check for empty pages
            if (jp.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No data found for this report.");
                return;
            }

            // 5. Define Output Directory & Filename
            String outputDir = "C:\\reports\\Production\\"; // Organized into subfolder
            new File(outputDir).mkdirs();

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String outputFile = outputDir + outputName + "_" + timestamp + ".pdf";

            // 6. Export to PDF File
            JasperExportManager.exportReportToPdfFile(jp, outputFile);

            // 7. Automatically Open the PDF
            if (Desktop.isDesktopSupported()) {
                File pdfFile = new File(outputFile);
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(null, "PDF created but could not be found: " + outputFile);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Report Saved Successfully at:\n" + outputFile);
            }

        } catch (JRException ex) {
            JOptionPane.showMessageDialog(null, "Error Generating Report:\n" + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "System Error: " + ex.getMessage());
        }
    }
}