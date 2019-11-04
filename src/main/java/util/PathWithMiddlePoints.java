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
        originalWayPoint = new LinkedList<>();
        addPositions(points);
    }

    private static void loadSubPath() {
        //TODO
    }

    @Override
    public void addPositions(List<GeoPosition> newPath) {
        if (!newPath.isEmpty()) {
            originalWayPoint.addAll(newPath);
            addPosition(newPath.get(0));
            for (int i = originalWayPoint.size() - newPath.size(); i < originalWayPoint.size() - 1; i++) {
                super.addPositions(mapToSubPath.get(new Pair<>(originalWayPoint.get(i), originalWayPoint.get(i + 1))));
                addPosition(originalWayPoint.get(i + 1));
            }
        }
    }

    public List<GeoPosition> getOriginalWayPoint() {
        return originalWayPoint;
    }
}
