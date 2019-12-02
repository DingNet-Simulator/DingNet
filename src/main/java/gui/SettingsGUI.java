package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import iot.mqtt.MQTTClientFactory;
import util.Constants;
import util.SettingsReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

public class SettingsGUI {

    private JPanel mainPanel;
    private JList<String> customSettingsList;
    private JScrollPane settingsPane;
    private JButton addSettingsButton;
    private JButton deleteSettingsButton;
    private JButton saveButton;
    private JPanel settingsPanel;

    private List<String> customSettingsFilenames;

    public SettingsGUI() {
        // Load the different settings files in the list
        loadCustomSettings();

        customSettingsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = customSettingsList.getSelectedIndex();
                if (index == -1) {
                    return;
                }

                String selected = customSettingsFilenames.get(index);
                String totalPath = Paths.get(Constants.PATH_CUSTOM_SETTINGS, selected).toString();
                SettingsReader.getInstance().loadSettings(totalPath);

                updateSettingsPane();
            }
        });

        addSettingsButton.addActionListener(e -> {
            // Ask the user for a settings name
            String name = JOptionPane.showInputDialog("New settings name: ");

            // Create a default settings file
            try {
                Properties properties = new Properties();
                properties.load(SettingsReader.class.getResourceAsStream(Constants.DEFAULT_SETTINGS_FILE));

                File newFile = new File(Paths.get(Constants.PATH_CUSTOM_SETTINGS, name + ".properties").toString());

                if (!newFile.exists() && newFile.createNewFile()) {
                    properties.store(new FileOutputStream(newFile), "");
                    customSettingsFilenames.add(name + ".properties");
                    updateGUISettingsList();
                    customSettingsList.setSelectedIndex(customSettingsFilenames.size() - 1);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        deleteSettingsButton.addActionListener(e -> {
            int index = customSettingsList.getSelectedIndex();
            if (index == -1) {
                return;
            }
            String selectedFile = customSettingsFilenames.get(index);

            if (JOptionPane.showConfirmDialog(this.getMainPanel(),
                String.format("Are you sure you want to delete the settings profile with name '%s'?", selectedFile.replace(".properties", "")),
                "Confirm deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                File toRemove = new File(Paths.get(Constants.PATH_CUSTOM_SETTINGS, selectedFile).toString());
                if (toRemove.exists() && toRemove.delete()) {
                    loadCustomSettings();
                    clearSaveButtonListeners();
                    settingsPanel.removeAll();
                }
            }

        });

        clearSaveButtonListeners();
    }

    private void loadCustomSettings() {
        customSettingsFilenames = SettingsReader.getCustomSettingsFiles();

        updateGUISettingsList();
    }

    private void updateGUISettingsList() {
        var model = new DefaultListModel<String>();
        customSettingsFilenames.forEach(f -> model.addElement(f.replace(".properties", "")));
        customSettingsList.setModel(model);
    }

    private void updateSettingsPane() {
        clearSaveButtonListeners();

        settingsPanel.removeAll();
        SettingsReader instance = SettingsReader.getInstance();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);

        settingsPanel.add(this.addBooleanSetting("Use antialiasing", instance::useGUIAntialiasing, "gui.UseAntialiasing"), constraints);
        settingsPanel.add(this.addBooleanSetting("Use map caching", instance::useMapCaching, "gui.UseMapCaching"), constraints);
        settingsPanel.add(this.addBooleanSetting("Start fullscreen", instance::shouldStartFullScreen, "gui.StartFullScreen"), constraints);

        settingsPanel.add(this.addIntegerSetting("Thread pool size", instance::getThreadPoolSize, "gui.ThreadPoolSize"), constraints);
        settingsPanel.add(this.addIntegerSetting("Visualization speed", instance::getBaseVisualizationSpeed, "gui.BaseVisualizationSpeed"), constraints);
        settingsPanel.add(this.addIntegerSetting("Pollution grid squares", instance::getPollutionGridSquares, "gui.PollutionGridSquares"), constraints);
        settingsPanel.add(this.addIntegerSetting("Connection line size", instance::getConnectionLineSize, "gui.ConnectionLineSize"), constraints);
        settingsPanel.add(this.addIntegerSetting("Routing path line size", instance::getRoutingPathLineSize, "gui.RoutingPathLineSize"), constraints);
        settingsPanel.add(this.addIntegerSetting("Mote path line size", instance::getMotePathLineSize, "gui.MotePathLineSize"), constraints);

        settingsPanel.add(this.addFloatSetting("Transparency pollution grid", instance::getPollutionGridTransparency, "gui.TransparencyPollutionGrid"), constraints);

        settingsPanel.add(this.addEnumSetting("MQTT client type", MQTTClientFactory.MqttClientType.class, instance::getMQTTClientType, "mqtt.client"), constraints);

        settingsPanel.add(this.addColorSetting("Default waypoint color", instance::getDefaultWaypointColor, "gui.DefaultWaypointColor"), constraints);
        settingsPanel.add(this.addColorSetting("Connection line color", instance::getConnectionLineColor, "gui.ConnectionLineColor"), constraints);
        settingsPanel.add(this.addColorSetting("Routing path line color", instance::getRoutingPathLineColor, "gui.RoutingPathLineColor"), constraints);
        settingsPanel.add(this.addColorSetting("Mote path line color", instance::getMotePathLineColor, "gui.MotePathLineColor"), constraints);

        settingsPanel.add(this.addStringSetting("Image path mote", instance::getMoteImagePath, "gui.imagePath.Mote"), constraints);
        settingsPanel.add(this.addStringSetting("Image path active user mote", instance::getActiveUsermoteImagePath, "gui.imagePath.UsermoteActive"), constraints);
        settingsPanel.add(this.addStringSetting("Image path inactive user mote", instance::getInactiveUsermoteImagePath, "gui.imagePath.UsermoteInactive"), constraints);
        settingsPanel.add(this.addStringSetting("Image path gateway", instance::getGatewayImagePath, "gui.imagePath.Gateway"), constraints);
        settingsPanel.add(this.addStringSetting("Image path circle selected", instance::getSelectedCircleImagePath, "gui.imagePath.CircleSelected"), constraints);
        settingsPanel.add(this.addStringSetting("Image path circle unselected", instance::getUnselectedCircleImagePath, "gui.imagePath.CircleUnselected"), constraints);
        settingsPanel.add(this.addStringSetting("Image path edit icon", instance::getEditIconImagePath, "gui.imagePath.EditIcon"), constraints);
        settingsPanel.add(this.addStringSetting("Tile factory cache path", instance::getTileFactoryCachePath, "gui.path.CacheTileFactory"), constraints);


        settingsPanel.repaint();
        settingsPanel.revalidate();
    }

    // region Setting entries

    private JPanel addIntegerSetting(String name, Supplier<Integer> supplier, String propertyName) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name + ":");
        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel());
        spinner.setValue(supplier.get());
        spinner.setMinimumSize(new Dimension(75, 20));
        spinner.setMaximumSize(new Dimension(75, 20));
        spinner.setPreferredSize(new Dimension(75, 20));
        panel.add(label, BorderLayout.WEST);
        panel.add(spinner, BorderLayout.EAST);

        saveButton.addActionListener(e -> SettingsReader.getInstance()
            .updateProperty(propertyName, Integer.toString((int) spinner.getValue())));

        return panel;
    }

    private JPanel addBooleanSetting(String name, Supplier<Boolean> supplier, String propertyName) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name + ":");
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(supplier.get());
        panel.add(label, BorderLayout.WEST);
        panel.add(checkBox, BorderLayout.EAST);

        saveButton.addActionListener(e -> SettingsReader.getInstance()
            .updateProperty(propertyName, checkBox.isSelected() ? "true" : "false"));

        return panel;
    }

    @SuppressWarnings("SameParameterValue")
    private JPanel addFloatSetting(String name, Supplier<Float> supplier, String propertyName) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name + ":");
        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel((double) supplier.get(), 0.0, 1.0, 0.1));
        spinner.setMinimumSize(new Dimension(75, 20));
        spinner.setMaximumSize(new Dimension(75, 20));
        spinner.setPreferredSize(new Dimension(75, 20));
        panel.add(label, BorderLayout.WEST);
        panel.add(spinner, BorderLayout.EAST);

        saveButton.addActionListener(e -> SettingsReader.getInstance()
            .updateProperty(propertyName, Float.toString(((Double) spinner.getValue()).floatValue())));

        return panel;
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Enum<T>> JPanel addEnumSetting(String name, Class<T> enumClass, Supplier<T> currentValue, String propertyName) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name + ":");

        T selectedValue = currentValue.get();

        JComboBox<String> comboBox = new JComboBox<>();
        var model = new DefaultComboBoxModel<String>();
        List<T> enumValues = new ArrayList<>(EnumSet.allOf(enumClass));
        enumValues.forEach(v -> model.addElement(v.toString().toLowerCase()));

        comboBox.setModel(model);
        comboBox.setSelectedIndex(enumValues.indexOf(selectedValue));

        panel.add(label, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.EAST);

        // noinspection ConstantConditions
        saveButton.addActionListener(e -> SettingsReader.getInstance()
            .updateProperty(propertyName, comboBox.getSelectedItem().toString()));

        return panel;
    }

    private JPanel addColorSetting(String name, Supplier<Color> supplier, String propertyName) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name + ":");
        JTextField textField = new JTextField();
        Color color = supplier.get();
        textField.setText(String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue()));
        textField.setMinimumSize(new Dimension(100, 20));
        textField.setMaximumSize(new Dimension(100, 20));
        textField.setPreferredSize(new Dimension(100, 20));
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.EAST);

        saveButton.addActionListener(e -> SettingsReader.getInstance()
            .updateProperty(propertyName, textField.getText()));

        return panel;
    }

    private JPanel addStringSetting(String name, Supplier<String> supplier, String propertyName) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(name + ":");
        JTextField textField = new JTextField();
        textField.setText(supplier.get());
        textField.setMinimumSize(new Dimension(200, 20));
        textField.setMaximumSize(new Dimension(200, 20));
        textField.setPreferredSize(new Dimension(200, 20));
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.EAST);

        saveButton.addActionListener(e -> SettingsReader.getInstance()
            .updateProperty(propertyName, textField.getText()));

        return panel;
    }

    // endregion


    private void clearSaveButtonListeners() {
        var listeners = saveButton.getActionListeners();

        if (listeners != null) {
            for (var listener : listeners) {
                saveButton.removeActionListener(listener);
            }
        }

        saveButton.addActionListener(e -> {
            int index = customSettingsList.getSelectedIndex();

            if (index == -1) {
                return;
            }
            SettingsReader.getInstance().store(
                new File(Paths.get(Constants.PATH_CUSTOM_SETTINGS,
                    customSettingsFilenames.get(index)
                ).toString())
            );
        });
    }

    public List<String> getSettingsProfiles() {
        return customSettingsFilenames;
    }


    public JPanel getMainPanel() {
        return this.mainPanel;
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(5, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.setMinimumSize(new Dimension(900, 600));
        mainPanel.setPreferredSize(new Dimension(900, 600));
        settingsPane = new JScrollPane();
        mainPanel.add(settingsPane, new GridConstraints(0, 1, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        settingsPane.setViewportView(settingsPanel);
        customSettingsList = new JList();
        mainPanel.add(customSettingsList, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        addSettingsButton = new JButton();
        addSettingsButton.setText("Add");
        mainPanel.add(addSettingsButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteSettingsButton = new JButton();
        deleteSettingsButton.setText("Delete");
        mainPanel.add(deleteSettingsButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        mainPanel.add(saveButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        mainPanel.add(toolBar$Separator1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 5), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
