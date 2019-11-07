package GUI;


import GUI.util.GUIUtil;
import IotDomain.Environment;
import IotDomain.networkentity.MoteFactory;
import IotDomain.networkentity.MoteSensor;
import IotDomain.networkentity.UserMote;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.MapHelper;
import util.Path;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class NewMoteGUI {
    private JPanel mainPanel;
    private JTextField EUIDtextField;
    private JSpinner xPosSpinner;
    private JSpinner yPosSpinner;
    private JSpinner powerSpinner;
    private JSpinner SFSpinner;
    private JButton saveButton;
    private JButton generateButton;
    private JTextField LatitudeTextField;
    private JTextField LongitudeTextField;
    private JButton addButton;
    private JList sensorList;
    private JComboBox sensorComboBox;
    private JSpinner movementSpinner;
    private JSpinner movementStartOffsetSpinner;
    private JSpinner periodSpinner;
    private JSpinner offsetSendingSpinner;
    private JCheckBox isUserMoteCheckBox;
    private JCheckBox isActiveCheckBox;
    private Environment environment;

    private Random random = new Random();

    // Determines if a new waypoint should be added at the mote location, if no waypoint is found within 50m.
    private final double DISTANCE_THRESHOLD_NEW_WAYPOINT = 0.05;


    public NewMoteGUI(Environment environment, GeoPosition geoPosition, JFrame frame, ConfigureMotePanel parent) {
        this.environment = environment;

        xPosSpinner.setModel(new SpinnerNumberModel(environment.toMapXCoordinate(geoPosition), 0, environment.getMaxXpos(), 1));
        yPosSpinner.setModel(new SpinnerNumberModel(environment.toMapYCoordinate(geoPosition), 0, environment.getMaxYpos(), 1));
        powerSpinner.setModel(new SpinnerNumberModel(14, -3, 14, 1));
        SFSpinner.setModel(new SpinnerNumberModel(12, 1, 12, 1));
        periodSpinner.setModel(new SpinnerNumberModel(30, 1, Integer.MAX_VALUE, 1));
        offsetSendingSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        movementSpinner.setModel(new SpinnerNumberModel(1, 0.000001, 1000.0, 0.000001));
        movementStartOffsetSpinner.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        generateNewEUID();
        updateLatLonFields();

        saveButton.addActionListener((e) -> {
            if (isUserMoteCheckBox.isSelected()) {
                addUserMote();
            } else {
                addMote();
            }
            parent.refresh();
            frame.dispose();
        });


        sensorComboBox.setModel(new DefaultComboBoxModel(MoteSensor.values()));
        sensorList.setModel(new DefaultListModel());

        xPosSpinner.addChangeListener(evt -> updateLonField());
        yPosSpinner.addChangeListener(evt -> updateLatField());
        generateButton.addActionListener(e -> generateNewEUID());

        addButton.addActionListener(e -> {
            DefaultListModel<MoteSensor> sensorListModel = new DefaultListModel<>();

            for (int i = 0; i < sensorList.getModel().getSize(); i++) {
                sensorListModel.addElement((MoteSensor) sensorList.getModel().getElementAt(i));
            }
            sensorListModel.addElement((MoteSensor) sensorComboBox.getSelectedItem());
            sensorList.setModel(sensorListModel);
        });

        isUserMoteCheckBox.addChangeListener(evt -> {
            if (isUserMoteCheckBox.isSelected()) {
                isActiveCheckBox.setEnabled(true);
            } else {
                isActiveCheckBox.setEnabled(false);
                isActiveCheckBox.setSelected(false);
            }
        });
    }

    private void generateNewEUID() {
        EUIDtextField.setText(Long.toUnsignedString(random.nextLong()));
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void addWayPointIfNotPresent(int x, int y) {
        GraphStructure graph = GraphStructure.getInstance();

        GeoPosition pos = MapHelper.getInstance().toGeoPosition(x, y);

        if (graph.getClosestWayPointWithinRange(pos, DISTANCE_THRESHOLD_NEW_WAYPOINT).isEmpty()) {
            graph.addWayPoint(pos);
        }
    }

    private void addMote() {
        List<MoteSensor> moteSensors = new LinkedList<>();
        for (int i = 0; i < sensorList.getModel().getSize(); i++) {
            moteSensors.add((MoteSensor) sensorList.getModel().getElementAt(i));
        }

        int posX = (int) xPosSpinner.getValue();
        int posY = (int) yPosSpinner.getValue();
        addWayPointIfNotPresent(posX, posY);

        environment.addMote(MoteFactory.createMote(Long.parseUnsignedLong(EUIDtextField.getText()), posX, posY,
            environment, (int) powerSpinner.getValue(),
            (int) SFSpinner.getValue(), moteSensors, 20, new Path(),
            (double) movementSpinner.getValue(),
            (int) movementStartOffsetSpinner.getValue(), (int) periodSpinner.getValue(),
            (int) offsetSendingSpinner.getValue()));
    }

    private void addUserMote() {
        List<MoteSensor> moteSensors = new LinkedList<>();
        for (int i = 0; i < sensorList.getModel().getSize(); i++) {
            moteSensors.add((MoteSensor) sensorList.getModel().getElementAt(i));
        }

        int posX = (int) xPosSpinner.getValue();
        int posY = (int) yPosSpinner.getValue();
        addWayPointIfNotPresent(posX, posY);

        UserMote userMote = MoteFactory.createUserMote(Long.parseUnsignedLong(EUIDtextField.getText()), posX,
            posY, environment, (int) powerSpinner.getValue(),
            (int) SFSpinner.getValue(), moteSensors, 20, new Path(),
            (double) movementSpinner.getValue(),
            (int) movementStartOffsetSpinner.getValue(), (int) periodSpinner.getValue(),
            (int) offsetSendingSpinner.getValue(), MapHelper.getInstance().toGeoPosition(posX, posY));
        userMote.setActive(isActiveCheckBox.isSelected());
        environment.addMote(userMote);
    }

    private void updateLatLonFields() {
        updateLonField();
        updateLatField();
    }

    private void updateLatField() {
        GUIUtil.updateTextFieldCoordinates(LongitudeTextField, environment.toLongitude((int) xPosSpinner.getValue()), "E", "W");
    }

    private void updateLonField() {
        GUIUtil.updateTextFieldCoordinates(LatitudeTextField, environment.toLatitude((int) yPosSpinner.getValue()), "N", "S");
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
        mainPanel.setLayout(new GridLayoutManager(15, 6, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 5), new Dimension(-1, 5), new Dimension(-1, 5), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("EUID");
        mainPanel.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EUIDtextField = new JTextField();
        mainPanel.add(EUIDtextField, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        generateButton = new JButton();
        generateButton.setText("Generate");
        mainPanel.add(generateButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("x-coordinate");
        mainPanel.add(label2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xPosSpinner = new JSpinner();
        mainPanel.add(xPosSpinner, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("E");
        mainPanel.add(label3, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        LatitudeTextField = new JTextField();
        LatitudeTextField.setEditable(false);
        mainPanel.add(LatitudeTextField, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        LongitudeTextField = new JTextField();
        LongitudeTextField.setEditable(false);
        mainPanel.add(LongitudeTextField, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("N");
        mainPanel.add(label4, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yPosSpinner = new JSpinner();
        mainPanel.add(yPosSpinner, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("y-coordinate");
        mainPanel.add(label5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Powersetting");
        mainPanel.add(label6, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        powerSpinner = new JSpinner();
        mainPanel.add(powerSpinner, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Spreading factor");
        mainPanel.add(label7, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SFSpinner = new JSpinner();
        mainPanel.add(SFSpinner, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        mainPanel.add(saveButton, new GridConstraints(13, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 5), new Dimension(-1, 5), new Dimension(-1, 5), 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Sensors:");
        mainPanel.add(label8, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        mainPanel.add(addButton, new GridConstraints(11, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sensorList = new JList();
        mainPanel.add(sensorList, new GridConstraints(12, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        sensorComboBox = new JComboBox();
        mainPanel.add(sensorComboBox, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Movement speed");
        mainPanel.add(label9, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        movementSpinner = new JSpinner();
        mainPanel.add(movementSpinner, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("m/s");
        mainPanel.add(label10, new GridConstraints(9, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Offset start movement ");
        mainPanel.add(label11, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        movementStartOffsetSpinner = new JSpinner();
        mainPanel.add(movementStartOffsetSpinner, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Period for sending packet");
        mainPanel.add(label12, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("seconds");
        mainPanel.add(label13, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        periodSpinner = new JSpinner();
        mainPanel.add(periodSpinner, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Offset start sending packet");
        mainPanel.add(label14, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        offsetSendingSpinner = new JSpinner();
        mainPanel.add(offsetSendingSpinner, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("seconds");
        mainPanel.add(label15, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isUserMoteCheckBox = new JCheckBox();
        isUserMoteCheckBox.setText("is user mote");
        mainPanel.add(isUserMoteCheckBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setEnabled(false);
        isActiveCheckBox.setText("is active");
        mainPanel.add(isActiveCheckBox, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
