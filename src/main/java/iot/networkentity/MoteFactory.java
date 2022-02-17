package iot.networkentity;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Path;

import java.util.List;

/**
 * Factory different kind of {@link Mote}
 */
public class MoteFactory {

    public static Mote createMote(long devEUI, double xPos, double yPos, int transmissionPower,
                                  int spreadingFactor, List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed, Environment environment) {
        return new Mote(devEUI, xPos, yPos, transmissionPower, spreadingFactor, moteSensors, energyLevel, path, movementSpeed, environment);
    }

    public static Mote createMote(long devEUI, double xPos, double yPos, int transmissionPower,
                                  int spreadingFactor, List<MoteSensor> moteSensors, int energyLevel, Path path,
                                  double movementSpeed, int startMovementOffset, int periodSendingPacket, int startSendingOffset, Environment environment) {
        return new Mote(devEUI, xPos, yPos, transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment);

    }


    public static UserMote createUserMote(long devEUI, double xPos, double yPos, int transmissionPower, int spreadingFactor,
                                          List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed,
                                          int startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination, Environment environment) {
        return new UserMote(devEUI, xPos, yPos, transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, destination, environment);
    }

    public static LifeLongMote createLLSACompliantMote(long devEUI, double xPos, double yPos, int transmissionPower, int spreadingFactor,
                                          List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed,
                                          int startMovementOffset, int periodSendingPacket, int startSendingOffset, Environment environment, int transmittingInterval,
                                                       int expirationTime) {
        return new LifeLongMote(devEUI, xPos, yPos, transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment, transmittingInterval, expirationTime);
    }
}
