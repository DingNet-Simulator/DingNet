package IotDomain.networkcommunication;

import IotDomain.GlobalClock;
import IotDomain.networkentity.NetworkEntity;
import util.Pair;
import util.TimeHelper;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ReceiverLoRa implements Receiver<LoraWanPacket> {

    // The levels of power in between which it can discriminate.
    private final double transmissionPowerThreshold;
    private Consumer<LoraTransmission<LoraWanPacket>> consumerPacket;

    private List<LoraTransmission<LoraWanPacket>> transmissions = new LinkedList<>();

    private GlobalClock clock;

    private final NetworkEntity receiver;

    public ReceiverLoRa(NetworkEntity receiver, GlobalClock clock, double transmissionPowerThreshold) {
        this.transmissionPowerThreshold = transmissionPowerThreshold;
        this.receiver = receiver;
        this.clock = clock;
    }

    @Override
    public long getID() {
        return receiver.getEUI();
    }

    @Override
    public void receive(LoraTransmission<LoraWanPacket> packet) {
        transmissions.stream()
            .filter(t -> collision(packet, t))
            .peek(LoraTransmission::setCollided)
            .findAny()
            .ifPresent(t -> packet.setCollided());
        transmissions.add(packet);
        clock.addTrigger(packet.getDepartureTime().plus(packet.getTimeOnAir().longValue(), ChronoUnit.MILLIS),()->{
            packet.setArrived();
            consumerPacket.accept(packet);
            return LocalTime.of(0,0);
        });
    }

    /**
     * Checks if two packets collide according to the model
     * @param a The first packet.
     * @param b The second packet.
     * @return true if the packets collide, false otherwise.
     */
    private boolean collision(LoraTransmission a, LoraTransmission b) {
        return a.getSpreadingFactor().equals(b.getSpreadingFactor()) &&     //check spreading factor
            a.getTransmissionPower() - b.getTransmissionPower() < transmissionPowerThreshold && //check transmission power
            Math.abs(Duration.between(a.getDepartureTime().plusNanos(TimeHelper.miliToNano(a.getTimeOnAir().longValue()) / 2), //check time on air
                b.getDepartureTime().plusNanos(TimeHelper.miliToNano(b.getTimeOnAir().longValue()) / 2)).toNanos())
                < TimeHelper.miliToNano(a.getTimeOnAir().longValue()) / 2 + TimeHelper.miliToNano(b.getTimeOnAir().longValue()) / 2;
    }

    @Override
    public Pair<Double, Double> getReceiverPosition() {
        return new Pair<>(receiver.getXPosDouble(), receiver.getYPosDouble());
    }

    @Override
    public Pair<Integer, Integer> getReceiverPositionAsInt() {
        return receiver.getPosInt();
    }

    @Override
    public LoraWanPacket getPacket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Receiver<LoraWanPacket> setConsumerPacket(Consumer<LoraTransmission<LoraWanPacket>> consumerPacket) {
        this.consumerPacket = consumerPacket;
        return this;
    }

    @Override
    public void reset() {
        transmissions.clear();
    }
}
