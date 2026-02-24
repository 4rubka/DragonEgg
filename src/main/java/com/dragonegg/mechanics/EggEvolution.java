package com.dragonegg.mechanics;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import com.dragonegg.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EggEvolution - Egg levels up based on kills and playtime
 * 
 * Unlocks stronger effects at higher levels
 */
public class EggEvolution {

    private final DragonEggMain plugin;
    private File evolutionFile;
    private FileConfiguration evolutionConfig;
    
    // Player evolution data: UUID -> EvolutionData
    private final Map<UUID, EvolutionData> evolutionData = new ConcurrentHashMap<>();

    public EggEvolution(DragonEggMain plugin) {
        this.plugin = plugin;
        initializeEvolutionFile();
        loadAllProgress();
        
        if (plugin.getConfigManager().isEvolutionEnabled()) {
            startPlaytimeTask();
        }
    }

    /**
     * Initialize the evolution data file
     */
    private void initializeEvolutionFile() {
        evolutionFile = new File(plugin.getDataFolder(), "evolution_data.yml");
        if (!evolutionFile.exists()) {
            try {
                evolutionFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating evolution_data.yml: " + e.getMessage());
            }
        }
        evolutionConfig = YamlConfiguration.loadConfiguration(evolutionFile);
    }

    /**
     * Load all player evolution progress from file
     */
    public void loadAllProgress() {
        if (!evolutionFile.exists()) return;
        
        try {
            evolutionConfig.load(evolutionFile);
            
            if (evolutionConfig.contains("players")) {
                for (String uuid : evolutionConfig.getConfigurationSection("players").getKeys(false)) {
                    try {
                        UUID playerUuid = UUID.fromString(uuid);
                        int kills = evolutionConfig.getInt("players." + uuid + ".kills", 0);
                        int playtime = evolutionConfig.getInt("players." + uuid + ".playtime", 0);
                        int level = evolutionConfig.getInt("players." + uuid + ".level", 1);
                        
                        evolutionData.put(playerUuid, new EvolutionData(kills, playtime, level));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in evolution data: " + uuid);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading evolution data: " + e.getMessage());
        }
    }

    /**
     * Save all player evolution progress to file
     */
    public void saveAllProgress() {
        // Clear existing data
        evolutionConfig.set("players", null);
        
        // Save all player data
        for (Map.Entry<UUID, EvolutionData> entry : evolutionData.entrySet()) {
            String uuid = entry.getKey().toString();
            EvolutionData data = entry.getValue();
            
            evolutionConfig.set("players." + uuid + ".kills", data.kills);
            evolutionConfig.set("players." + uuid + ".playtime", data.playtime);
            evolutionConfig.set("players." + uuid + ".level", data.level);
        }
        
        try {
            evolutionConfig.save(evolutionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving evolution data: " + e.getMessage());
        }
    }

    /**
     * Start playtime tracking task
     */
    private void startPlaytimeTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasDragonEgg(player)) {
                    addPlaytime(player, 1);
                }
            }
        }, 1200L, 1200L); // Every minute
    }

    /**
     * Add a kill to a player's evolution progress
     */
    public void addKill(Player player) {
        if (!plugin.getConfigManager().isEvolutionEnabled()) return;
        
        UUID uuid = player.getUniqueId();
        EvolutionData data = evolutionData.computeIfAbsent(uuid, k -> new EvolutionData(0, 0, 1));
        
        data.kills++;
        
        // Check for level up
        checkLevelUp(player, data);
    }

    /**
     * Add playtime to a player's evolution progress
     */
    public void addPlaytime(Player player, int seconds) {
        if (!plugin.getConfigManager().isEvolutionEnabled()) return;
        
        UUID uuid = player.getUniqueId();
        EvolutionData data = evolutionData.computeIfAbsent(uuid, k -> new EvolutionData(0, 0, 1));
        
        data.playtime += seconds;
        
        // Check for level up
        checkLevelUp(player, data);
    }

    /**
     * Check if a player should level up
     */
    private void checkLevelUp(Player player, EvolutionData data) {
        Map<Integer, ConfigManager.EvolutionStage> stages = plugin.getConfigManager().getEvolutionStages();
        
        for (Map.Entry<Integer, ConfigManager.EvolutionStage> entry : stages.entrySet()) {
            int level = entry.getKey();
            ConfigManager.EvolutionStage stage = entry.getValue();
            
            if (level > data.level) {
                // Check if requirements are met
                boolean killsMet = stage.getRequiredKills() <= 0 || data.kills >= stage.getRequiredKills();
                boolean playtimeMet = stage.getRequiredPlaytime() <= 0 || data.playtime >= stage.getRequiredPlaytime();
                
                if (killsMet && playtimeMet) {
                    // Level up!
                    data.level = level;
                    onLevelUp(player, level, stage);
                }
            }
        }
    }

    /**
     * Handle level up event
     */
    private void onLevelUp(Player player, int newLevel, ConfigManager.EvolutionStage stage) {
        // Send notification
        player.sendMessage(plugin.getMessageManager().getMessage("evolution.leveled-up")
            .replace("%level%", String.valueOf(newLevel)));
        
        // Unlock new effects
        for (String effect : stage.getUnlockedEffects()) {
            player.sendMessage(plugin.getMessageManager().getMessage("evolution.new-ability")
                .replace("%ability%", effect));
        }
        
        // Play sound
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        
        // Spawn particles
        player.getWorld().spawnParticle(
            org.bukkit.Particle.DRAGON_BREATH,
            player.getLocation().add(0, 1, 0),
            100,
            1, 1, 1,
            0.5
        );
    }

    /**
     * Get the evolution level for a player
     */
    public int getLevel(Player player) {
        EvolutionData data = evolutionData.get(player.getUniqueId());
        return data != null ? data.level : 1;
    }

    /**
     * Get evolution data for a player
     */
    public EvolutionData getEvolutionData(Player player) {
        return evolutionData.get(player.getUniqueId());
    }

    /**
     * Get progress percentage to next level
     */
    public double getProgressPercent(Player player) {
        EvolutionData data = evolutionData.get(player.getUniqueId());
        if (data == null) return 0.0;
        
        Map<Integer, ConfigManager.EvolutionStage> stages = plugin.getConfigManager().getEvolutionStages();
        ConfigManager.EvolutionStage nextStage = stages.get(data.level + 1);
        
        if (nextStage == null) return 100.0; // Max level
        
        // Calculate based on kills (simplified)
        if (nextStage.getRequiredKills() > 0) {
            return Math.min(100.0, (data.kills * 100.0) / nextStage.getRequiredKills());
        }
        
        return 0.0;
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
     * Evolution data class
     */
    public static class EvolutionData {
        public int kills;
        public int playtime; // In minutes
        public int level;

        public EvolutionData(int kills, int playtime, int level) {
            this.kills = kills;
            this.playtime = playtime;
            this.level = level;
        }
    }
}
