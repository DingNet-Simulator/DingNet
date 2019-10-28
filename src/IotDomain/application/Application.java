package IotDomain.application;

import IotDomain.mqtt.MqttClientBasicApi;
import IotDomain.mqtt.MqttMessage;
import IotDomain.mqtt.MqttMock;

import java.util.List;

public abstract class Application {
    protected MqttClientBasicApi mqttClient;

    protected Application(List<String> topics) {
        this.mqttClient = new MqttMock();
        topics.forEach(t -> this.mqttClient.subscribe(t, this::consumePackets));
    }

    public abstract void consumePackets(String topicFilter, MqttMessage message);
}
