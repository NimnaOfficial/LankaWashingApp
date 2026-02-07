package controller;

import db.DatabaseConnection;
import java.awt.Desktop;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

public class AdminController {

    private Connection conn;

    public AdminController() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- BACKUP FUNCTIONALITY ---
    public boolean createBackup() {
        try {
            // 1. Define Output Directory (Auto-save to C:\backups\)
            String outputDir = "C:\\backups\\";
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs(); // Create folder if it doesn't exist
            }

            // 2. Generate Filename with Timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "production_backup_" + timeStamp + ".sql";
            String fullPath = outputDir + fileName;

            // 3. Database Config (MATCH YOUR DB SETTINGS)
            String dbName = "production_db";
            String dbUser = "root";
            String dbPass = "";
            String dbPort = "3307"; //

            // 4. XAMPP mysqldump Path
            String mysqlBinPath = "C:\\xampp\\mysql\\bin\\mysqldump.exe";
            if (!new File(mysqlBinPath).exists()) {
                JOptionPane.showMessageDialog(null, "Error: mysqldump not found at " + mysqlBinPath);
                return false;
            }

            // 5. Construct Command
            // Syntax: mysqldump -u [user] -p[pass] -P [port] --databases [db] -r [path]
            String command = String.format(
                    "cmd /c \"\"%s\" -u%s %s -P%s --databases %s -r \"%s\"\"",
                    mysqlBinPath,
                    dbUser,
                    (dbPass.isEmpty() ? "" : "-p" + dbPass),
                    dbPort,
                    dbName,
                    fullPath
            );

            // 6. Execute Command
            Process process = Runtime.getRuntime().exec(command);
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                // Success!
                System.out.println("Backup created: " + fileName);

                // 7. Open the folder/file automatically
                File backupFile = new File(fullPath);
                if (backupFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(backupFile);
                } else {
                    JOptionPane.showMessageDialog(null, "Backup Created Successfully:\n" + fullPath);
                }
                return true;
            } else {
                // Failure
                java.io.InputStream errorStream = process.getErrorStream();
                byte[] buffer = new byte[errorStream.available()];
                errorStream.read(buffer);
                String errorMsg = new String(buffer);

                JOptionPane.showMessageDialog(null,
                        "Backup Failed (Exit Code " + processComplete + ").\n" +
                                "Check if Port " + dbPort + " is correct.\n" +
                                "Error Details: " + errorMsg,
                        "Backup Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "System Error: " + e.getMessage());
            return false;
        }
    }
}