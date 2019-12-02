package application.routing;

import application.Application;
import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.lora.MessageType;
import iot.mqtt.BasicMqttMessage;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.GraphStructure;
import util.MapHelper;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoutingApplication extends Application {
    // Decides how many positions of the route are sent in a single MQTT message
    private final int AMOUNT_OF_POSITIONS_SENT = 3;

    // The routes stored per device
    private Map<Long, List<GeoPosition>> routes;

    // The last recorded positions of the requesting user motes
    private Map<Long, GeoPosition> lastPositions;

    // The graph with waypoints and connections
    private GraphStructure graph;

    // The route finding algorithm that is used to handle routing requests
    private PathFinder pathFinder;

    // The environment of the simulation
    private Environment environment;



    public RoutingApplication(PathFinder pathFinder, GraphStructure graph, Environment environment) {
        super(List.of(Topics.getNetServerToApp("+", "+")));
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.graph = graph;
        this.pathFinder = pathFinder;
        this.environment = environment;
    }

    private BasicMqttMessage generateMQTTMessageRoute(List<GeoPosition> route, boolean includeFirst) {
        int startOffset = includeFirst ? 0 : 1;

        // Compose the reply packet: up to 24 bytes for now, which can store 3 geopositions (in float)
        int amtPositions = Math.min(route.size() - startOffset, AMOUNT_OF_POSITIONS_SENT);
        ByteBuffer payloadRaw = ByteBuffer.allocate(8 * amtPositions);

        for (GeoPosition pos : route.subList(startOffset, amtPositions + startOffset)) {
            payloadRaw.putFloat((float) pos.getLatitude());
            payloadRaw.putFloat((float) pos.getLongitude());
        }

        List<Byte> payload = new ArrayList<>();
        for (byte b : payloadRaw.array()) {
            payload.add(b);
        }

        return new BasicMqttMessage(payload);
    }


    /**
     * Handle a route request message by replying with (part of) the route to the requesting device.
     * @param message The message which contains the route request.
     */
    private void handleRouteRequest(LoraWanPacket message) {
        var body = Arrays.stream(Converter.toObjectType(message.getPayload()))
            .skip(1) // Skip the first byte since this indicates the message type
            .collect(Collectors.toList());
        long deviceEUI = message.getSenderEUI();

        GeoPosition motePosition;
        GeoPosition destinationPosition;

        // This is the first request the mote has made for a route
        //  -> both the current position as well as the destination of the mote are transmitted
        byte[] rawPositions = new byte[16];
        IntStream.range(0, 16).forEach(i -> rawPositions[i] = body.get(i));

        ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);
        motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
        destinationPosition = new GeoPosition(byteBuffer.getFloat(8), byteBuffer.getFloat(12));


        // Use the routing algorithm to calculate the path for the mote
        List<GeoPosition> routeMote = this.pathFinder.retrievePath(graph, motePosition, destinationPosition);
        this.routes.put(deviceEUI, routeMote);


        // Update the position of the mote if it has changed since the previous time
        if (!lastPositions.containsKey(deviceEUI) || !lastPositions.get(deviceEUI).equals(motePosition)) {
            lastPositions.put(deviceEUI, motePosition);
        }

        BasicMqttMessage routeMessage = generateMQTTMessageRoute(routeMote, true);

        // Send the reply (via MQTT) to the requesting device
        this.mqttClient.publish(Topics.getAppToNetServer(message.getReceiverEUI(), deviceEUI), routeMessage);
    }


    private void handleRouteUpdate(LoraWanPacket message) {
        this.handleRouteUpdate(message, true);
    }

    private void handleRouteUpdate(LoraWanPacket message, boolean replyWhenUnchanged) {
        var body = Arrays.stream(Converter.toObjectType(message.getPayload()))
            .skip(1) // Skip the first byte since this indicates the message type
            .collect(Collectors.toList());
        long deviceEUI = message.getSenderEUI();

        // Extract the position of the mote from the GPS sensor reading
        this.retrieveSensorData((Mote) environment.getNetworkEntityById(deviceEUI), body).entrySet().stream()
            .filter(e -> e.getKey().equals(MoteSensor.GPS))
            .map(Map.Entry::getValue)
            .findFirst()
            .ifPresent(data -> {
                byte[] rawPositions = new byte[8];
                IntStream.range(0, 8).forEach(i -> rawPositions[i] = body.get(i));

                ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);
                GeoPosition motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));

                var currentRoute = routes.get(deviceEUI);

                // Shorten the path based on the received mote location
                for (int i = 1; i < Math.min(AMOUNT_OF_POSITIONS_SENT + 1, currentRoute.size()); i++) {
                    if (MapHelper.equalsGeoPosition(motePosition, currentRoute.get(i))) {
                        currentRoute = currentRoute.subList(i, currentRoute.size());
                        routes.put(deviceEUI, currentRoute);
                        break;
                    }
                }

                // If only 2 (or 1) positions left, the user mote could already be travelling over the connection to the destination
                if (currentRoute.size() <= 2) {
                    return;
                }

                // Calculate the path, starting from the NEXT position in the path
                //  (since the user could already be moving over a connection at this point)
                var newRoute = this.pathFinder.retrievePath(graph, currentRoute.get(1), currentRoute.get(currentRoute.size() - 1));

                if (currentRoute.subList(1, Math.min(currentRoute.size(), 1 + AMOUNT_OF_POSITIONS_SENT))
                    .equals(newRoute.subList(0, Math.min(newRoute.size(), AMOUNT_OF_POSITIONS_SENT)))) {
                    if (replyWhenUnchanged) {
                        var mqttMessage = this.generateMQTTMessageRoute(newRoute, true);
                        this.mqttClient.publish(Topics.getAppToNetServer(message.getReceiverEUI(), deviceEUI), mqttMessage);
                    }
                } else {
                    var mqttMessage = this.generateMQTTMessageRoute(newRoute, true);

                    // Add the current position of the mote to the route as well (was not used for path finding)
                    newRoute.add(0, currentRoute.get(0));

                    // Adjust existing route if the new found path is different
                    routes.put(deviceEUI, newRoute);
                    this.mqttClient.publish(Topics.getAppToNetServer(message.getReceiverEUI(), deviceEUI), mqttMessage);
                }

            });
    }


    /**
     * Get a stored route for a specific mote.
     * @param mote The mote from which the cached route is requested.
     * @return The route as a list of geo coordinates.
     */
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
        if (messageType == MessageType.REQUEST_PATH.getCode()) {
            handleRouteRequest(message);
        } else if ((messageType == MessageType.SENSOR_VALUE.getCode() || messageType == MessageType.KEEPALIVE.getCode())
            && lastPositions.containsKey(message.getSenderEUI())) {
            handleRouteUpdate(message);
        }
    }


    /**
     * Clean the cached routes and mote positions.
     */
    public void clean() {
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
    }
}
