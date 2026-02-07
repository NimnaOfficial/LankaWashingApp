package view;

import controller.UserController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserManagementPanel extends JPanel {

    private JTextField txtName, txtEmail, txtPhone;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole, cmbStatus;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private UserController controller;
    private int selectedUserId = -1;

    // Colors
    private final Color PRIMARY = new Color(13, 110, 253);
    private final Color DANGER = new Color(220, 53, 69);
    private final Color SUCCESS = new Color(25, 135, 84);

    public UserManagementPanel() {
        controller = new UserController();
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
        panel.setBorder(BorderFactory.createTitledBorder("User Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Rows
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; txtName = new JTextField(15); panel.add(txtName, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3; txtEmail = new JTextField(15); panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; txtPhone = new JTextField(15); panel.add(txtPhone, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 3; txtPassword = new JPasswordField(15); panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;

        // ✅ UPDATED: Added Roles 8, 9, 10
        String[] roles = {
                "1 - Admin",
                "2 - HR Manager",
                "3 - Production Manager",
                "4 - Inventory Manager",
                "5 - Maintenance Manager",
                "6 - Technician",
                "7 - Operator",
                "8 - Dyer",
                "9 - Washer",
                "10 - Dryer"
        };
        cmbRole = new JComboBox<>(roles);
        panel.add(cmbRole, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        panel.add(cmbStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Add", PRIMARY);
        JButton btnUpdate = createButton("Update", SUCCESS);
        JButton btnDelete = createButton("Delete", DANGER);
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"ID", "Name", "Email", "Phone", "Role", "Status", "RoleID_Hidden"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        userTable.getTableHeader().setBackground(new Color(230, 230, 230));

        // ✅ HIDE THE 'RoleID_Hidden' COLUMN (Index 6)
        userTable.getColumnModel().getColumn(6).setMinWidth(0);
        userTable.getColumnModel().getColumn(6).setMaxWidth(0);
        userTable.getColumnModel().getColumn(6).setWidth(0);

        userTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = userTable.getSelectedRow();
                if (row != -1) {
                    selectedUserId = (int) tableModel.getValueAt(row, 0);
                    txtName.setText(tableModel.getValueAt(row, 1).toString());
                    txtEmail.setText(tableModel.getValueAt(row, 2).toString());
                    txtPhone.setText(tableModel.getValueAt(row, 3).toString());
                    cmbStatus.setSelectedItem(tableModel.getValueAt(row, 5).toString());

                    // ✅ UPDATED: Handle logic for all 10 Roles
                    // The dropdown index matches (RoleID - 1) perfectly.
                    int rId = (int) tableModel.getValueAt(row, 6);
                    if(rId > 0 && rId <= 10) {
                        cmbRole.setSelectedIndex(rId - 1);
                    }
                }
            }
        });

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC METHODS ---
    private void addUser() {
        if(validateFields()) {
            String pass = new String(txtPassword.getPassword());
            // Dropdown index 0 = Role 1, index 1 = Role 2, ... index 9 = Role 10
            int roleId = cmbRole.getSelectedIndex() + 1;

            if (controller.addUser(txtName.getText(), txtEmail.getText(), txtPhone.getText(), pass, roleId, cmbStatus.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "User Added Successfully!");
                refreshTable();
                clearForm();
            }
        }
    }

    private void updateUser() {
        if (selectedUserId == -1) return;
        int roleId = cmbRole.getSelectedIndex() + 1;

        if (controller.updateUser(selectedUserId, txtName.getText(), txtEmail.getText(), txtPhone.getText(), roleId, cmbStatus.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "User Updated!");
            refreshTable();
            clearForm();
        }
    }

    private void deleteUser() {
        if (selectedUserId == -1) return;
        if (JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (controller.deleteUser(selectedUserId)) {
                JOptionPane.showMessageDialog(this, "User Deleted!");
                refreshTable();
                clearForm();
            }
        }
    }

    private void refreshTable() {
        controller.loadUsers(tableModel);
    }

    private void clearForm() {
        txtName.setText(""); txtEmail.setText(""); txtPhone.setText(""); txtPassword.setText("");
        cmbRole.setSelectedIndex(0); cmbStatus.setSelectedIndex(0); selectedUserId = -1;
        userTable.clearSelection();
    }

    private boolean validateFields() {
        if (txtName.getText().isEmpty() || txtEmail.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields.");
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