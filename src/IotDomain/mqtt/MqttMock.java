package IotDomain.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MqttMock implements MqttClientBasicApi {

    private final Map<String, BiConsumer<String, MqttMessage>> subscribed = new HashMap<>();
    private final MqttBrokerMock broker = MqttBrokerMock.getInstance();

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
     * Subscribe to all the topic that start with startTopic
     * @param startTopic
     * @param messageListener
     */
    @Override
    public void subscribe(String startTopic, BiConsumer<String, MqttMessage> messageListener) {
        if (!subscribed.containsKey(startTopic)) {
            broker.subscribe(this, startTopic);
        }
        subscribed.put(startTopic, messageListener);
    }

    @Override
    public void unsubscribe(String topic) {
        subscribed.remove(topic);
        broker.unsubscribe(this, topic);
    }

    public void dispatch(String topic, MqttMessage message) {
        if (subscribed.containsKey(topic)) {
            subscribed.get(topic).accept(topic, message);
        }
    }
}
