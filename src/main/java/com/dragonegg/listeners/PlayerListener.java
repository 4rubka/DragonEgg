package com.dragonegg.listeners;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * PlayerListener - Handles player-related events for Dragon Egg mechanics
 */
public class PlayerListener implements Listener {

    private final DragonEggMain plugin;

    public PlayerListener(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player join - apply effects and hearts
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Schedule task to apply effects after a short delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (hasDragonEgg(player)) {
                // Apply effects
                plugin.getEffectManager().applyAllEffects(player);

                // Apply hearts
                plugin.getHeartManager().updatePlayerHealth(player);

                // Send welcome message
                player.sendMessage(plugin.getMessageManager().getMessage("egg.in-range"));
            }
        }, 20L); // 1 second delay
    }

    /**
     * Handle player quit - save data
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save player data
        plugin.getHeartManager().saveAllHearts();
        plugin.getEffectManager().saveAllEffects();
        plugin.getDragonBond().saveAllBonds();
        plugin.getEggEvolution().saveAllProgress();

        // Remove glowing effect
        if (plugin.getVisualListener() != null) {
            plugin.getVisualListener().removeGlowingEffect(player);
        }
    }

    /**
     * Handle player respawn - reapply effects
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Schedule task to apply effects after respawn
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (hasDragonEgg(player)) {
                plugin.getEffectManager().applyAllEffects(player);
                plugin.getHeartManager().updatePlayerHealth(player);
            }
        }, 5L);
    }

    /**
     * Handle entity death - track kills for evolution
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // Track kill for evolution
        if (hasDragonEgg(killer)) {
            plugin.getEggEvolution().addKill(killer);
        }
    }

    /**
     * Handle player death - check for revive
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (hasDragonEgg(player)) {
            // Check for revive
            if (plugin.getReviveMechanic().handleDeath(event)) {
                // Cancel death if revived
                event.setKeepLevel(true);
                event.setDroppedExp(0);
                
                // Clear death message
                event.setDeathMessage(null);
            }
        }
        
        // Check if killer has Dragon Egg for special death message
        if (player.getKiller() != null && hasDragonEgg(player.getKiller())) {
            String message = plugin.getMessageManager().getMessage("death.by-egg-holder")
                .replace("%player%", player.getName())
                .replace("%killer%", player.getKiller().getName());
            event.setDeathMessage(message);
        } else if (hasDragonEgg(player)) {
            String message = plugin.getMessageManager().getMessage("death.with-egg")
                .replace("%player%", player.getName());
            event.setDeathMessage(message);
        }
    }

    /**
     * Handle damage - check for shield proc
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (hasDragonEgg(player)) {
            // Check for shield proc
            if (plugin.getDragonShield().handleDamage(event, player)) {
                return; // Damage was negated
            }
        }
    }

    /**
     * Check if a player has the Dragon Egg in their inventory
     */
    private boolean hasDragonEgg(Player player) {
        if (plugin.getConfigManager().isInventoryModeEnabled()) {
            for (ItemStack item : player.getInventory().getContents()) {
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
