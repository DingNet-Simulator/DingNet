package IotDomain;

import IotDomain.motepacketstrategy.consumeStrategy.ConsumePacketStrategy;
import IotDomain.motepacketstrategy.consumeStrategy.DummyConsumer;
import IotDomain.motepacketstrategy.storeStrategy.ReceivedPacketStrategy;
import IotDomain.motepacketstrategy.storeStrategy.StoreAllMessage;
import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Model;
import be.kuleuven.cs.som.annotate.Raw;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;


/**
 * A class representing the energy bound and moving motes in the network.
 */
public class Mote extends NetworkEntity {
    /**
     * Returns the mote sensors of the mote.
     * @return The mote sensors of the mote.
     */
    @Basic
    public List<MoteSensor> getSensors() {
        return moteSensors;
    }

    /**
     * A LinkedList MoteSensors representing all sensors on the mote.
     */
    @Model
    private List<MoteSensor> moteSensors;
    /**
     * A LinkedList of GeoPositions representing the path the mote will follow.
     */
    @Model
    private List<GeoPosition> path;

    /**
     * An integer representing the energy level of the mote.
     */
    @Model
    private Integer energyLevel;

    /**
     * A Double representing the movement speed of the mote.
     */
    @Model
    private Double movementSpeed;
    /**
     * An integer representing the start offset of the mote in seconds.
     */
    @Model
    private Integer startMovementOffset;

    //TODO add comments and constructor for these parameters
    //both in seconds
    private static final int DEFAULT_START_SENDING_OFFSET = 1;
    private static final int DEFAULT_PERIOD_SENDING_PACKET = 20;
    /**
     * time to await before send the first packet (in seconds)
     */
    private final int startSendingOffset;
    /**
     * period to define how many seconds the mote has to send a packet (in seconds)
     */
    private int periodSendingPacket;

    private static final long DEFAULT_APPLICATION_EUI = 1;
    /**
     * application identifier
     */
    private long applicationEUI = DEFAULT_APPLICATION_EUI;

    private final ReceivedPacketStrategy receivedPacketStrategy = new StoreAllMessage();

    private final List<ConsumePacketStrategy> consumePacketStrategies = List.of(new DummyConsumer());

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
     * @param movementSpeed The movement speed of this mote.
     * @param startMovementOffset The start offset of this mote (in seconds).
     * @param periodSendingPacket period to define how many seconds the mote has to send a packet (in seconds)
     * @param startSendingOffset time to await before send the first packet (in seconds)
     */
    @Raw
    public Mote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower,
                Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path,
                Double movementSpeed, Integer startMovementOffset, int periodSendingPacket, int startSendingOffset){
       super(DevEUI, xPos,yPos, environment,transmissionPower,SF,1.0);
        environment.addMote(this);
        OverTheAirActivation();
        this.moteSensors = moteSensors;
        this.path = path;
        this.energyLevel = energyLevel;
        this.movementSpeed = movementSpeed;
        this.startMovementOffset = startMovementOffset;
        this.periodSendingPacket = periodSendingPacket;
        this.startSendingOffset = startSendingOffset;
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
        this(DevEUI,xPos,yPos, environment,transmissionPower,SF,moteSensors,energyLevel,path, movementSpeed,
            Math.abs((new Random()).nextInt(5)), DEFAULT_PERIOD_SENDING_PACKET, DEFAULT_START_SENDING_OFFSET);
    }

    /**
     * A method describing what the mote should do after successfully receiving a packet.
     * @param packet The received packet.
     */
    @Override
    protected void OnReceive(LoraWanPacket packet) {
        //if is a message sent to from a gateway to this mote
        if (getEUI().equals(packet.getDesignatedReceiverEUI()) &&
            getEnvironment().getGateways().stream().anyMatch(m -> m.getEUI().equals(packet.getSenderEUI()))) {
            receivedPacketStrategy.addReceivedMessage(packet);
        }
    }

    @Override
    boolean filterLoraSend(NetworkEntity networkEntity, LoraWanPacket packet) {
        return !networkEntity.equals(this);
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
    public List<GeoPosition> getPath() {
        return path;
    }

    /**
     * Sets the path of the mote to a given path.
     * @param path The path to set.
     */
    @Basic
    public void setPath(List<GeoPosition> path) {
        this.path = path;
    }

    /**
     *
     * @return ID of application to send the package to
     */
    public long getApplicationEUI() {
        return applicationEUI;
    }

    /**
     * A function for sending a message with MAC commands to the gateways.
     * @param data The data to send in the message
     * @param macCommands the MAC commands to include in the message.
     */
    public void sendToGateWay(Byte[] data, HashMap<MacCommand,Byte[]> macCommands){
        var packet = composePacket(data, macCommands);
        if (packet.getPayload().length > 1) {
            loraSend(packet);
        }
    }

    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand,Byte[]> macCommands) {
        Byte[] payload = new Byte[data.length+macCommands.size()+1];
        payload[0] = 0;
        if (payload.length > 1) {
            int i = 1;
            for (MacCommand key : macCommands.keySet()) {
                for (Byte dataByte : macCommands.get(key)) {
                    payload[i] = dataByte;
                    i++;
                }
            }
            for (int j = 0; j < data.length; j++) {
                payload[i] = data[j];
                i++;
            }
        }
        return new LoraWanPacket(getEUI(), getApplicationEUI(), payload, new LinkedList<>(macCommands.keySet()));
    }

    /**
     * consume all the packet arrived with the strategies previous defined
     */
    public void consumePackets() {
        if (receivedPacketStrategy.hasPackets()) {
            var packets = receivedPacketStrategy.getReceivedPacket();
            consumePacketStrategies.forEach(s -> s.consume(this, packets));
        }
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
     * Returns the start offset of the mote in seconds.
     * @return the start offset of the mote in seconds.
     */
    @Basic
    public Integer getStartMovementOffset(){
        return this.startMovementOffset;
    }

    /**
     *
     * @return time to await before send the first packet (in seconds)
     */
    public int getStartSendingOffset() {
        return startSendingOffset;
    }

    /**
     *
     * @return period to define how many seconds the mote has to send a packet (in seconds)
     */
    public int getPeriodSendingPacket() {
        return periodSendingPacket;
    }
}
