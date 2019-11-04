package GUI.MapViewer;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MoteWayPoint implements Waypoint {

    private final static String MOTE_ICON_PATH = "/images/Mote.png";
    private final static String USERMOTE_ACTIVE_ICON_PATH = "/images/Mote-green.png";
    private final static String USERMOTE_DEACTIVE_ICON_PATH = "/images/Mote-blue.png";
    private final GeoPosition position;
    private final boolean isUserMote;
    private final boolean isActive;
    private BufferedImage icon;

    public MoteWayPoint(GeoPosition position) {
        this(position, false);
    }

    public MoteWayPoint(GeoPosition position, boolean isUserMote) {
        this(position, isUserMote, false);
    }

    public MoteWayPoint(GeoPosition position, boolean isUserMote, boolean isActive) {
        this.position = position;
        this.isUserMote = isUserMote;
        this.isActive = isActive;

        var path = !isUserMote ? MOTE_ICON_PATH :
                    isActive ? USERMOTE_ACTIVE_ICON_PATH : USERMOTE_DEACTIVE_ICON_PATH;

        try {
            icon = ImageIO.read(this.getClass().getResource(path));
            int w = icon.getWidth();
            int h = icon.getHeight();
            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(0.2, 0.2);
            AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            icon = scaleOp.filter(icon, after);
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible load with icon: " + path);
        }
    }

    public BufferedImage getIcon() {
        return icon;
    }

    @Override
    public GeoPosition getPosition() {
        return position;
    }
}
