package IotDomain.application;

import IotDomain.lora.BasicFrameHeader;
import IotDomain.lora.MessageType;
import IotDomain.mqtt.MqttMessage;
import IotDomain.networkentity.Mote;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RoutingApplication extends Application {
    private static short seqNr = (short) 1;
    private Map<Long, List<GeoPosition>> routes;
    private Map<Long, GeoPosition> lastPositions;
    private GraphStructure graph;
    private PathFinder pathFinder;

    public RoutingApplication(PathFinder pathFinder) {
        super(List.of("application/+/node/+/rx"));
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.graph = GraphStructure.getInstance();
        this.pathFinder = pathFinder;
    }


    private void handleRouteRequest(MqttMessage message) {
        var body = message.getData().subList(1, message.getData().size());
        long deviceEUI = message.getDeviceEUI();

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

        // Compose packet: 24 bytes for now, which can store 3 geopositions (in float)
        ByteBuffer payloadRaw = ByteBuffer.allocate(24);

        for (GeoPosition pos : routeMote.subList(1, Math.min(routeMote.size(), 4))) {
            payloadRaw.putFloat((float) pos.getLatitude());
            payloadRaw.putFloat((float) pos.getLongitude());
        }

        List<Byte> payload = new ArrayList<>();
        for (byte b : payloadRaw.array()) {
            payload.add(b);
        }

        short frameCounter;
        if (!lastPositions.containsKey(deviceEUI) || !lastPositions.get(deviceEUI).equals(motePosition)) {
            frameCounter = seqNr++;
            lastPositions.put(deviceEUI, motePosition);
        } else {
            // Reuse the previous sequence number if the same request is received
            frameCounter = (short) (seqNr-1);
        }

        BasicFrameHeader header = new BasicFrameHeader().setFCnt(frameCounter);

        MqttMessage routeMessage = new MqttMessage(header, payload, deviceEUI, -1L, 1L);
        this.mqttClient.publish(String.format("application/%d/node/%d/tx", message.getApplicationEUI(), deviceEUI), routeMessage);
    }


    public List<GeoPosition> getRoute(Mote mote) {
        if (routes.containsKey(mote.getEUI())) {
            return routes.get(mote.getEUI());
        }
        return new ArrayList<>();
    }

    @Override
    public void consumePackets(String topicFilter, MqttMessage message) {
        // Only handle packets with a route request
        var messageType = message.getData().get(0);
        if (messageType == MessageType.REQUEST_PATH.getCode() || messageType == MessageType.REQUEST_UPDATE_PATH.getCode()) {
            handleRouteRequest(message);
        }
    }
}
