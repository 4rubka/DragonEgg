package com.dragonegg.config;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ConfigManager - Handles all plugin configuration
 * 
 * Manages loading, saving, and accessing config.yml settings
 */
public class ConfigManager {

    private final DragonEggMain plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Load the main configuration file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Reload actual config to get latest values
        try {
            config.load(configFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading config.yml: " + e.getMessage());
        }
    }

    /**
     * Save the configuration file
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving config.yml: " + e.getMessage());
        }
    }

    /**
     * Reload the configuration from disk
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * Get the FileConfiguration object
     */
    public FileConfiguration getConfig() {
        return config;
    }

    // ==================== EFFECTS CONFIG ====================

    /**
     * Get all configured effects
     */
    public Map<String, EffectConfig> getEffects() {
        Map<String, EffectConfig> effects = new HashMap<>();
        if (!config.contains("effects")) return effects;

        for (String effect : config.getConfigurationSection("effects").getKeys(false)) {
            boolean enabled = config.getBoolean("effects." + effect + ".enabled", true);
            int level = config.getInt("effects." + effect + ".level", 1);
            int duration = config.getInt("effects." + effect + ".duration", 0);
            boolean permanent = config.getBoolean("effects." + effect + ".permanent", true);

            effects.put(effect, new EffectConfig(enabled, level, duration, permanent));
        }
        return effects;
    }

    /**
     * Check if a specific effect is enabled
     */
    public boolean isEffectEnabled(String effect) {
        return config.getBoolean("effects." + effect + ".enabled", true);
    }

    /**
     * Get effect level
     */
    public int getEffectLevel(String effect) {
        return config.getInt("effects." + effect + ".level", 1);
    }

    // ==================== HEARTS CONFIG ====================

    /**
     * Get extra hearts amount
     */
    public int getExtraHearts() {
        return config.getInt("hearts.extra", 4);
    }

    /**
     * Get maximum hearts cap
     */
    public int getMaxHearts() {
        return config.getInt("hearts.max-cap", 40);
    }

    /**
     * Get hearts per permission level
     */
    public Map<String, Integer> getHeartsPermissions() {
        Map<String, Integer> hearts = new HashMap<>();
        if (!config.contains("hearts.permission-scaling")) return hearts;

        for (String perm : config.getConfigurationSection("hearts.permission-scaling").getKeys(false)) {
            hearts.put(perm, config.getInt("hearts.permission-scaling." + perm));
        }
        return hearts;
    }

    // ==================== AURA CONFIG ====================

    /**
     * Get dragon aura radius
     */
    public double getAuraRadius() {
        return config.getDouble("mechanics.aura.radius", 10.0);
    }

    /**
     * Check if aura is enabled
     */
    public boolean isAuraEnabled() {
        return config.getBoolean("mechanics.aura.enabled", true);
    }

    /**
     * Check if aura is PvP only
     */
    public boolean isAuraPvPOnly() {
        return config.getBoolean("mechanics.aura.pvp-only", false);
    }

    /**
     * Get aura effects
     */
    public List<String> getAuraEffects() {
        return config.getStringList("mechanics.aura.effects");
    }

    // ==================== FURY CONFIG ====================

    /**
     * Check if fury mode is enabled
     */
    public boolean isFuryEnabled() {
        return config.getBoolean("mechanics.fury.enabled", true);
    }

    /**
     * Get fury activation HP threshold
     */
    public double getFuryThreshold() {
        return config.getDouble("mechanics.fury.hp-threshold", 20.0);
    }

    /**
     * Get fury duration in seconds
     */
    public int getFuryDuration() {
        return config.getInt("mechanics.fury.duration", 10);
    }

    /**
     * Get fury cooldown in seconds
     */
    public int getFuryCooldown() {
        return config.getInt("mechanics.fury.cooldown", 60);
    }

    // ==================== EVOLUTION CONFIG ====================

    /**
     * Check if evolution is enabled
     */
    public boolean isEvolutionEnabled() {
        return config.getBoolean("mechanics.evolution.enabled", true);
    }

    /**
     * Get evolution stages
     */
    public Map<Integer, EvolutionStage> getEvolutionStages() {
        Map<Integer, EvolutionStage> stages = new HashMap<>();
        if (!config.contains("mechanics.evolution.stages")) return stages;

        for (String key : config.getConfigurationSection("mechanics.evolution.stages").getKeys(false)) {
            int level = Integer.parseInt(key);
            int requiredKills = config.getInt("mechanics.evolution.stages." + key + ".required-kills", 0);
            int requiredPlaytime = config.getInt("mechanics.evolution.stages." + key + ".required-playtime", 0);
            List<String> unlockedEffects = config.getStringList("mechanics.evolution.stages." + key + ".unlocked-effects");
            
            stages.put(level, new EvolutionStage(level, requiredKills, requiredPlaytime, unlockedEffects));
        }
        return stages;
    }

    // ==================== BOND CONFIG ====================

    /**
     * Check if bonding is enabled
     */
    public boolean isBondEnabled() {
        return config.getBoolean("mechanics.bond.enabled", true);
    }

    /**
     * Check if egg can be stolen
     */
    public boolean canEggBeStolen() {
        return config.getBoolean("mechanics.bond.allow-stealing", false);
    }

    // ==================== SHIELD CONFIG ====================

    /**
     * Check if shield is enabled
     */
    public boolean isShieldEnabled() {
        return config.getBoolean("mechanics.shield.enabled", true);
    }

    /**
     * Get shield damage negation chance (0-100)
     */
    public double getShieldChance() {
        return config.getDouble("mechanics.shield.negation-chance", 10.0);
    }

    /**
     * Get shield cooldown in seconds
     */
    public int getShieldCooldown() {
        return config.getInt("mechanics.shield.cooldown", 5);
    }

    // ==================== REVIVE CONFIG ====================

    /**
     * Check if revive is enabled
     */
    public boolean isReviveEnabled() {
        return config.getBoolean("mechanics.revive.enabled", true);
    }

    /**
     * Get revive cooldown in hours
     */
    public int getReviveCooldownHours() {
        return config.getInt("mechanics.revive.cooldown-hours", 24);
    }

    /**
     * Get revive heal percentage
     */
    public double getReviveHealPercent() {
        return config.getDouble("mechanics.revive.heal-percent", 50.0);
    }

    // ==================== DRAGON EGG SETTINGS ====================

    /**
     * Check if Dragon Egg can be placed
     */
    public boolean isDragonEggPlaceable() {
        return config.getBoolean("dragon-egg.placeable", false);
    }

    // ==================== VISUALS CONFIG ====================

    /**
     * Check if particles are enabled
     */
    public boolean areParticlesEnabled() {
        return config.getBoolean("visuals.particles.enabled", true);
    }

    /**
     * Get particle type
     */
    public String getParticleType() {
        return config.getString("visuals.particles.type", "DRAGON_BREATH");
    }

    /**
     * Get particle count
     */
    public int getParticleCount() {
        return config.getInt("visuals.particles.count", 10);
    }

    /**
     * Check if glowing effect is enabled
     */
    public boolean isGlowingEnabled() {
        return config.getBoolean("visuals.glowing.enabled", true);
    }

    /**
     * Get glowing color
     */
    public String getGlowingColor() {
        return config.getString("visuals.glowing.color", "PURPLE");
    }

    // ==================== GENERAL CONFIG ====================

    /**
     * Check if egg works from inventory
     */
    public boolean isInventoryModeEnabled() {
        return config.getBoolean("general.inventory-mode", true);
    }

    /**
     * Check if egg works when placed
     */
    public boolean isPlacedModeEnabled() {
        return config.getBoolean("general.placed-mode", true);
    }

    /**
     * Get placed mode radius
     */
    public int getPlacedRadius() {
        return config.getInt("general.placed-radius", 50);
    }

    /**
     * Check if custom item is enabled
     */
    public boolean isCustomItemEnabled() {
        return config.getBoolean("general.custom-item.enabled", true);
    }

    /**
     * Get custom item name
     */
    public String getCustomItemName() {
        return config.getString("general.custom-item.name", "&5&lDragon Egg");
    }

    /**
     * Get custom item lore
     */
    public List<String> getCustomItemLore() {
        return config.getStringList("general.custom-item.lore");
    }

    // Inner classes for configuration objects
    public static class EffectConfig {
        private final boolean enabled;
        private final int level;
        private final int duration;
        private final boolean permanent;

        public EffectConfig(boolean enabled, int level, int duration, boolean permanent) {
            this.enabled = enabled;
            this.level = level;
            this.duration = duration;
            this.permanent = permanent;
        }

        public boolean isEnabled() { return enabled; }
        public int getLevel() { return level; }
        public int getDuration() { return duration; }
        public boolean isPermanent() { return permanent; }
    }

    public static class EvolutionStage {
        private final int level;
        private final int requiredKills;
        private final int requiredPlaytime;
        private final List<String> unlockedEffects;

        public EvolutionStage(int level, int requiredKills, int requiredPlaytime, List<String> unlockedEffects) {
            this.level = level;
            this.requiredKills = requiredKills;
            this.requiredPlaytime = requiredPlaytime;
            this.unlockedEffects = unlockedEffects;
        }

        public int getLevel() { return level; }
        public int getRequiredKills() { return requiredKills; }
        public int getRequiredPlaytime() { return requiredPlaytime; }
        public List<String> getUnlockedEffects() { return unlockedEffects; }
    }
}
