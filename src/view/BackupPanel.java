package view;

import controller.BackupController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class BackupPanel extends JPanel {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private BackupController controller;

    // Colors
    private final Color PRIMARY = new Color(13, 110, 253);
    private final Color DARK_BG = new Color(33, 37, 41);

    public BackupPanel() {
        controller = new BackupController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250)); // Light Grey
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 1. Header Section (Title + Button)
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Section (History Table)
        add(createHistoryPanel(), BorderLayout.CENTER);

        // Load initial data
        controller.loadHistory(tableModel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));

        // Title
        JLabel title = new JLabel("System Backup & Recovery");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(50, 50, 50));

        JLabel subtitle = new JLabel("Create SQL snapshots of your database securely.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        // Button
        JButton btnBackup = new JButton("☁ Create New Backup");
        btnBackup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBackup.setBackground(PRIMARY);
        btnBackup.setForeground(Color.WHITE);
        btnBackup.setFocusPainted(false);
        btnBackup.setPreferredSize(new Dimension(200, 45));
        btnBackup.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBackup.addActionListener(e -> performBackup());

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(btnBackup, BorderLayout.EAST);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Backup History"));
        panel.setBackground(Color.WHITE);

        String[] cols = {"ID", "File Name", "Date Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(230, 230, 230));

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private void performBackup() {
        // 1. Simple Confirmation (No File Chooser needed)
        int choice = JOptionPane.showConfirmDialog(this,
                "Create Database Backup now?\n(Saved to C:\\backups\\)",
                "Confirm Backup", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {

            // 2. Change Cursor to Wait
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // 3. Call Controller (No arguments needed now)
            boolean success = controller.createBackup();

            if (success) {
                // Refresh the history table if it exists
                if (tableModel != null) {
                    controller.loadHistory(tableModel);
                }
            }

            // 4. Reset Cursor
            this.setCursor(Cursor.getDefaultCursor());
        }
    }
}