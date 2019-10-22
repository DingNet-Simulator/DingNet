package util.xml;

import IotDomain.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import util.Connection;
import util.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
                String[] characteristicsRow = XMLHelper.readChild(characteristics, "row").split("-");
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

            Map<Long, GeoPosition> wayPoints = new HashMap<>();
            for (int i = 0; i < wayPointsElement.getElementsByTagName("wayPoint").getLength(); i++) {
                Element waypoint = (Element) wayPointsElement.getElementsByTagName("wayPoint").item(i);
                double wayPointLatitude = Double.parseDouble(waypoint.getTextContent().split(",")[0]);
                double wayPointLongitude = Double.parseDouble(waypoint.getTextContent().split(",")[1]);
                long ID = Long.parseLong(waypoint.getAttribute("id"));

                wayPoints.put(ID, new GeoPosition(wayPointLatitude, wayPointLongitude));
            }

            simulation.setEnvironment(new Environment(characteristicsMap, mapOrigin, numberOfZones));
            simulation.getEnvironment().setWayPoints(wayPoints);


            // ---------------
            //   Connections
            // ---------------

            Map<Long, Connection> connections = new HashMap<>();

            if (configuration.getElementsByTagName("connections").getLength() != 0) {
                Element connectionsElement = (Element) configuration.getElementsByTagName("connections").item(0);

                var con = connectionsElement.getElementsByTagName("connection");

                for (int i = 0; i < con.getLength(); i++) {
                    Element connectionNode = (Element) con.item(i);

                    connections.put(
                        Long.parseLong(connectionNode.getAttribute("id")),
                        new Connection(
                            Long.parseLong(connectionNode.getAttribute("src")),
                            Long.parseLong(connectionNode.getAttribute("dst"))
                        )
                    );
                }
            }
            simulation.getEnvironment().setConnections(connections);



            // ---------------
            //      Motes
            // ---------------

            Element motes = (Element) configuration.getElementsByTagName("motes").item(0);
            Element moteNode;

            for (int i = 0; i < motes.getElementsByTagName("mote").getLength(); i++) {
                moteNode = (Element) motes.getElementsByTagName("mote").item(i);
                long devEUI = Long.parseUnsignedLong(XMLHelper.readChild(moteNode, "devEUI"));
                Element location = (Element) moteNode.getElementsByTagName("location").item(0);
                int xPos = Integer.parseInt(XMLHelper.readChild(location, "xPos"));
                int yPos = Integer.parseInt(XMLHelper.readChild(location, "yPos"));

                int transmissionPower = Integer.parseInt(XMLHelper.readChild(moteNode, "transmissionPower"));
                int spreadingFactor = Integer.parseInt(XMLHelper.readChild(moteNode, "spreadingFactor"));
                int energyLevel = Integer.parseInt(XMLHelper.readChild(moteNode, "energyLevel"));
                int samplingRate = Integer.parseInt(XMLHelper.readChild(moteNode, "samplingRate"));
                double movementSpeed = Double.parseDouble(XMLHelper.readChild(moteNode, "movementSpeed"));

                Element sensors = (Element) moteNode.getElementsByTagName("sensors").item(0);
                LinkedList<MoteSensor> moteSensors = new LinkedList<>();
                for (int j = 0; j < sensors.getElementsByTagName("sensor").getLength(); j++) {
                    Element sensornode = (Element) sensors.getElementsByTagName("sensor").item(j);
                    moteSensors.add(MoteSensor.valueOf(sensornode.getAttribute("SensorType")));
                }


                Path path = new Path(simulation.getEnvironment().getGraph());
                Element pathElement = (Element) moteNode.getElementsByTagName("path").item(0);
                for (int j = 0; j < pathElement.getElementsByTagName("connection").getLength(); j++) {
                    Element connectionElement = (Element) pathElement.getElementsByTagName("connection").item(j);

                    path.addConnection(connections.get(Long.parseLong(connectionElement.getAttribute("id"))));
                }

                new Mote(devEUI, xPos, yPos, simulation.getEnvironment(), transmissionPower, spreadingFactor, moteSensors, energyLevel, path, samplingRate, movementSpeed);
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
}
