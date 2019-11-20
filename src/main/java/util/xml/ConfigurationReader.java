package util.xml;

import datagenerator.GPSDataGenerator;
import iot.Characteristic;
import iot.Environment;
import iot.Simulation;
import iot.networkentity.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import util.Connection;
import util.MapHelper;
import util.Pair;
import util.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigurationReader {

    private static Map<Long, Long> IDMappingWayPoints = new HashMap<>();
    private static Map<Long, Long> IDMappingConnections = new HashMap<>();

    private static Map<Long, GeoPosition> wayPoints = new HashMap<>();
    private static Map<Long, Connection> connections = new HashMap<>();

    public static void loadConfiguration(File file, Simulation simulation) {
        IDMappingWayPoints.clear();
        IDMappingConnections.clear();
        wayPoints.clear();
        connections.clear();

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element configuration = doc.getDocumentElement();


            // ---------------
            //      Map
            // ---------------

            Element map = (Element) configuration.getElementsByTagName("map").item(0);
            Element region = (Element) map.getElementsByTagName("region").item(0);
            int width = Integer.parseInt(XMLHelper.readChild(region, "width"));
            int height = Integer.parseInt(XMLHelper.readChild(region, "height"));

            Element origin = (Element) region.getElementsByTagName("origin").item(0);
            GeoPosition mapOrigin = new GeoPosition(
                Double.parseDouble(XMLHelper.readChild(origin, "latitude")),
                Double.parseDouble(XMLHelper.readChild(origin, "longitude"))
            );


            // ---------------
            // Characteristics
            // ---------------

            Element characteristics = (Element) configuration.getElementsByTagName("characteristics").item(0);
            int numberOfZones = Integer.parseInt(((Element) characteristics.getElementsByTagName("regionProperty").item(0)).getAttribute("numberOfZones"));
            long n = Math.round(Math.sqrt(numberOfZones));
            Characteristic[][] characteristicsMap = new Characteristic[width][height];

            for (int i = 0; i < n; i++) {
                String[] characteristicsRow = characteristics.getElementsByTagName("row").item(i).getTextContent().split("-");
                for (int j = 0; j < characteristicsRow.length; j++) {
                    Characteristic characteristic = Characteristic.valueOf(characteristicsRow[j]);

                    double widthSize = ((double) width) / n;
                    double heightSize = ((double) height) / n;
                    for (int x = (int) Math.round(j * widthSize); x < (int) Math.round((j + 1) * widthSize); x++) {
                        for (int y = (int) Math.round(i * heightSize); y < (int) Math.round((i + 1) * heightSize); y++) {
                            characteristicsMap[x][y] = characteristic;
                        }
                    }
                }

            }



            // ---------------
            //    WayPoints
            // ---------------

            Element wayPointsElement = (Element) configuration.getElementsByTagName("wayPoints").item(0);

            // Remapping of the waypoint IDs to start from 1
            long currentIDWayPoint = 1;

            for (int i = 0; i < wayPointsElement.getElementsByTagName("wayPoint").getLength(); i++) {
                Element waypoint = (Element) wayPointsElement.getElementsByTagName("wayPoint").item(i);

                double wayPointLatitude = Double.parseDouble(waypoint.getTextContent().split(",")[0]);
                double wayPointLongitude = Double.parseDouble(waypoint.getTextContent().split(",")[1]);

                long ID = Long.parseLong(waypoint.getAttribute("id"));
                IDMappingWayPoints.put(ID, currentIDWayPoint);
                currentIDWayPoint++;

                wayPoints.put(IDMappingWayPoints.get(ID), new GeoPosition(wayPointLatitude, wayPointLongitude));
            }



            // ---------------
            //   Connections
            // ---------------

            // Remapping of the connection IDs to start from 1
            long currentIDConnection = 1;

            if (configuration.getElementsByTagName("connections").getLength() != 0) {
                Element connectionsElement = (Element) configuration.getElementsByTagName("connections").item(0);

                var con = connectionsElement.getElementsByTagName("connection");

                for (int i = 0; i < con.getLength(); i++) {
                    Element connectionNode = (Element) con.item(i);

                    long ID = Long.parseLong(connectionNode.getAttribute("id"));
                    IDMappingConnections.put(ID, currentIDConnection);
                    currentIDConnection++;

                    connections.put(
                        IDMappingConnections.get(ID),
                        new Connection(
                            IDMappingWayPoints.get(Long.parseLong(connectionNode.getAttribute("src"))),
                            IDMappingWayPoints.get(Long.parseLong(connectionNode.getAttribute("dst")))
                        )
                    );
                }
            }

            simulation.setEnvironment(new Environment(characteristicsMap, mapOrigin, numberOfZones, wayPoints, connections));

            Environment environment = simulation.getEnvironment();

            // ---------------
            //      Motes
            // ---------------

            Element motes = (Element) configuration.getElementsByTagName("motes").item(0);

            for (int i = 0; i < motes.getElementsByTagName("mote").getLength(); i++) {
                Element moteNode = (Element) motes.getElementsByTagName("mote").item(i);
                environment.addMote(new MoteReader(moteNode, environment).buildMote());
            }
            for (int i = 0; i < motes.getElementsByTagName("userMote").getLength(); i++) {
                Element userMoteNode = (Element) motes.getElementsByTagName("userMote").item(i);
                environment.addMote(new UserMoteReader(userMoteNode, environment).buildMote());
            }


            // ---------------
            //    Gateways
            // ---------------

            Element gateways = (Element) configuration.getElementsByTagName("gateways").item(0);
            Element gatewayNode;

            for (int i = 0; i < gateways.getElementsByTagName("gateway").getLength(); i++) {
                gatewayNode = (Element) gateways.getElementsByTagName("gateway").item(i);
                long devEUI = Long.parseUnsignedLong(XMLHelper.readChild(gatewayNode, "devEUI"));
                Element location = (Element) gatewayNode.getElementsByTagName("location").item(0);
                int xPos = Integer.parseInt(XMLHelper.readChild(location, "xPos"));
                int yPos = Integer.parseInt(XMLHelper.readChild(location, "yPos"));

                int transmissionPower = Integer.parseInt(XMLHelper.readChild(gatewayNode, "transmissionPower"));
                int spreadingFactor = Integer.parseInt(XMLHelper.readChild(gatewayNode, "spreadingFactor"));
                environment.addGateway(new Gateway(devEUI, xPos, yPos, environment, transmissionPower, spreadingFactor));
            }
        } catch (ParserConfigurationException | SAXException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static boolean hasChild(Element root, String childName) {
        return root.getElementsByTagName(childName).getLength() != 0;
    }


    private static class MoteReader {
        protected Element node;
        protected Environment environment;

        MoteReader(Element moteNode, Environment environment) {
            this.node = moteNode;
            this.environment = environment;
        }

        long getDevEUI() {
            return Long.parseUnsignedLong(XMLHelper.readChild(node, "devEUI"));
        }

        Pair<Integer, Integer> getMapCoordinates() {
            Element location = (Element) node.getElementsByTagName("location").item(0);
            Element waypoint = (Element) location.getElementsByTagName("waypoint").item(0);
            GeoPosition position = wayPoints.get(IDMappingWayPoints.get(Long.parseLong(waypoint.getAttribute("id"))));
            return MapHelper.toMapCoordinate(position, environment.getMapOrigin());
        }

        int getTransmissionPower() {
            return Integer.parseInt(XMLHelper.readChild(node, "transmissionPower"));
        }

        int getSpreadingFactor() {
            return Integer.parseInt(XMLHelper.readChild(node, "spreadingFactor"));
        }

        int getEnergyLevel() {
            return Integer.parseInt(XMLHelper.readChild(node, "energyLevel"));
        }

        double getMovementSpeed() {
            return Double.parseDouble(XMLHelper.readChild(node, "movementSpeed"));
        }

        List<MoteSensor> getMoteSensors() {
            Element sensors = (Element) node.getElementsByTagName("sensors").item(0);
            List<MoteSensor> moteSensors = new LinkedList<>();
            for (int i = 0; i < sensors.getElementsByTagName("sensor").getLength(); i++) {
                Element sensornode = (Element) sensors.getElementsByTagName("sensor").item(i);
                moteSensors.add(MoteSensor.valueOf(sensornode.getAttribute("SensorType")));
            }

            // FIXME find another way to do this...
            moteSensors.stream()
                .map(MoteSensor::getSensorDataGenerator)
                .filter(o -> o instanceof GPSDataGenerator)
                .forEach(o -> ((GPSDataGenerator) o).setOrigin(environment.getMapOrigin()));

            return moteSensors;
        }

        Path getPath() {
            Path path = new Path();
            Element pathElement = (Element) node.getElementsByTagName("path").item(0);
            for (int i = 0; i < pathElement.getElementsByTagName("connection").getLength(); i++) {
                Element connectionElement = (Element) pathElement.getElementsByTagName("connection").item(i);
                long connectionId = IDMappingConnections.get(Long.parseLong(connectionElement.getAttribute("id")));

                path.addPosition(wayPoints.get(connections.get(connectionId).getFrom()));

                if (i == pathElement.getElementsByTagName("connection").getLength() - 1) {
                    // Add the last destination
                    path.addPosition(wayPoints.get(connections.get(connectionId).getTo()));
                }
            }
            return path;
        }

        Optional<Integer> getStartMovementOffset() {
            if (hasChild(node, "startMovementOffset")) {
                return Optional.of(Integer.parseInt(XMLHelper.readChild(node, "startMovementOffset")));
            }
            return Optional.empty();
        }

        Optional<Integer> getPeriodSendingPacket() {
            if (hasChild(node, "periodSendingPacket")) {
                return Optional.of(Integer.parseInt(XMLHelper.readChild(node, "periodSendingPacket")));
            }
            return Optional.empty();
        }

        Optional<Integer> getStartSendingOffset() {
            if (hasChild(node, "startSendingOffset")) {
                return Optional.of(Integer.parseInt(XMLHelper.readChild(node, "startSendingOffset")));
            }
            return Optional.empty();
        }

        public Mote buildMote() {
            var startMovementOffset = getStartMovementOffset();
            var periodSendingPacket = getPeriodSendingPacket();
            var startSendingOffset = getStartSendingOffset();
            Mote mote;

            if (startMovementOffset.isPresent() && periodSendingPacket.isPresent() && startSendingOffset.isPresent()) {
                mote = MoteFactory.createMote(
                    getDevEUI(),
                    getMapCoordinates().getLeft(),
                    getMapCoordinates().getRight(),
                    environment,
                    getTransmissionPower(),
                    getSpreadingFactor(),
                    getMoteSensors(),
                    getEnergyLevel(),
                    getPath(),
                    getMovementSpeed(),
                    startMovementOffset.get(),
                    periodSendingPacket.get(),
                    startSendingOffset.get()
                );
            } else {
                mote = MoteFactory.createMote(
                    getDevEUI(),
                    getMapCoordinates().getLeft(),
                    getMapCoordinates().getRight(),
                    environment,
                    getTransmissionPower(),
                    getSpreadingFactor(),
                    getMoteSensors(),
                    getEnergyLevel(),
                    getPath(),
                    getMovementSpeed()
                );
            }
            return mote;
        }
    }

    private static class UserMoteReader extends MoteReader {
        protected UserMoteReader(Element moteNode, Environment environment) {
            super(moteNode, environment);
        }


        boolean isActive() {
            return Boolean.parseBoolean(XMLHelper.readChild(node, "userMoteState"));
        }

        GeoPosition getDestination() {
            Element destinationElement = (Element) node.getElementsByTagName("destination").item(0);
            long wayPointId =  Long.parseLong(destinationElement.getAttribute("id"));
            return wayPoints.get(IDMappingWayPoints.get(wayPointId));
        }

        @Override
        public Mote buildMote() {
            UserMote userMote = MoteFactory.createUserMote(
                getDevEUI(),
                getMapCoordinates().getLeft(),
                getMapCoordinates().getRight(),
                environment,
                getTransmissionPower(),
                getSpreadingFactor(),
                getMoteSensors(),
                getEnergyLevel(),
                getPath(),
                getMovementSpeed(),
                getStartMovementOffset().get(), // Intentional
                getPeriodSendingPacket().get(), // Intentional
                getStartSendingOffset().get(),  // Intentional
                getDestination()
            );
            userMote.setActive(isActive());
            return userMote;
        }
    }
}
