package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class DingNetCache {

    private static Optional<Properties> getCachedProperties() {
        Properties cacheProperties = new Properties();

        if (new File(Constants.DINGNET_CACHE_FILE).exists()) {
            try {
                cacheProperties.load(new FileInputStream(Constants.DINGNET_CACHE_FILE));
                return Optional.of(cacheProperties);
            } catch (IOException ignored) {}
        }

        return Optional.empty();
    }

    private static void createCacheFileIfNeeded() {
        // Create a cache file if it does not exist yet
        File file = new File(Constants.DINGNET_CACHE_FILE);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    private static void appendAndStoreProperty(String property, String value) {
        getCachedProperties().ifPresent(prop -> {
            prop.put(property, value);
            storeCacheProperties(prop);
        });
    }

    private static void storeCacheProperties(Properties properties) {
        try {
            properties.store(new FileOutputStream(Constants.DINGNET_CACHE_FILE), "");
        } catch (IOException ignored) {}
    }

    public static Optional<String> getLastUsedSettingsProfile() {
        Optional<Properties> cachedProperties =  getCachedProperties();

        if (cachedProperties.isPresent()) {
            var lastUsed = cachedProperties.get().getProperty("settings.lastUsed");

            if (lastUsed != null && new File(Paths.get(Constants.PATH_CUSTOM_SETTINGS, lastUsed.trim()).toString()).exists()) {
                return Optional.of(lastUsed.trim());
            }
        }
        return Optional.empty();
    }

    public static void updateLastUsedSettingsProfile(String profile) {
        createCacheFileIfNeeded();
        appendAndStoreProperty("settings.lastUsed", profile);
    }

    public static Optional<String> getLastUsedInputProfile() {
        Optional<Properties> cachedProperties =  getCachedProperties();

        if (cachedProperties.isPresent()) {
            var lastUsed = cachedProperties.get().getProperty("inputProfile.lastUsed");
            if (lastUsed != null) {
                return Optional.of(lastUsed);
            }
        }

        return Optional.empty();
    }

    public static void updateLastUsedInputProfile(String profile) {
        createCacheFileIfNeeded();
        appendAndStoreProperty("inputProfile.lastUsed", profile);
    }
}
