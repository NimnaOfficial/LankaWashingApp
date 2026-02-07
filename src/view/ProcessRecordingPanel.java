package view;

import controller.ProductionProcessController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessRecordingPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbFilter;
    private ProductionProcessController controller;
    private final Color HEADER_BG = new Color(15, 76, 117);

    public ProcessRecordingPanel() {
        controller = new ProductionProcessController();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 244, 248));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("📊 Production Activity & Cost Monitor");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(lblTitle);

        panel.add(Box.createHorizontalStrut(50));
        panel.add(new JLabel("Filter:"));
        cmbFilter = new JComboBox<>(new String[]{"All", "Dyeing", "Washing", "Drying"});
        cmbFilter.addActionListener(e -> refreshTable());
        panel.add(cmbFilter);

        JButton btnRefresh = new JButton("🔄 Refresh Data");
        btnRefresh.addActionListener(e -> refreshTable());
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // ✅ Added COST Column
        String[] columns = {"Batch ID", "Type", "Detail 1", "Quantity Used", "Cost (Rs)", "Duration"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshTable() {
        controller.loadProcessHistory(model, (String) cmbFilter.getSelectedItem());
    }
}