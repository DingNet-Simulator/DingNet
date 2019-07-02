package IotDomain;


import GUI.MainGUI;
import SelfAdaptation.AdaptationGoals.AdaptationGoal;
import SelfAdaptation.AdaptationGoals.IntervalAdaptationGoal;
import SelfAdaptation.AdaptationGoals.ThresholdAdaptationGoal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Set;

/**
 * A class representing an input profile for the simulator.
 */
public class InputProfile {
    /**
     * The name of the input profile.
     */
    private String name;
    /**
     * A quality of service profile for this inputProfile
     */
    private QualityOfService qualityOfServiceProfile;

    /**
     * The number of runs in this inputProfile
     */
    private Integer numberOfRuns;

    /**
     * The probabilities for the motes to run a certain path.
     */
    private HashMap<Integer,Double> probabilitiesForMotes;

    /**
     * The probabilities for the gateways to work.
     */
    private HashMap<Integer,Double> probabilitiesForGateways;
    /**
     * Other probabilities chosen for the simulation
     */
    private HashMap<Integer,Double> regionProbabilities;
    /**
     * The source Document of the profile.
     */
    private Document xmlSource;
    /**
     * The gui on which the inputProfile is displayed.
     */
    private MainGUI gui;

    /**
     * Generates InputProfile with a given qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
     * regionProbabilities, xmlSource and gui.
     * @param qualityOfServiceProfile The quality of service profile.
     * @param numberOfRuns The number of runs.
     * @param probabilitiesForMotes The probabilities for the motes.
     * @param probabilitiesForGateways The probabilities for the gateways.
     * @param regionProbabilities The probabilities for the regions.
     * @param xmlSource The source of the InputProfile.
     * @param gui The MainGUI displaying the input profiles.
     */
    public InputProfile(String name, QualityOfService qualityOfServiceProfile,
                        Integer numberOfRuns,
                        HashMap<Integer, Double> probabilitiesForMotes,
                        HashMap<Integer, Double> probabilitiesForGateways, HashMap<Integer, Double> regionProbabilities,
                        Element xmlSource, MainGUI gui){
        this.name = name;
        this.qualityOfServiceProfile = qualityOfServiceProfile;
        this.numberOfRuns =numberOfRuns;
        this.probabilitiesForMotes = probabilitiesForMotes;
        this.regionProbabilities = regionProbabilities;
        this.probabilitiesForGateways = probabilitiesForGateways;
        Node node = xmlSource;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document newDocument = builder.newDocument();
        Node importedNode = newDocument.importNode(node, true);
        newDocument.appendChild(importedNode);
        this.xmlSource = newDocument;
        this.gui = gui;
    }

    /**
     * Returns the quality of service profile.
     * @returnthe The quality of service profile.
     */
    public QualityOfService getQualityOfServiceProfile() {
        return qualityOfServiceProfile;
    }

    /**
     * Sets the quality of service profile.
     * @param qualityOfServiceProfile The quality of service profile to set.
     */
    public void setQualityOfServiceProfile(QualityOfService qualityOfServiceProfile) {
        this.qualityOfServiceProfile = qualityOfServiceProfile;
        updateFile();
    }

    /**
     * Returns the number of runs.
     * @return The number of runs.
     */
    public Integer getNumberOfRuns() {
        return numberOfRuns;
    }

    /**
     * Sets the number of runs.
     * @param numberOfRuns The number of runs to set.
     */
    public void setNumberOfRuns(Integer numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
        updateFile();
    }

    /**
     * Returns the probability for a given mote number.
     * @param moteNumber The number of the mote.
     * @return The probability for the mote.
     */
    public Double getProbabilityForMote(Integer moteNumber) {
        if(probabilitiesForMotes.get(moteNumber) != null)
            return probabilitiesForMotes.get(moteNumber);
        else{
            return 0.0;
        }
    }

    /**
     * Returns he numbers of the motes where there are probabilities for.
     * @return The numbers of the motes where there are probabilities for.
     */
    public Set<Integer> getProbabilitiesForMotesKeys(){
        return probabilitiesForMotes.keySet();
    }

    /**
     * Puts a given probability with a given moteNumber in the map.
     * @param moteNumber The number of the mote.
     * @param probability The probability of the mote.
     */
    public void putProbabilitiyForMote(Integer moteNumber, Double probability) {
        this.probabilitiesForMotes.put(moteNumber,probability);
        updateFile();
    }

    /**
     * Returns the probability for a given gateway number.
     * @param gatewayNumber The number of the gateway.
     * @return The probability for the gateway.
     */
    public Double getProbabilityForGateway(Integer gatewayNumber) {
        if(probabilitiesForGateways.get(gatewayNumber) != null)
            return probabilitiesForGateways.get(gatewayNumber);
        else{
            return 0.0;
        }
    }

    /**
     * Returns he numbers of the gateways where there are probabilities for.
     * @return The numbers of the gateways where there are probabilities for.
     */
    public Set<Integer> getProbabilitiesForGatewayKeys(){
        return probabilitiesForGateways.keySet();
    }

    /**
     * Puts a given probability with a given gateway number in the map.
     * @param gatewayNumber The number of the gateway.
     * @param probability The probability of the gateway.
     */
    public void putProbabilitiyForGateway(Integer gatewayNumber, Double probability) {
        this.probabilitiesForGateways.put(gatewayNumber,probability);
        updateFile();
    }

    /**
     * Returns the probability for a given region number.
     * @param regionNumber The number of the region.
     * @return The probability for the region.
     */
    public Double getRegionProbability(Integer regionNumber) {

        if(regionProbabilities.get(regionNumber) != null)
            return regionProbabilities.get(regionNumber);
        else{
            return 0.0;
        }
    }

    /**
     * Returns he numbers of the regions where there are probabilities for.
     * @return The numbers of the regions where there are probabilities for.
     */
    public Set<Integer> getRegionProbabilitiesKeys(){
        return regionProbabilities.keySet();
    }

    /**
     * Puts a given probability with a given region number in the map.
     * @param regionNumber The number of the region.
     * @param probability The probability of the region.
     */
    public void putProbabilitiyForRegion(Integer regionNumber, Double probability) {
        this.regionProbabilities.put(regionNumber,probability);
        updateFile();
    }

    /**
     * returns the xml source.
     * @return The xml source.
     */
    public Document getXmlSource() {
        return xmlSource;
    }

    /**
     * Returns the name of the InputProfile.
     * @return the name of the InputProfile.
     */
    public String getName() {
        return name;
    }

    /**
     * A function which updates the source file.
     */
    private void updateFile(){
        Document doc = getXmlSource();
        for(int i =0 ; i<doc.getChildNodes().getLength();){
            doc.removeChild(doc.getChildNodes().item(0));
        }
        Element inputProfileElement = doc.createElement("inputProfile");
        doc.appendChild(inputProfileElement);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(getName()));
        inputProfileElement.appendChild(name);

        Element numberOfRuns = doc.createElement("numberOfRuns");
        numberOfRuns.appendChild(doc.createTextNode(getNumberOfRuns().toString()));
        inputProfileElement.appendChild(numberOfRuns);

        Element Qos = doc.createElement("QoS");

        for(String goalName : getQualityOfServiceProfile().getNames()) {
            Element adaptationGoalElement = doc.createElement("adaptationGoal");

            Element goalNameElement = doc.createElement("name");
            goalNameElement.appendChild(doc.createTextNode(goalName));
            adaptationGoalElement.appendChild(goalNameElement);

            if (getQualityOfServiceProfile().getAdaptationGoal(goalName).getClass() == IntervalAdaptationGoal.class) {
                adaptationGoalElement.setAttribute("type", "interval");
                Element upperValue = doc.createElement("upperValue");
                upperValue.appendChild(doc.createTextNode(((IntervalAdaptationGoal) getQualityOfServiceProfile().getAdaptationGoal(goalName)).getUpperBoundary().toString()));
                adaptationGoalElement.appendChild(upperValue);
                Element lowerValue = doc.createElement("lowerValue");
                lowerValue.appendChild(doc.createTextNode(((IntervalAdaptationGoal) getQualityOfServiceProfile().getAdaptationGoal(goalName)).getLowerBoundary().toString()));
                adaptationGoalElement.appendChild(lowerValue);
            }
            if (getQualityOfServiceProfile().getAdaptationGoal(goalName).getClass() == ThresholdAdaptationGoal.class) {
                adaptationGoalElement.setAttribute("type", "threshold");
                Element threshold = doc.createElement("threshold");
                threshold.appendChild(doc.createTextNode(((ThresholdAdaptationGoal) getQualityOfServiceProfile().getAdaptationGoal(goalName)).getThreshold().toString()));
                adaptationGoalElement.appendChild(threshold);
            }
            Qos.appendChild(adaptationGoalElement);
        }


        inputProfileElement.appendChild(Qos);

        Element moteProbabilaties = doc.createElement("moteProbabilaties");
        for(Integer moteNumber : getProbabilitiesForMotesKeys()){
            Element moteElement = doc.createElement("mote");
            Element moteNumberElement = doc.createElement("moteNumber");
            moteNumberElement.appendChild(doc.createTextNode(Integer.toString(moteNumber+1)));
            moteElement.appendChild(moteNumberElement);
            Element activityProbability = doc.createElement("activityProbability");
            activityProbability.appendChild(doc.createTextNode(getProbabilityForMote(moteNumber).toString()));
            moteElement.appendChild(activityProbability);
            moteProbabilaties.appendChild(moteElement);

        }
        inputProfileElement.appendChild(moteProbabilaties);
        gui.updateInputProfilesFile();

    }

    /**
     * Puts the adaptation goal for reliable communications with the name in the map of the QualityOfServiceProfile and updates the file.
     * @param name The name of the adaptationGoal.
     * @param adaptationGoal The AdaptationGoal to put in the map.
     */
    public void putAdaptationGoal(String name,AdaptationGoal adaptationGoal) {
        this.getQualityOfServiceProfile().putAdaptationGoal(name,adaptationGoal);
        updateFile();
    }

}
