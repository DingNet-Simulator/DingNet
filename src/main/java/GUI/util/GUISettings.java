package GUI.util;

import java.awt.*;

public class GUISettings {
    // TODO: provide a configuration file specifically for the GUI in the future

    public static final int THREADPOOLSIZE = 8;

    public static final boolean USE_ANTIALIASING = true;

    public static final float TRANSPARENCY_POLLUTIONGRID = .3f;
    public static final Color DEFAULT_WAYPOINT_COLOR = new Color(102,0,153);

    public static final String PATH_MOTE_IMAGE = "/images/Mote.png";
    public static final String PATH_USERMOTE_ACTIVE_IMAGE = "/images/Mote-green.png";
    public static final String PATH_USERMOTE_INACTIVE_IMAGE = "/images/Mote-blue.png";
    public static final String PATH_GATEWAY_IMAGE = "images/Gateway.png";

}
