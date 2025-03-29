package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.shared.block.BlockBehavior;

public abstract class AbstractBlockBehavior extends BlockBehavior {

    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state;
    }
}
