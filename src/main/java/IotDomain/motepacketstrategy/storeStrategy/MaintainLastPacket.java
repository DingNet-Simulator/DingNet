package IotDomain.motepacketstrategy.storeStrategy;


import IotDomain.networkcommunication.LoraWanPacket;

import java.util.Optional;

public class MaintainLastPacket implements ReceivedPacketStrategy {

    private int lastPacketReceived = 0;
    private LoraWanPacket packet;

    @Override
    public void addReceivedMessage(LoraWanPacket packet) {
        if (packet.getFrameHeader().getFCntAsShort() > lastPacketReceived) {
            lastPacketReceived = packet.getFrameHeader().getFCntAsShort();
            this.packet = packet;
        }
    }

    @Override
    public boolean hasPackets() {
        return packet != null;
    }

    @Override
    public Optional<LoraWanPacket> getReceivedPacket() {
        var tmp = Optional.ofNullable(packet);
        packet = null;
        return tmp;
    }

    @Override
    public void clear() {
        this.lastPacketReceived = 0;
        this.packet = null;
    }
}
