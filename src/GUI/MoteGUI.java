package GUI;

import IotDomain.Mote;
import IotDomain.MoteSensor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

public class MoteGUI extends JFrame {
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
    private JComboBox sensorSpinner;
    private JButton addButton;
    private JList sensorList;
    private JSpinner samplingSpinner;
    private JSpinner movementSpinner;
    private Mote mote;
    private JFrame frame;

    public MoteGUI(Mote mote, JFrame frame) {
        this.frame = frame;
        this.mote = mote;
        moteNumberLabel.setText(Integer.toString(mote.getEnvironment().getMotes().indexOf(mote) + 1));
        EUIDText.setText(Long.toUnsignedString(mote.getEUI()));
        Double latitude = mote.getEnvironment().toLatitude(mote.getYPos());
        Integer latitudeDegrees = (int) Math.round(Math.floor(latitude));
        Integer latitudeMinutes = (int) Math.round(Math.floor((latitude - latitudeDegrees) * 60));
        Double latitudeSeconds = (double) Math.round(((latitude - latitudeDegrees) * 60 - latitudeMinutes) * 60 * 1000d) / 1000d;
        Double longitude = mote.getEnvironment().toLongitude(mote.getXPos());
        Integer longitudeDegrees = (int) Math.round(Math.floor(longitude));
        Integer longitudeMinutes = (int) Math.round(Math.floor((longitude - longitudeDegrees) * 60));
        Double longitudeSeconds = (double) Math.round(((longitude - longitudeDegrees) * 60 - longitudeMinutes) * 60 * 1000d) / 1000d;
        latitudeLabel.setText(((Math.signum(mote.getEnvironment().toLatitude(mote.getYPos())) == 1) ? "N " : "S ") +
                latitudeDegrees + "° " + latitudeMinutes + "' " + latitudeSeconds + "\" ");
        longitudeLabel.setText(((Math.signum(mote.getEnvironment().toLongitude(mote.getXPos())) == 1) ? "E " : "W ") +
                longitudeDegrees + "° " + longitudeMinutes + "' " + longitudeSeconds + "\" ");
        xPosSpinner.setModel(new SpinnerNumberModel(mote.getXPos(), Integer.valueOf(0), mote.getEnvironment().getMaxXpos(), Integer.valueOf(1)));
        yPosSpinner.setModel(new SpinnerNumberModel(mote.getYPos(), Integer.valueOf(0), mote.getEnvironment().getMaxYpos(), Integer.valueOf(1)));
        powerSpinner.setModel(new SpinnerNumberModel(mote.getTransmissionPower(), Integer.valueOf(-3), Integer.valueOf(14), Integer.valueOf(1)));
        SFSpinner.setModel(new SpinnerNumberModel(mote.getSF(), Integer.valueOf(1), Integer.valueOf(12), Integer.valueOf(1)));
        samplingSpinner.setModel(new SpinnerNumberModel(mote.getSamplingRate(), Integer.valueOf(1), Integer.valueOf(1000), Integer.valueOf(1)));
        movementSpinner.setModel(new SpinnerNumberModel(mote.getMovementSpeed(), Double.valueOf(0.01), Double.valueOf(1000.0), Double.valueOf(0.01)));
        TPThresholdText.setText(mote.getTransmissionPowerThreshold().toString());
        saveButton.addActionListener(saveActionListener);
        sensorSpinner.setModel(new DefaultComboBoxModel(MoteSensor.values()));
        DefaultListModel<MoteSensor> sensorListModel = new DefaultListModel<>();

        for (int i = 0; i < mote.getSensors().size(); i++) {
            sensorListModel.addElement(mote.getSensors().get(i));
        }
        sensorList.setModel(sensorListModel);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel<MoteSensor> sensorListModel = new DefaultListModel<>();

                for (int i = 0; i < sensorList.getModel().getSize(); i++) {
                    sensorListModel.addElement((MoteSensor) sensorList.getModel().getElementAt(i));
                }
                sensorListModel.addElement((MoteSensor) sensorSpinner.getSelectedItem());
                sensorList.setModel(sensorListModel);
            }
        });

    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void refresh() {
        EUIDText.setText(Long.toUnsignedString(mote.getEUI()));
        Double latitude = mote.getEnvironment().toLatitude(mote.getYPos());
        Integer latitudeDegrees = (int) Math.round(Math.floor(latitude));
        Integer latitudeMinutes = (int) Math.round(Math.floor((latitude - latitudeDegrees) * 60));
        Double latitudeSeconds = (double) Math.round(((latitude - latitudeDegrees) * 60 - latitudeMinutes) * 60 * 1000d) / 1000d;
        Double longitude = mote.getEnvironment().toLongitude(mote.getXPos());
        Integer longitudeDegrees = (int) Math.round(Math.floor(longitude));
        Integer longitudeMinutes = (int) Math.round(Math.floor((longitude - longitudeDegrees) * 60));
        Double longitudeSeconds = (double) Math.round(((longitude - longitudeDegrees) * 60 - longitudeMinutes) * 60 * 1000d) / 1000d;
        latitudeLabel.setText(((Math.signum(mote.getEnvironment().toLatitude(mote.getYPos())) == 1) ? "N " : "S ") +
                latitudeDegrees + "° " + latitudeMinutes + "' " + latitudeSeconds + "\" ");
        longitudeLabel.setText(((Math.signum(mote.getEnvironment().toLongitude(mote.getXPos())) == 1) ? "E " : "W ") +
                longitudeDegrees + "° " + longitudeMinutes + "' " + longitudeSeconds + "\" ");
        xPosSpinner.setValue(mote.getXPos());
        yPosSpinner.setValue(mote.getYPos());
        powerSpinner.setValue(mote.getTransmissionPower());
        SFSpinner.setValue(mote.getSF());
        TPThresholdText.setText(mote.getTransmissionPowerThreshold().toString());

    }

    ActionListener saveActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            mote.setSF((Integer) SFSpinner.getValue());
            mote.setXPos((Integer) xPosSpinner.getValue());
            mote.setYPos((Integer) yPosSpinner.getValue());
            mote.setTransmissionPower((Integer) powerSpinner.getValue());
            LinkedList<MoteSensor> moteSensors = new LinkedList<>();
            for (Object moteSensor : ((DefaultListModel) sensorList.getModel()).toArray()) {
                moteSensors.add((MoteSensor) moteSensor);
            }
            mote.setSensors(moteSensors);
            mote.setSamplingRate((Integer) samplingSpinner.getValue());
            mote.setMovementSpeed((Double) movementSpinner.getValue());
            refresh();
            frame.dispose();
        }
    };

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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(14, 6, new Insets(0, 0, 0, 0), -1, -1));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 20), new Dimension(20, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("EUID");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EUIDText = new JLabel();
        EUIDText.setText("Label");
        mainPanel.add(EUIDText, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("x-coordinate");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xPosSpinner = new JSpinner();
        mainPanel.add(xPosSpinner, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(10, -1), new Dimension(10, -1), null, 0, false));
        longitudeLabel = new JLabel();
        longitudeLabel.setText("Label");
        mainPanel.add(longitudeLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(3, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(10, -1), new Dimension(10, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("y-coordinate");
        mainPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yPosSpinner = new JSpinner();
        mainPanel.add(yPosSpinner, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        latitudeLabel = new JLabel();
        latitudeLabel.setText("Label");
        mainPanel.add(latitudeLabel, new com.intellij.uiDesigner.core.GridConstraints(4, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        powerSpinner = new JSpinner();
        mainPanel.add(powerSpinner, new com.intellij.uiDesigner.core.GridConstraints(5, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Powersetting");
        mainPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Spreading factor");
        mainPanel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SFSpinner = new JSpinner();
        mainPanel.add(SFSpinner, new com.intellij.uiDesigner.core.GridConstraints(6, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TPThresholdText = new JLabel();
        TPThresholdText.setText("Label");
        mainPanel.add(TPThresholdText, new com.intellij.uiDesigner.core.GridConstraints(7, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Transmission power threshold");
        mainPanel.add(label6, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        mainPanel.add(saveButton, new com.intellij.uiDesigner.core.GridConstraints(12, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(13, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 20), new Dimension(-1, 20), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Mote ");
        mainPanel.add(label7, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        moteNumberLabel = new JLabel();
        moteNumberLabel.setText("Label");
        mainPanel.add(moteNumberLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Sensors:");
        mainPanel.add(label8, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        mainPanel.add(addButton, new com.intellij.uiDesigner.core.GridConstraints(10, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sensorSpinner = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        sensorSpinner.setModel(defaultComboBoxModel1);
        mainPanel.add(sensorSpinner, new com.intellij.uiDesigner.core.GridConstraints(10, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Sampling rate");
        mainPanel.add(label9, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Movement speed");
        mainPanel.add(label10, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        samplingSpinner = new JSpinner();
        mainPanel.add(samplingSpinner, new com.intellij.uiDesigner.core.GridConstraints(8, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        movementSpinner = new JSpinner();
        mainPanel.add(movementSpinner, new com.intellij.uiDesigner.core.GridConstraints(9, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("m/s");
        mainPanel.add(label11, new com.intellij.uiDesigner.core.GridConstraints(9, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("m");
        mainPanel.add(label12, new com.intellij.uiDesigner.core.GridConstraints(8, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        mainPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(11, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(-1, 90), 0, false));
        sensorList = new JList();
        scrollPane1.setViewportView(sensorList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
