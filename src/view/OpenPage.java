package view;

import javax.swing.*;
import java.awt.*;

public class OpenPage extends JFrame {

    private JProgressBar progressBar;
    private JLabel loadingLabel;

    public OpenPage() {
        // 1. Window Setup
        setUndecorated(true); // Removes borders for the "Modern Splash" look
        setSize(900, 550);
        setLocationRelativeTo(null); // Centers on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 2. main.Main Layout (Dark Theme)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(33, 37, 41)); // Dark Grey
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(13, 110, 253), 2)); // Blue Border

        // 3. Center Title
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Integrated Production & Resource Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        centerPanel.add(titleLabel, gbc);

        gbc.gridy++;
        JLabel subTitleLabel = new JLabel("Enterprise Version 1.0");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subTitleLabel.setForeground(new Color(173, 181, 189));
        centerPanel.add(subTitleLabel, gbc);

        // 4. Bottom Progress Bar
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));

        loadingLabel = new JLabel("Initializing System...", SwingConstants.LEFT);
        loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(800, 8));
        progressBar.setForeground(new Color(13, 110, 253)); // Blue
        progressBar.setBackground(new Color(52, 58, 64));   // Dark Grey
        progressBar.setBorderPainted(false);

        bottomPanel.add(loadingLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // Logic to update progress and switch to Login
    public void startLoading() {
        try {
            for (int i = 0; i <= 100; i++) {
                Thread.sleep(30); // Control loading speed
                progressBar.setValue(i);

                if (i == 40) loadingLabel.setText("Loading Modules...");
                if (i == 70) loadingLabel.setText("Connecting to Database...");

                if (i == 100) {
                    // IMPORTANT: Dispose this frame and open Login
                    SwingUtilities.invokeLater(() -> {
                        new LoginForm().setVisible(true);
                        dispose();
                    });
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}