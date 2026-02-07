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

public class InventoryReportController {

    private Connection conn;

    public InventoryReportController() {
        try {
            // Using existing singleton connection
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateReport(String reportFileName, String outputName) {

        // 1. Set Cursor to Wait

        try {
            // -------- LOAD REPORT SAFELY --------
            String reportPath = "/reports/" + reportFileName;
            InputStream is = getClass().getResourceAsStream(reportPath);

            if (is == null) {
                throw new RuntimeException("Report file not found: " + reportPath);
            }

            JasperReport jr = (JasperReport) JRLoader.loadObject(is);

            // -------- PARAMETERS --------
            Map<String, Object> params = new HashMap<>();
            params.put("printedBy", "SmartCrop Inventory System");

            // OPTIONAL LOGO (Your specific path)
            String logoPath = "C:/Users/SANDANIMNE/Desktop/EAD fnl/logo.png";
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                ImageIcon logo = new ImageIcon(logoPath);
                params.put("logo", logo.getImage());
            }

            // Fill Report
            JasperPrint jp = JasperFillManager.fillReport(jr, params, conn);

            // 2. Define output folder and filename
            String outputDir = "C:\\reports\\";
            new File(outputDir).mkdirs(); // create folder if it doesn't exist

            // Add timestamp to filename to avoid overwriting
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String outputFile = outputDir + outputName + "_" + timestamp + ".pdf";

            // 3. Export PDF
            JasperExportManager.exportReportToPdfFile(jp, outputFile);

            // 4. Success Message
            JOptionPane.showMessageDialog(null, "Report Generated Successfully!\nSaved to: " + outputFile);

            // 5. Open the PDF automatically
            if (Desktop.isDesktopSupported()) {
                File pdfFile = new File(outputFile);
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Report Error:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}