package iot.networkcommunication.api;

import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import util.Pair;

import java.util.function.Consumer;

public interface Receiver {

    long getID();

    void receive(LoraTransmission packet);

    Pair<Double, Double> getReceiverPosition();

    Pair<Integer, Integer> getReceiverPositionAsInt();

    LoraWanPacket getPacket();

    Receiver setConsumerPacket(Consumer<LoraTransmission> consumerPacket);

    void reset();
}
