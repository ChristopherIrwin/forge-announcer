package com.forge.announcer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Loads announcements from config, rotates each on its own Bukkit scheduler
 * interval, and delivers the formatted message to every online player.
 */
public final class AnnouncerManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ForgeAnnouncer plugin;
    private final List<Announcement> announcements = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<BossBar> activeBars = new ArrayList<>();
    private final Map<String, Integer> sequentialIndex = new HashMap<>();

    public AnnouncerManager(ForgeAnnouncer plugin) {
        this.plugin = plugin;
    }

    public void start() {
        load();
    }

    public void stop() {
        cancelAll();
    }

    public void reload() {
        cancelAll();
        plugin.reloadConfig();
        load();
    }

    public int count() {
        return announcements.size();
    }

    public List<String> ids() {
        List<String> ids = new ArrayList<>();
        for (Announcement announcement : announcements) {
            ids.add(announcement.id());
        }
        return ids;
    }

    /** Immediately broadcasts one announcement's next message. Returns false when the id is unknown. */
    public boolean broadcastNow(String id) {
        for (Announcement announcement : announcements) {
            if (announcement.id().equalsIgnoreCase(id)) {
                deliver(announcement);
                return true;
            }
        }
        return false;
    }

    private void load() {
        List<Map<?, ?>> entries = plugin.getConfig().getMapList("announcements");
        for (Map<?, ?> entry : entries) {
            Announcement announcement = Announcement.fromMap(entry);
            if (announcement == null) {
                plugin.getLogger().warning("Skipping announcement entry: needs an id, at least one message, and a positive interval-seconds.");
                continue;
            }
            announcements.add(announcement);
            long periodTicks = announcement.intervalSeconds() * 20L;
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> deliver(announcement), periodTicks, periodTicks);
            tasks.add(task);
            plugin.getLogger().info("Scheduled announcement '" + announcement.id() + "' every "
                    + announcement.intervalSeconds() + "s (" + announcement.delivery().name().toLowerCase() + ").");
        }
    }

    private void cancelAll() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        for (BossBar bar : activeBars) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hideBossBar(bar);
            }
        }
        activeBars.clear();
        announcements.clear();
        sequentialIndex.clear();
    }

    private void deliver(Announcement announcement) {
        Component message = nextMessage(announcement);
        switch (announcement.delivery()) {
            case CHAT -> Bukkit.broadcast(message);
            case ACTIONBAR -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(message);
                }
            }
            case TITLE -> {
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(announcement.titleFadeIn() * 50L),
                        Duration.ofMillis(announcement.titleStay() * 50L),
                        Duration.ofMillis(announcement.titleFadeOut() * 50L));
                Title title = Title.title(message, Component.empty(), times);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(title);
                }
            }
            case BOSSBAR -> {
                BossBar bar = BossBar.bossBar(message, 1.0f, announcement.bossbarColor(), BossBar.Overlay.PROGRESS);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showBossBar(bar);
                }
                activeBars.add(bar);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.hideBossBar(bar);
                    }
                    activeBars.remove(bar);
                }, announcement.bossbarSeconds() * 20L);
            }
        }
    }

    private Component nextMessage(Announcement announcement) {
        List<String> messages = announcement.messages();
        String raw;
        if (announcement.mode() == Announcement.Mode.RANDOM || messages.size() == 1) {
            raw = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        } else {
            int index = sequentialIndex.getOrDefault(announcement.id(), 0);
            raw = messages.get(index % messages.size());
            sequentialIndex.put(announcement.id(), index + 1);
        }
        return MINI_MESSAGE.deserialize(raw);
    }
}
