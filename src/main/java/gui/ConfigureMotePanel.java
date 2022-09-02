package gui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.AbstractConfigurePanel;
import gui.util.CompoundPainterBuilder;
import iot.networkentity.LifeLongMote;
import iot.networkentity.Mote;
import iot.networkentity.MoteFactory;
import iot.networkentity.MoteSensor;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.Pair;
import util.Path;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigureMotePanel extends AbstractConfigurePanel {
    private JPanel mainPanel;
    private JPanel drawPanel;
    private JButton generateRandomMotesButton;

    private Random random;
    private GraphStructure graph;
    private List<Connection> connections;
    List<Long> currentWayPoints = new LinkedList<>();
    Map<Long, Pair<LinkedList<Long>,GeoPosition>> endWaypoints;

    public ConfigureMotePanel(MainGUI mainGUI) {
        super(mainGUI, 5);
        random = environment.getRandom();
        mapViewer.addMouseListener(new MapMouseAdapter(this));
        loadMap(false);
        graph = mainGUI.getEnvironment().getGraph();
        generateRandomMotesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer amountOfMotes = Integer.parseInt(JOptionPane.showInputDialog("How many random motes do you wish to create?"));
                for(int i = 0; i< amountOfMotes; i++){
                    LifeLongMote llsaCompliantMote = MoteFactory.createLLSACompliantMote(Long.parseUnsignedLong(Long.toUnsignedString(random.nextLong())),
                        new Pair<>(environment.getMaxXpos()*random.nextDouble(),environment.getMaxYpos()*random.nextDouble()),
                        14,
                        12, 20, 1+ random.nextDouble()*2,
                        0,  120 +random.nextInt(500),
                        random.nextInt(20), Arrays.asList(MoteSensor.PARTICULATE_MATTER,MoteSensor.CARBON_DIOXIDE,MoteSensor.OZONE),
                        new Path(new LinkedList<>()), 120+ random.nextInt(600),
                        60 + random.nextInt(200),
                        environment);
                    environment.addMote(llsaCompliantMote);
                    GeoPosition motePosition = llsaCompliantMote.getPos();
                    long wayPointID = graph.getClosestWayPoint(motePosition);
                    currentWayPoints.add(wayPointID);
                    endWaypoints = new HashMap<>();
                    graph.getOutgoingConnections(currentWayPoints.get(currentWayPoints.size()-1)).forEach(conn -> endWaypoints.put(conn.getTo(),new Pair<>(new LinkedList<>(Collections.singleton(conn.getTo())),graph.getWayPoint(conn.getTo()))));

                        int choices = 150+random.nextInt(150);
                        for(int j = 0; j < choices; j++){
                            List<Pair<LinkedList<Long>,GeoPosition>> options = new ArrayList<>(endWaypoints.values());
                            Collections.shuffle(options);
                            LinkedList<Long> choice = options.get(0).getLeft();
                            int k = 0;
                            while(currentWayPoints.contains(choice.getLast()) && k < options.size()) {
                                Collections.shuffle(options);
                                choice = options.get(0).getLeft();
                                k++;
                            }

                            currentWayPoints.addAll(choice.subList(1,choice.size()));
                            computePossiblePaths();

                        }
                    Path path = new Path(new LinkedList<>());

                    currentWayPoints.forEach(o -> path.addPosition(graph.getWayPoint(o)));
                    llsaCompliantMote.setPath(path);
                }
            }
        });
    }

    private void computePossiblePaths(){
        connections = graph.getOutgoingConnections(currentWayPoints.get(currentWayPoints.size() - 1));

        Long currentWaypoint = currentWayPoints.get(currentWayPoints.size()-1);
        Long initialWaypoint = currentWaypoint;
        LinkedList<Long> previousWaypoints = new LinkedList<>();
        int connectionIndex = 0;
        List<Connection> originalConnections = new ArrayList<>(connections);
        Connection connection = originalConnections.get(connectionIndex);
        endWaypoints = new HashMap<>();

        while(connectionIndex < originalConnections.size()){
            previousWaypoints.add(currentWaypoint);

            currentWaypoint = connection.getTo();

            if (graph.getOutgoingConnections(currentWaypoint).size() == 2) {
                if(graph.getOutgoingConnections(currentWaypoint).get(0).getTo() != previousWaypoints.getLast()){
                    connections.add(graph.getOutgoingConnections(currentWaypoint).get(0));
                    connection = graph.getOutgoingConnections(currentWaypoint).get(0);
                } else {
                    connections.add(graph.getOutgoingConnections(currentWaypoint).get(1));
                    connection = graph.getOutgoingConnections(currentWaypoint).get(1);
                }
            }else{
                previousWaypoints.add(currentWaypoint);
                endWaypoints.put(originalConnections.get(connectionIndex).getTo(), new Pair<>(previousWaypoints,graph.getWayPoint(currentWaypoint)));
                connectionIndex ++;
                if(connectionIndex < originalConnections.size()){
                    connection = originalConnections.get(connectionIndex);
                }
                currentWaypoint = initialWaypoint;
                previousWaypoints = new LinkedList<>();
            }
        }

        // Filter out the previous connection, in case at least one connection has already been made
        if (currentWayPoints.size() > 1) {
            connections = connections.stream()
                .filter(o -> o.getTo() != currentWayPoints.get(currentWayPoints.size() - 2))
                .collect(Collectors.toList());
        }
    }

    protected void loadMap(boolean isRefresh) {
        mapViewer.setOverlayPainter(new CompoundPainterBuilder()
            .withMotes(environment)
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

    private class MapMouseAdapter implements MouseListener {
        private ConfigureMotePanel panel;

        MapMouseAdapter(ConfigureMotePanel panel) {
            this.panel = panel;
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                Point p = e.getPoint();
                GeoPosition geo = mapViewer.convertPointToGeoPosition(p);

                boolean exists = false;

                for (Mote mote : environment.getMotes()) {
                    double xDistance = Math.abs(environment.getMapHelper().toMapXCoordinate(geo) - environment.getMapHelper().toMapXCoordinate(mote.getPos()));
                    double yDistance = Math.abs(environment.getMapHelper().toMapYCoordinate(geo) - environment.getMapHelper().toMapYCoordinate(mote.getPos()));
                    if (xDistance < 100 && yDistance > -20 && yDistance < 250) {
                        JFrame frame = new JFrame("Mote settings");
                        MoteGUI moteGUI = new MoteGUI(environment, mote.getOriginalPos(), frame, panel, mainGUI, mote);
                        frame.setContentPane(moteGUI.getMainPanel());
                        frame.setMinimumSize(moteGUI.getMainPanel().getMinimumSize());
                        frame.setPreferredSize(moteGUI.getMainPanel().getPreferredSize());
                        frame.setVisible(true);
                        exists = true;
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                refresh();
                            }
                        });

                    }
                }

                if (!exists) {
                    JFrame frame = new JFrame("New mote");
                    MoteGUI moteGUI = new MoteGUI(environment, geo, frame, panel, mainGUI);
                    frame.setMinimumSize(moteGUI.getMainPanel().getMinimumSize());
                    frame.setPreferredSize(moteGUI.getMainPanel().getPreferredSize());
                    frame.setContentPane(moteGUI.getMainPanel());
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
        mainPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        drawPanel = new JPanel();
        drawPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(drawPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        drawPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final Spacer spacer2 = new Spacer();
        mainPanel.add(spacer2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 15), null, 0, false));
        final Spacer spacer3 = new Spacer();
        mainPanel.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        mainPanel.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(15, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


}
