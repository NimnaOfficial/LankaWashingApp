package view;

import com.toedter.calendar.JDateChooser;
import controller.FaultLogController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class FaultLogPanel extends JPanel {

    private JComboBox<String> cmbMachine, cmbSeverity;
    private JTextArea txtDescription;
    private JDateChooser dateReported;
    private JTable table;
    private DefaultTableModel model;

    private FaultLogController controller;
    private int selectedId = -1;
    private HashMap<String, Integer> machineMap; // Stores "Name" -> ID

    // Theme: Engineering Blue
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color HEADER_BG = new Color(15, 76, 117);

    public FaultLogPanel() {
        controller = new FaultLogController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 244, 248));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadMachines(); // Populate dropdown
        refreshTable();
    }

    private void loadMachines() {
        machineMap = controller.getMachineMap();
        cmbMachine.removeAllItems();
        for (String m : machineMap.keySet()) {
            cmbMachine.addItem(m);
        }
        cmbMachine.setSelectedIndex(-1);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), " Report Machine Fault ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Select Machine:"), gbc);
        gbc.gridx = 1;
        cmbMachine = new JComboBox<>();
        cmbMachine.setBackground(Color.WHITE);
        cmbMachine.setPreferredSize(new Dimension(200, 25));
        panel.add(cmbMachine, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Date Reported:"), gbc);
        gbc.gridx = 3;
        dateReported = new JDateChooser();
        dateReported.setDateFormatString("yyyy-MM-dd");
        dateReported.setDate(new Date());
        dateReported.setPreferredSize(new Dimension(150, 25));
        panel.add(dateReported, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Severity Level:"), gbc);
        gbc.gridx = 1;
        String[] levels = {"Low", "Medium", "High", "Critical"};
        cmbSeverity = new JComboBox<>(levels);
        cmbSeverity.setBackground(Color.WHITE);
        panel.add(cmbSeverity, gbc);

        // Row 3 (Full Width Description)
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Fault Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtDescription = new JTextArea(3, 20);
        txtDescription.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDescription, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Log Fault", ACCENT_COLOR);
        JButton btnUpdate = createButton("Update", new Color(23, 162, 184));
        JButton btnDelete = createButton("Delete", new Color(220, 53, 69));
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addFault());
        btnUpdate.addActionListener(e -> updateFault());
        btnDelete.addActionListener(e -> deleteFault());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Machine", "Date", "Severity", "Description"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) {
                    try {
                        selectedId = (int) model.getValueAt(row, 0);

                        cmbMachine.setSelectedItem(model.getValueAt(row, 1).toString());

                        Object dateObj = model.getValueAt(row, 2);
                        if(dateObj != null) {
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateObj.toString());
                            dateReported.setDate(date);
                        }

                        cmbSeverity.setSelectedItem(model.getValueAt(row, 3).toString());
                        txtDescription.setText(model.getValueAt(row, 4).toString());
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC ---
    private void addFault() {
        if(cmbMachine.getSelectedItem() == null || dateReported.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Machine and Date are required!");
            return;
        }
        try {
            int machineId = machineMap.get(cmbMachine.getSelectedItem().toString());
            String date = new SimpleDateFormat("yyyy-MM-dd").format(dateReported.getDate());

            if(controller.addFault(machineId, date, cmbSeverity.getSelectedItem().toString(), txtDescription.getText())) {
                JOptionPane.showMessageDialog(this, "Fault Logged!");
                refreshTable(); clearForm();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateFault() {
        if(selectedId == -1) return;
        try {
            int machineId = machineMap.get(cmbMachine.getSelectedItem().toString());
            String date = new SimpleDateFormat("yyyy-MM-dd").format(dateReported.getDate());

            if(controller.updateFault(selectedId, machineId, date, cmbSeverity.getSelectedItem().toString(), txtDescription.getText())) {
                JOptionPane.showMessageDialog(this, "Fault Updated!");
                refreshTable(); clearForm();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteFault() {
        if(selectedId != -1 && JOptionPane.showConfirmDialog(this, "Delete Fault?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.deleteFault(selectedId);
            refreshTable(); clearForm();
        }
    }

    private void refreshTable() { controller.loadFaults(model); }

    private void clearForm() {
        cmbMachine.setSelectedIndex(-1);
        cmbSeverity.setSelectedIndex(0);
        txtDescription.setText("");
        dateReported.setDate(new Date());
        selectedId = -1; table.clearSelection();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}