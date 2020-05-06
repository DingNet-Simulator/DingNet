package util.xml;

import org.w3c.dom.Element;

import java.util.Optional;

class XMLHelper {

    static String readChild(Element element, String childName) {
        return element.getElementsByTagName(childName).item(0).getTextContent();
    }

    static boolean hasChild(Element root, String childName) {
        return root.getElementsByTagName(childName).getLength() != 0;
    }

    static Optional<String> readOptionalChild(Element element, String childName) {
        return hasChild(element, childName) ? Optional.of(readChild(element, childName)) : Optional.empty();
    }
}
