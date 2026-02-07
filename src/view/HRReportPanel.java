package view;

import controller.HRReportController;
import javax.swing.*;
import java.awt.*;

public class HRReportPanel extends JPanel {

    private JComboBox<String> cmbReport;
    private HRReportController controller; // Updated to specific controller
    private final Color TEAL = new Color(0, 128, 128); // Professional HR Teal
    private final Color BG_COLOR = new Color(248, 249, 250);

    public HRReportPanel() {
        controller = new HRReportController();
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(BG_COLOR);

        JLabel title = new JLabel("HR Strategy & Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEAL);

        JLabel sub = new JLabel("Generate payroll audits and workforce cost analysis.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(Color.GRAY);

        p.add(title);
        p.add(sub);
        return p;
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Icon
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel icon = new JLabel("👥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        panel.add(icon, gbc);

        // 2. Report Selector
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Select Report Type:"), gbc);

        gbc.gridx = 1;
        // ✅ ADDED: "OT Cost Analysis" (Strategic Decision Report)
        String[] reports = {
                "Select Report...",
                "Employee Master List",
                "Monthly Payroll Log",
                "OT Cost Analysis (Strategic)"
        };
        cmbReport = new JComboBox<>(reports);
        cmbReport.setPreferredSize(new Dimension(280, 35));
        panel.add(cmbReport, gbc);

        // 3. Generate Button
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;

        JButton btnGen = new JButton("Generate PDF");
        btnGen.setBackground(TEAL);
        btnGen.setForeground(Color.WHITE);
        btnGen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGen.setPreferredSize(new Dimension(180, 40));
        btnGen.setFocusPainted(false);
        btnGen.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGen.addActionListener(e -> generate());
        panel.add(btnGen, gbc);

        // Wrapper to center the card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_COLOR);
        wrapper.add(panel);

        return wrapper;
    }

    private void generate() {
        String selection = (String) cmbReport.getSelectedItem();

        if (selection == null || selection.equals("Select Report...")) {
            JOptionPane.showMessageDialog(this, "Please select a valid report type.");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Pass selection to controller
        controller.handleReportSelection(selection);

        setCursor(Cursor.getDefaultCursor());
    }
}