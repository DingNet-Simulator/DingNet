package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.LoraWanPacket;

import java.util.Collections;
import java.util.List;

public class IgnoreAllMessage implements ReceivedPackedStrategy {

    @Override
    public void addReceivedMessage(LoraWanPacket packet) { }

    @Override
    public boolean hasPackets() {
        return false;
    }

    @Override
    public List<LoraWanPacket> getReceivedPacket() {
        return Collections.emptyList();
    }
}
