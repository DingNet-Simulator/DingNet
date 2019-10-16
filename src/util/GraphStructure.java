package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphStructure {
    private Set<GeoPosition> waypoints;
    private Set<Path> paths;


    public GraphStructure() {
        waypoints = new HashSet<>();
        paths = new HashSet<>();
    }

    public void addWayPoint(GeoPosition pos) {
        if (!waypoints.contains(pos)) {
            waypoints.add(pos);
        }
    }

    public void addPath(Path path) {
        if (!paths.contains(path)) {
            paths.add(path);
        }
    }

    public Set<GeoPosition> getWayPoints() {
        return waypoints;
    }

    public Set<Path> getPaths() {
        return paths;
    }



}
