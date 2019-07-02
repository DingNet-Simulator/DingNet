package GUI.MapViewer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 * This is a standard waypoint renderer.
 */
public class PathWaypointRenderer implements WaypointRenderer<Waypoint>
{
    private static final Log log = LogFactory.getLog(MoteNumberWaypointRenderer.class);

    private BufferedImage img = null;

    /**
     * Uses a default wayPoint image
     */
    public PathWaypointRenderer()
    {

        img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g.fillRect(0, 0, 12, 12);
        //reset composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setColor(new Color(102,0,153));
        g.fill(new Ellipse2D.Double(0, 0, 12, 12));


    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w)
    {
        if (img == null)
            return;

        Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());

        int x = (int)Math.round(point.getX() -img.getWidth()/2);
        int y = (int)Math.round(point.getY() -img.getHeight()/2);

        g.drawImage(img, x, y, null);
    }


}
