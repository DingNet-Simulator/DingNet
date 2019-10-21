package IotDomain;

import GUI.MainGUI;
import IotDomain.InputProfile;
import IotDomain.QualityOfService;
import IotDomain.Simulation;
import SelfAdaptation.AdaptationGoals.AdaptationGoal;
import SelfAdaptation.AdaptationGoals.IntervalAdaptationGoal;
import SelfAdaptation.AdaptationGoals.ThresholdAdaptationGoal;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import SelfAdaptation.FeedbackLoop.ReliableEfficientDistanceGateway;
import SelfAdaptation.FeedbackLoop.SignalBasedAdaptation;
import SelfAdaptation.Instrumentation.MoteEffector;
import SelfAdaptation.Instrumentation.MoteProbe;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import util.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

public class SimulationRunner {
    private static SimulationRunner instance = null;

    private List<InputProfile> inputProfiles;
    private Simulation simulation;
    private List<GenericFeedbackLoop> algorithms;
    private LinkedList<MoteProbe> moteProbe;
    private LinkedList<MoteEffector> moteEffector;
    private QualityOfService QoS;


    public static SimulationRunner getInstance() {
        if (instance == null) {
            instance = new SimulationRunner();
        }

        return instance;
    }

    private SimulationRunner() {
        QoS = new QualityOfService(new HashMap<>());
        QoS.putAdaptationGoal("reliableCommunication", new IntervalAdaptationGoal(0.0, 0.0));
        QoS.putAdaptationGoal("energyConsumption", new ThresholdAdaptationGoal(0.0));
        QoS.putAdaptationGoal("collisionBound", new ThresholdAdaptationGoal(0.0));

        simulation = new Simulation();
        inputProfiles = loadInputProfiles();


        /**
         * Loading all the algorithms
         */
        GenericFeedbackLoop noAdaptation = new GenericFeedbackLoop("No Adaptation") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {

            }
        };

        algorithms = new ArrayList<>();
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
    }


    public Environment getEnvironment() {
        return simulation.getEnvironment();
    }

    public List<InputProfile> getInputProfiles() {
        return inputProfiles;
    }

    public List<GenericFeedbackLoop> getAlgorithms() {
        return algorithms;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public QualityOfService getQoS() {
        return QoS;
    }


    public void setApproach(String name) {
        GenericFeedbackLoop selectedAlgorithm = null;
        for (GenericFeedbackLoop algorithm : algorithms) {
            if (algorithm.getName().equals(name)) {
                selectedAlgorithm = algorithm;
            }
        }

        if (selectedAlgorithm == null) {
            throw new RuntimeException(String.format("Could not load approach with name %s", name));
        }
        simulation.setApproach(selectedAlgorithm);
    }




    public Simulation.SimulationResult singleRun() {
        return simulation.singleRun();
    }


    public void totalRun() {
        totalRun(null);
    }

    public void totalRun(Function<Pair<Integer, Integer>, Void> fn) {
        simulation.multipleRuns(fn);
    }




    void updateInputProfilesFile() {
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
        } catch (URISyntaxException | ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }

    }


    private List<InputProfile> loadInputProfiles() {
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

                inputProfiles.add(new InputProfile(
                    name,
                    new QualityOfService(adaptationGoalHashMap),
                    numberOfRuns,
                    moteProbabilaties,
                    new HashMap<>(),
                    new HashMap<>(),
                    inputProfileElement)
                );
            }
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return inputProfiles;
    }










    // TODO move xml saving and loading to separate utility classes
    public void loadConfigurationFromFile(File file) {
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
            int numberOfZones = Integer.valueOf(((Element) characteristics.getElementsByTagName("regionProperty").item(0)).getAttribute("numberOfZones"));

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

            simulation.setEnvironment(new Environment(characteristicsMap, mapOrigin, wayPointsSet, numberOfZones));

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
                for (int j = 0; j < sensors.getElementsByTagName("sensor").getLength(); j++) {
                    Element sensornode = (Element) sensors.getElementsByTagName("sensor").item(j);
                    moteSensors.add(MoteSensor.valueOf(sensornode.getAttribute("SensorType")));
                }

                Element pathElement = (Element) moteNode.getElementsByTagName("path").item(0);
                Element waypoint;
                LinkedList<GeoPosition> path = new LinkedList<>();
                for (int j = 0; j < pathElement.getElementsByTagName("wayPoint").getLength(); j++) {
                    waypoint = (Element) pathElement.getElementsByTagName("wayPoint").item(j);
                    int wayPointX = Integer.parseInt(waypoint.getTextContent().split(",")[0]);
                    int wayPointY = Integer.parseInt(waypoint.getTextContent().split(",")[1]);
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


        } catch (ParserConfigurationException | SAXException | IOException e1) {
            e1.printStackTrace();
        }


        for (Gateway gateway : simulation.getEnvironment().getGateways()) {
            for (int i = 0; i < algorithms.size(); i++) {
                gateway.addSubscription(moteProbe.get(i));
            }
        }
    }


    public void saveConfigurationToFile(File file) {
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
            width.appendChild(doc.createTextNode(Integer.toString(simulation.getEnvironment().getMaxXpos() + 1)));
            Element height = doc.createElement("height");
            height.appendChild(doc.createTextNode(Integer.toString(simulation.getEnvironment().getMaxYpos() + 1)));
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
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void saveSimulationToFile(File file) {
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
