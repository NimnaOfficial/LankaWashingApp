package view;

import controller.InvoiceController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;

public class ViewInvoicePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private InvoiceController controller;
    private JButton btnDownload, btnApprove, btnReject;
    private JLabel lblSelected;

    private final Color ACCENT_BLUE = new Color(52, 152, 219);
    private final Color ACCENT_GREEN = new Color(46, 204, 113);
    private final Color ACCENT_RED = new Color(231, 76, 60);

    public ViewInvoicePanel() {
        controller = new InvoiceController();
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Invoice Approval Center");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));

        JButton btnRefresh = new JButton("Refresh Data");
        styleButton(btnRefresh, new Color(240, 240, 240), Color.BLACK);
        btnRefresh.addActionListener(e -> refreshTable());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 30, 0, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        add(createActionBar(), BorderLayout.SOUTH);

        refreshTable();
    }

    private void createTable() {
        // ✅ ADDED "Amount" Column back (Index 4)
        String[] cols = {"ID", "PO Ref", "Invoice #", "Date", "Amount (Rs)", "Status", "Link"};

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        // Header Style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.getTableHeader().setForeground(new Color(100, 100, 100));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // Center Alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Invoice #
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Amount

        // Custom Status Renderer (Index 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        // Hide Link Column (Index 6)
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setWidth(0);

        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
    }

    private JPanel createActionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 30, 10, 30));

        lblSelected = new JLabel("Select an invoice to enable actions");
        lblSelected.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSelected.setForeground(Color.GRAY);

        btnDownload = new JButton("⬇ Download File");
        styleButton(btnDownload, ACCENT_BLUE, Color.WHITE);
        btnDownload.addActionListener(e -> performDownload());

        btnApprove = new JButton("✔ Approve");
        styleButton(btnApprove, ACCENT_GREEN, Color.WHITE);
        btnApprove.addActionListener(e -> updateStatus("Approved"));

        btnReject = new JButton("✖ Reject");
        styleButton(btnReject, ACCENT_RED, Color.WHITE);
        btnReject.addActionListener(e -> updateStatus("Rejected"));

        setButtonsEnabled(false);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(15, 30, 30, 30));
        container.add(lblSelected, BorderLayout.WEST);
        container.add(panel, BorderLayout.EAST);

        panel.add(btnDownload);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnReject);
        panel.add(btnApprove);

        return container;
    }

    private void onRowSelected(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
            setButtonsEnabled(true);
            String ref = (String) table.getValueAt(table.getSelectedRow(), 1);
            lblSelected.setText("Selected Invoice: " + ref);
        } else if (table.getSelectedRow() == -1) {
            setButtonsEnabled(false);
            lblSelected.setText("Select an invoice to enable actions");
        }
    }

    private void performDownload() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        // ✅ FIXED INDEX: Link is at 6
        String url = (String) table.getValueAt(row, 6);
        if (url != null && url.startsWith("http")) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Link Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "No valid file link attached to this invoice.");
        }
    }

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) table.getValueAt(row, 0);
        // ✅ FIXED INDEX: Status is at 5
        String currentStatus = (String) table.getValueAt(row, 5);

        if (currentStatus.equalsIgnoreCase(newStatus)) {
            JOptionPane.showMessageDialog(this, "This invoice is already " + newStatus);
            return;
        }

        boolean success = controller.updateStatus(id, newStatus);
        if (success) {
            refreshTable();
            JOptionPane.showMessageDialog(this, "Status updated to: " + newStatus);
        } else {
            JOptionPane.showMessageDialog(this, "Database Error!");
        }
    }

    private void refreshTable() {
        controller.loadInvoices(model);
        setButtonsEnabled(false);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnDownload.setEnabled(enabled);
        btnApprove.setEnabled(enabled);
        btnReject.setEnabled(enabled);

        if(!enabled) {
            btnDownload.setBackground(Color.LIGHT_GRAY);
            btnApprove.setBackground(Color.LIGHT_GRAY);
            btnReject.setBackground(Color.LIGHT_GRAY);
        } else {
            btnDownload.setBackground(ACCENT_BLUE);
            btnApprove.setBackground(ACCENT_GREEN);
            btnReject.setBackground(ACCENT_RED);
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));

            if ("Approved".equalsIgnoreCase(status)) {
                setForeground(new Color(39, 174, 96));
            } else if ("Rejected".equalsIgnoreCase(status)) {
                setForeground(new Color(192, 57, 43));
            } else {
                setForeground(new Color(243, 156, 18));
            }
            return c;
        }
    }
}