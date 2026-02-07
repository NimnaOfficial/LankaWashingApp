package view;

import controller.TechnicianController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TechnicianPanel extends JPanel {

    private JTextField txtName;
    private JComboBox<String> cmbStatus;
    private JTable table;
    private DefaultTableModel model;
    private TechnicianController controller;
    private int selectedId = -1;

    // Theme: Engineering Blue
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color HEADER_BG = new Color(15, 76, 117);

    public TechnicianPanel() {
        controller = new TechnicianController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 244, 248));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), " Technician Management ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Technician Name:"), gbc);

        gbc.gridx = 1;
        txtName = new JTextField(20);
        panel.add(txtName, gbc);

        // Row 1: Status
        gbc.gridx = 2;
        panel.add(new JLabel("Availability:"), gbc);

        gbc.gridx = 3;
        String[] statuses = {"Available", "Busy", "On Leave", "Inactive"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setBackground(Color.WHITE);
        panel.add(cmbStatus, gbc);

        // --- BUTTONS (Add Button Removed) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnUpdate = createButton("Update", new Color(23, 162, 184)); // Teal
        JButton btnDelete = createButton("Delete", new Color(220, 53, 69));  // Red
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnUpdate.addActionListener(e -> updateTech());
        btnDelete.addActionListener(e -> deleteTech());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        // Add Button Panel to Main Form
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Name", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Click Listener to populate form
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) {
                    selectedId = (int) model.getValueAt(row, 0);
                    txtName.setText(model.getValueAt(row, 1).toString());
                    cmbStatus.setSelectedItem(model.getValueAt(row, 2).toString());
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC METHODS ---

    private void updateTech() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a technician to update.");
            return;
        }
        if(controller.updateTechnician(selectedId, txtName.getText(), cmbStatus.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "Technician Updated!");
            refreshTable();
            clearForm();
        }
    }

    private void deleteTech() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a technician to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this technician?", "Confirm", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            controller.deleteTechnician(selectedId);
            refreshTable();
            clearForm();
        }
    }

    private void refreshTable() {
        controller.loadTechnicians(model);
    }

    private void clearForm() {
        txtName.setText("");
        cmbStatus.setSelectedIndex(0);
        selectedId = -1;
        table.clearSelection();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}