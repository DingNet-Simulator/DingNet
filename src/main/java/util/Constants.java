package util;

import java.nio.file.Paths;

public class Constants {
    public static String PATH_DINGNET_CACHE = Paths.get(System.getProperty("user.home"), ".DingNet").toString();
    public static String PATH_CUSTOM_SETTINGS = Paths.get(PATH_DINGNET_CACHE, "settings").toString();
    public static String DINGNET_CACHE_FILE = Paths.get(PATH_DINGNET_CACHE, "cache.properties").toString();
    public static String DEFAULT_SETTINGS_FILE = "/settings/default.properties";
    public static final String PAHO_ADDRESS = "tcp://test.mosquitto.org:1883";
    public static final String MQTT_LOCALHOST_ADDRESS = "tcp://127.0.0.1:1883";
    public static final String PAHO_CLIENT = "DingNetSimulator";
}
