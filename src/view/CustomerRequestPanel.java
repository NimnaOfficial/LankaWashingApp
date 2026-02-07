package view;

import com.toedter.calendar.JDateChooser;
import controller.CustomerRequestController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.HashMap;

public class CustomerRequestPanel extends JPanel {

    // Inputs for Manual Entry
    private JComboBox<String> cmbCustomer;
    private JTextField txtProduct, txtQty;
    private JComboBox<String> cmbPriority;
    private JDateChooser dateExpected;

    // Table Components
    private JTable table;
    private DefaultTableModel model;

    private CustomerRequestController controller;
    private int selectedId = -1;

    // Theme Colors
    private final Color HEADER_BG = new Color(15, 76, 117);
    private final Color BG_COLOR = new Color(240, 244, 248);

    public CustomerRequestPanel() {
        controller = new CustomerRequestController();
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Top Form (To simulate incoming requests)
        add(createFormPanel(), BorderLayout.NORTH);

        // 2. Center Table (To view pending requests)
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. Bottom Actions (Approve/Reject)
        add(createActionPanel(), BorderLayout.SOUTH);

        // Load Data
        loadCustomers();
        refreshTable();
    }

    private void loadCustomers() {
        HashMap<String, Integer> map = controller.getCustomerMap();
        cmbCustomer.removeAllItems();
        for (String name : map.keySet()) cmbCustomer.addItem(name);
    }

    // --- FORM PANEL (Manual Entry) ---
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(HEADER_BG), " New Request Entry ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), HEADER_BG));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx=1; cmbCustomer = new JComboBox<>(); panel.add(cmbCustomer, gbc);

        gbc.gridx=2; panel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx=3; txtProduct = new JTextField(15); panel.add(txtProduct, gbc);

        // Row 2
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx=1; txtQty = new JTextField(); panel.add(txtQty, gbc);

        gbc.gridx=2; panel.add(new JLabel("Expected Date:"), gbc);
        gbc.gridx=3; dateExpected = new JDateChooser(); dateExpected.setDate(new Date()); panel.add(dateExpected, gbc);

        // Row 3
        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Priority:"), gbc);
        gbc.gridx=1; cmbPriority = new JComboBox<>(new String[]{"Low", "Medium", "High"}); panel.add(cmbPriority, gbc);

        // Add Button
        JButton btnAdd = new JButton("➕ Log Request");
        btnAdd.setBackground(new Color(0, 123, 255));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addRequest());
        gbc.gridx=3; panel.add(btnAdd, gbc);

        return panel;
    }

    // --- TABLE PANEL ---
    private JPanel createTablePanel() {
        String[] cols = {"Req ID", "Customer", "Product", "Qty", "Expected Date", "Priority", "Status"};

        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row != -1) selectedId = (int) model.getValueAt(row, 0);
            }
        });

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(table)); }};
    }

    // --- ACTION PANEL (Approve / Reject) ---
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setBackground(BG_COLOR);

        JButton btnApprove = new JButton("✅ Approve & Create Order");
        btnApprove.setBackground(new Color(40, 167, 69)); // Green
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnReject = new JButton("❌ Reject Request");
        btnReject.setBackground(new Color(220, 53, 69)); // Red
        btnReject.setForeground(Color.WHITE);
        btnReject.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnApprove.addActionListener(e -> processRequest(true));
        btnReject.addActionListener(e -> processRequest(false));

        panel.add(btnApprove);
        panel.add(btnReject);
        return panel;
    }

    // --- LOGIC METHODS ---

    private void addRequest() {
        try {
            if(txtProduct.getText().isEmpty() || txtQty.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Product and Quantity.");
                return;
            }
            double qty = Double.parseDouble(txtQty.getText());

            if(controller.addRequest((String)cmbCustomer.getSelectedItem(), txtProduct.getText(), qty, dateExpected.getDate(), (String)cmbPriority.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "Request Logged Successfully!");
                refreshTable();
                txtProduct.setText(""); txtQty.setText(""); // Clear fields
            }
        } catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number!");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void processRequest(boolean isApprove) {
        if(selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a request from the table first!");
            return;
        }

        // Validation: Don't process if already done
        int row = table.getSelectedRow();
        String currentStatus = model.getValueAt(row, 6).toString();
        if(!currentStatus.equals("Pending")) {
            JOptionPane.showMessageDialog(this, "This request is already " + currentStatus + "!");
            return;
        }

        if(isApprove) {
            if(controller.approveRequest(selectedId)) {
                JOptionPane.showMessageDialog(this, "Success! Request Approved.\nNew Order has been created.");
            }
        } else {
            if(controller.rejectRequest(selectedId)) {
                JOptionPane.showMessageDialog(this, "Request Rejected.");
            }
        }
        refreshTable();
    }

    private void refreshTable() { controller.loadRequests(model); }
}