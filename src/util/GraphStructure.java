package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphStructure {
    private Map<Long, GeoPosition> waypoints;
    private Set<Connection> connections;


    public GraphStructure() {
        waypoints = new HashMap<>();
        connections = new HashSet<>();
    }

    public void addWayPoint(long ID, GeoPosition pos) {
        waypoints.put(ID, pos);
    }

    public void addPath(Connection connection) {
        connections.add(connection);
    }

    public Map<Long, GeoPosition> getWayPoints() {
        return waypoints;
    }

    public Set<Connection> getConnections() {
        return connections;
    }



}
