package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.Mote;
import IotDomain.lora.LoraWanPacket;

import java.util.Arrays;
import java.util.List;

public class DummyConsumer implements ConsumePacketStrategy {
    @Override
    public void consume(Mote mote, List<LoraWanPacket> packets) {
        packets.forEach(p -> System.out.println(Arrays.toString(p.getPayload())));
    }
}
