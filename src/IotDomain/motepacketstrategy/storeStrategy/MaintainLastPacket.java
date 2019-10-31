package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.lora.LoraWanPacket;

import java.util.Optional;

public class MaintainLastPacket implements ReceivedPacketStrategy {

    private int lastPacketReceived = 0;
    private LoraWanPacket packet;

    @Override
    public void addReceivedMessage(LoraWanPacket packet) {
        if (packet.getHeader().getFCntAsShort() > lastPacketReceived) {
            lastPacketReceived = packet.getHeader().getFCntAsShort();
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
}
