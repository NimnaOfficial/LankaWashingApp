package view;

import controller.EmployeeController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EmployeeManagementPanel extends JPanel {

    // Inputs
    private JTextField txtName, txtNic, txtPhone, txtSalary;
    private JTextArea txtAddress;
    private JComboBox<String> cmbDesignation, cmbDepartment, cmbStatus;
    private JTable table;
    private DefaultTableModel model;

    private EmployeeController controller;
    private int selectedId = -1;

    // Modern Colors (Matching HR Dashboard)
    private final Color TEAL = new Color(7, 81, 173);
    private final Color LIGHT_BG = new Color(248, 249, 250);
    private final Color WHITE = Color.WHITE;

    public EmployeeManagementPanel() {
        controller = new EmployeeController();
        setLayout(new BorderLayout(20, 20));
        setBackground(LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Layout Components
        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        // Styled Border
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(TEAL), " Employee Details ",
                        0, 0, new Font("Segoe UI", Font.BOLD, 14), TEAL)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -- Row 1 --
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; txtName = new JTextField(15); panel.add(txtName, gbc);

        gbc.gridx = 2; panel.add(new JLabel("NIC Number:"), gbc);
        gbc.gridx = 3; txtNic = new JTextField(15); panel.add(txtNic, gbc);

        // -- Row 2 --
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; txtPhone = new JTextField(15); panel.add(txtPhone, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 3; txtSalary = new JTextField(15); panel.add(txtSalary, gbc);

        // -- Row 3 --
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        String[] depts = {"HR", "Production", "Inventory", "Maintenance", "Sales"};
        cmbDepartment = new JComboBox<>(depts);
        panel.add(cmbDepartment, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Designation:"), gbc);
        gbc.gridx = 3;
        String[] desigs = {"Manager", "Supervisor", "Operator", "Technician", "Helper"};
        cmbDesignation = new JComboBox<>(desigs);
        panel.add(cmbDesignation, gbc);

        // -- Row 4 (Address - Spans 2 rows) --
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtAddress = new JTextArea(2, 20);
        txtAddress.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtAddress, gbc);
        gbc.gridwidth = 1; // Reset

        // -- Row 5 (Status & Buttons) --
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"Active", "Resigned", "Retired"});
        panel.add(cmbStatus, gbc);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(WHITE);

        JButton btnAdd = createButton("Save", TEAL);
        JButton btnUpdate = createButton("Update", new Color(255, 152, 0)); // Orange
        JButton btnDelete = createButton("Delete", new Color(220, 53, 69)); // Red
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addEmployee());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 2; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Name", "NIC", "Phone", "Address", "Designation", "Department", "Salary", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(TEAL);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) {
                    selectedId = (int) model.getValueAt(row, 0);
                    txtName.setText(model.getValueAt(row, 1).toString());
                    txtNic.setText(model.getValueAt(row, 2).toString());
                    txtPhone.setText(model.getValueAt(row, 3).toString());
                    txtAddress.setText(model.getValueAt(row, 4).toString());
                    cmbDesignation.setSelectedItem(model.getValueAt(row, 5).toString());
                    cmbDepartment.setSelectedItem(model.getValueAt(row, 6).toString());
                    txtSalary.setText(model.getValueAt(row, 7).toString());
                    cmbStatus.setSelectedItem(model.getValueAt(row, 8).toString());
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC ---
    private void addEmployee() {
        if(validateForm()) {
            try {
                double sal = Double.parseDouble(txtSalary.getText());
                if(controller.addEmployee(txtName.getText(), txtNic.getText(), txtPhone.getText(),
                        txtAddress.getText(), cmbDesignation.getSelectedItem().toString(),
                        cmbDepartment.getSelectedItem().toString(), sal)) {
                    JOptionPane.showMessageDialog(this, "Employee Added Successfully!");
                    refreshTable(); clearForm();
                }
            } catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Salary must be a valid number!");
            }
        }
    }

    private void updateEmployee() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.");
            return;
        }
        try {
            double sal = Double.parseDouble(txtSalary.getText());
            if(controller.updateEmployee(selectedId, txtName.getText(), txtNic.getText(), txtPhone.getText(),
                    txtAddress.getText(), cmbDesignation.getSelectedItem().toString(),
                    cmbDepartment.getSelectedItem().toString(), sal, cmbStatus.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "Employee Updated!");
                refreshTable(); clearForm();
            }
        } catch(NumberFormatException e) { JOptionPane.showMessageDialog(this, "Invalid Salary"); }
    }

    private void deleteEmployee() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }
        if(JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(controller.deleteEmployee(selectedId)) {
                JOptionPane.showMessageDialog(this, "Employee Deleted!");
                refreshTable(); clearForm();
            }
        }
    }

    private void refreshTable() { controller.loadEmployees(model); }

    private void clearForm() {
        txtName.setText(""); txtNic.setText(""); txtPhone.setText(""); txtSalary.setText(""); txtAddress.setText("");
        cmbDesignation.setSelectedIndex(0); cmbDepartment.setSelectedIndex(0);
        selectedId = -1; table.clearSelection();
    }

    private boolean validateForm() {
        if(txtName.getText().isEmpty() || txtNic.getText().isEmpty() || txtSalary.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Name, NIC, and Salary.");
            return false;
        }
        return true;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }
}