package gui.util;

import util.SettingsReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;


/**
 * Class used to load GUI images.
 */
public class ImageLoader {
    public static final BufferedImage IMAGE_MOTE;
    public static final BufferedImage IMAGE_USERMOTE_ACTIVE;
    public static final BufferedImage IMAGE_USERMOTE_INACTIVE;
    public static final BufferedImage IMAGE_GATEWAY;

    public static final Image IMAGE_CIRCLE_SELECTED;
    public static final Image IMAGE_CIRCLE_UNSELECTED;
    public static final Image IMAGE_EDIT_ICON;

    static {
        SettingsReader reader = SettingsReader.getInstance();

        IMAGE_MOTE = loadNetworkEntityImage(reader.getMoteImagePath());
        IMAGE_USERMOTE_ACTIVE = loadNetworkEntityImage(reader.getActiveUsermoteImagePath());
        IMAGE_USERMOTE_INACTIVE = loadNetworkEntityImage(reader.getInactiveUsermoteImagePath());
        IMAGE_GATEWAY = loadNetworkEntityImage(reader.getGatewayImagePath());

        IMAGE_CIRCLE_SELECTED = loadInputProfileImage(reader.getSelectedCircleImagePath());
        IMAGE_CIRCLE_UNSELECTED = loadInputProfileImage(reader.getUnselectedCircleImagePath());
        IMAGE_EDIT_ICON = loadInputProfileImage(reader.getEditIconImagePath());
    }

    private static BufferedImage loadNetworkEntityImage(String path) {
        try {
            BufferedImage icon = ImageIO.read(GUIUtil.class.getResource(path));
            int w = icon.getWidth();
            int h = icon.getHeight();
            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(0.2, 0.2);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            return scaleOp.filter(icon, after);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not image at '%s'", path));
        }
    }

    private static Image loadInputProfileImage(String path) {
        try {
            return ImageIO.read(GUIUtil.class.getResource(path))
                .getScaledInstance(23, 23, 0);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not image at '%s'", path));
        }
    }
}
