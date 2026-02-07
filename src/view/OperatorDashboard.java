package view;

import model.User;
import javax.swing.*;
import java.awt.*;

public class OperatorDashboard extends JFrame {

    private User user;

    // Modified Constructor: Accepts 'initialTab' index
    public OperatorDashboard(User user, int initialTab) {
        this.user = user;
        initComponents(initialTab);
    }

    private void initComponents(int initialTab) {
        setTitle("Production Panel - " + user.getName());
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 76, 117));
        header.setPreferredSize(new Dimension(1000, 70));

        JLabel title = new JLabel("  🏭 Production Process Logging");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- TABS ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add the 3 Separate Forms
        tabbedPane.addTab(" 🧪 Dyeing ", new DyeingForm());  // Index 0
        tabbedPane.addTab(" 💧 Washing ", new WashingForm()); // Index 1
        tabbedPane.addTab(" 🔥 Drying ", new DryingForm());   // Index 2

        // --- LOGIC: SELECT AND LOCK TABS BASED ON ROLE ---
        // If initialTab is 0 (Dyer), select 0. If 1 (Washer), select 1, etc.
        if (initialTab >= 0 && initialTab < 3) {
            tabbedPane.setSelectedIndex(initialTab);

            // Optional: Disable other tabs so they can't switch
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (i != initialTab) {
                    tabbedPane.setEnabledAt(i, false);
                }
            }
        }

        add(tabbedPane, BorderLayout.CENTER);
    }
}