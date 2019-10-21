package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashSet;
import java.util.Set;

public class GraphStructure {
    private Set<GeoPosition> waypoints;
    private Set<Connection> connections;


    public GraphStructure() {
        waypoints = new HashSet<>();
        connections = new HashSet<>();
    }

    public void addWayPoint(GeoPosition pos) {
        waypoints.add(pos);
    }

    public void addPath(Connection connection) {
        connections.add(connection);
    }

    public Set<GeoPosition> getWayPoints() {
        return waypoints;
    }

    public Set<Connection> getConnections() {
        return connections;
    }



}
