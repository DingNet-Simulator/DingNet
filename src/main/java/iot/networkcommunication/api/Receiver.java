package iot.networkcommunication.api;

import iot.lora.LoraTransmission;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.function.Consumer;

/**
 * Interface for a LoRa receiver entity
 */
public interface Receiver {

    /**
     *
     * @return Id of the receiver
     */
    long getID();

    /**
     *
     * @param packet the transmission received
     */
    void receive(LoraTransmission packet);

    /**
     *
     * @return receiver position as geo position
     */
    GeoPosition getReceiverPosition();


    /**
     * Setter for the consumer for all the received transmission
     * @param consumerPacket the consumer
     * @return this
     */
    Receiver setConsumerPacket(Consumer<LoraTransmission> consumerPacket);

    /**
     * reset the receiver to the initial state
     */
    void reset();
}
