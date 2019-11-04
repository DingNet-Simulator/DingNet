package util.xml;

import IotDomain.Environment;
import IotDomain.Simulation;
import IotDomain.networkentity.Gateway;
import IotDomain.networkentity.Mote;
import IotDomain.networkentity.MoteSensor;
import IotDomain.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.GraphStructure;
import util.MapHelper;
import util.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationWriter {

    public static void saveConfigurationToFile(File file, Simulation simulation) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Environment environment = simulation.getEnvironment();
            GraphStructure graph = GraphStructure.getInstance();

            // root element
            Element rootElement = doc.createElement("configuration");
            doc.appendChild(rootElement);


            // ---------------
            //      Map
            // ---------------
            Element map = doc.createElement("map");
            Element region = doc.createElement("region");
            Element origin = doc.createElement("origin");
            map.appendChild(origin);

            Element MapZeroLatitude = doc.createElement("latitude");
            MapZeroLatitude.appendChild(doc.createTextNode(Double.toString(environment.getMapOrigin().getLatitude())));
            origin.appendChild(MapZeroLatitude);

            Element MapZeroLongitude = doc.createElement("longitude");
            MapZeroLongitude.appendChild(doc.createTextNode(Double.toString(environment.getMapOrigin().getLongitude())));
            origin.appendChild(MapZeroLongitude);
            region.appendChild(origin);

            Element size = doc.createElement("size");
            Element width = doc.createElement("width");
            width.appendChild(doc.createTextNode(Integer.toString(environment.getMaxXpos() + 1)));
            Element height = doc.createElement("height");
            height.appendChild(doc.createTextNode(Integer.toString(environment.getMaxYpos() + 1)));
            size.appendChild(width);
            size.appendChild(height);
            region.appendChild(size);

            map.appendChild(region);
            rootElement.appendChild(map);


            // ---------------
            // Characteristics
            // ---------------

            Element characteristics = doc.createElement("characteristics");
            Element regionProperty = doc.createElement("regionProperty");
            regionProperty.setAttribute("numberOfZones", Integer.toString(environment.getNumberOfZones()));
            characteristics.appendChild(regionProperty);

            int amountOfSquares = (int) Math.sqrt(environment.getNumberOfZones());
            LinkedList<Element> row = new LinkedList<>();
            for (int i = 0; i < amountOfSquares; i++) {
                row.add(doc.createElement("row"));
                row.getLast().appendChild(doc.createTextNode(environment.getCharacteristic(0, (int) Math.round(i * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                ).name()));
                for (int j = 1; j < amountOfSquares; j++) {

                    row.getLast().appendChild(doc.createTextNode("-" + environment.getCharacteristic((int) Math.round(j * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                        , (int) Math.round(i * ((double) environment.getMaxYpos()) / amountOfSquares) + 1).name()));
                }
                characteristics.appendChild(row.getLast());
            }

            rootElement.appendChild(characteristics);



            // ---------------
            //      Motes
            // ---------------

            Element motes = doc.createElement("motes");

            for (Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    motes.appendChild(new UserMoteWriter(doc, (UserMote) mote).buildMoteElement());
                } else {
                    motes.appendChild(new MoteWriter(doc, mote).buildMoteElement());
                }
            }

            rootElement.appendChild(motes);



            // ---------------
            //    Gateways
            // ---------------

            Element gateways = doc.createElement("gateways");

            for (Gateway gateway : environment.getGateways()) {
                Element gatewayElement = doc.createElement("gateway");

                Element devEUI = doc.createElement("devEUI");
                devEUI.appendChild(doc.createTextNode(Long.toUnsignedString(gateway.getEUI())));

                Element location = doc.createElement("location");
                Element xPos = doc.createElement("xPos");
                xPos.appendChild(doc.createTextNode(Integer.toString(gateway.getXPos())));
                Element yPos = doc.createElement("yPos");
                yPos.appendChild(doc.createTextNode(Integer.toString(gateway.getYPos())));
                location.appendChild(xPos);
                location.appendChild(yPos);

                Element transmissionPower = doc.createElement("transmissionPower");
                transmissionPower.appendChild(doc.createTextNode(Integer.toString(gateway.getTransmissionPower())));

                Element spreadingFactor = doc.createElement("spreadingFactor");
                spreadingFactor.appendChild(doc.createTextNode(Integer.toString(gateway.getSF())));

                gatewayElement.appendChild(devEUI);
                gatewayElement.appendChild(location);
                gatewayElement.appendChild(transmissionPower);
                gatewayElement.appendChild(spreadingFactor);
                gateways.appendChild(gatewayElement);
            }

            rootElement.appendChild(gateways);



            // TODO could reassign IDs here so that they are sequential/'defragmented' again (starting from 1) for both waypoints and connections
            // ---------------
            //    WayPoints
            // ---------------

            Element wayPointsElement = doc.createElement("wayPoints");
            var wayPoints = graph.getWayPoints();

            for (var me : wayPoints.entrySet()) {
                Element wayPointElement = doc.createElement("wayPoint");

                wayPointElement.setAttribute("id", me.getKey().toString());
                var wayPoint = me.getValue();
                wayPointElement.appendChild(doc.createTextNode(wayPoint.getLatitude() + "," + wayPoint.getLongitude()));
                wayPointsElement.appendChild(wayPointElement);
            }

            rootElement.appendChild(wayPointsElement);


            // ---------------
            //   Connections
            // ---------------

            Element connectionsElement = doc.createElement("connections");
            var connections = graph.getConnections();

            for (var me : connections.entrySet()) {
                Element connectionElement = doc.createElement("connection");

                connectionElement.setAttribute("id", me.getKey().toString());
                connectionElement.setAttribute("src", Long.toString(me.getValue().getFrom()));
                connectionElement.setAttribute("dst", Long.toString(me.getValue().getTo()));

                connectionsElement.appendChild(connectionElement);
            }

            rootElement.appendChild(connectionsElement);


            // ---------------
            //    Data dump
            // ---------------

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

    private static class MoteWriter {
        Document doc;
        Mote mote;
        GraphStructure graph;

        MoteWriter(Document doc, Mote mote) {
            this.doc = doc;
            this.mote = mote;
            this.graph = GraphStructure.getInstance();
        }

        Element generateDevEUIElement() {
            Element devEUI =  doc.createElement("devEUI");
            devEUI.appendChild(doc.createTextNode(Long.toUnsignedString(mote.getEUI())));
            return devEUI;
        }

        Element generateLocationElement() {
            Element location = doc.createElement("location");
            Element wayPoint = doc.createElement("waypoint");

            GeoPosition position = MapHelper.getInstance().toGeoPosition(mote.getPos());
            wayPoint.setAttribute("id", Long.toString(graph.getClosestWayPoint(position)));
            location.appendChild(wayPoint);

            return location;
        }

        Element generateTransmissionPowerElement() {
            Element transmissionPower = doc.createElement("transmissionPower");
            transmissionPower.appendChild(doc.createTextNode(Integer.toString(mote.getTransmissionPower())));
            return transmissionPower;
        }

        Element generateSpreadingFactorElement() {
            Element spreadingFactor = doc.createElement("spreadingFactor");
            spreadingFactor.appendChild(doc.createTextNode(Integer.toString(mote.getSF())));
            return spreadingFactor;
        }

        Element generateEnergyLevelElement() {
            Element energyLevel = doc.createElement("energyLevel");
            energyLevel.appendChild(doc.createTextNode(Integer.toString(mote.getEnergyLevel())));
            return energyLevel;
        }

        Element generateMovementSpeedElement() {
            Element movementSpeed = doc.createElement("movementSpeed");
            movementSpeed.appendChild(doc.createTextNode(Double.toString(mote.getMovementSpeed())));
            return movementSpeed;
        }

        Element generateStartMovementSpeedElement() {
            Element startMovementOffset = doc.createElement("startMovementOffset");
            startMovementOffset.appendChild(doc.createTextNode(Integer.toString(mote.getStartMovementOffset())));
            return startMovementOffset;
        }

        Element generatePeriodSendingPacketElement() {
            Element periodSendingPacket = doc.createElement("periodSendingPacket");
            periodSendingPacket.appendChild(doc.createTextNode(""+mote.getPeriodSendingPacket()));
            return periodSendingPacket;
        }

        Element generateStartSendingOffsetElement() {
            Element startSendingOffset = doc.createElement("startSendingOffset");
            startSendingOffset.appendChild(doc.createTextNode(""+mote.getStartSendingOffset()));
            return startSendingOffset;
        }

        Element generateMoteSensorsElement() {
            Element sensors = doc.createElement("sensors");
            for (MoteSensor sensor : mote.getSensors()) {
                Element sensorElement = doc.createElement("sensor");
                sensorElement.setAttribute("SensorType", sensor.name());
                sensors.appendChild(sensorElement);
            }
            return sensors;
        }

        Element generatePathElement() {
            Element pathElement = doc.createElement("path");
            Path path = mote.getPath();
            for (Long id : path.getConnectionsByID()) {
                Element connectionElement = doc.createElement("connection");
                connectionElement.setAttribute("id", Long.toString(id));
                pathElement.appendChild(connectionElement);
            }
            return pathElement;
        }


        void addMoteDetails(Element element) {
            List.of(generateDevEUIElement(), generateLocationElement(), generateTransmissionPowerElement(), generateSpreadingFactorElement(),
                generateEnergyLevelElement(), generateMovementSpeedElement(), generateStartMovementSpeedElement(), generatePeriodSendingPacketElement(),
                generateStartSendingOffsetElement(), generateMoteSensorsElement(), generatePathElement())
                .forEach(element::appendChild);
        }

        public Element buildMoteElement() {
            Element moteElement = doc.createElement("mote");
            addMoteDetails(moteElement);
            return moteElement;
        }
    }

    private static class UserMoteWriter extends MoteWriter {
        public UserMoteWriter(Document doc, UserMote mote) {
            super(doc, mote);
        }

        Element generateIsActiveElement() {
            Element isActive = doc.createElement("userMoteState");
            isActive.appendChild(doc.createTextNode(Boolean.toString(((UserMote) mote).isActive())));
            return isActive;
        }

        Element generateDestinationElement() {
            GeoPosition destinationPos = ((UserMote) mote).getDestination();
            Element destination = doc.createElement("destination");
            destination.setAttribute("id", Long.toString(graph.getClosestWayPoint(destinationPos)));
            return destination;
        }

        void addUserMoteDetails(Element element) {
            List.of(generateIsActiveElement(), generateDestinationElement()).forEach(element::appendChild);
        }

        @Override
        public Element buildMoteElement() {
            Element moteElement = doc.createElement("userMote");
            addMoteDetails(moteElement);
            addUserMoteDetails(moteElement);
            return moteElement;
        }
    }
}
