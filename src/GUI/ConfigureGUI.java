package GUI;

import IotDomain.Environment;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigureGUI {
    private JPanel mainPanel;
    private JButton regionButton;
    private JButton moteButton;
    private JButton gatewayButton;
    private JButton closeButton;
    private JPanel configurePanel;
    private JButton mapButton;
    private JButton waypointsButton;
    private Environment environment;
    private MainGUI parent;
    private JFrame frame;

    public ConfigureGUI(Environment environment, MainGUI parent, JFrame frame) {
        this.frame = frame;
        this.parent = parent;

        mapButton.setFocusPainted(false);
        mapButton.setMargin(new Insets(0, 0, 0, 0));
        mapButton.setContentAreaFilled(false);
        mapButton.setBorderPainted(false);
        mapButton.setOpaque(false);
        mapButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        mapButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        regionButton.setFocusPainted(false);
        regionButton.setMargin(new Insets(0, 0, 0, 0));
        regionButton.setContentAreaFilled(false);
        regionButton.setBorderPainted(false);
        regionButton.setOpaque(false);
        regionButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        regionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        moteButton.setFocusPainted(false);
        moteButton.setMargin(new Insets(0, 0, 0, 0));
        moteButton.setContentAreaFilled(false);
        moteButton.setBorderPainted(false);
        moteButton.setOpaque(false);
        moteButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        moteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gatewayButton.setFocusPainted(false);
        gatewayButton.setMargin(new Insets(0, 0, 0, 0));
        gatewayButton.setContentAreaFilled(false);
        gatewayButton.setBorderPainted(false);
        gatewayButton.setOpaque(false);
        gatewayButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        gatewayButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeButton.setFocusPainted(false);
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);
        closeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        waypointsButton.setFocusPainted(false);
        waypointsButton.setMargin(new Insets(0, 0, 0, 0));
        waypointsButton.setContentAreaFilled(false);
        waypointsButton.setBorderPainted(false);
        waypointsButton.setOpaque(false);
        waypointsButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        waypointsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.environment = environment;

        regionButton.addActionListener(e -> {
            ConfigureRegionPanel configureRegionPanel = new ConfigureRegionPanel(environment);
            configurePanel.removeAll();
            configurePanel.add(configureRegionPanel.getMainPanel());
            configurePanel.repaint();
            configurePanel.revalidate();
        });

        moteButton.addActionListener(e -> {
            ConfigureMotePanel configureMotePanel = new ConfigureMotePanel(environment, parent);
            configurePanel.removeAll();
            configurePanel.add(configureMotePanel.getMainPanel());
            configurePanel.repaint();
            configurePanel.revalidate();
        });

        gatewayButton.addActionListener(e -> {
            ConfigureGatewayPanel configureGatewayPanel = new ConfigureGatewayPanel(environment, parent);
            configurePanel.removeAll();
            configurePanel.add(configureGatewayPanel.getMainPanel());
            configurePanel.repaint();
            configurePanel.revalidate();
        });

        closeButton.addActionListener(e -> frame.dispose());

        mapButton.addActionListener(e -> {
            ConfigureMapPanel configureMapPanel = new ConfigureMapPanel(environment, parent);
            configurePanel.removeAll();
            configurePanel.add(configureMapPanel.getMainPanel());
            configurePanel.repaint();
            configurePanel.revalidate();
        });

        waypointsButton.addActionListener(e -> {
            ConfigureWayPointsPanel configureWayPointsPanel = new ConfigureWayPointsPanel(environment, parent);
            configurePanel.removeAll();
            configurePanel.add(configureWayPointsPanel.getMainPanel());
            configurePanel.repaint();
            configurePanel.revalidate();
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
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
        mainPanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(13, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(50, -1), null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel1.add(separator1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel1.add(separator2, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        panel1.add(separator3, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(10, -1), null, null, 0, false));
        moteButton = new JButton();
        moteButton.setText("Mote");
        panel1.add(moteButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        panel1.add(closeButton, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator4 = new JSeparator();
        panel1.add(separator4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        mapButton = new JButton();
        mapButton.setText("Paths");
        panel1.add(mapButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        regionButton = new JButton();
        regionButton.setText("Region");
        panel1.add(regionButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        gatewayButton = new JButton();
        gatewayButton.setText("Gateway");
        panel1.add(gatewayButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        waypointsButton = new JButton();
        waypointsButton.setText("WayPoints");
        panel1.add(waypointsButton, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator5 = new JSeparator();
        panel1.add(separator5, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator6 = new JSeparator();
        panel1.add(separator6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 5), null, 0, false));
        final Spacer spacer5 = new Spacer();
        mainPanel.add(spacer5, new GridConstraints(0, 2, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(5, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new GridConstraints(0, 1, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        configurePanel = new JPanel();
        configurePanel.setLayout(new BorderLayout(0, 0));
        panel2.add(configurePanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel2.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel2.add(spacer7, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 10), null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel2.add(spacer8, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel2.add(spacer9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}

