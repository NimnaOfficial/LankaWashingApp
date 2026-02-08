package view;

import com.toedter.calendar.JDateChooser;
import controller.ReceiptController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
        gbc.weightx = 1.0;

        // --- Row 1 ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        panel.add(new JLabel("Select Stock In Transaction:"), gbc);

        cmbStock = new JComboBox<>();
        cmbStock.setPreferredSize(new Dimension(250, 30));
        cmbStock.addActionListener(e -> autoFillCost());
        gbc.gridx = 1; gbc.weightx = 0.8;
        panel.add(cmbStock, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        panel.add(new JLabel("Receipt Ref (Auto):"), gbc);

        txtRef = new JTextField();
        txtRef.setEditable(false);
        txtRef.setBackground(new Color(240, 240, 240));
        txtRef.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 3; gbc.weightx = 0.8;
        panel.add(txtRef, gbc);

        // --- Row 2 ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        panel.add(new JLabel("Total Cost ($):"), gbc);

        txtCost = new JTextField();
        txtCost.setEditable(false);
        txtCost.setBackground(new Color(245, 255, 245));
        txtCost.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1; gbc.weightx = 0.8;
        panel.add(txtCost, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        panel.add(new JLabel("Receipt Date:"), gbc);

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        dateChooser.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 3; gbc.weightx = 0.8;
        panel.add(dateChooser, gbc);

        // --- Row 3 ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        panel.add(new JLabel("Attach Receipt (PDF/Img):"), gbc);

        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setBackground(Color.WHITE);

        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        txtFilePath.setPreferredSize(new Dimension(200, 30));

        btnUpload = new JButton("📂 Browse");
        btnUpload.addActionListener(e -> chooseFile());

        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnUpload, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(filePanel, gbc);

        btnSend = new JButton("🚀 Upload & Send");
        btnSend.setBackground(new Color(46, 204, 113));
        btnSend.setForeground(Color.white);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSend.setFocusPainted(false);
        btnSend.addActionListener(e -> sendReceipt());

        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.8;
        panel.add(btnSend, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        String[] cols = {"Reference", "Stock Details", "Date", "Amount ($)", "Drive Link"};

        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(table)); }};
    }

    private void loadData() {
        txtRef.setText(controller.generateRef());
        stockDataMap = controller.getStockTransactions();

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
                try {
                    double val = Double.parseDouble(data[1]);
                    txtCost.setText(String.format("%.2f", val));
                } catch (Exception e) {
                    txtCost.setText(data[1]);
                }
            }
        }
    }

    // 🔥 PRO FIX: Use PowerShell to open the REAL Windows File Picker
    // No Swing, No AWT, No Crashes. Pure Windows Native.
    private void chooseFile() {
        // This command runs a tiny script to open the file dialog and print the path
        String command = "powershell -Command \"Add-Type -AssemblyName System.Windows.Forms; " +
                "$d = New-Object System.Windows.Forms.OpenFileDialog; " +
                "$d.Filter = 'Files|*.jpg;*.jpeg;*.png;*.pdf'; " +
                "$d.Title = 'Select Receipt'; " +
                "if($d.ShowDialog() -eq 'OK'){Write-Host $d.FileName}\"";

        try {
            // Run the command
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the output (the file path)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            if (line != null && !line.trim().isEmpty()) {
                File f = new File(line.trim());
                if(f.exists()) {
                    selectedFile = f;
                    txtFilePath.setText(selectedFile.getName());
                    System.out.println("✅ File selected: " + selectedFile.getAbsolutePath());
                }
            }
            process.waitFor();

        } catch (Exception e) {
            // Fallback just in case powershell is blocked
            JOptionPane.showMessageDialog(this, "Could not open file picker: " + e.getMessage());
        }
    }

    private void sendReceipt() {
        if(cmbStock.getSelectedIndex() <= 0 || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a transaction and attach a file!");
            return;
        }

        String label = (String) cmbStock.getSelectedItem();
        String[] data = stockDataMap.get(label);

        if (data == null) {
            JOptionPane.showMessageDialog(this, "Error: Invalid stock selection.");
            return;
        }

        int transId = Integer.parseInt(data[0]);
        double cost = 0;
        try {
            cost = Double.parseDouble(txtCost.getText());
        } catch (NumberFormatException e) {
            cost = 0;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnSend.setEnabled(false);
        btnSend.setText("Uploading...");

        final double finalCost = cost;

        new Thread(() -> {
            boolean success = controller.processReceipt(
                    txtRef.getText(),
                    transId,
                    label,
                    dateChooser.getDate(),
                    finalCost,
                    selectedFile
            );

            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                btnSend.setEnabled(true);
                btnSend.setText("🚀 Upload & Send");

                if(success) {
                    JOptionPane.showMessageDialog(ReceiptPanel.this, "Receipt Sent & Uploaded Successfully!");
                    txtFilePath.setText("");
                    selectedFile = null;
                    dateChooser.setDate(new Date());
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(ReceiptPanel.this, "Error uploading or saving receipt.");
                }
            });
        }).start();
    }
}