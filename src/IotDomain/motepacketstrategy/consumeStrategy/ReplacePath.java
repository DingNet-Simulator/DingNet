package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.LoraWanPacket;
import IotDomain.Mote;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.Collections;
import java.util.List;

public class ReplacePath implements ConsumePacketStrategy {

    @Override
    public void consume(Mote mote, List<LoraWanPacket> packets) {
        mote.setPath(extractPath(packets));
    }

    private List<GeoPosition> extractPath(List<LoraWanPacket> packets) {
        //TODO to implement after define packet structure
        return Collections.emptyList();
    }
}
