package gui.util;

import java.awt.*;

public class GUISettings {
    // TODO: provide a configuration file specifically for the GUI in the future

    public static final int THREADPOOLSIZE = 8;
    public static final int POLLUTION_GRID_SQUARES = 100;
    public static final int BASE_VISUALIZATION_SPEED = 1;

    public static final boolean USE_ANTIALIASING = true;
    public static final boolean USE_MAP_CACHING = true;

    public static final float TRANSPARENCY_POLLUTIONGRID = .3f;
    public static final Color DEFAULT_WAYPOINT_COLOR = new Color(102,0,153);

    public static final String PATH_MOTE_IMAGE = "/images/Mote.png";
    public static final String PATH_USERMOTE_ACTIVE_IMAGE = "/images/Mote-green.png";
    public static final String PATH_USERMOTE_INACTIVE_IMAGE = "/images/Mote-blue.png";
    public static final String PATH_GATEWAY_IMAGE = "/images/Gateway.png";

    public static final String PATH_CIRCLE_SELECTED = "/images/Circle_selected.png";
    public static final String PATH_CIRCLE_UNSELECTED = "/images/Circle_unselected.png";
    public static final String PATH_EDIT_ICON = "/images/Edit_icon.png";

    public static final String PATH_CACHE_TILEFACTORY = System.getProperty("user.dir") + "/.cache";
}
