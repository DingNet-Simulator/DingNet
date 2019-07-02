package SelfAdaptation.Instrumentation;

import IotDomain.Mote;

/**
 * A class to allow self-adaptation software to edit mote settings.
 */
public class MoteEffector {

    /**
     * Constructs a MoteEffector.
     */
    public MoteEffector(){
    }

    /**
     * A method to set the power of a mote.
     * @param mote The mote to set the power of.
     * @param power The power to set.
     */
    public void setPower(Mote mote, Integer power){
        mote.setTransmissionPower(power);
    }

    /**
     * A method to set the spreading factor of a mote.
     * @param mote The mote to set the spreading factor of.
     * @param spreadingFactor The spreading factor to set.
     */
    public void setSpreadingFactor(Mote mote, Integer spreadingFactor){
        mote.setSF(spreadingFactor);
    }

    /**
     * A method to set the sampling rate of a mote.
     * @param mote The mote to set the sampling rate of.
     * @param samplingRate The sampling rate to set.
     */
    public void setSamplingRate(Mote mote, Integer samplingRate){
        mote.setSamplingRate(samplingRate);
    }

    /**
     * A method to set the movement speed of a mote.
     * @param mote The mote to set the movement speed of.
     * @param movementSpeed The movement speed to set.
     */
    public void setMovementSpeed(Mote mote, Double movementSpeed){
        mote.setMovementSpeed(movementSpeed);
    }

    /**
     * A method to set the energy level of a mote.
     * @param mote The mote to set the energy level of.
     * @param energyLevel The energy level to set.
     */
    public void setEnergyLevel(Mote mote, Integer energyLevel){
        mote.setEnergyLevel(energyLevel);
    }


}
