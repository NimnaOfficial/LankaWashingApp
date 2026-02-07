package view;

import controller.StockController;
import com.toedter.calendar.JDateChooser; // ✅ Import JCalendar library

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class StockPanel extends JPanel {

    // Components
    private JComboBox<String> cmbResource;
    private JTextField txtPrice; // Removed txtQty, txtDate
    private JSpinner spnQty;     // ✅ Numeric Up/Down
    private JDateChooser dateChooser; // ✅ Date Picker

    private JLabel lblCurrentStock, lblDate;
    private JRadioButton rbtnOrder, rbtnIn, rbtnOut;

    // Tables
    private JTable tblHistory, tblUsage;
    private DefaultTableModel modelHistory, modelUsage;
    private JTabbedPane tabbedPane;

    private StockController controller;
    private HashMap<String, Integer> resourceMap;

    // Theme
    private final Color ACCENT_COLOR = new Color(255, 140, 0); // Orange
    private final Color HEADER_BG = new Color(50, 50, 50);     // Dark Grey

    public StockPanel() {
        controller = new StockController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Top Section: Transaction Entry Form
        add(createFormPanel(), BorderLayout.NORTH);

        // 2. Center Section: Tabbed Tables
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("📦 Stock History", createHistoryPanel());
        tabbedPane.addTab("⚙️ Operator Usage & Costs", createUsagePanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadResources();
        refreshAllTables();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), " Inventory Management ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Resource Selection
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Select Material:"), gbc);
        cmbResource = new JComboBox<>();
        cmbResource.addActionListener(e -> updateCurrentStockDisplay());
        gbc.gridx = 1; panel.add(cmbResource, gbc);

        lblCurrentStock = new JLabel("Available: 0.0");
        lblCurrentStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCurrentStock.setForeground(new Color(0, 100, 0)); // Dark Green
        gbc.gridx = 2; panel.add(lblCurrentStock, gbc);

        // Row 2: Action Type
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Action:"), gbc);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setBackground(Color.WHITE);

        ButtonGroup grp = new ButtonGroup();
        rbtnOrder = new JRadioButton("Place Order (PO)");
        rbtnIn = new JRadioButton("Receive Stock (Delivery)");
        rbtnOut = new JRadioButton("Issue Stock (Usage)");

        rbtnOrder.setBackground(Color.WHITE);
        rbtnIn.setBackground(Color.WHITE);
        rbtnOut.setBackground(Color.WHITE);
        rbtnOrder.setSelected(true); // Default to Order

        grp.add(rbtnOrder); grp.add(rbtnIn); grp.add(rbtnOut);

        // UI Logic for Radio Buttons
        rbtnOrder.addActionListener(e -> updateFieldVisibility(true, true));
        rbtnIn.addActionListener(e -> updateFieldVisibility(true, false));
        rbtnOut.addActionListener(e -> updateFieldVisibility(false, false));

        radioPanel.add(rbtnOrder);
        radioPanel.add(Box.createHorizontalStrut(10));
        radioPanel.add(rbtnIn);
        radioPanel.add(Box.createHorizontalStrut(10));
        radioPanel.add(rbtnOut);

        gbc.gridx = 1; gbc.gridwidth = 3; panel.add(radioPanel, gbc);

        // Row 3: Inputs (Quantity & Price)
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Quantity:"), gbc);

        // ✅ JSpinner for Quantity (Start: 1.0, Min: 0.1, Max: 100000, Step: 1.0)
        spnQty = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 100000.0, 1.0));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spnQty, "#0.0"); // Format decimal
        spnQty.setEditor(editor);
        gbc.gridx = 1; panel.add(spnQty, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Unit Price (Rs):"), gbc);
        txtPrice = new JTextField(10);
        gbc.gridx = 3; panel.add(txtPrice, gbc);

        // Row 4: Date Input (For POs) using JDateChooser
        gbc.gridx = 0; gbc.gridy = 3;
        lblDate = new JLabel("Expected Date:");
        panel.add(lblDate, gbc);

        // ✅ JDateChooser for Date Picker
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date()); // Default to today
        gbc.gridx = 1; panel.add(dateChooser, gbc);

        // Row 5: Process Button
        JButton btnProcess = new JButton("⚡ Process Action");
        btnProcess.setBackground(ACCENT_COLOR);
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFocusPainted(false);
        btnProcess.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnProcess.addActionListener(e -> processTransaction());
        gbc.gridx = 2; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnProcess, gbc);

        return panel;
    }

    private void updateFieldVisibility(boolean priceEnabled, boolean dateVisible) {
        txtPrice.setEnabled(priceEnabled);
        if(!priceEnabled) txtPrice.setText("0");

        lblDate.setVisible(dateVisible);
        dateChooser.setVisible(dateVisible); // ✅ Hide/Show DateChooser
    }

    // --- TAB 1: HISTORY TABLE ---
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Date", "Material", "Type", "Qty", "Status", "Total Value (Rs)"};
        modelHistory = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblHistory = new JTable(modelHistory);
        tblHistory.setRowHeight(25);
        tblHistory.getTableHeader().setBackground(HEADER_BG);
        tblHistory.getTableHeader().setForeground(Color.WHITE);
        panel.add(new JScrollPane(tblHistory), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 2: OPERATOR USAGE TABLE ---
    private JPanel createUsagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Process", "Batch ID", "Material Used", "Quantity", "Actual Cost (Rs)"};
        modelUsage = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblUsage = new JTable(modelUsage);
        tblUsage.setRowHeight(25);
        tblUsage.getTableHeader().setBackground(new Color(0, 123, 255));
        tblUsage.getTableHeader().setForeground(Color.WHITE);
        panel.add(new JScrollPane(tblUsage), BorderLayout.CENTER);
        return panel;
    }

    private void loadResources() {
        resourceMap = controller.getResourceMap();
        cmbResource.removeAllItems();
        for(String name : resourceMap.keySet()) cmbResource.addItem(name);
    }

    private void updateCurrentStockDisplay() {
        if(cmbResource.getSelectedItem() != null) {
            String name = cmbResource.getSelectedItem().toString();
            int id = resourceMap.get(name);
            lblCurrentStock.setText("Available: " + controller.getCurrentQty(id));
        }
    }

    private void processTransaction() {
        if(cmbResource.getSelectedItem() == null) return;

        try {
            String name = cmbResource.getSelectedItem().toString();
            int id = resourceMap.get(name);

            // ✅ Get Qty from JSpinner
            double qty = (Double) spnQty.getValue();
            double price = 0;

            // 1. PLACE ORDER (PO) Logic
            if (rbtnOrder.isSelected()) {
                if(txtPrice.getText().isEmpty() || dateChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Price and Expected Date are required for Orders.");
                    return;
                }
                price = Double.parseDouble(txtPrice.getText());

                // ✅ Get Date from JDateChooser
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(dateChooser.getDate());

                if(controller.placeOrder(id, qty, price, date)) {
                    JOptionPane.showMessageDialog(this, "Purchase Order Created! Supplier has been notified.");
                    spnQty.setValue(1.0); txtPrice.setText("");
                }
            }

            // 2. RECEIVE (IN) or ISSUE (OUT) Logic
            else {
                if(rbtnIn.isSelected()) {
                    if(txtPrice.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter Unit Price for Stock Delivery.");
                        return;
                    }
                    price = Double.parseDouble(txtPrice.getText());
                }

                if(controller.processTransaction(id, rbtnIn.isSelected()?"IN":"OUT", qty, price)) {
                    JOptionPane.showMessageDialog(this, "Stock Updated Successfully!");
                    refreshAllTables();
                    updateCurrentStockDisplay();
                    spnQty.setValue(1.0); txtPrice.setText("");
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Price Format!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing transaction: " + e.getMessage());
        }
    }

    private void refreshAllTables() {
        controller.loadHistory(modelHistory);
        controller.loadOperatorUsage(modelUsage);
    }
}