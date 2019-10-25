package IotDomain.gatewayresponsestrategy;

import IotDomain.Gateway;
import IotDomain.LoraWanPacket;
import util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
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
                getTopic(m.getLeft(), m.getRight()),
                (t, msg) -> packetBuffer.put(m, new LoraWanPacket(gateway.getEUI(), msg.getDeviceEUI(), msg.getDataAsArray(), new LinkedList<>()))));
    }

    private String getTopic(Long applicationEUI, Long deviceEUI) {
        return new StringBuilder()
            .append("application/")
            .append(applicationEUI)
            .append("/node/")
            .append(deviceEUI)
            .append("/tx")
            .toString();
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.ofNullable(packetBuffer.remove(new Pair<>(applicationEUI, deviceEUI)));
    }
}
