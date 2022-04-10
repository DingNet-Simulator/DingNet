package gui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.overpass.OverpassMapDataApi;
import gui.util.AbstractConfigurePanel;
import gui.util.CompoundPainterBuilder;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.MapHelper;
import util.MyMapDataHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ConfigureWayPointsPanel extends AbstractConfigurePanel {
    private JPanel mainPanel;
    private JPanel drawPanel;
    private JRadioButton addRadioBtn;
    private JPanel configurePanel;
    private JRadioButton deleteRadioBtn;
    private JButton downloadRoadsButton;

    private Mode mode;


    ConfigureWayPointsPanel(MainGUI mainGUI) {
        super(mainGUI, 5);
        mapViewer.addMouseListener(new MapMouseAdapter());

        this.mode = Mode.ADD;

        addRadioBtn.addActionListener(e -> {
            if (deleteRadioBtn.isSelected()) {
                deleteRadioBtn.setSelected(false);
            }

            addRadioBtn.setSelected(true);
            this.mode = Mode.ADD;
        });

        deleteRadioBtn.addActionListener(e -> {
            if (addRadioBtn.isSelected()) {
                addRadioBtn.setSelected(false);
            }
            deleteRadioBtn.setSelected(true);
            this.mode = Mode.DELETE;
        });

        downloadRoadsButton.addActionListener(e->{
            downloadWayPoints();
        });

        loadMap(false);
    }

    protected void loadMap(boolean isRefresh) {
        mapViewer.setOverlayPainter(new CompoundPainterBuilder()
            .withWaypoints(mainGUI.getEnvironment().getGraph(), false)
            .withBorders(environment)
            .build()
        );

        if (!isRefresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
        }

        drawPanel.add(mapViewer);
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    private enum Mode {
        ADD, DELETE
    }

    private class MapMouseAdapter implements MouseListener {

        MapMouseAdapter() {
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                GraphStructure graph = mainGUI.getEnvironment().getGraph();

                if (ConfigureWayPointsPanel.this.mode == Mode.ADD) {
                    graph.addWayPoint(geo);
                } else if (ConfigureWayPointsPanel.this.mode == Mode.DELETE) {
                    // Calculate the distances to the closest wayPoints
                    Map<Long, Double> distances = new HashMap<>();

                    graph.getWayPoints().forEach((k, v) -> distances.put(k, MapHelper.distance(v, geo)));

                    // TODO ask for confirmation from the user? (Visualize deleted routes as well)
                    distances.entrySet().stream()
                        .min(Comparator.comparing(Map.Entry::getValue))
                        .ifPresent(o -> graph.deleteWayPoint(o.getKey(), environment));

                    mainGUI.refresh();
                }
                loadMap(true);
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

    public void downloadWayPoints(){

        Environment environment = mainGUI.getEnvironment();
        // ----------------
        // Alt Connections
        // ----------------
        OsmConnection connection = new OsmConnection("https://overpass-api.de/api/", "dingNet-simulator");
        OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
        MyMapDataHandler handler = new MyMapDataHandler();
        overpass.queryElements(
            "( way[\"highway\"][\"highway\"!= \"footway\"][\"highway\"!= \"pedestrian\"][\"highway\"!= \"path\"][\"highway\"!= \"bicycle\"]("+environment.getMapOrigin().getLatitude()+","+environment.getMapOrigin().getLongitude()+","+ environment.getMapHelper().toLatitude(environment.getMaxXpos())+","+environment.getMapHelper().toLongitude(environment.getMaxYpos())+"); node(w); ); out skel;",
            handler
        );
        environment.updateGraph(new GraphStructure(handler.getWayPoints(),handler.getConnections()));
        loadMap(true);

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
        mainPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(drawPanel, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(454, 337), null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 5), new Dimension(454, 5), null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 15), new Dimension(454, 14), null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(15, -1), new Dimension(14, 337), null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(15, -1), new Dimension(14, 337), null, 0, false));
        configurePanel = new JPanel();
        configurePanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(configurePanel, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), new Dimension(-1, 40), new Dimension(-1, 40), 0, false));
        addRadioBtn = new JRadioButton();
        addRadioBtn.setSelected(true);
        addRadioBtn.setText("Add");
        configurePanel.add(addRadioBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteRadioBtn = new JRadioButton();
        deleteRadioBtn.setText("Delete");
        configurePanel.add(deleteRadioBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

