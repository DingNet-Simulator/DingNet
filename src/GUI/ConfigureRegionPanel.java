package GUI;

import GUI.MapViewer.BorderPainter;
import GUI.MapViewer.CharacteristicPainter;
import IotDomain.Characteristic;
import IotDomain.Environment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import util.Pair;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ConfigureRegionPanel {
    private JPanel mainPanel;
    private JPanel drawPanel;
    private JPanel legendPanel;
    private Environment environment;
    private static JXMapViewer mapViewer = new JXMapViewer();
    // Create a TileFactoryInfo for OpenStreetMap
    private static TileFactoryInfo info = new OSMTileFactoryInfo();
    private static DefaultTileFactory tileFactory = new DefaultTileFactory(info);
    private Integer amountOfSquares;

    public ConfigureRegionPanel(Environment environment) {
        this.environment = environment;
        amountOfSquares = (int) Math.round(Math.sqrt(environment.getNumberOfZones()));
        loadMap(false);
        loadLegend();
        mapViewer.addMouseListener(new RegionMouseAdapter(this));
        mapViewer.setZoom(6);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
    }

    public void update() {
        drawPanel.removeAll();
        loadMap(true);
        legendPanel.removeAll();
        loadLegend();
        drawPanel.repaint();
        drawPanel.revalidate();
        legendPanel.repaint();
        legendPanel.revalidate();
    }

    private void loadLegend() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        for (Characteristic characteristic : EnumSet.allOf(Characteristic.class)) {
            legendPanel.add(new JLabel(characteristic.name() + ": "), c);
            BufferedImage img = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g.fillRect(0, 0, 15, 15);
            //reset composite
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(characteristic.getColor());
            g.fill(new Ellipse2D.Double(0, 0, 15, 15));
            legendPanel.add(new JLabel(new ImageIcon(img)), c);
        }
    }

    private void loadMap(Boolean isRefresh) {
        GeoPosition centerPosition = mapViewer.getCenterPosition();
        Integer zoom = mapViewer.getZoom();
        mapViewer.removeAll();
        mapViewer.setTileFactory(tileFactory);
        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(1);
        LinkedList<LinkedList<Pair<Double, Double>>> points = new LinkedList<>();
        LinkedList<LinkedList<GeoPosition>> verticalLines = new LinkedList<>();
        LinkedList<LinkedList<GeoPosition>> horizontalLines = new LinkedList<>();
        for (int i = 0; i < (amountOfSquares + 1); i += 1) {
            points.add(new LinkedList<>());
            horizontalLines.add(new LinkedList<>());
            verticalLines.add(new LinkedList<>());
            for (int j = 0; j < (amountOfSquares + 1); j += 1) {
                points.getLast().add(new Pair(environment.toLatitude((int) Math.round(j * ((double) environment.getMaxYpos()) / amountOfSquares)), environment.toLongitude((int) Math.round(i * ((double) environment.getMaxXpos()) / amountOfSquares))));
            }
        }


        Set<GeoPosition> geoPositionSet = new HashSet<>();

        for (int counter1 = 0; counter1 < points.size(); counter1++) {
            for (int counter2 = 0; counter2 < points.get(counter1).size(); counter2++) {
                geoPositionSet.add(new GeoPosition(points.get(counter1).get(counter2).getLeft(), points.get(counter1).get(counter2).getRight()));
                verticalLines.get(counter1).add(new GeoPosition(points.get(counter1).get(counter2).getLeft(), points.get(counter1).get(counter2).getRight()));
                horizontalLines.get(counter2).add(new GeoPosition(points.get(counter1).get(counter2).getLeft(), points.get(counter1).get(counter2).getRight()));
            }
        }

        LinkedList<GeoPosition> centerpoints = new LinkedList<>();

        for (int counter1 = 0; counter1 < points.size() - 1; counter1++) {
            for (int counter2 = 0; counter2 < points.get(counter1).size() - 1; counter2++) {
                centerpoints.add(new GeoPosition(points.get(counter1).get(counter2).getLeft()
                        + (points.get(counter1 + 1).get(counter2 + 1).getLeft() - points.get(counter1).get(counter2).getLeft()) / 5
                        , points.get(counter1 + 1).get(counter2 + 1).getRight() -
                        (points.get(counter1 + 1).get(counter2 + 1).getRight() - points.get(counter1).get(counter2).getRight()) / 5));

            }

        }

        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        for (GeoPosition geoPosition : centerpoints) {
            painters.add(new CharacteristicPainter(geoPosition, environment.getCharacteristic(environment.toMapXCoordinate(geoPosition), environment.toMapYCoordinate(geoPosition))));
        }

        for (LinkedList<GeoPosition> verticalLine : verticalLines) {
            painters.add(new BorderPainter(verticalLine));
        }

        for (LinkedList<GeoPosition> horizontalLine : horizontalLines) {
            painters.add(new BorderPainter(horizontalLine));
        }

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
        if (isRefresh) {
            mapViewer.setAddressLocation(centerPosition);
            mapViewer.setZoom(zoom);
        } else {
            mapViewer.setAddressLocation(environment.getMapCenter());
            mapViewer.setZoom(5);
        }
        drawPanel.add(mapViewer);
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        panel1.add(drawPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(30, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Legend:");
        panel2.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        legendPanel = new JPanel();
        legendPanel.setLayout(new GridBagLayout());
        panel2.add(legendPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        panel2.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


    private class RegionMouseAdapter implements MouseListener {

        private ConfigureRegionPanel configureRegionPanel;

        public RegionMouseAdapter(ConfigureRegionPanel configureRegionPanel) {
            this.configureRegionPanel = configureRegionPanel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                Integer xPos = environment.toMapXCoordinate(geo);
                Integer yPos = environment.toMapYCoordinate(geo);
                int i = 0;
                while (xPos > 1) {
                    xPos -= environment.getMaxXpos() / amountOfSquares;
                    i++;
                }
                int j = 0;
                while (yPos > 1) {
                    yPos -= environment.getMaxYpos() / amountOfSquares;
                    j++;
                }

                JFrame frame = new JFrame("Choose Characteristics");
                CharacteristicGUI characteristicGUI = new CharacteristicGUI(environment, i - 1, j - 1, amountOfSquares, configureRegionPanel, frame);
                frame.setContentPane(characteristicGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(600, 400));
                frame.setMinimumSize(new Dimension(600, 400));
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);


            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }


        public void actionPerformed(ActionEvent e) {

        }
    }
}
