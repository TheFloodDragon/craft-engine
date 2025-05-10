package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.LazyContextParameterProvider;
import net.momirealms.craftengine.core.world.BlockInWorld;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BlockParameterProvider implements LazyContextParameterProvider {
    private static final Map<ContextKey<?>, Function<BlockInWorld, Object>> CONTEXT_FUNCTIONS = new HashMap<>();
    static {
        CONTEXT_FUNCTIONS.put(BlockParameters.X, BlockInWorld::x);
        CONTEXT_FUNCTIONS.put(BlockParameters.Y, BlockInWorld::y);
        CONTEXT_FUNCTIONS.put(BlockParameters.Z, BlockInWorld::z);
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_X, BlockInWorld::x);
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_Y, BlockInWorld::y);
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_Z, BlockInWorld::z);
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_OWNER, BlockInWorld::owner);
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_STATE, BlockInWorld::getAsString);
        CONTEXT_FUNCTIONS.put(BlockParameters.WORLD_NAME, b -> b.world().name());
    }

    private final BlockInWorld block;

    public BlockParameterProvider(@NotNull BlockInWorld block) {
        this.block = Objects.requireNonNull(block);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return (Optional<T>) Optional.ofNullable(CONTEXT_FUNCTIONS.get(parameter)).map(f -> f.apply(this.block));
    }
}