package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class DingNetCache {

    public static Optional<String> getLastUsedSettingsProfile() {
        Properties cacheProperties = new Properties();

        if (new File(Constants.DINGNET_CACHE_FILE).exists()) {
            try {
                cacheProperties.load(new FileInputStream(Constants.DINGNET_CACHE_FILE));
                var lastUsed = cacheProperties.getProperty("settings.lastUsed");

                if (lastUsed != null && new File(Paths.get(Constants.PATH_CUSTOM_SETTINGS, lastUsed.trim()).toString()).exists()) {
                    return Optional.of(lastUsed.trim());
                }

            } catch (IOException ignored) {}
        }
        return Optional.empty();
    }

    public static void updateLastUsedSettingsProfile(String profile) {
        // Create the file if it doesn't exist yet
        File file = new File(Constants.DINGNET_CACHE_FILE);
        Properties cacheProperties = new Properties();

        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                cacheProperties.load(new FileInputStream(Constants.DINGNET_CACHE_FILE));
            }

            cacheProperties.put("settings.lastUsed", profile);
            cacheProperties.store(new FileOutputStream(Constants.DINGNET_CACHE_FILE), "");
        } catch (IOException ignored) {}
    }
}
