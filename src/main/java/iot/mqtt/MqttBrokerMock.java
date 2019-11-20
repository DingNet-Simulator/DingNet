package iot.mqtt;

import util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MqttBrokerMock {

    private static final String WILDCARD_SINGLE_LEVEL = "+";
    private static final String WILDCARD_MULTI_LEVEL = "#";
    private static final String LEVEL_SEPARATOR = "/";

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

    public void publish(String topic, MqttMessageType message) {
        clientSubscribed.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue()
                .stream()
                .filter(f -> checkTopicMatch(topic, f))
                .collect(Collectors.toList())))
            .forEach(e-> e.getRight().forEach(t -> e.getLeft().dispatch(t, topic, message)));
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

    private boolean checkTopicMatch(final String topic, final String filter) {
        var topicSplitted = topic.split(LEVEL_SEPARATOR);
        var filterSplitted = filter.split(LEVEL_SEPARATOR);
        int index = 0;
        while (index < topicSplitted.length && index < filterSplitted.length &&
            (topicSplitted[index].equals(filterSplitted[index]) || filterSplitted[index].equals(WILDCARD_SINGLE_LEVEL))) {
            index++;
        }

        return (index == filterSplitted.length && index == topicSplitted.length) ||
            (index == filterSplitted.length - 1 && filterSplitted[index].equals(WILDCARD_MULTI_LEVEL));
    }
}
