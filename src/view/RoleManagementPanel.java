package view;

import controller.RoleController; // NEW IMPORT
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoleManagementPanel extends JPanel {

    private JTextField txtRoleName;
    private JTextArea txtPermissions;
    private JTable roleTable;
    private DefaultTableModel tableModel;
    private RoleController controller;
    private int selectedRoleId = -1;

    private final Color PRIMARY = new Color(13, 110, 253);
    private final Color SUCCESS = new Color(25, 135, 84);
    private final Color DANGER = new Color(220, 53, 69);

    public RoleManagementPanel() {
        controller = new RoleController();
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Role Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Role Name:"), gbc);
        gbc.gridx = 1; txtRoleName = new JTextField(20); panel.add(txtRoleName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Permissions:"), gbc);
        gbc.gridx = 1;
        txtPermissions = new JTextArea(3, 20);
        txtPermissions.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtPermissions, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = createButton("Save", PRIMARY);
        JButton btnUpdate = createButton("Update", SUCCESS);
        JButton btnDelete = createButton("Delete", DANGER);
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addRole());
        btnUpdate.addActionListener(e -> updateRole());
        btnDelete.addActionListener(e -> deleteRole());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(btnPanel, gbc);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Role Name", "Permissions"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        roleTable = new JTable(tableModel);
        roleTable.setRowHeight(25);

        roleTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = roleTable.getSelectedRow();
                if (row != -1) {
                    selectedRoleId = (int) tableModel.getValueAt(row, 0);
                    txtRoleName.setText(tableModel.getValueAt(row, 1).toString());
                    txtPermissions.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });
        panel.add(new JScrollPane(roleTable), BorderLayout.CENTER);
        return panel;
    }

    private void addRole() {
        if(txtRoleName.getText().isEmpty()) return;
        if(controller.addRole(txtRoleName.getText(), txtPermissions.getText())) {
            JOptionPane.showMessageDialog(this, "Role Added!");
            refreshTable();
            clearForm();
        }
    }
    private void updateRole() {
        if(selectedRoleId == -1) return;
        if(controller.updateRole(selectedRoleId, txtRoleName.getText(), txtPermissions.getText())) {
            JOptionPane.showMessageDialog(this, "Role Updated!");
            refreshTable();
            clearForm();
        }
    }
    private void deleteRole() {
        if(selectedRoleId == -1) return;
        if(JOptionPane.showConfirmDialog(this, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(controller.deleteRole(selectedRoleId)) {
                JOptionPane.showMessageDialog(this, "Role Deleted!");
                refreshTable();
                clearForm();
            }
        }
    }
    private void refreshTable() { controller.loadRolesToTable(tableModel); }
    private void clearForm() { txtRoleName.setText(""); txtPermissions.setText(""); selectedRoleId = -1; }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(Color.WHITE); return btn;
    }
}