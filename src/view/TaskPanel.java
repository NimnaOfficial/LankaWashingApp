package view;

import com.toedter.calendar.JDateChooser;
import controller.TaskController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class TaskPanel extends JPanel {

    private JComboBox<String> cmbFault, cmbTechnician;
    private JTextField txtCost, txtParts;
    private JTextArea txtDetails;
    private JDateChooser dateTask;
    private JTable table;
    private DefaultTableModel model;

    private TaskController controller;
    private int selectedId = -1;

    private LinkedHashMap<String, Integer> faultMap;
    private LinkedHashMap<String, Integer> techMap;

    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color HEADER_BG = new Color(15, 76, 117);

    public TaskPanel() {
        controller = new TaskController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 244, 248));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadDropdowns();
        refreshTable();
    }

    private void loadDropdowns() {
        faultMap = controller.getFaultMap();
        techMap = controller.getTechnicianMap();

        cmbFault.removeAllItems();
        for (String f : faultMap.keySet()) cmbFault.addItem(f);
        cmbFault.setSelectedIndex(-1);

        cmbTechnician.removeAllItems();
        for (String t : techMap.keySet()) cmbTechnician.addItem(t);
        cmbTechnician.setSelectedIndex(-1);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), " Maintenance Task Log ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Select Fault Log:"), gbc);
        gbc.gridx = 1;
        cmbFault = new JComboBox<>();
        cmbFault.setBackground(Color.WHITE);
        cmbFault.setPreferredSize(new Dimension(200, 25));
        panel.add(cmbFault, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Assign Technician:"), gbc);
        gbc.gridx = 3;
        cmbTechnician = new JComboBox<>();
        cmbTechnician.setBackground(Color.WHITE);
        cmbTechnician.setPreferredSize(new Dimension(200, 25));
        panel.add(cmbTechnician, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Parts Used:"), gbc);
        gbc.gridx = 1;
        txtParts = new JTextField(15);
        panel.add(txtParts, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Repair Cost:"), gbc);
        gbc.gridx = 3;
        txtCost = new JTextField(15);
        txtCost.setText("0.0");
        panel.add(txtCost, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Task Date:"), gbc);
        gbc.gridx = 1;
        dateTask = new JDateChooser();
        dateTask.setDateFormatString("yyyy-MM-dd");
        dateTask.setDate(new Date());
        dateTask.setPreferredSize(new Dimension(200, 25));
        panel.add(dateTask, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Repair Details:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtDetails = new JTextArea(3, 20);
        txtDetails.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDetails, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Log Task", ACCENT_COLOR);
        JButton btnUpdate = createButton("Update", new Color(23, 162, 184));
        JButton btnDelete = createButton("Delete", new Color(220, 53, 69));
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addTask());
        btnUpdate.addActionListener(e -> updateTask());
        btnDelete.addActionListener(e -> deleteTask());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Fault Log", "Technician", "Details", "Parts", "Cost", "Date"};
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
                        // ✅ FIX: Use safe helper method to avoid NullPointerException
                        selectedId = (int) model.getValueAt(row, 0);

                        String fVal = getSafeString(model.getValueAt(row, 1));
                        String tVal = getSafeString(model.getValueAt(row, 2));

                        cmbFault.setSelectedItem(fVal);

                        // Fuzzy select technician
                        if (!tVal.isEmpty()) {
                            for(int i=0; i<cmbTechnician.getItemCount(); i++) {
                                if(cmbTechnician.getItemAt(i).startsWith(tVal)) {
                                    cmbTechnician.setSelectedIndex(i); break;
                                }
                            }
                        }

                        txtDetails.setText(getSafeString(model.getValueAt(row, 3)));
                        txtParts.setText(getSafeString(model.getValueAt(row, 4)));
                        txtCost.setText(getSafeString(model.getValueAt(row, 5)));

                        Object dateObj = model.getValueAt(row, 6);
                        if(dateObj != null) {
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateObj.toString());
                            dateTask.setDate(date);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ✅ NEW Helper Method: Returns empty string if value is null
    private String getSafeString(Object value) {
        return (value == null) ? "" : value.toString();
    }

    private void addTask() {
        if(cmbFault.getSelectedItem() == null || cmbTechnician.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Select Fault and Technician!");
            return;
        }
        try {
            int faultId = faultMap.get(cmbFault.getSelectedItem().toString());
            int techId = techMap.get(cmbTechnician.getSelectedItem().toString());
            double cost = txtCost.getText().isEmpty() ? 0.0 : Double.parseDouble(txtCost.getText());
            String date = new SimpleDateFormat("yyyy-MM-dd").format(dateTask.getDate());

            if(controller.addTask(faultId, techId, txtDetails.getText(), txtParts.getText(), cost, date)) {
                JOptionPane.showMessageDialog(this, "Task Logged!");
                refreshTable(); clearForm();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateTask() {
        if(selectedId == -1) return;
        try {
            int faultId = faultMap.get(cmbFault.getSelectedItem().toString());
            int techId = techMap.get(cmbTechnician.getSelectedItem().toString());
            double cost = txtCost.getText().isEmpty() ? 0.0 : Double.parseDouble(txtCost.getText());
            String date = new SimpleDateFormat("yyyy-MM-dd").format(dateTask.getDate());

            if(controller.updateTask(selectedId, faultId, techId, txtDetails.getText(), txtParts.getText(), cost, date)) {
                JOptionPane.showMessageDialog(this, "Task Updated!");
                refreshTable(); clearForm();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteTask() {
        if(selectedId != -1 && JOptionPane.showConfirmDialog(this, "Delete Task?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.deleteTask(selectedId);
            refreshTable(); clearForm();
        }
    }

    private void refreshTable() { controller.loadTasks(model); }

    private void clearForm() {
        cmbFault.setSelectedIndex(-1);
        cmbTechnician.setSelectedIndex(-1);
        txtDetails.setText(""); txtParts.setText(""); txtCost.setText("0.0");
        dateTask.setDate(new Date());
        selectedId = -1; table.clearSelection();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}