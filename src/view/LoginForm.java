package view;

import controller.LoginController;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginForm extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private LoginController controller;

    // Modern Colors
    private final Color PRIMARY_COLOR = new Color(13, 110, 253);
    private final Color HOVER_COLOR = new Color(10, 88, 202);
    private final Color TEXT_COLOR = new Color(33, 37, 41);
    private final Color BG_GRADIENT_1 = new Color(33, 37, 41);
    private final Color BG_GRADIENT_2 = new Color(13, 110, 253);

    public LoginForm() {
        controller = new LoginController();
        initComponents();
    }

    private void initComponents() {
        // 1. Window Setup
        setTitle("Integrated Production System - Login");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        // 2. LEFT PANEL (Branding)
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_1, getWidth(), getHeight(), BG_GRADIENT_2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setLayout(new GridBagLayout());

        JLabel brandLabel = new JLabel("<html><div style='text-align: center;'>Integrated Production<br>& Resource Management<br>System</div></html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandLabel.setForeground(Color.WHITE);
        leftPanel.add(brandLabel);

        // 3. RIGHT PANEL (Inputs)
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -- Header --
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        rightPanel.add(welcomeLabel, gbc);

        JLabel subLabel = new JLabel("Login to your account");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        rightPanel.add(subLabel, gbc);

        // -- Email --
        JLabel emailTxt = new JLabel("Email Address");
        emailTxt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 10);
        rightPanel.add(emailTxt, gbc);

        emailField = new JTextField();
        styleTextField(emailField);
        gbc.gridy = 3;
        gbc.ipadx = 250;
        gbc.ipady = 10;
        rightPanel.add(emailField, gbc);

        // -- Password --
        JLabel passTxt = new JLabel("Password");
        passTxt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 4;
        gbc.ipady = 0;
        rightPanel.add(passTxt, gbc);

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        gbc.gridy = 5;
        gbc.ipady = 10;
        rightPanel.add(passwordField, gbc);

        // -- Button --
        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());

        // Hover Effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginButton.setBackground(HOVER_COLOR); }
            public void mouseExited(MouseEvent e) { loginButton.setBackground(PRIMARY_COLOR); }
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(30, 10, 10, 10);
        gbc.ipady = 15;
        rightPanel.add(loginButton, gbc);

        // -- Status Label --
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        rightPanel.add(statusLabel, gbc);

        add(leftPanel);
        add(rightPanel);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setBackground(Color.WHITE);
    }

    // --- MAIN LOGIN LOGIC ---
    private void handleLogin() {
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        User user = controller.authenticateUser(email, pass);

        if (user != null) {
            statusLabel.setText("Success! Redirecting...");
            statusLabel.setForeground(new Color(25, 135, 84)); // Green

            JOptionPane.showMessageDialog(this, "Login Successful!\nWelcome, " + user.getName());

            // --- ROUTING BASED ON YOUR DATABASE TABLE ---
            int role = user.getRoleId();

            if (role == 1) { // Admin
                new AdminDashboard(user).setVisible(true);
            }
            else if (role == 2) { // HR
                new HRDashboard(user).setVisible(true);
            }
            else if (role == 3) { // Production Manager
                new ProductionDashboard(user).setVisible(true);
            }
            else if (role == 4) { // Inventory Manager
                new InventoryDashboard(user).setVisible(true);
            }
            else if (role == 5) { // Maintenance Manager
                new MaintenanceDashboard(user).setVisible(true);
            }
            else if (role == 6) { // Technician
                new TechnicianDashboard(user).setVisible(true);
            }
            else if (role == 7) { // Generic Operator (Defaults to Dyeing Tab 0)
                new OperatorDashboard(user, 0).setVisible(true);
            }
            // --- THE 3 SPECIALIZED OPERATORS ---
            else if (role == 8) { // Dyer -> Opens Tab 0
                new OperatorDashboard(user, 0).setVisible(true);
            }
            else if (role == 9) { // Washer -> Opens Tab 1
                new OperatorDashboard(user, 1).setVisible(true);
            }
            else if (role == 10) { // Dryer -> Opens Tab 2
                new OperatorDashboard(user, 2).setVisible(true);
            }
            else {
                JOptionPane.showMessageDialog(this, "Unknown Role ID: " + role);
                return; // Don't dispose if role is unknown
            }

            this.dispose(); // Close Login Window

        } else {
            statusLabel.setText("Invalid Email or Password.");
            statusLabel.setForeground(Color.RED);
        }
    }
}