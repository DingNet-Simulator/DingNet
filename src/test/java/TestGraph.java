import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestGraph {

    @BeforeEach
    void init() throws NoSuchFieldException, IllegalAccessException {
        Field instance = GraphStructure.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void addWayPoint() {
        GraphStructure graph = GraphStructure.initialize(new HashMap<>(), new HashMap<>());

        graph.addWayPoint(new GeoPosition(1, 1));
        graph.addWayPoint(new GeoPosition(2, 2));

        Collection<GeoPosition> points = graph.getWayPoints().values();
        assertTrue(points.contains(new GeoPosition(1,1)));
        assertTrue(points.contains(new GeoPosition(2,2)));
    }

    @Test
    void addConnection() {
        GraphStructure graph = GraphStructure.initialize(new HashMap<>(), new HashMap<>());

        graph.addWayPoint(new GeoPosition(1, 1));
        graph.addWayPoint(new GeoPosition(2, 2));

        Map<Long, GeoPosition> wayPoints = graph.getWayPoints();
        Long idPoint1 = wayPoints.entrySet().stream().filter(e -> e.getValue().equals(new GeoPosition(1,1))).map(Map.Entry::getKey).findFirst().orElse(-1L);
        Long idPoint2 = wayPoints.entrySet().stream().filter(e -> e.getValue().equals(new GeoPosition(2,2))).map(Map.Entry::getKey).findFirst().orElse(-1L);
        graph.addConnection(new Connection(idPoint1, idPoint2));

        assertFalse(graph.getConnections().isEmpty());
        assertEquals(graph.getConnections().values().iterator().next().getFrom(), idPoint1);
        assertEquals(graph.getConnections().values().iterator().next().getTo(), idPoint2);
    }
}
