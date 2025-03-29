package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockStateParser;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// TODO A better command suggestion system
public class WorldEditCommandHelper implements Listener {
    private final BukkitBlockManager manager;
    private final BukkitCraftEngine plugin;

    public WorldEditCommandHelper(BukkitCraftEngine plugin, BukkitBlockManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("//")) return;

        Set<String> cachedNamespaces = manager.namespacesInUse();
        String[] args = message.split(" ");
        boolean modified = false;

        for (int i = 1; i < args.length; i++) {
            String[] parts = args[i].split(",");
            List<String> processedParts = new ArrayList<>(parts.length);
            boolean partModified = false;

            for (String part : parts) {
                String processed = processIdentifier(part, cachedNamespaces);
                partModified |= !part.equals(processed);
                processedParts.add(processed);
            }

            if (partModified) {
                args[i] = String.join(",", processedParts);
                modified = true;
            }
        }

        if (modified) {
            event.setMessage(String.join(" ", args));
        }
    }

    private String processIdentifier(String identifier, Set<String> cachedNamespaces) {
        int colonIndex = identifier.indexOf(':');
        if (colonIndex == -1) return identifier;

        String namespace = identifier.substring(0, colonIndex);
        if (!cachedNamespaces.contains(namespace)) return identifier;

        ImmutableBlockState state = BlockStateParser.deserialize(identifier);
        if (state == null) return identifier;

        try {
            return BlockStateUtils.getBlockOwnerIdFromState(
                    state.customBlockState().handle()
            ).toString();
        } catch (NullPointerException e) {
            return identifier;
        }
    }
}
