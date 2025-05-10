package net.momirealms.craftengine.bukkit.plugin;

import net.momirealms.craftengine.core.plugin.Platform;
import org.bukkit.Bukkit;

public class BukkitPlatform implements Platform {

    @Override
    public void dispatchCommand(String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
