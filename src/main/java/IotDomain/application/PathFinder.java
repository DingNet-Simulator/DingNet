package IotDomain.application;

import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;

import java.util.List;

public interface PathFinder {
    List<GeoPosition> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end);
}
