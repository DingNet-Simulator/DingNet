package util;

import iot.mqtt.MQTTClientFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SettingsPropertiesReader {
    private static Path DINGNET_CACHE_PATH = Paths.get(System.getProperty("user.home"), ".DingNet");
    private static Path PATH_TO_CUSTOM_SETTINGS = Paths.get(DINGNET_CACHE_PATH.toString(), "settings");

    private static String DEFAULT_SETTINGS_FILE = "/settings/default.properties";

    private static SettingsPropertiesReader instance;

    private Properties properties;


    private SettingsPropertiesReader() {
        properties = new Properties();

        String customFilePath = Paths.get(PATH_TO_CUSTOM_SETTINGS.toString(), "custom_settings.properties").toString();
        File customSettingsFile = new File(customFilePath);
        if (customSettingsFile.exists()) {
            try {
                properties.load(new FileInputStream(customSettingsFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                properties.load(SettingsPropertiesReader.class.getResourceAsStream(DEFAULT_SETTINGS_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Make the standard DingNet cache directory if it does not exist yet
        File dingNetCache = new File(DINGNET_CACHE_PATH.toUri());
        if (!dingNetCache.exists()) {
            dingNetCache.mkdir();
        }

        // Make sure the GUI map cache directory also exists (if used in this simulation)
        if (this.useMapCaching()) {
            File mapCache = new File(this.getTileFactoryCachePath());
            if (!mapCache.exists()) {
                mapCache.mkdir();
            }
        }

    }

    public static SettingsPropertiesReader getInstance() {
        if (instance == null) {
            instance = new SettingsPropertiesReader();
        }

        return instance;
    }


    // region MQTT

    public MQTTClientFactory.MqttClientType getMQTTClientType() {
        String clientType = properties.getProperty("mqtt.client").trim().toUpperCase();
        return MQTTClientFactory.MqttClientType.valueOf(clientType);
    }

    // endregion


    // region GUI

    public boolean useGUIAntialiasing() {
        return properties.getProperty("gui.UseAntialiasing").trim().toLowerCase().equals("true");
    }

    public boolean useMapCaching() {
        return properties.getProperty("gui.UseMapCaching").trim().toLowerCase().equals("true");
    }

    public boolean startFullScreen() {
        return properties.getProperty("gui.StartFullScreen").trim().toLowerCase().equals("true");
    }



    public int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("gui.ThreadPoolSize").trim());
    }

    public int getBaseVisualizationSpeed() {
        return Integer.parseInt(properties.getProperty("gui.BaseVisualizationSpeed").trim());
    }

    public int getPollutionGridSquares() {
        return Integer.parseInt(properties.getProperty("gui.PollutionGridSquares").trim());
    }

    public int getConnectionLineSize() {
        return Integer.parseInt(properties.getProperty("gui.ConnectionLineSize").trim());
    }

    public int getRoutingPathLineSize() {
        return Integer.parseInt(properties.getProperty("gui.RoutingPathLineSize").trim());
    }

    public int getMotePathLineSize() {
        return Integer.parseInt(properties.getProperty("gui.MotePathLineSize").trim());
    }


    public float getPollutionGridTransparency() {
        return Float.parseFloat(properties.getProperty("gui.TransparencyPollutionGrid").trim());
    }



    public Color getDefaultWaypointColor() {
        return this.getColorFromString(properties.getProperty("gui.DefaultWaypointColor"));
    }

    public Color getConnectionLineColor() {
        return this.getColorFromString(properties.getProperty("gui.ConnectionLineColor"));
    }

    public Color getRoutingPathLineColor() {
        return this.getColorFromString(properties.getProperty("gui.RoutingPathLineColor"));
    }

    public Color getMotePathLineColor() {
        return this.getColorFromString(properties.getProperty("gui.MotePathLineColor"));
    }



    public String getMoteImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.Mote"));
    }

    public String getActiveUsermoteImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.UsermoteActive"));
    }

    public String getInactiveUsermoteImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.UsermoteInactive"));
    }

    public String getGatewayImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.Gateway"));
    }


    public String getSelectedCircleImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.CircleSelected"));
    }

    public String getUnselectedCircleImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.CircleUnselected"));
    }

    public String getEditIconImagePath() {
        return this.adjustPathString(properties.getProperty("gui.imagePath.EditIcon"));
    }


    public String getTileFactoryCachePath() {
        return this.adjustPathString((properties.getProperty("gui.path.CacheTileFactory")));
    }


    // endregion


    // region Helper functions

    /**
     * Replace the ~ present in a path string with the home directory of the current user.
     * @param path The string of the path to be converted.
     * @return The converted path string.
     */
    private String adjustPathString(String path) {
        return path.trim().replace("~", System.getProperty("user.home"));
    }

    /**
     * Retrieve the color specified by the given RGB string (values separated by comma's).
      * @param rgbString The string with the RGB values.
     * @return The corresponding color.
     */
    private Color getColorFromString(String rgbString) {
        String[] colorValues = rgbString.trim().split(",");
        if (colorValues.length != 3) {
            throw new IllegalStateException("A color should have 3 values for Red, Green and Blue respectively");
        }

        return new Color(Integer.parseInt(colorValues[0].trim()),
            Integer.parseInt(colorValues[1].trim()),
            Integer.parseInt(colorValues[2].trim()));
    }

    // endregion
}
