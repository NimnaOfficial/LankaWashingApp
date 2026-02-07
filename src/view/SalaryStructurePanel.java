package view;

import controller.EmployeeController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SalaryStructurePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtBasicSalary;
    private JLabel lblId;
    private EmployeeController controller;
    private final Color TEAL = new Color(7, 81, 173);

    public SalaryStructurePanel() {
        controller = new EmployeeController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createUpdatePanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(new Color(248, 249, 250));
        JLabel title = new JLabel("Basic Salary Registry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEAL);
        p.add(title);
        return p;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Headers matching the specific query
        String[] cols = {"ID", "Name", "Designation", "Basic Salary (LKR)"};

        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(TEAL);
        table.getTableHeader().setForeground(Color.WHITE);

        // Fill text fields when row clicked
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) {
                    lblId.setText(model.getValueAt(row, 0).toString());
                    txtName.setText(model.getValueAt(row, 1).toString());
                    // Index 3 is now definitely Salary
                    txtBasicSalary.setText(model.getValueAt(row, 3).toString());
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(" Update Basic Salary "));

        lblId = new JLabel("-");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblId.setForeground(TEAL);

        txtName = new JTextField(15);
        txtName.setEditable(false);

        txtBasicSalary = new JTextField(10);

        JButton btnUpdate = new JButton("Update Salary");
        btnUpdate.setBackground(new Color(255, 152, 0)); // Orange
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.addActionListener(e -> updateSalary());

        panel.add(new JLabel("ID:"));
        panel.add(lblId);
        panel.add(new JLabel("Name:"));
        panel.add(txtName);
        panel.add(new JLabel("New Basic Salary:"));
        panel.add(txtBasicSalary);
        panel.add(btnUpdate);

        return panel;
    }

    private void updateSalary() {
        if(lblId.getText().equals("-")) return;
        try {
            int id = Integer.parseInt(lblId.getText());
            double newSal = Double.parseDouble(txtBasicSalary.getText());

            // Calls the new Controller method
            if(controller.updateBasicSalary(id, newSal)) {
                JOptionPane.showMessageDialog(this, "Salary Updated Successfully!");
                refreshTable();

                // Clear inputs
                txtBasicSalary.setText("");
                txtName.setText("");
                lblId.setText("-");
            } else {
                JOptionPane.showMessageDialog(this, "Update Failed.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
        }
    }

    private void refreshTable() {
        // Calls the new Controller method (Fixes the Phone/Salary column issue)
        controller.loadEmployeeSalaries(model);
    }
}