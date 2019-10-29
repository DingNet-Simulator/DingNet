package util.xml;

import IotDomain.Gateway;
import IotDomain.Mote;
import IotDomain.NetworkEntity;
import IotDomain.Simulation;
import IotDomain.lora.LoraTransmission;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class SimulationWriter {
    public static void saveSimulationToFile(File file, Simulation simulation) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            // root element
            Element rootElement = doc.createElement("experimentalData");
            doc.appendChild(rootElement);


            Element runs = doc.createElement("runs");

            for (int i = 0; i < simulation.getEnvironment().getNumberOfRuns(); i++) {
                Element runElement = doc.createElement("run");

                for (Mote mote : simulation.getEnvironment().getMotes()) {
                    Element moteElement = doc.createElement("mote");
                    Element number = doc.createElement("number");
                    number.appendChild(doc.createTextNode(Integer.toString(simulation.getEnvironment().getMotes().indexOf(mote) + 1)));
                    moteElement.appendChild(number);

                    Element receivedTransmissions = writeLoraTransmissions(doc, mote, i, simulation);
                    moteElement.appendChild(receivedTransmissions);
                    runElement.appendChild(moteElement);
                }

                for (Gateway gateway : simulation.getEnvironment().getGateways()) {
                    Element gatewayElement = doc.createElement("gateway");
                    Element number = doc.createElement("number");
                    number.appendChild(doc.createTextNode(((Integer) (simulation.getEnvironment().getGateways().indexOf(gateway) + 1)).toString()));
                    gatewayElement.appendChild(number);

                    Element receivedTransmissions = writeLoraTransmissions(doc, gateway, i, simulation);
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


    private static Element writeLoraTransmissions(Document doc, NetworkEntity networkEntity, int run, Simulation simulation) {
        Element receivedTransmissions = doc.createElement("receivedTransmissions");
        int i = 0;

        for (LoraTransmission transmission : networkEntity.getSentTransmissions(run)) {
            Element receivedTransmissionElement = doc.createElement("receivedTransmission");
            Element sender = doc.createElement("sender");

            if (transmission.getSender().getClass() == Mote.class) {
                Mote mote = (Mote) transmission.getSender();
                int moteId = simulation.getEnvironment().getMotes().indexOf(mote);
                sender.appendChild(doc.createTextNode("Mote " + moteId));
            } else {
                Gateway gateway = (Gateway) transmission.getSender();
                int gatewayId = simulation.getEnvironment().getGateways().indexOf(gateway);
                sender.appendChild(doc.createTextNode("Gateway " + gatewayId));
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
            powerSetting.appendChild(doc.createTextNode(networkEntity.getPowerSettingHistory(run).get(i).toString()));

            Element collision = doc.createElement("collision");
            collision.appendChild(doc.createTextNode(transmission.getReceiver().getAllReceivedTransmissions(run).get(transmission).toString()));

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

            i++;
        }
        return receivedTransmissions;
    }
}
