package GUI.MapViewer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 * This is a standard waypoint renderer.
 * @author joshy
 */
public class AWaypointRenderer implements WaypointRenderer<Waypoint>
{
    private static final Log log = LogFactory.getLog(GatewayWaypointRenderer.class);

    private BufferedImage img = null;

    /**
     * Uses a default waypoint image
     */
    public AWaypointRenderer()
    {
        try
        {
            img = ImageIO.read(DefaultWaypointRenderer.class.getResource("/GUI/MapViewer/A.png"));
            int w = img.getWidth();
            int h = img.getHeight();
            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(0.17, 0.17);
            AffineTransformOp scaleOp =
                    new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            img = scaleOp.filter(img, after);
        }
        catch (Exception ex)
        {
            log.warn("couldn't read tower.png", ex);
        }
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w)
    {
        if (img == null)
            return;

        Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());

        int x = (int)point.getX() -img.getWidth() *3/10;
        int y = (int)point.getY() -img.getHeight()*3/10;

        g.drawImage(img, x, y, null);
    }
}
