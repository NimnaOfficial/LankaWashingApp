package view;

import com.toedter.calendar.JDateChooser;
import controller.ProductionReportController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

public class ProductionReportPanel extends JPanel {

    private ProductionReportController controller;
    private JDateChooser dateFrom, dateTo;

    // Theme: Engineering Blue
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color BG_COLOR = new Color(240, 244, 248);

    public ProductionReportPanel() {
        controller = new ProductionReportController();
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Main Container
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 0);

        // 1. General Reports Panel
        mainContainer.add(createMasterReportsPanel(), gbc);

        // 2. Monthly Analysis Panel
        gbc.gridy++;
        mainContainer.add(createMonthlyReportPanel(), gbc);

        // Push to top
        gbc.gridy++;
        gbc.weighty = 1.0;
        mainContainer.add(Box.createGlue(), gbc);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createMasterReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE),
                        " Master Production Reports ",
                        0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
                )
        ));

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 20, 0)); // 3 Columns
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Button 1: Full History (Operational Audit)
        JButton btnBatch = createLargeButton("📄 Full Batch History", "Combined logs from Dyeing, Washing & Drying");
        btnBatch.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            controller.generateFullBatchReport();
            setCursor(Cursor.getDefaultCursor());
        });

        // Button 2: Resource Usage (Cost Control)
        JButton btnRes = createLargeButton("🛢️ Resource Consumption", "Chemical & Wood usage analysis");
        btnRes.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            controller.generateResourceReport();
            setCursor(Cursor.getDefaultCursor());
        });

        // Button 3: Profitability (STRATEGIC DECISION)
        // Replaced "Customer Requests" with "Batch Profitability"
        JButton btnProfit = createLargeButton("💰 Batch Profitability", "Analyze production costs vs sales revenue");
        btnProfit.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            controller.generateBatchProfitReport();
            setCursor(Cursor.getDefaultCursor());
        });

        btnPanel.add(btnBatch);
        btnPanel.add(btnRes);
        btnPanel.add(btnProfit);
        panel.add(btnPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMonthlyReportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE),
                        " Date Range Analysis ",
                        0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        JLabel lblDesc = new JLabel("<html>Select a date range to analyze completed batches and efficiency.</html>");
        lblDesc.setForeground(Color.GRAY);
        panel.add(lblDesc, gbc);

        // Row 2: Inputs
        gbc.gridwidth = 1; gbc.gridy = 1;

        gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel("From Date:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.3;
        dateFrom = new JDateChooser();
        dateFrom.setDateFormatString("yyyy-MM-dd");
        dateFrom.setDate(new Date());
        panel.add(dateFrom, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("To Date:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.3;
        dateTo = new JDateChooser();
        dateTo.setDateFormatString("yyyy-MM-dd");
        dateTo.setDate(new Date());
        panel.add(dateTo, gbc);

        // Row 3: Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(20, 15, 20, 15);

        JButton btnGenerate = new JButton("Generate Date Range Report");
        btnGenerate.setBackground(new Color(40, 167, 69));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerate.setFocusPainted(false);
        btnGenerate.setPreferredSize(new Dimension(200, 35));

        btnGenerate.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            controller.generateDateRangeReport(dateFrom.getDate(), dateTo.getDate());
            setCursor(Cursor.getDefaultCursor());
        });

        panel.add(btnGenerate, gbc);

        return panel;
    }

    private JButton createLargeButton(String title, String subtitle) {
        JButton btn = new JButton("<html><left><div style='padding: 5px;'><span style='font-size:12px; font-weight:bold'>" + title + "</span><br/><span style='font-size:9px; color:gray'>" + subtitle + "</span></div></left></html>");
        btn.setPreferredSize(new Dimension(280, 70));
        btn.setBackground(new Color(250, 250, 250));
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}