package util.xml;

import GUI.MainGUI;
import IotDomain.InputProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


public class InputProfilesWriter {
    public static void updateInputProfilesFile(List<InputProfile> inputProfiles) {
        try {
            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(file.getParent() + "/inputProfiles/inputProfile.xml");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

            Element inputProfilesElement = doc.getDocumentElement();

            for (int i = 0; i < inputProfilesElement.getChildNodes().getLength(); ) {
                inputProfilesElement.removeChild(inputProfilesElement.getChildNodes().item(0));
            }

            for (InputProfile inputProfile : inputProfiles) {
                Node importedNode = doc.importNode(inputProfile.getXmlSource().getDocumentElement(), true);
                inputProfilesElement.appendChild(importedNode);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (URISyntaxException | ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }

    }
}
