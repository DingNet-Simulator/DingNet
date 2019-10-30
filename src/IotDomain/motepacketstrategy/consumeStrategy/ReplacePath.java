package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.lora.LoraWanPacket;
import IotDomain.networkentity.Mote;
import org.jxmapviewer.viewer.GeoPosition;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class ReplacePath implements ConsumePacketStrategy {

    private final static int BYTES_FOR_COORDINATE = 4;

    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        mote.setPath(extractPath(packet));
    }

    protected List<GeoPosition> extractPath(LoraWanPacket packet) {
        if (packet.getPayload().length % 8 != 0) {
            throw  new IllegalStateException("the packet doesn't contain the correct amount of byte");
        }
        var payload = new byte[packet.getPayload().length];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = packet.getPayload()[i];
        }
        ByteBuffer buf = ByteBuffer.wrap(payload);
        final List<GeoPosition> path = new LinkedList<>();
        for (int i = 0; i+BYTES_FOR_COORDINATE*2 <= payload.length; i+=BYTES_FOR_COORDINATE*2) {
            path.add(new GeoPosition(buf.getFloat(i), buf.getFloat(i+BYTES_FOR_COORDINATE)));
        }
        return path;
    }
}
