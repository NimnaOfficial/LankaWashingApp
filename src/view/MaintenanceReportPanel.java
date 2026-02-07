package view;

import controller.MaintenanceReportController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MaintenanceReportPanel extends JPanel {

    private JComboBox<String> cmbReports;
    private JTable table;
    private DefaultTableModel model;
    private MaintenanceReportController controller;
    private JLabel lblPreview;

    // Theme Colors
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color BG_COLOR = new Color(240, 244, 248);
    private final Color HEADER_BG = new Color(15, 76, 117);

    public MaintenanceReportPanel() {
        controller = new MaintenanceReportController();
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP PANEL (Selection) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel lblSelect = new JLabel("Select Report:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] reports = {
                "Maintenance Task History",
                "Critical Faults Log",
                "Technician Utilization",
                "Machine Cost & ROI Analysis (Strategic)" // ✅ NEW DECISION REPORT
        };
        cmbReports = new JComboBox<>(reports);
        cmbReports.setPreferredSize(new Dimension(300, 30));
        cmbReports.setBackground(Color.WHITE);

        // Trigger data load on selection
        cmbReports.addActionListener(this::updatePreview);

        JButton btnPrint = new JButton("Print PDF / Export");
        btnPrint.setBackground(new Color(40, 167, 69)); // Green
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrint.setPreferredSize(new Dimension(150, 30));
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPrint.addActionListener(e -> {
            String selected = (String) cmbReports.getSelectedItem();
            // Show wait cursor
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            controller.generateReport(selected);
            setCursor(Cursor.getDefaultCursor());
        });

        topPanel.add(lblSelect);
        topPanel.add(cmbReports);
        topPanel.add(btnPrint);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL (Table Preview) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblPreview = new JLabel("Data Preview: Maintenance Task History");
        lblPreview.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPreview.setForeground(HEADER_BG);
        lblPreview.setBorder(new EmptyBorder(0, 0, 10, 0));

        model = new DefaultTableModel();
        table = new JTable(model) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table.setRowHeight(25);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        centerPanel.add(lblPreview, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Load initial data
        updatePreview(null);
    }

    private void updatePreview(ActionEvent e) {
        String selected = (String) cmbReports.getSelectedItem();
        lblPreview.setText("Data Preview: " + selected);

        // Load data into table via controller
        controller.loadReportPreview(selected, model);
    }
}