package iot.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MqttMock implements MqttClientBasicApi {

    private final Map<String, MqttMessageConsumer> subscribed = new HashMap<>();
    private final MqttBrokerMock broker = MqttBrokerMock.getInstance();


    public MqttMock() {
        this.connect();
    }

    @Override
    public void connect() {
        broker.connect(this);
    }

    @Override
    public void disconnect() {
        broker.disconnect(this);
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        broker.publish(topic, message);
    }

    /**
     * Subscribe to all the topic that start with topicFilter
     * @param topicFilter
     * @param messageListener
     */
    @Override
    public <T extends MqttMessage> void subscribe(String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageListener) {
        if (!subscribed.containsKey(topicFilter)) {
            broker.subscribe(this, topicFilter);
        }
        subscribed.put(topicFilter, new MqttMessageConsumer<T>(messageListener, classMessage));
    }

    @Override
    public void unsubscribe(String topicFilter) {
        subscribed.remove(topicFilter);
        broker.unsubscribe(this, topicFilter);
    }

    public void dispatch(String filter, String topic, MqttMessage message) {
        if (subscribed.containsKey(filter)) {
            subscribed.get(filter).accept(topic, message);
        }
    }

    private class MqttMessageConsumer<T extends MqttMessage> {

        private final BiConsumer<String, T> consumer;
        private final Class<T> clazz;

        public MqttMessageConsumer(BiConsumer<String, T> consumer, Class<T> clazz) {
            this.consumer = consumer;
            this.clazz = clazz;
        }

        public void accept(String t, MqttMessage message) {
            consumer.accept(t, clazz.cast(message));
        }
    }
}
