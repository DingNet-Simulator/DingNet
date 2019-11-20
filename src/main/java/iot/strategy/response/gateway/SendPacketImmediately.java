package iot.strategy.response.gateway;


import iot.lora.LoraWanPacket;
import iot.mqtt.LoraWanPacketWrapper;
import iot.mqtt.Topics;
import iot.networkentity.Gateway;
import util.Pair;

import java.util.Optional;

public class SendPacketImmediately implements ResponseStrategy {

    private Gateway gateway;

    @Override
    public ResponseStrategy init(Gateway gateway) {
        this.gateway = gateway;
        //subscribe to all mote topic
        subscribeToMotesTopic();
        return this;
    }

    private void subscribeToMotesTopic() {
        gateway.getEnvironment().getMotes().stream()
            .map(m -> new Pair<>(m.getApplicationEUI(), m.getEUI()))
            .forEach(m -> gateway.getMqttClient().subscribe(
                this,
                Topics.getNetServerToGateway(m.getLeft(), gateway.getEUI(), m.getRight()),
                LoraWanPacketWrapper.class,
                (t, msg) -> gateway.sendToDevice(msg.getPacket())
            ));
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.empty();
    }
}
