package GUI;


import IotDomain.Environment;
import IotDomain.Mote;
import util.Pair;
import GUI.MapViewer.MoteNumberWaypointPainter;
import GUI.MapViewer.MoteWaypointPainter;
import GUI.MapViewer.BorderPainter;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class ConfigureMotePanel {
    private JPanel mainPanel;
    private JPanel drawPanel;
    private Environment environment;
    private static JXMapViewer mapViewer = new JXMapViewer();
    // Create a TileFactoryInfo for OpenStreetMap
    private static TileFactoryInfo info = new OSMTileFactoryInfo();
    private static DefaultTileFactory tileFactory = new DefaultTileFactory(info);
    private MapMouseAdapter mouseAdapter = new MapMouseAdapter(this);
    private MainGUI parent;

    public ConfigureMotePanel(Environment environment, MainGUI parent) {
        this.parent = parent;
        this.environment = environment;
        loadMap(false);
        for (MouseListener ml : mapViewer.getMouseListeners()) {
            mapViewer.removeMouseListener(ml);
        }
        mapViewer.addMouseListener(mouseAdapter);
        mapViewer.setZoom(6);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);

    }

    private void loadMap(Boolean isRefresh) {
        GeoPosition centerPosition = mapViewer.getCenterPosition();
        Integer zoom = mapViewer.getZoom();
        mapViewer.setTileFactory(tileFactory);
        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(1);
        mapViewer.setZoom(0);
        LinkedList<LinkedList<Pair<Double, Double>>> points = new LinkedList<>();
        for (int i = 0; i <= environment.getMaxXpos(); i += environment.getMaxXpos()) {
            points.add(new LinkedList<>());
            for (int j = 0; j <= environment.getMaxYpos(); j += environment.getMaxYpos()) {
                points.getLast().add(new Pair(environment.toLatitude(j), environment.toLongitude(i)));
            }
        }
        LinkedList<LinkedList<GeoPosition>> verticalLines = new LinkedList<>();
        verticalLines.add(new LinkedList<>());
        verticalLines.add(new LinkedList<>());

        LinkedList<LinkedList<GeoPosition>> horizontalLines = new LinkedList<>();
        horizontalLines.add(new LinkedList<>());
        horizontalLines.add(new LinkedList<>());


        for (int counter1 = 0; counter1 < points.size(); counter1++) {
            for (int counter2 = 0; counter2 < points.get(counter1).size(); counter2++) {
                verticalLines.get(counter1).add(new GeoPosition(points.get(counter1).get(counter2).getLeft(), points.get(counter1).get(counter2).getRight()));
                horizontalLines.get(counter2).add(new GeoPosition(points.get(counter1).get(counter2).getLeft(), points.get(counter1).get(counter2).getRight()));
            }
        }

        int i = 1;
        Map<Waypoint, Integer> motes = new HashMap();
        for (Mote mote : environment.getMotes()) {
            motes.put(new DefaultWaypoint(new GeoPosition(environment.toLatitude(mote.getYPos()), environment.toLongitude(mote.getXPos()))), i);
            i++;
        }

        MoteWaypointPainter<Waypoint> motePainter = new MoteWaypointPainter<>();
        motePainter.setWaypoints(motes.keySet());

        MoteNumberWaypointPainter<Waypoint> moteNumberPainter = new MoteNumberWaypointPainter<>();
        moteNumberPainter.setWaypoints(motes);

        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        painters.add(motePainter);
        painters.add(moteNumberPainter);

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

    public void refresh() {
        loadMap(true);
        parent.refresh();
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(drawPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private class MapMouseAdapter implements MouseListener {
        private ConfigureMotePanel panel;

        MapMouseAdapter(ConfigureMotePanel panel) {
            this.panel = panel;
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                Boolean exists = false;
                for (Mote mote : environment.getMotes()) {
                    Integer xDistance = Math.abs(environment.toMapXCoordinate(geo) - mote.getXPos());
                    Integer yDistance = environment.toMapYCoordinate(geo) - mote.getYPos();
                    if (xDistance < 100 && yDistance > -20 && yDistance < 250) {
                        JFrame frame = new JFrame("Mote settings");
                        MoteGUI moteGUI = new MoteGUI(mote, frame);
                        frame.setContentPane(moteGUI.getMainPanel());
                        frame.setPreferredSize(new Dimension(600, 400));
                        frame.setMinimumSize(new Dimension(600, 400));
                        frame.setVisible(true);
                        exists = true;
                    }
                }

                if (!exists) {
                    JFrame frame = new JFrame("New mote");
                    NewMoteGUI newMoteGUI = new NewMoteGUI(environment, geo, frame, panel);
                    frame.setContentPane(newMoteGUI.getMainPanel());
                    frame.setPreferredSize(new Dimension(600, 400));
                    frame.setMinimumSize(new Dimension(600, 400));
                    frame.setVisible(true);
                }

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
    }
}
