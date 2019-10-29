package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.lora.LoraWanPacket;
import IotDomain.networkentity.Mote;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class ReplacePath implements ConsumePacketStrategy {

    private final static int BYTES_FOR_COORDINATE = 4;
    private final Map<Pair<GeoPosition, GeoPosition>, List<GeoPosition>> mapToSubPath;

    public ReplacePath() {
        this.mapToSubPath = new HashMap<>();
        loadSubPath();
    }

    private void loadSubPath() {
        //TODO
    }

    @Override
    public void consume(Mote mote, List<LoraWanPacket> packets) {
        mote.setPath(extractPath(packets.get(packets.size()-1)));
    }

    private List<GeoPosition> extractPath(LoraWanPacket packet) {
        if (packet.getPayload().length % 8 != 0) {
            throw  new IllegalStateException("the packet doesn't contain the correct amount of byte");
        }
        var payload = new byte[packet.getPayload().length];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = packet.getPayload()[i];
        }
        ByteBuffer buf = ByteBuffer.wrap(payload);
        if (payload.length == BYTES_FOR_COORDINATE*2) {
            return List.of(new GeoPosition(buf.getFloat(), buf.getFloat(BYTES_FOR_COORDINATE)));
        }
        final List<Pair<GeoPosition, GeoPosition>> paths = new LinkedList<>();
        for (int i = 0; i+BYTES_FOR_COORDINATE*4 <= payload.length; i+=BYTES_FOR_COORDINATE*2) {
            paths.add(new Pair<>(
                new GeoPosition(buf.getFloat(i), buf.getFloat(i+BYTES_FOR_COORDINATE)),
                new GeoPosition(buf.getFloat(i+BYTES_FOR_COORDINATE*2), buf.getFloat(i+BYTES_FOR_COORDINATE*3))
            ));
        }
        return paths.stream()
            .flatMap(p -> mapToSubPath.getOrDefault(p, Collections.emptyList()).stream())
            .distinct()
            .collect(Collectors.toList());
    }
}
