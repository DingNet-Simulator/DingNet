package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphStructure {
    private Map<Long, GeoPosition> waypoints;
    private Map<Long, Connection> connections;


    public GraphStructure() {
        waypoints = new HashMap<>();
        connections = new HashMap<>();
    }

    public void addWayPoint(long id, GeoPosition pos) {
        if (!waypoints.containsKey(id)) {
            waypoints.put(id, pos);
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
        assert !waypoints.isEmpty();

        Map<Long, Double> distances = new HashMap<>();

        for (var me : waypoints.entrySet()) {
            distances.put(me.getKey(), MapHelper.distance(me.getValue(), pos));
        }

        return distances.entrySet().stream()
            .min((o1, o2) -> Double.compare(o1.getValue(), o2.getValue()))
            .get()
            .getKey();
    }

    public GeoPosition getWayPoint(long id) {
        return waypoints.get(id);
    }

    public Map<Long, GeoPosition> getWayPoints() {
        return waypoints;
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


    /**
     * Check whether a connection exists between 2 waypoints.
     * @param from the source waypoint ID.
     * @param to the destination waypoint ID.
     * @return True if a connection exists.
     */
    public boolean connectionExists(long from, long to) {
        return getConnection(from, to) != null;
    }



}
