package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.LazyContextParameterProvider;
import net.momirealms.craftengine.core.util.MCUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class PlayerParameterProvider implements LazyContextParameterProvider {
    private static final Map<ContextKey<?>, Function<Player, Object>> CONTEXT_FUNCTIONS = new HashMap<>();
    static {
        CONTEXT_FUNCTIONS.put(PlayerParameters.X, Entity::x);
        CONTEXT_FUNCTIONS.put(PlayerParameters.Y, Entity::y);
        CONTEXT_FUNCTIONS.put(PlayerParameters.Z, Entity::z);
        CONTEXT_FUNCTIONS.put(PlayerParameters.POS, Entity::position);
        CONTEXT_FUNCTIONS.put(PlayerParameters.BLOCK_X, p -> MCUtils.fastFloor(p.x()));
        CONTEXT_FUNCTIONS.put(PlayerParameters.BLOCK_Y, p -> MCUtils.fastFloor(p.y()));
        CONTEXT_FUNCTIONS.put(PlayerParameters.BLOCK_Z, p -> MCUtils.fastFloor(p.z()));
        CONTEXT_FUNCTIONS.put(PlayerParameters.NAME, Player::name);
        CONTEXT_FUNCTIONS.put(PlayerParameters.UUID, Player::uuid);
        CONTEXT_FUNCTIONS.put(PlayerParameters.WORLD_NAME, p -> p.world().name());
        CONTEXT_FUNCTIONS.put(PlayerParameters.MAIN_HAND_ITEM, p -> p.getItemInHand(InteractionHand.MAIN_HAND));
        CONTEXT_FUNCTIONS.put(PlayerParameters.OFF_HAND_ITEM, p -> p.getItemInHand(InteractionHand.OFF_HAND));
    }

    private final Player player;

    public PlayerParameterProvider(@NotNull Player player) {
        this.player = Objects.requireNonNull(player);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return (Optional<T>) Optional.ofNullable(CONTEXT_FUNCTIONS.get(parameter)).map(f -> f.apply(this.player));
    }
}