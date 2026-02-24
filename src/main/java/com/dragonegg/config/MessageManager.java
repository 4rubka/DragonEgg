package com.dragonegg.config;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MessageManager - Handles multi-language messages
 * 
 * Supports English, Ukrainian, Polish, German, French, Spanish
 */
public class MessageManager {

    private final DragonEggMain plugin;
    private FileConfiguration messages;
    private File messagesFile;
    private String currentLanguage;
    
    // Cache for translated messages
    private final Map<String, String> messageCache = new HashMap<>();
    
    // Supported languages
    public static final String ENGLISH = "en";
    public static final String UKRAINIAN = "ua";
    public static final String POLISH = "pl";
    public static final String GERMAN = "de";
    public static final String FRENCH = "fr";
    public static final String SPANISH = "es";

    public MessageManager(DragonEggMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Load messages configuration
     */
    public void loadMessages() {
        currentLanguage = plugin.getConfig().getString("language", "en");
        
        messagesFile = new File(plugin.getDataFolder(), "messages_" + currentLanguage + ".yml");
        
        // Create default messages file if not exists
        if (!messagesFile.exists()) {
            plugin.saveResource("messages_" + currentLanguage + ".yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Reload to get latest values
        try {
            messages.load(messagesFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading messages file: " + e.getMessage());
        }
        
        // Cache all messages
        cacheMessages();
    }

    /**
     * Cache all messages for faster access
     */
    private void cacheMessages() {
        if (messages == null) return;
        
        for (String key : messages.getKeys(true)) {
            if (messages.isString(key)) {
                messageCache.put(key, messages.getString(key));
            }
        }
    }

    /**
     * Reload messages from disk
     */
    public void reloadMessages() {
        loadMessages();
    }

    /**
     * Get a message by key with color code support
     */
    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }

    /**
     * Get a message with placeholders
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messageCache.getOrDefault(key, "&cMessage not found: " + key);
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        
        // Convert legacy color codes (&) to modern format
        return message.replace("&", "§");
    }

    /**
     * Get a list of messages (for lore, etc.)
     */
    public List<String> getMessageList(String key) {
        return messages.getStringList(key);
    }

    /**
     * Send a message to a player
     */
    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessage(key));
    }

    /**
     * Send a message with placeholders to a player
     */
    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(getMessage(key, placeholders));
    }

    /**
     * Send action bar message to player
     */
    public void sendActionBar(Player player, String key) {
        String message = getMessage(key);
        player.sendActionBar(Component.text(message.replace("§", "")));
    }

    /**
     * Send title to player
     */
    public void sendTitle(Player player, String titleKey, String subtitleKey, int fadeIn, int stay, int fadeOut) {
        String title = getMessage(titleKey);
        String subtitle = getMessage(subtitleKey);
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Get the current language code
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Set the language for a player (stored in player data)
     */
    public void setLanguage(String language) {
        this.currentLanguage = language;
        loadMessages();
    }

    /**
     * Check if a language is supported
     */
    public static boolean isLanguageSupported(String language) {
        return language.equals(ENGLISH) || language.equals(UKRAINIAN) || 
               language.equals(POLISH) || language.equals(GERMAN) || 
               language.equals(FRENCH) || language.equals(SPANISH);
    }
}
