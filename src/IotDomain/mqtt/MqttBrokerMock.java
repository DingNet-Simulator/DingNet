package IotDomain.mqtt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MqttBrokerMock {

    private final Map<MqttMock, List<String>> clientSubscribed;

    private static MqttBrokerMock ourInstance = new MqttBrokerMock();

    public static MqttBrokerMock getInstance() {
        return ourInstance;
    }

    private MqttBrokerMock() {
        clientSubscribed = new HashMap<>();
    }

    public void connect(MqttMock instance) {
        if (clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.put(instance, new LinkedList<>());
    }

    public void disconnect(MqttMock instance) {
        if (!clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.remove(instance);
    }

    public void publish(String topic, MqttMessage message) {
        clientSubscribed.entrySet().stream()
            .peek(e -> e.setValue(e.getValue()
                                    .stream()
                                    .filter(t -> t.startsWith(topic))
                                    .collect(Collectors.toList())))
            .forEach(e-> e.getValue().forEach(t -> e.getKey().dispatch(t, message)));
    }

    public void subscribe(MqttMock instance, String topicFilter) {
        if (!clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.get(instance).add(topicFilter);
    }

    public void unsubscribe(MqttMock instance, String topicFilter) {
        if (!clientSubscribed.containsKey(instance)) {
            throw new IllegalStateException();
        }
        clientSubscribed.get(instance).remove(topicFilter);
    }
}
