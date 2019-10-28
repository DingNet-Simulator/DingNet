package IotDomain;

import org.jxmapviewer.viewer.GeoPosition;
import util.Path;

import java.util.LinkedList;

public class MoteFactory {

    public static Mote createMote(long devEUI, int xPos, int yPos, Environment environment, int transmissionPower,
                                  int spreadingFactor, LinkedList<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed){
        return new Mote(devEUI, xPos, yPos, environment, transmissionPower, spreadingFactor, moteSensors, energyLevel, path, movementSpeed);
    }

    public static Mote createMote(long devEUI, int xPos, int yPos, Environment environment, int transmissionPower,
                                  int spreadingFactor, LinkedList<MoteSensor> moteSensors, int energyLevel, Path path,
                                  double movementSpeed, int startMovementOffset, int periodSendingPacket, int startSendingOffset) {
        return new Mote(devEUI, xPos, yPos, environment, transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
    }

    public static UserMote createUserMote(long devEUI, int xPos, int yPos, Environment environment, int transmissionPower,
                                  int spreadingFactor, LinkedList<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed){
        return new UserMote(devEUI, xPos, yPos, environment, transmissionPower, spreadingFactor, moteSensors, energyLevel, path, movementSpeed);
    }

    public static UserMote createUserMote(long devEUI, int xPos, int yPos, Environment environment, int transmissionPower,
                                  int spreadingFactor, LinkedList<MoteSensor> moteSensors, int energyLevel, Path path,
                                  double movementSpeed, int startMovementOffset, int periodSendingPacket, int startSendingOffset) {
        return new UserMote(devEUI, xPos, yPos, environment, transmissionPower, spreadingFactor, moteSensors,
            energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
    }
}
