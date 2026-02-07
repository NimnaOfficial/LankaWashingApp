package view;

import com.toedter.calendar.JDateChooser;
import controller.ReceiptController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class ReceiptPanel extends JPanel {

    // Form Components
    private JComboBox<String> cmbStock;
    private JTextField txtRef, txtCost, txtFilePath;
    private JDateChooser dateChooser;
    private JButton btnUpload, btnSend;

    // Table
    private JTable table;
    private DefaultTableModel model;

    private ReceiptController controller;
    private HashMap<String, String[]> stockDataMap;
    private File selectedFile;

    private final Color ACCENT_COLOR = new Color(255, 140, 0);

    public ReceiptPanel() {
        controller = new ReceiptController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR), " 🧾 Send New Receipt ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), ACCENT_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Stock Combo
        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Select Stock In Transaction:"), gbc);
        cmbStock = new JComboBox<>();
        cmbStock.setPreferredSize(new Dimension(250, 30));
        cmbStock.addActionListener(e -> autoFillCost());
        gbc.gridx=1; panel.add(cmbStock, gbc);

        // Receipt Ref (Auto)
        gbc.gridx=2; panel.add(new JLabel("Receipt Ref (Auto):"), gbc);
        txtRef = new JTextField();
        txtRef.setEditable(false);
        txtRef.setBackground(new Color(240, 240, 240));
        gbc.gridx=3; panel.add(txtRef, gbc);

        // Row 2: Cost (Auto)
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Total Cost ($):"), gbc);
        txtCost = new JTextField();
        txtCost.setEditable(false);
        txtCost.setBackground(new Color(245, 255, 245));
        gbc.gridx=1; panel.add(txtCost, gbc);

        // Date Chooser
        gbc.gridx=2; panel.add(new JLabel("Receipt Date:"), gbc);
        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        gbc.gridx=3; panel.add(dateChooser, gbc);

        // Row 3: File Upload
        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Attach Receipt (PDF/Img):"), gbc);
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setBackground(Color.WHITE);
        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        btnUpload = new JButton("📂 Browse");
        btnUpload.addActionListener(e -> chooseFile());
        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnUpload, BorderLayout.EAST);
        gbc.gridx=1; gbc.gridwidth=2; panel.add(filePanel, gbc);

        // Send Button
        btnSend = new JButton("🚀 Upload & Send");
        btnSend.setBackground(new Color(46, 204, 113));
        btnSend.setForeground(Color.black);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSend.addActionListener(e -> sendReceipt());
        gbc.gridx=3; gbc.gridwidth=1; panel.add(btnSend, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        // Standard Columns
        String[] cols = {"Reference", "Stock Details", "Date", "Amount ($)", "Drive Link"};

        // Standard Model (No Button Logic)
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(table)); }};
    }

    // --- LOGIC ---

    private void loadData() {
        txtRef.setText(controller.generateRef());
        stockDataMap = controller.getStockTransactions(); // Refresh the map from DB

        cmbStock.removeAllItems();
        cmbStock.addItem("-- Select Transaction --");
        if (stockDataMap != null) {
            for(String key : stockDataMap.keySet()) {
                cmbStock.addItem(key);
            }
        }
        controller.loadReceiptHistory(model);
    }

    private void autoFillCost() {
        if(cmbStock.getSelectedIndex() <= 0) {
            txtCost.setText("");
            return;
        }
        String label = (String) cmbStock.getSelectedItem();
        if(stockDataMap != null && stockDataMap.containsKey(label)) {
            String[] data = stockDataMap.get(label);
            if(data != null) {
                txtCost.setText(data[1]);
            }
        }
    }

    private void chooseFile() {
        // Use JFileChooser (Swing) instead of FileDialog (Native)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Receipt File");
                chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images & PDF", "jpg", "png", "pdf", "jpeg"));

                Component parent = SwingUtilities.getWindowAncestor(this);
                int result = chooser.showOpenDialog(parent);

                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = chooser.getSelectedFile();
                    txtFilePath.setText(selectedFile.getName());
                    System.out.println("✅ File selected successfully: " + selectedFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("❌ FileChooser Error: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error opening file browser: " + e.getMessage());
            }
        });
    }

    private void sendReceipt() {
        if(cmbStock.getSelectedIndex() <= 0 || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a transaction and attach a file!");
            return;
        }

        String label = (String) cmbStock.getSelectedItem();
        String[] data = stockDataMap.get(label);
        int transId = Integer.parseInt(data[0]);
        double cost = Double.parseDouble(txtCost.getText());

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnSend.setEnabled(false);
        btnSend.setText("Uploading...");

        new Thread(() -> {
            boolean success = controller.processReceipt(
                    txtRef.getText(),
                    transId,
                    label,
                    dateChooser.getDate(),
                    cost,
                    selectedFile
            );

            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                btnSend.setEnabled(true);
                btnSend.setText("🚀 Upload & Send");

                if(success) {
                    JOptionPane.showMessageDialog(this, "Receipt Sent & Uploaded Successfully!");

                    // Clear inputs
                    txtFilePath.setText("");
                    selectedFile = null;

                    // REFRESH DATA (Reloads Combo Box & Table)
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Error uploading or saving receipt.");
                }
            });
        }).start();
    }
}