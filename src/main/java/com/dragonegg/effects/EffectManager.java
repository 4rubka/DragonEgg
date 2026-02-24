package com.dragonegg.effects;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import com.dragonegg.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EffectManager - Manages permanent and temporary potion effects for Dragon Egg holders
 * 
 * Handles loading, saving, and applying effects to players
 */
public class EffectManager {

    private final DragonEggMain plugin;
    private File effectsFile;
    private FileConfiguration effectsConfig;
    
    // Cache of player effects: UUID -> Map<EffectType, Level>
    private final Map<UUID, Map<String, Integer>> playerEffects = new ConcurrentHashMap<>();
    
    // Currently active effects on players: UUID -> List<PotionEffect>
    private final Map<UUID, List<PotionEffect>> activeEffects = new ConcurrentHashMap<>();

    public EffectManager(DragonEggMain plugin) {
        this.plugin = plugin;
        initializeEffectsFile();
    }

    /**
     * Initialize the effects data file
     */
    private void initializeEffectsFile() {
        effectsFile = new File(plugin.getDataFolder(), "effects_data.yml");
        if (!effectsFile.exists()) {
            try {
                effectsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating effects_data.yml: " + e.getMessage());
            }
        }
        effectsConfig = YamlConfiguration.loadConfiguration(effectsFile);
    }

    /**
     * Load all player effects from file
     */
    public void loadAllEffects() {
        if (!effectsFile.exists()) return;
        
        try {
            effectsConfig.load(effectsFile);
            
            if (effectsConfig.contains("players")) {
                for (String uuid : effectsConfig.getConfigurationSection("players").getKeys(false)) {
                    UUID playerUuid = UUID.fromString(uuid);
                    Map<String, Integer> effects = new HashMap<>();
                    
                    for (String effect : effectsConfig.getConfigurationSection("players." + uuid).getKeys(false)) {
                        effects.put(effect, effectsConfig.getInt("players." + uuid + "." + effect));
                    }
                    
                    playerEffects.put(playerUuid, effects);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading effects data: " + e.getMessage());
        }
    }

    /**
     * Save all player effects to file
     */
    public void saveAllEffects() {
        // Clear existing data
        effectsConfig.set("players", null);
        
        // Save all player effects
        for (Map.Entry<UUID, Map<String, Integer>> entry : playerEffects.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Map.Entry<String, Integer> effect : entry.getValue().entrySet()) {
                effectsConfig.set("players." + uuid + "." + effect.getKey(), effect.getValue());
            }
        }
        
        try {
            effectsConfig.save(effectsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving effects data: " + e.getMessage());
        }
    }

    /**
     * Add an effect to a player
     */
    public void addEffect(Player player, PotionEffectType effectType, int level) {
        UUID uuid = player.getUniqueId();
        String effectName = effectType.getName();
        
        playerEffects.computeIfAbsent(uuid, k -> new HashMap<>()).put(effectName, level);
        
        applyEffect(player, effectType, level);
    }

    /**
     * Remove an effect from a player
     */
    public void removeEffect(Player player, PotionEffectType effectType) {
        UUID uuid = player.getUniqueId();
        String effectName = effectType.getName();
        
        if (playerEffects.containsKey(uuid)) {
            playerEffects.get(uuid).remove(effectName);
        }
        
        player.removePotionEffect(effectType);
    }

    /**
     * Set the level of an effect for a player
     */
    public void setEffectLevel(Player player, PotionEffectType effectType, int level) {
        if (level <= 0) {
            removeEffect(player, effectType);
            return;
        }
        addEffect(player, effectType, level);
    }

    /**
     * Clear all effects from a player
     */
    public void clearEffects(Player player) {
        UUID uuid = player.getUniqueId();
        playerEffects.remove(uuid);
        
        // Remove all potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Apply an effect to a player
     */
    private void applyEffect(Player player, PotionEffectType effectType, int level) {
        // Check if effect is enabled in config
        if (!plugin.getConfigManager().isEffectEnabled(effectType.getName())) {
            return;
        }
        
        // Get config level if not specified
        int configLevel = plugin.getConfigManager().getEffectLevel(effectType.getName());
        int finalLevel = Math.max(level, configLevel);
        
        PotionEffect effect = new PotionEffect(
            effectType,
            Integer.MAX_VALUE, // Permanent
            finalLevel - 1,    // Level - 1 (PotionEffect uses 0-based)
            true,              // Ambient
            true,              // Show particles
            true               // Show icon
        );
        
        player.addPotionEffect(effect);
    }

    /**
     * Apply all configured effects to a player
     */
    public void applyAllEffects(Player player) {
        UUID uuid = player.getUniqueId();
        
        // First, apply custom player-specific effects
        if (playerEffects.containsKey(uuid)) {
            for (Map.Entry<String, Integer> entry : playerEffects.get(uuid).entrySet()) {
                PotionEffectType type = PotionEffectType.getByName(entry.getKey());
                if (type != null) {
                    applyEffect(player, type, entry.getValue());
                }
            }
        }
        
        // Then apply default config effects
        Map<String, ConfigManager.EffectConfig> configEffects = plugin.getConfigManager().getEffects();
        for (Map.Entry<String, ConfigManager.EffectConfig> entry : configEffects.entrySet()) {
            ConfigManager.EffectConfig config = entry.getValue();
            if (config.isEnabled()) {
                PotionEffectType type = PotionEffectType.getByName(entry.getKey());
                if (type != null) {
                    applyEffect(player, type, config.getLevel());
                }
            }
        }
    }

    /**
     * Remove all effects from a player
     */
    public void removeAllEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Refresh effects on a player (for non-permanent effects)
     */
    public void refreshEffects(Player player) {
        Map<String, ConfigManager.EffectConfig> configEffects = plugin.getConfigManager().getEffects();
        
        for (Map.Entry<String, ConfigManager.EffectConfig> entry : configEffects.entrySet()) {
            ConfigManager.EffectConfig config = entry.getValue();
            if (config.isEnabled() && !config.isPermanent()) {
                PotionEffectType type = PotionEffectType.getByName(entry.getKey());
                if (type != null) {
                    PotionEffect effect = new PotionEffect(
                        type,
                        config.getDuration() * 20, // Convert to ticks
                        config.getLevel() - 1,
                        true,
                        true,
                        true
                    );
                    player.addPotionEffect(effect);
                }
            }
        }
    }

    /**
     * Get all effects for a player
     */
    public Map<String, Integer> getPlayerEffects(Player player) {
        return playerEffects.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    /**
     * Check if a player has a specific effect
     */
    public boolean hasEffect(Player player, PotionEffectType effectType) {
        UUID uuid = player.getUniqueId();
        if (playerEffects.containsKey(uuid)) {
            return playerEffects.get(uuid).containsKey(effectType.getName());
        }
        return player.hasPotionEffect(effectType);
    }

    /**
     * Get the level of an effect for a player
     */
    public int getEffectLevel(Player player, PotionEffectType effectType) {
        UUID uuid = player.getUniqueId();
        if (playerEffects.containsKey(uuid)) {
            return playerEffects.get(uuid).getOrDefault(effectType.getName(), 0);
        }
        PotionEffect effect = player.getPotionEffect(effectType);
        return effect != null ? effect.getAmplifier() + 1 : 0;
    }

    /**
     * Start the effect refresh task for non-permanent effects
     */
    public void startRefreshTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasDragonEgg(player)) {
                    refreshEffects(player);
                }
            }
        }, 20L, 20L); // Check every second
    }

    /**
     * Check if a player has the Dragon Egg
     */
    private boolean hasDragonEgg(Player player) {
        // Check inventory
        if (plugin.getConfigManager().isInventoryModeEnabled()) {
            for (var item : player.getInventory().getContents()) {
                if (item != null && isDragonEggItem(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if an item is a Dragon Egg item
     */
    private boolean isDragonEggItem(org.bukkit.inventory.ItemStack item) {
        if (item.getType() != org.bukkit.Material.DRAGON_EGG) {
            return false;
        }
        if (!plugin.getConfigManager().isCustomItemEnabled()) {
            return true;
        }
        // Check custom name if configured
        return true;
    }
}
