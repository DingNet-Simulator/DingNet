package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.LoraWanPacket;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MaintainLastPacket implements ReceivedPacketStrategy {

    private int lastPacketReceived = 0;
    private Optional<LoraWanPacket> packet = Optional.empty();

    @Override
    public void addReceivedMessage(LoraWanPacket packet) {
        byte[] sequenceNumberAsBytes = new byte[4];
        for (int i = 0; i < sequenceNumberAsBytes.length && i < packet.getPayload().length; i++) {
            sequenceNumberAsBytes[i] = packet.getPayload()[i];
        }
        ByteBuffer buf = ByteBuffer.wrap(sequenceNumberAsBytes);
        var sequenceNumber = buf.getInt();
        if (sequenceNumber > lastPacketReceived) {
            lastPacketReceived = sequenceNumber;
            this.packet = Optional.of(packet);
        }
    }

    @Override
    public boolean hasPackets() {
        return packet.isPresent();
    }

    @Override
    public List<LoraWanPacket> getReceivedPacket() {
        return hasPackets() ? List.of(packet.get()) : Collections.emptyList();
    }
}
