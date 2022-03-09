package iot.networkentity;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;
import util.Path;

import java.util.List;

/**
 * Factory different kind of {@link Mote}
 */
public class MoteFactory {

    public static Mote createMote(long devEUI, Pair<Double,Double> pos, int transmissionPower,
                                  int spreadingFactor, int energyLevel, double movementSpeed, List<MoteSensor> moteSensors, Path path, Environment environment) {
        return new Mote(devEUI, pos.getLeft(), pos.getRight(), transmissionPower, spreadingFactor, moteSensors, energyLevel, path, movementSpeed, environment);
    }

    public static Mote createMote(long devEUI, Pair<Double,Double> pos, int transmissionPower,
                                  int spreadingFactor, int energyLevel, double movementSpeed,
                                  int startMovementOffset, int periodSendingPacket, int startSendingOffset,
                                  List<MoteSensor> moteSensors, Path path, Environment environment) {
        return new Mote(devEUI, pos.getLeft(), pos.getRight(), transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment);

    }


    public static UserMote createUserMote(long devEUI, Pair<Double,Double> pos, int transmissionPower, int spreadingFactor,
                                          int energyLevel, double movementSpeed, int startMovementOffset,
                                          int periodSendingPacket, int startSendingOffset, GeoPosition destination,
                                          List<MoteSensor> moteSensors, Path path,  Environment environment) {
        return new UserMote(devEUI, pos.getLeft(), pos.getRight(), transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, destination, environment);
    }

    public static LifeLongMote createLLSACompliantMote(long devEUI, Pair<Double,Double> pos, int transmissionPower, int spreadingFactor, int energyLevel,
                                                       double movementSpeed, int startMovementOffset, int periodSendingPacket, int startSendingOffset,
                                                       List<MoteSensor> moteSensors,  Path path, int transmittingInterval, int expirationTime, Environment environment) {
        return new LifeLongMote(devEUI, pos.getLeft(), pos.getRight(), transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment, transmittingInterval, expirationTime);
    }
}
