package util;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;

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


    /**
     * Set the path to a given list of positions.
     * @param positions The list of GeoPositions.
     */
    public void setPath(List<GeoPosition> positions) {
        this.points = positions;
    }



    // NOTE: The following functions are only used during setup/saving of the configuration, not at runtime

    /**
     * Add a waypoint at the end of this path.
     * @param point The waypoint which is added at the end of the path.
     */
    public void addPosition(GeoPosition point) {
        this.points.add(point);
    }

    public void addPositions(List<GeoPosition> points) {
        this.points.addAll(points);
    }


    /**
     * Retrieve the used connections in this path.
     * @return A list of connection ids of the connections in this path.
     */
    public List<Long> getConnectionsByID() {
        var connectionsMap = graphStructure.getConnections();
        List<Long> connections = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            final int index = i;

            long connectionId = connectionsMap.entrySet().stream()
                .filter(o -> o.getValue().getFrom() == graphStructure.getClosestWayPoint(points.get(index))
                    && o.getValue().getTo() == graphStructure.getClosestWayPoint(points.get(index+1)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();

            connections.add(connectionId);
        }

        return connections;
    }


    /**
     * Remove all the waypoints in the path from the given waypoint (including this waypoint itself).
     * @param waypointId The id of the waypoint from which the path should be shortened.
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


    /**
     * Remove waypoints in the path from the given connection (including waypoints in this connection).
     * @param connectionId The id of the connection.
     */
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

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public Optional<GeoPosition> getSource() {
        return isEmpty() ? Optional.empty() : Optional.of(points.get(0));
    }

    public Optional<GeoPosition> getDestination() {
        return isEmpty() ? Optional.empty() : Optional.of(points.get(points.size()-1));
    }
}
