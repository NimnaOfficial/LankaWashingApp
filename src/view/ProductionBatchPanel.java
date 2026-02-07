package view;

import com.toedter.calendar.JDateChooser;
import controller.ProductionBatchController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductionBatchPanel extends JPanel {

    private JTextField txtProductName, txtOrderId, txtCost;
    private JDateChooser dateStart, dateEnd;
    private JComboBox<String> cmbStatus;
    private JTable table;
    private DefaultTableModel model;
    private ProductionBatchController controller;
    private int selectedId = -1;

    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color HEADER_BG = new Color(15, 76, 117);

    public ProductionBatchPanel() {
        controller = new ProductionBatchController();
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
                BorderFactory.createLineBorder(ACCENT_COLOR), " Batch Details ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Order ID (Linked):"), gbc);
        gbc.gridx = 1;
        txtOrderId = new JTextField(15);
        txtOrderId.setEditable(false);
        txtOrderId.setBackground(new Color(230, 230, 230));
        panel.add(txtOrderId, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 3; txtProductName = new JTextField(15); panel.add(txtProductName, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        dateStart = new JDateChooser();
        dateStart.setDateFormatString("yyyy-MM-dd");
        dateStart.setDate(new Date());
        dateStart.setPreferredSize(new Dimension(150, 25));
        panel.add(dateStart, gbc);

        gbc.gridx = 2; panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 3;
        dateEnd = new JDateChooser();
        dateEnd.setDateFormatString("yyyy-MM-dd");
        dateEnd.setPreferredSize(new Dimension(150, 25));
        panel.add(dateEnd, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Total Cost:"), gbc);
        gbc.gridx = 1;
        txtCost = new JTextField(15);
        txtCost.setText("0.0");
        txtCost.setEditable(false); // ✅ Read-only: Calculated automatically
        txtCost.setBackground(new Color(245, 245, 245));
        panel.add(txtCost, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        cmbStatus = new JComboBox<>(new String[]{"Scheduled", "In Progress", "Completed", "Cancelled"});
        cmbStatus.setBackground(Color.WHITE);
        panel.add(cmbStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("✨ Create New Batch");
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addBatch());

        JButton btnUpdate = new JButton("💾 Update Batch");
        btnUpdate.setBackground(new Color(23, 162, 184));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.addActionListener(e -> updateBatch());

        JButton btnDelete = new JButton("🗑️ Delete");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteBatch());

        JButton btnClear = new JButton("Clear");
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
        String[] cols = {"Batch ID", "Order Link", "Product Name", "Start Date", "End Date", "Cost", "Status"};
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
                        txtOrderId.setText(model.getValueAt(row, 1).toString());
                        txtProductName.setText(model.getValueAt(row, 2).toString());

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Object sDate = model.getValueAt(row, 3);
                        if(sDate != null) dateStart.setDate(sdf.parse(sDate.toString()));

                        Object eDate = model.getValueAt(row, 4);
                        if(eDate != null && !eDate.toString().isEmpty()) dateEnd.setDate(sdf.parse(eDate.toString()));
                        else dateEnd.setDate(null);

                        // ✅ Populate calculated cost from table
                        txtCost.setText(model.getValueAt(row, 5).toString());
                        cmbStatus.setSelectedItem(model.getValueAt(row, 6).toString());
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void addBatch() {
        if(txtProductName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Name is required!");
            return;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String sDate = sdf.format(dateStart.getDate());
            String eDate = (dateEnd.getDate() != null) ? sdf.format(dateEnd.getDate()) : "";

            // ✅ No cost parameter sent
            if(controller.addBatch("Manual", txtProductName.getText(), sDate, eDate, cmbStatus.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "New Batch Created Successfully!");
                refreshTable();
                clearForm();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Input!");
        }
    }

    private void updateBatch() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a batch to update.");
            return;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String sDate = sdf.format(dateStart.getDate());
            String eDate = (dateEnd.getDate() != null) ? sdf.format(dateEnd.getDate()) : "";

            // ✅ No cost parameter sent
            if(controller.updateBatch(selectedId, "Manual", txtProductName.getText(), sDate, eDate, cmbStatus.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "Batch Updated!");
                refreshTable(); clearForm();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Input!");
        }
    }

    private void deleteBatch() {
        if(selectedId != -1 && JOptionPane.showConfirmDialog(this, "Delete Batch?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.deleteBatch(selectedId);
            refreshTable(); clearForm();
        }
    }

    private void refreshTable() { controller.loadBatches(model); }

    private void clearForm() {
        txtOrderId.setText(""); txtProductName.setText(""); txtCost.setText("0.0");
        dateStart.setDate(new Date()); dateEnd.setDate(null);
        selectedId = -1; table.clearSelection();
    }
}