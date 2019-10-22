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

    public Path(GraphStructure graphStructure) { this(graphStructure, new ArrayList<>()); }

    public Path(GraphStructure graphStructure, List<Connection> connections) {
        this.connections = connections;
        this.graphStructure = graphStructure;
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
}
