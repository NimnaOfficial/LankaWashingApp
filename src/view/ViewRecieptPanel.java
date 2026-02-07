package view;

import controller.CheckPaymentController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;

public class ViewRecieptPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private CheckPaymentController controller;

    private JButton btnDownload, btnMarkDone, btnMarkFailed;
    private JLabel lblStatusInfo;

    // Enterprise Theme Palette
    private final Color COLOR_DONE = new Color(46, 204, 113);   // Success Green
    private final Color COLOR_FAILED = new Color(231, 76, 60); // Failure Red
    private final Color COLOR_DRIVE = new Color(52, 152, 219);  // Action Blue

    public ViewRecieptPanel() {
        controller = new CheckPaymentController();
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(242, 245, 248));

        // --- 1. Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Payment Verification Center");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 39, 46));

        JButton refreshBtn = new JButton("Refresh Records");
        styleButton(refreshBtn, Color.WHITE, Color.DARK_GRAY);
        refreshBtn.addActionListener(e -> refreshData());

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- 2. Table Area ---
        initTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 30, 10, 30));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // --- 3. Action Bar (Modern UI UX) ---
        add(createActionBar(), BorderLayout.SOUTH);

        refreshData();
    }

    private void initTable() {
        String[] columns = {"ID", "Order ID", "Amount ($)", "Method", "Status", "Date", "Ref Link"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Hide Transaction Ref/Link Column from View
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);

        // Alignments and Custom Renderers
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        table.getSelectionModel().addListSelectionListener(e -> toggleButtons());
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(15, 30, 30, 30));

        lblStatusInfo = new JLabel("Select a payment record to manage verification");
        lblStatusInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblStatusInfo.setForeground(Color.GRAY);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnGroup.setOpaque(false);

        btnDownload = new JButton("⬇ Download Receipt");
        styleButton(btnDownload, COLOR_DRIVE, Color.WHITE);
        btnDownload.addActionListener(e -> openLink());

        btnMarkFailed = new JButton("✖ Mark Failed");
        styleButton(btnMarkFailed, COLOR_FAILED, Color.WHITE);
        btnMarkFailed.addActionListener(e -> updateStatus("Failed"));

        btnMarkDone = new JButton("✔ Mark Success");
        styleButton(btnMarkDone, COLOR_DONE, Color.WHITE);
        btnMarkDone.addActionListener(e -> updateStatus("Success"));

        btnGroup.add(btnDownload);
        btnGroup.add(btnMarkFailed);
        btnGroup.add(btnMarkDone);

        bar.add(lblStatusInfo, BorderLayout.WEST);
        bar.add(btnGroup, BorderLayout.EAST);

        setButtonsEnabled(false);
        return bar;
    }

    private void refreshData() {
        controller.loadPayments(model);
        setButtonsEnabled(false);
    }

    private void toggleButtons() {
        int row = table.getSelectedRow();
        boolean selected = (row != -1);
        setButtonsEnabled(selected);

        if(selected) {
            lblStatusInfo.setText("Verifying: " + table.getValueAt(row, 1));
            String method = (String) table.getValueAt(row, 3);
            // Disable download if it's a card token instead of a drive link
            btnDownload.setEnabled(method.equalsIgnoreCase("Bank Transfer"));
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnDownload.setEnabled(enabled);
        btnMarkDone.setEnabled(enabled);
        btnMarkFailed.setEnabled(enabled);
    }

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        int id = (int) table.getValueAt(row, 0);
        if(controller.updatePaymentStatus(id, status)) {
            refreshData();
            JOptionPane.showMessageDialog(this, "Payment " + id + " updated to " + status);
        }
    }

    private void openLink() {
        try {
            String url = (String) table.getValueAt(table.getSelectedRow(), 6);
            if(url.startsWith("http")) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "No Drive Link available for this method.");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Link Error"); }
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    // Modern Status Color Highlights
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            String val = String.valueOf(v);
            if("Success".equalsIgnoreCase(val)) l.setForeground(COLOR_DONE);
            else if("Failed".equalsIgnoreCase(val)) l.setForeground(COLOR_FAILED);
            else l.setForeground(new Color(243, 156, 18)); // Pending Orange
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            return l;
        }
    }
}