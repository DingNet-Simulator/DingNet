package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.lora.LoraWanPacket;
import IotDomain.networkentity.Mote;

public class ReplacePathWithMiddlePoints extends ReplacePath {
    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        mote.getPath().addPositions(extractPath(packet));
    }
}
