package com.dragonegg.listeners;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import com.dragonegg.menu.DragonEggMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * DragonEggListener - Handles inventory and menu events
 */
public class DragonEggListener implements Listener {

    private final DragonEggMain plugin;

    public DragonEggListener(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle inventory clicks for menu interaction
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();

        // Handle DragonEggMenu clicks
        if (holder instanceof DragonEggMenu menu) {
            event.setCancelled(true);

            if (event.getRawSlot() >= 0 && event.getRawSlot() < event.getInventory().getSize()) {
                menu.handleClick(event.getRawSlot());

                // Play click sound
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }

            return;
        }

        // Dragon Egg can be freely moved between inventories
        // No restrictions on trading, dropping, or storing
    }

    /**
     * Handle inventory close to save menu state
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof DragonEggMenu) {
            // Save any pending changes
            plugin.getHeartManager().saveAllHearts();
            plugin.getEffectManager().saveAllEffects();
        }
    }
}
