package com.dragonegg.menu;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuManager - Manages GUI menus for DragonEgg configuration
 */
public class MenuManager {

    private final DragonEggMain plugin;

    public MenuManager(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a menu item with custom name and lore
     */
    public ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            
            if (lore != null) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(line.replace("&", "§"));
                }
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Create a menu item with custom name
     */
    public ItemStack createMenuItem(Material material, String name) {
        return createMenuItem(material, name, null);
    }

    /**
     * Create a toggle item (on/off)
     */
    public ItemStack createToggleItem(boolean enabled, String name, List<String> lore) {
        Material material = enabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String displayName = enabled ? "&a&l" + name : "&c&l" + name;
        
        return createMenuItem(material, displayName, lore);
    }

    /**
     * Create a filler item for menu
     */
    public ItemStack createFiller() {
        return createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }
}
