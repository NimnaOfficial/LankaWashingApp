package view;

import controller.ProductionDashboardController;
import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ProductionDashboard extends JFrame {

    private User user;
    private JPanel contentPanel;
    private ProductionDashboardController statsController;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    // Theme: "Engineering Blue"
    private final Color SIDEBAR_TOP = new Color(15, 76, 117);
    private final Color SIDEBAR_BOTTOM = new Color(50, 130, 184);
    private final Color TEXT_COLOR = new Color(225, 245, 254);
    private final Color BG_COLOR = new Color(240, 244, 248);
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    private final Color ACTIVE_BTN_COLOR = new Color(255, 255, 255, 30);

    public ProductionDashboard(User user) {
        this.user = user;
        this.statsController = new ProductionDashboardController();
        initComponents();
    }

    private void initComponents() {
        setTitle("Production Management System");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new GradientPanel();
        sidebar.setPreferredSize(new Dimension(260, 720));
        sidebar.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(40, 20, 30, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel iconLabel = new JLabel("🏭");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        iconLabel.setForeground(TEXT_COLOR);
        headerPanel.add(iconLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(15, 0, 0, 0);
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        headerPanel.add(nameLabel, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(5, 0, 0, 0);
        JLabel roleLabel = new JLabel("PRODUCTION MANAGER");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(new Color(187, 222, 251));
        headerPanel.add(roleLabel, gbc);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        // Menu Buttons Container
        JPanel menuContainer = new JPanel(new BorderLayout());
        menuContainer.setOpaque(false);

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create buttons
        addButton(menuPanel, " Dashboard Overview", "Home");
        addButton(menuPanel, " Production Batches", "Batches");
        addButton(menuPanel, " Monitor Process Activity", "Process");
        addButton(menuPanel, " Customer Requests", "Customer");
        addButton(menuPanel, " Orders Mgr", "Orders");

        // ✅ ADDED NEW OPTION: CHECK PAYMENTS
        addButton(menuPanel, " Check Payments", "Payments");

        addButton(menuPanel, " Production Reports", "Reports");

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(menuPanel, BorderLayout.NORTH);

        menuContainer.add(wrapperPanel, BorderLayout.CENTER);
        sidebar.add(menuContainer, BorderLayout.CENTER);

        // Logout Button
        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setBackground(new Color(211, 47, 47));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.addActionListener(e -> { new LoginForm().setVisible(true); dispose(); });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 0, 20, 0));
        footer.add(logoutBtn);
        sidebar.add(footer, BorderLayout.SOUTH);

        // --- CONTENT ---
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);

        handleMenuAction("Home");
        setActiveButton("Home");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void addButton(JPanel panel, String text, String command) {
        SidebarButton btn = new SidebarButton(text, command);
        sidebarButtons.add(btn);
        panel.add(btn);
    }

    private void handleMenuAction(String command) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        switch (command) {
            case "Home": loadHomeView(); break;
            case "Batches":
                contentPanel.add(new ProductionBatchPanel(), BorderLayout.CENTER);
                break;
            case "Process":
                contentPanel.add(new ProcessRecordingPanel(), BorderLayout.CENTER);
                break;
            case "Customer":
                contentPanel.add(new CustomerRequestPanel(), BorderLayout.CENTER);
                break;
            case "Orders":
                contentPanel.add(new OrderManagerPanel(), BorderLayout.CENTER);
                break;
            case "Reports":
                contentPanel.add(new ProductionReportPanel(), BorderLayout.CENTER);
                break;
            // ✅ CASE FOR CHECK PAYMENTS
            case "Payments":
                contentPanel.add(new ViewRecieptPanel(), BorderLayout.CENTER);
                break;
        }

        setActiveButton(command);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void setActiveButton(String command) {
        for (SidebarButton btn : sidebarButtons) {
            if (btn.getActionCommand().equals(command)) {
                btn.setActive(true);
            } else {
                btn.setActive(false);
            }
        }
    }

    private void loadHomeView() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 20, 10, 20);
        contentPanel.add(createStatsCard(
                "Active Batches",
                statsController.getActiveBatchCount(),
                new Color(46, 204, 113)
        ), gbc);

        gbc.gridx = 1;
        contentPanel.add(createStatsCard(
                "Total Processes Logged",
                statsController.getDailyActivityCount(),
                ACCENT_COLOR
        ), gbc);

        gbc.gridx = 2;
        contentPanel.add(createStatsCard(
                "Pending Requests",
                statsController.getPendingRequestCount(),
                new Color(241, 196, 15)
        ), gbc);
    }

    private JPanel createStatsCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(280, 140));
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, color));

        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 36)); v.setForeground(color);

        t.setBorder(new EmptyBorder(15, 20, 0, 0));
        v.setBorder(new EmptyBorder(0, 20, 15, 0));

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(new GradientPaint(0, 0, SIDEBAR_TOP, 0, getHeight(), SIDEBAR_BOTTOM));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class SidebarButton extends JButton {
        private boolean isActive = false;
        private boolean isHover = false;

        public SidebarButton(String text, String command) {
            super(text);
            setActionCommand(command);
            setForeground(TEXT_COLOR);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 20, 10, 0));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addActionListener(e -> handleMenuAction(command));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHover = false; repaint(); }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            if (active) {
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(255, 255, 255, 200)),
                        new EmptyBorder(10, 15, 10, 0)
                ));
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setBorder(new EmptyBorder(10, 20, 10, 0));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isActive || isHover) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) g2d.setColor(ACTIVE_BTN_COLOR);
                else g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
            super.paintComponent(g);
        }
    }
}