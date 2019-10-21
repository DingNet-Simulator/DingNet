package IotDomain;

import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Model;
import be.kuleuven.cs.som.annotate.Raw;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;


/**
 * A class representing the energy bound and moving motes in the network.
 */
public class Mote extends NetworkEntity {
    /**
     * Returns the mote sensors of the mote.
     * @return The mote sensors of the mote.
     */
    @Basic
    public LinkedList<MoteSensor> getSensors() {
        return moteSensors;
    }

    /**
     * A LinkedList MoteSensors representing all sensors on the mote.
     */
    @Model
    private LinkedList<MoteSensor> moteSensors = new LinkedList<>();
    /**
     * A LinkedList of GeoPositions representing the path the mote will follow.
     */
    @Model
    private LinkedList<GeoPosition> path;

    /**
     * An integer representing the energy level of the mote.
     */
    @Model
    private Integer energyLevel;
    /**
     * An integer representing the sampling rate of the mote.
     */
    @Model
    private Integer samplingRate;
    /**
     * An integer representing the number of requests for data of the mote.
     */
    @Model
    private Integer numberOfRequests;
    /**
     * A Double representing the movement speed of the mote.
     */
    @Model
    private Double movementSpeed;
    /**
     * An integer representing the start offset of the mote.
     */
    @Model
    private Integer startOffset;

    /**
     * A constructor generating a node with a given x-coordinate, y-coordinate, environment, transmitting power
     * spreading factor, list of MoteSensors, energy level, path, sampling rate, movement speed and start offset.
     * @param DevEUI The device's unique identifier
     * @param xPos  The x-coordinate of the node.
     * @param yPos  The y-coordinate of the node.
     * @param environment   The environment of the node.
     * @param SF    The spreading factor of the node.
     * @param transmissionPower The transmitting power of the node.
     * @param moteSensors The mote sensors for this mote.
     * @param energyLevel The energy level for this mote.
     * @param path The path for this mote to follow.
     * @param samplingRate The sampling rate of this mote.
     * @param movementSpeed The movement speed of this mote.
     * @param startOffset The start offset of this mote.
     */
    @Raw
    public Mote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower,
                Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path, Integer samplingRate, Double movementSpeed, Integer startOffset){
       super(DevEUI, xPos,yPos, environment,transmissionPower,SF,1.0);
        environment.addMote(this);
        OverTheAirActivation();
        this.moteSensors = moteSensors;
        this.path = path;
        this.energyLevel = energyLevel;
        this.samplingRate = samplingRate;
        numberOfRequests = samplingRate;
        this.movementSpeed = movementSpeed;
        this.startOffset = startOffset;

    }

    /**
     * A constructor generating a node with a given x-coordinate, y-coordinate, environment, transmitting power
     * spreading factor, list of MoteSensors, energy level, path, sampling rate and movement speed and  random start offset.
     * @param DevEUI The device's unique identifier
     * @param xPos  The x-coordinate of the node.
     * @param yPos  The y-coordinate of the node.
     * @param environment   The environment of the node.
     * @param SF    The spreading factor of the node.
     * @param transmissionPower The transmitting power of the node.
     * @param moteSensors The mote sensors for this mote.
     * @param energyLevel The energy level for this mote.
     * @param path The path for this mote to follow.
     * @param samplingRate The sampling rate of this mote.
     * @param movementSpeed The movement speed of this mote.
     */
    @Raw
    public Mote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower,
                Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path, Integer samplingRate, Double movementSpeed){
        this(DevEUI,xPos,yPos, environment,transmissionPower,SF,moteSensors,energyLevel,path,samplingRate, movementSpeed,Math.abs((new Random()).nextInt(5)));
    }

    /**
     * A method describing what the mote should do after successfully receiving a packet.
     * @param packet The received packet.
     * @param senderEUI The EUI of the sender
     * @param designatedReceiver The EUI designated receiver for the packet.
     */
    @Override
    protected void OnReceive(Byte[] packet, Long senderEUI, Long designatedReceiver) {

    }

    /**
     * a function for the OTAA protocol.
     */
    public void OverTheAirActivation(){
    }

    /**
     * Returns the path of the mote.
     * @return The path of the mote.
     */
    @Basic
    public LinkedList<GeoPosition> getPath() {
        return path;
    }

    /**
     * Sets the path of the mote to a given path.
     * @param path The path to set.
     */
    @Basic
    public void setPath(LinkedList<GeoPosition> path) {
        this.path = path;
    }

    /**
     * A function for sending a message with MAC commands to the gateways.
     * @param data The data to send in the message
     * @param macCommands the MAC commands to include in the message.
     */
    public void sendToGateWay(Byte[] data, HashMap<MacCommand,Byte[]> macCommands){
        Byte[] payload = new Byte[data.length+macCommands.size()];
        int i = 0;
        for(MacCommand key : macCommands.keySet()){
            for(Byte dataByte : macCommands.get(key)){
            payload[i] = dataByte;
            i++;
            }
        }
        for(int j =0; j< data.length;j++){
            payload[i] = data[j];
            i++;
        }

        LoraWanPacket packet = new LoraWanPacket(getEUI(), (long) 1,payload, new LinkedList<>(macCommands.keySet()));
        loraSend(packet);
    }

    /**
     * Returns the energy level of the mote.
     * @return The energy level of the mote.
     */
    @Basic
    public Integer getEnergyLevel(){
        return this.energyLevel;
    }

    /**
     * Sets the energy level of the mote.
     * @param energyLevel The energy level to set.
     */
    @Basic
    public void setEnergyLevel(Integer energyLevel) {
        this.energyLevel = energyLevel;
    }

    /**
     * Sets the mote sensors of the mote.
     * @param moteSensors the mote sensors to set.
     */
    @Basic
    public void setSensors(LinkedList<MoteSensor> moteSensors) {
        this.moteSensors = moteSensors;
    }

    /**
     * Returns the sampling rate of the mote.
     * @return The sampling rate of the mote.
     */
    @Basic
    public Integer getSamplingRate() {
        return samplingRate;
    }

    /**
     * Returns the number of requests for data.
     * @return The number of requests for data.
     */
    @Basic
    public Integer getNumberOfRequests() {
        return numberOfRequests;
    }

    /**
     * Sets the sampling rate of the mote.
     * @param samplingRate The sampling rate of the mote
     */
    @Basic
    public void setSamplingRate(Integer samplingRate){
        this.samplingRate = samplingRate;
        setNumberOfRequests(getSamplingRate());
    }

    /**
     * Sets the number of requests for data.
     * @param numberOfRequests The number of requests for data.
     */
    @Model
    private void setNumberOfRequests(Integer numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

    /**
     * Returns if a mote should send data on this request.
     * @return true if the number of request since last answer is the sampling rate.
     * @return false otherwise.
     */
    public boolean shouldSend(){
        if(getNumberOfRequests() == 0){
            setNumberOfRequests(getSamplingRate());
            return true;
        }
        else{
            setNumberOfRequests(getNumberOfRequests()-1);
            return false;
        }
    }

    /**
     * Returns the movementSpeed of the mote.
     * @return The movementSpeed of the mote.
     */
    @Basic
    public Double getMovementSpeed() {
        return movementSpeed;
    }

    /**
     * Sets the movement speed of the mote.
     * @param movementSpeed The movement speed of the mote.
     */
    @Basic
    public void setMovementSpeed(Double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    /**
     * Returns the start offset of the mote.
     * @return the start offset of the mote.
     */
    @Basic
    public Integer getStartOffset(){
        return this.startOffset;
    }
}
