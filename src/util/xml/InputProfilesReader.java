package util.xml;

import GUI.MainGUI;
import IotDomain.InputProfile;
import IotDomain.QualityOfService;
import SelfAdaptation.AdaptationGoals.AdaptationGoal;
import SelfAdaptation.AdaptationGoals.IntervalAdaptationGoal;
import SelfAdaptation.AdaptationGoals.ThresholdAdaptationGoal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InputProfilesReader {
    public static List<InputProfile> readInputProfiles() {
        List<InputProfile> inputProfiles = new LinkedList<>();

        try {
            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(file.getParent() + "/inputProfiles/inputProfile.xml");

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element inputProfilesElement = doc.getDocumentElement();

            var inputProfilesList = inputProfilesElement.getElementsByTagName("inputProfile");
            for (int i = 0; i < inputProfilesList.getLength(); i++) {
                Element inputProfileElement = (Element) inputProfilesList.item(i);

                String name = XMLHelper.readChild(inputProfileElement, "name");
                int numberOfRuns = Integer.parseInt(XMLHelper.readChild(inputProfileElement, "numberOfRuns"));

                Element QoSElement = (Element) inputProfileElement.getElementsByTagName("QoS").item(0);
                HashMap<String, AdaptationGoal> adaptationGoalHashMap = new HashMap<>();
                var adaptationGoals = QoSElement.getElementsByTagName("adaptationGoal");

                for (int j = 0; j < adaptationGoals.getLength(); j++) {
                    Element adaptationGoalElement = (Element) adaptationGoals.item(j);

                    String goalName = XMLHelper.readChild(adaptationGoalElement, "name");
                    String goalType = adaptationGoalElement.getAttribute("type");
                    AdaptationGoal adaptationGoal;

                    if (goalType.equals("interval")) {
                        double upperValue = Double.parseDouble(XMLHelper.readChild(adaptationGoalElement, "upperValue"));
                        double lowerValue = Double.parseDouble(XMLHelper.readChild(adaptationGoalElement, "lowerValue"));
                        adaptationGoal = new IntervalAdaptationGoal(lowerValue, upperValue);
                    } else if (goalType.equals("threshold")) {
                        double threshold = Double.parseDouble(XMLHelper.readChild(adaptationGoalElement, "threshold"));
                        adaptationGoal = new ThresholdAdaptationGoal(threshold);
                    } else {
                        throw new RuntimeException(String.format("Unsupported type of adaptation goal: %s", goalType));
                    }
                    adaptationGoalHashMap.put(goalName, adaptationGoal);
                }


                Map<Integer, Double> moteProbabilities = new HashMap<>();
                var moteProbabilitiesList = inputProfileElement.getElementsByTagName("mote");

                for (int j = 0; j < moteProbabilitiesList.getLength(); j++) {
                    Element moteElement = (Element) moteProbabilitiesList.item(j);

                    int moteNumber = Integer.parseInt(XMLHelper.readChild(moteElement, "moteNumber"));
                    double activationProbability = Double.parseDouble(XMLHelper.readChild(moteElement, "activityProbability"));

                    // TODO make sure the '-1' is correct here
                    moteProbabilities.put(moteNumber - 1, activationProbability);
                }

                inputProfiles.add(new InputProfile(
                    name,
                    new QualityOfService(adaptationGoalHashMap),
                    numberOfRuns,
                    moteProbabilities,
                    new HashMap<>(),
                    new HashMap<>(),
                    inputProfileElement)
                );
            }
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return inputProfiles;
    }
}
