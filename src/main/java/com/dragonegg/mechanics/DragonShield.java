package com.dragonegg.mechanics;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DragonShield - Chance to negate incoming damage
 * 
 * Configurable percentage and cooldown
 */
public class DragonShield {

    private final DragonEggMain plugin;
    
    // Cooldown tracking: UUID -> cooldown end time
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    
    // Last proc time for internal cooldown: UUID -> last proc time
    private final Map<UUID, Long> lastProc = new ConcurrentHashMap<>();

    public DragonShield(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle damage event and potentially negate it
     * 
     * @return true if damage was negated, false otherwise
     */
    public boolean handleDamage(EntityDamageEvent event, Player player) {
        if (!plugin.getConfigManager().isShieldEnabled()) return false;
        if (!hasDragonEgg(player)) return false;
        
        // Check cooldown
        if (isOnCooldown(player)) return false;
        
        // Check chance
        double chance = plugin.getConfigManager().getShieldChance();
        double roll = Math.random() * 100;
        
        if (roll <= chance) {
            // Negate damage
            event.setCancelled(true);
            
            // Set cooldown
            setCooldown(player);
            
            // Send notification
            player.sendMessage(plugin.getMessageManager().getMessage("shield.activated"));
            
            // Play sound
            playShieldSound(player);
            
            // Spawn particles
            spawnShieldParticles(player);
            
            return true;
        }
        
        return false;
    }

    /**
     * Set shield cooldown for a player
     */
    private void setCooldown(Player player) {
        int cooldown = plugin.getConfigManager().getShieldCooldown();
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldown * 1000L));
    }

    /**
     * Check if shield is on cooldown for a player
     */
    public boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!cooldowns.containsKey(uuid)) return false;
        
        return System.currentTimeMillis() < cooldowns.get(uuid);
    }

    /**
     * Get remaining cooldown time in seconds
     */
    public int getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!cooldowns.containsKey(uuid)) return 0;
        
        long remaining = cooldowns.get(uuid) - System.currentTimeMillis();
        return (int) Math.max(0, remaining / 1000);
    }

    /**
     * Play shield activation sound
     */
    private void playShieldSound(Player player) {
        String soundStr = plugin.getConfig().getString("sounds.shield", "BLOCK_ANVIL_PLACE:1.0:2.0");
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
     * Spawn shield particles
     */
    private void spawnShieldParticles(Player player) {
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.END_ROD;
            player.getWorld().spawnParticle(
                particle,
                player.getLocation().add(0, 1, 0),
                30,
                0.5, 0.5, 0.5,
                0.1
            );
        } catch (IllegalArgumentException e) {
            // Invalid particle
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
     * Get shield chance for a player (can be modified by permissions/upgrades)
     */
    public double getShieldChance(Player player) {
        double baseChance = plugin.getConfigManager().getShieldChance();
        
        // Check for bypass permission
        if (player.hasPermission("dragonegg.bypass.shield")) {
            return 100.0;
        }
        
        // Check for bonus permissions
        if (player.hasPermission("dragonegg.shield.bonus")) {
            baseChance += 10.0;
        }
        
        return Math.min(100.0, baseChance);
    }
}
