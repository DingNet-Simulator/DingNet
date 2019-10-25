package GUI;

import GUI.MapViewer.*;
import GUI.util.GUIUtil;
import IotDomain.Environment;
import IotDomain.Mote;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import util.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigureMapPanel {
    private JPanel mainPanel;

    private JPanel drawPanel;
    private JButton saveTrackButton;
    private JLabel errorLabel;
    private JButton cancelButton;
    private JButton guidedButton;
    private JButton freeButton;
    private Environment environment;
    private static JXMapViewer mapViewer = new JXMapViewer();
    // Create a TileFactoryInfo for OpenStreetMap
    private static TileFactoryInfo info = new OSMTileFactoryInfo();
    private static DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    private List<Long> currentWayPoints;
    private GraphStructure graph;

    private Mote currentMote = null;
    private MainGUI parent;

    public ConfigureMapPanel(Environment environment, MainGUI parent) {
        this.parent = parent;
        this.environment = environment;
        graph = GraphStructure.getInstance();
        currentWayPoints = new LinkedList<>();
        saveTrackButton.setEnabled(false);

        loadMap(false);


        mapViewer.addMouseListener(new MapMouseAdapter());
        mapViewer.setZoom(6);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        saveTrackButton.addActionListener(new MapSaveTrackActionLister());
        cancelButton.addActionListener(new MapCancelActionLister());
        guidedButton.addActionListener(e -> {
            loadMap(true);
            errorLabel.setText("<html><br>1. Select mote<br> by clicking on <br>mote symbol<br><br>2. Select first<br>" +
                    "waypoint<br>along the path<br><br>3.<br>Continue<br>selecting<br>waypoints<br>until the end<br>of " +
                    "the path<br><br>4. Save the<br>path</html>");
        });
        freeButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Only guided path configuration is supported for now.", "Notification", JOptionPane.ERROR_MESSAGE);
        });
    }


    private void loadMap(Boolean isRefresh) {
        mapViewer.removeAll();
        mapViewer.setTileFactory(tileFactory);
        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(8);

        List<Painter<JXMapViewer>> painters = new ArrayList<>();


        // Draw the motes
        int i = 1;
        Map<Waypoint, Integer> motes = new HashMap<>();
        for (Mote mote : environment.getMotes()) {
            motes.put(new DefaultWaypoint(new GeoPosition(environment.toLatitude(mote.getYPos()), environment.toLongitude(mote.getXPos()))), i);
            i++;
        }

        MoteWaypointPainter<Waypoint> motePainter = new MoteWaypointPainter<>();
        motePainter.setWaypoints(motes.keySet());

        MoteNumberWaypointPainter<Waypoint> moteNumberPainter = new MoteNumberWaypointPainter<>();
        moteNumberPainter.setWaypoints(motes);


        painters.add(motePainter);
        painters.add(moteNumberPainter);


        // Draw the borders
        painters.addAll(GUIUtil.getBorderPainters(environment.getMaxXpos(), environment.getMaxYpos()));

        // Draw the path of the currently selected mote in red
        if (currentMote == null) {
            for (Mote mote : environment.getMotes()) {
                painters.add(new LinePainter(mote.getPath().getWayPoints(), Color.RED, 1));
            }
        }

        // Draw the waypoints
        Set<DefaultWaypoint> set = new HashSet<>();
        PathWaypointPainter<DefaultWaypoint> waypointPainter = new PathWaypointPainter<>();
        for (GeoPosition waypoint : graph.getWayPoints().values()) {
            set.add(new DefaultWaypoint(waypoint));
        }
        waypointPainter.setWaypoints(set);
        painters.add(waypointPainter);


        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
        if (!isRefresh) {
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
        mainPanel.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(drawPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(5, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(5, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 3, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(110, -1), new Dimension(110, -1), new Dimension(110, -1), 0, false));
        saveTrackButton = new JButton();
        saveTrackButton.setText("Save");
        panel1.add(saveTrackButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel1.add(spacer5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel1.add(cancelButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        guidedButton = new JButton();
        guidedButton.setText("Guided");
        panel1.add(guidedButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        errorLabel = new JLabel();
        errorLabel.setText("<html><br>Select free<br>or guided</html>");
        panel1.add(errorLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        freeButton = new JButton();
        freeButton.setText("Free");
        panel1.add(freeButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


    private class MapMouseAdapter implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {

                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);

                if (currentWayPoints.size() == 0) {
                    // No mote has been selected yet -> find the nearest mote
                    Map<Mote, Double> moteDistances = new HashMap<>();

                    for (Mote mote : environment.getMotes()) {
                        double distance = MapHelper.distance(geo, MapHelper.getInstance().toGeoPosition(mote.getPos()));
                        moteDistances.put(mote, distance);
                    }

                    Map.Entry<Mote, Double> nearest = moteDistances.entrySet().stream()
//                        .min(Double::compare(Map.Entry::getValue))
                        .min((o1, o2) -> Double.compare(o1.getValue(), o2.getValue()))
                        .orElse(null);

                    if (nearest != null) {
                        currentMote = nearest.getKey();
                        loadMap(true);
                        GeoPosition motePosition = MapHelper.getInstance().toGeoPosition(currentMote.getPos());
                        long wayPointID = graph.getClosestWayPoint(motePosition);
                        currentWayPoints.add(wayPointID);
                    }
                } else {
                    Map<Long, Double> distances = new HashMap<>();
                    for (var me : graph.getWayPoints().entrySet()) {
                        double distance = MapHelper.distance(geo, me.getValue());
                        distances.put(me.getKey(), distance);
                    }
                    Map.Entry<Long, Double> nearest = distances.entrySet().stream()
                        .min((o1, o2) -> Double.compare(o1.getValue(), o2.getValue()))
                        .orElse(null);

                    if (nearest != null) {

                        // Make sure there is a connection between the last point and the currently selected point
                        if (graph.connectionExists(currentWayPoints.get(currentWayPoints.size() - 1), nearest.getKey())) {
                            loadMap(true);
                            currentWayPoints.add(nearest.getKey());
                        }
                    }
                }


                if (currentWayPoints.size() == 0) {
                    return;
                }

                CompoundPainter<JXMapViewer> painter = (CompoundPainter<JXMapViewer>) mapViewer.getOverlayPainter();
                painter.addPainter(
                    new LinePainter(currentWayPoints.stream().map(graph::getWayPoint).collect(Collectors.toList()), Color.RED, 1)
                );


                List<Connection> connections = graph.getOutgoingConnections(currentWayPoints.get(currentWayPoints.size() - 1));
                // Filter out the previous connection, in case at least one connection has already been made
                if (currentWayPoints.size() > 1) {
                    saveTrackButton.setEnabled(true);
                    connections = connections.stream()
                        .filter(o -> o.getTo() != currentWayPoints.get(currentWayPoints.size() - 2))
                        .collect(Collectors.toList());
                }

                for (var con : connections) {
                    painter.addPainter(
                        new LinePainter(List.of(graph.getWayPoint(con.getFrom()), graph.getWayPoint(con.getTo())), Color.GREEN, 2)
                    );
                }
                mapViewer.setOverlayPainter(painter);

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

    private class MapSaveTrackActionLister implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Path path = new Path();

            currentWayPoints.forEach(o -> path.addPosition(graph.getWayPoint(o)));

            if (currentWayPoints.size() > 1) {
                currentMote.setPath(path);
                currentMote = null;
                currentWayPoints = new LinkedList<>();
                loadMap(true);

                parent.refresh();
                saveTrackButton.setEnabled(false);
            }
        }
    }

    private class MapCancelActionLister implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            currentWayPoints = new LinkedList<>();
            currentMote = null;
            loadMap(true);
        }
    }
}
