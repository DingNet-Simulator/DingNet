package iot.networkentity;

import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;

import java.util.*;
import java.util.function.BinaryOperator;

public class NetworkServer {

    // Map moteId -> (Map gatewayId -> lastTransmission)
    private final Map<Long, Map<Long, LoraTransmission<LoraWanPacket>>> transmissionReceived;
    private final Map<Long, List<LoraTransmission<LoraWanPacket>>> historyMote;
    private final MqttClientBasicApi mqttClient;
    private BinaryOperator<Map.Entry<Long, LoraTransmission<LoraWanPacket>>> chooseGatewayStrategy = this::chooseByTransmissionPower;

    public NetworkServer(MqttClientBasicApi mqttClient) {
        this.mqttClient = mqttClient;
        transmissionReceived = new HashMap<>();
        historyMote = new HashMap<>();
        subscribeToGateways();
        subscribeToApps();
    }

    public void reset() {
        transmissionReceived.clear();
        historyMote.clear();
    }

    public NetworkServer setChooseGatewayStrategy(BinaryOperator<Map.Entry<Long, LoraTransmission<LoraWanPacket>>> strategy) {
        chooseGatewayStrategy = strategy;
        return this;
    }

    private void subscribeToGateways() {
        mqttClient.subscribe(Topics.getGatewayToNetServer("+", "+", "+"),
            (topic, msg) -> {
                var transmission = mqttClient.convertMessage(msg, TransmissionWrapper.class).getTransmission();
                //get mote id
                var moteId = getMote(topic);
                //get gateway id
                var gatewayId = getGateway(topic);
                //add trans to map with all check
                if (!transmissionReceived.containsKey(moteId)) {
                    transmissionReceived.put(moteId, new HashMap<>());
                    historyMote.put(moteId, new LinkedList<>());
                }
                transmissionReceived.get(moteId).put(gatewayId, transmission);
                //check if packet is duplicated (is not send to app)
                if (historyMote.get(moteId).stream().noneMatch(t-> t.equals(transmission))) {
                    mqttClient.publish(Topics.getNetServerToApp(transmission.getContent().getReceiverEUI(), moteId), msg);
                }
                historyMote.get(moteId).add(transmission);
            });
    }
    //TODO add check
    private long getMote(String topic) {
        var list = Arrays.asList(topic.split("/"));
        var index = list.indexOf("node");
        return Long.valueOf(list.get(index + 1));
    }
    //TODO add check
    private long getGateway(String topic) {
        var list = Arrays.asList(topic.split("/"));
        var index = list.indexOf("gateway");
        return Long.valueOf(list.get(index + 1));
    }

    //TODO add check
    private long getApp(String topic) {
        var list = Arrays.asList(topic.split("/"));
        var index = list.indexOf("application");
        return Long.valueOf(list.get(index + 1));
    }

    private void subscribeToApps() {
        mqttClient.subscribe(Topics.getAppToNetServer("+", "+"),
            (topic, msg) -> {
                //get mote id
                var moteId = getMote(topic);
                //find best gateway
                if (transmissionReceived.containsKey(moteId)) {
                    var gatewayId = transmissionReceived.get(moteId)
                        .entrySet()
                        .stream()
                        .reduce((e1, e2) -> chooseGatewayStrategy.apply(e1,e2))
                        .map(Map.Entry::getKey)
                        .orElseThrow(() -> new IllegalStateException("no gateway available for the mote: " + moteId));
                    //send to best gateway
                    mqttClient.publish(Topics.getNetServerToGateway(getApp(topic), gatewayId, moteId), msg);
                }
            });
    }

    private Map.Entry<Long, LoraTransmission<LoraWanPacket>> chooseByTransmissionPower(
            Map.Entry<Long, LoraTransmission<LoraWanPacket>> e1,
            Map.Entry<Long, LoraTransmission<LoraWanPacket>> e2) {
        return e1.getValue().getTransmissionPower() >= e2.getValue().getTransmissionPower() ? e1 : e2;
    }
}
