package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PathWithMiddlePoints extends Path {

    //TODO do it static?
    private static final Map<Pair<GeoPosition, GeoPosition>, List<GeoPosition>> mapToSubPath;
    static {
        mapToSubPath = new HashMap<>();
        loadSubPath();
    }

    private final List<GeoPosition> originalWayPoint;

    public PathWithMiddlePoints() {
        this(new LinkedList<>());
    }

    public PathWithMiddlePoints(List<GeoPosition> points) {
        originalWayPoint = points;
        var finalPath = new LinkedList<GeoPosition>();
        for (int i = 0; i < originalWayPoint.size()-1; i++) {
            finalPath.add(originalWayPoint.get(i));
            finalPath.addAll(mapToSubPath.get(new Pair<>(originalWayPoint.get(i), originalWayPoint.get(i+1))));
        }
        finalPath.add(originalWayPoint.get(originalWayPoint.size()-1));
        setPath(finalPath);
    }

    private static void loadSubPath() {
        //TODO
    }

    public List<GeoPosition> getOriginalWayPoint() {
        return originalWayPoint;
    }
}
