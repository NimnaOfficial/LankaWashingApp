package view;

import controller.InventoryReportController;
import javax.swing.*;
import java.awt.*;

public class InventoryReportPanel extends JPanel {

    private InventoryReportController controller;

    // Theme Colors
    private final Color ACCENT_COLOR = new Color(255, 140, 0);
    private final Color BG_COLOR = new Color(248, 249, 250);
    private final Color CARD_BG = Color.WHITE;

    public InventoryReportPanel() {
        controller = new InventoryReportController();
        setLayout(new BorderLayout(40, 40));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // 1. Header
        add(createHeader(), BorderLayout.NORTH);

        // 2. Report Buttons Grid
        add(createReportGrid(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("Inventory Strategy & Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(50, 50, 50));

        JLabel subtitle = new JLabel("Generate intelligence reports for procurement decisions.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    private JPanel createReportGrid() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 30, 0));
        panel.setBackground(BG_COLOR);

        // --- Button 1: Low Stock (Critical for Operations) ---
        panel.add(createReportCard(
                "🚨 Low Stock Alerts",
                "Items below minimum level that need immediate re-ordering.",
                "low_stock_report.jasper",
                "Low_Stock_Report"
        ));

        // --- Button 2: Stock Velocity (NEW STRATEGIC REPORT) ---
        // Replaced "Full Inventory" because this provides the same data + intelligence
        panel.add(createReportCard(
                "⚡ Stock Velocity & Runway",
                "Analyze burn rates, estimated days left, and overstocked items.",
                "StockVelocityReport.jasper",
                "Stock_Velocity_Analysis"
        ));

        // --- Button 3: Transactions (Audit Trail) ---
        panel.add(createReportCard(
                "📜 Transaction History",
                "Log of all recent Stock IN and Stock OUT actions.",
                "transaction_report.jasper",
                "Transaction_Log"
        ));

        return panel;
    }

    private JPanel createReportCard(String title, String desc, String jasperFile, String outputName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(CARD_BG);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(40, 40, 40));

        JLabel lblDesc = new JLabel("<html>" + desc + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);

        textPanel.add(lblTitle);
        textPanel.add(lblDesc);

        JButton btnPrint = new JButton("Generate Report");
        btnPrint.setBackground(ACCENT_COLOR);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Calls the generic controller method
        btnPrint.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            controller.generateReport(jasperFile, outputName);
            setCursor(Cursor.getDefaultCursor());
        });

        card.add(textPanel, BorderLayout.CENTER);
        card.add(btnPrint, BorderLayout.SOUTH);

        return card;
    }
}