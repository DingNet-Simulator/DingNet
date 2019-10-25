package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.LoraWanPacket;

import java.util.List;
import java.util.stream.Stream;

public interface ReceivedPacketStrategy {

    void addReceivedMessage(LoraWanPacket packet);

    boolean hasPackets();

    List<LoraWanPacket> getReceivedPacket();

    default Stream<LoraWanPacket> getReceivedPacketAsStream() {
        return getReceivedPacket().stream();
    }
}
