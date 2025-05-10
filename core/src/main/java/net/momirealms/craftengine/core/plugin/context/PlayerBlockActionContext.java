package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.parameter.BlockParameterProvider;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameterProvider;
import net.momirealms.craftengine.core.plugin.context.parameter.PlayerParameterProvider;
import net.momirealms.craftengine.core.world.BlockInWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerBlockActionContext extends PlayerOptionalContext {

    public PlayerBlockActionContext(@NotNull Player player, @NotNull BlockInWorld block, @NotNull ContextHolder contexts) {
        super(player, contexts, List.of(new CommonParameterProvider(), new PlayerParameterProvider(player), new BlockParameterProvider(block)));
    }

    public static PlayerBlockActionContext of(@NotNull Player player, @NotNull BlockInWorld block, @NotNull ContextHolder contexts) {
        return new PlayerBlockActionContext(player, block, contexts);
    }
}
