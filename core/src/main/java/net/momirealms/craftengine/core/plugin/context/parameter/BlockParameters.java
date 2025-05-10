package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.util.Key;

public final class BlockParameters {
    private BlockParameters() {}

    public static final ContextKey<Double> X = ContextKey.of("block.x");
    public static final ContextKey<Double> Y = ContextKey.of("block.y");
    public static final ContextKey<Double> Z = ContextKey.of("block.z");
    public static final ContextKey<Integer> BLOCK_X = ContextKey.of("block.block_x");
    public static final ContextKey<Integer> BLOCK_Y = ContextKey.of("block.block_y");
    public static final ContextKey<Integer> BLOCK_Z = ContextKey.of("block.block_z");
    public static final ContextKey<ImmutableBlockState> BLOCK_STATE = ContextKey.of("block.state");
    public static final ContextKey<Key> BLOCK_OWNER = ContextKey.of("block.owner.id");
    public static final ContextKey<String> WORLD_NAME = ContextKey.of("block.world.name");
}
