package util;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;
import java.util.stream.Collectors;

public class Path implements Iterable<GeoPosition> {
    // FIXME use IDs here to identify the connections
    private List<Connection> connections;

    // The Graph which contains all the waypoints and connections
    private GraphStructure graphStructure;


    public Path() { this(new ArrayList<>()); }

    public Path(List<Connection> connections) {
        this.connections = connections;
        this.graphStructure = GraphStructure.getInstance();
    }

    public LinkedList<GeoPosition> getWayPoints() {
        LinkedList<GeoPosition> result = new LinkedList<>();

        for (var con : this.connections) {
            result.add(graphStructure.getWayPoint(con.getFrom()));
        }

        // Add the last point separately
        result.add(graphStructure.getWayPoint(this.connections.get(this.connections.size() - 1).getTo()));

        return result;
    }

    @NotNull
    public Iterator<GeoPosition> iterator() {
        return getWayPoints().iterator();
    }

    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }


    public List<Long> getConnectionsByID() {
        var connectionsMap = graphStructure.getConnections();

        return this.connections.stream()
            .map(c -> connectionsMap.entrySet().stream()
                .filter(e -> c.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .get(0))
            .collect(Collectors.toList());
    }

    /**
     * Remove all the waypoints in the path from the given waypoint (including this waypoint itself)
     * @param waypointId the id of the waypoint from which the path should be shortened
     */
    public void shortenPathFromWayPoint(long waypointId) {
        int index = 0;

        for (var conn : this.connections) {
            if (conn.getFrom() == waypointId || conn.getTo() == waypointId) {
                break;
            }
            index++;
        }

        this.connections = this.connections.subList(0, index);
    }

    public void shortenPathFromConnection(long connectionId) {
        var connection = GraphStructure.getInstance().getConnection(connectionId);
        int index = 0;

        for (var conn : this.connections) {
            if (conn.equals(connection)) {
                break;
            }
            index++;
        }

        this.connections = this.connections.subList(0, index);
    }
}
