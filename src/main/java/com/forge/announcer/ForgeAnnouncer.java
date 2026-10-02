package com.forge.announcer;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * ForgeAnnouncer — scheduled MiniMessage broadcasts delivered via chat,
 * action bar, boss bar, or title.
 */
public final class ForgeAnnouncer extends JavaPlugin {

    private AnnouncerManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.manager = new AnnouncerManager(this);
        this.manager.start();
        AnnouncerCommand handler = new AnnouncerCommand(manager);
        var command = getCommand("fannouncer");
        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            getLogger().warning("Command 'fannouncer' is missing from plugin.yml; commands will not work.");
        }
        getLogger().info("Enabled with " + manager.count() + " announcement(s).");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.stop();
        }
    }
}
