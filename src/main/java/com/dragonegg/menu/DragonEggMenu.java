package com.dragonegg.menu;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * DragonEggMenu - Main configuration menu for Dragon Egg
 * 
 * Allows players to toggle and configure features via GUI
 */
public class DragonEggMenu implements InventoryHolder {

    private final DragonEggMain plugin;
    private final Player player;
    private Inventory inventory;
    
    // Menu types
    public enum MenuType {
        MAIN,
        EFFECTS,
        HEARTS,
        MECHANICS,
        VISUALS
    }
    
    private MenuType currentType = MenuType.MAIN;

    public DragonEggMenu(DragonEggMain plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Open the main menu
     */
    public void open() {
        openMenu(MenuType.MAIN);
    }

    /**
     * Open a specific menu type
     */
    public void openMenu(MenuType type) {
        currentType = type;
        
        switch (type) {
            case MAIN -> createMainMenu();
            case EFFECTS -> createEffectsMenu();
            case HEARTS -> createHeartsMenu();
            case MECHANICS -> createMechanicsMenu();
            case VISUALS -> createVisualsMenu();
        }
        
        player.openInventory(inventory);
    }

    /**
     * Create the main menu
     */
    private void createMainMenu() {
        inventory = Bukkit.createInventory(this, 54, plugin.getMessageManager().getMessage("menu.title"));
        
        MenuManager mm = plugin.getMenuManager();
        
        // Fill borders
        fillBorder(mm);
        
        // Effects option (slot 11)
        ItemStack effectsItem = mm.createMenuItem(
            Material.BREWING_STAND,
            "&e&lEffects",
            Arrays.asList(
                "&7Manage your Dragon Egg effects",
                "&7Add, remove, or modify effects",
                "",
                "&aClick to open"
            )
        );
        inventory.setItem(11, effectsItem);
        
        // Hearts option (slot 13)
        ItemStack heartsItem = mm.createMenuItem(
            Material.REDSTONE,
            "&c&lExtra Hearts",
            Arrays.asList(
                "&7Manage extra hearts",
                "&7Current: &e" + (int) plugin.getHeartManager().getExtraHearts(player),
                "",
                "&aClick to open"
            )
        );
        inventory.setItem(13, heartsItem);
        
        // Mechanics option (slot 15)
        ItemStack mechanicsItem = mm.createMenuItem(
            Material.COMPARATOR,
            "&b&lMechanics",
            Arrays.asList(
                "&7Manage Dragon Egg mechanics",
                "&7Aura, Fury, Shield, Revive",
                "",
                "&aClick to open"
            )
        );
        inventory.setItem(15, mechanicsItem);
        
        // Visuals option (slot 29)
        ItemStack visualsItem = mm.createMenuItem(
            Material.FIREWORK_STAR,
            "&d&lVisual Effects",
            Arrays.asList(
                "&7Manage visual effects",
                "&7Particles, sounds, glowing",
                "",
                "&aClick to open"
            )
        );
        inventory.setItem(29, visualsItem);

        // Evolution info (slot 31)
        int level = plugin.getEggEvolution().getLevel(player);
        var evolutionData = plugin.getEggEvolution().getEvolutionData(player);
        int kills = evolutionData != null ? evolutionData.kills : 0;
        int playtime = evolutionData != null ? evolutionData.playtime : 0;
        
        ItemStack evolutionItem = mm.createMenuItem(
            Material.DRAGON_EGG,
            "&5&lEvolution",
            Arrays.asList(
                "&7Current level: &e" + level,
                "&7Progress: &e" + String.format("%.1f", plugin.getEggEvolution().getProgressPercent(player)) + "%",
                "",
                "&7Kills: &e" + kills,
                "&7Playtime: &e" + playtime + "m"
            )
        );
        inventory.setItem(31, evolutionItem);
        
        // Close button (slot 49)
        ItemStack closeItem = mm.createMenuItem(
            Material.BARRIER,
            "&c&lClose Menu",
            Arrays.asList(
                "&7Click to close"
            )
        );
        inventory.setItem(49, closeItem);
    }

    /**
     * Create the effects menu
     */
    private void createEffectsMenu() {
        inventory = Bukkit.createInventory(this, 54, "&5&lEffects Configuration");

        MenuManager mm = plugin.getMenuManager();

        // Fill borders
        fillBorder(mm);

        // Row 1 - Combat Effects
        ItemStack strengthItem = mm.createMenuItem(
            Material.BLAZE_POWDER,
            "&c&lStrength",
            Arrays.asList(
                "&7Click to add Strength I",
                "&7Right-click for Strength II"
            )
        );
        inventory.setItem(10, strengthItem);

        ItemStack speedItem = mm.createMenuItem(
            Material.FEATHER,
            "&b&lSpeed",
            Arrays.asList(
                "&7Click to add Speed I",
                "&7Right-click for Speed II"
            )
        );
        inventory.setItem(11, speedItem);

        ItemStack hasteItem = mm.createMenuItem(
            Material.GOLDEN_PICKAXE,
            "&e&lHaste",
            Arrays.asList(
                "&7Click to add Haste I",
                "&7Right-click for Haste II"
            )
        );
        inventory.setItem(12, hasteItem);

        // Row 2 - Defense Effects
        ItemStack regenItem = mm.createMenuItem(
            Material.GHAST_TEAR,
            "&a&lRegeneration",
            Arrays.asList(
                "&7Click to add Regeneration I",
                "&7Right-click for Regeneration II"
            )
        );
        inventory.setItem(19, regenItem);

        ItemStack resistanceItem = mm.createMenuItem(
            Material.SHIELD,
            "&7&lResistance",
            Arrays.asList(
                "&7Click to add Resistance I",
                "&7Right-click for Resistance II"
            )
        );
        inventory.setItem(20, resistanceItem);

        ItemStack absorptionItem = mm.createMenuItem(
            Material.GOLDEN_APPLE,
            "&6&lAbsorption",
            Arrays.asList(
                "&7Click to add Absorption I",
                "&7Right-click for Absorption II"
            )
        );
        inventory.setItem(21, absorptionItem);

        // Row 3 - Utility Effects
        ItemStack fireResItem = mm.createMenuItem(
            Material.MAGMA_CREAM,
            "&4&lFire Resistance",
            Arrays.asList(
                "&7Click to add Fire Resistance"
            )
        );
        inventory.setItem(28, fireResItem);

        ItemStack waterBreathingItem = mm.createMenuItem(
            Material.COD,
            "&3&lWater Breathing",
            Arrays.asList(
                "&7Click to add Water Breathing"
            )
        );
        inventory.setItem(29, waterBreathingItem);

        ItemStack nightVisionItem = mm.createMenuItem(
            Material.SPYGLASS,
            "&e&lNight Vision",
            Arrays.asList(
                "&7Click to add Night Vision"
            )
        );
        inventory.setItem(30, nightVisionItem);

        // Row 4 - Special Effects
        ItemStack jumpBoostItem = mm.createMenuItem(
            Material.RABBIT_FOOT,
            "&a&lJump Boost",
            Arrays.asList(
                "&7Click to add Jump Boost I",
                "&7Right-click for Jump Boost II"
            )
        );
        inventory.setItem(37, jumpBoostItem);

        ItemStack invisibilityItem = mm.createMenuItem(
            Material.FERMENTED_SPIDER_EYE,
            "&5&lInvisibility",
            Arrays.asList(
                "&7Click to add Invisibility"
            )
        );
        inventory.setItem(38, invisibilityItem);

        ItemStack luckItem = mm.createMenuItem(
            Material.EXPERIENCE_BOTTLE,
            "&2&lLuck",
            Arrays.asList(
                "&7Click to add Luck"
            )
        );
        inventory.setItem(39, luckItem);

        // Clear effects button (center)
        ItemStack clearItem = mm.createMenuItem(
            Material.BUCKET,
            "&c&lClear All Effects",
            Arrays.asList(
                "&7Click to remove all effects"
            )
        );
        inventory.setItem(31, clearItem);

        // Back button
        ItemStack backItem = mm.createMenuItem(
            Material.ARROW,
            "&e&lBack",
            Arrays.asList(
                "&7Return to main menu"
            )
        );
        inventory.setItem(49, backItem);
    }

    /**
     * Create the hearts menu
     */
    private void createHeartsMenu() {
        inventory = Bukkit.createInventory(this, 54, "&5&lExtra Hearts Configuration");
        
        MenuManager mm = plugin.getMenuManager();
        
        // Fill borders
        fillBorder(mm);
        
        double currentHearts = plugin.getHeartManager().getExtraHearts(player);
        
        // Current hearts display
        ItemStack displayItem = mm.createMenuItem(
            Material.REDSTONE_BLOCK,
            "&c&lCurrent Extra Hearts",
            Arrays.asList(
                "&7You currently have:",
                "&e" + (int) currentHearts + " &7extra hearts",
                "&7(&e+" + (int) (currentHearts * 2) + " &7HP)",
                "",
                "&7Max: &e" + plugin.getConfigManager().getMaxHearts()
            )
        );
        inventory.setItem(13, displayItem);
        
        // Add heart buttons
        ItemStack addOneItem = mm.createMenuItem(
            Material.LIME_WOOL,
            "&a&l+1 Heart",
            Arrays.asList(
                "&7Click to add 1 heart"
            )
        );
        inventory.setItem(10, addOneItem);
        
        ItemStack addFiveItem = mm.createMenuItem(
            Material.GREEN_WOOL,
            "&a&l+5 Hearts",
            Arrays.asList(
                "&7Click to add 5 hearts"
            )
        );
        inventory.setItem(11, addFiveItem);
        
        // Remove heart buttons
        ItemStack removeOneItem = mm.createMenuItem(
            Material.RED_WOOL,
            "&c&l-1 Heart",
            Arrays.asList(
                "&7Click to remove 1 heart"
            )
        );
        inventory.setItem(15, removeOneItem);
        
        ItemStack removeFiveItem = mm.createMenuItem(
            Material.BROWN_WOOL,
            "&c&l-5 Hearts",
            Arrays.asList(
                "&7Click to remove 5 hearts"
            )
        );
        inventory.setItem(16, removeFiveItem);
        
        // Set specific amount
        ItemStack setItem = mm.createMenuItem(
            Material.PAPER,
            "&e&lSet Amount",
            Arrays.asList(
                "&7Click and type amount in chat"
            )
        );
        inventory.setItem(31, setItem);
        
        // Back button
        ItemStack backItem = mm.createMenuItem(
            Material.ARROW,
            "&e&lBack",
            Arrays.asList(
                "&7Return to main menu"
            )
        );
        inventory.setItem(49, backItem);
    }

    /**
     * Create the mechanics menu
     */
    private void createMechanicsMenu() {
        inventory = Bukkit.createInventory(this, 54, "&5&lMechanics Configuration");
        
        MenuManager mm = plugin.getMenuManager();
        
        // Fill borders
        fillBorder(mm);
        
        // Evolution info
        ItemStack evolutionItem = mm.createMenuItem(
            Material.DRAGON_EGG,
            "&5&lEvolution System",
            Arrays.asList(
                "&7Your Level: &e" + plugin.getEggEvolution().getLevel(player),
                "",
                "&7Level up by getting kills",
                "&7and playing with the egg"
            )
        );
        inventory.setItem(31, evolutionItem);
        
        // Back button
        ItemStack backItem = mm.createMenuItem(
            Material.ARROW,
            "&e&lBack",
            Arrays.asList(
                "&7Return to main menu"
            )
        );
        inventory.setItem(49, backItem);
    }

    /**
     * Create the visuals menu
     */
    private void createVisualsMenu() {
        inventory = Bukkit.createInventory(this, 54, "&5&lVisual Effects Configuration");
        
        MenuManager mm = plugin.getMenuManager();
        
        // Fill borders
        fillBorder(mm);
        
        // Particles toggle
        boolean particlesEnabled = plugin.getConfigManager().areParticlesEnabled();
        ItemStack particlesItem = mm.createToggleItem(
            particlesEnabled,
            "Particles",
            Arrays.asList(
                "&7Status: " + (particlesEnabled ? "&aEnabled" : "&cDisabled"),
                "&7Type: &e" + plugin.getConfigManager().getParticleType(),
                "&7Count: &e" + plugin.getConfigManager().getParticleCount(),
                "",
                "&7Show particle effects"
            )
        );
        inventory.setItem(11, particlesItem);
        
        // Glowing toggle
        boolean glowingEnabled = plugin.getConfigManager().isGlowingEnabled();
        ItemStack glowingItem = mm.createToggleItem(
            glowingEnabled,
            "Glowing Effect",
            Arrays.asList(
                "&7Status: " + (glowingEnabled ? "&aEnabled" : "&cDisabled"),
                "&7Color: &e" + plugin.getConfigManager().getGlowingColor(),
                "",
                "&7Make player glow"
            )
        );
        inventory.setItem(15, glowingItem);
        
        // Custom item toggle
        boolean customItemEnabled = plugin.getConfigManager().isCustomItemEnabled();
        ItemStack customItem = mm.createToggleItem(
            customItemEnabled,
            "Custom Egg Item",
            Arrays.asList(
                "&7Status: " + (customItemEnabled ? "&aEnabled" : "&cDisabled"),
                "&7Name: &e" + plugin.getConfigManager().getCustomItemName(),
                "",
                "&7Use custom name and lore"
            )
        );
        inventory.setItem(29, customItem);
        
        // Preview item
        ItemStack previewItem = new ItemStack(Material.DRAGON_EGG);
        if (customItemEnabled) {
            var meta = previewItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.getConfigManager().getCustomItemName().replace("&", "§"));
                List<String> lore = plugin.getConfigManager().getCustomItemLore();
                if (lore != null) {
                    List<String> coloredLore = new java.util.ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(line.replace("&", "§"));
                    }
                    meta.setLore(coloredLore);
                }
                previewItem.setItemMeta(meta);
            }
        }
        inventory.setItem(31, previewItem);
        
        // Back button
        ItemStack backItem = mm.createMenuItem(
            Material.ARROW,
            "&e&lBack",
            Arrays.asList(
                "&7Return to main menu"
            )
        );
        inventory.setItem(49, backItem);
    }

    /**
     * Fill menu borders with glass panes
     */
    private void fillBorder(MenuManager mm) {
        ItemStack filler = mm.createFiller();
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, filler);
            inventory.setItem(45 + i, filler);
        }
        
        // Left and right columns
        for (int i = 0; i < 6; i++) {
            inventory.setItem(i * 9, filler);
            inventory.setItem(i * 9 + 8, filler);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Handle menu clicks
     */
    public void handleClick(int slot) {
        switch (currentType) {
            case MAIN -> handleMainMenuClick(slot);
            case EFFECTS -> handleEffectsClick(slot);
            case HEARTS -> handleHeartsClick(slot);
            case MECHANICS -> handleMechanicsClick(slot);
            case VISUALS -> handleVisualsClick(slot);
        }
    }

    private void handleMainMenuClick(int slot) {
        if (slot == 11) openMenu(MenuType.EFFECTS);
        else if (slot == 13) openMenu(MenuType.HEARTS);
        else if (slot == 15) openMenu(MenuType.MECHANICS);
        else if (slot == 29) openMenu(MenuType.VISUALS);
        else if (slot == 49) player.closeInventory();
    }

    private void handleEffectsClick(int slot) {
        // Row 1 - Combat Effects
        if (slot == 10) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("STRENGTH"), 1);
        else if (slot == 11) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("SPEED"), 1);
        else if (slot == 12) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("HASTE"), 1);
        // Row 2 - Defense Effects
        else if (slot == 19) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("REGENERATION"), 1);
        else if (slot == 20) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("RESISTANCE"), 1);
        else if (slot == 21) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("ABSORPTION"), 1);
        // Row 3 - Utility Effects
        else if (slot == 28) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("FIRE_RESISTANCE"), 0);
        else if (slot == 29) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("WATER_BREATHING"), 0);
        else if (slot == 30) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("NIGHT_VISION"), 0);
        // Row 4 - Special Effects
        else if (slot == 37) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("JUMP_BOOST"), 1);
        else if (slot == 38) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("INVISIBILITY"), 0);
        else if (slot == 39) plugin.getEffectManager().addEffect(player, org.bukkit.potion.PotionEffectType.getByName("LUCK"), 0);
        // Clear effects and back
        else if (slot == 31) plugin.getEffectManager().clearEffects(player);
        else if (slot == 49) openMenu(MenuType.MAIN);
    }

    private void handleHeartsClick(int slot) {
        if (slot == 10) plugin.getHeartManager().addExtraHearts(player, 1);
        else if (slot == 11) plugin.getHeartManager().addExtraHearts(player, 5);
        else if (slot == 15) plugin.getHeartManager().removeExtraHearts(player, 1);
        else if (slot == 16) plugin.getHeartManager().removeExtraHearts(player, 5);
        else if (slot == 49) openMenu(MenuType.MAIN);
    }

    private void handleMechanicsClick(int slot) {
        if (slot == 49) openMenu(MenuType.MAIN);
    }

    private void handleVisualsClick(int slot) {
        if (slot == 49) openMenu(MenuType.MAIN);
    }
}
