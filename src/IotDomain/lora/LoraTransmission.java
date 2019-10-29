package IotDomain.lora;

import IotDomain.Characteristic;
import IotDomain.Environment;
import IotDomain.networkentity.NetworkEntity;
import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Immutable;
import be.kuleuven.cs.som.annotate.Model;
import util.Pair;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A class representing a packet in the LoraWan simulation.
 */
public class LoraTransmission implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * A network entity representing the sender of the packet.
     */
    private final NetworkEntity sender;

    /**
     * A network entity representing the receiver of the packet.
     */
    private final NetworkEntity receiver;

    /**
     * A double representing the transmission power.
     */
    private Double transmissionPower = 0.0;

    /**
     * An integer representing the bandwidth.
     */
    private final Integer bandwidth;

    /**
     * An integer representing the spreading factor.
     */
    private final Integer spreadingFactor;
    /**
     * An integer representing the x-coordinate of the packet.
     */
    private Integer xPos = 0;

    /**
     * An integer representing the y-coordinate of the packet.
     */
    private Integer yPos = 0;


    /**
     * The environment in which the packet is being sent.
     */
    private final Environment environment;

    /**
     * The content of the message.
     */
    private final LoraWanPacket content;

    /**
     * A Random necessary for the gaussian in the model.
     */
    private final Random random = new Random();

    private final List<RegionalParameters> regionalParameters = EU868ParameterByDataRate.valuesAsList();

    /**
     * The departure time of the message
     */
    private final LocalTime departureTime;

    /**
     * Returns the departure time of the transmission.
     * @return  The departure time of the transmission.
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Returns the time on air.
     * @return  The time on air.
     */
    public Double getTimeOnAir() {
        return timeOnAir;
    }

    /**
     * The time on air of a transmission.
     */
    private final Double timeOnAir;
    /**
     * The path travelled by the transmission.
     */
    private LinkedList<Pair<Integer,Integer>> usedPath;

    /**
     * A constructor generating a transmission with a given sender, receiver, transmission power, bandwidth, spreading factor,
     * environment and content.
     * @param sender    The sender sending the transmission.
     * @param receiver The receiver receiving the transmission.
     * @param transmissionPower The transmission power of the transmission.
     * @param bandwidth The bandwidth of the transmission.
     * @param spreadingFactor   The spreading factor of the transmission.
     * @param content The content of the transmission.
     * @Post    If the given transmission power is valid, it is set to that power.
     * @Post    If the given transmission power is not valid, it is set to 0.
     * @Post    If the given bandwidth is valid, it is set to that bandwidth.
     * @Post    If the given bandwidth is not valid, it is set to 0.
     * @Post    If the given spreading factor is valid, it is set to that spreading factor.
     * @Post    If the given spreading factor is not valid, it is set to 0.
     * @Post    If the given sender and receiver have the same environment the xPos is set to the given x position.
     * @Post    If the given sender and receiver have the same environment the yPos is set to the given y position.
     * @Post    If the given sender and receiver have the same environment the environment is set
     *          to the given environment.
     * @Post    If the given sender and receiver have the same environment the sender is set to the given sender.
     * @Post    If the given sender and receiver have the same environment the receiver is set to the given receiver.
     * @Post    If the given sender and receiver have a different environment the environment is se to null.
     * @Post    If the given sender and receiver have a different environment the sender is set to null.
     * @Post    If the given sender and receiver have a different environment the receiver is set to null.
     * @Post    If the given sender and receiver have a different environment the xPos is set to 0.
     * @Post    If the given sender and receiver have a different environment the yPos is set to 0.
     * @Effect   The departure time is set to LocalTime.now().
     */
    public LoraTransmission(NetworkEntity sender, NetworkEntity receiver, Integer transmissionPower, Integer bandwidth,
                            Integer spreadingFactor, LoraWanPacket content) {
        if(sender.getEnvironment() == receiver.getEnvironment()){
            this.sender = sender;
            this.receiver = receiver;
            this.environment = sender.getEnvironment();
            this.xPos = sender.getXPos();
            this.yPos = sender.getYPos();
        }
        else{
            this.sender = null;
            this.receiver = null;
            this.environment = null;
            this.xPos = 0;
            this.yPos = 0;
        }
        this.content = content;

        if(isValidTransmissionPower(transmissionPower)) {
            this.transmissionPower = Double.valueOf(transmissionPower);

        }
        if(isValidBandwidth(bandwidth)){
            this.bandwidth = bandwidth;
        }
        else
            this.bandwidth = 0;
        if(isValidSpreadingFactor(spreadingFactor)) {
            this.spreadingFactor = spreadingFactor;
        }
        else
            this.spreadingFactor = 0;

        departureTime = getEnvironment().getClock().getTime();
        /**
         * https://docs.google.com/spreadsheets/d/1voGAtQAjC1qBmaVuP1ApNKs1ekgUjavHuVQIXyYSvNc/edit#gid=0
         */
        timeOnAir = ((Math.pow(2,getSpreadingFactor())/getBandwidth())*(
                (8+Math.max(Math.ceil(
                        (8*getContent().getPayload().length-4*getSpreadingFactor()+28+16 - 20*(getContent().hasHeader()? 1: 0))
                                /4*(getSpreadingFactor() -2*(getContent().hasLowDataRateOptimization()?0:1)))
                        *getContent().getCodingRate(),0))
                        +getContent().getAmountOfPreambleSymbols()*4.25))/10;
        usedPath = new LinkedList<>();
    }


    /**
     *  Returns the sender of this transmission.
     * @return The sender of this transmission.
     */
    @Basic
    public NetworkEntity getSender() {
        return sender;
    }

    /**
     *  Returns the receiver of this transmission.
     * @return The receiver of this transmission.
     */
    @Basic
    public NetworkEntity getReceiver() {
        return receiver;
    }

    /**
     * Checks if a given transmission power is valid.
     * @param transmissionPower The transmission power to check.
     * @return  True if the transmission power is valid.
     */
    @Immutable
    private static boolean isValidTransmissionPower(double transmissionPower) {
        return true;
    }

    /**
     *  Returns The transmission power of this transmission.
     * @return The transmission power of this transmission.
     */
    @Basic
    public double getTransmissionPower() {
        return transmissionPower;
    }

    /**
     * Sets the transmission power of the transmission
     * @param transmissionPower The transmission power to set.
     * @Post Sets the transmission power to the given value if the given value is valid.
     * @Post Sets the transmission power to 0 if the given value is not valid.
     */
    @Model
    private void setTransmissionPower(double transmissionPower) {
        if (isValidTransmissionPower(transmissionPower)){
            this.transmissionPower = transmissionPower;
        }
        else
            this.transmissionPower = 0.0;
    }

    /**
     * Checks if a given bandwidth is valid.
     * @param bandwidth The bandwidth to check.
     * @return  True if the bandwidth is valid.
     */
    @Immutable
    private static boolean isValidBandwidth(Integer bandwidth) {
        return bandwidth > 0;
    }

    /**
     * Returns the bandwidth of the transmission.
     * @return  The bandwidth of the transmission.
     */
    public Integer getBandwidth() {
        return bandwidth;
    }

    /**
     * Checks if a given spreading factor is valid.
     * @param spreadingFactor   The spreading factor to check.
     * @return  True if the given factor is smaller than 13 and bigger than 6. False otherwise.
     */
    private boolean isValidSpreadingFactor(Integer spreadingFactor) {
        return spreadingFactor <= 12 && spreadingFactor >= 7;
    }

    /**
     * Returns the spreading factor.
     * @return The spreading factor.
     */
    public Integer getSpreadingFactor() {
        return spreadingFactor;
    }

    /**
     * A function to make this transmission depart to its destination.
     * @Effect  Moves the transmission to the receiver.
     * @Effect  Tells the receiver to receiveTransmission this transmission.
     */
    public void depart(){
        var par = regionalParameters.stream().filter(p -> p.getSpreadingFactor() == getSpreadingFactor() &&
            p.getBandwidth() == getBandwidth()).collect(Collectors.toList());
        if (par.isEmpty()) {
            throw new IllegalArgumentException("No regional parameter found with spreading factor: "
                + getSpreadingFactor() + " and bandwidth: " + getBandwidth());
        }
        var payloadSize = getContent().getPayload().length + getContent().getHeader().map(m -> m.getFOpts().length).orElse(0);
        if (par.stream().noneMatch(p -> p.getMaximumPayloadSize() >= payloadSize)) {
            throw new IllegalArgumentException("No regional parameter found with spreading factor: "
                + getSpreadingFactor() + " and bandwidth: " + getBandwidth() + " and payload size: " + payloadSize);
        }
        if(getReceiver() != null){
            moveTo(getReceiver().getXPos(),getReceiver().getYPos());
            getReceiver().receiveTransmission(this);
        }
    }

    /**
     * Moves a transmission to a given position, while adapting the transmission power.
     * @param xPos  The x-coordinate of the destination.
     * @param yPos  The y-coordinate of the destination.
     * @Effect  If the destination is more than 0 moves away from the current position, decrease the transmissionPower
     *          according to the model and the characteristics of the next position. Which is the position 1 move
     *          in the direction of the longest difference or in the x direction if the difference is equal.
     */
    private void moveTo(Integer xPos, Integer yPos){
        Integer xDist;
        Integer yDist;
        Integer xDir;
        Integer yDir;
        Characteristic characteristic;
        xDist = Math.abs(xPos - getXPos());
        yDist = Math.abs(yPos - getYPos());
        xDir = Integer.signum(xPos - getXPos());
        yDir = Integer.signum(yPos - getYPos());
        characteristic = getEnvironment().getCharacteristic(xPos, yPos);

        while (getTransmissionPower() > -300 && xDist + yDist > 0){
            xDist = Math.abs(xPos - getXPos());
            yDist = Math.abs(yPos - getYPos());
            xDir = Integer.signum(xPos - getXPos());
            yDir = Integer.signum(yPos - getYPos());
            characteristic = getEnvironment().getCharacteristic(xPos, yPos);

            if(xDist + yDist > 1){
                if(xDist >  2*yDist || yDist >  2*xDist){
                    setTransmissionPower(getTransmissionPower() - 10 * characteristic.getPathLossExponent() * (Math.log10(xDist + yDist) - Math.log10(xDist + yDist - 1)));
                    if(xDist >  2*yDist) {
                        xPos = xPos - xDir;
                    }
                    else{
                        yPos = yPos - yDir;
                    }
                }
                else {
                    setTransmissionPower(getTransmissionPower() - 10 * characteristic.getPathLossExponent() * (Math.log10(xDist + yDist) - Math.log10(xDist + yDist - Math.sqrt(2))));
                    xPos =xPos - xDir;
                    yPos = yPos - yDir;
                }
            }

            else if (xDist + yDist == 1){
                if(xDist >  yDist){
                    xPos = xPos - xDir;
                }
                else {
                    yPos = yPos - yDir;
                }
            }

        }
        setTransmissionPower(getTransmissionPower() - random.nextGaussian() * characteristic.getShadowFading());
    }



    /**
     * Returns the x-coordinate of the transmission.
     * @return The x-coordinate of the transmission.
     */
    public Integer getXPos() {
        return xPos;
    }

    /**
     * Returns the y-coordinate of the transmission.
     * @return The y-coordinate of the transmission.
     */
    public Integer getYPos() {
        return yPos;
    }

    /**
     * Returns the environment of this packet.
     * @return The environment of this packet.
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Returns the content of the transmission.
     * @return  the content of the transmission.
     */
    @Basic
    public LoraWanPacket getContent() {
        return content;
    }

    /**
     * Checks if a content is valid.
     * @param content The content to check.
     * @return  true if the content is valid.
     */
    @Immutable
    public static Boolean isValidContent(LoraWanPacket content){
        return true;
    }

    /**
     * Adds a Pair to the used path.
     * @param pair The Pair to add.
     */
    private void addToUsedPath(Pair<Integer,Integer> pair){
        usedPath.add(pair);
    }

    /**
     * Returns the used path.
     * @return The used path.
     */
    public LinkedList<Pair<Integer,Integer>> getUsedPath(){
        return usedPath;
    }

}
