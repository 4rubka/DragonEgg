package com.dragonegg.DragonEggPlugin;

import com.dragonegg.commands.DragonEggCommand;
import com.dragonegg.config.ConfigManager;
import com.dragonegg.config.MessageManager;
import com.dragonegg.effects.EffectManager;
import com.dragonegg.hearts.HeartManager;
import com.dragonegg.listeners.BlockListener;
import com.dragonegg.listeners.DragonEggListener;
import com.dragonegg.listeners.PlayerListener;
import com.dragonegg.listeners.VisualListener;
import com.dragonegg.mechanics.*;
import com.dragonegg.menu.MenuManager;
import com.dragonegg.utils.Metrics;
import com.dragonegg.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * DragonEggMain - Main plugin class for DragonEgg
 * 
 * A powerful Dragon Egg plugin with unique mechanics and abilities.
 * Features include permanent effects, extra hearts, dragon aura, fury mode,
 * evolution system, bond system, shield, revive mechanic, and more.
 * 
 * @author DragonEggTeam
 * @version 1.0.0
 */
public class DragonEggMain extends JavaPlugin {

    private static DragonEggMain instance;

    // Managers
    private ConfigManager configManager;
    private MessageManager messageManager;
    private EffectManager effectManager;
    private HeartManager heartManager;
    private MenuManager menuManager;

    // Mechanics
    private DragonAura dragonAura;
    private EggEvolution eggEvolution;
    private DragonBond dragonBond;
    private DragonShield dragonShield;
    private ReviveMechanic reviveMechanic;

    // Listeners
    private VisualListener visualListener;

    // Metrics ID for bStats
    private static final int METRICS_ID = 0; // Set your bStats ID here
    
    @Override
    public void onEnable() {
        instance = this;
        
        log("&6=====================================");
        log("&6      DragonEgg Plugin v1.0.0        ");
        log("&6      By DragonEggTeam               ");
        log("&6=====================================");
        
        // Initialize configuration
        initializeConfig();
        
        // Initialize managers
        initializeManagers();
        
        // Initialize mechanics
        initializeMechanics();
        
        // Register listeners
        registerListeners();
        
        // Register commands
        registerCommands();
        
        // Initialize bStats metrics
        initializeMetrics();
        
        // Check for updates
        checkForUpdates();
        
        log("&aDragonEgg has been successfully enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save all player data
        if (dragonBond != null) dragonBond.saveAllBonds();
        if (eggEvolution != null) eggEvolution.saveAllProgress();
        if (heartManager != null) heartManager.saveAllHearts();
        if (effectManager != null) effectManager.saveAllEffects();

        // Cancel visual effects
        if (visualListener != null) visualListener.cancelParticleTask();

        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);

        log("&cDragonEgg has been disabled.");
    }
    
    /**
     * Initialize configuration files
     */
    private void initializeConfig() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        messageManager = new MessageManager(this);
        messageManager.loadMessages();
    }
    
    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        effectManager = new EffectManager(this);
        heartManager = new HeartManager(this);
        menuManager = new MenuManager(this);
    }
    
    /**
     * Initialize all game mechanics
     */
    private void initializeMechanics() {
        dragonAura = new DragonAura(this);
        eggEvolution = new EggEvolution(this);
        dragonBond = new DragonBond(this);
        dragonShield = new DragonShield(this);
        reviveMechanic = new ReviveMechanic(this);
    }

    /**
     * Register all event listeners
     */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new DragonEggListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
        visualListener = new VisualListener(this);
        Bukkit.getPluginManager().registerEvents(visualListener, this);
    }
    
    /**
     * Register all commands
     */
    private void registerCommands() {
        DragonEggCommand command = new DragonEggCommand(this);
        getCommand("dragonegg").setExecutor(command);
        getCommand("dragonegg").setTabCompleter(command);
    }
    
    /**
     * Initialize bStats metrics for plugin statistics
     */
    private void initializeMetrics() {
        if (getConfig().getBoolean("metrics.enabled", true)) {
            new Metrics(this, METRICS_ID);
            log("&7bStats metrics initialized.");
        }
    }
    
    /**
     * Check for plugin updates
     */
    private void checkForUpdates() {
        if (getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this, 0).fetchVersion(version -> {
                if (isVersionNewer(version)) {
                    log("&eA new version is available: " + version);
                    log("&eDownload at: " + getConfig().getString("update-checker.download-url", "https://modrinth.com/plugin/dragonegg"));
                }
            });
        }
    }
    
    /**
     * Check if a version string is newer than current
     */
    private boolean isVersionNewer(String version) {
        String current = getDescription().getVersion();
        return version.compareTo(current) > 0;
    }
    
    /**
     * Log a message with color code support
     */
    private void log(String message) {
        getLogger().info(message.replace("&", "§"));
    }
    
    // ==================== GETTERS ====================
    
    public static DragonEggMain getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    public HeartManager getHeartManager() {
        return heartManager;
    }
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public DragonAura getDragonAura() {
        return dragonAura;
    }

    public VisualListener getVisualListener() {
        return visualListener;
    }
    
    public EggEvolution getEggEvolution() {
        return eggEvolution;
    }
    
    public DragonBond getDragonBond() {
        return dragonBond;
    }
    
    public DragonShield getDragonShield() {
        return dragonShield;
    }
    
    public ReviveMechanic getReviveMechanic() {
        return reviveMechanic;
    }
}
