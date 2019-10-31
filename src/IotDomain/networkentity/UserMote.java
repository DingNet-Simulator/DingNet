package IotDomain.networkentity;

import IotDomain.Environment;
import IotDomain.lora.BasicFrameHeader;
import IotDomain.lora.LoraWanPacket;
import IotDomain.lora.MacCommand;
import SensorDataGenerators.SensorDataGenerator;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.Path;

import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserMote extends Mote {

    private boolean isActive = false;
    private GeoPosition destination;
    private final LocalTime whenAskPath = LocalTime.of(0, 15);
    private boolean alreadyRequested = false;

    UserMote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF, List<MoteSensor> moteSensors, Integer energyLevel, Path path, Double movementSpeed, Integer startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
        this.destination = destination;
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        if (isActive && !alreadyRequested && whenAskPath.isBefore(getEnvironment().getClock().getTime())) {
            alreadyRequested = true;
            byte[] payload= new byte[17];
            payload[0] = 1;
            System.arraycopy(getGPSSensor().generateData(getPos(), getEnvironment().getClock().getTime()), 0, payload, 1, 8);
            ByteBuffer.wrap(payload, 9, 4).putFloat((float)destination.getLatitude());
            ByteBuffer.wrap(payload, 13, 4).putFloat((float)destination.getLongitude());
            return new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
                new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
        } else {
            return super.composePacket(data, macCommands);
        }
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
                .forEach(m -> m.setActive(false));
        }
        isActive = active;
    }

    public GeoPosition getDestination() {
        return this.destination;
    }
}
