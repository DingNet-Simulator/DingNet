package IotDomain.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MqttMock implements MqttClientBasicApi {

    private final Map<String, BiConsumer<String, MqttMessage>> subscribed = new HashMap<>();
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
    public void subscribe(String topicFilter, BiConsumer<String, MqttMessage> messageListener) {
        if (!subscribed.containsKey(topicFilter)) {
            broker.subscribe(this, topicFilter);
        }
        subscribed.put(topicFilter, messageListener);
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
}
