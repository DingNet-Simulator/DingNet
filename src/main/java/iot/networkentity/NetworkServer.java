package iot.networkentity;


import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;

import java.util.*;

public class NetworkServer {

    // Map moteId -> (Map gatewayId -> lastTransmission)
    private final Map<Long, Map<Long, LoraTransmission<LoraWanPacket>>> transmissionReceived;
    private final Map<Long, List<LoraTransmission<LoraWanPacket>>> historyMote;
    private final MqttClientBasicApi mqttClient;

    public NetworkServer(MqttClientBasicApi mqttClient) {
        this.mqttClient = mqttClient;
        transmissionReceived = new HashMap<>();
        historyMote = new HashMap<>();
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

    private void subscribeToApps() {
        mqttClient.subscribe(Topics.getAppToNetServer("+", "+"),
            (topic, msg) -> {
                //get mote id
                var moteId = getMote(topic);
                //find best gateway
                //send to best gateway
            });
    }
}
