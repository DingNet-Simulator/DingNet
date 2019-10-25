package util;

import IotDomain.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphStructure {
    private static GraphStructure instance = null;

    private Map<Long, GeoPosition> wayPoints;
    private Map<Long, Connection> connections;

    private long newWayPointID;
    private long newConnectionID;


    private GraphStructure(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        this.wayPoints = wayPoints;
        this.connections = connections;

        newWayPointID = wayPoints.entrySet().stream()
            .map(Map.Entry::getKey)
            .max(Long::compare)
            .orElse(0L) + 1;
        newConnectionID = connections.entrySet().stream()
            .map(Map.Entry::getKey)
            .max(Long::compare)
            .orElse(0L) + 1;
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




    public void addWayPoint(GeoPosition pos) {
        this.addWayPoint(this.newWayPointID, pos);
    }

    public void addConnection(Connection connection) {
        this.addConnection(this.newConnectionID, connection);
    }


    private void addWayPoint(long id, GeoPosition pos) {
        if (wayPoints.containsKey(id)) {
            throw new RuntimeException(String.format("WayPoint with id=%d exists already.", id));
        }

        wayPoints.put(id, pos);
        if (id >= this.newWayPointID) {
            this.newWayPointID = id + 1;
        }
    }

    public void addConnection(long id, Connection connection) {
        if (connections.containsKey(id)) {
            throw new RuntimeException(String.format("Connection with id=%d exists already.", id));
        }

        connections.put(id, connection);
        if (id >= this.newConnectionID) {
            this.newWayPointID = id + 1;
        }
    }

    public void deleteWayPoint(long id, Environment environment) {
        wayPoints.remove(id);
        var connToDelete  = connections.entrySet().stream()
            .filter(o -> o.getValue().getTo() == id || o.getValue().getFrom() == id)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (var conn : connToDelete) {
            connections.remove(conn);
        }

        // Make sure to also delete part of the paths of motes which use this waypoint
        environment.removeWayPointFromMotes(id);
    }

    public void deleteConnection(long idFrom, long idTo, Environment environment) {
        var possibleConnections = connections.entrySet().stream()
            .filter(o -> o.getValue().getTo() == idTo && o.getValue().getFrom() == idFrom)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (possibleConnections.size() == 0) {
            return;
        }
        assert possibleConnections.size() == 1;

        environment.removeConnectionFromMotes(possibleConnections.get(0));
        connections.remove(possibleConnections.get(0));
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



    public Connection getConnection(long id) {
        return this.connections.get(id);
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
