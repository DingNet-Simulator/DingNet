package util.xml;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.Connection;
import util.GraphStructure;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.List;

public class ConfigurationWriter {
    private static IdRemapping idRemapping = new IdRemapping();


    public static void saveConfigurationToFile(File file, SimulationRunner simulationRunner) {
        idRemapping.reset();

        try {

            Environment environment = simulationRunner.getEnvironment();
            GraphStructure graph = environment.getGraph();

            // root element
            XMLOutputFactory output = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(new FileOutputStream(file),"UTF-8"));
            writer.writeStartDocument();
            writer.writeStartElement("configuration");

            // ---------------
            //      Map
            // ---------------
            writer.writeStartElement("map");
            writer.writeStartElement("region");
            writer.writeStartElement("origin");
            writer.writeStartElement("latitude");
            writer.writeCharacters(Double.toString(environment.getMapOrigin().getLatitude()));
            writer.writeEndElement();
            writer.writeStartElement("longitude");
            writer.writeCharacters(Double.toString(environment.getMapOrigin().getLongitude()));
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement("size");
            writer.writeStartElement("width");
            writer.writeCharacters(Integer.toString(environment.getMaxXpos() + 1));
            writer.writeEndElement();
            writer.writeStartElement("height");
            writer.writeCharacters(Integer.toString(environment.getMaxYpos() + 1));
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();


            // ---------------
            // Characteristics
            // ---------------
            writer.writeStartElement("characteristics");
            writer.writeStartElement("regionProperty");
            writer.writeAttribute("numberOfZones",Integer.toString(environment.getNumberOfZones()));
            writer.writeEndElement();

            int amountOfSquares = (int) Math.sqrt(environment.getNumberOfZones());
            for (int i = 0; i < amountOfSquares; i++) {
                writer.writeStartElement("row");
                writer.writeCharacters(environment.getCharacteristic(0, (int) Math.round(i * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                ).name());
                for (int j = 1; j < amountOfSquares; j++) {

                    writer.writeCharacters("-" + environment.getCharacteristic((int) Math.round(j * ((double) environment.getMaxXpos()) / amountOfSquares) + 1
                        , (int) Math.round(i * ((double) environment.getMaxYpos()) / amountOfSquares) + 1).name());
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();

            // ---------------
            //    WayPoints
            // ---------------
            writer.writeStartElement("wayPoints");
            var wayPoints = graph.getWayPoints();

            for (var me : wayPoints.entrySet()) {
                writer.writeStartElement("wayPoint");
                long newId = idRemapping.addWayPoint(me.getKey(), me.getValue());
                writer.writeAttribute("id",Long.toString(newId));
                var wayPoint = me.getValue();
                writer.writeCharacters(wayPoint.getLatitude() + "," + wayPoint.getLongitude());
                writer.writeEndElement();
            }

            writer.writeEndElement();



            // ---------------
            //   Connections
            // ---------------

            writer.writeStartElement("connections");
            var connections = graph.getConnections();

            for (var me : connections.entrySet()) {
                writer.writeStartElement("connection");
                long originalId = me.getKey();
                Connection conn = me.getValue();

                long newFromId = idRemapping.getNewWayPointId(conn.getFrom());
                long newToId = idRemapping.getNewWayPointId(conn.getTo());

                // Not really necesary here to make a new connection, but it's a bit awkward to put null here
                long newId = idRemapping.addConnection(originalId, new Connection(newFromId, newToId));
                writer.writeAttribute("id",Long.toString(newId));
                writer.writeAttribute("src",Long.toString(newFromId));
                writer.writeAttribute("dst",Long.toString(newToId));

                writer.writeEndElement();
            }

            writer.writeEndElement();

            // ---------------
            //      Motes
            // ---------------
            writer.writeStartElement("motes");

            for (Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    new UserMoteWriter((UserMote) mote, environment).writeMoteElement(writer);
                } else {
                    if (mote instanceof LifeLongMote) {
                        new LLMoteWriter((LifeLongMote) mote, environment).writeMoteElement(writer);
                    }else {
                        new MoteWriter(mote, environment).writeMoteElement(writer);
                    }
                }
            }
            writer.writeEndElement();


            // ---------------
            //    Gateways
            // ---------------
            writer.writeStartElement("gateways");

            for (Gateway gateway : environment.getGateways()) {
                writer.writeStartElement("gateway");

                writer.writeStartElement("devEUI");
                writer.writeCharacters((Long.toUnsignedString(gateway.getEUI())));
                writer.writeEndElement();

                writer.writeStartElement("location");
                writer.writeStartElement("xPos");
                writer.writeCharacters(Integer.toString((int) Math.round(environment.getMapHelper().toMapXCoordinate(gateway.getPos()))));
                writer.writeEndElement();
                writer.writeStartElement("yPos");
                writer.writeCharacters(Integer.toString((int) Math.round(environment.getMapHelper().toMapYCoordinate(gateway.getPos()))));
                writer.writeEndElement();
                writer.writeEndElement();

                writer.writeStartElement("transmissionPower");
                writer.writeCharacters(Integer.toString(gateway.getTransmissionPower()));
                writer.writeEndElement();

                writer.writeStartElement("spreadingFactor");
                writer.writeCharacters(Integer.toString(gateway.getSF()));
                writer.writeEndElement();

                writer.writeEndElement();
            }
            writer.writeEndElement();


            // ---------------
            //    Data dump
            // ---------------

            writer.writeEndElement();
            writer.writeEndDocument();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class MoteWriter {
        Mote mote;
        GraphStructure graph;
        Environment environment;

        MoteWriter(Mote mote, Environment environment) {
            this.mote = mote;
            this.graph = environment.getGraph();
            this.environment = environment;
        }

        void writeDevEUIElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("devEUI");
            writer.writeCharacters(Long.toUnsignedString(mote.getEUI()));
            writer.writeEndElement();
        }

        void writeLocationElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("location");
            writer.writeStartElement("waypoint");
            GeoPosition position = mote.getOriginalPos();
            writer.writeAttribute("id",Long.toString(graph.getClosestWayPoint(position)));
            writer.writeEndElement();
            writer.writeEndElement();

        }

        void writeTransmissionPowerElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("transmissionPower");
            writer.writeCharacters(Integer.toString(mote.getTransmissionPower()));
            writer.writeEndElement();
        }

        void writeSpreadingFactorElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("spreadingFactor");
            writer.writeCharacters(Integer.toString(mote.getSF()));
            writer.writeEndElement();
        }

        void writeEnergyLevelElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("energyLevel");
            writer.writeCharacters(Integer.toString(mote.getEnergyLevel()));
            writer.writeEndElement();
        }

        void writeMovementSpeedElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("movementSpeed");
            writer.writeCharacters(Double.toString(mote.getMovementSpeed()));
            writer.writeEndElement();
        }

        void writeStartMovementSpeedElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("startMovementOffset");
            writer.writeCharacters(Integer.toString(mote.getStartMovementOffset()));
            writer.writeEndElement();
        }

        void writePeriodSendingPacketElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("periodSendingPacket");
            writer.writeCharacters(Integer.toString(mote.getPeriodSendingPacket()));
            writer.writeEndElement();
        }

        void writeStartSendingOffsetElement(XMLStreamWriter writer) throws  XMLStreamException {

            writer.writeStartElement("startSendingOffset");
            writer.writeCharacters(Integer.toString(mote.getStartSendingOffset()));
            writer.writeEndElement();
        }

        void writeMoteSensorsElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("sensors");
            for (MoteSensor sensor : mote.getSensors()) {
                writer.writeStartElement("sensor");
                writer.writeAttribute("SensorType",sensor.name());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        void writePathElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("path");
            // ---------------
            //    WayPoints
            // ---------------

            writer.writeStartElement("wayPoints");
            var wayPoints = mote.getPath().getWayPoints();

            for (var wayPoint : wayPoints) {
                writer.writeStartElement("wayPoint");
                writer.writeCharacters(wayPoint.getLatitude() + "," + wayPoint.getLongitude());
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }


        void addMoteDetails(XMLStreamWriter writer) throws XMLStreamException {
            writeDevEUIElement(writer);
            writeLocationElement(writer);
            writeTransmissionPowerElement(writer);
            writeSpreadingFactorElement(writer);
            writeEnergyLevelElement(writer);
            writeMovementSpeedElement(writer);
            writeStartMovementSpeedElement(writer);
            writePeriodSendingPacketElement(writer);
            writeStartSendingOffsetElement(writer);
            writeMoteSensorsElement(writer);
            writePathElement(writer);
        }

        public void writeMoteElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("mote");
            addMoteDetails(writer);
            writer.writeEndElement();
        }
    }

    private static class UserMoteWriter extends MoteWriter {
        UserMoteWriter( UserMote mote, Environment environment) {
            super( mote, environment);
        }

        void writeIsActiveElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("userMoteState");
            writer.writeCharacters(Boolean.toString(((UserMote) mote).isActive()));
            writer.writeEndElement();
        }

        void writeDestinationElement(XMLStreamWriter writer) throws XMLStreamException {
            GeoPosition destinationPos = ((UserMote) mote).getDestination();
            writer.writeStartElement("destination");
            writer.writeAttribute("id",Long.toString(idRemapping.getNewWayPointId(graph.getClosestWayPoint(destinationPos))));
            writer.writeEndElement();
        }

        void addUserMoteDetails(XMLStreamWriter writer) throws XMLStreamException {
            writeIsActiveElement(writer);
            writeDestinationElement(writer);
        }

        @Override
        public void writePathElement(XMLStreamWriter writer) throws XMLStreamException{
            // Empty element since user motes get their path from the routing application
            // (Currently, a starting path for user motes is not supported)
            writer.writeStartElement("path");
            writer.writeEndElement();
        }

        @Override
        public void writeMoteElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("userMote");
            addMoteDetails(writer);
            addUserMoteDetails(writer);
            writer.writeEndElement();
        }
    }

    private static class LLMoteWriter extends MoteWriter {
        LLMoteWriter( LifeLongMote mote, Environment environment) {
            super( mote, environment);
        }

        void writeTransmittingIntervalElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("transmittingInterval");
            writer.writeCharacters(Integer.toString(((LifeLongMote) mote).getTransmittingInterval()));
            writer.writeEndElement();
        }

        void writeExpirationTimeElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("expirationTime");
            writer.writeCharacters(Integer.toString(((LifeLongMote) mote).getExpirationTime()));
            writer.writeEndElement();
        }

        void addLifeLongMoteDetails(XMLStreamWriter writer) throws XMLStreamException {
            writeTransmittingIntervalElement(writer);
            writeExpirationTimeElement(writer);
        }

        @Override
        public void writeMoteElement(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("lifeLongMote");
            addMoteDetails(writer);
            addLifeLongMoteDetails(writer);
            writer.writeEndElement();
        }
    }
}
