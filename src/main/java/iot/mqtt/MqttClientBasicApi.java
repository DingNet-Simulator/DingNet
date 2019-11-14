package iot.mqtt;

import java.util.function.BiConsumer;

public interface MqttClientBasicApi {

    void connect();

    void disconnect();

    void publish(String topic, MqttMessage message);

    void subscribe(String topicFilter, BiConsumer<String, MqttMessage> messageListener);

    void unsubscribe(String topicFilter);

    <T> T convertMessage(MqttMessage message, Class<T> clazz);
}
