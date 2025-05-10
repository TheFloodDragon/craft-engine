package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameterProvider;
import net.momirealms.craftengine.core.plugin.context.parameter.PlayerParameterProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerFurnitureActionContext extends PlayerOptionalContext {

    public PlayerFurnitureActionContext(@NotNull Player player, @NotNull Furniture furniture, @NotNull ContextHolder contexts) {
        super(player, contexts, List.of(new CommonParameterProvider(), new PlayerParameterProvider(player)));
    }

    public static PlayerFurnitureActionContext of(@NotNull Player player, @NotNull Furniture furniture, @NotNull ContextHolder contexts) {
        return new PlayerFurnitureActionContext(player, furniture, contexts);
    }
}
