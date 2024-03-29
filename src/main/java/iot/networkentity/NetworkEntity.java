package iot.networkentity;


import iot.Environment;
import iot.lora.*;
import iot.networkcommunication.api.Receiver;
import iot.networkcommunication.api.Sender;
import iot.networkcommunication.impl.ReceiverWaitPacket;
import iot.networkcommunication.impl.SenderNoWaitPacket;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;
import util.Pair;
import util.Statistics;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * An  abstract class representing an entity active in the LoraWan network
 */
public abstract class NetworkEntity implements Serializable {
    // EUI of the network entity
    private static final long serialVersionUID = 1L;

    // An unsinged long representing the 64 bit unique identifier.
    private final long EUI;


    // NOTE: The x and y coordinates below (in double format) are NOT geo coordinates
    //       Rather, they represent the (x,y) coordinates of the grid of the environment (specified in configuration files)
    // FIXME: adjust the simulator to completely use geographical coordinates
    private GeoPosition pos;

    protected GeoPosition initialPosition;


    // The transmission power of the entity.
    private int transmissionPower;

    // The levels of power in between which it can discriminate.
    private final double transmissionPowerThreshold;

    // If the mote is enabled in the current simulation.
    private boolean enabled;

    private final List<RegionalParameter> regionalParameters = EU868ParameterByDataRate.valuesAsList();

    // strategy to send a LoRa packet
    private Sender sender;

    // strategy to receive a LoRa packet
    private Receiver receiver;

    private Environment environment;

    /**
     *  A constructor generating a Network with a given x-position, y-position, spreading factor, transmission power (threshold) and environment.
     * @param xPos  The x-coordinate of the entity on the map.
     * @param yPos  The y-coordinate of the entity on the map.
     * @param transmissionPower   The transmission power of the entity.
     * @param SF    The spreading factor of the entity.
     * @param transmissionPowerThreshold The threshold for discriminating different transmissions.
     * @param environment The environment to which the entity belongs.
     */
    NetworkEntity(long EUI, double xPos, double yPos, int transmissionPower, int SF, double transmissionPowerThreshold, Environment environment) {
        this.environment = environment;
        this.pos = getEnvironment().getMapHelper().toGeoPosition(xPos,yPos);
        this.initialPosition = pos;

        this.transmissionPower = isValidTransmissionPower(transmissionPower) ? transmissionPower : 0;

        this.transmissionPowerThreshold = transmissionPowerThreshold;
        this.EUI = EUI;

        enabled = true;
        receiver = new ReceiverWaitPacket(this, transmissionPowerThreshold, environment.getClock()).setConsumerPacket(this::receive);
        sender = new SenderNoWaitPacket(this, environment)
            .setRegionalParameter(regionalParameters.stream().filter(r -> r.getSpreadingFactor() == SF).findFirst().orElseThrow())
            .setTransmissionPower(transmissionPower);
    }

    /**
     * Returns the transmission power threshold.
     * @return The transmission power threshold.
     */
    public double getTransmissionPowerThreshold() {
        return transmissionPowerThreshold;
    }

    /**
     * Checks if a transmission power is valid.
     * @param transmissionPower The transmission power to check.
     * @return true if the transmission power is valid. False otherwise.
     */
    private static boolean isValidTransmissionPower(int transmissionPower) {
        return true;
    }

    public void setTransmissionPower(int transmissionPower) {
        this.transmissionPower = transmissionPower;
        sender.setTransmissionPower(transmissionPower);
    }

    /**
     *  Returns The transmission power of the entity.
     * @return The transmission power of the entity.
     */
    public int getTransmissionPower() {
        return transmissionPower;
    }

    /**
     * A method for receiving a packet, which checks if it can detect the packet and then adds it to the received packets.
     * @param transmission The transmission to receive.
     */
    private void receive(LoraTransmission transmission) {
        Statistics.getInstance().addReceivedTransmissionsEntry(this.getEUI(), transmission);
        if (!transmission.isCollided()) {
            handleMacCommands(transmission.getContent());
            OnReceive(transmission);
        }
    }

    /**
     * A function for handling MAC commands.
     * @param packet the packets with MAC commands
     */
    public void handleMacCommands(LoraWanPacket packet) {
        List<Byte> payload = new LinkedList<>(Arrays.asList(Converter.toObjectType(packet.getPayload())));
        List<Byte> variables = new LinkedList<>();
        for (MacCommand command : packet.getMacCommands()) {
            for (int i = 0; i < command.getLength(); i++) {
                variables.add(payload.get(i));
            }
            payload.removeAll(variables);
            switch (command) {
                case ResetInd:
                case ResetConf:
                case RekeyInd:
                case RekeyConf:
                case LinkADRAns:
                case LinkADRReq:
                case DevStatusAns:
                case DevStatusReq:
                case DlChannelAns:
                case DlChannelReq:
                case DutyCycleAns:
                case DutyCycleReq:
                case LinkCheckAns:
                case LinkCheckReq:
                case DeviceTimeAns:
                case DeviceTimeReq:
                case NewChannelAns:
                case NewChannelReq:
                case ForceRejoinReq:
                case RXParamSetupAns:
                case RXParamSetupReq:
                case TxParamSetupAns:
                case TxParamSetupReq:
                case ADRParamSetupAns:
                case ADRParamSetupReq:
                case RXTimingSetupAns:
                case RXTimingSetupReq:
                case RejoinParamSetupAns:
                case RejoinParamSetupReq:
            }
        }
    }

    /**
     * A method describing what the entity should do after successfully receiving a transmission.
     * @param transmission The received transmission.
     */
    protected abstract void OnReceive(LoraTransmission transmission);




    public double getLatPos() {
        return pos.getLatitude();
    }



    public void setLatPos(double latPos) {
        this.pos = new GeoPosition(latPos,pos.getLongitude());
    }

    public double getLongPos() {
        return pos.getLongitude();
    }

    public void setLongPos(double longPos) {
        this.pos = new GeoPosition(pos.getLatitude(),longPos);

    }



    public GeoPosition getPos() {
        return pos;
    }

    public GeoPosition getOriginalPos() {
        return this.initialPosition;
    }

    public void setPos(GeoPosition pos) {
        this.pos = pos;
    }

    public void updateInitialPosition(GeoPosition position) {
        if (initialPosition.equals(pos)) {
            this.pos = position;
        }
        this.initialPosition = position;
    }

    /**
     *  Returns The spreading factor.
     * @return The spreading factor.
     */
    public int getSF() {
        return this.sender.getRegionalParameter().getSpreadingFactor();
    }

    /**
     * Checks if a spreading factor is valid and then sets it to the spreading factor.
     * @param SF the spreading factor to set.
     */
    public void setSF(int SF) {
        this.sender.setRegionalParameter(regionalParameters.stream().filter(r -> r.getSpreadingFactor() == SF)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new));
    }



    protected Environment getEnvironment() {
        return this.environment;
    }


    /**
     * A method which sends a message to all gateways in the environment
     * @param message The message to send.
     */
    protected void send(LoraWanPacket message) {
        Environment environment = this.getEnvironment();

        var recs = Stream.concat(environment.getGateways().stream(), environment.getMotes().stream())
            .filter(ne -> filterLoraSend(ne, message))
            .map(NetworkEntity::getReceiver)
            .collect(Collectors.toSet());
        sender.send(message, recs)
            .ifPresent(t -> {
                Statistics statistics = Statistics.getInstance();
                statistics.addPowerSettingEntry(this.getEUI(), environment.getClock().getTime().toEpochSecond(ZoneOffset.UTC), getTransmissionPower());
                statistics.addSpreadingFactorEntry(this.getEUI(), this.getSF());
                statistics.addSentTransmissionsEntry(this.getEUI(), t);
            });
    }

    public Receiver getReceiver() {
        return receiver;
    }

    /**
     * Returns the unique identifier.
     * @return the unique identifier.
     */
    public long getEUI() {
        return EUI;
    }


    /**
     * Returns if the entity is enabled in this run.
     * @return If the entity is enabled in this run.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the entity.
     * @param enabled If the entity is enabled.
     */
    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     *
     * @param networkEntity the receiver
     * @param packet the packet to send
     * @return true if the packet has to be sent
     */
    abstract boolean filterLoraSend(NetworkEntity networkEntity, LoraWanPacket packet);


    /**
     * Method which is called at the start of every simulation run.
     */
    protected abstract void initialize();

    public void reset() {
        receiver.reset();
        sender.reset();

        this.initialize();
    }

    public Sender getSender(){
        return sender;
    }
}
