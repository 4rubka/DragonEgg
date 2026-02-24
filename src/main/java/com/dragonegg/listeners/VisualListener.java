package com.dragonegg.listeners;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * VisualListener - Handles visual effects (particles, glowing) for Dragon Egg holders
 */
public class VisualListener implements Listener {

    private final DragonEggMain plugin;
    private final Set<UUID> playersWithParticles = new HashSet<>();
    private int particleTaskId = -1;

    public VisualListener(DragonEggMain plugin) {
        this.plugin = plugin;
        startParticleTask();
    }

    /**
     * Start the particle effect task
     */
    private void startParticleTask() {
        particleTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getConfigManager().areParticlesEnabled()) {
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasDragonEgg(player)) {
                    spawnDragonParticles(player);
                }
            }
        }, 20L, 20L).getTaskId(); // Every second
    }

    /**
     * Spawn dragon particles around the player
     */
    private void spawnDragonParticles(Player player) {
        String particleType = plugin.getConfigManager().getParticleType();
        int count = plugin.getConfigManager().getParticleCount();

        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
            player.getWorld().spawnParticle(
                particle,
                player.getLocation().add(0, 1, 0),
                count,
                0.5, 0.5, 0.5,
                0.05
            );
        } catch (IllegalArgumentException e) {
            // Try fallback particle
            try {
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.END_ROD,
                    player.getLocation().add(0, 1, 0),
                    count,
                    0.5, 0.5, 0.5,
                    0.05
                );
            } catch (IllegalArgumentException ex) {
                // Ignore
            }
        }
    }

    /**
     * Handle player join - apply glowing effect
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Schedule glowing effect application
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (hasDragonEgg(player) && plugin.getConfigManager().isGlowingEnabled()) {
                applyGlowingEffect(player);
            }
        }, 30L);
    }

    /**
     * Apply glowing effect to player using scoreboard team
     */
    public void applyGlowingEffect(Player player) {
        if (!plugin.getConfigManager().isGlowingEnabled()) {
            return;
        }

        String colorName = plugin.getConfigManager().getGlowingColor();
        
        // Get or create team
        Team team = player.getScoreboard().getTeam("dragonegg_glow");
        if (team == null) {
            team = player.getScoreboard().registerNewTeam("dragonegg_glow");
        }

        // Set team color
        try {
            ChatColor color = ChatColor.valueOf(colorName);
            team.setColor(color);
        } catch (IllegalArgumentException e) {
            try {
                team.setColor(ChatColor.DARK_PURPLE);
            } catch (Exception ex) {
                // Ignore if setColor is not available
            }
        }

        // Add player to team (this enables glowing in most versions)
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        // Set collision rule
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }

    /**
     * Remove glowing effect from player
     */
    public void removeGlowingEffect(Player player) {
        Team team = player.getScoreboard().getTeam("dragonegg_glow");
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    /**
     * Check if a player has the Dragon Egg
     */
    private boolean hasDragonEgg(Player player) {
        if (plugin.getConfigManager().isInventoryModeEnabled()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == org.bukkit.Material.DRAGON_EGG) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Cancel the particle task on disable
     */
    public void cancelParticleTask() {
        if (particleTaskId != -1) {
            Bukkit.getScheduler().cancelTask(particleTaskId);
        }
    }
}
