package view;

import controller.PayrollController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PayrollPanel extends JPanel {

    private JTextField txtEmpId, txtBasic, txtOtHours, txtAllowance, txtDeduction;
    private JComboBox<String> cmbMonth;
    private JTable table;
    private DefaultTableModel model;
    private PayrollController controller;
    private final Color TEAL = new Color(7, 81, 173);

    public PayrollPanel() {
        controller = new PayrollController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Split Layout: Top (Form), Center (Table)
        add(createCalculatorPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        // Load data immediately
        refreshTable();
    }

    private JPanel createCalculatorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TEAL), " Process New Payment ", 0, 0, new Font("Segoe UI", Font.BOLD, 14), TEAL
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Row 1 ---
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Employee ID:"), gbc);

        JPanel searchBox = new JPanel(new BorderLayout(5,0));
        searchBox.setBackground(Color.WHITE);
        txtEmpId = new JTextField(10);
        JButton btnFind = new JButton("🔍");
        btnFind.setBackground(TEAL);
        btnFind.setForeground(Color.WHITE);
        btnFind.addActionListener(e -> findEmployee());
        searchBox.add(txtEmpId, BorderLayout.CENTER);
        searchBox.add(btnFind, BorderLayout.EAST);

        gbc.gridx = 1; panel.add(searchBox, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Select Month:"), gbc);
        gbc.gridx = 3;
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        cmbMonth = new JComboBox<>(months);
        panel.add(cmbMonth, gbc);

        // --- Row 2 ---
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        txtBasic = new JTextField();
        txtBasic.setEditable(false);
        txtBasic.setBackground(new Color(245,245,245));
        panel.add(txtBasic, gbc);

        gbc.gridx = 2; panel.add(new JLabel("OT Hours:"), gbc);
        gbc.gridx = 3; txtOtHours = new JTextField("0"); panel.add(txtOtHours, gbc);

        // --- Row 3 ---
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Allowances (+):"), gbc);
        gbc.gridx = 1; txtAllowance = new JTextField("0.0"); panel.add(txtAllowance, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Deductions (-):"), gbc);
        gbc.gridx = 3; txtDeduction = new JTextField("0.0"); panel.add(txtDeduction, gbc);

        // --- Row 4 (Button) ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnProcess = new JButton("⚡ Calculate & Save Payment");
        btnProcess.setBackground(TEAL);
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProcess.setPreferredSize(new Dimension(250, 35));
        btnProcess.addActionListener(e -> processPayroll());

        panel.add(btnProcess, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(" Payment History "));

        String[] cols = {"ID", "Emp ID", "Month", "Basic", "OT Hrs", "OT Earn", "Net Salary", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(TEAL);
        table.getTableHeader().setForeground(Color.WHITE);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC ---
    private void findEmployee() {
        try {
            int id = Integer.parseInt(txtEmpId.getText());
            double basic = controller.getBasicSalary(id);
            if(basic > 0) {
                txtBasic.setText(String.valueOf(basic));
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found!");
                txtBasic.setText("");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID");
        }
    }

    private void processPayroll() {
        if(txtBasic.getText().isEmpty()) return;
        try {
            int id = Integer.parseInt(txtEmpId.getText());
            String month = cmbMonth.getSelectedItem().toString();
            double basic = Double.parseDouble(txtBasic.getText());
            int ot = Integer.parseInt(txtOtHours.getText());
            double allow = Double.parseDouble(txtAllowance.getText());
            double deduct = Double.parseDouble(txtDeduction.getText());

            if(controller.savePayroll(id, month, basic, ot, allow, deduct)) {
                refreshTable(); // Update table automatically
                // Optional: Clear fields
                txtEmpId.setText(""); txtBasic.setText(""); txtOtHours.setText("0");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Check inputs!");
        }
    }

    private void refreshTable() {
        controller.loadPayrollHistory(model);
    }
}