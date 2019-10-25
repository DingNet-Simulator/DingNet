package GUI.MapViewer;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Paints waypoints on the JXMapViewer. This is an
 * instance of Painter that only can draw on to JXMapViewers.
 * @param <W> the waypoint type
 * @author rbair
 */
public class NumberPainter<W extends Waypoint> extends AbstractPainter<JXMapViewer> {
    private Map<W,Integer> waypoints = new HashMap<>();
    private int xOffset;
    private int yOffset;


    public NumberPainter(Type type) {
        this.xOffset = type.xOffset;
        this.yOffset = type.yOffset;

        setAntialiasing(true);
        setCacheable(false);
    }


    /**
     * Gets the current set of waypoints to paint
     * @return a typed Set of Waypoints
     */
    public Set<W> getWaypoints() {
        return Collections.unmodifiableSet(waypoints.keySet());
    }

    /**
     * Sets the current set of waypoints to paint
     * @param waypoints the new Set of Waypoints to use
     */
    public void setWaypoints(Map<? extends W, Integer> waypoints) {
        this.waypoints.clear();
        this.waypoints.putAll(waypoints);
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {

        Rectangle viewportBounds = map.getViewportBounds();

        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (W w : getWaypoints()) {
            NumberRenderer renderer = new NumberRenderer(waypoints.get(w), xOffset, yOffset);
            renderer.paintWaypoint(g, map, w);
        }

        g.translate(viewportBounds.getX(), viewportBounds.getY());

    }

    public enum Type {
        MOTE(20, -20),
        GATEWAY(15, -30);

        public int xOffset;
        public int yOffset;

        Type(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}

