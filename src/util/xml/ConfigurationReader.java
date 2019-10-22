package util.xml;

import IotDomain.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;

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

            Element wayPoints = (Element) configuration.getElementsByTagName("wayPoints").item(0);

            LinkedHashSet<GeoPosition> wayPointsSet = new LinkedHashSet<>();
            for (int i = 0; i < wayPoints.getElementsByTagName("wayPoint").getLength(); i++) {
                Element waypoint = (Element) wayPoints.getElementsByTagName("wayPoint").item(i);
                double wayPointLatitude = Double.parseDouble(waypoint.getTextContent().split(",")[0]);
                double wayPointLongitude = Double.parseDouble(waypoint.getTextContent().split(",")[1]);
                wayPointsSet.add(new GeoPosition(wayPointLatitude, wayPointLongitude));
            }

            simulation.setEnvironment(new Environment(characteristicsMap, mapOrigin, wayPointsSet, numberOfZones));



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
