package GUI.MapViewer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 * Paints waypoints on the JXMapViewer. This is an
 * instance of Painter that only can draw on to JXMapViewers.
 * @param <W> the waypoint type
 * @author rbair
 */
public class GatewayNumberWaypointPainter<W extends Waypoint> extends AbstractPainter<JXMapViewer> {
    private WaypointRenderer<? super W> renderer;
    private Map<W,Integer> waypoints = new HashMap<>();

    /**
     * Creates a new instance of WaypointPainter
     */
    public GatewayNumberWaypointPainter() {
        setAntialiasing(true);
        setCacheable(false);
    }

    /**
     * Gets the current set of waypoints to paint
     *
     * @return a typed Set of Waypoints
     */
    public Set<W> getWaypoints() {
        return Collections.unmodifiableSet(waypoints.keySet());
    }

    /**
     * Sets the current set of waypoints to paint
     *
     * @param waypoints the new Set of Waypoints to use
     */
    public void setWaypoints(Map<? extends W,Integer> waypoints) {
        this.waypoints.clear();
        this.waypoints.putAll(waypoints);
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {

        Rectangle viewportBounds = map.getViewportBounds();

        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (W w : getWaypoints()) {
            renderer = new GatewayNumberWaypointRenderer(waypoints.get(w));
            renderer.paintWaypoint(g, map, w);
        }

        g.translate(viewportBounds.getX(), viewportBounds.getY());

    }
}