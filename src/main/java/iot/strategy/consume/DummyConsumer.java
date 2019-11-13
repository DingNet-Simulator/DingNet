package iot.strategy.consume;


import iot.networkcommunication.LoraWanPacket;
import iot.networkentity.Mote;

import java.util.Arrays;

public class DummyConsumer implements ConsumePacketStrategy {
    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        System.out.println(Arrays.toString(packet.getPayload()));
    }
}
