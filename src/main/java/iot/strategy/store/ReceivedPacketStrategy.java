package iot.strategy.store;

import iot.lora.LoraWanPacket;

import java.util.Optional;

public interface ReceivedPacketStrategy {

    void addReceivedMessage(LoraWanPacket packet);

    boolean hasPackets();

    Optional<LoraWanPacket> getReceivedPacket();

    void clear();
}
