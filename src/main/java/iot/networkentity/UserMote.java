package iot.networkentity;

import datagenerator.SensorDataGenerator;
import iot.Environment;
import iot.GlobalClock;
import iot.lora.BasicFrameHeader;
import iot.lora.LoraWanPacket;
import iot.lora.MacCommand;
import iot.lora.MessageType;
import iot.strategy.consume.ReplacePath;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.Path;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserMote extends Mote {

    // the user mote can ask for a path only if this property is true
    private boolean isActive = false;
    private GeoPosition destination;
    private final LocalDateTime whenAskPath = LocalDateTime.ofEpochSecond(15, 0, ZoneOffset.UTC);
    private boolean alreadyRequested;

    UserMote(long DevEUI, double xPos, double yPos, int transmissionPower, int SF,
             List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed,
             int startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination, Environment environment) {
        super(DevEUI, xPos, yPos, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment);
        this.destination = destination;

        this.initialize();
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        GlobalClock clock = this.getEnvironment().getClock();

        if (isActive()) {
            if (!alreadyRequested && whenAskPath.isBefore(clock.getTime())) {
                alreadyRequested = true;
                byte[] payload = new byte[17];
                payload[0] = MessageType.REQUEST_PATH.getCode();
                System.arraycopy(getGPSSensor().generateData(getEnvironment(),getPathPosition(), clock.getTime()), 0, payload, 1, 8);
                System.arraycopy(Converter.toByteArray(destination), 0, payload, 9, 8);
                return new LoraWanPacket(getEUI(), getApplicationEUI(), payload,
                    new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
            } else {
                return super.composePacket(data, macCommands);
            }
        }
        return LoraWanPacket.createEmptyPacket(getEUI(), getApplicationEUI());
    }

    private SensorDataGenerator getGPSSensor() {
        return getSensors().stream().filter(s -> s.equals(MoteSensor.GPS)).findFirst().orElseThrow().getSensorDataGenerator();
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Setting active a userMote means also set not active all this other userMote
     * @param active true to set active, false otherwise
     */
    public void setActive(boolean active) {
        if (active) {
            this.getEnvironment().getMotes().stream()
                .filter(m -> m instanceof UserMote)
                .map(m -> (UserMote)m)
                .forEach(m -> {
                    m.setActive(false);
                    m.enable(false);
                });
        }
        isActive = active;
    }

    public GeoPosition getDestination() {
        return this.destination;
    }

    public void setDestination(GeoPosition destination) {
        this.destination = destination;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && isActive();
    }

    @Override
    public boolean isArrivedToDestination() {
        return this.getPos().equals(destination);
    }


    @Override
    protected void initialize() {
        super.initialize();

        setPath(new Path(List.of(this.getPos()),
            this.getEnvironment().getGraph()));
        this.pathPositionIndex = 0;

        this.alreadyRequested = false;
        consumePacketStrategies.add(new ReplacePath());
    }
}
