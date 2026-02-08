package view;

import controller.ProductionProcessController;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DryingForm extends JPanel {
    private ProductionProcessController controller;
    private JComboBox<String> cmbBatch, cmbFuel, cmbType;
    private JTextField txtWood, txtDuration;
    private JTextArea txtInstructions; // ✅ NEW
    private HashMap<String, Integer> batchMap, fuelMap;

    public DryingForm() {
        controller = new ProductionProcessController();
        setLayout(new GridBagLayout());
        setBackground(new Color(255, 250, 205));
        setBorder(BorderFactory.createTitledBorder("🔥 Drying Process Log"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbBatch = new JComboBox<>();
        cmbFuel = new JComboBox<>();
        cmbType = new JComboBox<>(new String[]{"Sun Dry", "Steam Dry", "Oven Dry"});
        txtWood = new JTextField(10);
        txtDuration = new JTextField(10);

        txtInstructions = new JTextArea(4, 20);
        txtInstructions.setEditable(false);
        txtInstructions.setBackground(new Color(255, 255, 224));
        txtInstructions.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtInstructions.setFont(new Font("Monospaced", Font.BOLD, 12));

        JButton btnSave = new JButton("Record Drying");
        btnSave.setBackground(new Color(255, 140, 0));
        btnSave.setForeground(Color.WHITE);

        loadData();

        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Select Batch:"), gbc);
        gbc.gridx=1; add(cmbBatch, gbc);

        // ✅ INSTRUCTIONS ROW
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=2;
        add(new JScrollPane(txtInstructions), gbc);
        gbc.gridwidth=1;

        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Fuel Resource:"), gbc);
        gbc.gridx=1; add(cmbFuel, gbc);

        gbc.gridx=0; gbc.gridy=3; add(new JLabel("Usage (Kg):"), gbc);
        gbc.gridx=1; add(txtWood, gbc);

        gbc.gridx=0; gbc.gridy=4; add(new JLabel("Method:"), gbc);
        gbc.gridx=1; add(cmbType, gbc);

        gbc.gridx=0; gbc.gridy=5; add(new JLabel("Duration:"), gbc);
        gbc.gridx=1; add(txtDuration, gbc);

        gbc.gridx=1; gbc.gridy=6; add(btnSave, gbc);

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
        fuelMap = controller.getDryingResources();

        cmbBatch.removeAllItems();
        batchMap.keySet().forEach(cmbBatch::addItem);

        cmbFuel.removeAllItems();
        fuelMap.keySet().forEach(cmbFuel::addItem);
    }

    private void save() {
        if(cmbBatch.getSelectedItem() == null || cmbFuel.getSelectedItem() == null) return;
        int batchId = batchMap.get(cmbBatch.getSelectedItem().toString());
        int resId = fuelMap.get(cmbFuel.getSelectedItem().toString());

        if(controller.saveDryProcess(batchId, resId, Double.parseDouble(txtWood.getText()), cmbType.getSelectedItem().toString(), txtDuration.getText())) {
            JOptionPane.showMessageDialog(this, "Drying Recorded & Cost Deducted!");
            txtWood.setText("");
        }
    }
}