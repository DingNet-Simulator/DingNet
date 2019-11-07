package IotDomain.networkentity;

import IotDomain.Environment;
import IotDomain.lora.LoraTransmission;
import IotDomain.lora.LoraWanPacket;
import IotDomain.lora.MacCommand;
import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Immutable;
import be.kuleuven.cs.som.annotate.Raw;
import util.Pair;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * An  abstract class representing an entity active in the LoraWan network
 */
public abstract class NetworkEntity implements Serializable{
    // EUI of the network entity
    private static final long serialVersionUID = 1L;

    // A boolean to know when the entity is transmitting
    private boolean isTransmitting;

    // A list representing the power setting of every transmission.
    private LinkedList<List<Pair<Integer,Integer>>> powerSettingHistory;

    // A list representing the spreading factor of every transmission.
    private LinkedList<List<Integer>> spreadingFactorHistory;

    // An unsinged long representing the 64 bit unique identifier.
    private final Long EUI;


    // NOTE: The x and y coordinates below (in double format) are NOT geo coordinates
    //       Rather, they represent the (x,y) coordinates of the grid of the environment (specified in configuration files)
    // FIXME: adjust the simulator to completely use geographical coordinates
    private double xPos = 0.0;
    private double yPos = 0.0;


    final Pair<Double, Double> initialPosition;

    // A map of the environment the entity is placed in.
    private Environment environment;

    // The transmission power of the entity.
    private int transmissionPower;

    // The spreading factor setting of the node.
    private int SF = 12;

    // The levels of power in between which it can discriminate.
    private final double transmissionPowerThreshold;

    // A map with the transmissions received by the entity and if they collided with an other packet.
    private LinkedList<LinkedHashMap<LoraTransmission,Boolean>> receivedTransmissions = new LinkedList<>();

    // A list with the transmissions transmitted by the entity
    private LinkedList<LinkedList<LoraTransmission>> sentTransmissions = new LinkedList<>();

    // If the mote is enabled in the current simulation.
    private Boolean enabled;

    /**
     *  A constructor generating a Network with a given x-position, y-position, environment and transmission power.
     * @param xPos  The x-coordinate of the entity on the map.
     * @param yPos  The y-coordinate of the entity on the map.
     * @param environment   The map of the environment.
     * @param transmissionPower   The transmission power of the entity.
     * @param SF    The spreading factor of the entity.
     * @param transmissionPowerThreshold The threshold for discriminating different transmissions.
     * @Post    If the x-coordinate was valid, it is set.
     * @Post    If the x-coordinate was not valid, it is set to 0.
     * @Post    If the y-coordinate was valid, it is set.
     * @Post    If the y-coordinate was not valid, it is set to 0.
     * @Post    If the transmission power was valid, it is set.
     * @Post    If the transmission power was not valid, it is set to 0.
     *
     */
    @Raw
    NetworkEntity(long EUI, double xPos, double yPos, Environment environment, int transmissionPower, int SF,
                  double transmissionPowerThreshold) {
        this.environment = environment;
        if (environment.isValidXpos(xPos)) {
            this.xPos = xPos;
        }
        if (environment.isValidYpos(yPos)) {
            this.yPos = yPos;
        }
        this.initialPosition = new Pair<>(xPos, yPos);

        this.transmissionPower = isValidTransmissionPower(transmissionPower) ? transmissionPower : 0;

        if (isValidSF(SF)) {
            this.SF = SF;
        }

        this.transmissionPowerThreshold = transmissionPowerThreshold;
        this.EUI = EUI;
        powerSettingHistory = new LinkedList<>();
        powerSettingHistory.add(new LinkedList<>());
        spreadingFactorHistory = new LinkedList<>();
        spreadingFactorHistory.add(new LinkedList<>());
        receivedTransmissions.add(new LinkedHashMap<>());
        sentTransmissions.add(new LinkedList<>());
        isTransmitting = false;
        enabled = true;
    }

    /**
     * Returns the transmission power threshold.
     * @return The transmission power threshold.
     */
    public Double getTransmissionPowerThreshold() {
        return transmissionPowerThreshold;
    }

    /**
     *
     * @return The environment of the entity.
     */
    @Basic
    @Raw
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Return the power setting history of the entity.
     * @return The power setting history of the entity.
     */
    public List<Pair<Integer,Integer>> getPowerSettingHistory(Integer run) {
        return powerSettingHistory.get(run);
    }

    /**
     * Return the spreading factor history of the entity.
     * @return The spreading factor history of the entity.
     */
    public List<Integer> getSpreadingFactorHistory(Integer run) {
        return spreadingFactorHistory.get(run);
    }

    /**
     *  Returns The transmissions sent by the entity.
     * @return The transmissions sent by the entity.
     */
    @Basic
    @Raw
    public LinkedList<LoraTransmission> getSentTransmissions(Integer run) {
        return sentTransmissions.get(run);
    }

    /**
     * Returns all transmissions with collisions included
     * @return all transmissions with collisions included
     */
    public LinkedHashMap<LoraTransmission,Boolean> getAllReceivedTransmissions(Integer run) {
        return receivedTransmissions.get(run);
    }

    /**
     * Returns only the transmission actually received by the gateway.
     * @return only the transmission actually received by the gateway.
     */
    public LinkedList<LoraTransmission> getReceivedTransmissions(Integer run) {
        LinkedList<LoraTransmission> transmissions = new LinkedList<>();
        for(LoraTransmission transmission : getAllReceivedTransmissions(run).keySet()) {
            if(!getAllReceivedTransmissions(run).get(transmission))
                transmissions.add(transmission);
        }
        return transmissions;
    }

    /**
     * Checks if a transmission power is valid.
     * @param transmissionPower The transmission power to check.
     * @return true if the transmission power is valid. False otherwise.
     */
    @Immutable
    private static boolean isValidTransmissionPower(Integer transmissionPower) {
        return true;
    }

    public void setTransmissionPower(Integer transmissionPower) {
        this.transmissionPower = transmissionPower;
    }

    /**
     *  Returns The transmission power of the entity.
     * @return The transmission power of the entity.
     */
    @Basic
    @Raw
    public int getTransmissionPower() {
        return transmissionPower;
    }

    /**
     * A method for receiving a packet, which checks if it can detect the packet and then adds it to the reeived packets.
     * @param transmission The transmission to receiveTransmission.
     * @Effect if the package has a high enough transmission power, it is added using packetStrengthHighEnough().
     */
    public void receiveTransmission(LoraTransmission transmission) {
        if(packetStrengthHighEnough(transmission)) {
            boolean collision = false;
            for (LoraTransmission receivedTransmission: getAllReceivedTransmissions(getEnvironment().getNumberOfRuns()-1).keySet()) {
                if(collision(transmission,receivedTransmission)) {
                    this.receivedTransmissions.getLast().put(receivedTransmission,true);
                    collision = true;
                    transmission.setCollided();
                    receivedTransmission.setCollided();
                }
            }
            receivedTransmissions.getLast().put(transmission,collision);
            getEnvironment().getClock().addTrigger(transmission.getDepartureTime().plus(transmission.getTimeOnAir().longValue(), ChronoUnit.MILLIS),()->{
                transmission.setArrived();
                if(!transmission.isCollided()) {
                    handleMacCommands(transmission.getContent());
                    OnReceive(transmission.getContent());
                }
                return LocalTime.of(0,0);
            });
        }

    }

    /**
     * A function for handling MAC commands.
     * @param packet the packets with MAC commands
     */
    public void handleMacCommands(LoraWanPacket packet) {
        LinkedList<Byte> payload = new LinkedList<>(Arrays.asList(packet.getPayload()));
        LinkedList<Byte> variables = new LinkedList<>();
        for(MacCommand command : packet.getMacCommands()) {
            for(Integer i = 0; i<command.getLength(); i++) {
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
     * A method describing what the entity should do after successfully receiving a packet.
     * @param packet The received packet.
     */
    protected abstract void OnReceive(LoraWanPacket packet);


    /**
     *  Returns The x-coordinate of the entity.
     * @return The x-coordinate of the entity.
     */
    @Basic
    @Raw
    public int getXPosInt() {
        return (int) xPos;
    }

    public double getXPosDouble() {
        return xPos;
    }

    /**
     * Checks if a new x-coordinate is valid and then sets that coordinate.
     * @param xPos The new x-coordinate.
     */
    @Basic
    public void setXPos(double xPos) {
        if (environment.isValidXpos(xPos)) {
            this.xPos = xPos;
        }
    }



    /**
     *  Returns The y-coordinate of the entity.
     * @return The y-coordinate of the entity.
     */
    @Basic
    @Raw
    public int getYPosInt() {
        return (int) yPos;
    }

    public double getYPosDouble() {
        return yPos;
    }

    /**
     * Checks if a new y-coordinate is valid and then sets that coordinate.
     * @param yPos The new y-coordinate.
     * @Post    If the new y-coordinate is valid, the new coordinate is set.
     */
    @Basic
    public void setYPos(double yPos) {
        if (environment.isValidYpos(yPos)) {
            this.yPos = yPos;
        }
    }

    public Pair<Integer, Integer> getPosInt() {
        return new Pair<>(getXPosInt(), getYPosInt());
    }

    public Pair<Integer, Integer> getOriginalPosInt() {
        return new Pair<>(this.initialPosition.getLeft().intValue(), this.initialPosition.getRight().intValue());
    }

    public void setPos(double xPos, double yPos) {
        setXPos(xPos);
        setYPos(yPos);
    }


    /**
     * Checks if a given Spreading factor is valid
     * @param SF The spreading factor to check.
     * @return  If the spreading factor is valid
     */
    @Immutable
    private static boolean isValidSF(int SF) {
        return SF >= 7 && SF <= 12;
    }

    /**
     *  Returns The spreading factor.
     * @return The spreading factor.
     */
    @Basic
    @Raw
    public int getSF() {
        return SF;
    }

    /**
     * Checks if a spreading factor is valid and then sets it to the spreading factor.
     * @param SF the spreading factor to set.
     */
    public void setSF(int SF) {
        if (isValidSF(SF)) {
            this.SF = SF;
        }
    }

    /**
     * A method which sends a message to all gateways in the environment
     * @param message The message to send.
     */
    void loraSend(LoraWanPacket message) {
        if (!isTransmitting) {
            powerSettingHistory.getLast().add(new Pair<>(getEnvironment().getClock().getTime().toSecondOfDay(),getTransmissionPower()));
            spreadingFactorHistory.getLast().add(getSF());
            List<LoraTransmission> packetsToSend = Stream.concat(
                getEnvironment().getGateways().stream(),
                getEnvironment().getMotes().stream())
                    .filter(ne -> filterLoraSend(ne, message))
                    .map(ne -> new LoraTransmission(this, ne, getTransmissionPower(), 125, getSF(), message))
                    .collect(Collectors.toList());
            if (!packetsToSend.isEmpty()) {
                sentTransmissions.getLast().add(packetsToSend.get(0));
            }
            packetsToSend.forEach(LoraTransmission::depart);
        }
    }

    /**
     * Checks if two packets collide according to the model
     * @param a The first packet.
     * @param b The second packet.
     * @return true if the packets collide, false otherwise.
     */
    private boolean collision(LoraTransmission a, LoraTransmission b) {
        return a.getSpreadingFactor().equals(b.getSpreadingFactor()) &&     //check spreading factor
            a.getTransmissionPower() - b.getTransmissionPower() < getTransmissionPowerThreshold() && //check transmission power
            Math.abs(Duration.between(a.getDepartureTime().plusNanos(a.getTimeOnAir().longValue() * 1000000 / 2), //check time on air
                    b.getDepartureTime().plusNanos(b.getTimeOnAir().longValue() * 1000000 / 2)).toNanos())
                    < a.getTimeOnAir().longValue() * 1000000 / 2 + b.getTimeOnAir().longValue() * 1000000 / 2;
    }

    /**
     * Checks if a transmission is strong enough to be received.
     * @param packet
     * @return
     */
    private boolean packetStrengthHighEnough(LoraTransmission packet) {
        return packet.getTransmissionPower() > -174 - 10 * Math.log10(packet.getBandwidth()) - (2.5 * packet.getSpreadingFactor() - 10);
    }

    /**
     * Returns the unique identifier.
     * @return the unique identifier.
     */
    public long getEUI() {
        return EUI;
    }

    public List<Double> getUsedEnergy(Integer run) {
        List<Double> usedEnergy = new LinkedList<>();
        int i= 0;
        for(LoraTransmission transmission: getSentTransmissions(run)) {
            usedEnergy.add(Math.pow(10,((double)getPowerSettingHistory(run).get(i).getRight())/10)*transmission.getTimeOnAir()/1000);
            i++;
        }
        return usedEnergy;
    }

    /**
     * Resets the received and sent transmissions and the power setting and the spreading factor history of the entity.
     */
    public void reset() {
        powerSettingHistory.clear();
        powerSettingHistory.add(new LinkedList<>());
        spreadingFactorHistory.clear();
        spreadingFactorHistory.add(new LinkedList<>());
        receivedTransmissions.clear();
        receivedTransmissions.add(new LinkedHashMap<>());
        sentTransmissions.clear();
        sentTransmissions.add(new LinkedList<>());
    }

    /**
     * Adds a new list to the received and sent transmissions and the power setting and the spreading factor history of the entity.
     */
    public void addRun() {
        powerSettingHistory.add(new LinkedList<>());
        spreadingFactorHistory.add(new LinkedList<>());
        receivedTransmissions.add(new LinkedHashMap<>());
        sentTransmissions.add(new LinkedList<>());
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
    public abstract void initialize();
}
