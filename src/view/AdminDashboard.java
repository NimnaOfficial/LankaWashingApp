package view;

import controller.AdminController; // ✅ IMPORTED Correct Controller
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboard extends JFrame {

    private User user;
    private JPanel contentPanel; // The center area where forms will load

    // ✅ FIX: Declare the Controller here
    private AdminController controller;

    // Modern Colors
    private final Color SIDEBAR_BG = new Color(33, 37, 41);   // Dark Grey
    private final Color HOVER_COLOR = new Color(13, 110, 253); // Bright Blue
    private final Color BG_COLOR = new Color(248, 249, 250);   // Light Grey Background

    public AdminDashboard(User user) {
        this.user = user;

        // ✅ FIX: Initialize the Controller
        this.controller = new AdminController();

        initComponents();
    }

    private void initComponents() {
        // 1. Window Setup
        setTitle("Integrated Production System - Admin Console");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 2. Sidebar (Left Panel)
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, 650));
        sidebar.setLayout(new BorderLayout());

        // --- Sidebar Header (User Info) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(SIDEBAR_BG);
        headerPanel.setLayout(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel appTitle = new JLabel("SYSTEM ADMIN");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(new Color(13, 110, 253)); // Blue Accent

        JLabel userLabel = new JLabel("User: " + user.getName());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.LIGHT_GRAY);

        headerPanel.add(appTitle);
        headerPanel.add(userLabel);
        sidebar.add(headerPanel, BorderLayout.NORTH);

        // --- Sidebar Menu Buttons ---
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(SIDEBAR_BG);
        menuPanel.setLayout(new GridLayout(10, 1, 0, 5));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addMenuButton(menuPanel, "Dashboard Home", "Home");
        addMenuButton(menuPanel, "User Management", "Users");
        addMenuButton(menuPanel, "Role Management", "Roles");
        addMenuButton(menuPanel, "Backup & Restore", "Backup");
        addMenuButton(menuPanel, "System Reports", "Reports");

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // --- Sidebar Footer (Logout) ---
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(SIDEBAR_BG);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(220, 53, 69)); // Danger Red
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setPreferredSize(new Dimension(200, 40));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to Sign Out?", "Sign Out", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                new LoginForm().setVisible(true);
                this.dispose();
            }
        });

        footerPanel.add(logoutBtn);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        // 3. Content Area (Center Panel)
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);

        // Initial "Home" View
        loadHomeView();

        // 4. Add to main.Main Frame
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    // Helper Method to Create Styled Sidebar Buttons
    private void addMenuButton(JPanel panel, String text, String actionCommand) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(HOVER_COLOR);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SIDEBAR_BG);
                btn.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        btn.addActionListener(e -> handleMenuAction(actionCommand));
        panel.add(btn);
    }

    // Handles Button Clicks
    // In AdminDashboard.java

    // Inside AdminDashboard.java

    private void handleMenuAction(String command) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        switch (command) {
            case "Home":
                contentPanel.setLayout(new GridBagLayout());
                loadHomeView();
                break;
            case "Users":
                contentPanel.add(new UserManagementPanel(), BorderLayout.CENTER);
                break;
            case "Roles":
                contentPanel.add(new RoleManagementPanel(), BorderLayout.CENTER);
                break;

            // ✅ LINKED: Use your existing BackupPanel class
            case "Backup":
                contentPanel.add(new view.BackupPanel(), BorderLayout.CENTER);
                break;

            case "Reports":
                contentPanel.add(new ReportPanel(), BorderLayout.CENTER);
                break;
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadHomeView() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel welcome = new JLabel("Welcome back, " + user.getName());
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcome.setForeground(new Color(50, 50, 50));
        contentPanel.add(welcome, gbc);

        gbc.gridy = 1;
        JLabel sub = new JLabel("Select an option from the sidebar to manage the system.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        contentPanel.add(sub, gbc);
    }
}