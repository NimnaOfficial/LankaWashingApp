package model;

public class BackupLog {
    private int backupId;
    private String fileName;
    private String date;

    public BackupLog(int backupId, String fileName, String date) {
        this.backupId = backupId;
        this.fileName = fileName;
        this.date = date;
    }

    public int getBackupId() { return backupId; }
    public String getFileName() { return fileName; }
    public String getDate() { return date; }
}