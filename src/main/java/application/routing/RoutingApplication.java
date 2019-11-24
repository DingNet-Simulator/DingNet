package application.routing;

import application.Application;
import iot.lora.LoraWanPacket;
import iot.lora.MessageType;
import iot.mqtt.BasicMqttMessage;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Mote;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.GraphStructure;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoutingApplication extends Application {
    private Map<Long, List<GeoPosition>> routes;
    private Map<Long, GeoPosition> lastPositions;
    private GraphStructure graph;
    private PathFinder pathFinder;

    public RoutingApplication(PathFinder pathFinder, GraphStructure graph) {
        super(List.of(Topics.getNetServerToApp("+", "+")));
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.graph = graph;
        this.pathFinder = pathFinder;
    }


    private void handleRouteRequest(LoraWanPacket message) {
        var body = Arrays.stream(Converter.toObjectType(message.getPayload()))
            .skip(1)
            .collect(Collectors.toList());
        long deviceEUI = message.getSenderEUI();

        GeoPosition motePosition;
        GeoPosition destinationPosition;

        if (lastPositions.containsKey(deviceEUI)) {
            // Only the current position is transmitted
            byte[] rawPositions = new byte[8];
            IntStream.range(0, 8).forEach(i -> rawPositions[i] = body.get(i));
            ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);

            motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
            destinationPosition = routes.get(deviceEUI).get(routes.get(deviceEUI).size()-1);
        } else {
            // Both the current position as well as the destination of the mote are transmitted
            byte[] rawPositions = new byte[16];
            IntStream.range(0, 16).forEach(i -> rawPositions[i] = body.get(i));

            ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);
            motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
            destinationPosition = new GeoPosition(byteBuffer.getFloat(8), byteBuffer.getFloat(12));
        }


        List<GeoPosition> routeMote = this.pathFinder.retrievePath(graph, motePosition, destinationPosition);
        this.routes.put(deviceEUI, routeMote);

        // Compose packet: up to 24 bytes for now, which can store 3 geopositions (in float)
        int amtPositions = Math.min(routeMote.size() - 1, 3);
        ByteBuffer payloadRaw = ByteBuffer.allocate(8 * amtPositions);

        for (GeoPosition pos : routeMote.subList(1, amtPositions + 1)) {
            payloadRaw.putFloat((float) pos.getLatitude());
            payloadRaw.putFloat((float) pos.getLongitude());
        }

        List<Byte> payload = new ArrayList<>();
        for (byte b : payloadRaw.array()) {
            payload.add(b);
        }

        if (!lastPositions.containsKey(deviceEUI) || !lastPositions.get(deviceEUI).equals(motePosition)) {
            lastPositions.put(deviceEUI, motePosition);
        }

        BasicMqttMessage routeMessage = new BasicMqttMessage(payload);
        this.mqttClient.publish(Topics.getAppToNetServer(message.getReceiverEUI(), deviceEUI), routeMessage);
    }


    public List<GeoPosition> getRoute(Mote mote) {
        if (routes.containsKey(mote.getEUI())) {
            return routes.get(mote.getEUI());
        }
        return new ArrayList<>();
    }

    @Override
    public void consumePackets(String topicFilter, TransmissionWrapper transmission) {
        var message = transmission.getTransmission().getContent();
        // Only handle packets with a route request
        var messageType = message.getPayload()[0];
        if (messageType == MessageType.REQUEST_PATH.getCode() || messageType == MessageType.REQUEST_UPDATE_PATH.getCode()) {
            handleRouteRequest(message);
        }
    }

    public void clean() {
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
    }
}
