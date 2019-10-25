package GUI.MapViewer;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

/**
 * Paints waypoints on the JXMapViewer. This is an
 * instance of Painter that only can draw on to JXMapViewers.
 * @param <W> the waypoint type
 * @author rbair
 */
public class PathWaypointPainter<W extends Waypoint> extends AbstractPainter<JXMapViewer> {
    private WaypointRenderer<? super W> renderer;
    private Set<W> waypoints;
    private Color color;

    /**
     * Creates a new instance of WaypointPainter
     */
    public PathWaypointPainter() {
        this(new Color(102,0,153));
    }

    public PathWaypointPainter(Color color) {
        setAntialiasing(true);
        setCacheable(false);
        this.color = color;
    }

    /**
     * Gets the current set of waypoints to paint
     *
     * @return a typed Set of Waypoints
     */
    public Set<W> getWaypoints() {
        return Collections.unmodifiableSet(waypoints);
    }

    /**
     * Sets the current set of waypoints to paint
     *
     * @param waypoints the new Set of Waypoints to use
     */
    public void setWaypoints(Set<W> waypoints) {
        this.waypoints = waypoints;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {

        Rectangle viewportBounds = map.getViewportBounds();

        g.translate(-viewportBounds.getX(), -viewportBounds.getY());

        for (W w : getWaypoints()) {
            renderer = new PathWaypointRenderer(this.color);
            renderer.paintWaypoint(g, map, w);
        }

        g.translate(viewportBounds.getX(), viewportBounds.getY());

    }
}

