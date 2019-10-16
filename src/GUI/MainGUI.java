package GUI;


import GUI.MapViewer.*;
import IotDomain.*;
import SelfAdaptation.AdaptationGoals.AdaptationGoal;
import SelfAdaptation.AdaptationGoals.IntervalAdaptationGoal;
import SelfAdaptation.AdaptationGoals.ThresholdAdaptationGoal;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import SelfAdaptation.FeedbackLoop.ReliableEfficientDistanceGateway;
import SelfAdaptation.FeedbackLoop.SignalBasedAdaptation;
import SelfAdaptation.Instrumentation.MoteEffector;
import SelfAdaptation.Instrumentation.MoteProbe;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class MainGUI extends JFrame {
    private JPanel map;
    private JPanel console;
    private JPanel mainPanel;
    private JToolBar toolBarEnvironment;
    private JToolBar toolBarAdaptation;
    private JTabbedPane tabbedPaneGraphs;
    private JButton openButton;
    private JButton configureButton;
    private JButton helpButton;
    private JButton aboutButton;
    private JButton simulationSaveButton;
    private JButton totalRunButton;
    private JLabel centerLabel;
    private JPanel entitesPanel;
    private JScrollPane entitiesPane;
    private JPanel receivedPowerGraph;
    private JPanel powerSettingGraph;
    private JPanel spreadingFactorGraph;
    private JButton environmentSaveButton;
    private JPanel usedEnergyGraph;
    private JPanel transmissionsPanel;
    private JButton moteApplicationButton;
    private JButton regionButton;
    private JTabbedPane tabbedPane1;
    private JButton moteCharacteristicsButton;
    private JLabel moteCharacteristicsLabel;
    private JLabel moteApplicationLabel;
    private JPanel particulateMatterPanel;
    private JPanel carbonDioxideField;
    private JPanel sootPanel;
    private JPanel ozonePanel;
    private JProgressBar totalRunProgressBar;
    private JPanel distanceToGatewayGraph;
    private JPanel InputProfilePanel;
    private JLabel progressLabel;
    private JButton singleRunButton;
    private JComboBox adaptationComboBox;
    private JSlider speedSlider;
    private JButton clearButton;
    private JButton editRelComButton;
    private JButton editEnConButton;
    private JButton editColBoundButton;
    private JLabel relComlabel;
    private JLabel enConLabel;
    private JLabel colBoundLabel;
    private JButton resultsButton;
    private JPanel particulateMatterCenterPanel;
    private JPanel particulateMatterRightPanel;

    private static JXMapViewer mapViewer = new JXMapViewer();
    // Create a TileFactoryInfo for OpenStreetMap
    private static TileFactoryInfo info = new OSMTileFactoryInfo();
    private static DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    private static LinkedList<InputProfile> inputProfiles;
    private static Simulation simulation;
    private static LinkedList<GenericFeedbackLoop> algorithms = new LinkedList<>();
    private LinkedList<MoteProbe> moteProbe;
    private LinkedList<MoteEffector> moteEffector;
    private QualityOfService QoS = new QualityOfService(new HashMap<>());
    private Double usedEnergy;
    private Integer packetsSent;
    private Integer packetsLost;


    public MainGUI() {

        QoS.putAdaptationGoal("reliableCommunication", new IntervalAdaptationGoal(0.0, 0.0));
        QoS.putAdaptationGoal("energyConsumption", new ThresholdAdaptationGoal(0.0));
        QoS.putAdaptationGoal("collisionBound", new ThresholdAdaptationGoal(0.0));

        resultsButton.setEnabled(false);

        editColBoundButton.setEnabled(false);
        editEnConButton.setEnabled(false);
        editRelComButton.setEnabled(false);


        simulation = new Simulation(this);
        inputProfiles = loadInputProfiles();
        updateInputProfile(inputProfiles);
        updateAdaptationGoals();

        /**
         * Loading all the algorithms
         */
        GenericFeedbackLoop noAdaptation = new GenericFeedbackLoop("No Adaptation") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {

            }
        };
        algorithms.add(noAdaptation);

        SignalBasedAdaptation signalBasedAdaptation = new SignalBasedAdaptation(QoS);
        algorithms.add(signalBasedAdaptation);

        ReliableEfficientDistanceGateway reliableEfficientDistanceGateway = new ReliableEfficientDistanceGateway();
        algorithms.add(reliableEfficientDistanceGateway);


        /**
         * Setting the mote probes
         */
        moteProbe = new LinkedList<>();
        moteEffector = new LinkedList<>();
        for (int i = 0; i < algorithms.size(); i++) {
            moteProbe.add(new MoteProbe());
            moteEffector.add(new MoteEffector());
        }
        for (GenericFeedbackLoop feedbackLoop : algorithms) {
            feedbackLoop.setMoteProbe(moteProbe.get(algorithms.indexOf(feedbackLoop)));
            feedbackLoop.setMoteEffector(moteEffector.get(algorithms.indexOf(feedbackLoop)));
        }

        ConfigureActionListener configureActionListener = new ConfigureActionListener(this);
        configureButton.addActionListener(configureActionListener);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Load a configuration");
                fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));

                File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                String basePath = file.getParentFile().getParent();
                fc.setCurrentDirectory(new File(Paths.get(basePath, "res", "configurations").toUri()));

                int returnVal = fc.showOpenDialog(mainPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();

                    JFrame frame = new JFrame("Loading configuration");
                    LoadingGUI loadingGUI = new LoadingGUI();
                    frame.setContentPane(loadingGUI.getMainPanel());
                    frame.setMinimumSize(new Dimension(300, 300));
                    frame.setVisible(true);

                    try {
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                        Document doc = docBuilder.parse(file);
                        Element configuration = doc.getDocumentElement();
                        Element map = (Element) configuration.getElementsByTagName("map").item(0);
                        Element characteristics = (Element) configuration.getElementsByTagName("characteristics").item(0);
                        Element motes = (Element) configuration.getElementsByTagName("motes").item(0);
                        Element gateways = (Element) configuration.getElementsByTagName("gateways").item(0);
                        Element wayPoints = (Element) configuration.getElementsByTagName("wayPoints").item(0);
                        Element region = (Element) map.getElementsByTagName("region").item(0);
                        Element origin = (Element) region.getElementsByTagName("origin").item(0);
                        GeoPosition mapOrigin = new GeoPosition(Double.valueOf(origin.getElementsByTagName("latitude").item(0).getTextContent())
                                , Double.valueOf(origin.getElementsByTagName("longitude").item(0).getTextContent()));
                        Integer width = Integer.valueOf(region.getElementsByTagName("width").item(0).getTextContent());
                        Integer height = Integer.valueOf(region.getElementsByTagName("height").item(0).getTextContent());
                        Integer numberOfZones = Integer.valueOf(((Element) characteristics.getElementsByTagName("regionProperty").item(0)).getAttribute("numberOfZones"));

                        Characteristic[][] characteristicsMap = new Characteristic[width][height];
                        for (int j = 0; j < Math.round(Math.sqrt(numberOfZones)); j++) {
                            int i = 0;
                            for (String characteristicName : characteristics.getElementsByTagName("row").item(j).getTextContent().split("-")) {
                                Characteristic characteristic = Characteristic.valueOf(characteristicName);
                                for (int x = (int) Math.round(i * ((double) width) / Math.round(Math.sqrt(numberOfZones)));
                                     x < (int) Math.round((i + 1) * ((double) width) / Math.round(Math.sqrt(numberOfZones))); x++) {
                                    for (int y = (int) Math.round(j * ((double) height) / Math.round(Math.sqrt(numberOfZones)));
                                         y < (int) Math.round((j + 1) * ((double) height) / Math.round(Math.sqrt(numberOfZones))); y++) {
                                        characteristicsMap[x][y] = characteristic;

                                    }

                                }
                                i++;
                            }

                        }

                        LinkedHashSet<GeoPosition> wayPointsSet = new LinkedHashSet<>();
                        for (int i = 0; i < wayPoints.getElementsByTagName("wayPoint").getLength(); i++) {
                            Element waypoint = (Element) wayPoints.getElementsByTagName("wayPoint").item(i);
                            Double wayPointLatitude = Double.valueOf(waypoint.getTextContent().split(",")[0]);
                            Double wayPointLongitude = Double.valueOf(waypoint.getTextContent().split(",")[1]);
                            wayPointsSet.add(new GeoPosition(wayPointLatitude, wayPointLongitude));
                        }

                        simulation.setEnvironment(new Environment(characteristicsMap, mapOrigin, wayPointsSet));

                        Element moteNode;

                        for (int i = 0; i < motes.getElementsByTagName("mote").getLength(); i++) {
                            moteNode = (Element) motes.getElementsByTagName("mote").item(i);
                            Long devEUI = Long.parseUnsignedLong(moteNode.getElementsByTagName("devEUI").item(0).getTextContent());
                            Element location = (Element) moteNode.getElementsByTagName("location").item(0);
                            Integer xPos = Integer.valueOf(location.getElementsByTagName("xPos").item(0).getTextContent());
                            Integer yPos = Integer.valueOf(location.getElementsByTagName("yPos").item(0).getTextContent());
                            Integer transmissionPower = Integer.valueOf(moteNode.getElementsByTagName("transmissionPower").item(0).getTextContent());
                            Integer spreadingFactor = Integer.valueOf(moteNode.getElementsByTagName("spreadingFactor").item(0).getTextContent());
                            Integer energyLevel = Integer.valueOf(moteNode.getElementsByTagName("energyLevel").item(0).getTextContent());
                            Integer samplingRate = Integer.valueOf(moteNode.getElementsByTagName("samplingRate").item(0).getTextContent());
                            Double movementSpeed = Double.valueOf(moteNode.getElementsByTagName("movementSpeed").item(0).getTextContent());

                            Element sensors = (Element) moteNode.getElementsByTagName("sensors").item(0);
                            LinkedList<MoteSensor> moteSensors = new LinkedList<>();
                            for (int j = 0; j < sensors.getElementsByTagName("sensors").getLength(); j++) {
                                Element sensornode = (Element) sensors.getElementsByTagName("sensor").item(j);
                                moteSensors.add(MoteSensor.valueOf(sensornode.getAttribute("SensorType")));
                            }
//                            while (sensornode != null) {
//                                sensornode = (Element) sensornode.getNextSibling();
//                            }
                            Element pathElement = (Element) moteNode.getElementsByTagName("path").item(0);
                            Element waypoint;
                            LinkedList<GeoPosition> path = new LinkedList<>();
                            for (int j = 0; j < pathElement.getElementsByTagName("wayPoint").getLength(); j++) {
                                waypoint = (Element) pathElement.getElementsByTagName("wayPoint").item(j);
                                Integer wayPointX = Integer.valueOf(waypoint.getTextContent().split(",")[0]);
                                Integer wayPointY = Integer.valueOf(waypoint.getTextContent().split(",")[1]);
                                path.add(new GeoPosition(simulation.getEnvironment().toLatitude(wayPointY), simulation.getEnvironment().toLongitude(wayPointX)));
                            }
                            new Mote(devEUI, xPos, yPos, simulation.getEnvironment(), transmissionPower, spreadingFactor, moteSensors, energyLevel, path, samplingRate, movementSpeed);
                        }

                        Element gatewayNode;

                        for (int i = 0; i < gateways.getElementsByTagName("gateway").getLength(); i++) {
                            gatewayNode = (Element) gateways.getElementsByTagName("gateway").item(i);
                            Long devEUI = Long.parseUnsignedLong(gatewayNode.getElementsByTagName("devEUI").item(0).getTextContent());
                            Element location = (Element) gatewayNode.getElementsByTagName("location").item(0);
                            Integer xPos = Integer.valueOf(location.getElementsByTagName("xPos").item(0).getTextContent());
                            Integer yPos = Integer.valueOf(location.getElementsByTagName("yPos").item(0).getTextContent());
                            Integer transmissionPower = Integer.valueOf(gatewayNode.getElementsByTagName("transmissionPower").item(0).getTextContent());
                            Integer spreadingFactor = Integer.valueOf(gatewayNode.getElementsByTagName("spreadingFactor").item(0).getTextContent());
                            new Gateway(devEUI, xPos, yPos, simulation.getEnvironment(), transmissionPower, spreadingFactor);
                        }


                    } catch (ParserConfigurationException e1) {
                        e1.printStackTrace();
                    } catch (SAXException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    frame.setVisible(false);
                    updateEntries(simulation.getEnvironment());
                    loadMap(simulation.getEnvironment(), mapViewer, false);

                    for (Gateway gateway : simulation.getEnvironment().getGateways()) {
                        for (int i = 0; i < algorithms.size(); i++) {
                            gateway.addSubscription(moteProbe.get(i));
                        }
                    }

                    MouseInputListener mia = new PanMouseInputListener(mapViewer);
                    mapViewer.addMouseListener(mia);
                    mapViewer.addMouseMotionListener(mia);
                    mapViewer.addMouseListener(new CenterMapListener(mapViewer));
                    mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
                }
            }
        });
        environmentSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save a configuration");
                fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));
                try {
                    File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    file = new File(file.getParent() + "/user/configurations");
                    fc.setCurrentDirectory(file);
                } catch (URISyntaxException e1) {

                }
                int returnVal = fc.showSaveDialog(mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    file = new File(file.getPath() + ".xml");
                    try {
                        DocumentBuilderFactory dbFactory =
                                DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.newDocument();

                        // root element
                        Element rootElement = doc.createElement("configuration");
                        doc.appendChild(rootElement);

                        Element map = doc.createElement("map");
                        Element region = doc.createElement("region");
                        Element origin = doc.createElement("origin");
                        map.appendChild(origin);

                        Element MapZeroLatitude = doc.createElement("latitude");
                        MapZeroLatitude.appendChild(doc.createTextNode(Double.toString(simulation.getEnvironment().getMapOrigin().getLatitude())));
                        origin.appendChild(MapZeroLatitude);

                        Element MapZeroLongitude = doc.createElement("longitude");
                        MapZeroLongitude.appendChild(doc.createTextNode(Double.toString(simulation.getEnvironment().getMapOrigin().getLongitude())));
                        origin.appendChild(MapZeroLongitude);
                        region.appendChild(origin);

                        Element size = doc.createElement("size");
                        Element width = doc.createElement("width");
                        width.appendChild(doc.createTextNode(Integer.toString(simulation.getEnvironment().getMaxXpos())));
                        Element height = doc.createElement("height");
                        height.appendChild(doc.createTextNode(Integer.toString(simulation.getEnvironment().getMaxYpos())));
                        size.appendChild(width);
                        size.appendChild(height);
                        region.appendChild(size);

                        map.appendChild(region);
                        rootElement.appendChild(map);

                        Element characteristics = doc.createElement("characteristics");
                        Element regionProperty = doc.createElement("regionProperty");
                        regionProperty.setAttribute("numberOfZones", Integer.toString(simulation.getEnvironment().getNumberOfZones()));
                        characteristics.appendChild(regionProperty);
                        int amountOfSquares = (int) Math.sqrt(simulation.getEnvironment().getNumberOfZones());
                        LinkedList<Element> row = new LinkedList<>();
                        for (int j = 0; j < amountOfSquares; j++) {
                            row.add(doc.createElement("row"));
                            row.getLast().appendChild(doc.createTextNode(simulation.getEnvironment().getCharacteristic(0, (int) Math.round(j * ((double) simulation.getEnvironment().getMaxXpos()) / amountOfSquares) + 1
                            ).name()));
                            for (int i = 1; i < amountOfSquares; i++) {

                                row.getLast().appendChild(doc.createTextNode("-" + simulation.getEnvironment().getCharacteristic((int) Math.round(i * ((double) simulation.getEnvironment().getMaxXpos()) / amountOfSquares) + 1
                                        , (int) Math.round(j * ((double) simulation.getEnvironment().getMaxYpos()) / amountOfSquares) + 1).name()));
                            }
                            characteristics.appendChild(row.getLast());
                        }

                        rootElement.appendChild(characteristics);

                        Element motes = doc.createElement("motes");

                        for (Mote mote : simulation.getEnvironment().getMotes()) {
                            Element moteElement = doc.createElement("mote");
                            Element devEUI = doc.createElement("devEUI");
                            devEUI.appendChild(doc.createTextNode(Long.toUnsignedString(mote.getEUI())));
                            Element location = doc.createElement("location");
                            Element xPos = doc.createElement("xPos");
                            xPos.appendChild(doc.createTextNode(mote.getXPos().toString()));
                            Element yPos = doc.createElement("yPos");
                            location.appendChild(xPos);
                            location.appendChild(yPos);
                            yPos.appendChild(doc.createTextNode(mote.getYPos().toString()));
                            Element transmissionPower = doc.createElement("transmissionPower");
                            transmissionPower.appendChild(doc.createTextNode(mote.getTransmissionPower().toString()));
                            Element spreadingFactor = doc.createElement("spreadingFactor");
                            spreadingFactor.appendChild(doc.createTextNode(mote.getSF().toString()));
                            Element energyLevel = doc.createElement("energyLevel");
                            energyLevel.appendChild(doc.createTextNode(mote.getEnergyLevel().toString()));
                            Element samplingRate = doc.createElement("samplingRate");
                            samplingRate.appendChild(doc.createTextNode(mote.getSamplingRate().toString()));
                            Element movementSpeed = doc.createElement("movementSpeed");
                            movementSpeed.appendChild(doc.createTextNode(mote.getMovementSpeed().toString()));
                            moteElement.appendChild(devEUI);
                            moteElement.appendChild(location);
                            moteElement.appendChild(transmissionPower);
                            moteElement.appendChild(spreadingFactor);
                            moteElement.appendChild(energyLevel);
                            moteElement.appendChild(samplingRate);
                            moteElement.appendChild(movementSpeed);
                            Element sensors = doc.createElement("sensors");
                            for (MoteSensor sensor : mote.getSensors()) {
                                Element sensorElement = doc.createElement("sensor");
                                sensorElement.setAttribute("SensorType", sensor.name());
                                sensors.appendChild(sensorElement);

                            }
                            moteElement.appendChild(sensors);

                            Element pathElement = doc.createElement("path");
                            for (GeoPosition waypoint : mote.getPath()) {
                                Element waypointElement = doc.createElement("wayPoint");
                                waypointElement.appendChild(doc.createTextNode(simulation.getEnvironment().toMapXCoordinate(waypoint) + "," + simulation.getEnvironment().toMapYCoordinate(waypoint)));
                                pathElement.appendChild(waypointElement);
                            }
                            moteElement.appendChild(pathElement);
                            motes.appendChild(moteElement);
                        }

                        rootElement.appendChild(motes);

                        Element gateways = doc.createElement("gateways");

                        for (Gateway gateway : simulation.getEnvironment().getGateways()) {
                            Element gatewayElement = doc.createElement("gateway");
                            Element devEUI = doc.createElement("devEUI");
                            devEUI.appendChild(doc.createTextNode(Long.toUnsignedString(gateway.getEUI())));
                            Element location = doc.createElement("location");
                            Element xPos = doc.createElement("xPos");
                            xPos.appendChild(doc.createTextNode(gateway.getXPos().toString()));
                            Element yPos = doc.createElement("yPos");
                            location.appendChild(xPos);
                            location.appendChild(yPos);
                            yPos.appendChild(doc.createTextNode(gateway.getYPos().toString()));
                            Element transmissionPower = doc.createElement("transmissionPower");
                            transmissionPower.appendChild(doc.createTextNode(gateway.getTransmissionPower().toString()));
                            Element spreadingFactor = doc.createElement("spreadingFactor");
                            spreadingFactor.appendChild(doc.createTextNode(gateway.getSF().toString()));
                            gatewayElement.appendChild(devEUI);
                            gatewayElement.appendChild(location);
                            gatewayElement.appendChild(transmissionPower);
                            gatewayElement.appendChild(spreadingFactor);
                            gateways.appendChild(gatewayElement);
                        }

                        rootElement.appendChild(gateways);

                        Element wayPoints = doc.createElement("wayPoints");

                        for (GeoPosition wayPoint : simulation.getEnvironment().getWayPoints()) {
                            Element wayPointElement = doc.createElement("wayPoint");
                            wayPointElement.appendChild(doc.createTextNode(wayPoint.getLatitude() + "," + wayPoint.getLongitude()));
                            wayPoints.appendChild(wayPointElement);

                        }

                        rootElement.appendChild(wayPoints);

                        // write the content into xml file
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(file);
                        transformer.transform(source, result);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        moteCharacteristicsButton.addActionListener(new MoteSelectActionListener(this));
        moteApplicationButton.addActionListener(new MoteApplicationSelectActionListener(this));
        regionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setApplicationGraphs(Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), simulation.getEnvironment());
            }
        });
        singleRunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulation.singleRun(speedSlider.getValue());
            }
        });
        adaptationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GenericFeedbackLoop selectedAlgorithm = algorithms.get(0);
                for (GenericFeedbackLoop algorithm : algorithms) {
                    if (algorithm.getName() == adaptationComboBox.getSelectedItem()) {
                        selectedAlgorithm = algorithm;
                    }
                }
                simulation.setApproach(selectedAlgorithm);
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moteCharacteristicsLabel.setText("");
                // update received power graph
                receivedPowerGraph.removeAll();
                receivedPowerGraph.repaint();
                receivedPowerGraph.revalidate();
                // update power setting graph
                powerSettingGraph.removeAll();
                powerSettingGraph.repaint();
                powerSettingGraph.revalidate();
                // update spreading factor graph
                spreadingFactorGraph.removeAll();
                spreadingFactorGraph.repaint();
                spreadingFactorGraph.revalidate();
                // update used energy graph
                usedEnergyGraph.removeAll();
                usedEnergyGraph.repaint();
                usedEnergyGraph.revalidate();
                // update distance to gateway graph
                distanceToGatewayGraph.removeAll();
                distanceToGatewayGraph.repaint();
                distanceToGatewayGraph.revalidate();

                moteApplicationLabel.setText("");

                ozonePanel.removeAll();
                ozonePanel.repaint();
                ozonePanel.revalidate();

                sootPanel.removeAll();
                sootPanel.repaint();
                sootPanel.revalidate();

                carbonDioxideField.removeAll();
                carbonDioxideField.repaint();
                carbonDioxideField.revalidate();

                particulateMatterPanel.removeAll();
                particulateMatterPanel.repaint();
                particulateMatterPanel.revalidate();

                resultsButton.setEnabled(false);
            }
        });
        totalRunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(simulation);
                t.start();
            }
        });
        simulationSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save output");
                fc.setFileFilter(new FileNameExtensionFilter("xml output", "xml"));
                try {
                    File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    file = new File(file.getParent() + "/user/output");
                    fc.setCurrentDirectory(file);
                } catch (URISyntaxException e1) {
                    System.out.println("URi");
                }
                int returnVal = fc.showSaveDialog(mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    file = new File(file.getPath() + ".xml");
                    try {
                        DocumentBuilderFactory dbFactory =
                                DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.newDocument();

                        // root element
                        Element rootElement = doc.createElement("experimentalData");
                        doc.appendChild(rootElement);

                        Element runs = doc.createElement("runs");

                        for (int i = 0; i < simulation.getEnvironment().getNumberOfRuns(); i++) {
                            Element runElement = doc.createElement("run");

                            for (Mote mote : simulation.getEnvironment().getMotes()) {
                                Element moteElement = doc.createElement("mote");
                                Element number = doc.createElement("number");
                                number.appendChild(doc.createTextNode(((Integer) (simulation.getEnvironment().getMotes().indexOf(mote) + 1)).toString()));
                                moteElement.appendChild(number);
                                Element receivedTransmissions = doc.createElement("receivedTransmissions");
                                int j = 0;
                                for (LoraTransmission transmission : mote.getSentTransmissions(i)) {
                                    Element receivedTransmissionElement = doc.createElement("receivedTransmission");
                                    Element sender = doc.createElement("sender");
                                    if (transmission.getSender().getClass() == Mote.class) {
                                        sender.appendChild(doc.createTextNode("Mote " + simulation.getEnvironment().getMotes().indexOf((Mote) transmission.getSender())));
                                    } else {
                                        sender.appendChild(doc.createTextNode("Gateway " + simulation.getEnvironment().getGateways().indexOf((Gateway) transmission.getSender())));
                                    }
                                    Element transmissionPower = doc.createElement("transmissionPower");
                                    transmissionPower.appendChild(doc.createTextNode(Double.toString(transmission.getTransmissionPower())));
                                    Element bandwidth = doc.createElement("bandwidth");
                                    bandwidth.appendChild(doc.createTextNode(Double.toString(transmission.getBandwidth())));
                                    Element spreadingFactor = doc.createElement("spreadingFactor");
                                    spreadingFactor.appendChild(doc.createTextNode(Double.toString(transmission.getSpreadingFactor())));
                                    Element origin = doc.createElement("origin");
                                    Element xPos = doc.createElement("xPosition");
                                    xPos.appendChild(doc.createTextNode(transmission.getXPos().toString()));
                                    Element yPos = doc.createElement("yPosition");
                                    yPos.appendChild(doc.createTextNode(transmission.getYPos().toString()));
                                    origin.appendChild(xPos);
                                    origin.appendChild(yPos);
                                    Element contentSize = doc.createElement("contentSize");
                                    contentSize.appendChild(doc.createTextNode(transmission.getContent().getLength().toString()));
                                    Element departureTime = doc.createElement("departureTime");
                                    departureTime.appendChild(doc.createTextNode(transmission.getDepartureTime().toString()));
                                    Element timeOnAir = doc.createElement("timeOnAir");
                                    timeOnAir.appendChild(doc.createTextNode(transmission.getTimeOnAir().toString()));
                                    Element powerSetting = doc.createElement("powerSetting");
                                    powerSetting.appendChild(doc.createTextNode(mote.getPowerSettingHistory(i).get(j).toString()));
                                    Element collision = doc.createElement("collision");
                                    collision.appendChild(doc.createTextNode(transmission.getReceiver().getAllReceivedTransmissions(i).get(transmission).toString()));
                                    receivedTransmissionElement.appendChild(sender);
                                    receivedTransmissionElement.appendChild(transmissionPower);
                                    receivedTransmissionElement.appendChild(bandwidth);
                                    receivedTransmissionElement.appendChild(spreadingFactor);
                                    receivedTransmissionElement.appendChild(origin);
                                    receivedTransmissionElement.appendChild(contentSize);
                                    receivedTransmissionElement.appendChild(departureTime);
                                    receivedTransmissionElement.appendChild(timeOnAir);
                                    receivedTransmissionElement.appendChild(powerSetting);
                                    receivedTransmissionElement.appendChild(collision);

                                    receivedTransmissions.appendChild(receivedTransmissionElement);

                                    j++;

                                }
                                moteElement.appendChild(receivedTransmissions);
                                runElement.appendChild(moteElement);
                            }

                            for (Gateway gateway : simulation.getEnvironment().getGateways()) {
                                Element gatewayElement = doc.createElement("gateway");
                                Element number = doc.createElement("number");
                                number.appendChild(doc.createTextNode(((Integer) (simulation.getEnvironment().getGateways().indexOf(gateway) + 1)).toString()));
                                gatewayElement.appendChild(number);
                                Element receivedTransmissions = doc.createElement("receivedTransmissions");
                                int j = 0;
                                for (LoraTransmission transmission : gateway.getSentTransmissions(i)) {
                                    Element receivedTransmissionElement = doc.createElement("receivedTransmission");
                                    Element sender = doc.createElement("sender");
                                    if (transmission.getSender().getClass() == Mote.class) {
                                        sender.appendChild(doc.createTextNode("Mote " + simulation.getEnvironment().getMotes().indexOf((Mote) transmission.getSender())));
                                    } else {
                                        sender.appendChild(doc.createTextNode("Gateway " + simulation.getEnvironment().getGateways().indexOf((Gateway) transmission.getSender())));
                                    }
                                    Element transmissionPower = doc.createElement("transmissionPower");
                                    transmissionPower.appendChild(doc.createTextNode(Double.toString(transmission.getTransmissionPower())));
                                    Element bandwidth = doc.createElement("bandwidth");
                                    bandwidth.appendChild(doc.createTextNode(Double.toString(transmission.getBandwidth())));
                                    Element spreadingFactor = doc.createElement("spreadingFactor");
                                    spreadingFactor.appendChild(doc.createTextNode(Double.toString(transmission.getSpreadingFactor())));
                                    Element origin = doc.createElement("origin");
                                    Element xPos = doc.createElement("xPosition");
                                    xPos.appendChild(doc.createTextNode(transmission.getXPos().toString()));
                                    Element yPos = doc.createElement("yPosition");
                                    yPos.appendChild(doc.createTextNode(transmission.getYPos().toString()));
                                    origin.appendChild(xPos);
                                    origin.appendChild(yPos);
                                    Element contentSize = doc.createElement("contentSize");
                                    contentSize.appendChild(doc.createTextNode(transmission.getContent().getLength().toString()));
                                    Element departureTime = doc.createElement("departureTime");
                                    departureTime.appendChild(doc.createTextNode(transmission.getDepartureTime().toString()));
                                    Element timeOnAir = doc.createElement("timeOnAir");
                                    timeOnAir.appendChild(doc.createTextNode(transmission.getTimeOnAir().toString()));
                                    Element powerSetting = doc.createElement("powerSetting");
                                    powerSetting.appendChild(doc.createTextNode(gateway.getPowerSettingHistory(i).get(j).toString()));
                                    Element collision = doc.createElement("collision");
                                    collision.appendChild(doc.createTextNode(transmission.getReceiver().getAllReceivedTransmissions(i).get(transmission).toString()));
                                    receivedTransmissionElement.appendChild(sender);
                                    receivedTransmissionElement.appendChild(transmissionPower);
                                    receivedTransmissionElement.appendChild(bandwidth);
                                    receivedTransmissionElement.appendChild(spreadingFactor);
                                    receivedTransmissionElement.appendChild(origin);
                                    receivedTransmissionElement.appendChild(contentSize);
                                    receivedTransmissionElement.appendChild(departureTime);
                                    receivedTransmissionElement.appendChild(timeOnAir);
                                    receivedTransmissionElement.appendChild(powerSetting);
                                    receivedTransmissionElement.appendChild(collision);

                                    receivedTransmissions.appendChild(receivedTransmissionElement);

                                    j++;

                                }
                                gatewayElement.appendChild(receivedTransmissions);
                                runElement.appendChild(gatewayElement);


                            }
                            runs.appendChild(runElement);
                        }

                        rootElement.appendChild(runs);

                        // write the content into xml file
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(file);
                        transformer.transform(source, result);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HelpGUI helpgui = new HelpGUI();
                helpgui.pack();
                helpgui.setVisible(true);
            }
        });
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutSimDialog aboutSimDialog = new AboutSimDialog();
                aboutSimDialog.pack();
                aboutSimDialog.setVisible(true);
            }
        });
        editRelComButton.addActionListener(new ComRelActionListener(this));
        editColBoundButton.addActionListener(new ColBoundActionListener(this));
        editEnConButton.addActionListener(new EnConActionListener(this));
        resultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MoteCharactesticsDialog moteCharactesticsDialog = new MoteCharactesticsDialog(usedEnergy, packetsSent, packetsLost);
                moteCharactesticsDialog.pack();
                moteCharactesticsDialog.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {

        mapViewer.setTileFactory(tileFactory);

        tileFactory.setThreadPoolSize(4);

        JFrame frame = new JFrame("Dynamic DingNet simulator");
        MainGUI gui = new MainGUI();
        frame.setContentPane(gui.mainPanel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        gui.updateInputProfile(inputProfiles);
        gui.loadAlgorithms(algorithms);
        frame.setVisible(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    private void loadAlgorithms(LinkedList<GenericFeedbackLoop> algorithms) {
        DefaultComboBoxModel adaptationComboBoxModel = new DefaultComboBoxModel();
        for (GenericFeedbackLoop algorithm : algorithms) {
            adaptationComboBoxModel.addElement(algorithm.getName());
        }

        adaptationComboBox.setModel(adaptationComboBoxModel);
    }

    private void updateEntries(Environment environment) {
        entitesPanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);
        JTextArea textArea;
        for (Gateway gateway : environment.getGateways()) {
            textArea = new JTextArea();
            textArea.append("Gateway " + (environment.getGateways().indexOf(gateway) + 1) + ":\n");
            textArea.append("EUID: " + Long.toUnsignedString(gateway.getEUI()) + "\n");
            Double latitude = environment.toLatitude(gateway.getYPos());
            Integer latitudeDegrees = (int) Math.round(Math.floor(latitude));
            Integer latitudeMinutes = (int) Math.round(Math.floor((latitude - latitudeDegrees) * 60));
            Double latitudeSeconds = (double) Math.round(((latitude - latitudeDegrees) * 60 - latitudeMinutes) * 60 * 1000d) / 1000d;
            Double longitude = environment.toLongitude(gateway.getXPos());
            Integer longitudeDegrees = (int) Math.round(Math.floor(longitude));
            Integer longitudeMinutes = (int) Math.round(Math.floor((longitude - longitudeDegrees) * 60));
            Double longitudeSeconds = (double) Math.round(((longitude - longitudeDegrees) * 60 - longitudeMinutes) * 60 * 1000d) / 1000d;
            textArea.append(((Math.signum(environment.toLatitude(gateway.getYPos())) == 1) ? "N " : "S ") +
                    latitudeDegrees + "° " + latitudeMinutes + "' " + latitudeSeconds + "\" " + ", " +
                    ((Math.signum(environment.toLongitude(gateway.getXPos())) == 1) ? "E " : "W ") +
                    longitudeDegrees + "° " + longitudeMinutes + "' " + longitudeSeconds + "\" ");
            for (int i = 0; i < textArea.getMouseListeners().length; i++) {
                textArea.removeMouseListener(textArea.getMouseListeners()[i]);
            }
            textArea.addMouseListener(gateWayMouse);
            entitesPanel.add(textArea, constraints);
        }
        for (Mote mote : environment.getMotes()) {
            textArea = new JTextArea();
            textArea.append("Mote " + (environment.getMotes().indexOf(mote) + 1) + ":\n");
            textArea.append("EUID: " + Long.toUnsignedString(mote.getEUI()) + "\n");
            Double latitude = environment.toLatitude(mote.getYPos());
            Integer latitudeDegrees = (int) Math.round(Math.floor(latitude));
            Integer latitudeMinutes = (int) Math.round(Math.floor((latitude - latitudeDegrees) * 60));
            Double latitudeSeconds = (double) Math.round(((latitude - latitudeDegrees) * 60 - latitudeMinutes) * 60 * 1000d) / 1000d;
            Double longitude = environment.toLongitude(mote.getXPos());
            Integer longitudeDegrees = (int) Math.round(Math.floor(longitude));
            Integer longitudeMinutes = (int) Math.round(Math.floor((longitude - longitudeDegrees) * 60));
            Double longitudeSeconds = (double) Math.round(((longitude - longitudeDegrees) * 60 - longitudeMinutes) * 60 * 1000d) / 1000d;
            textArea.append(((Math.signum(environment.toLatitude(mote.getYPos())) == 1) ? "N " : "S ") +
                    latitudeDegrees + "° " + latitudeMinutes + "' " + latitudeSeconds + "\" " + ", " +
                    ((Math.signum(environment.toLongitude(mote.getXPos())) == 1) ? "E " : "W ") +
                    longitudeDegrees + "° " + longitudeMinutes + "' " + longitudeSeconds + "\" ");
            for (int i = 0; i < textArea.getMouseListeners().length; i++) {
                textArea.removeMouseListener(textArea.getMouseListeners()[i]);
            }
            textArea.addMouseListener(moteMouse);
            entitesPanel.add(textArea, constraints);
        }
        entitesPanel.repaint();
        entitesPanel.revalidate();
    }

    private LinkedList<InputProfile> getInputProfiles() {
        return inputProfiles;
    }

    public void updateInputProfilesFile() {
        File file;
        try {
            file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(file.getParent() + "/inputProfiles/inputProfile.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            Element inputProfilesElement = doc.getDocumentElement();
            for (int i = 0; i < inputProfilesElement.getChildNodes().getLength(); ) {
                inputProfilesElement.removeChild(inputProfilesElement.getChildNodes().item(0));
            }
            for (InputProfile inputProfile : inputProfiles) {
                Node importedNode = doc.importNode(inputProfile.getXmlSource().getDocumentElement(), true);
                inputProfilesElement.appendChild(importedNode);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    private LinkedList<InputProfile> loadInputProfiles() {
        LinkedList<InputProfile> inputProfiles = new LinkedList<>();
        try {
            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(file.getParent() + "/inputProfiles/inputProfile.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            Element inputProfilesElement = doc.getDocumentElement();
            for (int i = 0; i < inputProfilesElement.getElementsByTagName("inputProfile").getLength(); i++) {
                Element inputProfileElement = (Element) inputProfilesElement.getElementsByTagName("inputProfile").item(i);
                String name = inputProfileElement.getElementsByTagName("name").item(0).getTextContent();
                Integer numberOfRuns = Integer.valueOf(inputProfileElement.getElementsByTagName("numberOfRuns").item(0).getTextContent());
                Element QoSElement = (Element) inputProfileElement.getElementsByTagName("QoS").item(0);
                HashMap<String, AdaptationGoal> adaptationGoalHashMap = new HashMap<>();
                for (int j = 0; j < QoSElement.getElementsByTagName("adaptationGoal").getLength(); j++) {
                    Element adaptationGoalElement = (Element) QoSElement.getElementsByTagName("adaptationGoal").item(j);
                    String goalName = adaptationGoalElement.getElementsByTagName("name").item(0).getTextContent();
                    AdaptationGoal adaptationGoal = new ThresholdAdaptationGoal(0.0);
                    if (adaptationGoalElement.getAttribute("type").equals("interval")) {
                        Double upperValue = Double.valueOf(adaptationGoalElement.getElementsByTagName("upperValue").item(0).getTextContent());
                        Double lowerValue = Double.valueOf(adaptationGoalElement.getElementsByTagName("lowerValue").item(0).getTextContent());
                        adaptationGoal = new IntervalAdaptationGoal(lowerValue, upperValue);
                    }
                    if (adaptationGoalElement.getAttribute("type").equals("threshold")) {
                        Double threshold = Double.valueOf(adaptationGoalElement.getElementsByTagName("threshold").item(0).getTextContent());
                        adaptationGoal = new ThresholdAdaptationGoal(threshold);
                    }
                    adaptationGoalHashMap.put(goalName, adaptationGoal);
                }
                HashMap<Integer, Double> moteProbabilaties = new HashMap<>();
                for (int j = 0; j < inputProfileElement.getElementsByTagName("mote").getLength(); j++) {
                    Element moteElement = (Element) inputProfileElement.getElementsByTagName("mote").item(j);

                    moteProbabilaties.put(Integer.valueOf(moteElement.getElementsByTagName("moteNumber").item(0).getTextContent()) - 1, Double.parseDouble(moteElement.getElementsByTagName("activityProbability").item(0).getTextContent()));
                }

                inputProfiles.add(new InputProfile(name, new QualityOfService(adaptationGoalHashMap), numberOfRuns, moteProbabilaties, new HashMap<>(), new HashMap<>(), inputProfileElement, this));
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return inputProfiles;

    }

    private void updateInputProfile(LinkedList<InputProfile> inputProfiles) {
        InputProfilePanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);
        constraints.weighty = 0;
        JPanel panel;

        for (InputProfile inputProfile : getInputProfiles()) {
            panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(InputProfilePanel.getWidth() - 10, 50));
            panel.setBackground(Color.white);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            Image unselectedImage = null;
            Image selectedImage = null;
            Image editImage = null;
            try {
                unselectedImage = ImageIO.read(getClass().getResource("/GUI/circle_unselected.png"));
                unselectedImage = unselectedImage.getScaledInstance(23, 23, 0);
                selectedImage = ImageIO.read(getClass().getResource("/GUI/circle_selected.png"));
                selectedImage = selectedImage.getScaledInstance(23, 23, 0);
                editImage = ImageIO.read(getClass().getResource("/GUI/edit_icon.png"));
                editImage = editImage.getScaledInstance(23, 23, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JPanel subPanel1 = new JPanel();
            subPanel1.setOpaque(false);
            JPanel subPanel2 = new JPanel();
            subPanel2.setOpaque(false);
            if (inputProfile == simulation.getInputProfile()) {
                subPanel1.add(new JLabel(new ImageIcon(selectedImage)));
            } else
                subPanel1.add(new JLabel(new ImageIcon(unselectedImage)));
            subPanel1.add(new JLabel(inputProfile.getName()));
            panel.add(subPanel1, BorderLayout.WEST);
            subPanel2.add(new JLabel(new ImageIcon(editImage)));
            panel.add(subPanel2, BorderLayout.EAST);

            subPanel2.addMouseListener(new InputProfileEditMouse(inputProfile));
            subPanel1.addMouseListener(new InputProfileSelectMouse(inputProfile));
            InputProfilePanel.add(panel, constraints);
        }

        panel = new JPanel();
        constraints.weighty = 0.9;
        InputProfilePanel.add(panel, constraints);
        InputProfilePanel.repaint();
        InputProfilePanel.revalidate();
    }

    private void updateAdaptationGoals() {
        relComlabel.setText("Interval: [" + ((IntervalAdaptationGoal) QoS.getAdaptationGoal("reliableCommunication")).getLowerBoundary() + "," + ((IntervalAdaptationGoal) QoS.getAdaptationGoal("reliableCommunication")).getUpperBoundary() + "]");
        enConLabel.setText("Threshold: " + ((ThresholdAdaptationGoal) QoS.getAdaptationGoal("energyConsumption")).getThreshold());
        colBoundLabel.setText("Threshold: " + ((ThresholdAdaptationGoal) QoS.getAdaptationGoal("collisionBound")).getThreshold() * 100);

    }

    private void loadMap(Environment environment, JXMapViewer mapViewer, Boolean isRefresh) {

        GeoPosition centerPosition = mapViewer.getCenterPosition();
        Integer zoom = mapViewer.getZoom();

        Map<Waypoint, Integer> gateWays = new HashMap();
        int i = 1;
        for (Gateway gateway : environment.getGateways()) {
            gateWays.put(new DefaultWaypoint(new GeoPosition(environment.toLatitude(gateway.getYPos()), environment.toLongitude(gateway.getXPos()))), i);
            i++;
        }
        i = 1;
        Map<Waypoint, Integer> motes = new HashMap();
        for (Mote mote : environment.getMotes()) {
            motes.put(new DefaultWaypoint(new GeoPosition(environment.toLatitude(mote.getYPos()), environment.toLongitude(mote.getXPos()))), i);
            i++;
        }
        GatewayNumberWaypointPainter<Waypoint> gateWayNumberPainter = new GatewayNumberWaypointPainter<>();
        gateWayNumberPainter.setWaypoints(gateWays);

        GatewayWaypointPainter<Waypoint> gateWayPainter = new GatewayWaypointPainter<>();
        gateWayPainter.setWaypoints(gateWays.keySet());

        MoteWaypointPainter<Waypoint> motePainter = new MoteWaypointPainter<>();
        motePainter.setWaypoints(motes.keySet());

        MoteNumberWaypointPainter<Waypoint> moteNumberPainter = new MoteNumberWaypointPainter<>();
        moteNumberPainter.setWaypoints(motes);

        if (isRefresh) {
            mapViewer.setAddressLocation(centerPosition);
            mapViewer.setZoom(zoom);
        } else {
            mapViewer.setAddressLocation(environment.getMapCenter());
            mapViewer.setZoom(5);
        }

        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        painters.add(gateWayPainter);
        painters.add(motePainter);
        painters.add(moteNumberPainter);
        painters.add(gateWayNumberPainter);

        for (Mote mote : environment.getMotes()) {
            painters.add(new TrackPainter(mote.getPath()));
        }

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
        map.add(mapViewer);

        Double latitude = environment.getMapCenter().getLatitude();
        Integer latitudeDegrees = (int) Math.round(Math.floor(latitude));
        Integer latitudeMinutes = (int) Math.round(Math.floor((latitude - latitudeDegrees) * 60));
        Double latitudeSeconds = (double) Math.round(((latitude - latitudeDegrees) * 60 - latitudeMinutes) * 60 * 1000d) / 1000d;
        Double longitude = environment.getMapCenter().getLongitude();
        Integer longitudeDegrees = (int) Math.round(Math.floor(longitude));
        Integer longitudeMinutes = (int) Math.round(Math.floor((longitude - longitudeDegrees) * 60));
        Double longitudeSeconds = (double) Math.round(((longitude - longitudeDegrees) * 60 - longitudeMinutes) * 60 * 1000d) / 1000d;
        centerLabel.setText(" " + ((Math.signum(environment.getMapCenter().getLatitude()) == 1) ? "N " : "S ") +
                latitudeDegrees + "° " + latitudeMinutes + "' " + latitudeSeconds + "\" " + ", " +
                ((Math.signum(environment.getMapCenter().getLongitude()) == 1) ? "E " : "W ") +
                longitudeDegrees + "° " + longitudeMinutes + "' " + longitudeSeconds + "\" ");

    }

    public void refresh() {
        loadMap(simulation.getEnvironment(), mapViewer, true);
        updateEntries(simulation.getEnvironment());

    }

    public void refreshMap() {
        loadMap(simulation.getEnvironment(), mapViewer, true);
    }

    private MouseAdapter gateWayMouse = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            JTextArea jTextArea = (JTextArea) e.getSource();
            String text = jTextArea.getText();
            Integer index = Integer.parseInt(text.substring(8, text.indexOf(":")));
            if (e.getClickCount() == 2) {
                JFrame frame = new JFrame("Gateway settings");
                GatewayGUI gatewayGUI = new GatewayGUI(simulation.getEnvironment().getGateways().get(index - 1), frame);
                frame.setContentPane(gatewayGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(600, 400));
                frame.setMinimumSize(new Dimension(600, 400));
                frame.setVisible(true);
            }
            if (e.getClickCount() == 1) {
                /*

                // update received power graph
                receivedPowerGraph.removeAll();
                receivedPowerGraph.add(generateReceivedPowerGraphForGateways(environment.getGateways().get(index-1)));
                receivedPowerGraph.repaint();
                receivedPowerGraph.revalidate();
                // update power setting graph
                powerSettingGraph.removeAll();
                powerSettingGraph.add(generatePowerSettingGraph(environment.getGateways().get(index-1)));
                powerSettingGraph.repaint();
                powerSettingGraph.revalidate();
                // update spreading factor graph
                spreadingFactorGraph.removeAll();
                spreadingFactorGraph.add(generateSpreadingFactorGraph(environment.getGateways().get(index-1)));
                spreadingFactorGraph.repaint();
                spreadingFactorGraph.revalidate();
                // update used energy graph
                usedEnergyGraph.removeAll();
                usedEnergyGraph.add(generateUsedEnergyGraph(environment.getGateways().get(index-1)));
                usedEnergyGraph.repaint();
                usedEnergyGraph.revalidate();
                // load the messages
                */


            }

        }

    };

    private MouseAdapter moteMouse = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            JTextArea jTextArea = (JTextArea) e.getSource();
            String text = jTextArea.getText();
            Integer index = Integer.parseInt(text.substring(5, text.indexOf(":")));
            if (e.getClickCount() == 2) {
                JFrame frame = new JFrame("Mote settings");
                MoteGUI moteGUI = new MoteGUI(simulation.getEnvironment().getMotes().get(index - 1), frame);
                frame.setContentPane(moteGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(600, 400));
                frame.setMinimumSize(new Dimension(600, 400));
                frame.setVisible(true);
            }
            if (e.getClickCount() == 1) {
                setCharacteristics(index - 1, 0);
            }


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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        toolBarEnvironment = new JToolBar();
        toolBarEnvironment.setFloatable(false);
        toolBarEnvironment.setRollover(true);
        toolBarEnvironment.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        mainPanel.add(toolBarEnvironment, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Configuration:");
        toolBarEnvironment.add(label1);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator1);
        openButton = new JButton();
        openButton.setText("Open");
        toolBarEnvironment.add(openButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator2);
        environmentSaveButton = new JButton();
        environmentSaveButton.setText("Save");
        toolBarEnvironment.add(environmentSaveButton);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator3);
        configureButton = new JButton();
        configureButton.setText("Configure");
        toolBarEnvironment.add(configureButton);
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        toolBarEnvironment.add(spacer1);
        final JToolBar.Separator toolBar$Separator4 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator4);
        helpButton = new JButton();
        helpButton.setText("Help");
        toolBarEnvironment.add(helpButton);
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        toolBarEnvironment.add(spacer2);
        final JToolBar.Separator toolBar$Separator5 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator5);
        aboutButton = new JButton();
        aboutButton.setText("About");
        toolBarEnvironment.add(aboutButton);
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        toolBarEnvironment.add(spacer3);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(550);
        splitPane1.setOrientation(0);
        mainPanel.add(splitPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane1.setLeftComponent(splitPane2);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setLeftComponent(panel1);
        entitiesPane = new JScrollPane();
        entitiesPane.setHorizontalScrollBarPolicy(31);
        entitiesPane.setVerticalScrollBarPolicy(22);
        panel1.add(entitiesPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), new Dimension(250, -1), 0, false));
        entitesPanel = new JPanel();
        entitesPanel.setLayout(new GridBagLayout());
        entitiesPane.setViewportView(entitesPanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setRightComponent(panel2);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel2.add(toolBar1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Map");
        toolBar1.add(label2);
        final JToolBar.Separator toolBar$Separator6 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator6);
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        toolBar1.add(spacer4);
        final JLabel label3 = new JLabel();
        label3.setText("Center:");
        toolBar1.add(label3);
        centerLabel = new JLabel();
        centerLabel.setText("");
        toolBar1.add(centerLabel);
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        toolBar1.add(spacer5);
        map = new JPanel();
        map.setLayout(new BorderLayout(0, 0));
        map.setBackground(new Color(-4473925));
        map.setForeground(new Color(-12828863));
        panel2.add(map, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, 200), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        panel2.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 250), null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel3);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setDividerLocation(450);
        panel3.add(splitPane3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane3.setLeftComponent(panel4);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        console = new JPanel();
        console.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 5, 0, 0), -1, -1));
        console.setBackground(new Color(-4473925));
        console.setForeground(new Color(-12828863));
        panel5.add(console, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), new Dimension(-1, 400), 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(20);
        console.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(250, -1), null, null, 0, false));
        InputProfilePanel = new JPanel();
        InputProfilePanel.setLayout(new GridBagLayout());
        scrollPane1.setViewportView(InputProfilePanel);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(5, 5, 20, 0), -1, -1));
        panel5.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), null, new Dimension(-1, 150), 0, false));
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel6.add(toolBar2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Adaptation Goals:");
        toolBar2.add(label4);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel8.add(panel9, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(-1, 36), 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Reliable communication:");
        panel9.add(label5, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        panel9.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        relComlabel = new JLabel();
        relComlabel.setText("Interval: [-48,-42]");
        panel9.add(relComlabel, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editRelComButton = new JButton();
        editRelComButton.setText("Edit");
        panel9.add(editRelComButton, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("dB");
        panel9.add(label6, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel8.add(panel10, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(262, 36), new Dimension(-1, 36), 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Energy consumption:");
        panel10.add(label7, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enConLabel = new JLabel();
        enConLabel.setText("Threshold: 100");
        panel10.add(enConLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editEnConButton = new JButton();
        editEnConButton.setText("Edit");
        panel10.add(editEnConButton, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        panel10.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("mJ/min");
        panel10.add(label8, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel8.add(panel11, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(262, 36), new Dimension(-1, 36), 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Collision Bound: ");
        panel11.add(label9, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        colBoundLabel = new JLabel();
        colBoundLabel.setText("Threshold: 10");
        panel11.add(colBoundLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editColBoundButton = new JButton();
        editColBoundButton.setText("Edit");
        panel11.add(editColBoundButton, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer9 = new com.intellij.uiDesigner.core.Spacer();
        panel11.add(spacer9, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("%");
        panel11.add(label10, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar3 = new JToolBar();
        toolBar3.setFloatable(false);
        panel5.add(toolBar3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Input Profile");
        toolBar3.add(label11);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane3.setRightComponent(panel12);
        final JSplitPane splitPane4 = new JSplitPane();
        splitPane4.setDividerLocation(650);
        panel12.add(splitPane4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane4.setLeftComponent(panel13);
        tabbedPaneGraphs = new JTabbedPane();
        tabbedPaneGraphs.setBackground(new Color(-4473925));
        tabbedPaneGraphs.setForeground(new Color(-12828863));
        panel13.add(tabbedPaneGraphs, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(250, 400), null, 0, false));
        receivedPowerGraph = new JPanel();
        receivedPowerGraph.setLayout(new BorderLayout(0, 0));
        receivedPowerGraph.setBackground(new Color(-4473925));
        receivedPowerGraph.setForeground(new Color(-12828863));
        tabbedPaneGraphs.addTab("Received Power", receivedPowerGraph);
        powerSettingGraph = new JPanel();
        powerSettingGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Power Setting", powerSettingGraph);
        distanceToGatewayGraph = new JPanel();
        distanceToGatewayGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Distance To Gateway", distanceToGatewayGraph);
        spreadingFactorGraph = new JPanel();
        spreadingFactorGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Spreading Factor", spreadingFactorGraph);
        usedEnergyGraph = new JPanel();
        usedEnergyGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Used Energy", usedEnergyGraph);
        final JToolBar toolBar4 = new JToolBar();
        toolBar4.setFloatable(false);
        panel13.add(toolBar4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator7 = new JToolBar.Separator();
        toolBar4.add(toolBar$Separator7);
        moteCharacteristicsButton = new JButton();
        moteCharacteristicsButton.setText("Mote");
        toolBar4.add(moteCharacteristicsButton);
        final JToolBar.Separator toolBar$Separator8 = new JToolBar.Separator();
        toolBar4.add(toolBar$Separator8);
        final JLabel label12 = new JLabel();
        label12.setText("Selected: ");
        toolBar4.add(label12);
        moteCharacteristicsLabel = new JLabel();
        moteCharacteristicsLabel.setText("");
        toolBar4.add(moteCharacteristicsLabel);
        final JLabel label13 = new JLabel();
        label13.setText(" ");
        toolBar4.add(label13);
        resultsButton = new JButton();
        resultsButton.setText("Results");
        toolBar4.add(resultsButton);
        final com.intellij.uiDesigner.core.Spacer spacer10 = new com.intellij.uiDesigner.core.Spacer();
        toolBar4.add(spacer10);
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane4.setRightComponent(panel14);
        final JToolBar toolBar5 = new JToolBar();
        toolBar5.setFloatable(false);
        panel14.add(toolBar5, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator9 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator9);
        moteApplicationButton = new JButton();
        moteApplicationButton.setText("Mote");
        toolBar5.add(moteApplicationButton);
        final JToolBar.Separator toolBar$Separator10 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator10);
        regionButton = new JButton();
        regionButton.setText("Region");
        toolBar5.add(regionButton);
        final JToolBar.Separator toolBar$Separator11 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator11);
        final JLabel label14 = new JLabel();
        label14.setText("Selected: ");
        toolBar5.add(label14);
        moteApplicationLabel = new JLabel();
        moteApplicationLabel.setText("");
        toolBar5.add(moteApplicationLabel);
        final com.intellij.uiDesigner.core.Spacer spacer11 = new com.intellij.uiDesigner.core.Spacer();
        toolBar5.add(spacer11);
        tabbedPane1 = new JTabbedPane();
        panel14.add(tabbedPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        particulateMatterPanel = new JPanel();
        particulateMatterPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Particulate matter", particulateMatterPanel);
        carbonDioxideField = new JPanel();
        carbonDioxideField.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Carbon dioxide", carbonDioxideField);
        sootPanel = new JPanel();
        sootPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Soot", sootPanel);
        ozonePanel = new JPanel();
        ozonePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Ozone", ozonePanel);
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 0, 0));
        panel3.add(panel15, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toolBarAdaptation = new JToolBar();
        toolBarAdaptation.setBorderPainted(true);
        toolBarAdaptation.setFloatable(false);
        toolBarAdaptation.setRollover(true);
        toolBarAdaptation.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        panel15.add(toolBarAdaptation, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Simulation  ");
        toolBarAdaptation.add(label15);
        adaptationComboBox = new JComboBox();
        toolBarAdaptation.add(adaptationComboBox);
        final JToolBar.Separator toolBar$Separator12 = new JToolBar.Separator();
        toolBarAdaptation.add(toolBar$Separator12);
        singleRunButton = new JButton();
        singleRunButton.setText("Single Run");
        toolBarAdaptation.add(singleRunButton);
        final JLabel label16 = new JLabel();
        label16.setText("  ");
        toolBarAdaptation.add(label16);
        final JLabel label17 = new JLabel();
        label17.setText("Speed:");
        toolBarAdaptation.add(label17);
        speedSlider = new JSlider();
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMaximum(5);
        speedSlider.setMinimum(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintLabels(false);
        speedSlider.setPaintTicks(true);
        speedSlider.setValue(1);
        speedSlider.setValueIsAdjusting(false);
        toolBarAdaptation.add(speedSlider);
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        panel15.add(panel16, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, -1), new Dimension(350, -1), 0, false));
        final JToolBar toolBar6 = new JToolBar();
        toolBar6.setFloatable(false);
        panel16.add(toolBar6, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        final JToolBar.Separator toolBar$Separator13 = new JToolBar.Separator();
        toolBar6.add(toolBar$Separator13);
        totalRunButton = new JButton();
        totalRunButton.setText("Total Run");
        toolBar6.add(totalRunButton);
        final JLabel label18 = new JLabel();
        label18.setText("  ");
        toolBar6.add(label18);
        final JLabel label19 = new JLabel();
        label19.setText("Progress: ");
        toolBar6.add(label19);
        totalRunProgressBar = new JProgressBar();
        toolBar6.add(totalRunProgressBar);
        final JToolBar.Separator toolBar$Separator14 = new JToolBar.Separator();
        toolBar6.add(toolBar$Separator14);
        progressLabel = new JLabel();
        progressLabel.setText("0/0");
        toolBar6.add(progressLabel);
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        panel15.add(panel17, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JToolBar toolBar7 = new JToolBar();
        toolBar7.setFloatable(false);
        panel17.add(toolBar7, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        final JToolBar.Separator toolBar$Separator15 = new JToolBar.Separator();
        toolBar7.add(toolBar$Separator15);
        final JLabel label20 = new JLabel();
        label20.setText("Experimental results:  ");
        toolBar7.add(label20);
        simulationSaveButton = new JButton();
        simulationSaveButton.setText("Save");
        toolBar7.add(simulationSaveButton);
        final JLabel label21 = new JLabel();
        label21.setText("  ");
        toolBar7.add(label21);
        clearButton = new JButton();
        clearButton.setText("Clear");
        toolBar7.add(clearButton);
        final JToolBar.Separator toolBar$Separator16 = new JToolBar.Separator();
        toolBar7.add(toolBar$Separator16);
        final com.intellij.uiDesigner.core.Spacer spacer12 = new com.intellij.uiDesigner.core.Spacer();
        toolBar7.add(spacer12);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    class MoteSelectActionListener implements ActionListener {
        private MainGUI mainGui;

        public MoteSelectActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Select Mote");
            SelectMoteGUI selectMoteGUI = new SelectMoteGUI(simulation.getEnvironment(), mainGui, frame);
            frame.setContentPane(selectMoteGUI.getMainPanel());
            frame.setPreferredSize(new Dimension(750, 750));
            frame.setMinimumSize(new Dimension(750, 750));
            frame.setVisible(true);
        }
    }

    class ComRelActionListener implements ActionListener {
        private MainGUI mainGui;

        public ComRelActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Change reliable communication goal");
            EditRelComGUI editRelComGUI = new EditRelComGUI(((IntervalAdaptationGoal) QoS.getAdaptationGoal("reliableCommunication")), mainGui, frame);
            frame.setContentPane(editRelComGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class EnConActionListener implements ActionListener {
        private MainGUI mainGui;

        public EnConActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Change energy consumption goal");
            EditEnConGUI editEnConGUI = new EditEnConGUI((ThresholdAdaptationGoal) QoS.getAdaptationGoal("energyConsumption"), mainGui, frame);
            frame.setContentPane(editEnConGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class ColBoundActionListener implements ActionListener {
        private MainGUI mainGui;

        public ColBoundActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Change collision boundary goal");
            EditColBoundGUI editColBoundGUI = new EditColBoundGUI((ThresholdAdaptationGoal) QoS.getAdaptationGoal("collisionBound"), mainGui, frame);
            frame.setContentPane(editColBoundGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class MoteApplicationSelectActionListener implements ActionListener {
        private MainGUI mainGui;

        public MoteApplicationSelectActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Select Mote");
            SelectMoteApplicationGUI selectMoteApplicationGUI = new SelectMoteApplicationGUI(simulation.getEnvironment(), mainGui, frame);
            frame.setContentPane(selectMoteApplicationGUI.getMainPanel());
            frame.setPreferredSize(new Dimension(600, 400));
            frame.setMinimumSize(new Dimension(600, 400));
            frame.setVisible(true);

        }
    }

    private class InputProfileEditMouse extends MouseAdapter {

        private InputProfile inputProfile;

        public InputProfileEditMouse(InputProfile inputProfile) {
            this.inputProfile = inputProfile;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (simulation.getEnvironment() != null) {
                JFrame frame = new JFrame("Edit input profile");
                EditInputProfileGUI EditInputProfileGUI = new EditInputProfileGUI(inputProfile, simulation.getEnvironment());
                frame.setContentPane(EditInputProfileGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(750, 400));
                frame.setMinimumSize(new Dimension(750, 400));
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Load a configuration before editing an input profile", "InfoBox: " + "Edit InputProfile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class InputProfileSelectMouse extends MouseAdapter {

        private InputProfile inputProfile;

        public InputProfileSelectMouse(InputProfile inputProfile) {
            this.inputProfile = inputProfile;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (simulation.getEnvironment() != null) {
                if (inputProfile != simulation.getInputProfile()) {
                    simulation.setInputProfile(inputProfile);
                    if (inputProfile.getQualityOfServiceProfile().getNames().contains("reliableCommunication") &&
                            inputProfile.getQualityOfServiceProfile().getAdaptationGoal("reliableCommunication").getClass() == IntervalAdaptationGoal.class) {
                        QoS.putAdaptationGoal("reliableCommunication", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("reliableCommunication"));
                        editRelComButton.setEnabled(true);
                    } else {
                        editRelComButton.setEnabled(false);
                    }
                    if (inputProfile.getQualityOfServiceProfile().getNames().contains("energyConsumption") &&
                            inputProfile.getQualityOfServiceProfile().getAdaptationGoal("energyConsumption").getClass() == ThresholdAdaptationGoal.class) {
                        QoS.putAdaptationGoal("energyConsumption", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("energyConsumption"));
                        editEnConButton.setEnabled(true);
                    } else {
                        editEnConButton.setEnabled(false);
                    }
                    if (inputProfile.getQualityOfServiceProfile().getNames().contains("collisionBound") &&
                            inputProfile.getQualityOfServiceProfile().getAdaptationGoal("collisionBound").getClass() == ThresholdAdaptationGoal.class) {
                        QoS.putAdaptationGoal("collisionBound", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("collisionBound"));
                        editColBoundButton.setEnabled(true);
                    } else {
                        editColBoundButton.setEnabled(false);
                    }
                } else {
                    simulation.setInputProfile(null);
                    editRelComButton.setEnabled(false);
                    editColBoundButton.setEnabled(false);
                    editEnConButton.setEnabled(false);
                }
                updateInputProfile(inputProfiles);
                updateAdaptationGoals();

            } else {
                JOptionPane.showMessageDialog(null, "Load a configuration before selecting an input profile",
                        "InfoBox: " + "Select InputProfile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Sets the graphs of the corresponding characteristics of a given mote of a given run.
     *
     * @param moteIndex The index of the mote.
     * @param run       The number of the run.
     */
    public void setCharacteristics(Integer moteIndex, Integer run) {

        moteCharacteristicsLabel.setText("Mote " + (moteIndex + 1) + " | Run " + (run + 1));
        // update received power graph
        receivedPowerGraph.removeAll();
        Pair<ChartPanel, Pair<Integer, Integer>> powerData = generateReceivedPowerGraphForMotes(simulation.getEnvironment().getMotes().get(moteIndex), run);
        receivedPowerGraph.add(powerData.getLeft());
        receivedPowerGraph.repaint();
        receivedPowerGraph.revalidate();
        // update power setting graph
        powerSettingGraph.removeAll();
        powerSettingGraph.add(generatePowerSettingGraph(simulation.getEnvironment().getMotes().get(moteIndex), run));
        powerSettingGraph.repaint();
        powerSettingGraph.revalidate();
        // update spreading factor graph
        spreadingFactorGraph.removeAll();
        spreadingFactorGraph.add(generateSpreadingFactorGraph(simulation.getEnvironment().getMotes().get(moteIndex), run));
        spreadingFactorGraph.repaint();
        spreadingFactorGraph.revalidate();
        // update used energy graph
        usedEnergyGraph.removeAll();
        Pair<ChartPanel, Double> energyData = generateUsedEnergyGraph(simulation.getEnvironment().getMotes().get(moteIndex), run);
        usedEnergyGraph.add(energyData.getLeft());
        usedEnergyGraph.repaint();
        usedEnergyGraph.revalidate();
        // update distance to gateway graph
        distanceToGatewayGraph.removeAll();
        distanceToGatewayGraph.add(generateDistanceToGatewayGraph(simulation.getEnvironment().getMotes().get(moteIndex), run));
        distanceToGatewayGraph.repaint();
        distanceToGatewayGraph.revalidate();

        resultsButton.setEnabled(true);
        usedEnergy = energyData.getRight();
        packetsSent = powerData.getRight().getLeft();
        packetsLost = powerData.getRight().getRight();

    }

    public void setApplicationGraphs(int index) {
        moteApplicationLabel.setText("Mote " + (index + 1));
        // update particulate matter field
        particulateMatterPanel.removeAll();
        particulateMatterPanel.add(generateParticulateMatterGraph(simulation.getEnvironment().getMotes().get(index)));
        particulateMatterPanel.repaint();
        particulateMatterPanel.revalidate();
        // update carbon dioxide graph
        carbonDioxideField.removeAll();
        carbonDioxideField.add(generateCarbonDioxideGraph(simulation.getEnvironment().getMotes().get(index)));
        carbonDioxideField.repaint();
        carbonDioxideField.revalidate();
        // update soot graph
        sootPanel.removeAll();
        sootPanel.add(generateSootGraph(simulation.getEnvironment().getMotes().get(index)));
        sootPanel.repaint();
        sootPanel.revalidate();
        // update ozone graph
        ozonePanel.removeAll();
        ozonePanel.add(generateOzoneGraph(simulation.getEnvironment().getMotes().get(index)));
        ozonePanel.repaint();
        ozonePanel.revalidate();
    }

    public void setApplicationGraphs(Integer xBase, Integer yBase, Integer xSize, Integer ySize, Environment environment) {
        moteApplicationLabel.setText("Region 1");
        // update particulate matter field
        particulateMatterPanel.removeAll();
        Pair<JPanel, JComponent> particulateMatterGraph = generateParticulateMatterGraph(xBase, yBase, xSize, ySize, environment);

        particulateMatterPanel.add(particulateMatterGraph.getLeft());
        particulateMatterPanel.add(particulateMatterGraph.getRight(), BorderLayout.EAST);
        particulateMatterPanel.repaint();
        particulateMatterPanel.revalidate();
        // update carbon dioxide graph

        carbonDioxideField.removeAll();
        Pair<JPanel, JComponent> carbonDioxideGraph = generateCarbonDioxideGraph(xBase, yBase, xSize, ySize, environment);
        carbonDioxideField.add(carbonDioxideGraph.getLeft());
        carbonDioxideField.add(carbonDioxideGraph.getRight(), BorderLayout.EAST);
        carbonDioxideField.repaint();
        carbonDioxideField.revalidate();
        // update soot graph
        sootPanel.removeAll();
        Pair<JPanel, JComponent> sootGraph = generateSootGraph(xBase, yBase, xSize, ySize, environment);
        sootPanel.add(sootGraph.getLeft());
        sootPanel.add(sootGraph.getRight(), BorderLayout.EAST);
        sootPanel.repaint();
        sootPanel.revalidate();
        // update ozone graph
        ozonePanel.removeAll();
        Pair<JPanel, JComponent> ozoneGraph = generateOzoneGraph(xBase, yBase, xSize, ySize, environment);
        ozonePanel.add(ozoneGraph.getLeft());
        ozonePanel.add(ozoneGraph.getRight(), BorderLayout.EAST);
        ozonePanel.repaint();
        ozonePanel.revalidate();
    }

    private static Double particulateMatter(double x, double y) {
        Random random = new Random();
        if (x < 250 && y < 250)
            return (double) 97 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 750 && y < 750)
            return 90 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1250 && y < 1250)
            return 95 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 0.3 * random.nextGaussian();
        else
            return 85 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }

    private ChartPanel generateParticulateMatterGraph(Mote mote) {

        XYSeriesCollection dataParticulateMatter = new XYSeriesCollection();
        int i = 0;
        XYSeries seriesParticulateMatter = new XYSeries("Particulate Matter");
        if (mote.getSensors().contains(MoteSensor.PARTICULATE_MATTER)) {
            for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                seriesParticulateMatter.add(i * 10, particulateMatter(transmission.getXPos(), transmission.getYPos()));
                i = i + 1;
            }
        }
        dataParticulateMatter.addSeries(seriesParticulateMatter);

        JFreeChart ParticulateMatterChart = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance traveled in meter", // x axis label
                "Particulate Matter", // y axis label
                dataParticulateMatter, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) ParticulateMatterChart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);

        return new ChartPanel(ParticulateMatterChart);

    }

    /**
     * private JFreeChart createHeatChart(XYZDataset dataset, JPanel panel) {
     * <p>
     * ValueAxis xAxis = new NumberAxis("x-distance from grid zero in meter");
     * ValueAxis yAxis = new NumberAxis("y-distance from grid zero in meter");
     * <p>
     * // create a paint-scale and a legend showing it
     * SpectrumPaintScale paintScale = new SpectrumPaintScale(80, 100);
     * <p>
     * <p>
     * PaintScaleLegend psl = new PaintScaleLegend(paintScale, new NumberAxis());
     * psl.setPosition(RectangleEdge.RIGHT);
     * psl.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
     * psl.setMargin(50.0, 20.0, 80.0, 0.0);
     * <p>
     * // finally a renderer and a plot
     * XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYShapeRenderer());
     * ((XYShapeRenderer) plot.getRenderer()).setPaintScale(paintScale);
     * plot.getRenderer().setDefaultShape(new Ellipse2D.Double(0, 0, 20, 20));
     * plot.getDomainAxis().setRange(0, ((double) panel.getWidth()) / ((double) panel.getHeight()) * plot.getRangeAxis().getUpperBound());
     * <p>
     * <p>
     * JFreeChart chart = new JFreeChart(null, null, plot, false);
     * chart.addSubtitle(psl);
     * chart.getXYPlot().setDomainGridlinesVisible(false);
     * chart.getXYPlot().setRangeGridlinesVisible(false);
     * Color trans = new Color(0xFF, 0xFF, 0xFF, 0);
     * chart.getPlot().setBackgroundPaint(trans);
     * <p>
     * BufferedImage img = null;
     * try {
     * <p>
     * img = ImageIO.read(getClass().getResource("/GUI/map.png"));
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * Image img2 = img.getScaledInstance((int) Math.round(img.getWidth() * 1.25), (int) Math.round(img.getHeight() * 1.25), Image.SCALE_DEFAULT);
     * BufferedImage img3 = new BufferedImage((int) Math.round(img.getWidth() * 1.25), (int) Math.round(img.getHeight() * 1.25), BufferedImage.TYPE_INT_RGB);
     * Graphics bg = img3.getGraphics();
     * bg.drawImage(img2, 0, 0, null);
     * bg.dispose();
     * img3 = img3.getSubimage(30, 100, img.getWidth(), img.getHeight());
     * <p>
     * chart.setBackgroundImage(img3);
     * return chart;
     * <p>
     * }
     **/

    private Pair<JPanel, JComponent> createHeatChart(LinkedList<Pair<GeoPosition, Double>> dataSet, Environment environment) {

        // create a paint-scale and a legend showing it
        SpectrumPaintScale paintScale = new SpectrumPaintScale(80, 100);

        PaintScaleLegend psl = new PaintScaleLegend(paintScale, new NumberAxis());
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        psl.setMargin(50.0, 20.0, 80.0, 0.0);

        XYPlot plot = new XYPlot();
        JFreeChart chart = new JFreeChart(null, null, plot, false);
        chart.addSubtitle(psl);
        ImageIcon icon = new ImageIcon(chart.createBufferedImage(400, 300));
        Image image = icon.getImage();
        image = createImage(new FilteredImageSource(image.getSource(),
                new CropImageFilter(350, 40, 50, 220)));
        ImageIcon newIcon = new ImageIcon(image);
        JLabel jLabel = new JLabel(newIcon);
        jLabel.setMinimumSize(new Dimension(0, 0));

        // finally a renderer and a plot

        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setAddressLocation(environment.getMapCenter());
        mapViewer.setZoom(5);
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
        for (Pair<GeoPosition, Double> data : dataSet) {
            compoundPainter.addPainter(new SensorDataPainter(data.getLeft(), paintScale.getPaint(data.getRight())));
        }
        mapViewer.setOverlayPainter(compoundPainter);


        return new Pair<>(mapViewer, jLabel);

    }

    private Pair<JPanel, JComponent> generateParticulateMatterGraph(Integer xBase, Integer yBase, Integer xSize, Integer ySize, Environment environment) {

        DefaultXYZDataset dataParticulateMatter = new DefaultXYZDataset();
        HashMap<Pair<Integer, Integer>, LinkedList<Double>> seriesParticulateMatterList = new HashMap<>();
        LinkedList<Pair<GeoPosition, Double>> dataSet = new LinkedList<>();
        for (Mote mote : simulation.getEnvironment().getMotes()) {
            if (mote.getSensors().contains(MoteSensor.PARTICULATE_MATTER)) {
                for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                    Integer xPos = transmission.getXPos();
                    Integer yPos = transmission.getYPos();
                    for (Pair<Integer, Integer> key : seriesParticulateMatterList.keySet()) {
                        if (Math.sqrt(Math.pow(key.getLeft() - xPos, 2) + Math.pow(key.getRight() - yPos, 2)) < 300) {
                            xPos = key.getLeft();
                            yPos = key.getRight();
                        }
                    }
                    if (seriesParticulateMatterList.keySet().contains(new Pair<>(xPos, yPos))) {
                        seriesParticulateMatterList.get(new Pair<>(xPos, yPos)).add(particulateMatter(xPos, yPos));
                    } else {
                        LinkedList<Double> dataList = new LinkedList<>();
                        dataList.add(particulateMatter(xPos, yPos));
                        seriesParticulateMatterList.put(new Pair<>(xPos, yPos), dataList);
                    }
                }
            }
        }
        for (Pair<Integer, Integer> key : seriesParticulateMatterList.keySet()) {
            Double average = 0.0;
            Integer amount = 0;
            for (Double value : seriesParticulateMatterList.get(key)) {
                average = average + value;
                amount++;
            }
            average = average / amount;
            seriesParticulateMatterList.get(key).clear();
            seriesParticulateMatterList.get(key).add(average);
            dataSet.add(new Pair<>(new GeoPosition(environment.toLatitude(key.getRight()), environment.toLongitude(key.getLeft())), average));
        }

        double[][] seriesParticulateMatter = new double[3][seriesParticulateMatterList.size()];
        int i = 0;
        for (Pair<Integer, Integer> key : seriesParticulateMatterList.keySet()) {
            seriesParticulateMatter[0][i] = key.getLeft();
            seriesParticulateMatter[1][i] = key.getRight();
            seriesParticulateMatter[2][i] = seriesParticulateMatterList.get(key).get(0);
            i++;
        }
        dataParticulateMatter.addSeries("Particulate Matter", seriesParticulateMatter);


        return createHeatChart(dataSet, environment);

    }

    private static Double carbonDioxide(double x, double y) {
        Random random = new Random();
        if (x < 200 && y < 230)
            return (double) 97 - 20 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1000 && y < 1000)
            return 90 - 20 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1400 && y < 1400)
            return 95 - 20 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 0.3 * random.nextGaussian();
        else
            return 85 - 17.5 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }

    private ChartPanel generateCarbonDioxideGraph(Mote mote) {
        XYSeriesCollection dataCarbonDioxide = new XYSeriesCollection();
        int i = 0;

        XYSeries seriesCarbonDioxide = new XYSeries("Carbon Dioxide");
        if (mote.getSensors().contains(MoteSensor.CARBON_DIOXIDE)) {
            for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                seriesCarbonDioxide.add(i * 10, carbonDioxide(transmission.getXPos(), transmission.getYPos()));
                i = i + 1;
            }
        }
        dataCarbonDioxide.addSeries(seriesCarbonDioxide);

        JFreeChart spreadingFactorChartMote = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance traveled in meter", // x axis label
                "Carbon Dioxide", // y axis label
                dataCarbonDioxide, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) spreadingFactorChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);
        return new ChartPanel(spreadingFactorChartMote);
    }

    private Pair<JPanel, JComponent> generateCarbonDioxideGraph(Integer xBase, Integer yBase, Integer xSize, Integer ySize, Environment environment) {

        DefaultXYZDataset dataCarbonDioxide = new DefaultXYZDataset();
        HashMap<Pair<Integer, Integer>, LinkedList<Double>> seriesCarbonDioxideList = new HashMap<>();
        LinkedList<Pair<GeoPosition, Double>> dataSet = new LinkedList<>();

        for (Mote mote : simulation.getEnvironment().getMotes()) {
            if (mote.getSensors().contains(MoteSensor.CARBON_DIOXIDE)) {
                for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                    Integer xPos = transmission.getXPos();
                    Integer yPos = transmission.getYPos();
                    for (Pair<Integer, Integer> key : seriesCarbonDioxideList.keySet()) {
                        if (Math.sqrt(Math.pow(key.getLeft() - xPos, 2) + Math.pow(key.getRight() - yPos, 2)) < 300) {
                            xPos = key.getLeft();
                            yPos = key.getRight();
                        }
                    }
                    if (seriesCarbonDioxideList.keySet().contains(new Pair<>(xPos, yPos))) {
                        seriesCarbonDioxideList.get(new Pair<>(xPos, yPos)).add(carbonDioxide(xPos, yPos));
                    } else {
                        LinkedList<Double> dataList = new LinkedList<>();
                        dataList.add(carbonDioxide(xPos, yPos));
                        seriesCarbonDioxideList.put(new Pair<>(xPos, yPos), dataList);
                    }
                }
            }
        }
        for (Pair<Integer, Integer> key : seriesCarbonDioxideList.keySet()) {
            Double average = 0.0;
            Integer amount = 0;
            for (Double value : seriesCarbonDioxideList.get(key)) {
                average = average + value;
                amount++;
            }
            average = average / amount;
            seriesCarbonDioxideList.get(key).clear();
            seriesCarbonDioxideList.get(key).add(average);
            dataSet.add(new Pair<>(new GeoPosition(environment.toLatitude(key.getRight()), environment.toLongitude(key.getLeft())), average));
        }

        double[][] seriesCarbonDioxide = new double[3][seriesCarbonDioxideList.size()];
        int i = 0;
        for (Pair<Integer, Integer> key : seriesCarbonDioxideList.keySet()) {
            seriesCarbonDioxide[0][i] = key.getLeft();
            seriesCarbonDioxide[1][i] = key.getRight();
            seriesCarbonDioxide[2][i] = seriesCarbonDioxideList.get(key).get(0);
            i++;
        }
        dataCarbonDioxide.addSeries("Carbon Dioxide", seriesCarbonDioxide);
        return createHeatChart(dataSet, environment);

    }

    private static Double soot(double x, double y) {
        Random random = new Random();
        if (x < 210 && y < 230)
            return (double) 97 - 10 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1100 && y < 1100)
            return 98 - 10 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1400 && y < 1700)
            return 95 - 4 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 0.3 * random.nextGaussian();
        else
            return 85 - 2 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }

    private ChartPanel generateSootGraph(Mote mote) {
        XYSeriesCollection dataSoot = new XYSeriesCollection();
        int i = 0;
        XYSeries seriesSoot = new XYSeries("Soot");
        if (mote.getSensors().contains(MoteSensor.SOOT)) {
            for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                seriesSoot.add(i * 10, soot(transmission.getXPos(), transmission.getYPos()));
                i = i + 1;
            }
            dataSoot.addSeries(seriesSoot);
        }

        JFreeChart sootChart = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance traveled in meter", // x axis label
                "Soot", // y axis label
                dataSoot, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) sootChart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);
        return new ChartPanel(sootChart);
    }

    private Pair<JPanel, JComponent> generateSootGraph(Integer xBase, Integer yBase, Integer xSize, Integer ySize, Environment environment) {

        DefaultXYZDataset dataSoot = new DefaultXYZDataset();
        HashMap<Pair<Integer, Integer>, LinkedList<Double>> seriesSootList = new HashMap<>();
        LinkedList<Pair<GeoPosition, Double>> dataSet = new LinkedList<>();

        for (Mote mote : simulation.getEnvironment().getMotes()) {
            if (mote.getSensors().contains(MoteSensor.SOOT)) {
                for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                    Integer xPos = transmission.getXPos();
                    Integer yPos = transmission.getYPos();
                    for (Pair<Integer, Integer> key : seriesSootList.keySet()) {
                        if (Math.sqrt(Math.pow(key.getLeft() - xPos, 2) + Math.pow(key.getRight() - yPos, 2)) < 300) {
                            xPos = key.getLeft();
                            yPos = key.getRight();
                        }
                    }
                    if (seriesSootList.keySet().contains(new Pair<>(xPos, yPos))) {
                        seriesSootList.get(new Pair<>(xPos, yPos)).add(soot(xPos, yPos));
                    } else {
                        LinkedList<Double> dataList = new LinkedList<>();
                        dataList.add(soot(xPos, yPos));
                        seriesSootList.put(new Pair<>(xPos, yPos), dataList);
                    }
                }
            }
        }
        for (Pair<Integer, Integer> key : seriesSootList.keySet()) {
            Double average = 0.0;
            Integer amount = 0;
            for (Double value : seriesSootList.get(key)) {
                average = average + value;
                amount++;
            }
            average = average / amount;
            seriesSootList.get(key).clear();
            seriesSootList.get(key).add(average);
            dataSet.add(new Pair<>(new GeoPosition(environment.toLatitude(key.getRight()), environment.toLongitude(key.getLeft())), average));
        }

        double[][] seriesSoot = new double[3][seriesSootList.size()];
        int i = 0;
        for (Pair<Integer, Integer> key : seriesSootList.keySet()) {
            seriesSoot[0][i] = key.getLeft();
            seriesSoot[1][i] = key.getRight();
            seriesSoot[2][i] = seriesSootList.get(key).get(0);
            i++;
        }
        dataSoot.addSeries("Soot", seriesSoot);
        return createHeatChart(dataSet, environment);

    }

    private static Double ozone(double x, double y) {
        Random random = new Random();
        if (x < 200 && y < 200)
            return (double) 97 - 30 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1000 && y < 1000)
            return 98 - 30 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1200 && y < 1200)
            return 95 - 24.5 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 0.3 * random.nextGaussian();
        else
            return 85 - 24 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }

    private ChartPanel generateOzoneGraph(Mote mote) {
        XYSeriesCollection dataOzone = new XYSeriesCollection();
        int i = 0;
        XYSeries seriesOzone = new XYSeries("Ozone");
        if (mote.getSensors().contains(MoteSensor.OZONE)) {
            for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                seriesOzone.add(i * 10, ozone(transmission.getXPos(), transmission.getYPos()));
                i = i + 1;
            }
        }
        dataOzone.addSeries(seriesOzone);

        JFreeChart ozoneChart = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance traveled in meter", // x axis label
                "Ozone", // y axis label
                dataOzone, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) ozoneChart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);
        return new ChartPanel(ozoneChart);
    }

    private Pair<JPanel, JComponent> generateOzoneGraph(Integer xBase, Integer yBase, Integer xSize, Integer ySize, Environment environment) {

        DefaultXYZDataset dataOzone = new DefaultXYZDataset();
        HashMap<Pair<Integer, Integer>, LinkedList<Double>> seriesOzoneList = new HashMap<>();
        LinkedList<Pair<GeoPosition, Double>> dataSet = new LinkedList<>();

        for (Mote mote : simulation.getEnvironment().getMotes()) {
            if (mote.getSensors().contains(MoteSensor.OZONE)) {
                for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                    Integer xPos = transmission.getXPos();
                    Integer yPos = transmission.getYPos();
                    for (Pair<Integer, Integer> key : seriesOzoneList.keySet()) {
                        if (Math.sqrt(Math.pow(key.getLeft() - xPos, 2) + Math.pow(key.getRight() - yPos, 2)) < 300) {
                            xPos = key.getLeft();
                            yPos = key.getRight();
                        }
                    }
                    if (seriesOzoneList.keySet().contains(new Pair<>(xPos, yPos))) {
                        seriesOzoneList.get(new Pair<>(xPos, yPos)).add(ozone(xPos, yPos));
                    } else {
                        LinkedList<Double> dataList = new LinkedList<>();
                        dataList.add(ozone(xPos, yPos));
                        seriesOzoneList.put(new Pair<>(xPos, yPos), dataList);
                    }
                }
            }
        }
        for (Pair<Integer, Integer> key : seriesOzoneList.keySet()) {
            Double average = 0.0;
            Integer amount = 0;
            for (Double value : seriesOzoneList.get(key)) {
                average = average + value;
                amount++;
            }
            average = average / amount;
            seriesOzoneList.get(key).clear();
            seriesOzoneList.get(key).add(average);
            dataSet.add(new Pair<>(new GeoPosition(environment.toLatitude(key.getRight()), environment.toLongitude(key.getLeft())), average));
        }

        double[][] seriesOzone = new double[3][seriesOzoneList.size()];
        int i = 0;
        for (Pair<Integer, Integer> key : seriesOzoneList.keySet()) {
            seriesOzone[0][i] = key.getLeft();
            seriesOzone[1][i] = key.getRight();
            seriesOzone[2][i] = seriesOzoneList.get(key).get(0);
            i++;
        }
        dataOzone.addSeries("Ozone", seriesOzone);
        return createHeatChart(dataSet, environment);

    }

    private class ConfigureActionListener implements ActionListener {
        private MainGUI gui;

        ConfigureActionListener(MainGUI gui) {
            this.gui = gui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Configure configuration");
            ConfigureGUI configureGUI = new ConfigureGUI(simulation.getEnvironment(), gui, frame);
            frame.setContentPane(configureGUI.getMainPanel());
            frame.setPreferredSize(new Dimension(700, 600));
            frame.setMinimumSize(new Dimension(700, 600));
            frame.setVisible(true);

        }

    }

    public void setProgress(Integer index, Integer total) {
        totalRunProgressBar.setMinimum(0);
        totalRunProgressBar.setMaximum(total);
        totalRunProgressBar.setValue(index);
        progressLabel.setText(index + "/" + total);
    }

    /**
     * Generates a received power graph for a specific mote for a specific run, the amount of packets sent and the amount lost.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair containing ChartPanel containing a received power graph and another pair containing 2 integers: the amount of packets sent and the amount lost.
     */
    public static Pair<ChartPanel, Pair<Integer, Integer>> generateReceivedPowerGraphForMotes(Mote mote, Integer run) {
        LinkedList<LinkedList<Pair<NetworkEntity, Pair<Integer, Double>>>> transmissionsMote = new LinkedList<>();

        Integer amountSent = 0;
        Integer amountLost = 0;

        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : gateway.getAllReceivedTransmissions(run).keySet()) {
                if (transmission.getSender() == mote) {
                    amountSent += 1;
                    if (!gateway.getAllReceivedTransmissions(run).get(transmission))
                        transmissionsMote.getLast().add(new Pair<>(transmission.getReceiver(), new Pair<>(transmission.getDepartureTime().toSecondOfDay(), transmission.getTransmissionPower())));
                    else {
                        transmissionsMote.getLast().add(new Pair<>(transmission.getReceiver(), new Pair<>(transmission.getDepartureTime().toSecondOfDay(), (double) 20)));
                        amountLost += 1;
                    }
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataReceivedPowerMote = new XYSeriesCollection();


        for (LinkedList<Pair<NetworkEntity, Pair<Integer, Double>>> list : transmissionsMote) {
            XYSeries series = new XYSeries("gateway " + (mote.getEnvironment().getGateways().indexOf(list.get(0).getLeft()) + 1));

            for (Pair<NetworkEntity, Pair<Integer, Double>> data : list) {
                series.add(data.getRight().getLeft(), data.getRight().getRight());

            }
            dataReceivedPowerMote.addSeries(series);
        }


        JFreeChart receivedPowerChartMote = ChartFactory.createScatterPlot(
                null, // chart title
                "Seconds", // x axis label
                "Received signal strength in dBm", // y axis label
                dataReceivedPowerMote, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) receivedPowerChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        for (int i = 0; i < dataReceivedPowerMote.getSeriesCount(); i++) {
            renderer.setSeriesShape(i, shape);
        }
        return new Pair<>(new ChartPanel(receivedPowerChartMote), new Pair<>(amountSent, amountLost));

    }

    /**
     * Generates a spreading factor graph for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a spreading factor graph.
     */
    public static ChartPanel generateSpreadingFactorGraph(NetworkEntity mote, Integer run) {
        XYSeriesCollection dataSpreadingFactorMote = new XYSeriesCollection();
        XYSeries seriesSpreadingFactorMote = new XYSeries("Spreading factor");
        int i = 0;
        for (Integer spreadingFactor : mote.getSpreadingFactorHistory(run)) {
            seriesSpreadingFactorMote.add(i, spreadingFactor);
            i = i + 1;
        }
        dataSpreadingFactorMote.addSeries(seriesSpreadingFactorMote);

        JFreeChart spreadingFactorChartMote = ChartFactory.createScatterPlot(
                null, // chart title
                "Transmissions", // x axis label
                "Spreading factor", // y axis label
                dataSpreadingFactorMote, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) spreadingFactorChartMote.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 15.0);
        range.setTickUnit(new NumberTickUnit(1.0));
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (Integer series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);

        return new ChartPanel(spreadingFactorChartMote);

    }

    /**
     * Generates a used energy graph and the total used energy for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair withChartPanel containing a used energy graph and a double the total ued energy.
     */
    public static Pair<ChartPanel, Double> generateUsedEnergyGraph(NetworkEntity mote, Integer run) {
        XYSeriesCollection dataUsedEnergyEntity = new XYSeriesCollection();
        int i = 0;
        XYSeries seriesUsedEnergyEntity = new XYSeries("Used energy");
        Double totalEnergy = 0.0;
        for (Double usedEnergy : mote.getUsedEnergy(run)) {
            totalEnergy += usedEnergy;
            seriesUsedEnergyEntity.add(i, usedEnergy);
            i = i + 1;
        }
        dataUsedEnergyEntity.addSeries(seriesUsedEnergyEntity);
        JFreeChart usedEnergyChartEntity = ChartFactory.createXYLineChart(
                null, // chart title
                "Transmissions", // x axis label
                "Used energy in mJoule", // y axis label
                dataUsedEnergyEntity, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) usedEnergyChartEntity.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (Integer series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);

        return new Pair<>(new ChartPanel(usedEnergyChartEntity), totalEnergy);

    }

    /**
     * Generates a distance to gateway graph for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a distance to gateway graph.
     */
    public static ChartPanel generateDistanceToGatewayGraph(Mote mote, Integer run) {
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote = new LinkedList<>();

        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : gateway.getAllReceivedTransmissions(run).keySet()) {
                if (transmission.getSender() == mote) {
                    transmissionsMote.getLast().add(transmission);
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataDistanceToGateway = new XYSeriesCollection();

        for (LinkedList<LoraTransmission> list : transmissionsMote) {
            XYSeries series = new XYSeries("gateway " + (mote.getEnvironment().getGateways().indexOf(list.get(0).getReceiver()) + 1));
            Integer i = 0;
            for (LoraTransmission transmission : list) {
                series.add(i, (Number) Math.sqrt(Math.pow(transmission.getReceiver().getYPos() - transmission.getYPos(), 2) +
                        Math.pow(transmission.getReceiver().getXPos() - transmission.getXPos(), 2)));
                i = i + 1;
            }
            dataDistanceToGateway.addSeries(series);
        }

        JFreeChart DistanceToGatewayChartMote = ChartFactory.createXYLineChart(
                null, // chart title
                "Transmissions", // x axis label
                "Distance to the gateway in  m", // y axis label
                dataDistanceToGateway, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) DistanceToGatewayChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (Integer series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);
        return new ChartPanel(DistanceToGatewayChartMote);

    }

    /**
     * Generates a power setting graph for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a power setting graph.
     */
    public static ChartPanel generatePowerSettingGraph(NetworkEntity mote, Integer run) {
        XYSeriesCollection dataPowerSettingMote = new XYSeriesCollection();
        XYSeries seriesPowerSettingMote = new XYSeries("Power setting");
        for (Pair<Integer, Integer> powerSetting : mote.getPowerSettingHistory(run)) {
            seriesPowerSettingMote.add(powerSetting.getLeft(), powerSetting.getRight());
        }
        dataPowerSettingMote.addSeries(seriesPowerSettingMote);

        JFreeChart powerSettingChartMote = ChartFactory.createXYLineChart(
                null, // chart title
                "Seconds", // x axis label
                "Power setting in dBm", // y axis label
                dataPowerSettingMote, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        XYPlot plot = (XYPlot) powerSettingChartMote.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 15.0);
        range.setTickUnit(new NumberTickUnit(1.0));
        return new ChartPanel(powerSettingChartMote);

    }

    public void setRelCom(IntervalAdaptationGoal intervalAdaptationGoal) {
        simulation.getInputProfile().putAdaptationGoal("reliableCommunication", intervalAdaptationGoal);
        if (simulation.getInputProfile().getQualityOfServiceProfile().getAdaptationGoal("reliableCommunication").getClass() == IntervalAdaptationGoal.class) {
            QoS.putAdaptationGoal("reliableCommunication", simulation.getInputProfile().getQualityOfServiceProfile().getAdaptationGoal("reliableCommunication"));
        }
        updateAdaptationGoals();
    }

    public void setEnCon(ThresholdAdaptationGoal thresholdAdaptationGoal) {
        simulation.getInputProfile().putAdaptationGoal("energyConsumption", thresholdAdaptationGoal);
        if (simulation.getInputProfile().getQualityOfServiceProfile().getAdaptationGoal("energyConsumption").getClass() == ThresholdAdaptationGoal.class) {
            QoS.putAdaptationGoal("energyConsumption", simulation.getInputProfile().getQualityOfServiceProfile().getAdaptationGoal("energyConsumption"));
        }
        updateAdaptationGoals();
    }

    public void setColBound(ThresholdAdaptationGoal thresholdAdaptationGoal) {
        simulation.getInputProfile().putAdaptationGoal("collisionBound", thresholdAdaptationGoal);
        if (simulation.getInputProfile().getQualityOfServiceProfile().getAdaptationGoal("collisionBound").getClass() == ThresholdAdaptationGoal.class) {
            QoS.putAdaptationGoal("collisionBound", simulation.getInputProfile().getQualityOfServiceProfile().getAdaptationGoal("collisionBound"));
        }
        updateAdaptationGoals();
    }

}

