package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.lora.LoraWanPacket;

import java.util.Optional;

public interface ReceivedPacketStrategy {

    void addReceivedMessage(LoraWanPacket packet);

    boolean hasPackets();

    Optional<LoraWanPacket> getReceivedPacket();
}
