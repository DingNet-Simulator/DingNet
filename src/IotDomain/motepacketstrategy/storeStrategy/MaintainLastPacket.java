package IotDomain.motepacketstrategy.storeStrategy;

import IotDomain.lora.LoraWanPacket;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
            var newPacket = new LoraWanPacket(packet.getSenderEUI(), packet.getDesignatedReceiverEUI(),
                Arrays.stream(packet.getPayload()).skip(4).toArray(Byte[]::new),
                packet.hasHeader(), packet.hasLowDataRateOptimization(),
                packet.getAmountOfPreambleSymbols(), packet.getCodingRate(), packet.getMacCommands());
            lastPacketReceived = sequenceNumber;
            this.packet = Optional.of(newPacket);
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
