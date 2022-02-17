package iot.networkentity;

import iot.Environment;
import iot.lora.EU868ParameterByDataRate;
import iot.lora.LoraWanPacket;
import iot.lora.MacCommand;
import iot.lora.MessageType;
import iot.strategy.consume.ChangeSettings;
import iot.strategy.consume.ReplacePath;
import util.MoteSettings;
import util.Pair;
import util.Path;
import util.buffer.Buffer;
import util.buffer.ExpiringBuffer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LifeLongMote extends Mote{


    public LifeLongMote(long DevEUI, double xPos, double yPos, int transmissionPower,
                        int SF, List<MoteSensor> moteSensors, int energyLevel, Path path,
                        double movementSpeed, int startMovementOffset, int periodSendingPacket,
                        int startSendingOffset, Environment environment, int transmittingInterval,
                        int expirationTime) {
        super(DevEUI, xPos, yPos, transmissionPower, SF, moteSensors, energyLevel, path,
            movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset,
            environment);
        this.transmittingInterval = transmittingInterval;
        this.isAllowedToTransmit = true;
        this.expirationTime = expirationTime;
    }

    public LifeLongMote(long DevEUI, double xPos, double yPos, int transmissionPower, int SF,
                        List<MoteSensor> moteSensors, int energyLevel, Path path,
                        double movementSpeed, Environment environment, int transmittingInterval,
                        int expirationTime) {
        super(DevEUI, xPos, yPos, transmissionPower, SF, moteSensors, energyLevel, path,
            movementSpeed, environment);
        this.transmittingInterval = transmittingInterval;
        this.isAllowedToTransmit = true;
        this.expirationTime = expirationTime;
    }
    private int expirationTime;
    private ExpiringBuffer<Pair<LoraWanPacket, LocalDateTime>> packetBuffer;
    private boolean isAllowedToTransmit;
    private byte packetCounter;
    private int transmittingInterval;

    public int getTransmittingInterval() {
        return transmittingInterval;
    }


    public ExpiringBuffer<Pair<LoraWanPacket, LocalDateTime>> getPacketBuffer() {
        return packetBuffer;
    }

    public boolean allowedToTransmit() {
        return isAllowedToTransmit;
    }

    public void allowTransmission(){
        isAllowedToTransmit = true;
        transmit();
    }

    private void transmit(){
        if (allowedToTransmit()) {
            if (!packetBuffer.isEmpty()) {
                Pair<LoraWanPacket, LocalDateTime> packetInfo = packetBuffer.retrieve();
                LoraWanPacket packet = packetInfo.getLeft();
                if(packet.getPayload().length + 5 < EU868ParameterByDataRate.valuesAsList().stream().filter(r -> r.getSpreadingFactor() == getSF()).findFirst().orElseThrow().getMaximumPayloadSize()) {
                    byte[] newPayload = new byte[packet.getPayload().length + 5];
                    for (int i = 0; i < packet.getPayload().length; i++) {
                        newPayload[i] = packet.getPayload()[i];
                    }
                    newPayload[newPayload.length - 5] = (byte) (getTransmittingInterval()/5);
                    newPayload[newPayload.length - 4] = (byte) (getExpirationTime()/5);
                    newPayload[newPayload.length - 3] = (byte) getTransmissionPower();
                    newPayload[newPayload.length - 2] = packetCounter;
                    newPayload[newPayload.length - 1] = (byte) (ChronoUnit.SECONDS.between(packetInfo.getRight(), getEnvironment().getClock().getTime()) * 100 / getExpirationTime());
                    LoraWanPacket newPacket = new LoraWanPacket(packet.getSenderEUI(), packet.getReceiverEUI(), newPayload, packet.getFrameHeader(), packet.getMacCommands());
                    super.sendToGateWay(newPacket);
                    isAllowedToTransmit = false;
                    packetCounter = (byte) ((packetCounter + 1) % 100);
                }else{
                    super.sendToGateWay(packet);
                }
            }
            getEnvironment().getClock().addTriggerOneShot(
                getEnvironment().getClock().getTime().plusSeconds(getTransmittingInterval()),
                this::allowTransmission
            );
        }
    }

    public void setTransmittingInterval(int newInterval){
        this.transmittingInterval = newInterval;
    }


    /**
     * A function for sending a packet to the gateways.
     * @param packet the packet to send
     */
    @Override
    public void sendToGateWay(LoraWanPacket packet) {
        packetBuffer.add(new Pair<>(packet,getEnvironment().getClock().getTime()));
        transmit();
    }

    @Override
    protected void initialize() {
        super.initialize();
        packetBuffer= new ExpiringBuffer<>(getEnvironment().getClock(),expirationTime);
        packetCounter = 0;
        isAllowedToTransmit = true;
        consumePacketStrategies.add(new ChangeSettings());


    }


    public void setExpirationTime(int value) {
        if(value > 0) {
            this.expirationTime = value;
            packetBuffer = new ExpiringBuffer<>(getEnvironment().getClock(), expirationTime);
        }
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setSettings(MoteSettings settings) {
        setTransmittingInterval(settings.getTransmissionInterval());
        setTransmissionPower(settings.getTransmissionPower());
        setExpirationTime(settings.getExpirationTime());
    }

    public void adjustSettings(MoteSettings settings) {
        setTransmittingInterval(Math.max(5,getTransmittingInterval()+settings.getTransmissionInterval()));
        setTransmissionPower(Math.min(14,Math.max(0,getTransmissionPower()+settings.getTransmissionPower())));
        setExpirationTime(Math.max(5,getTransmittingInterval()+settings.getExpirationTime()));
    }
}
