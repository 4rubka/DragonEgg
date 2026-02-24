package com.dragonegg.hearts;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HeartManager - Manages extra hearts (health) for Dragon Egg holders
 * 
 * Handles permission-based scaling, caps, and persistence
 */
public class HeartManager {

    private final DragonEggMain plugin;
    private File heartsFile;
    private FileConfiguration heartsConfig;
    
    // Cache of player extra hearts: UUID -> amount
    private final Map<UUID, Double> playerHearts = new ConcurrentHashMap<>();

    public HeartManager(DragonEggMain plugin) {
        this.plugin = plugin;
        initializeHeartsFile();
        loadAllHearts();
    }

    /**
     * Initialize the hearts data file
     */
    private void initializeHeartsFile() {
        heartsFile = new File(plugin.getDataFolder(), "hearts_data.yml");
        if (!heartsFile.exists()) {
            try {
                heartsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating hearts_data.yml: " + e.getMessage());
            }
        }
        heartsConfig = YamlConfiguration.loadConfiguration(heartsFile);
    }

    /**
     * Load all player hearts from file
     */
    public void loadAllHearts() {
        if (!heartsFile.exists()) return;
        
        try {
            heartsConfig.load(heartsFile);
            
            if (heartsConfig.contains("players")) {
                for (String uuid : heartsConfig.getConfigurationSection("players").getKeys(false)) {
                    try {
                        UUID playerUuid = UUID.fromString(uuid);
                        double hearts = heartsConfig.getDouble("players." + uuid);
                        playerHearts.put(playerUuid, hearts);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in hearts data: " + uuid);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading hearts data: " + e.getMessage());
        }
    }

    /**
     * Save all player hearts to file
     */
    public void saveAllHearts() {
        // Clear existing data
        heartsConfig.set("players", null);
        
        // Save all player hearts
        for (Map.Entry<UUID, Double> entry : playerHearts.entrySet()) {
            heartsConfig.set("players." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            heartsConfig.save(heartsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving hearts data: " + e.getMessage());
        }
    }

    /**
     * Set extra hearts for a player
     */
    public void setExtraHearts(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double cappedAmount = Math.min(amount, plugin.getConfigManager().getMaxHearts());
        
        playerHearts.put(uuid, cappedAmount);
        updatePlayerHealth(player);
    }

    /**
     * Add extra hearts to a player
     */
    public void addExtraHearts(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = playerHearts.getOrDefault(uuid, 0.0);
        double newAmount = Math.min(current + amount, plugin.getConfigManager().getMaxHearts());
        
        playerHearts.put(uuid, newAmount);
        updatePlayerHealth(player);
    }

    /**
     * Remove extra hearts from a player
     */
    public void removeExtraHearts(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = playerHearts.getOrDefault(uuid, 0.0);
        double newAmount = Math.max(current - amount, 0.0);
        
        playerHearts.put(uuid, newAmount);
        updatePlayerHealth(player);
    }

    /**
     * Clear extra hearts from a player
     */
    public void clearExtraHearts(Player player) {
        playerHearts.remove(player.getUniqueId());
        updatePlayerHealth(player);
    }

    /**
     * Update a player's maximum health based on extra hearts
     * Each heart = 2 HP in Minecraft
     */
    public void updatePlayerHealth(Player player) {
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) return;
        
        double baseHealth = 20.0; // Default Minecraft health
        UUID uuid = player.getUniqueId();
        
        // Get extra hearts from cache
        double extraHearts = playerHearts.getOrDefault(uuid, 0.0);
        
        // Add permission-based hearts
        extraHearts += getPermissionBonusHearts(player);
        
        // Calculate total health
        double totalHealth = baseHealth + (extraHearts * 2);
        
        // Apply cap
        double maxCap = plugin.getConfigManager().getMaxHearts() * 2 + baseHealth;
        totalHealth = Math.min(totalHealth, maxCap);
        
        // Set the new max health
        healthAttribute.setBaseValue(totalHealth);
        
        // Heal player to new max if they have full health
        if (player.getHealth() >= healthAttribute.getBaseValue() - (extraHearts * 2)) {
            player.setHealth(totalHealth);
        }
    }

    /**
     * Get permission-based heart bonus for a player
     */
    private double getPermissionBonusHearts(Player player) {
        Map<String, Integer> permissionScaling = plugin.getConfigManager().getHeartsPermissions();
        double bonus = 0.0;
        
        for (Map.Entry<String, Integer> entry : permissionScaling.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                bonus = Math.max(bonus, entry.getValue());
            }
        }
        
        return bonus;
    }

    /**
     * Get extra hearts for a player
     */
    public double getExtraHearts(Player player) {
        return playerHearts.getOrDefault(player.getUniqueId(), 0.0);
    }

    /**
     * Get total hearts for a player (base + extra)
     */
    public double getTotalHearts(Player player) {
        return 10.0 + getExtraHearts(player) + getPermissionBonusHearts(player);
    }

    /**
     * Apply hearts to all online players with Dragon Egg
     */
    public void applyAllHearts() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasDragonEgg(player)) {
                updatePlayerHealth(player);
            }
        }
    }

    /**
     * Reset player health on disable
     */
    public void resetAllHearts() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(20.0);
            }
        }
    }

    /**
     * Check if a player has the Dragon Egg
     */
    private boolean hasDragonEgg(Player player) {
        if (plugin.getConfigManager().isInventoryModeEnabled()) {
            for (var item : player.getInventory().getContents()) {
                if (item != null && item.getType() == org.bukkit.Material.DRAGON_EGG) {
                    return true;
                }
            }
        }
        
        // Check if player is in range of placed egg
        if (plugin.getConfigManager().isPlacedModeEnabled()) {
            // This would require tracking placed eggs - simplified for now
        }
        
        return false;
    }

    /**
     * Start health update task
     */
    public void startHealthTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasDragonEgg(player)) {
                    updatePlayerHealth(player);
                }
            }
        }, 20L, 100L); // Check every 5 seconds
    }
}
