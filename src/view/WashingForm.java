package view;

import controller.ProductionProcessController;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class WashingForm extends JPanel {
    private ProductionProcessController controller;
    private JComboBox<String> cmbBatch, cmbResource;
    private JTextField txtLevel, txtDuration;
    private JTextArea txtInstructions; // ✅ NEW
    private HashMap<String, Integer> batchMap, resourceMap;

    public WashingForm() {
        controller = new ProductionProcessController();
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 255, 240));
        setBorder(BorderFactory.createTitledBorder("💧 Washing Process Log"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbBatch = new JComboBox<>();
        cmbResource = new JComboBox<>();
        txtLevel = new JTextField(10);
        txtDuration = new JTextField(10);

        txtInstructions = new JTextArea(4, 20);
        txtInstructions.setEditable(false);
        txtInstructions.setBackground(new Color(255, 255, 224));
        txtInstructions.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtInstructions.setFont(new Font("Monospaced", Font.BOLD, 12));

        JButton btnSave = new JButton("Record Washing");
        btnSave.setBackground(new Color(60, 179, 113));
        btnSave.setForeground(Color.WHITE);

        loadData();

        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Select Batch:"), gbc);
        gbc.gridx=1; add(cmbBatch, gbc);

        // ✅ INSTRUCTIONS ROW
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=2;
        add(new JScrollPane(txtInstructions), gbc);
        gbc.gridwidth=1;

        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Cleaning Agent (Soap):"), gbc);
        gbc.gridx=1; add(cmbResource, gbc);

        gbc.gridx=0; gbc.gridy=3; add(new JLabel("Amount Used (Liters):"), gbc);
        gbc.gridx=1; add(txtLevel, gbc);

        gbc.gridx=0; gbc.gridy=4; add(new JLabel("Duration:"), gbc);
        gbc.gridx=1; add(txtDuration, gbc);

        gbc.gridx=1; gbc.gridy=5; add(btnSave, gbc);

        btnSave.addActionListener(e -> save());

        // ✅ Auto-Load Instructions
        cmbBatch.addActionListener(e -> {
            if(cmbBatch.getSelectedItem() != null) {
                String key = cmbBatch.getSelectedItem().toString();
                if(batchMap.containsKey(key)) {
                    int id = batchMap.get(key);
                    txtInstructions.setText(controller.getBatchDetails(id));
                }
            }
        });
    }

    private void loadData() {
        batchMap = controller.getActiveBatches();
        resourceMap = controller.getWashingResources();

        cmbBatch.removeAllItems();
        batchMap.keySet().forEach(cmbBatch::addItem);

        cmbResource.removeAllItems();
        resourceMap.keySet().forEach(cmbResource::addItem);
    }

    private void save() {
        if(cmbBatch.getSelectedItem() == null || cmbResource.getSelectedItem() == null) return;
        int batchId = batchMap.get(cmbBatch.getSelectedItem().toString());
        int resId = resourceMap.get(cmbResource.getSelectedItem().toString());

        if(controller.saveWashProcess(batchId, resId, Double.parseDouble(txtLevel.getText()), txtDuration.getText())) {
            JOptionPane.showMessageDialog(this, "Washing Recorded & Cost Deducted!");
            txtLevel.setText("");
        }
    }
}