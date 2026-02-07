package view;

import controller.SupplierController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

public class SupplierPanel extends JPanel {

    private JTextField txtCompany, txtName, txtPhone, txtEmail;
    private JTable table;
    private DefaultTableModel model;
    private SupplierController controller;
    private int selectedId = -1;

    // Theme Colors
    private final Color ACCENT_COLOR = new Color(255, 140, 0);
    private final Color TABLE_HEADER_BG = new Color(33, 37, 41);

    public SupplierPanel() {
        controller = new SupplierController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR),
                        " Supplier Information ",
                        0, 0,
                        new Font("Segoe UI", Font.BOLD, 14),
                        ACCENT_COLOR
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Company Name:"), gbc);
        gbc.gridx = 1;
        txtCompany = new JTextField(15);
        panel.add(txtCompany, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Contact Person:"), gbc);
        gbc.gridx = 3;
        txtName = new JTextField(15);
        panel.add(txtName, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(15);
        panel.add(txtPhone, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 3;
        txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("💾 Save Supplier", ACCENT_COLOR);
        JButton btnUpdate = createButton("✎ Update", new Color(13, 110, 253));
        JButton btnDelete = createButton("🗑 Delete", new Color(220, 53, 69));
        JButton btnClear = createButton("⟳ Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSupplier());
        btnDelete.addActionListener(e -> deleteSupplier());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Company Name", "Contact Person", "Phone", "Email"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionBackground(new Color(255, 224, 178));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedId = (int) model.getValueAt(row, 0);
                    txtCompany.setText(model.getValueAt(row, 1).toString());
                    txtName.setText(model.getValueAt(row, 2).toString());
                    txtPhone.setText(model.getValueAt(row, 3).toString());
                    txtEmail.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ---------------- USE CASE LOGIC ----------------

    private void addSupplier() {
        if (validateFields()) {
            if (controller.addSupplier(
                    txtCompany.getText(),
                    txtName.getText(),
                    txtPhone.getText(),
                    txtEmail.getText())) {

                JOptionPane.showMessageDialog(this, "Supplier Registered Successfully! Defualt password is 'supplier123'. Please ask them to change it after first login.");
                refreshTable();
                clearForm();
            }
        }
    }

    private void updateSupplier() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier from the table.");
            return;
        }

        if (validateFields()) {
            if (controller.updateSupplier(
                    selectedId,
                    txtCompany.getText(),
                    txtName.getText(),
                    txtPhone.getText(),
                    txtEmail.getText())) {

                JOptionPane.showMessageDialog(this, "Supplier Details Updated!");
                refreshTable();
                clearForm();
            }
        }
    }

    private void deleteSupplier() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this supplier?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteSupplier(selectedId)) {
                JOptionPane.showMessageDialog(this, "Supplier Deleted.");
                refreshTable();
                clearForm();
            }
        }
    }

    // ---------------- HELPERS ----------------

    private void refreshTable() {
        model.setRowCount(0);
        controller.getAllSuppliers().forEach(s ->
                model.addRow(new Object[]{
                        s.getId(),
                        s.getCompanyName(),
                        s.getContactName(),
                        s.getPhone(),
                        s.getEmail()
                })
        );
    }

    private void clearForm() {
        txtCompany.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        selectedId = -1;
        table.clearSelection();
    }

    private boolean validateFields() {
        if (txtCompany.getText().isEmpty() ||
                txtName.getText().isEmpty() ||
                txtPhone.getText().isEmpty() ||
                txtEmail.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "All fields are required.");
            return false;
        }

        if (!Pattern.matches("^\\+?[0-9]{7,15}$", txtPhone.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid phone number.");
            return false;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", txtEmail.getText())) {
            JOptionPane.showMessageDialog(this, "Invalid email address.");
            return false;
        }

        return true;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }
}
