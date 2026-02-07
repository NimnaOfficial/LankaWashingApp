package view;

import controller.ResourceController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class ResourcePanel extends JPanel {

    // Removed txtPrice from here
    private JTextField txtName, txtMinLevel;
    private JComboBox<String> cmbCategory, cmbUnit, cmbSupplier;

    private JLabel lblQty, lblTotalValue;
    private JTable table;
    private DefaultTableModel model;
    private ResourceController controller;

    private HashMap<String, Integer> supplierMap;
    private int selectedId = -1;
    private double currentPriceInRow = 0.0; // To store price for updates

    // --- PREDEFINED OPTIONS ---
    private final String[] CATEGORIES = {
            "Raw Material", "Chemical", "Fuel", "Packaging", "Consumable",
            "Maintenance", "Spare Parts", "Office Supplies", "Safety Gear", "Electronics"
    };

    private final String[] UNITS = {
            "Kg", "Liters", "Meters", "Pcs", "Rolls",
            "Packs", "Sets", "Bottles", "Cans", "Sheets"
    };

    // Theme Colors
    private final Color ACCENT_COLOR = new Color(255, 140, 0); // Industrial Orange
    private final Color TABLE_HEADER = new Color(33, 37, 41);

    public ResourcePanel() {
        controller = new ResourceController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadSupplierDropdown();
        refreshTable();
    }

    private void loadSupplierDropdown() {
        supplierMap = controller.getSupplierMap();
        cmbSupplier.removeAllItems();
        for (String company : supplierMap.keySet()) {
            cmbSupplier.addItem(company);
        }
        cmbSupplier.setSelectedIndex(-1);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR), " Raw Material Details ",
                        0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -- Row 1 --
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Material Name:"), gbc);
        gbc.gridx = 1; txtName = new JTextField(15); panel.add(txtName, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        cmbCategory = new JComboBox<>(CATEGORIES);
        cmbCategory.setBackground(Color.WHITE);
        panel.add(cmbCategory, gbc);

        // -- Row 2 --
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        cmbUnit = new JComboBox<>(UNITS);
        cmbUnit.setBackground(Color.WHITE);
        panel.add(cmbUnit, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Min Stock Level:"), gbc);
        gbc.gridx = 3; txtMinLevel = new JTextField(15); panel.add(txtMinLevel, gbc);

        // -- Row 3 --
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Primary Supplier:"), gbc);
        gbc.gridx = 1;
        cmbSupplier = new JComboBox<>();
        cmbSupplier.setBackground(Color.WHITE);
        panel.add(cmbSupplier, gbc);

        // Removed Price Input Field from here

        // -- Row 4: READ ONLY STATUS (Qty & Total) --
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Current Stock:"), gbc);

        lblQty = new JLabel("0.0");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQty.setForeground(new Color(0, 128, 0)); // Green
        gbc.gridx = 1; panel.add(lblQty, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Total Stock Value:"), gbc);

        lblTotalValue = new JLabel("Rs 0.00");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalValue.setForeground(new Color(0, 0, 139)); // Dark Blue
        gbc.gridx = 3; panel.add(lblTotalValue, gbc);

        // -- Buttons --
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Add Material", ACCENT_COLOR);
        JButton btnUpdate = createButton("Update", new Color(13, 110, 253));
        JButton btnDelete = createButton("Delete", new Color(220, 53, 69));
        JButton btnClear = createButton("Clear", Color.GRAY);

        btnAdd.addActionListener(e -> addResource());
        btnUpdate.addActionListener(e -> updateResource());
        btnDelete.addActionListener(e -> deleteResource());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Kept columns for Price and Value
        String[] cols = {"ID", "Name", "Category", "Unit", "Qty Available", "Min Level", "Supplier", "Unit Price", "Total Value"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) {
                    selectedId = (int) model.getValueAt(row, 0);
                    txtName.setText(model.getValueAt(row, 1).toString());

                    cmbCategory.setSelectedItem(model.getValueAt(row, 2).toString());
                    cmbUnit.setSelectedItem(model.getValueAt(row, 3).toString());

                    String qty = model.getValueAt(row, 4).toString();
                    lblQty.setText(qty);

                    txtMinLevel.setText(model.getValueAt(row, 5).toString());

                    String supName = model.getValueAt(row, 6).toString();
                    cmbSupplier.setSelectedItem(supName);

                    // Store existing price for update logic
                    currentPriceInRow = Double.parseDouble(model.getValueAt(row, 7).toString());

                    String total = model.getValueAt(row, 8).toString();
                    lblTotalValue.setText("Rs " + total);
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC ---
    private void addResource() {
        if(validateFields()) {
            double min = Double.parseDouble(txtMinLevel.getText());
            int supId = supplierMap.get(cmbSupplier.getSelectedItem().toString());

            String category = cmbCategory.getSelectedItem().toString();
            String unit = cmbUnit.getSelectedItem().toString();

            // Default Price to 0.0 since we removed input
            if(controller.addResource(txtName.getText(), category, unit, min, supId, 0.0)) {
                JOptionPane.showMessageDialog(this, "Material Added!");
                refreshTable();
                clearForm();
            }
        }
    }

    private void updateResource() {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a material to update.");
            return;
        }
        if(validateFields()) {
            double min = Double.parseDouble(txtMinLevel.getText());
            int supId = supplierMap.get(cmbSupplier.getSelectedItem().toString());

            String category = cmbCategory.getSelectedItem().toString();
            String unit = cmbUnit.getSelectedItem().toString();

            // Pass 'currentPriceInRow' so we don't overwrite price with 0
            if(controller.updateResource(selectedId, txtName.getText(), category, unit, min, supId, currentPriceInRow)) {
                JOptionPane.showMessageDialog(this, "Material Updated!");
                refreshTable();
                clearForm();
            }
        }
    }

    private void deleteResource() {
        if(selectedId == -1) return;
        if(JOptionPane.showConfirmDialog(this, "Delete this material?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(controller.deleteResource(selectedId)) {
                refreshTable();
                clearForm();
            }
        }
    }

    private boolean validateFields() {
        // Removed check for txtPrice
        if(txtName.getText().isEmpty() || cmbSupplier.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Name and Supplier are required!");
            return false;
        }
        try {
            Double.parseDouble(txtMinLevel.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Min Level must be a number!");
            return false;
        }
        return true;
    }

    private void refreshTable() { controller.loadResources(model); }

    private void clearForm() {
        txtName.setText("");
        txtMinLevel.setText("0.0");
        lblQty.setText("0.0");
        lblTotalValue.setText("Rs 0.00");

        cmbCategory.setSelectedIndex(0);
        cmbUnit.setSelectedIndex(0);
        cmbSupplier.setSelectedIndex(-1);

        selectedId = -1;
        currentPriceInRow = 0.0;
        table.clearSelection();
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