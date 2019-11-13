package util;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;
import java.util.stream.Collectors;

public class GraphStructure {
    private static GraphStructure instance = null;

    private Map<Long, GeoPosition> wayPoints;
    private Map<Long, Connection> connections;

    private long newWayPointID;
    private long newConnectionID;


    private GraphStructure(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        init(wayPoints, connections);
    }

    private void init(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        this.wayPoints = wayPoints;
        this.connections = connections;

        newWayPointID = wayPoints.keySet().stream()
            .max(Long::compare)
            .orElse(0L) + 1;
        newConnectionID = connections.keySet().stream()
            .max(Long::compare)
            .orElse(0L) + 1;
    }


    public static GraphStructure getInstance() {
        if (!isInitialized()) {
            throw new IllegalStateException("GraphStructure not initialized yet before calling 'getInstance'.");
        }
        return instance;
    }


    public static GraphStructure initialize(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        if (isInitialized()) {
            throw new UnsupportedOperationException("GraphStructure has already been initialized.");
        }

        instance = new GraphStructure(wayPoints, connections);
        return instance;
    }


    public static boolean isInitialized() {
        return instance != null;
    }

    public GraphStructure reInitialize(Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        this.init(wayPoints, connections);
        return instance;
    }




    public void addWayPoint(GeoPosition pos) {
        this.addWayPoint(this.newWayPointID, pos);
    }

    public void addConnection(Connection connection) {
        if (connection.getTo() == connection.getFrom()) {
            throw new IllegalArgumentException(String.format("Cannot have circular connections (Waypoint %d -> Waypoint %d).",
                connection.getFrom(), connection.getTo()));
        }
        this.addConnection(this.newConnectionID, connection);
    }


    /**
     * Add a waypoint to the graph.
     * @param id The id of the new waypoint.
     * @param position The waypoint itself
     * @throws IllegalStateException if a waypoint already exists with the given id.
     */
    private void addWayPoint(long id, GeoPosition position) {
        if (wayPoints.containsKey(id)) {
            throw new IllegalStateException(String.format("WayPoint with id=%d exists already.", id));
        }

        wayPoints.put(id, position);
        if (id >= this.newWayPointID) {
            this.newWayPointID = id + 1;
        }
    }


    /**
     * Add a connection to the graph.
     * @param id The id of the new connection.
     * @param connection The connection itself.
     * @throws IllegalStateException if a connection already exists with the given id.
     */
    private void addConnection(long id, Connection connection) {
        if (connections.containsKey(id)) {
            throw new IllegalStateException(String.format("Connection with id=%d exists already.", id));
        } else if (!wayPoints.containsKey(connection.getFrom())) {
            throw new IllegalStateException(String.format("Could not add connection: waypoint with id=%d does not exist yet.", connection.getFrom()));
        } else if (!wayPoints.containsKey(connection.getTo())) {
            throw new IllegalStateException(String.format("Could not add connection: waypoint with id=%d does not exist yet.", connection.getTo()));
        }

        connections.put(id, connection);
        if (id >= this.newConnectionID) {
            this.newConnectionID = id + 1;
        }
    }




    /**
     * Gets the ID of the closest waypoint to a given location.
     * @param pos The location.
     * @return The ID of the closest waypoint, or a runtime exception in case no waypoints are present.
     */
    public Long getClosestWayPoint(GeoPosition pos) {
        return this.getClosestWayPointWithDistance(pos).getLeft();
    }

    /**
     * Get the closest waypoint to a given location, if the closest waypoint is withing a specified range of that location.
     * @param pos The location.
     * @param range The maximum range between the closest waypoint and the position (expressed in km).
     * @return The closest waypoint ID if it is within the specified range, otherwise an empty Optional.
     */
    public Optional<Long> getClosestWayPointWithinRange(GeoPosition pos, double range) {
        var wp = this.getClosestWayPointWithDistance(pos);
        if (wp.getRight() <= range) {
            return Optional.of(wp.getLeft());
        }
        return Optional.empty();
    }

    /**
     * Get the closest waypoint to a given location, including the distance between the waypoint and the location.
     * @param pos The location.
     * @return A pair of the waypoint id and the distance (in km) to it.
     */
    private Pair<Long, Double> getClosestWayPointWithDistance(GeoPosition pos) {
        assert !wayPoints.isEmpty();

        Map<Long, Double> distances = new HashMap<>();

        for (var me : wayPoints.entrySet()) {
            distances.put(me.getKey(), MapHelper.distance(me.getValue(), pos));
        }

        return distances.entrySet().stream()
            .min(Comparator.comparing(Map.Entry::getValue))
            .map(o -> new Pair<>(o.getKey(), o.getValue()))
            .orElseThrow();
    }


    public GeoPosition getWayPoint(long wayPointId) {
        return wayPoints.get(wayPointId);
    }

    public Map<Long, GeoPosition> getWayPoints() {
        return wayPoints;
    }


    Connection getConnection(long connectionId) {
        return this.connections.get(connectionId);
    }


    public Map<Long, Connection> getConnections() {
        return connections;
    }


    /**
     * Retrieve a list of connections which have the given waypoint as source.
     * @param wayPointId The waypoint at which the connections start.
     * @return A list of connections which all start at {@code wayPointId}.
     */
    public List<Connection> getOutgoingConnections(long wayPointId) {
        return connections.values().stream()
            .filter(c -> c.getFrom() == wayPointId)
            .collect(Collectors.toList());
    }


    /**
     * Retrieve a list of connection ids which have the given waypoint as source.
     * @param wayPointId The waypion at which the connections start.
     * @return A list of connection ids which all start at {@code wayPointId}.
     */
    public List<Long> getOutgoingConnectionsById(long wayPointId) {
        return connections.entrySet().stream()
            .filter(c -> c.getValue().getFrom() == wayPointId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Check whether a connection exists between 2 waypoints.
     * @param fromWayPointId The source waypoint id.
     * @param toWayPointId The destination waypoint id.
     * @return True if a connection exists.
     */
    public boolean connectionExists(long fromWayPointId, long toWayPointId) {
        return getConnection(fromWayPointId, toWayPointId) != null;
    }


    /**
     * Get the connection which starts and ends at the given waypoint ids.
     * @param fromWayPointId The waypoint id at which the connection starts.
     * @param toWayPointId The waypoint id at which the connection ends.
     * @return Either the connection which has the right source and destination, or {@code null} if no such connection exists.
     */
    private Connection getConnection(long fromWayPointId, long toWayPointId) {
        for (var conn : connections.values()) {
            if (conn.getFrom() == fromWayPointId && conn.getTo() == toWayPointId) {
                return conn;
            }
        }
        return null;
    }


    /**
     * Delete a waypoint in the graph.
     * Note: this also shortens the paths of motes which make use of this waypoint.
     * @param wayPointId The id of the waypoint to be deleted.
     * @param environment The environment of the simulation.
     */
    public void deleteWayPoint(long wayPointId, Environment environment) {
        wayPoints.remove(wayPointId);
        var connToDelete  = connections.entrySet().stream()
            .filter(o -> o.getValue().getTo() == wayPointId || o.getValue().getFrom() == wayPointId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (var conn : connToDelete) {
            connections.remove(conn);
        }

        // Make sure to also delete part of the paths of motes which use this waypoint
        environment.removeWayPointFromMotes(wayPointId);
    }


    /**
     * Delete a connection which starts at {@code fromWayPointId} and ends at {@code toWayPointId}.
     * Note: this also shortens the paths of motes which make use of this connection.
     * @param fromWayPointId The waypoint id at which the connection starts.
     * @param toWayPointId The waypoint id at which the connection ends.
     * @param environment The environment of the simulation.
     */
    public void deleteConnection(long fromWayPointId, long toWayPointId, Environment environment) {
        var possibleConnections = connections.entrySet().stream()
            .filter(o -> o.getValue().getTo() == toWayPointId && o.getValue().getFrom() == fromWayPointId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (possibleConnections.size() == 0) {
            return;
        }
        assert possibleConnections.size() == 1;

        environment.removeConnectionFromMotes(possibleConnections.get(0));
        connections.remove(possibleConnections.get(0));
    }

}
