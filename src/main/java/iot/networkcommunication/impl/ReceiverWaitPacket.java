package iot.networkcommunication.impl;

import iot.GlobalClock;
import iot.lora.LoraTransmission;
import iot.networkcommunication.api.Receiver;
import iot.networkentity.NetworkEntity;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;
import util.TimeHelper;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReceiverWaitPacket implements Receiver {

    // The levels of power in between which it can discriminate.
    private final double transmissionPowerThreshold;
    private Consumer<LoraTransmission> consumerPacket;

    private List<LoraTransmission> transmissions = Collections.synchronizedList(new LinkedList<>());
    private List<LoraTransmission> transmissionsWithPossibleCollision = Collections.synchronizedList(new LinkedList<>());

    private GlobalClock clock;

    private final NetworkEntity receiver;

    public ReceiverWaitPacket(NetworkEntity receiver, double transmissionPowerThreshold, GlobalClock clock) {
        this.transmissionPowerThreshold = transmissionPowerThreshold;
        this.receiver = receiver;
        this.clock = clock;
    }

    @Override
    public long getID() {
        return receiver.getEUI();
    }

    @Override
    public void receive(LoraTransmission transmission) {
        List<LoraTransmission> collidedTransmissions =new LinkedList<>();
        synchronized (transmissionsWithPossibleCollision) {
            Iterator<LoraTransmission> transmissionIterator = transmissionsWithPossibleCollision.listIterator();
            while (transmissionIterator.hasNext()) {
                LoraTransmission collidedTransmission = transmissionIterator.next();
                if (collision(transmission, collidedTransmission)) {
                    collidedTransmissions.add(collidedTransmission);
                }
            }
            for (LoraTransmission collidedTransmission : collidedTransmissions) {
                collidedTransmission.setCollided();
            }
            if (!collidedTransmissions.isEmpty()) {
                transmission.setCollided();
            }
            transmissions.add(transmission);
            transmissionsWithPossibleCollision.add(transmission);
        }
        clock.addTriggerOneShot(transmission.getDepartureTime().plus((long)transmission.getTimeOnAir(), ChronoUnit.MILLIS), () -> {
            transmission.setArrived();
            synchronized (transmissionsWithPossibleCollision) {
                transmissionsWithPossibleCollision.remove(transmission);
            }
            consumerPacket.accept(transmission);
        });
    }

    /**
     * Checks if two packets collide according to the model
     * @param a The first packet.
     * @param b The second packet.
     * @return true if the packets collide, false otherwise.
     */
    private boolean collision(LoraTransmission a, LoraTransmission b) {
        return a.getSpreadingFactor() == b.getSpreadingFactor() &&     //check spreading factor
            a.getTransmissionPower() - b.getTransmissionPower() < transmissionPowerThreshold && //check transmission power
            Math.abs(Duration.between(a.getDepartureTime().plusNanos(TimeHelper.miliToNano((long)a.getTimeOnAir()) / 2), //check time on air
                b.getDepartureTime().plusNanos(TimeHelper.miliToNano((long)b.getTimeOnAir()) / 2)).toNanos())
                < TimeHelper.miliToNano((long)a.getTimeOnAir()) / 2 + TimeHelper.miliToNano((long)b.getTimeOnAir()) / 2;
    }

    @Override
    public GeoPosition getReceiverPosition() {
        return receiver.getPos();
    }

    @Override
    public Receiver setConsumerPacket(Consumer<LoraTransmission> consumerPacket) {
        this.consumerPacket = consumerPacket;
        return this;
    }

    @Override
    public void reset() {
        transmissions.clear();
    }
}
