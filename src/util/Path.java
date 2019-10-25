package util;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;
import java.util.stream.Collectors;

public class Path implements Iterable<GeoPosition> {
    private List<GeoPosition> points;

    // The Graph which contains all the waypoints and connections
    private GraphStructure graphStructure;


    public Path() { this(new ArrayList<>()); }

    public Path(List<GeoPosition> points) {
        this.points = points;
        this.graphStructure = GraphStructure.getInstance();
    }

    public List<GeoPosition> getWayPoints() {
        return this.points;
    }

    @NotNull
    public Iterator<GeoPosition> iterator() {
        return getWayPoints().iterator();
    }




    // NOTE: The following functions are only used during setup/saving of the configuration, not at runtime

    public void addPosition(GeoPosition point) {
        this.points.add(point);
    }


    public List<Long> getConnectionsByID() {
        var connectionsMap = graphStructure.getConnections();
        List<Long> connections = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int index = i;

            long connectionId = connectionsMap.entrySet().stream()
                .filter(o -> o.getValue().getFrom() == graphStructure.getClosestWayPoint(points.get(index))
                    && o.getValue().getTo() == graphStructure.getClosestWayPoint(points.get(index+1)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .get(0);

            connections.add(connectionId);
        }

        return connections;
    }


    /**
     * Remove all the waypoints in the path from the given waypoint (including this waypoint itself)
     * @param waypointId the id of the waypoint from which the path should be shortened
     */
    public void shortenPathFromWayPoint(long waypointId) {
        int index = 0;

        for (var wp : this.points) {
            if (wp.equals(graphStructure.getWayPoint(waypointId))) {
                break;
            }
            index++;
        }

        this.points = this.points.subList(0, index);
    }

    public void shortenPathFromConnection(long connectionId) {
        var connection = graphStructure.getConnection(connectionId);
        int index = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            if (graphStructure.getWayPoint(connection.getFrom()).equals(points.get(i))
                && graphStructure.getWayPoint(connection.getTo()).equals(points.get(i + 1))) {
                break;
            }
            index++;
        }

        this.points = this.points.subList(0, index);
    }
}
