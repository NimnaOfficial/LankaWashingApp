package view;

import controller.TechnicianController;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TechnicianDashboard extends JFrame {

    private User user;
    private TechnicianController controller;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;
    private int myTechnicianId = -1; // Stores the linked Technician ID

    // Theme Colors
    private final Color HEADER_BG = new Color(40, 167, 69); // Green
    private final Color BG_COLOR = new Color(240, 244, 248);

    public TechnicianDashboard(User user) {
        this.user = user;
        this.controller = new TechnicianController();

        // 1. Link User Login to Technician Profile
        this.myTechnicianId = controller.getTechnicianIdByName(user.getName());

        if (myTechnicianId == -1) {
            JOptionPane.showMessageDialog(this, "Error: No Technician profile found for user '" + user.getName() + "'.");
            // In a real app, you might close here, but we'll keep it open for safety
        }

        initComponents();
        loadMyTasks();
        updateStatusLabel();
    }

    private void initComponents() {
        setTitle("Technician Portal - " + user.getName());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- A. HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(1000, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("🛠️ My Assigned Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        header.add(title, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- B. STATUS PANEL (Top) ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        lblStatus = new JLabel("Current Status: Checking...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnToggleStatus = new JButton("Toggle Availability");
        btnToggleStatus.setBackground(new Color(255, 193, 7)); // Amber
        btnToggleStatus.setFocusPainted(false);
        btnToggleStatus.addActionListener(e -> toggleMyStatus());

        statusPanel.add(lblStatus);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(btnToggleStatus);

        add(statusPanel, BorderLayout.SOUTH); // Placed at bottom for visibility

        // --- C. TASK TABLE (Center) ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTasks = new JLabel("Pending Repairs:");
        lblTasks.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Columns matching Controller query
        String[] cols = {"Task ID", "Machine", "Fault Description", "Date Assigned", "Task Cost"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(30);
        taskTable.getTableHeader().setBackground(HEADER_BG);
        taskTable.getTableHeader().setForeground(Color.WHITE);
        taskTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(BG_COLOR);

        JButton btnComplete = new JButton("✅ Mark Selected Task as Completed");
        btnComplete.setBackground(new Color(13, 110, 253)); // Blue
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnComplete.setFocusPainted(false);
        btnComplete.addActionListener(e -> completeTask());

        JButton btnRefresh = new JButton("🔄 Refresh List");
        btnRefresh.setBackground(Color.LIGHT_GRAY);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadMyTasks());

        actionPanel.add(btnRefresh);
        actionPanel.add(btnComplete);

        centerPanel.add(lblTasks, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    // --- LOGIC METHODS ---

    private void loadMyTasks() {
        if (myTechnicianId != -1) {
            // Uses the method we added to TechnicianController earlier
            controller.loadTechnicianTasks(myTechnicianId, tableModel);
        }
    }

    private void updateStatusLabel() {
        if (myTechnicianId != -1) {
            String status = controller.getTechnicianStatus(myTechnicianId);
            lblStatus.setText("My Status: " + status);

            if ("Available".equalsIgnoreCase(status)) {
                lblStatus.setForeground(new Color(25, 135, 84)); // Green
            } else {
                lblStatus.setForeground(Color.RED); // Red/Busy
            }
        }
    }

    private void toggleMyStatus() {
        if (myTechnicianId == -1) return;

        String current = controller.getTechnicianStatus(myTechnicianId);
        String newStatus = "Available".equalsIgnoreCase(current) ? "Busy" : "Available";

        controller.updateTechnicianStatus(myTechnicianId, newStatus);
        updateStatusLabel();
    }

    private void completeTask() {
        int row = taskTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task from the table first.");
            return;
        }

        int taskId = (int) tableModel.getValueAt(row, 0); // Column 0 is Task ID

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark Task #" + taskId + " as Completed?",
                "Confirm Completion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.completeTask(taskId);
            JOptionPane.showMessageDialog(this, "Task Marked as Completed!");

            // Refresh table to remove completed task
            loadMyTasks();

            // Optional: Auto-set status to Available since job is done
            int autoSet = JOptionPane.showConfirmDialog(this,
                    "Do you want to set your status to 'Available' now?",
                    "Update Status", JOptionPane.YES_NO_OPTION);

            if(autoSet == JOptionPane.YES_OPTION) {
                controller.updateTechnicianStatus(myTechnicianId, "Available");
                updateStatusLabel();
            }
        }
    }
}