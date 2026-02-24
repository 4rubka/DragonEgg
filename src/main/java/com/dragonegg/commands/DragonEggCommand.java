package com.dragonegg.commands;

import com.dragonegg.DragonEggPlugin.DragonEggMain;
import com.dragonegg.menu.DragonEggMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * DragonEggCommand - Main command handler for DragonEgg plugin
 * 
 * Handles all subcommands: give, reload, info, sethearts, addeffect, removeeffect, seteffectlevel, cleareffects, menu
 */
public class DragonEggCommand implements CommandExecutor, TabCompleter {

    private final DragonEggMain plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public DragonEggCommand(DragonEggMain plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    /**
     * Register all subcommands
     */
    private void registerSubCommands() {
        subCommands.put("give", new GiveCommand());
        subCommands.put("reload", new ReloadCommand());
        subCommands.put("info", new InfoCommand());
        subCommands.put("sethearts", new SetHeartsCommand());
        subCommands.put("addeffect", new AddEffectCommand());
        subCommands.put("removeeffect", new RemoveEffectCommand());
        subCommands.put("seteffectlevel", new SetEffectLevelCommand());
        subCommands.put("cleareffects", new ClearEffectsCommand());
        subCommands.put("menu", new MenuCommand());
        subCommands.put("help", new HelpCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sendMessage(sender, "&cUnknown command. Use /dragonegg help for help.");
            return true;
        }

        // Check permission
        if (!sender.hasPermission(subCommand.getPermission())) {
            sendMessage(sender, plugin.getMessageManager().getMessage("general.no-permission"));
            return true;
        }

        // Execute command
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete subcommand names
            for (String subCommand : subCommands.keySet()) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length >= 2) {
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandName);
            
            if (subCommand != null) {
                completions = subCommand.onTabComplete(sender, args);
            }
        }

        return completions;
    }

    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "&5&l=== Dragon Egg Commands ===");
        sendMessage(sender, "&e/dragonegg give <player> &7- Give Dragon Egg");
        sendMessage(sender, "&e/dragonegg reload &7- Reload configuration");
        sendMessage(sender, "&e/dragonegg info &7- Show Dragon Egg info");
        sendMessage(sender, "&e/dragonegg sethearts <amount> &7- Set extra hearts");
        sendMessage(sender, "&e/dragonegg menu &7- Open configuration menu");
        sendMessage(sender, "&e/dragonegg help &7- Show all commands");
    }

    /**
     * Send a message to a sender
     */
    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message.replace("&", "§"));
    }

    // ==================== SUB COMMAND CLASSES ====================

    /**
     * Interface for subcommands
     */
    private interface SubCommand {
        void execute(CommandSender sender, String[] args);
        String getPermission();
        List<String> onTabComplete(CommandSender sender, String[] args);
    }

    /**
     * /dragonegg give <player> - Give Dragon Egg to a player
     */
    private class GiveCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sendMessage(sender, "&cUsage: /dragonegg give <player>");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, plugin.getMessageManager().getMessage("general.player-not-found"));
                return;
            }

            ItemStack dragonEgg = new ItemStack(org.bukkit.Material.DRAGON_EGG, 1);
            if (plugin.getConfigManager().isCustomItemEnabled()) {
                var meta = dragonEgg.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(plugin.getConfigManager().getCustomItemName().replace("&", "§"));
                    var lore = plugin.getConfigManager().getCustomItemLore();
                    if (lore != null && !lore.isEmpty()) {
                        List<String> coloredLore = new ArrayList<>();
                        for (String line : lore) {
                            coloredLore.add(line.replace("&", "§"));
                        }
                        meta.setLore(coloredLore);
                    }
                    dragonEgg.setItemMeta(meta);
                }
            }

            target.getInventory().addItem(dragonEgg);
            sendMessage(sender, plugin.getMessageManager().getMessage("commands.give")
                .replace("%player%", target.getName()));
            
            // Send message to recipient
            target.sendMessage(plugin.getMessageManager().getMessage("egg.obtained"));
            plugin.getHeartManager().updatePlayerHealth(target);
            plugin.getEffectManager().applyAllEffects(target);
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.give";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        }
    }

    /**
     * /dragonegg reload - Reload configuration
     */
    private class ReloadCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            plugin.getConfigManager().reloadConfig();
            plugin.getMessageManager().reloadMessages();
            sendMessage(sender, plugin.getMessageManager().getMessage("general.reload"));
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.reload";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    /**
     * /dragonegg info - Show Dragon Egg info
     */
    private class InfoCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            Player target;
            
            if (args.length > 0 && sender.hasPermission("dragonegg.commands.info.others")) {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sendMessage(sender, plugin.getMessageManager().getMessage("general.player-not-found"));
                    return;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sendMessage(sender, "&cUsage: /dragonegg info [player]");
                return;
            }

            sendMessage(sender, "&5&l=== Dragon Egg Info ===");
            
            // Bond info
            boolean bonded = plugin.getDragonBond().isBonded(target);
            String bondStatus = bonded ? 
                plugin.getMessageManager().getMessage("bond.info").replace("%player%", target.getName()) :
                plugin.getMessageManager().getMessage("bond.no-bond");
            sendMessage(sender, "&7Bonded: " + bondStatus);
            
            // Hearts info
            double hearts = plugin.getHeartManager().getExtraHearts(target);
            sendMessage(sender, "&7Extra Hearts: &e" + (int) hearts);

            // Effects info
            var effects = plugin.getEffectManager().getPlayerEffects(target);
            if (effects.isEmpty()) {
                sendMessage(sender, "&7Effects: &eNone");
            } else {
                StringBuilder effectsStr = new StringBuilder();
                for (Map.Entry<String, Integer> entry : effects.entrySet()) {
                    if (effectsStr.length() > 0) effectsStr.append(", ");
                    effectsStr.append(entry.getKey());
                    // Append Roman numerals for level
                    for (int i = 0; i < entry.getValue(); i++) {
                        effectsStr.append("I");
                    }
                }
                sendMessage(sender, "&7Effects: &e" + effectsStr);
            }
            
            // Evolution info
            int level = plugin.getEggEvolution().getLevel(target);
            sendMessage(sender, "&7Evolution Level: &e" + level);
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.info";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        }
    }

    /**
     * /dragonegg sethearts <amount> - Set extra hearts
     */
    private class SetHeartsCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "&cThis command can only be used by players!");
                return;
            }

            if (args.length < 1) {
                sendMessage(sender, "&cUsage: /dragonegg sethearts <amount>");
                return;
            }

            Player player = (Player) sender;
            int amount;
            
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "&cInvalid number!");
                return;
            }

            if (amount < 0) {
                sendMessage(sender, "&cAmount cannot be negative!");
                return;
            }

            plugin.getHeartManager().setExtraHearts(player, amount);
            sendMessage(sender, plugin.getMessageManager().getMessage("hearts.set")
                .replace("%amount%", String.valueOf(amount)));
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.sethearts";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    /**
     * /dragonegg addEffect <effect> <level> - Add effect
     */
    private class AddEffectCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "&cThis command can only be used by players!");
                return;
            }

            if (args.length < 2) {
                sendMessage(sender, "&cUsage: /dragonegg addEffect <effect> <level>");
                return;
            }

            Player player = (Player) sender;
            PotionEffectType effectType = PotionEffectType.getByName(args[0].toUpperCase());
            
            if (effectType == null) {
                sendMessage(sender, "&cInvalid effect type!");
                return;
            }

            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "&cInvalid level!");
                return;
            }

            if (level < 1 || level > 255) {
                sendMessage(sender, "&cLevel must be between 1 and 255!");
                return;
            }

            plugin.getEffectManager().addEffect(player, effectType, level);
            sendMessage(sender, plugin.getMessageManager().getMessage("effects.added")
                .replace("%effect%", effectType.getName())
                .replace("%level%", String.valueOf(level)));
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.addeffect";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> completions = new ArrayList<>();
            
            if (args.length == 1) {
                // Complete effect names
                String partial = args[0].toLowerCase();
                for (PotionEffectType type : PotionEffectType.values()) {
                    if (type != null && type.getName() != null) {
                        String effectName = type.getName().toLowerCase();
                        if (effectName.startsWith(partial)) {
                            completions.add(type.getName());
                        }
                    }
                }
            } else if (args.length == 2) {
                // Complete effect levels
                String partial = args[1];
                if (partial.isEmpty() || partial.equals("1")) completions.add("1");
                if (partial.isEmpty() || partial.equals("2")) completions.add("2");
                if (partial.isEmpty() || partial.equals("3")) completions.add("3");
                if (partial.isEmpty() || partial.equals("4")) completions.add("4");
                if (partial.isEmpty() || partial.equals("5")) completions.add("5");
            }
            
            return completions;
        }
    }

    /**
     * /dragonegg removeEffect <effect> - Remove effect
     */
    private class RemoveEffectCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "&cThis command can only be used by players!");
                return;
            }

            if (args.length < 1) {
                sendMessage(sender, "&cUsage: /dragonegg removeEffect <effect>");
                return;
            }

            Player player = (Player) sender;
            PotionEffectType effectType = PotionEffectType.getByName(args[0].toUpperCase());
            
            if (effectType == null) {
                sendMessage(sender, "&cInvalid effect type!");
                return;
            }

            plugin.getEffectManager().removeEffect(player, effectType);
            sendMessage(sender, plugin.getMessageManager().getMessage("effects.removed")
                .replace("%effect%", effectType.getName()));
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.removeeffect";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                String partial = args[0].toLowerCase();
                for (PotionEffectType type : PotionEffectType.values()) {
                    if (type != null && type.getName() != null) {
                        String effectName = type.getName().toLowerCase();
                        if (effectName.startsWith(partial)) {
                            completions.add(type.getName());
                        }
                    }
                }
            }
            return completions;
        }
    }

    /**
     * /dragonegg setEffectLevel <effect> <level> - Set effect level
     */
    private class SetEffectLevelCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "&cThis command can only be used by players!");
                return;
            }

            if (args.length < 2) {
                sendMessage(sender, "&cUsage: /dragonegg setEffectLevel <effect> <level>");
                return;
            }

            Player player = (Player) sender;
            PotionEffectType effectType = PotionEffectType.getByName(args[0].toUpperCase());
            
            if (effectType == null) {
                sendMessage(sender, "&cInvalid effect type!");
                return;
            }

            int level;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "&cInvalid level!");
                return;
            }

            plugin.getEffectManager().setEffectLevel(player, effectType, level);
            sendMessage(sender, plugin.getMessageManager().getMessage("effects.level-set")
                .replace("%effect%", effectType.getName())
                .replace("%level%", String.valueOf(level)));
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.seteffect";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> completions = new ArrayList<>();
            
            if (args.length == 1) {
                String partial = args[0].toLowerCase();
                for (PotionEffectType type : PotionEffectType.values()) {
                    if (type != null && type.getName() != null) {
                        String effectName = type.getName().toLowerCase();
                        if (effectName.startsWith(partial)) {
                            completions.add(type.getName());
                        }
                    }
                }
            } else if (args.length == 2) {
                String partial = args[1];
                if (partial.isEmpty() || partial.equals("1")) completions.add("1");
                if (partial.isEmpty() || partial.equals("2")) completions.add("2");
                if (partial.isEmpty() || partial.equals("3")) completions.add("3");
                if (partial.isEmpty() || partial.equals("4")) completions.add("4");
                if (partial.isEmpty() || partial.equals("5")) completions.add("5");
            }
            
            return completions;
        }
    }

    /**
     * /dragonegg clearEffects - Clear all effects
     */
    private class ClearEffectsCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "&cThis command can only be used by players!");
                return;
            }

            Player player = (Player) sender;
            plugin.getEffectManager().clearEffects(player);
            sendMessage(sender, plugin.getMessageManager().getMessage("effects.cleared"));
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.cleareffects";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    /**
     * /dragonegg menu - Open configuration menu
     */
    private class MenuCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "&cThis command can only be used by players!");
                return;
            }

            Player player = (Player) sender;
            DragonEggMenu menu = new DragonEggMenu(plugin, player);
            menu.open();
        }

        @Override
        public String getPermission() {
            return "dragonegg.commands.menu";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    /**
     * /dragonegg help - Show all commands
     */
    private class HelpCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            sendHelp(sender);
            
            sendMessage(sender, " ");
            sendMessage(sender, "&e/dragonegg addeffect <effect> <level> &7- Add effect");
            sendMessage(sender, "&e/dragonegg removeeffect <effect> &7- Remove effect");
            sendMessage(sender, "&e/dragonegg seteffectlevel <effect> <level> &7- Set effect level");
            sendMessage(sender, "&e/dragonegg cleareffects &7- Clear all effects");
        }

        @Override
        public String getPermission() {
            return "dragonegg.use";
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }
}
