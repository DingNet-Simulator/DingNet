package iot.strategy.response.gateway;


import iot.lora.LoraWanPacket;
import iot.networkentity.Gateway;

import java.util.LinkedList;
import java.util.Optional;

public class DummyResponse implements ResponseStrategy {

    private Gateway gateway;
    private int count = 0;

    @Override
    public ResponseStrategy init(Gateway gateway) {
        this.gateway = gateway;
        return this;
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        LoraWanPacket resp = null;
        if (count++ % 50 == 0) {
            resp = new LoraWanPacket(gateway.getEUI(), deviceEUI, new Byte[]{(byte)(count/50)}, new LinkedList<>());
        }
        return Optional.ofNullable(resp);
    }
}
