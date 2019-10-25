package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.LoraWanPacket;

import java.util.LinkedList;
import java.util.List;

public class StoreAllMessage implements ReceivedPacketStrategy {

    private List<LoraWanPacket> packets = new LinkedList<>();

    @Override
    public void addReceivedMessage(LoraWanPacket packet) {
        packets.add(packet);
    }

    @Override
    public boolean hasPackets() {
        return !packets.isEmpty();
    }

    @Override
    public List<LoraWanPacket> getReceivedPacket() {
        var ret = packets;
        packets = new LinkedList<>();
        return ret;
    }
}
