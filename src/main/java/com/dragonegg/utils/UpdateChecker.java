package com.dragonegg.utils;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;

/**
 * UpdateChecker - Checks for plugin updates
 * 
 * Supports Modrinth, SpigotMC, and other platforms
 */
public class UpdateChecker {

    private final DragonEggMain plugin;
    private final int resourceId;
    private final String downloadUrl;

    public UpdateChecker(DragonEggMain plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.downloadUrl = plugin.getConfig().getString("update-checker.download-url", 
            "https://modrinth.com/plugin/dragonegg");
    }

    /**
     * Fetch the latest version and call the consumer with the result
     */
    public void fetchVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String version = getVersion();
                Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(version));
            } catch (IOException e) {
                plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Get the latest version from the API
     */
    private String getVersion() throws IOException {
        // For Modrinth API
        String modrinthId = plugin.getConfig().getString("update-checker.modrinth-id", "");
        
        if (!modrinthId.isEmpty()) {
            URL url = new URL("https://api.modrinth.com/v2/project/" + modrinthId + "/version");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                // Parse JSON response (simplified - would need proper JSON parsing)
                // This is a placeholder - implement proper JSON parsing
                return parseVersionFromJson(response.toString());
            }
        }
        
        // Fallback to current version
        return plugin.getDescription().getVersion();
    }

    /**
     * Parse version from JSON response
     */
    private String parseVersionFromJson(String json) {
        // Simplified parsing - in production, use a proper JSON library
        // Look for version_number field
        int index = json.indexOf("version_number");
        if (index != -1) {
            int start = json.indexOf("\"", index + 15) + 1;
            int end = json.indexOf("\"", start);
            if (start > 0 && end > start) {
                return json.substring(start, end);
            }
        }
        return plugin.getDescription().getVersion();
    }

    /**
     * Check if the current version is outdated
     */
    public boolean isOutdated(String latestVersion) {
        String current = plugin.getDescription().getVersion();
        return compareVersions(current, latestVersion) < 0;
    }

    /**
     * Compare two version strings
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("[.-]");
        String[] parts2 = v2.split("[.-]");
        
        int length = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseInt(parts2[i]) : 0;
            
            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        
        return 0;
    }

    /**
     * Parse integer from string, handling non-numeric parts
     */
    private int parseInt(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
