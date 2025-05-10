package net.momirealms.craftengine.bukkit.plugin.command.feature;

import net.momirealms.craftengine.bukkit.plugin.command.BukkitCommandFeature;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.StringParser;

public class TestCommand extends BukkitCommandFeature<CommandSender> {

    public TestCommand(CraftEngineCommandManager<CommandSender> commandManager, CraftEngine plugin) {
        super(commandManager, plugin);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(org.incendo.cloud.CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("text", StringParser.greedyStringParser())
                .handler(context -> {
                    String text = "<red><arg:1:0912012><papi:player_name></red>";
                    PlayerOptionalContext context1 = PlayerOptionalContext.of(plugin().adapt(context.sender()), ContextHolder.builder());
                    plugin().senderFactory().wrap(context.sender()).sendMessage(AdventureHelper.customMiniMessage().deserialize(text, context1.tagResolvers()));
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_test";
    }
}
