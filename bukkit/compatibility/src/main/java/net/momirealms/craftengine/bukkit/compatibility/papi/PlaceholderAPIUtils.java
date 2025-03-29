package net.momirealms.craftengine.bukkit.compatibility.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIUtils {

    private PlaceholderAPIUtils() {}

    public static String parse(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static void registerExpansions(CraftEngine plugin) {
        new ImageExpansion(plugin).register();
        new ShiftExpansion(plugin).register();
    }
}
