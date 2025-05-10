package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Key;

public interface BlockInWorld {

    default boolean canBeReplaced(BlockPlaceContext blockPlaceContext) {
        return false;
    }

    default boolean isWaterSource(BlockPlaceContext blockPlaceContext) {
        return false;
    }

    Key owner();

    String getAsString();

    World world();

    int x();

    int y();

    int z();
}
