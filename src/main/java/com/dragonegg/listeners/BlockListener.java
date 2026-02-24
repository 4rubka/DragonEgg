package com.dragonegg.listeners;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * BlockListener - Handles block placement events for Dragon Egg
 *
 * Prevents Dragon Egg from being placed (configurable)
 */
public class BlockListener implements Listener {

    private final DragonEggMain plugin;

    public BlockListener(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle block placement - prevent Dragon Egg placement if configured
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) {
            return;
        }

        // Check if placing Dragon Egg is allowed
        if (!plugin.getConfigManager().isDragonEggPlaceable()) {
            event.setCancelled(true);
            
            // Send message
            event.getPlayer().sendMessage(
                plugin.getMessageManager().getMessage("egg.cannot-place")
            );
        }
    }
}
