package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public final class CommonParameters {
    private CommonParameters() {}

    public static final ContextKey<Double> RANDOM = ContextKey.of("random");
    public static final ContextKey<Double> LAST_RANDOM = ContextKey.of("last_random");
    public static final ContextKey<Vec3d> LOCATION = ContextKey.of("location");
    public static final ContextKey<World> WORLD = ContextKey.of("world");
    public static final ContextKey<Boolean> FALLING_BLOCK = ContextKey.of("falling_block");
    public static final ContextKey<Float> EXPLOSION_RADIUS = ContextKey.of("explosion_radius");
    public static final ContextKey<Player> PLAYER = ContextKey.of("player");
    public static final ContextKey<ImmutableBlockState> BLOCK_STATE = ContextKey.of("block_state");
}
