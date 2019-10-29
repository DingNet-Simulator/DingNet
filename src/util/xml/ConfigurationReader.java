package util.xml;

import IotDomain.Characteristic;
import IotDomain.Environment;
import IotDomain.Simulation;
import IotDomain.networkentity.Gateway;
import IotDomain.networkentity.MoteFactory;
import IotDomain.networkentity.MoteSensor;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ConfigurationReader {

    public static void loadConfiguration(File file, Simulation simulation) {
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
                Double.valueOf(XMLHelper.readChild(origin, "latitude")),
                Double.valueOf(XMLHelper.readChild(origin, "longitude"))
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
            Map<Long, Long> IDMappingWayPoints = new HashMap<>();
            long currentIDWayPoint = 1;

            Map<Long, GeoPosition> wayPoints = new HashMap<>();
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

            Map<Long, Connection> connections = new HashMap<>();

            // Remapping of the connection IDs to start from 1
            Map<Long, Long> IDMappingConnections = new HashMap<>();
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

            if (GraphStructure.isInitialized()) {
                // Remove the currently loaded graph for the newly loaded one
                GraphStructure.getInstance().close();
            }
            simulation.setEnvironment(new Environment(characteristicsMap, mapOrigin, numberOfZones, wayPoints, connections));



            // ---------------
            //      Motes
            // ---------------

            Element motes = (Element) configuration.getElementsByTagName("motes").item(0);
            Element moteNode;

            for (int i = 0; i < motes.getElementsByTagName("mote").getLength(); i++) {
                moteNode = (Element) motes.getElementsByTagName("mote").item(i);
                long devEUI = Long.parseUnsignedLong(XMLHelper.readChild(moteNode, "devEUI"));

                Element location = (Element) moteNode.getElementsByTagName("location").item(0);
                Element waypoint = (Element) location.getElementsByTagName("waypoint").item(0);
                GeoPosition position = wayPoints.get(IDMappingWayPoints.get(Long.parseLong(waypoint.getAttribute("id"))));
                Pair<Integer, Integer> coords = MapHelper.getInstance().toMapCoordinate(position);

                int transmissionPower = Integer.parseInt(XMLHelper.readChild(moteNode, "transmissionPower"));
                int spreadingFactor = Integer.parseInt(XMLHelper.readChild(moteNode, "spreadingFactor"));
                int energyLevel = Integer.parseInt(XMLHelper.readChild(moteNode, "energyLevel"));
                double movementSpeed = Double.parseDouble(XMLHelper.readChild(moteNode, "movementSpeed"));

                Element sensors = (Element) moteNode.getElementsByTagName("sensors").item(0);
                LinkedList<MoteSensor> moteSensors = new LinkedList<>();
                for (int j = 0; j < sensors.getElementsByTagName("sensor").getLength(); j++) {
                    Element sensornode = (Element) sensors.getElementsByTagName("sensor").item(j);
                    moteSensors.add(MoteSensor.valueOf(sensornode.getAttribute("SensorType")));
                }


                Path path = new Path();
                Element pathElement = (Element) moteNode.getElementsByTagName("path").item(0);
                for (int j = 0; j < pathElement.getElementsByTagName("connection").getLength(); j++) {
                    Element connectionElement = (Element) pathElement.getElementsByTagName("connection").item(j);
                    long connectionId = IDMappingConnections.get(Long.parseLong(connectionElement.getAttribute("id")));

                    path.addPosition(wayPoints.get(connections.get(connectionId).getFrom()));

                    if (j == pathElement.getElementsByTagName("connection").getLength() - 1) {
                        // Add the last destination
                        path.addPosition(wayPoints.get(connections.get(connectionId).getTo()));
                    }
                }
                if (hasChild(moteNode, "startMovementOffset") &&
                    hasChild(moteNode, "periodSendingPacket") &&
                    hasChild(moteNode, "startSendingOffset")) {

                    int startMovementOffset = Integer.parseInt(XMLHelper.readChild(moteNode, "startMovementOffset"));
                    int periodSendingPacket = Integer.parseInt(XMLHelper.readChild(moteNode, "periodSendingPacket"));
                    int startSendingOffset = Integer.parseInt(XMLHelper.readChild(moteNode, "startSendingOffset"));
                    if (hasChild(moteNode, "userMoteState")) {
                        boolean isActive = Boolean.parseBoolean(XMLHelper.readChild(moteNode, "userMoteState"));
                        MoteFactory.createUserMote(devEUI, coords.getLeft(), coords.getRight(), simulation.getEnvironment(), transmissionPower,
                            spreadingFactor, moteSensors, energyLevel, path, movementSpeed, startMovementOffset,
                            periodSendingPacket, startSendingOffset).setActive(isActive);
                    } else {
                        MoteFactory.createMote(devEUI, coords.getLeft(), coords.getRight(), simulation.getEnvironment(), transmissionPower,
                            spreadingFactor, moteSensors, energyLevel, path, movementSpeed, startMovementOffset,
                            periodSendingPacket, startSendingOffset);
                    }
                } else {
                    //old configuration file version
                    MoteFactory.createMote(devEUI, coords.getLeft(), coords.getRight(), simulation.getEnvironment(), transmissionPower, spreadingFactor, moteSensors, energyLevel, path, movementSpeed);
                }
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
                new Gateway(devEUI, xPos, yPos, simulation.getEnvironment(), transmissionPower, spreadingFactor);
            }
        } catch (ParserConfigurationException | SAXException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static boolean hasChild(Element root, String childName) {
        return root.getElementsByTagName(childName).getLength() != 0;
    }
}
