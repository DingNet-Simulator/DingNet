package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.GUIUtil;
import gui.util.Refreshable;
import iot.Environment;
import iot.networkentity.*;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.function.Consumer;

public class NewGatewayGUI {
    private JPanel mainPanel;
    private JTextField EUIDtextField;
    private JSpinner xPosSpinner;
    private JSpinner yPosSpinner;
    private JSpinner powerSpinner;
    private JSpinner SFSpinner;
    private JButton saveButton;
    private JButton generateButton;
    private JTextField LatitudeLabel;
    private JTextField LongitudeLabel;
    private JRadioButton newPositionRadioButton;
    private JRadioButton existingPositionRadioButton;
    private JLabel xPositionLabel;
    private JLabel lonPositionLabel;
    private JLabel yPositionLabel;
    private JLabel latPositionLabel;
    private Environment environment;

    private Random random = new Random();
    private GeoPosition source;
    private MainGUI mainGUI;

    public NewGatewayGUI(Environment environment, JFrame frame, Refreshable parent, MainGUI mainGUI, Gateway gateway) {
        this(environment, gateway.getPos(), frame, parent, mainGUI, gateway);
    }
    public NewGatewayGUI(Environment environment, Pair<Double, Double> pos, JFrame frame, Refreshable parent, MainGUI mainGUI, Gateway gateway) {
        this(environment, environment.getMapHelper().toGeoPosition(pos), frame, parent, mainGUI, gateway);
    }

    public NewGatewayGUI(Environment environment, GeoPosition geoPosition, JFrame frame, Refreshable parent, MainGUI mainGUI) {
        this(environment, geoPosition, frame, parent, mainGUI, null);
    }

    public NewGatewayGUI(Environment environment, GeoPosition geoPosition, JFrame frame, Refreshable parent, MainGUI mainGUI, Gateway gateway) {
        this.environment = environment;
        this.mainGUI = mainGUI;

        boolean isNewGateway = gateway == null;

        updateSourcePosition(geoPosition);
        powerSpinner.setModel(new SpinnerNumberModel(14, -3, 14, 1));
        SFSpinner.setModel(new SpinnerNumberModel(12, 1, 12, 1));
        generateNewEUID();

        saveButton.addActionListener(e -> {
            if (!isNewGateway) {
                updateGateway(gateway);
            } else {
                addGateway();
            }

            parent.refresh();
            frame.dispose();
            environment.addGateway(new Gateway(Long.parseUnsignedLong(EUIDtextField.getText()),
                (int) xPosSpinner.getValue(), (int) yPosSpinner.getValue(),
                (int) powerSpinner.getValue(), (int) SFSpinner.getValue(),
                environment));
            parent.refresh();
            frame.dispose();
        });

        ButtonGroup positionButtonGroup = new ButtonGroup();
        positionButtonGroup.add(newPositionRadioButton);
        positionButtonGroup.add(existingPositionRadioButton);

        // For new motes, add a new waypoint by default
        // Reverse case for existing motes
        newPositionRadioButton.setSelected(isNewGateway);
        existingPositionRadioButton.setSelected(!isNewGateway);

        newPositionRadioButton.addActionListener(e -> spawnMapFrame(this::updateSourcePosition));
        existingPositionRadioButton.addActionListener(e -> spawnMapFrame(this::updateSourcePosition));

        generateButton.addActionListener(e -> generateNewEUID());
    }

    private void generateNewEUID() {
        EUIDtextField.setText(Long.toUnsignedString(random.nextLong()));
    }


    private void updateSourcePosition(GeoPosition pos) {
        if (newPositionRadioButton.isSelected()) {
            this.source = pos;
        } else {
            GraphStructure graph = this.environment.getGraph();
            this.source = graph.getWayPoint(graph.getClosestWayPoint(pos));
        }

        // Update the labels for the x/y positions and lat/lon accordingly
        GUIUtil.updateLabelCoordinateLat(latPositionLabel, pos.getLatitude());
        GUIUtil.updateLabelCoordinateLon(lonPositionLabel, pos.getLongitude());

        xPositionLabel.setText(String.format("x: %d", (int) Math.round(environment.getMapHelper().toMapXCoordinate(pos))));
        yPositionLabel.setText(String.format("y: %d", (int) Math.round(environment.getMapHelper().toMapYCoordinate(pos))));
    }

    private void spawnMapFrame(Consumer<GeoPosition> consumer) {
        JFrame framePositionSelection = new JFrame("Choose a position");
        DestinationGUI destinationGUI = new DestinationGUI(framePositionSelection, mainGUI, consumer);
        framePositionSelection.setContentPane(destinationGUI.getMainPanel());
        framePositionSelection.setMinimumSize(destinationGUI.getMainPanel().getMinimumSize());
        framePositionSelection.setPreferredSize(destinationGUI.getMainPanel().getPreferredSize());
        framePositionSelection.setVisible(true);
    }

    private void addGateway() {
        Pair<Double, Double> position = handleChosenStartingPosition();

        environment.addGateway(new Gateway(Long.parseUnsignedLong(EUIDtextField.getText()), position.getLeft(), position.getRight(),
            (int) powerSpinner.getValue(),
            (int) SFSpinner.getValue(),environment));

    }

    private void updateGateway(Gateway gateway) {
        Pair<Double, Double> position = handleChosenStartingPosition();

        gateway.updateInitialPosition(environment.getMapHelper().toGeoPosition(position));

        gateway.setSF((int) SFSpinner.getValue());
        gateway.setTransmissionPower((int) powerSpinner.getValue());


    }

    private Pair<Double, Double> handleChosenStartingPosition() {
        if (newPositionRadioButton.isSelected()) {
            // Add a new waypoint to the graph
            environment.getGraph().addWayPoint(source);
        }
        return environment.getMapHelper().toMapCoordinate(source);
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
        mainPanel.setLayout(new GridLayoutManager(8, 6, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(600, 400));
        mainPanel.setPreferredSize(new Dimension(600, 400));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        EUIDtextField = new JTextField();
        mainPanel.add(EUIDtextField, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("EUID");
        mainPanel.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateButton = new JButton();
        generateButton.setText("Generate");
        mainPanel.add(generateButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("x-coordinate");
        mainPanel.add(label2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xPosSpinner = new JSpinner();
        mainPanel.add(xPosSpinner, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("E");
        mainPanel.add(label3, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        LatitudeLabel = new JTextField();
        LatitudeLabel.setEditable(false);
        mainPanel.add(LatitudeLabel, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("y-coordinate");
        mainPanel.add(label4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yPosSpinner = new JSpinner();
        mainPanel.add(yPosSpinner, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("N");
        mainPanel.add(label5, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        LongitudeLabel = new JTextField();
        LongitudeLabel.setEditable(false);
        mainPanel.add(LongitudeLabel, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Powersetting");
        mainPanel.add(label6, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Spreading factor");
        mainPanel.add(label7, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        powerSpinner = new JSpinner();
        mainPanel.add(powerSpinner, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SFSpinner = new JSpinner();
        mainPanel.add(SFSpinner, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        mainPanel.add(saveButton, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(7, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
