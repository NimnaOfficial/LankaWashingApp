package view;

import com.toedter.calendar.JDateChooser;
import controller.OrderManagerController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderManagerPanel extends JPanel {

    private JTextField txtCustomer, txtTotal;
    private JTextArea txtItemsPreview, txtReqDescription;
    private JComboBox<String> cmbStatus, cmbBatchAssign;
    private JDateChooser dateExpected;
    private JTable table;
    private DefaultTableModel model;

    private OrderManagerController controller;
    private int selectedOrderId = -1;
    private LinkedHashMap<String, Integer> batchMap;
    private boolean isLoading = false; // Flag to prevent listener loop

    private final Color HEADER_BG = new Color(15, 76, 117);
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color BG_COLOR = new Color(240, 244, 248);

    public OrderManagerPanel() {
        controller = new OrderManagerController();
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTablePanel(), BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);

        refreshTable();
        loadBatches(-1);
    }

    private void loadBatches(int currentOrderId) {
        isLoading = true; // Stop listener firing
        batchMap = controller.getAvailableBatches(currentOrderId);
        cmbBatchAssign.removeAllItems();
        for (String label : batchMap.keySet()) {
            cmbBatchAssign.addItem(label);
        }
        isLoading = false;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(HEADER_BG), " Active Orders List ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), HEADER_BG));
        panel.setBackground(Color.WHITE);

        String[] cols = {"Order ID", "Customer", "Ordered Date", "Expected Date", "Status", "Total ($)", "Req ID", "Description"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(6).setMinWidth(0); table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(7).setMinWidth(0); table.getColumnModel().getColumn(7).setMaxWidth(0);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                loadSelectedOrder(row);
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Manage Order Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ACCENT_COLOR);
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; panel.add(lblTitle, gbc);

        gbc.gridy++; panel.add(new JSeparator(), gbc);

        gbc.gridy++; gbc.gridwidth=1; panel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridy++; txtCustomer = new JTextField(); txtCustomer.setEditable(false);
        txtCustomer.setBackground(new Color(245, 245, 245)); panel.add(txtCustomer, gbc);

        gbc.gridy++; panel.add(new JLabel("Original Request Note:"), gbc);
        gbc.gridy++; txtReqDescription = new JTextArea(3, 20); txtReqDescription.setEditable(false);
        txtReqDescription.setLineWrap(true); txtReqDescription.setWrapStyleWord(true);
        txtReqDescription.setBackground(new Color(240, 248, 255));
        txtReqDescription.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(new JScrollPane(txtReqDescription), gbc);

        gbc.gridy++; panel.add(new JLabel("Order Items:"), gbc);
        gbc.gridy++; txtItemsPreview = new JTextArea(3, 20); txtItemsPreview.setEditable(false);
        txtItemsPreview.setBackground(new Color(255, 255, 240));
        txtItemsPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(new JScrollPane(txtItemsPreview), gbc);

        gbc.gridy++; panel.add(new JLabel("Update Status:"), gbc);
        gbc.gridy++; cmbStatus = new JComboBox<>(new String[]{"Pending", "Processing", "Packaging", "Shipped", "Completed", "Cancelled"});
        cmbStatus.setBackground(Color.WHITE); panel.add(cmbStatus, gbc);

        gbc.gridy++; panel.add(new JLabel("Assign Production Batch:"), gbc);
        gbc.gridy++; cmbBatchAssign = new JComboBox<>();
        cmbBatchAssign.setBackground(new Color(230, 240, 255));

        // ✅ NEW: Listener to update Price preview when batch changes
        cmbBatchAssign.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !isLoading) {
                updatePricePreview();
            }
        });
        panel.add(cmbBatchAssign, gbc);

        gbc.gridy++; panel.add(new JLabel("Expected Delivery:"), gbc);
        gbc.gridy++; dateExpected = new JDateChooser(); panel.add(dateExpected, gbc);

        gbc.gridy++; panel.add(new JLabel("Total Cost (Calculated):"), gbc);
        gbc.gridy++; txtTotal = new JTextField(); txtTotal.setEditable(false); // Read-only now
        panel.add(txtTotal, gbc);

        JLabel lblHint = new JLabel("(Auto-updates from Batch Log)");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 10)); lblHint.setForeground(Color.GRAY);
        gbc.gridy++; panel.add(lblHint, gbc);

        gbc.gridy++; gbc.insets = new Insets(20, 5, 5, 5);
        JButton btnUpdate = new JButton("💾 Update & Process");
        btnUpdate.setBackground(new Color(40, 167, 69)); btnUpdate.setForeground(Color.WHITE);
        btnUpdate.addActionListener(e -> updateOrder()); panel.add(btnUpdate, gbc);

        gbc.gridy++; gbc.insets = new Insets(5, 5, 5, 5);
        JButton btnDelete = new JButton("🗑️ Delete Order");
        btnDelete.setBackground(new Color(220, 53, 69)); btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteOrder()); panel.add(btnDelete, gbc);

        gbc.gridy++; gbc.weighty = 1.0; panel.add(new JLabel(), gbc);
        return panel;
    }

    private void updatePricePreview() {
        try {
            String selectedLabel = (String) cmbBatchAssign.getSelectedItem();
            if (selectedLabel == null) return;

            int batchId = batchMap.getOrDefault(selectedLabel, -1);
            if (batchId != -1) {
                double cost = controller.getBatchCost(batchId);
                txtTotal.setText(String.format("%.2f", cost));
            } else {
                txtTotal.setText("0.00");
            }
        } catch (Exception e) {}
    }

    private void loadSelectedOrder(int row) {
        if (row == -1 || row >= model.getRowCount()) return;
        isLoading = true; // Pause listeners

        try {
            selectedOrderId = (int) model.getValueAt(row, 0);
            txtCustomer.setText(model.getValueAt(row, 1).toString());

            Object dateObj = model.getValueAt(row, 3);
            if (dateObj != null) dateExpected.setDate((Date) dateObj);

            cmbStatus.setSelectedItem(model.getValueAt(row, 4).toString());

            // Initial cost load
            double productionCost = controller.getOrderTotalCost(selectedOrderId);
            txtTotal.setText(String.format("%.2f", productionCost));

            Object descObj = model.getValueAt(row, 7);
            txtReqDescription.setText(descObj != null ? descObj.toString() : "No description provided.");

            String items = controller.getOrderDetails(selectedOrderId);
            txtItemsPreview.setText(items);

            // Reload & Select Batch
            loadBatches(selectedOrderId);
            int assignedBatchId = controller.getAssignedBatchId(selectedOrderId);

            if (assignedBatchId != -1) {
                for (Map.Entry<String, Integer> entry : batchMap.entrySet()) {
                    if (entry.getValue() == assignedBatchId) {
                        cmbBatchAssign.setSelectedItem(entry.getKey());
                        break;
                    }
                }
            } else {
                cmbBatchAssign.setSelectedIndex(0);
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { isLoading = false; }
    }

    private void updateOrder() {
        if (selectedOrderId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }

        String selectedLabel = (String) cmbBatchAssign.getSelectedItem();
        int targetBatchId = batchMap.getOrDefault(selectedLabel, -1);

        if (controller.updateOrder(selectedOrderId, (String)cmbStatus.getSelectedItem(), dateExpected.getDate(), targetBatchId)) {
            JOptionPane.showMessageDialog(this, "Order Updated Successfully!");

            int currentId = selectedOrderId;
            refreshTable();

            // Smart Re-selection
            for (int i = 0; i < table.getRowCount(); i++) {
                if ((int) table.getValueAt(i, 0) == currentId) {
                    table.setRowSelectionInterval(i, i);
                    loadSelectedOrder(i);
                    return;
                }
            }
            clearForm();
        }
    }

    private void deleteOrder() {
        if (selectedOrderId == -1) return;
        if (JOptionPane.showConfirmDialog(this, "Delete order?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (controller.deleteOrder(selectedOrderId)) {
                refreshTable(); clearForm();
            }
        }
    }

    private void refreshTable() { controller.loadOrders(model); }

    private void clearForm() {
        selectedOrderId = -1;
        txtCustomer.setText(""); txtReqDescription.setText(""); txtItemsPreview.setText("");
        txtTotal.setText(""); cmbStatus.setSelectedIndex(0);
        dateExpected.setDate(null); table.clearSelection();
        loadBatches(-1);
    }
}