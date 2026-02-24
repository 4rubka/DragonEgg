package com.dragonegg.mechanics;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ReviveMechanic - Once per X hours, prevents death and restores health
 * 
 * Configurable cooldown and heal percentage
 */
public class ReviveMechanic {

    private final DragonEggMain plugin;
    private File reviveFile;
    private FileConfiguration reviveConfig;
    
    // Last revive time: UUID -> timestamp
    private final Map<UUID, Long> lastRevive = new ConcurrentHashMap<>();

    public ReviveMechanic(DragonEggMain plugin) {
        this.plugin = plugin;
        initializeReviveFile();
        loadReviveData();
    }

    /**
     * Initialize the revive data file
     */
    private void initializeReviveFile() {
        reviveFile = new File(plugin.getDataFolder(), "revive_data.yml");
        if (!reviveFile.exists()) {
            try {
                reviveFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating revive_data.yml: " + e.getMessage());
            }
        }
        reviveConfig = YamlConfiguration.loadConfiguration(reviveFile);
    }

    /**
     * Load revive data from file
     */
    private void loadReviveData() {
        if (!reviveFile.exists()) return;
        
        try {
            reviveConfig.load(reviveFile);
            
            if (reviveConfig.contains("players")) {
                for (String uuid : reviveConfig.getConfigurationSection("players").getKeys(false)) {
                    try {
                        UUID playerUuid = UUID.fromString(uuid);
                        long timestamp = reviveConfig.getLong("players." + uuid);
                        lastRevive.put(playerUuid, timestamp);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in revive data: " + uuid);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading revive data: " + e.getMessage());
        }
    }

    /**
     * Save revive data to file
     */
    public void saveReviveData() {
        // Clear existing data
        reviveConfig.set("players", null);
        
        // Save all revive times
        for (Map.Entry<UUID, Long> entry : lastRevive.entrySet()) {
            reviveConfig.set("players." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            reviveConfig.save(reviveFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving revive data: " + e.getMessage());
        }
    }

    /**
     * Handle player death and potentially revive them
     * 
     * @return true if player was revived, false otherwise
     */
    public boolean handleDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().isReviveEnabled()) return false;
        
        Player player = event.getEntity();
        
        if (!hasDragonEgg(player)) return false;
        
        // Check if revive is available
        if (!isReviveAvailable(player)) {
            sendCooldownMessage(player);
            return false;
        }
        
        // Perform revive
        performRevive(player);
        
        return true;
    }

    /**
     * Check if revive is available for a player
     */
    public boolean isReviveAvailable(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!lastRevive.containsKey(uuid)) return true;
        
        long cooldownHours = plugin.getConfigManager().getReviveCooldownHours();
        long cooldownMillis = cooldownHours * 60 * 60 * 1000;
        
        return System.currentTimeMillis() - lastRevive.get(uuid) >= cooldownMillis;
    }

    /**
     * Perform the revive
     */
    private void performRevive(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Set last revive time
        lastRevive.put(uuid, System.currentTimeMillis());
        
        // Save data
        saveReviveData();
        
        // Cancel death event (if possible - may need to be handled in listener)
        
        // Restore health
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double healPercent = plugin.getConfigManager().getReviveHealPercent();
        player.setHealth(maxHealth * (healPercent / 100.0));
        
        // Remove negative effects
        for (var effect : player.getActivePotionEffects()) {
            if (effect.getType().getName().contains("POISON") || 
                effect.getType().getName().contains("WITHER") ||
                effect.getType().getName().contains("BLINDNESS") ||
                effect.getType().getName().contains("CONFUSION")) {
                player.removePotionEffect(effect.getType());
            }
        }
        
        // Send notification
        player.sendMessage(plugin.getMessageManager().getMessage("revive.triggered"));
        player.sendMessage(plugin.getMessageManager().getMessage("revive.message"));
        
        // Play sound
        playReviveSound(player);
        
        // Spawn particles
        spawnReviveParticles(player);
        
        // Send title
        player.sendTitle(
            plugin.getMessageManager().getMessage("revive.triggered").replace("&", "§"),
            "",
            10, 60, 20
        );
    }

    /**
     * Get remaining cooldown time
     */
    public String getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!lastRevive.containsKey(uuid)) return "0h 0m";
        
        long cooldownHours = plugin.getConfigManager().getReviveCooldownHours();
        long cooldownMillis = cooldownHours * 60 * 60 * 1000;
        long elapsed = System.currentTimeMillis() - lastRevive.get(uuid);
        long remaining = Math.max(0, cooldownMillis - elapsed);
        
        long hours = remaining / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
        
        return hours + "h " + minutes + "m";
    }

    /**
     * Send cooldown message to player
     */
    private void sendCooldownMessage(Player player) {
        String cooldown = getRemainingCooldown(player);
        player.sendMessage(plugin.getMessageManager().getMessage("revive.cooldown")
            .replace("%hours%", String.valueOf(cooldown.split(" ")[0].replace("h", "")))
            .replace("%minutes%", String.valueOf(cooldown.split(" ")[1].replace("m", ""))));
    }

    /**
     * Play revive sound
     */
    private void playReviveSound(Player player) {
        String soundStr = plugin.getConfig().getString("sounds.revive", "ENTITY_PLAYER_LEVELUP:1.0:1.0");
        String[] parts = soundStr.split(":");
        
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(parts[0]);
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Invalid sound
        }
    }

    /**
     * Spawn revive particles
     */
    private void spawnReviveParticles(Player player) {
        try {
            // Use VILLAGER_HAPPY as totem-like particle
            org.bukkit.Particle particle = org.bukkit.Particle.VILLAGER_HAPPY;
            player.getWorld().spawnParticle(
                particle,
                player.getLocation().add(0, 1, 0),
                50,
                0.5, 0.5, 0.5,
                0.5
            );
        } catch (IllegalArgumentException e) {
            // Try alternative particle
            try {
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.HEART,
                    player.getLocation().add(0, 1, 0),
                    50,
                    0.5, 0.5, 0.5,
                    0.5
                );
            } catch (IllegalArgumentException ex) {
                // Ignore
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
        return false;
    }

    /**
     * Reset revive cooldown for a player (admin command)
     */
    public void resetCooldown(Player player) {
        lastRevive.remove(player.getUniqueId());
        saveReviveData();
    }
}
