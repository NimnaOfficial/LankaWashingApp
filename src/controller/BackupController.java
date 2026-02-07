package controller;

import db.DatabaseConnection;
import java.awt.Desktop;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class BackupController {

    private Connection conn;

    public BackupController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 1. CREATE BACKUP (Auto-save & Open Folder) ---
    public boolean createBackup() {
        try {
            // A. Define Output Directory
            String outputDir = "C:\\backups\\";
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs(); // Create folder if missing
            }

            // B. Generate Filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "production_backup_" + timeStamp + ".sql";
            String fullPath = outputDir + fileName;

            // C. Database Config (CHECK YOUR SETTINGS)
            String dbName = "production_db";
            String dbUser = "root";
            String dbPass = "";
            String dbPort = "3307"; //

            // D. XAMPP Path
            String mysqlBinPath = "C:\\xampp\\mysql\\bin\\mysqldump.exe";
            if (!new File(mysqlBinPath).exists()) {
                JOptionPane.showMessageDialog(null, "Error: mysqldump not found at " + mysqlBinPath);
                return false;
            }

            // E. Run Command
            String command = String.format(
                    "cmd /c \"\"%s\" -u%s %s -P%s --databases %s -r \"%s\"\"",
                    mysqlBinPath, dbUser, (dbPass.isEmpty() ? "" : "-p" + dbPass), dbPort, dbName, fullPath
            );

            Process process = Runtime.getRuntime().exec(command);
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                // Success: Log to DB and Open Folder
                logBackupToDB(fileName);

                File backupFile = new File(fullPath);
                if (backupFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(backupFile.getParentFile()); // Open the folder
                }
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Backup Failed. Check Port 3307 / Config.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 2. LOG HISTORY TO DATABASE ---
    private void logBackupToDB(String fileName) {
        String sql = "INSERT INTO BackupHistory (fileName, backupDate, status) VALUES (?, NOW(), 'Success')";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, fileName);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 3. LOAD HISTORY INTO TABLE ---
    public void loadHistory(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM BackupHistory ORDER BY backupDate DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("backupId"),
                        rs.getString("fileName"),
                        rs.getString("backupDate")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}