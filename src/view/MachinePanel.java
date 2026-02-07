package view;

import com.toedter.calendar.JDateChooser;
import controller.MachineController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MachinePanel extends JPanel {

    private JTextField txtType;
    private JDateChooser dateLastService; // ✅ Changed to JDateChooser
    private JComboBox<String> cmbStatus;
    private JTable table;
    private DefaultTableModel model;
    private MachineController controller;
    private int selectedId = -1;

    // Theme: Engineering Blue
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color HEADER_BG = new Color(15, 76, 117);

    public MachinePanel() {
        controller = new MachineController();
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

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR), " Machine Status Management ",
                        0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Machine Type/Name:"), gbc);
        gbc.gridx = 1; txtType = new JTextField(15); panel.add(txtType, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Last Service Date:"), gbc);
        gbc.gridx = 3;
        // ✅ JDateChooser Implementation
        dateLastService = new JDateChooser();
        dateLastService.setDateFormatString("yyyy-MM-dd");
        dateLastService.setDate(new Date()); // Default to Today
        dateLastService.setPreferredSize(new Dimension(150, 25)); // Ensure visible size
        panel.add(dateLastService, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Current Status:"), gbc);
        gbc.gridx = 1;
        String[] statuses = {"Available", "Running", "Maintenance", "Offline"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setBackground(Color.WHITE);
        panel.add(cmbStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Add Machine", ACCENT_COLOR);
        JButton btnUpdate = createButton("Update Status", new Color(23, 162, 184));
        JButton btnDelete = createButton("Delete", new Color(220, 53, 69));
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addMachine());
        btnUpdate.addActionListener(e -> updateMachine());
        btnDelete.addActionListener(e -> deleteMachine());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Machine Type", "Last Service", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) {
                    try {
                        selectedId = (int) model.getValueAt(row, 0);
                        txtType.setText(model.getValueAt(row, 1).toString());

                        // ✅ Parse Table Date to JDateChooser
                        Object dateObj = model.getValueAt(row, 2);
                        if(dateObj != null) {
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateObj.toString());
                            dateLastService.setDate(date);
                        }

                        cmbStatus.setSelectedItem(model.getValueAt(row, 3).toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC ---
    private void addMachine() {
        if(txtType.getText().isEmpty() || dateLastService.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Machine Name and Date are required!");
            return;
        }

        // Format Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(dateLastService.getDate());

        if(controller.addMachine(txtType.getText(), dateStr, cmbStatus.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "Machine Added!");
            refreshTable(); clearForm();
        }
    }

    private void updateMachine() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a machine to update.");
            return;
        }
        if(dateLastService.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Invalid Date!");
            return;
        }

        // Format Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(dateLastService.getDate());

        if(controller.updateMachine(selectedId, txtType.getText(), dateStr, cmbStatus.getSelectedItem().toString())) {
            JOptionPane.showMessageDialog(this, "Machine Status Updated!");
            refreshTable(); clearForm();
        }
    }

    private void deleteMachine() {
        if(selectedId == -1) return;
        if(JOptionPane.showConfirmDialog(this, "Delete this machine?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.deleteMachine(selectedId);
            refreshTable(); clearForm();
        }
    }

    private void refreshTable() { controller.loadMachines(model); }

    private void clearForm() {
        txtType.setText("");
        dateLastService.setDate(new Date()); // Reset to Today
        cmbStatus.setSelectedIndex(0);
        selectedId = -1; table.clearSelection();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}