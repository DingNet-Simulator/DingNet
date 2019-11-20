package iot.mqtt;

import java.util.function.BiConsumer;

public interface MqttClientBasicApi {

    void connect();

    void disconnect();

    void publish(String topic, MqttMessageType message);

    <T extends MqttMessageType> void subscribe(Object subscriber, String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageListener);

    void unsubscribe(Object subscriber, String topicFilter);
}
