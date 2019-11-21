package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.GUIUtil;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import iot.networkentity.UserMote;
import util.GraphStructure;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MoteGUI {
    private JLabel EUIDText;
    private JLabel latitudeLabel;
    private JLabel longitudeLabel;
    private JButton saveButton;
    private JSpinner xPosSpinner;
    private JSpinner yPosSpinner;
    private JSpinner powerSpinner;
    private JSpinner SFSpinner;
    private JLabel TPThresholdText;
    private JPanel mainPanel;
    private JLabel moteNumberLabel;
    private JComboBox<MoteSensor> sensorSpinner;
    private JButton addButton;
    private JList<MoteSensor> sensorList;
    private JSpinner movementSpinner;
    private JCheckBox isActiveCheckBox;
    private JLabel destinationLabel;
    private JComboBox<Long> destinationComboBox;
    private JButton chooseDestinationButton;
    private Mote mote;

    public MoteGUI(Mote mote, JFrame frame, MainGUI mainGUI) {
        this.mote = mote;

        moteNumberLabel.setText(Integer.toString(mote.getEnvironment().getMotes().indexOf(mote) + 1));
        EUIDText.setText(Long.toUnsignedString(mote.getEUI()));
        xPosSpinner.setModel(new SpinnerNumberModel(mote.getXPosInt(), 0, mote.getEnvironment().getMaxXpos(), 1));
        yPosSpinner.setModel(new SpinnerNumberModel(mote.getYPosInt(), 0, mote.getEnvironment().getMaxYpos(), 1));
        powerSpinner.setModel(new SpinnerNumberModel(mote.getTransmissionPower(), -3, 14, 1));
        SFSpinner.setModel(new SpinnerNumberModel(mote.getSF(), 1, 12, 1));
        movementSpinner.setModel(new SpinnerNumberModel(mote.getMovementSpeed(), 0.01, 1000, 0.01));
        TPThresholdText.setText(mote.getTransmissionPowerThreshold().toString());

        updateLatLonFields();

        if (mote instanceof UserMote) {
            isActiveCheckBox.setVisible(true);
            destinationLabel.setVisible(true);
            destinationComboBox.setVisible(true);

            isActiveCheckBox.setSelected(((UserMote) mote).isActive());
            initializeDestinationComboBox();
        } else {
            isActiveCheckBox.setVisible(false);
            destinationLabel.setVisible(false);
            destinationComboBox.setVisible(false);
        }
        sensorSpinner.setModel(new DefaultComboBoxModel<>(MoteSensor.values()));

        DefaultListModel<MoteSensor> sensorListModel = new DefaultListModel<>();

        for (int i = 0; i < mote.getSensors().size(); i++) {
            sensorListModel.addElement(mote.getSensors().get(i));
        }
        sensorList.setModel(sensorListModel);


        addButton.addActionListener(e -> {
            DefaultListModel<MoteSensor> listModel = new DefaultListModel<>();

            for (int i = 0; i < sensorList.getModel().getSize(); i++) {
                listModel.addElement(sensorList.getModel().getElementAt(i));
            }
            listModel.addElement((MoteSensor) sensorSpinner.getSelectedItem());
            sensorList.setModel(listModel);
        });


        saveButton.addActionListener(e -> {
            mote.setSF((int) SFSpinner.getValue());
            mote.setXPos((int) xPosSpinner.getValue());
            mote.setYPos((int) yPosSpinner.getValue());
            mote.setTransmissionPower((int) powerSpinner.getValue());
            List<MoteSensor> moteSensors = new LinkedList<>();
            for (Object sensor : ((DefaultListModel) sensorList.getModel()).toArray()) {
                moteSensors.add((MoteSensor) sensor);
            }
            mote.setSensors(moteSensors);
            mote.setMovementSpeed((double) movementSpinner.getValue());
            if (isActiveCheckBox.isVisible()) {
                assert mote instanceof UserMote;
                ((UserMote) mote).setActive(isActiveCheckBox.isSelected());
                int index = destinationComboBox.getSelectedIndex();
                ((UserMote) mote).setDestination(GraphStructure.getInstance().getWayPoint(destinationComboBox.getItemAt(index)));
            }
            frame.dispose();
        });

        chooseDestinationButton.addActionListener(e -> {
            // Open a new window where the destination waypoint can be selected
            JFrame frameDestination = new JFrame("Choose destination waypoint");
            DestinationGUI destinationGUI = new DestinationGUI(frameDestination, mainGUI, waypointId -> {
                for (int i = 0; i < destinationComboBox.getItemCount(); i++) {
                    if (destinationComboBox.getModel().getElementAt(i).equals(waypointId)) {
                        destinationComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            });
            frameDestination.setContentPane(destinationGUI.getMainPanel());
            frameDestination.setMinimumSize(destinationGUI.getMainPanel().getMinimumSize());
            frameDestination.setPreferredSize(destinationGUI.getMainPanel().getPreferredSize());
            frameDestination.setVisible(true);
        });
    }

    private void initializeDestinationComboBox() {
        destinationComboBox.setEnabled(true);

        UserMote userMote = (UserMote) this.mote;
        long currentDestinationID = GraphStructure.getInstance().getClosestWayPoint(userMote.getDestination());

        List<Long> wayPointIds = new ArrayList<>(GraphStructure.getInstance().getWayPoints().keySet());
        destinationComboBox.setModel(new DefaultComboBoxModel<>(wayPointIds.toArray(Long[]::new)));
        destinationComboBox.setSelectedIndex(wayPointIds.indexOf(currentDestinationID));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    private void updateLatLonFields() {
        updateLonField();
        updateLatField();
    }

    private void updateLatField() {
        GUIUtil.updateLabelCoordinate(longitudeLabel, mote.getEnvironment().toLongitude(mote.getXPosInt()), "E", "W");
    }

    private void updateLonField() {
        GUIUtil.updateLabelCoordinate(latitudeLabel, mote.getEnvironment().toLatitude(mote.getYPosInt()), "N", "S");
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
        mainPanel.setLayout(new GridLayoutManager(14, 6, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(680, 527));
        mainPanel.setPreferredSize(new Dimension(680, 527));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 20), new Dimension(20, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("EUID");
        mainPanel.add(label1, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EUIDText = new JLabel();
        EUIDText.setText("Label");
        mainPanel.add(EUIDText, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("x-coordinate");
        mainPanel.add(label2, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xPosSpinner = new JSpinner();
        mainPanel.add(xPosSpinner, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(10, -1), new Dimension(10, -1), null, 0, false));
        longitudeLabel = new JLabel();
        longitudeLabel.setText("Label");
        mainPanel.add(longitudeLabel, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(10, -1), new Dimension(10, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("y-coordinate");
        mainPanel.add(label3, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yPosSpinner = new JSpinner();
        mainPanel.add(yPosSpinner, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        latitudeLabel = new JLabel();
        latitudeLabel.setText("Label");
        mainPanel.add(latitudeLabel, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        powerSpinner = new JSpinner();
        mainPanel.add(powerSpinner, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Powersetting");
        mainPanel.add(label4, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Spreading factor");
        mainPanel.add(label5, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SFSpinner = new JSpinner();
        mainPanel.add(SFSpinner, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TPThresholdText = new JLabel();
        TPThresholdText.setText("Label");
        mainPanel.add(TPThresholdText, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Transmission power threshold");
        mainPanel.add(label6, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        mainPanel.add(saveButton, new GridConstraints(12, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(13, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 20), new Dimension(-1, 20), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Mote ");
        mainPanel.add(label7, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        moteNumberLabel = new JLabel();
        moteNumberLabel.setText("Label");
        mainPanel.add(moteNumberLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Sensors:");
        mainPanel.add(label8, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        mainPanel.add(addButton, new GridConstraints(10, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sensorSpinner = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        sensorSpinner.setModel(defaultComboBoxModel1);
        mainPanel.add(sensorSpinner, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Movement speed");
        mainPanel.add(label9, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        movementSpinner = new JSpinner();
        mainPanel.add(movementSpinner, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("m/s");
        mainPanel.add(label10, new GridConstraints(9, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        mainPanel.add(scrollPane1, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(-1, 90), 0, false));
        sensorList = new JList();
        scrollPane1.setViewportView(sensorList);
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setEnabled(true);
        isActiveCheckBox.setText("is active");
        mainPanel.add(isActiveCheckBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        destinationComboBox = new JComboBox();
        destinationComboBox.setVisible(true);
        mainPanel.add(destinationComboBox, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        destinationLabel = new JLabel();
        destinationLabel.setText("Destination (waypoint ID)");
        destinationLabel.setVisible(true);
        mainPanel.add(destinationLabel, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseDestinationButton = new JButton();
        chooseDestinationButton.setText("Choose on map");
        mainPanel.add(chooseDestinationButton, new GridConstraints(8, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
