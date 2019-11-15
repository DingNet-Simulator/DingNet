package iot.mqtt;

import java.util.function.BiConsumer;

public interface MqttClientBasicApi {

    void connect();

    void disconnect();

    void publish(String topic, MqttMessage message);

    <T extends MqttMessage> void subscribe(String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageListener);

    void unsubscribe(String topicFilter);
}
