package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.shared.block.BlockBehavior;

public abstract class AbstractBlockBehavior extends BlockBehavior {
    protected CustomBlock customBlock;

    public AbstractBlockBehavior(CustomBlock customBlock) {
        this.customBlock = customBlock;
    }

    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state;
    }

    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        return InteractionResult.PASS;
    }
}
