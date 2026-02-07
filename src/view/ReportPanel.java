package view;

import controller.ReportController;
import javax.swing.*;
import java.awt.*;

public class ReportPanel extends JPanel {

    private JComboBox<String> cmbReportType;
    private ReportController controller;
    private final Color PRIMARY = new Color(13, 110, 253);

    public ReportPanel() {
        controller = new ReportController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(new Color(248, 249, 250));

        JLabel title = new JLabel("System Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subtitle = new JLabel("Generate PDF reports and open them automatically.");

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Available Reports"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Report Type:"), gbc);

        gbc.gridx = 1;
        String[] reports = {"Select Report", "User Details Report", "Role Permissions Report"};
        cmbReportType = new JComboBox<>(reports);
        cmbReportType.setPreferredSize(new Dimension(250, 35));
        panel.add(cmbReportType, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;

        JButton btnGenerate = new JButton("📄 Generate & Open");
        btnGenerate.setBackground(PRIMARY);
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerate.setPreferredSize(new Dimension(180, 40));
        btnGenerate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGenerate.addActionListener(e -> performGeneration());

        panel.add(btnGenerate, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(248, 249, 250));
        wrapper.add(panel, BorderLayout.NORTH);

        return wrapper;
    }

    private void performGeneration() {
        String selection = cmbReportType.getSelectedItem().toString();

        if (selection.equals("Select Report")) {
            JOptionPane.showMessageDialog(this, "Please select a report type!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show Wait Cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Call Controller
        controller.generateAndOpenReport(selection);

        // Reset Cursor
        setCursor(Cursor.getDefaultCursor());
    }
}