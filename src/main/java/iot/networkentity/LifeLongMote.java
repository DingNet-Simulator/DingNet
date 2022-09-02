package iot.networkentity;

import iot.environment.Environment;
import iot.lora.EU868ParameterByDataRate;
import iot.lora.LoraWanPacket;
import iot.strategy.consume.ChangeSettings;
import org.jfree.data.json.impl.JSONObject;
import util.MoteSettings;
import util.Pair;
import util.Path;
import util.buffer.ExpiringBuffer;

import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
        setTransmittingInterval(transmittingInterval);
        this.isAllowedToTransmit = true;
        setExpirationTime(expirationTime);
    }

    public LifeLongMote(long DevEUI, double xPos, double yPos, int transmissionPower, int SF,
                        List<MoteSensor> moteSensors, int energyLevel, Path path,
                        double movementSpeed, Environment environment, int transmittingInterval,
                        int expirationTime) {
        super(DevEUI, xPos, yPos, transmissionPower, SF, moteSensors, energyLevel, path,
            movementSpeed, environment);
        synchronized (this.transmittingInterval) {
            this.transmittingInterval = transmittingInterval;
        }
        this.isAllowedToTransmit = true;
        synchronized (this.expirationTime) {
            this.expirationTime = expirationTime;
        }
    }
    private Integer expirationTime = 0;
    private ExpiringBuffer<Pair<LoraWanPacket, LocalDateTime>> packetBuffer;
    private Boolean isAllowedToTransmit;
    private byte packetCounter;
    private Integer transmittingInterval = 0;

    public Integer getTransmittingInterval() {
        synchronized (transmittingInterval){
            return transmittingInterval;
        }
    }


    public ExpiringBuffer<Pair<LoraWanPacket, LocalDateTime>> getPacketBuffer() {
        return packetBuffer;
    }

    public boolean allowedToTransmit() {
        synchronized (isAllowedToTransmit) {
            return isAllowedToTransmit;
        }
    }

    public void allowTransmission(){
        synchronized (isAllowedToTransmit) {
            isAllowedToTransmit = true;
        }
            transmit();
    }
    public void disallowTransmission(){
        synchronized (isAllowedToTransmit) {
            isAllowedToTransmit = false;
        }
    }

    private void transmit(){
        if (allowedToTransmit()) {
            getEnvironment().getClock().addTriggerOneShot(
                getEnvironment().getClock().getTime().plusSeconds(getTransmittingInterval()),
                ()-> {
                    this.allowTransmission();
                }

            );
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
                    JSONObject moteJsonObject = new JSONObject();

                    List<JSONObject> tranmsissionList = new ArrayList<>();

                    List<Pair<Long,Pair<Double,Boolean>>> transmissionDataList = super.sendToGateWay(newPacket);
                    for (Pair<Long,Pair<Double,Boolean>> transmissionData : transmissionDataList){
                        JSONObject json = new JSONObject();
                        json.put("transmission_interval", getTransmittingInterval());
                        json.put("transmission_power_setting", getTransmissionPower());
                        json.put("expiration_time", getExpirationTime());
                        json.put("latency", newPayload[newPayload.length - 1]);
                        json.put("transmission_power", transmissionData.getRight().getLeft());
                        json.put("departure_time", getEnvironment().getClock().getTime().toString());
                        json.put("receiver",transmissionData.getLeft());
                        json.put("collided", transmissionData.getRight().getRight());
                        tranmsissionList.add(json);
                    }
                    moteJsonObject.put(getEUI(),tranmsissionList);
                    try {
                        FileOutputStream is = new FileOutputStream(getEnvironment().getMoteDataFile(),true);
                        OutputStreamWriter osw = new OutputStreamWriter(is);
                        Writer w = new BufferedWriter(osw);
                        w.write(moteJsonObject.toJSONString());
                        w.write("\n");
                        w.flush();
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    disallowTransmission();
                    packetCounter = (byte) ((packetCounter + 1) % 100);
                }else{
                    super.sendToGateWay(packet);
                }
            }

        }
    }

    public void setTransmittingInterval(int newInterval){
        synchronized (this.transmittingInterval) {
            this.transmittingInterval = newInterval;
        }
    }


    /**
     * A function for sending a packet to the gateways.
     * @param packet the packet to send
     * @return
     */
    @Override
    public List<Pair<Long,Pair<Double,Boolean>>> sendToGateWay(LoraWanPacket packet) {
        packetBuffer.add(new Pair<>(packet, getEnvironment().getClock().getTime()));
        transmit();
        return null;
    }

    @Override
    protected void initialize() {
        super.initialize();
        packetBuffer= new ExpiringBuffer<>(getEnvironment().getClock(),0);
        packetCounter = 0;
        isAllowedToTransmit = true;
        consumePacketStrategies.add(new ChangeSettings());


    }


    public void setExpirationTime(Integer value) {
        synchronized (expirationTime) {
            if (value > 0) {
                this.expirationTime = value;
                packetBuffer.setExpirationTime(expirationTime);
            }
        }
    }

    public int getExpirationTime() {
        synchronized (expirationTime){
            return expirationTime;
        }
    }

    public void setSettings(MoteSettings settings) {
        setTransmittingInterval(settings.getTransmissionInterval());
        setTransmissionPower(settings.getTransmissionPower());
        setExpirationTime(settings.getExpirationTime());
    }

    public void adjustSettings(MoteSettings settings) {
        setTransmittingInterval(Math.max(5,getTransmittingInterval()+settings.getTransmissionInterval()));
        setTransmissionPower(Math.min(14,Math.max(0,getTransmissionPower()+settings.getTransmissionPower())));
        setExpirationTime(Math.max(5,getExpirationTime()+settings.getExpirationTime()));
    }
}
