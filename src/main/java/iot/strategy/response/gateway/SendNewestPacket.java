package iot.strategy.response.gateway;


import iot.lora.LoraWanPacket;
import iot.mqtt.BasicMqttMessage;
import iot.mqtt.Topics;
import iot.networkentity.Gateway;
import util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SendNewestPacket implements ResponseStrategy {

    //map <appEUI, devEUI> -> buffered packet
    private final Map<Pair<Long, Long>, LoraWanPacket> packetBuffer;
    private Gateway gateway;

    public SendNewestPacket() {
        packetBuffer = new HashMap<>();
    }

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
                Topics.getNetServerToGateway(m.getLeft(), gateway.getEUI(), m.getRight()),
                (t, msg1) -> {
                    var msg = gateway.getMqttClient().convertMessage(msg1, BasicMqttMessage.class);
                    packetBuffer.put(m, new LoraWanPacket(gateway.getEUI(), msg.getDeviceEUI(), msg.getDataAsArray(), msg.getHeader(), msg.getMacCommands()));
                }));
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.ofNullable(packetBuffer.remove(new Pair<>(applicationEUI, deviceEUI)));
    }
}
