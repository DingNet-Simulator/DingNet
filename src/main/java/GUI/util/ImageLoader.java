package GUI.util;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageLoader {
    public static final BufferedImage IMAGE_MOTE;
    public static final BufferedImage IMAGE_USERMOTE_ACTIVE;
    public static final BufferedImage IMAGE_USERMOTE_INACTIVE;
    public static final BufferedImage IMAGE_GATEWAY;


    static {
        IMAGE_MOTE = loadImage(GUISettings.PATH_MOTE_IMAGE);
        IMAGE_USERMOTE_ACTIVE = loadImage(GUISettings.PATH_USERMOTE_ACTIVE_IMAGE);
        IMAGE_USERMOTE_INACTIVE = loadImage(GUISettings.PATH_USERMOTE_INACTIVE_IMAGE);
        IMAGE_GATEWAY = loadImage(GUISettings.PATH_GATEWAY_IMAGE);
    }

    private static BufferedImage loadImage(String path) {
        try {
            BufferedImage icon = ImageIO.read(ImageLoader.class.getResource(path));
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
}
