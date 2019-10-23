package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphStructure {
    private static GraphStructure instance = null;

    private Map<Long, GeoPosition> wayPoints;
    private Map<Long, Connection> connections;


    private GraphStructure(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        this.wayPoints = wayPoints;
        this.connections = connections;
    }

    public static GraphStructure getInstance() {
        if (!isInitialized()) {
            throw new IllegalStateException("Graphstructure not initialized yet before calling 'getInstance'.");
        }
        return instance;
    }

    public static GraphStructure initialize(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        if (isInitialized()) {
            throw new UnsupportedOperationException("Graphstructure has already been initialized.");
        }

        instance = new GraphStructure(wayPoints, connections);
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public void close() {
        this.wayPoints.clear();
        this.connections.clear();

        GraphStructure.instance = null;
    }




    public void addWayPoint(long id, GeoPosition pos) {
        if (!wayPoints.containsKey(id)) {
            wayPoints.put(id, pos);
        } else {
            throw new RuntimeException(String.format("WayPoint with id=%d exists already.", id));
        }
    }

    public void addConnection(long id, Connection connection) {
        if (!connections.containsKey(id)) {
            connections.put(id, connection);
        } else {
            throw new RuntimeException(String.format("Connection with id=%d exists already.", id));
        }
    }



    /**
     * Gets the ID of the closest wayPoint to a given location.
     * @param pos the location.
     * @return the ID of the closest wayPoint
     */
    public Long getClosestWayPoint(GeoPosition pos) {
        assert !wayPoints.isEmpty();

        Map<Long, Double> distances = new HashMap<>();

        for (var me : wayPoints.entrySet()) {
            distances.put(me.getKey(), MapHelper.distance(me.getValue(), pos));
        }

        return distances.entrySet().stream()
            .min((o1, o2) -> Double.compare(o1.getValue(), o2.getValue()))
            .get()
            .getKey();
    }

    public GeoPosition getWayPoint(long id) {
        return wayPoints.get(id);
    }

    public Map<Long, GeoPosition> getWayPoints() {
        return wayPoints;
    }


    public Connection getConnection(long from, long to) {
        for (var conn : connections.values()) {
            if (conn.getFrom() == from && conn.getTo() == to) {
                return conn;
            }
        }
        return null;
    }

    public Map<Long, Connection> getConnections() {
        return connections;
    }


    public List<Connection> getOutgoingConnections(long wayPointID) {
        return connections.entrySet().stream()
            .filter(o -> o.getValue().getFrom() == wayPointID)
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    /**
     * Check whether a connection exists between 2 wayPoints.
     * @param from the source waypoint ID.
     * @param to the destination waypoint ID.
     * @return True if a connection exists.
     */
    public boolean connectionExists(long from, long to) {
        return getConnection(from, to) != null;
    }



}
