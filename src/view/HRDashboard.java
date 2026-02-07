package view;

import controller.EmployeeController;
import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class HRDashboard extends JFrame {

    private User user;
    private JPanel contentPanel;

    // --- "Midnight Executive" Palette ---
    private final Color SIDEBAR_TOP = new Color(20, 30, 48);      // Deep Midnight
    private final Color SIDEBAR_BOTTOM = new Color(36, 59, 85);   // Slate Navy
    private final Color TEXT_COLOR = new Color(236, 240, 241);    // Soft Off-White
    private final Color HOVER_COLOR = new Color(255, 255, 255, 30); // Transparent Highlight
    private final Color BG_COLOR = new Color(240, 242, 245);      // Crisp Light Grey
    private final Color CARD_BG = Color.WHITE;

    public HRDashboard(User user) {
        this.user = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("HR Portal - Integrated Production System");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. Sidebar ---
        JPanel sidebar = new GradientPanel();
        sidebar.setPreferredSize(new Dimension(260, 720));
        sidebar.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(40, 20, 30, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        iconLabel.setForeground(TEXT_COLOR);
        headerPanel.add(iconLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(15, 0, 0, 0);
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        headerPanel.add(nameLabel, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(5, 0, 0, 0);
        JLabel roleLabel = new JLabel("HR MANAGER");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(new Color(176, 190, 197));
        roleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(176, 190, 197)));
        headerPanel.add(roleLabel, gbc);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        // Menu
        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 0, 8));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Use the new CLEAN button method
        menuPanel.add(new SidebarButton(" Dashboard Overview", "Home"));
        menuPanel.add(new SidebarButton(" Employee Directory", "Employees"));
        menuPanel.add(new SidebarButton(" Salary Configuration", "Salary"));
        menuPanel.add(new SidebarButton(" Payroll Processing", "Payroll"));
        menuPanel.add(new SidebarButton(" System Reports", "Reports"));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 20, 30, 20));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setForeground(TEXT_COLOR);
        logoutBtn.setBackground(new Color(192, 57, 43));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });

        footerPanel.add(logoutBtn);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        // --- 2. Content Area ---
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);
        loadHomeView();

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    // --- HELPER CLASSES (The Fix) ---

    // 1. Custom Sidebar Button (Prevents text glitching)
    private class SidebarButton extends JButton {
        public SidebarButton(String text, String command) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(TEXT_COLOR);
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false); // Important for custom painting
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addActionListener(e -> handleMenuAction(command));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isRollover()) {
                g.setColor(HOVER_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
        }
    }

    // 2. Gradient Panel
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, SIDEBAR_TOP, 0, getHeight(), SIDEBAR_BOTTOM);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // --- NAVIGATION LOGIC ---
    private void handleMenuAction(String command) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        switch (command) {
            case "Home":
                loadHomeView();
                break;
            case "Employees":
                contentPanel.add(new EmployeeManagementPanel(), BorderLayout.CENTER);
                break;
            case "Salary":
                contentPanel.add(new SalaryStructurePanel(), BorderLayout.CENTER);
                break;
            case "Payroll":
                contentPanel.add(new PayrollPanel(), BorderLayout.CENTER);
                break;
            case "Reports":
                contentPanel.add(new HRReportPanel(), BorderLayout.CENTER);
                break;
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // --- DASHBOARD HOME ---
    private void loadHomeView() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        EmployeeController empController = new EmployeeController();
        Map<String, String> stats = empController.getDashboardStats();

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 30, 0);
        JLabel title = new JLabel("Welcome back, " + user.getName().split(" ")[0]);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));
        contentPanel.add(title, gbc);

        // Cards
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.insets = new Insets(10, 20, 10, 20);

        contentPanel.add(createStatsCard("Total Workforce", stats.get("active"), new Color(46, 204, 113)), gbc);
        gbc.gridx = 1;
        contentPanel.add(createStatsCard("Departments", stats.get("depts"), new Color(241, 196, 15)), gbc);
        gbc.gridx = 2;
        contentPanel.add(createStatsCard("Next Payroll", stats.get("payroll"), new Color(52, 152, 219)), gbc);
    }

    private JPanel createStatsCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setPreferredSize(new Dimension(240, 130));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(Color.GRAY);
        titleLbl.setBorder(new EmptyBorder(20, 25, 5, 0));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 42));
        valLbl.setForeground(accent);
        valLbl.setBorder(new EmptyBorder(0, 25, 20, 0));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent)
        ));

        return card;
    }
}