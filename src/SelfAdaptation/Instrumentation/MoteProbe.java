package SelfAdaptation.Instrumentation;

import IotDomain.*;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;

import java.util.LinkedList;

/**
 * A class representing methods for probing.
 */
public class MoteProbe {
    /**
     * A list with feedBackLoops using the probe.
     */
    private GenericFeedbackLoop genericFeedbackLoop;

    /**
     * Constructs a MoteProbe with no FeedBackLoops using it.
     */
    public MoteProbe(){

    }

    /**
     * Returns a list of GenericFeedbackLoops using the probe.
     * @return  A list of GenericFeedbackLoops using the probe.
     */
    public GenericFeedbackLoop getGenericFeedbackLoop() {
        return genericFeedbackLoop;
    }

    /**
     * Sets a GenericFeedbackLoop using the probe.
     * @param genericFeedbackLoop The FeedbackLoop to set.
     */
    public void setGenericFeedbackLoop(GenericFeedbackLoop genericFeedbackLoop){
        this.genericFeedbackLoop =genericFeedbackLoop;
    }

    /**
     * Returns the highest received signal by any of the gateways for a given mote .
     * @param mote The mote to generate the graph of.
     * @return The highest received signal
     */
    public Double getHighestReceivedSignal(Mote mote) {
        LinkedList<LoraTransmission> lastTransmissions = new LinkedList<>();
        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            Boolean placed = false;
            for (int i = gateway.getReceivedTransmissions(mote.getEnvironment().getNumberOfRuns()-1).size() - 1; i >= 0 && !placed; i--) {
                if (gateway.getReceivedTransmissions(mote.getEnvironment().getNumberOfRuns()-1).get(i).getSender() == mote) {
                    lastTransmissions.add(gateway.getReceivedTransmissions(mote.getEnvironment().getNumberOfRuns()-1).getLast());
                    placed = true;
                }
            }
        }
        LoraTransmission bestTransmission = lastTransmissions.getFirst();
        for (LoraTransmission transmission : lastTransmissions) {
            if (transmission.getTransmissionPower() > bestTransmission.getTransmissionPower())
                bestTransmission = transmission;
        }
        return bestTransmission.getTransmissionPower();
    }

    /**
     * Returns the spreading factor of a given mote.
     * @param mote The mote to generate the graph of.
     * @return the spreading factor of the mote
     */
    public Integer getSpreadingFactor(NetworkEntity mote) {
        return mote.getSF();
    }

    /**
     * Returns The distance to the nearest Gateway.
     * @param mote The given mote to find the shortest distance.
     * @return The distance to the nearest Gateway.
     */
    public Double getShortestDistanceToGateway(Mote mote) {
        LinkedList<LoraTransmission> lastTransmissions = new LinkedList<>();
        for(Gateway gateway :mote.getEnvironment().getGateways()){
            Boolean placed = false;
            for(int i = gateway.getReceivedTransmissions(mote.getEnvironment().getNumberOfRuns()-1).size()-1; i>=0 && !placed; i--) {
                if(gateway.getReceivedTransmissions(mote.getEnvironment().getNumberOfRuns()-1).get(i).getSender() == mote) {
                    lastTransmissions.add(gateway.getReceivedTransmissions(mote.getEnvironment().getNumberOfRuns()-1).getLast());
                    placed = true;
                }
            }
        }
        LoraTransmission bestTransmission = lastTransmissions.getFirst();
        for (LoraTransmission transmission : lastTransmissions){
            if(Math.sqrt(Math.pow(transmission.getReceiver().getYPos()-transmission.getYPos(),2)+
                    Math.pow(transmission.getReceiver().getXPos()-transmission.getXPos(),2))
                    < Math.sqrt(Math.pow(bestTransmission.getReceiver().getYPos()-bestTransmission.getYPos(),2)+
                    Math.pow(bestTransmission.getReceiver().getXPos()-bestTransmission.getXPos(),2)))
                bestTransmission = transmission;
        }
        return Math.sqrt(Math.pow(bestTransmission.getReceiver().getYPos()-bestTransmission.getYPos(),2)+
                Math.pow(bestTransmission.getReceiver().getXPos()-bestTransmission.getXPos(),2));

    }

    /**
     * Returns the power setting of a specific mote.
     * @param mote The mote to get the power setting of.
     * @return The power setting of the mote.
     */
    public Integer getPowerSetting(NetworkEntity mote) {

        return mote.getTransmissionPower();

    }

    /**
     * Triggers the feedback loop.
     * @param gateway
     * @param devEUI
     */
    public void trigger(Gateway gateway, Long devEUI){
        Boolean found = false;
        Mote sender = null;
        for (Mote mote :gateway.getEnvironment().getMotes()){
            if(mote.getEUI() == devEUI){
                sender = mote;
                found = true;
            }
        }
        if(found) {
                if(getGenericFeedbackLoop().isActive()) {
                    getGenericFeedbackLoop().adapt(sender, gateway);
                }
        }
    }

}
