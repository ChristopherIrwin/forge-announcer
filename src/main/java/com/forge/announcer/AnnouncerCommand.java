package com.forge.announcer;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Handles /fannouncer &lt;reload|broadcast|list&gt;. All subcommands require
 * the forgeannouncer.admin permission.
 */
public final class AnnouncerCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String PERMISSION = "forgeannouncer.admin";

    private final AnnouncerManager manager;

    public AnnouncerCommand(AnnouncerManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(MINI_MESSAGE.deserialize("<red>You don't have permission to use this command.</red>"));
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                manager.reload();
                sender.sendMessage(MINI_MESSAGE.deserialize(
                        "<green>ForgeAnnouncer reloaded: <white>" + manager.count() + "</white> announcement(s) active.</green>"));
            }
            case "list" -> {
                List<String> ids = manager.ids();
                if (ids.isEmpty()) {
                    sender.sendMessage(MINI_MESSAGE.deserialize("<yellow>No announcements configured.</yellow>"));
                } else {
                    String joined = String.join("<gray>, </gray><white>", ids);
                    sender.sendMessage(MINI_MESSAGE.deserialize("<green>Announcements: <white>" + joined + "</white></green>"));
                }
            }
            case "broadcast" -> {
                if (args.length < 2) {
                    sender.sendMessage(MINI_MESSAGE.deserialize("<red>Usage: /" + label + " broadcast <id></red>"));
                } else if (manager.broadcastNow(args[1])) {
                    String safe = MINI_MESSAGE.escapeTags(args[1]);
                    sender.sendMessage(MINI_MESSAGE.deserialize("<green>Broadcasted announcement <white>" + safe + "</white>.</green>"));
                } else {
                    String safe = MINI_MESSAGE.escapeTags(args[1]);
                    sender.sendMessage(MINI_MESSAGE.deserialize("<red>Unknown announcement id: <white>" + safe + "</white></red>"));
                }
            }
            default -> sendUsage(sender, label);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(List.of("reload", "broadcast", "list"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("broadcast")) {
            return filter(manager.ids(), args[1]);
        }
        return List.of();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(MINI_MESSAGE.deserialize("<gray>Usage: /" + label + " <reload|broadcast <id>|list></gray>"));
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
