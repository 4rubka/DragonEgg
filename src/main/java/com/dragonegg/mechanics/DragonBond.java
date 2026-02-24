package com.dragonegg.mechanics;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DragonBond - Egg binds to first player who uses it
 * 
 * Cannot be stolen unless configured
 */
public class DragonBond {

    private final DragonEggMain plugin;
    private File bondsFile;
    private FileConfiguration bondsConfig;
    
    // Bond data: UUID -> BondInfo
    private final Map<UUID, BondInfo> bonds = new ConcurrentHashMap<>();
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DragonBond(DragonEggMain plugin) {
        this.plugin = plugin;
        initializeBondsFile();
        loadAllBonds();
    }

    /**
     * Initialize the bonds data file
     */
    private void initializeBondsFile() {
        bondsFile = new File(plugin.getDataFolder(), "bonds_data.yml");
        if (!bondsFile.exists()) {
            try {
                bondsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating bonds_data.yml: " + e.getMessage());
            }
        }
        bondsConfig = YamlConfiguration.loadConfiguration(bondsFile);
    }

    /**
     * Load all bond data from file
     */
    public void loadAllBonds() {
        if (!bondsFile.exists()) return;
        
        try {
            bondsConfig.load(bondsFile);
            
            if (bondsConfig.contains("bonds")) {
                for (String uuid : bondsConfig.getConfigurationSection("bonds").getKeys(false)) {
                    try {
                        UUID playerUuid = UUID.fromString(uuid);
                        long bondTime = bondsConfig.getLong("bonds." + uuid + ".time", System.currentTimeMillis());
                        String playerName = bondsConfig.getString("bonds." + uuid + ".name", "Unknown");
                        
                        bonds.put(playerUuid, new BondInfo(playerUuid, playerName, bondTime));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in bonds data: " + uuid);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading bonds data: " + e.getMessage());
        }
    }

    /**
     * Save all bond data to file
     */
    public void saveAllBonds() {
        // Clear existing data
        bondsConfig.set("bonds", null);
        
        // Save all bonds
        for (Map.Entry<UUID, BondInfo> entry : bonds.entrySet()) {
            String uuid = entry.getKey().toString();
            BondInfo info = entry.getValue();
            
            bondsConfig.set("bonds." + uuid + ".name", info.playerName);
            bondsConfig.set("bonds." + uuid + ".time", info.bondTime);
        }
        
        try {
            bondsConfig.save(bondsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving bonds data: " + e.getMessage());
        }
    }

    /**
     * Bind the Dragon Egg to a player
     */
    public void bindEgg(Player player) {
        if (!plugin.getConfigManager().isBondEnabled()) return;
        
        UUID uuid = player.getUniqueId();
        
        if (bonds.containsKey(uuid)) {
            return; // Already bonded
        }
        
        BondInfo info = new BondInfo(uuid, player.getName(), System.currentTimeMillis());
        bonds.put(uuid, info);
        
        // Send notification
        player.sendMessage(plugin.getMessageManager().getMessage("egg.bonded"));
        
        // Apply visual effects
        if (plugin.getConfigManager().isGlowingEnabled()) {
            applyGlowingEffect(player);
        }
        
        // Save bonds
        saveAllBonds();
    }

    /**
     * Check if the Dragon Egg is bonded to a player
     */
    public boolean isBonded(Player player) {
        return bonds.containsKey(player.getUniqueId());
    }

    /**
     * Check if a player can use the Dragon Egg
     */
    public boolean canUseEgg(Player player) {
        if (!plugin.getConfigManager().isBondEnabled()) return true;
        
        return bonds.containsKey(player.getUniqueId());
    }

    /**
     * Check if a player can steal the Dragon Egg
     */
    public boolean canStealEgg(Player player) {
        if (!plugin.getConfigManager().isBondEnabled()) return true;
        
        return plugin.getConfigManager().canEggBeStolen();
    }

    /**
     * Get bond info for a player
     */
    public BondInfo getBondInfo(Player player) {
        return bonds.get(player.getUniqueId());
    }

    /**
     * Remove bond from a player
     */
    public void removeBond(Player player) {
        bonds.remove(player.getUniqueId());
        saveAllBonds();
    }

    /**
     * Apply glowing effect to bonded player
     */
    private void applyGlowingEffect(Player player) {
        if (!plugin.getConfigManager().isGlowingEnabled()) return;
        
        String colorName = plugin.getConfigManager().getGlowingColor();
        
        try {
            org.bukkit.ChatColor color = org.bukkit.ChatColor.valueOf(colorName);
            var team = player.getScoreboard().getTeam("dragonegg_glow");
            
            if (team == null) {
                team = player.getScoreboard().registerNewTeam("dragonegg_glow");
            }
            
            team.setColor(color);
            team.addEntry(player.getName());
            
            // Enable glowing through scoreboard
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        } catch (IllegalArgumentException e) {
            // Invalid color
        }
    }

    /**
     * Remove glowing effect from player
     */
    public void removeGlowingEffect(Player player) {
        var team = player.getScoreboard().getTeam("dragonegg_glow");
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    /**
     * Handle inventory click to check for egg stealing
     */
    public boolean handleEggTransfer(InventoryClickEvent event, Player clickedPlayer, Player otherPlayer) {
        if (!plugin.getConfigManager().isBondEnabled()) return true;
        
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != org.bukkit.Material.DRAGON_EGG) return true;
        
        // Check if egg is bonded
        if (bonds.containsKey(otherPlayer.getUniqueId())) {
            // Egg is bonded to another player
            if (!plugin.getConfigManager().canEggBeStolen()) {
                clickedPlayer.sendMessage(plugin.getMessageManager().getMessage("egg.cannot-steal"));
                event.setCancelled(true);
                return false;
            }
        }
        
        return true;
    }

    /**
     * Get all bonded players
     */
    public Collection<BondInfo> getAllBonds() {
        return bonds.values();
    }

    /**
     * Bond info class
     */
    public class BondInfo {
        public final UUID playerUuid;
        public final String playerName;
        public final long bondTime;
        private final SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public BondInfo(UUID playerUuid, String playerName, long bondTime) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.bondTime = bondTime;
        }

        public String getFormattedBondTime() {
            return localDateFormat.format(new Date(bondTime));
        }

        public long getDaysSinceBond() {
            return (System.currentTimeMillis() - bondTime) / (1000 * 60 * 60 * 24);
        }
    }
}
