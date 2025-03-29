package net.momirealms.craftengine.bukkit.plugin.bstats;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import org.bstats.bukkit.Metrics;

public class CraftEngineMetrics {
    private static final int pluginId = 24333;

    public static void init(BukkitCraftEngine plugin) {
        if (!ConfigManager.metrics()) return;
        new Metrics(plugin.bootstrap(), pluginId);
    }
}
