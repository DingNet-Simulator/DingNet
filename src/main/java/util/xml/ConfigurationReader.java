package util.xml;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler;
import de.westnordost.osmapi.overpass.OverpassMapDataApi;
import iot.Characteristic;
import iot.CharacteristicsMap;
import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import util.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ConfigurationReader {
    private static IdRemapping idRemapping = new IdRemapping();

    public static void loadConfiguration(File file, SimulationRunner simulationRunner) {
        idRemapping.reset();

        try {
            XMLInputFactory input = XMLInputFactory.newInstance();
            XMLEventReader reader = input.createXMLEventReader(new FileInputStream(file), "UTF-8");
            // ---------------
            //      Map
            // ---------------
            XMLEvent event = reader.nextEvent();
            while (!event.isStartElement() || !"map".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            while (!event.isStartElement() || !"latitude".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            double latitude = Double.parseDouble(reader.getElementText());
            while (!event.isStartElement() || !"longitude".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            double longitude = Double.parseDouble(reader.getElementText());


            GeoPosition mapOrigin = new GeoPosition(latitude, longitude);

            while (!event.isStartElement() || !"width".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            int width = Integer.parseInt(reader.getElementText());
            while (!event.isStartElement() || !"height".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            int height = Integer.parseInt(reader.getElementText());

            // ---------------
            // Characteristics
            // ---------------

            while (!event.isStartElement() || !"characteristics".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            reader.nextEvent();
            event = reader.nextEvent();
            int numberOfZones = Integer.parseInt(event.asStartElement().getAttributeByName(QName.valueOf("numberOfZones")).getValue());
            long n = Math.round(Math.sqrt(numberOfZones));
            CharacteristicsMap characteristicsMap = new CharacteristicsMap(width,height,n,n,Characteristic.City);
            reader.nextEvent();
            reader.nextEvent();
            reader.nextEvent();

            for (int i = 0; i < n; i++) {
                String[] characteristicsRow = reader.getElementText().split("-");

                for (int j = 0; j < characteristicsRow.length; j++) {
                    Characteristic characteristic = Characteristic.valueOf(characteristicsRow[j]);
                    characteristicsMap.setCharacterstics(characteristic,i,j);
                }
                reader.nextEvent();
                reader.nextEvent();
            }

            // ---------------
            //    WayPoints
            // ---------------

            while (!event.isStartElement() || !"wayPoints".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            reader.nextEvent();
            event = reader.nextEvent();
            while (!event.isEndElement()) {
                long ID = Long.parseLong(event.asStartElement().getAttributeByName(QName.valueOf("id")).getValue());

                String position = reader.getElementText();
                double wayPointLatitude = Double.parseDouble(position.split(",")[0]);
                double wayPointLongitude = Double.parseDouble(position.split(",")[1]);

                idRemapping.addWayPoint(ID, new GeoPosition(wayPointLatitude, wayPointLongitude));
                reader.nextEvent();
                event = reader.nextEvent();
            }


            // ---------------
            //   Connections
            // ---------------

            while (!event.isStartElement() || !"connections".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            reader.nextEvent();
            event = reader.nextEvent();
            while (!event.isEndElement()) {
                long ID = Long.parseLong(event.asStartElement().getAttributeByName(QName.valueOf("id")).getValue());

                idRemapping.addConnection(ID, new Connection(
                    idRemapping.getNewWayPointId(Long.parseLong(event.asStartElement().getAttributeByName(QName.valueOf("src")).getValue())),
                    idRemapping.getNewWayPointId(Long.parseLong(event.asStartElement().getAttributeByName(QName.valueOf("dst")).getValue()))
                ));
                reader.nextEvent();
                reader.nextEvent();
                event = reader.nextEvent();
            }

            simulationRunner.setEnvironment(new Environment(characteristicsMap, mapOrigin, numberOfZones,
                idRemapping.getWayPoints(), idRemapping.getConnections()));

            Environment environment = simulationRunner.getEnvironment();

            // ---------------
            //      Motes
            // ---------------

            while (!event.isStartElement() || !"motes".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            reader.nextEvent();
            event = reader.nextEvent();

            while (!event.isEndElement()) {
                reader.nextEvent();
                switch (event.asStartElement().getName().getLocalPart()) {
                    case "lifeLongMote":
                        environment.addMote(new LLMoteReader(reader, environment).buildMote());
                        break;
                    case "userMote":
                        environment.addMote(new UserMoteReader(reader, environment).buildMote());
                        break;
                    default:
                        environment.addMote(new MoteReader(reader, environment).buildMote());
                        break;
                }
                reader.nextEvent();
                event = reader.nextEvent();
            }


            // ---------------
            //    Gateways
            // ---------------

            while (!event.isStartElement() || !"gateways".equals(event.asStartElement().getName().getLocalPart())) {
                event = reader.nextEvent();
            }
            reader.nextEvent();
            event = reader.nextEvent();
            while (!event.isEndElement()) {

                reader.nextEvent();
                reader.nextEvent();
                long devEUI = Long.parseUnsignedLong(reader.getElementText());

                while(!reader.peek().isStartElement() ||
                    !reader.peek().asStartElement().getName().getLocalPart().equals("xPos")&&
                    !reader.peek().asStartElement().getName().getLocalPart().equals("latitude")){
                    reader.nextEvent();
                }
                boolean coordinates = false;
                int xPos = 0;
                if(reader.nextEvent().asStartElement().getName().getLocalPart().equals("xPos")){
                     xPos = Integer.parseInt(reader.getElementText());
                }else{
                    latitude = Double.parseDouble(reader.getElementText());
                    coordinates = true;
                }

                while(!reader.peek().isStartElement() ||
                    !reader.peek().asStartElement().getName().getLocalPart().equals("yPos")&&
                    !reader.peek().asStartElement().getName().getLocalPart().equals("longitude")){
                    reader.nextEvent();
                }
                int yPos = 0;
                if(reader.nextEvent().asStartElement().getName().getLocalPart().equals("yPos")){
                    yPos = Integer.parseInt(reader.getElementText());
                }else {
                    longitude = Double.parseDouble(reader.getElementText());
                }

                while(!reader.peek().isStartElement() ||
                    !reader.peek().asStartElement().getName().getLocalPart().equals("transmissionPower")){
                    reader.nextEvent();
                }
                reader.nextEvent();

                int transmissionPower = Integer.parseInt(reader.getElementText());
                while(!reader.peek().isStartElement() ||
                    !reader.peek().asStartElement().getName().getLocalPart().equals("spreadingFactor")){
                    reader.nextEvent();
                }
                reader.nextEvent();
                int spreadingFactor = Integer.parseInt(reader.getElementText());
                while(!reader.peek().isEndElement() ||
                    !reader.peek().asEndElement().getName().getLocalPart().equals("gateway")){
                    reader.nextEvent();
                }
                reader.nextEvent();
                reader.nextEvent();
                event = reader.nextEvent();
                if(coordinates){
                    environment.addGateway(new Gateway(devEUI,new GeoPosition(latitude,longitude),transmissionPower,spreadingFactor,environment));
                }else {
                    environment.addGateway(new Gateway(devEUI, xPos, yPos, transmissionPower, spreadingFactor, environment));
                }
            }


        } catch (IOException | XMLStreamException e1) {
            e1.printStackTrace();
        }
    }



    private static class MoteReader {
        protected XMLEventReader reader;
        protected Environment environment;

        MoteReader(XMLEventReader reader, Environment environment) {
            this.reader = reader;
            this.environment = environment;
        }

        long getDevEUI() throws XMLStreamException {
            reader.nextEvent();
            long EUI =  Long.parseUnsignedLong(reader.getElementText());
            reader.nextEvent();
            return EUI;
        }

        Pair<Double, Double> getMapCoordinates() throws XMLStreamException {
            reader.nextEvent();
            reader.nextEvent();
            GeoPosition position = idRemapping.getWayPointWithOriginalId(Long.parseLong(reader.nextEvent().asStartElement().getAttributeByName(QName.valueOf("id")).getValue()));
            reader.nextEvent();
            reader.nextEvent();
            reader.nextEvent();
            return environment.getMapHelper().toMapCoordinate(position);
        }

        int getTransmissionPower() throws XMLStreamException {
            reader.nextEvent();
            reader.nextEvent();
            int transmissionPower =  Integer.parseInt(reader.getElementText());
            reader.nextEvent();
            reader.nextEvent();
            return transmissionPower;
        }

        int getSpreadingFactor() throws XMLStreamException {
            int spreadingFactor =  Integer.parseInt(reader.getElementText());
            reader.nextEvent();
            return spreadingFactor;
        }

        int getEnergyLevel() throws XMLStreamException {

            reader.nextEvent();
            int energyLevel =  Integer.parseInt(reader.getElementText());
            reader.nextEvent();
            return energyLevel;
        }

        double getMovementSpeed() throws XMLStreamException {
            reader.nextEvent();
            double movementSpeed =  Double.parseDouble(reader.getElementText());
            reader.nextEvent();
            return movementSpeed;
        }

        List<MoteSensor> getMoteSensors() throws XMLStreamException {
            reader.nextEvent();
            reader.nextEvent();
            XMLEvent event = reader.nextEvent();
            List<MoteSensor> moteSensors = new LinkedList<>();
            while(event.isStartElement()){
                moteSensors.add(MoteSensor.valueOf(event.asStartElement().getAttributeByName(QName.valueOf("SensorType")).getValue()));
                reader.nextEvent();
                reader.nextEvent();
                event = reader.nextEvent();
            }
            while(!event.isStartElement()) {
                event = reader.nextEvent();
            }

            return moteSensors;
        }

        Path getPath() throws XMLStreamException {;
            reader.nextEvent();
            Path path;
            LinkedList<GeoPosition> pathWaypoints = new LinkedList<>();
            XMLEvent event = reader.nextEvent();

            if(event.asStartElement().getName().getLocalPart().equals("wayPoints")) {
                // ---------------
                //    WayPoints
                // ---------------
                reader.nextEvent();
                event = reader.nextEvent();

                while (event.isStartElement()) {
                    String waypoint = reader.getElementText();

                    double wayPointLatitude = Double.parseDouble(waypoint.split(",")[0]);
                    double wayPointLongitude = Double.parseDouble(waypoint.split(",")[1]);

                    pathWaypoints.add(new GeoPosition(wayPointLatitude, wayPointLongitude));
                    reader.nextEvent();
                    event =  reader.nextEvent();
                }
                reader.nextEvent();
                reader.nextEvent();
                path = new Path(pathWaypoints);
            } else {
                path = new Path(new LinkedList<>());
                while (event.isStartElement()) {
                    Connection connection = idRemapping.getConnectionWithOriginalId(Long.parseLong(event.asStartElement().getAttributeByName(QName.valueOf("id")).getValue()));
                    reader.nextEvent();
                    reader.nextEvent();
                    event = reader.nextEvent();

                    path.addPosition(idRemapping.getWayPointWithNewId(idRemapping.getNewWayPointId(connection.getFrom())));
                    if (!event.isStartElement()) {
                        // Add the last destination
                        path.addPosition(idRemapping.getWayPointWithNewId(connection.getTo()));
                    }
                }
            }

            return path;
        }

        Optional<Integer> getStartMovementOffset() throws XMLStreamException {
            if (reader.peek().asStartElement().getName().getLocalPart().equals("startMovementOffset")) {
                reader.nextEvent();
                Optional offset =  Optional.of(Integer.parseInt(reader.getElementText()));
                reader.nextEvent();
                return offset;
            }
            return Optional.empty();
        }

        Optional<Integer> getPeriodSendingPacket() throws XMLStreamException {
            if (reader.peek().asStartElement().getName().getLocalPart().equals("periodSendingPacket")) {
                reader.nextEvent();
                Optional period =  Optional.of(Integer.parseInt(reader.getElementText()));
                reader.nextEvent();
                return period;
            }
            return Optional.empty();
        }

        Optional<Integer> getStartSendingOffset() throws XMLStreamException {
            if (reader.peek().asStartElement().getName().getLocalPart().equals("startSendingOffset")) {
                reader.nextEvent();
                Optional offset =  Optional.of(Integer.parseInt(reader.getElementText()));
                reader.nextEvent();
                return offset;
            }
            return Optional.empty();

        }

        public Mote buildMote() throws XMLStreamException {
            var startMovementOffset = getStartMovementOffset();
            var periodSendingPacket = getPeriodSendingPacket();
            var startSendingOffset = getStartSendingOffset();
            Mote mote;

            if (startMovementOffset.isPresent() && periodSendingPacket.isPresent() && startSendingOffset.isPresent()) {
                mote = MoteFactory.createMote(
                    getDevEUI(),
                    getMapCoordinates(),
                    getTransmissionPower(),
                    getSpreadingFactor(),
                    getEnergyLevel(),
                    getMovementSpeed(),
                    startMovementOffset.get(),
                    periodSendingPacket.get(),
                    startSendingOffset.get(),
                    getMoteSensors(),
                    getPath(),
                    environment
                );
            } else {
                mote = MoteFactory.createMote(
                    getDevEUI(),
                    getMapCoordinates(),
                    getTransmissionPower(),
                    getSpreadingFactor(),
                    getEnergyLevel(),
                    getMovementSpeed(),
                    getMoteSensors(),
                    getPath(),

                    environment
                );
            }
            return mote;
        }
    }

    private static class UserMoteReader extends MoteReader {
        protected UserMoteReader(XMLEventReader reader, Environment environment) {
            super(reader, environment);
        }


        boolean isActive() throws XMLStreamException {
            reader.nextEvent();
            boolean active = Boolean.parseBoolean(reader.getElementText());
            reader.nextEvent();
            return active;
        }

        GeoPosition getDestination() throws XMLStreamException {
            XMLEvent event = reader.nextEvent();
            long wayPointId =  Long.parseLong(event.asStartElement().getAttributeByName(QName.valueOf("id")).getValue());
            reader.nextEvent();
            return idRemapping.getWayPointWithOriginalId(wayPointId);
        }

        @Override
        public Mote buildMote() throws XMLStreamException {
            UserMote userMote = MoteFactory.createUserMote(
                getDevEUI(),
                getMapCoordinates(),
                getTransmissionPower(),
                getSpreadingFactor(),
                getEnergyLevel(),
                getMovementSpeed(),
                getStartMovementOffset().get(), // Intentional
                getPeriodSendingPacket().get(), // Intentional
                getStartSendingOffset().get(),  // Intentional
                getDestination(),
                getMoteSensors(),
                getPath(),
                environment
            );
            userMote.setActive(isActive());
            return userMote;
        }
    }

    private static class LLMoteReader extends MoteReader {
        protected LLMoteReader(XMLEventReader reader, Environment environment) {
            super(reader, environment);
        }

        int getTransmittingInterval() throws XMLStreamException {
            while(!reader.nextEvent().isStartElement()){
            }
            int interval = Integer.parseInt(reader.getElementText());
            reader.nextEvent();
            return interval;
        }
        int getExpirationTime() throws XMLStreamException {
            reader.nextEvent();
            int expirationTime = Integer.parseInt(reader.getElementText());
            reader.nextEvent();
            reader.nextEvent();
            return expirationTime;
        }

        @Override
        public LifeLongMote buildMote() throws XMLStreamException {
            LifeLongMote lifeLongMote = MoteFactory.createLLSACompliantMote(
                getDevEUI(),
                getMapCoordinates(),
                getTransmissionPower(),
                getSpreadingFactor(),
                getEnergyLevel(),
                getMovementSpeed(),
                getStartMovementOffset().get(), // Intentional
                getPeriodSendingPacket().get(), // Intentional
                getStartSendingOffset().get(),  // Intentional
                getMoteSensors(),
                getPath(),
                getTransmittingInterval(),
                getExpirationTime(),
                environment
            );
            return lifeLongMote;
        }
    }
}
