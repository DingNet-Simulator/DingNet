package util.xml;

import org.w3c.dom.Element;

public class XMLHelper {

    public static String readChild(Element element, String childName) {
        return element.getElementsByTagName(childName).item(0).getTextContent();
    }
}
