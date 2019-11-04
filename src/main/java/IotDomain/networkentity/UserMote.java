package IotDomain.networkentity;

import IotDomain.Environment;
import IotDomain.lora.BasicFrameHeader;
import IotDomain.lora.LoraWanPacket;
import IotDomain.lora.MacCommand;
import IotDomain.lora.MessageType;
import IotDomain.motepacketstrategy.consumeStrategy.ReplacePathWithMiddlePoints;
import SensorDataGenerators.SensorDataGenerator;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;
import util.Path;
import util.PathWithMiddlePoints;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserMote extends Mote {

    private boolean isActive = false;
    private GeoPosition destination;
    private final LocalTime whenAskPath = LocalTime.of(0, 0, 15);
    private boolean alreadyRequested = false;

    UserMote(long DevEUI, int xPos, int yPos, Environment environment, int transmissionPower, int SF, List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed, int startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
        init(destination);
    }

    private void init(GeoPosition destination) {
        consumePacketStrategies.add(new ReplacePathWithMiddlePoints());
        setPath(new PathWithMiddlePoints(List.of(MapHelper.getInstance().toGeoPosition(this.getPos()))));
        this.destination = destination;
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        if (isActive() && !alreadyRequested && whenAskPath.isBefore(getEnvironment().getClock().getTime())) {
            alreadyRequested = true;
            byte[] payload= new byte[17];
            payload[0] = MessageType.REQUEST_PATH.getCode();
            System.arraycopy(getGPSSensor().generateData(getPos(), getEnvironment().getClock().getTime()), 0, payload, 1, 8);
            System.arraycopy(Converter.toByteArray(destination), 0, payload, 9, 8);
            return new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
                new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
        }
        return LoraWanPacket.createEmptyPacket(getEUI(), getApplicationEUI());
    }

    @Override
    public void setPos(int xPos, int yPos) {
        super.setPos(xPos, yPos);
        if (isActive() && getPath() instanceof PathWithMiddlePoints) {
            if (getPath().isEmpty()) {
                throw new IllegalStateException("I don't have any path to follow...I can't move:(");
            }
            var path = (PathWithMiddlePoints)getPath();
            var wayPoints = path.getOriginalWayPoint();
            //if I don't the path to the destination and I am at the penultimate position of the path
            if (path.getDestination().isPresent() &&    //at least tha path has one point
                !path.getDestination().get().equals(destination) &&
                wayPoints.size() > 1 &&
                MapHelper.getInstance().toMapCoordinate(wayPoints.get(wayPoints.size()-2)).equals(getPos())) {
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
        loraSend(new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
            new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>()));
        canReceive = true;

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
        return this.getPos().equals(MapHelper.getInstance().toMapCoordinate(destination));
    }


    @Override
    public void initialize() {
        super.initialize();
        this.setPath(new ArrayList<>());
        this.alreadyRequested = false;
    }
}
