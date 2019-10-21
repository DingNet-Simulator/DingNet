package IotDomain;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.LinkedList;

public class XMLVisitor {

    @Deprecated
    public Document visit(Environment environment) throws ParserConfigurationException {

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
        MapZeroLatitude.appendChild(doc.createTextNode(Double.toString(environment.getMapOrigin().getLatitude())));
        origin.appendChild(MapZeroLatitude);

        Element MapZeroLongitude = doc.createElement("longitude");
        MapZeroLongitude.appendChild(doc.createTextNode(Double.toString(environment.getMapOrigin().getLongitude())));
        origin.appendChild(MapZeroLongitude);
        region.appendChild(origin);

        Element size = doc.createElement("size");
        Element width = doc.createElement("width");
        width.appendChild(doc.createTextNode(""+environment.getMaxXpos()));
        Element height = doc.createElement("height");
        height.appendChild(doc.createTextNode(""+environment.getMaxYpos()));
        size.appendChild(width);
        size.appendChild(height);
        region.appendChild(size);

        map.appendChild(region);
        rootElement.appendChild(map);

        Element characteristics = doc.createElement("characteristics");
        Element regionProperty = doc.createElement("regionProperty");
        regionProperty.setAttribute("numberOfZones", ""+environment.getNumberOfZones());
        characteristics.appendChild(regionProperty);
        int amountOfSquares = (int) Math.sqrt(environment.getNumberOfZones());
        LinkedList<Element> row = new LinkedList<>();
        for (int j = 0; j < amountOfSquares; j++) {
            row.add(doc.createElement("row"));
            row.getLast().appendChild(doc.createTextNode(environment.getCharacteristic(0, (int) Math.round(j * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
            ).name()));
            for (int i = 1; i < amountOfSquares; i++) {

                row.getLast().appendChild(doc.createTextNode("-" + environment.getCharacteristic((int) Math.round(i * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                        , (int) Math.round(j * ((double) environment.getMaxYpos()) / amountOfSquares) + 1).name()));
            }
            characteristics.appendChild(row.getLast());
        }

        rootElement.appendChild(characteristics);

        Element motes = doc.createElement("motes");

        for (Mote mote : environment.getMotes()) {
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
                waypointElement.appendChild(doc.createTextNode(environment.toMapXCoordinate(waypoint) + "," + environment.toMapYCoordinate(waypoint)));
                pathElement.appendChild(waypointElement);
            }
            moteElement.appendChild(pathElement);
            motes.appendChild(moteElement);
        }

        rootElement.appendChild(motes);

        Element gateways = doc.createElement("gateways");

        for (Gateway gateway : environment.getGateways()) {
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

        for (var me : environment.getWayPoints().entrySet()) {
            Element wayPointElement = doc.createElement("wayPoint");

            wayPointElement.setAttribute("id", me.getKey().toString());
            var wayPoint = me.getValue();
            wayPointElement.appendChild(doc.createTextNode(wayPoint.getLatitude() + "," + wayPoint.getLongitude()));
            wayPoints.appendChild(wayPointElement);

        }

        rootElement.appendChild(wayPoints);

        return doc;
    }
}
