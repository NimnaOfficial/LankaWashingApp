package view;

import controller.DashboardController;
import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InventoryDashboard extends JFrame {

    private User user;
    private JPanel contentPanel;

    // --- "Midnight Executive" Palette ---
    private final Color SIDEBAR_TOP = new Color(20, 30, 48);      // Deep Midnight
    private final Color SIDEBAR_BOTTOM = new Color(36, 59, 85);   // Slate Navy
    private final Color TEXT_COLOR = new Color(236, 240, 241);    // Soft Off-White
    private final Color HOVER_COLOR = new Color(255, 255, 255, 30); // Transparent Highlight

    private final Color BG_COLOR = new Color(240, 242, 245);      // Crisp Light Grey
    private final Color CARD_BG = Color.WHITE;

    // Accent Color (Industrial Orange)
    private final Color ACCENT_COLOR = new Color(255, 140, 0);

    public InventoryDashboard(User user) {
        this.user = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Inventory Control - Integrated Production System");
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
        JLabel iconLabel = new JLabel("📦");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        iconLabel.setForeground(TEXT_COLOR);
        headerPanel.add(iconLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(15, 0, 0, 0);
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        headerPanel.add(nameLabel, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(5, 0, 0, 0);
        JLabel roleLabel = new JLabel("INVENTORY MANAGER");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(new Color(176, 190, 197));
        roleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(176, 190, 197)));
        headerPanel.add(roleLabel, gbc);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        // Menu
        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 0, 8));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Navigation Buttons
        menuPanel.add(new SidebarButton(" Dashboard Overview", "Home"));
        menuPanel.add(new SidebarButton(" Manage Suppliers", "Suppliers"));
        menuPanel.add(new SidebarButton(" Raw Materials", "Materials"));
        menuPanel.add(new SidebarButton(" Stock Transactions", "Stock"));
        menuPanel.add(new SidebarButton(" Inventory Reports", "Reports"));
        menuPanel.add(new SidebarButton(" Send Receipt", "SendReceipt"));

        // ✅ NEW OPTION ADDED HERE
        menuPanel.add(new SidebarButton(" View Invoices", "ViewInvoices"));

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

    // --- HELPER CLASSES ---

    // 1. Custom Sidebar Button
    private class SidebarButton extends JButton {
        public SidebarButton(String text, String command) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(TEXT_COLOR);
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
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
        // Clear previous view
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        // ✅ Switch views based on command
        switch (command) {
            case "Home":
                loadHomeView();
                break;
            case "Suppliers":
                 contentPanel.add(new SupplierPanel(), BorderLayout.CENTER);
                break;
            case "Materials":
                 contentPanel.add(new ResourcePanel(), BorderLayout.CENTER);
                break;
            case "Stock":
                 contentPanel.add(new StockPanel(), BorderLayout.CENTER);
                break;
            case "Reports":
                 contentPanel.add(new InventoryReportPanel(), BorderLayout.CENTER);
                break;
            case "SendReceipt":
                contentPanel.add(new ReceiptPanel(), BorderLayout.CENTER);
                break;
            case "ViewInvoices":
                contentPanel.add(new ViewInvoicePanel(), BorderLayout.CENTER);
                break;
        }

        // ✅ Essential for UI updates
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadHomeView() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Initialize Controller
        DashboardController stats = new DashboardController();

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 30, 0);
        JLabel title = new JLabel("Welcome back, " + user.getName().split(" ")[0]);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));
        contentPanel.add(title, gbc);

        // Stats Cards - Now using REAL DATA from 'stats' controller
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.insets = new Insets(10, 20, 10, 20);

        // Card 1: Low Stock (Real DB Count)
        contentPanel.add(createStatsCard("Low Stock Items", stats.getLowStockCount(), new Color(231, 76, 60)), gbc);

        gbc.gridx = 1;
        // Card 2: Total Suppliers (Real DB Count)
        contentPanel.add(createStatsCard("Total Suppliers", stats.getTotalSuppliers(), ACCENT_COLOR), gbc);

        gbc.gridx = 2;
        // Card 3: Total Materials (Real DB Count)
        contentPanel.add(createStatsCard("Total Materials", stats.getTotalMaterials(), new Color(46, 204, 113)), gbc);
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