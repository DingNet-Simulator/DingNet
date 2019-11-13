package IotDomain.networkcommunication;

import util.Pair;

import java.util.function.Consumer;

public interface Receiver<P extends Packet> {

    long getID();

    void receive(LoraTransmission<P> packet);

    Pair<Double, Double> getReceiverPosition();

    Pair<Integer, Integer> getReceiverPositionAsInt();

    P getPacket();

    Receiver<P> setConsumerPacket(Consumer<LoraTransmission<P>> consumerPacket);
}
