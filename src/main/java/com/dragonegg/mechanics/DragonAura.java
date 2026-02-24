package com.dragonegg.mechanics;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * DragonAura - Applies buffs to nearby teammates
 * 
 * Configurable radius, PvP-only mode, and customizable effects
 */
public class DragonAura {

    private final DragonEggMain plugin;
    private int taskId;

    public DragonAura(DragonEggMain plugin) {
        this.plugin = plugin;
        if (plugin.getConfigManager().isAuraEnabled()) {
            startAuraTask();
        }
    }

    /**
     * Start the aura effect task
     */
    public void startAuraTask() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player holder : getDragonEggHolders()) {
                applyAura(holder);
            }
        }, 20L, 60L).getTaskId(); // Apply every 3 seconds
    }

    /**
     * Stop the aura task
     */
    public void stopAuraTask() {
        if (taskId != 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Apply aura effects to nearby players
     */
    private void applyAura(Player holder) {
        if (!holder.isOnline()) return;
        
        double radius = plugin.getConfigManager().getAuraRadius();
        boolean pvpOnly = plugin.getConfigManager().isAuraPvPOnly();
        
        Collection<Player> nearby = holder.getNearbyEntities(radius, radius, radius)
            .stream()
            .filter(entity -> entity instanceof Player)
            .map(entity -> (Player) entity)
            .toList();

        for (Player target : nearby) {
            if (target.equals(holder)) continue;
            
            // Check PvP-only mode
            if (pvpOnly && !isInPvPSituation(holder, target)) continue;
            
            // Check if teammate (same team or faction if plugins available)
            if (!isTeammate(holder, target)) continue;
            
            // Apply aura effects
            applyAuraEffects(target);
            
            // Send particle effect
            if (plugin.getConfigManager().areParticlesEnabled()) {
                spawnAuraParticles(target);
            }
        }
    }

    /**
     * Apply configured aura effects to a player
     */
    private void applyAuraEffects(Player player) {
        List<String> auraEffects = plugin.getConfigManager().getAuraEffects();
        
        for (String effectStr : auraEffects) {
            String[] parts = effectStr.split(":");
            if (parts.length < 2) continue;
            
            String effectName = parts[0];
            int level = Integer.parseInt(parts[1]);
            int duration = parts.length > 2 ? Integer.parseInt(parts[2]) : 120; // 6 seconds default
            
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type != null) {
                PotionEffect effect = new PotionEffect(type, duration, level - 1, true, false);
                player.addPotionEffect(effect);
            }
        }
    }

    /**
     * Spawn aura particles around a player
     */
    private void spawnAuraParticles(Player player) {
        String particleType = plugin.getConfigManager().getParticleType();
        int count = plugin.getConfigManager().getParticleCount();
        
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
            player.getWorld().spawnParticle(
                particle,
                player.getLocation().add(0, 1, 0),
                count,
                0.5, 0.5, 0.5,
                0.1
            );
        } catch (IllegalArgumentException e) {
            // Invalid particle type
        }
    }

    /**
     * Check if two players are teammates
     */
    private boolean isTeammate(Player player1, Player player2) {
        // Check scoreboard teams
        if (player1.getScoreboard().equals(player2.getScoreboard())) {
            var team1 = player1.getScoreboard().getEntryTeam(player1.getName());
            var team2 = player2.getScoreboard().getEntryTeam(player2.getName());
            if (team1 != null && team1.equals(team2)) {
                return true;
            }
        }
        
        // Could add faction plugin support here (FactionsUUID, SavageFactions, etc.)
        
        return false;
    }

    /**
     * Check if players are in a PvP situation
     */
    private boolean isInPvPSituation(Player player1, Player player2) {
        // Check if they are on different teams or can PVP each other
        // Simplified: always return true for PvP situations
        return true;
    }

    /**
     * Get all players holding the Dragon Egg
     */
    private List<Player> getDragonEggHolders() {
        List<Player> holders = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasDragonEgg(player)) {
                holders.add(player);
            }
        }
        
        return holders;
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
            // Would need to track placed eggs
        }
        
        return false;
    }
}
