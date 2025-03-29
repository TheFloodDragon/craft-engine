package net.momirealms.craftengine.bukkit.font;

import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.font.AbstractImageManager;
import net.momirealms.craftengine.core.font.ImageManager;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.util.CharacterUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class BukkitImageManager extends AbstractImageManager implements Listener {
    private final BukkitCraftEngine plugin;
    private final Object serializer;

    public BukkitImageManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        try {
            Object builder = Reflections.method$GsonComponentSerializer$builder.invoke(null);
            this.serializer = Reflections.method$GsonComponentSerializer$Builder$build.invoke(builder);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
    }

    @Override
    public void disable() {
        super.disable();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("UnstableApiUsage")
    public void onChat(AsyncChatDecorateEvent event) {
        if (!ConfigManager.filterChat()) return;
        this.processChatEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("UnstableApiUsage")
    public void onChatCommand(AsyncChatCommandDecorateEvent event) {
        if (!ConfigManager.filterChat()) return;
        this.processChatEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!ConfigManager.filterCommand()) return;
        if (!this.isDefaultFontInUse()) return;
        if (event.getPlayer().hasPermission(ImageManager.BYPASS_COMMAND)) {
            return;
        }
        runIfContainsIllegalCharacter(event.getMessage(), event::setMessage);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void processChatEvent(AsyncChatDecorateEvent event) {
        Player player = event.player();
        if (player == null) return;
        if (!this.isDefaultFontInUse()) return;
        if (player.hasPermission(ImageManager.BYPASS_CHAT)) {
            return;
        }
        try {
            Object originalMessage = Reflections.field$AsyncChatDecorateEvent$originalMessage.get(event);
            String jsonMessage = (String) Reflections.method$ComponentSerializer$serialize.invoke(serializer, originalMessage);
            runIfContainsIllegalCharacter(jsonMessage, (json) -> {
                try {
                    Object component = Reflections.method$ComponentSerializer$deserialize.invoke(serializer, json);
                    Reflections.method$AsyncChatDecorateEvent$result.invoke(event, component);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void runIfContainsIllegalCharacter(String string, Consumer<String> callback) {
        //noinspection DuplicatedCode
        char[] chars = string.toCharArray();
        int[] codepoints = CharacterUtils.charsToCodePoints(chars);
        int[] newCodepoints = new int[codepoints.length];
        boolean hasIllegal = false;
        for (int i = 0; i < codepoints.length; i++) {
            int codepoint = codepoints[i];
            if (!isIllegalCharacter(codepoint)) {
                newCodepoints[i] = codepoint;
            } else {
                newCodepoints[i] = '*';
                hasIllegal = true;
            }
        }
        if (hasIllegal) {
            callback.accept(new String(newCodepoints, 0, newCodepoints.length));
        }
    }
}
