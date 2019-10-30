package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.lora.LoraWanPacket;
import IotDomain.networkentity.Mote;
import util.PathWithMiddlePoints;

public class ReplacePathWithMiddlePoints extends ReplacePath {
    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        mote.setPath(new PathWithMiddlePoints(extractPath(packet)));
    }
}
