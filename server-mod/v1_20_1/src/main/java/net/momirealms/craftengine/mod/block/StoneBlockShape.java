package net.momirealms.craftengine.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.momirealms.craftengine.shared.block.BlockShape;

public class StoneBlockShape implements BlockShape {
    private final BlockState rawBlockState;

    public StoneBlockShape(BlockState rawBlockState) {
        this.rawBlockState = rawBlockState;
    }

    @Override
    public Object getShape(Object thisObj, Object[] args) {
        return rawBlockState.getShape((BlockGetter) args[1], (BlockPos) args[2], (CollisionContext) args[3]);
    }

    @Override
    public Object getCollisionShape(Object thisObj, Object[] args) {
        return rawBlockState.getCollisionShape((BlockGetter) args[1], (BlockPos) args[2], (CollisionContext) args[3]);
    }

    @Override
    public Object getSupportShape(Object thisObj, Object[] args) {
        return rawBlockState.getBlockSupportShape((BlockGetter) args[1], (BlockPos) args[2]);
    }
}
