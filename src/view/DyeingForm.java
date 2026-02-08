package view;

import controller.ProductionProcessController;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DyeingForm extends JPanel {
    private ProductionProcessController controller;
    private JComboBox<String> cmbBatch, cmbChemical;
    private JTextField txtVolume, txtDuration;
    private JTextArea txtInstructions; // ✅ NEW
    private HashMap<String, Integer> batchMap, chemMap;

    public DyeingForm() {
        controller = new ProductionProcessController();
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 248, 255));
        setBorder(BorderFactory.createTitledBorder("🧪 Dyeing Process Log"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Init Components
        cmbBatch = new JComboBox<>();
        cmbChemical = new JComboBox<>();
        txtVolume = new JTextField(10);
        txtDuration = new JTextField(10);

        // Instructions Box
        txtInstructions = new JTextArea(4, 20);
        txtInstructions.setEditable(false);
        txtInstructions.setBackground(new Color(255, 255, 224)); // Light Yellow
        txtInstructions.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtInstructions.setFont(new Font("Monospaced", Font.BOLD, 12));

        JButton btnSave = new JButton("Record Dyeing");
        btnSave.setBackground(new Color(100, 149, 237));
        btnSave.setForeground(Color.WHITE);

        loadData();

        // --- LAYOUT ---
        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Select Batch:"), gbc);
        gbc.gridx=1; add(cmbBatch, gbc);

        // ✅ INSTRUCTIONS ROW
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=2;
        add(new JScrollPane(txtInstructions), gbc);
        gbc.gridwidth=1; // Reset width

        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Chemical / Dye:"), gbc);
        gbc.gridx=1; add(cmbChemical, gbc);

        gbc.gridx=0; gbc.gridy=3; add(new JLabel("Volume Used (Liters):"), gbc);
        gbc.gridx=1; add(txtVolume, gbc);

        gbc.gridx=0; gbc.gridy=4; add(new JLabel("Duration:"), gbc);
        gbc.gridx=1; add(txtDuration, gbc);

        gbc.gridx=1; gbc.gridy=5; add(btnSave, gbc);

        // --- LISTENERS ---
        btnSave.addActionListener(e -> save());

        // ✅ Auto-Load Instructions when Batch is selected
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
        chemMap = controller.getDyeingResources();

        cmbBatch.removeAllItems();
        batchMap.keySet().forEach(cmbBatch::addItem);

        cmbChemical.removeAllItems();
        chemMap.keySet().forEach(cmbChemical::addItem);
    }

    private void save() {
        if(cmbBatch.getSelectedItem() == null || cmbChemical.getSelectedItem() == null) return;
        try {
            int batchId = batchMap.get(cmbBatch.getSelectedItem().toString());
            String chemName = cmbChemical.getSelectedItem().toString();
            int resId = chemMap.get(chemName);
            double vol = Double.parseDouble(txtVolume.getText());

            if(controller.saveDyeProcess(batchId, resId, chemName, vol, txtDuration.getText())) {
                JOptionPane.showMessageDialog(this, "Dyeing Recorded & Cost Deducted!");
                txtVolume.setText("");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Volume!");
        }
    }
}