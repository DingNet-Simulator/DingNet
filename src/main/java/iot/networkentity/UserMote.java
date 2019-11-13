package iot.networkentity;

import datagenerator.SensorDataGenerator;
import iot.Environment;
import iot.lora.BasicFrameHeader;
import iot.lora.LoraWanPacket;
import iot.lora.MacCommand;
import iot.lora.MessageType;
import iot.strategy.consume.ReplacePath;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;
import util.Path;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserMote extends Mote {

    private boolean isActive = false;
    private GeoPosition destination;
    private final LocalTime whenAskPath = LocalTime.of(0, 0, 15);
    private boolean alreadyRequested;

    UserMote(long DevEUI, int xPos, int yPos, Environment environment, int transmissionPower, int SF, List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed, int startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
        consumePacketStrategies.add(new ReplacePath());
        this.destination = destination;
        // the method initialized() is called at the beginning of each simulation so we don't need to call also here
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        if (isActive() && !alreadyRequested && whenAskPath.isBefore(getEnvironment().getClock().getTime())) {
            alreadyRequested = true;
            byte[] payload= new byte[17];
            payload[0] = MessageType.REQUEST_PATH.getCode();
            System.arraycopy(getGPSSensor().generateData(getPosInt(), getEnvironment().getClock().getTime()), 0, payload, 1, 8);
            System.arraycopy(Converter.toByteArray(destination), 0, payload, 9, 8);
            return new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
                new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
        }
        return LoraWanPacket.createEmptyPacket(getEUI(), getApplicationEUI());
    }

    @Override
    public void setPos(double xPos, double yPos) {
        super.setPos(xPos, yPos);
        if (isActive()) {
            if (getPath().isEmpty()) {
                throw new IllegalStateException("I don't have any path to follow...I can't move:(");
            }
            var path = getPath();
            var wayPoints = path.getWayPoints();
            //if I don't the path to the destination and I am at the penultimate position of the path
            if (path.getDestination().isPresent() &&    //at least tha path has one point
                !path.getDestination().get().equals(destination) &&
                wayPoints.size() > 1 &&
                MapHelper.toMapCoordinate(wayPoints.get(wayPoints.size()-2), getEnvironment().getMapOrigin()).equals(getPosInt())) {
                //require new part of path
                askNewPartOfPath();
            }
        }
    }

    private void askNewPartOfPath() {
        if (getPath().getDestination().isEmpty()) {
            throw new IllegalStateException("You can't require new part of path without a previous one");
        }
        byte[] payload= new byte[9];
        payload[0] = MessageType.REQUEST_UPDATE_PATH.getCode();
        System.arraycopy(Converter.toByteArray(getPath().getDestination().get()), 0, payload, 1, 8);
        sendToGateWay(new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
            new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>()));

        var clock = getEnvironment().getClock();
        var oldDestination = getPath().getDestination();
        clock.addTrigger(clock.getTime().plusSeconds(30), () -> {
            if (oldDestination.equals(getPath().getDestination())) {
                askNewPartOfPath();
            }
            return LocalTime.of(0, 0);
        });
    }

    private SensorDataGenerator getGPSSensor() {
        return getSensors().stream().filter(s -> s.equals(MoteSensor.GPS)).findFirst().orElseThrow().getSensorDataGenerator();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (active) {
            getEnvironment().getMotes().stream()
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

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && isActive();
    }

    @Override
    public boolean isArrivedToDestination() {
        return this.getPosInt().equals(MapHelper.toMapCoordinate(destination, getEnvironment().getMapOrigin()));
    }


    @Override
    public void initialize() {
        super.initialize();
        setPath(new Path(List.of(MapHelper.toGeoPosition(this.getPosInt(), getEnvironment().getMapOrigin()))));
        this.alreadyRequested = false;
    }
}
