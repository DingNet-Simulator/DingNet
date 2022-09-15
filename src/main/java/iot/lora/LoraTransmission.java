package iot.lora;


import iot.environment.Characteristic;
import iot.environment.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * A class representing a packet in the LoraWan simulation.
 */
public class LoraTransmission implements Serializable {

    //region field
    private static final long serialVersionUID = 1L;

    /**
     * A network entity representing the sender of the packet.
     */
    private final long sender;

    /**
     * A network entity representing the receiver of the packet.
     */
    private final long receiver;

    /**
     * A double representing the transmission power.
     */
    private double transmissionPower = 0.0;

    /**
     * A GeoPosition representing the location of the sender of the packet at the time of transmission.
     */
    private GeoPosition positionSender;

    /**
     * A GeoPosition representing the location of the Receiver of the packet at the time of transmission.
     */
    private GeoPosition positionReceiver;

    /**
     * The content of the message.
     */
    private final LoraWanPacket content;

    /**
     * Set of parameter used by the {@link iot.networkentity.NetworkEntity} to send this transmission
     */
    private final RegionalParameter regionalParameter;

    /**
     * The departure time of the message
     */
    private final LocalDateTime departureTime;

    /**
     * The time on air of a transmission.
     */
    private final double timeOnAir;

    /**
     * true if the transmission is arrived to destination, false otherwise
     */
    private boolean arrived;


    /**
     * true if the transmission is collided to another one, false otherwise
     */
    private boolean collided;

    //endregion

    //region constructor
    /**
     * A constructor generating a transmission with a given sender, receiver, transmission power, bandwidth, spreading factor,
     * environment and content.
     * @param sender    The sender sending the transmission.
     * @param receiver The receiver receiving the transmission.
     * @param positionSender
     * @param transmissionPower The transmission power of the transmission.
     * @param content The content of the transmission.
     */
    public LoraTransmission(long sender, long receiver, GeoPosition positionSender, GeoPosition positionReceiver,
                            double transmissionPower, RegionalParameter regionalParameter, double timeOnAir,
                            LocalDateTime departureTime, LoraWanPacket content) {

        this.sender = sender;
        this.receiver = receiver;
        this.positionSender = positionSender;
        this.positionReceiver = positionReceiver;
        this.content = content;
        this.arrived = false;
        this.collided = false;

        if (isValidTransmissionPower(transmissionPower)) {
            this.transmissionPower = transmissionPower;

        }

        this.regionalParameter = regionalParameter;
        this.departureTime = departureTime;
        this.timeOnAir = timeOnAir;

    }
    //endregion

    //region setter and getter
    /**
     *  Returns the sender of this transmission.
     * @return The sender of this transmission.
     */
    public long getSender() {
        return sender;
    }

    /**
     *  Returns the receiver of this transmission.
     * @return The receiver of this transmission.
     */
    public long getReceiver() {
        return receiver;
    }

    /**
     * Returns the departure time of the transmission.
     * @return  The departure time of the transmission.
     */
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Returns the time on air.
     * @return  The time on air.
     */
    public double getTimeOnAir() {
        return timeOnAir;
    }

    /**
     * Checks if a given transmission power is valid.
     * @param transmissionPower The transmission power to check.
     * @return  True if the transmission power is valid.
     */
    private static boolean isValidTransmissionPower(double transmissionPower) {
        return true;
    }

    /**
     *  Returns The transmission power of this transmission.
     * @return The transmission power of this transmission.
     */
    public double getTransmissionPower() {
        return transmissionPower;
    }

    /**
     * Sets the transmission power of the transmission
     * @param transmissionPower The transmission power to set.
     * @Post Sets the transmission power to the given value if the given value is valid.
     * @Post Sets the transmission power to 0 if the given value is not valid.
     */
    private void setTransmissionPower(double transmissionPower) {
        if (isValidTransmissionPower(transmissionPower)) {
            this.transmissionPower = transmissionPower;
        } else {
            this.transmissionPower = 0.0;
        }
    }

    public RegionalParameter getRegionalParameter() {
        return regionalParameter;
    }

    /**
     * Returns the bandwidth of the transmission.
     * @return  The bandwidth of the transmission.
     */
    public int getBandwidth() {
        return regionalParameter.getBandwidth();
    }

    /**
     * Returns the spreading factor.
     * @return The spreading factor.
     */
    public int getSpreadingFactor() {
        return regionalParameter.getSpreadingFactor();
    }

    /**
     *
     * @return true if the transmission is arrived to destination, false otherwise
     */
    public boolean isArrived() {
        return arrived;
    }

    /**
     * set the transmission as arrived to destination
     * @return this
     */
    public LoraTransmission setArrived() {
        if (isArrived()) {
            throw new IllegalStateException("the transmission is already arrived, you can't modify again this property");
        }
        this.arrived = true;
        return this;
    }

    /**
     *
     * @return true if the transmission is collided with another one, false otherwise
     */
    public boolean isCollided() {
        return collided;
    }

    /**
     * set the transmission as collided with another one
     * @return this
     */
    public LoraTransmission setCollided() {
        if (isArrived()) {
            new IllegalStateException("the transmission is already arrived, you can't modify this property").printStackTrace();
        }
        if (!isCollided()) {
            this.collided = true;

        }
        return this;
    }

    /**
     * Returns the coordinates of the sender of the transmission at the time of transmission.
     * @return The coordinates of the transmission.
     */
    public GeoPosition getPositionSender() {
        return positionSender;
    }

    /**
     * Returns the coordinates of the receiver of the transmission at the time of transmission.
     * @return The coordinates of the transmission.
     */
    public GeoPosition getPositionReceiver() {
        return positionReceiver;
    }


    /**
     * Returns the content of the transmission.
     * @return  the content of the transmission.
     */
    public LoraWanPacket getContent() {
        return content;
    }

    /**
     * Moves a transmission to a its destination position, while adapting the transmission power.
     */
    public void moveTo(Environment env) {
        setTransmissionPower(moveTo(env.getMapHelper().toMapXCoordinate(getPositionReceiver()),env.getMapHelper().toMapYCoordinate(getPositionReceiver()), transmissionPower,env));
    }

    /**
     * Moves a transmission to a given position, while adapting the transmission power.
     * @param xDestPos  The x-coordinate of the destination.
     * @param yDestPos  The y-coordinate of the destination.
     * @param transmissionPower the initial transmission power
     * @return the transmission
     */
    private double moveTo(double xDestPos, double yDestPos, double transmissionPower,Environment env) {

        int xPos = (int) Math.round(env.getMapHelper().toMapXCoordinate(getPositionSender()));
        int yPos = (int) Math.round(env.getMapHelper().toMapYCoordinate(getPositionSender()));
        Characteristic characteristic = null;
        int xDist = (int) Math.abs(xDestPos - xPos);
        int yDist = (int) Math.abs(yDestPos - yPos);
        int dx = (int) Math.abs(xDestPos- xPos);
        int sx = (int) Math.signum(xDestPos -xPos);
        int dy = - (int) Math.abs(yDestPos - yPos);
        int sy = (int) Math.signum(yDestPos - yPos);
        int error = dx + dy;
        int e2;

        while (transmissionPower > -300 && !(xPos == xDestPos && yPos == yDestPos)) {
            characteristic = env.getCharacteristic(xPos, yPos);
            transmissionPower = transmissionPower - 10 * (characteristic.getPathLossExponent()
                //    +weatherCharacteristic.getPathLossExponent()
            )* (Math.log10(xDist + yDist) - Math.log10(xDist + yDist - 1));
                e2 = 2 * error;
            if (e2 >= dy && xPos != xDestPos) {
                error = error + dy;
                xPos = xPos + sx;
                xDist -=1;
            }

            if (e2 <= dx && yPos != yDestPos) {
                error = error + dx;
                yPos = yPos + sy;
                yDist -= 1;
            }
        }

        return transmissionPower - env.getRandom().nextGaussian() * ((characteristic == null) ? env.getCharacteristic(xPos, yPos) : characteristic).getShadowFading();
    }

    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoraTransmission that = (LoraTransmission) o;
        return getSender() == that.getSender() &&
            getContent().equals(that.getContent()) &&
            getDepartureTime().equals(that.getDepartureTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSender(), getReceiver(), getContent(), getDepartureTime());
    }

}
