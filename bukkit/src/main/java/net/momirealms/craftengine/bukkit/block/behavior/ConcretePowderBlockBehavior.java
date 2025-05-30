package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.shared.block.BlockBehavior;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ConcretePowderBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Key targetBlock; // TODO 更宽泛的，使用state，似乎也不是很好的方案？
    private Object defaultBlockState;
    private ImmutableBlockState defaultImmutableBlockState;

    public ConcretePowderBlockBehavior(CustomBlock block, Key targetBlock) {
        super(block);
        this.targetBlock = targetBlock;
    }

    public ImmutableBlockState defaultImmutableBlockState() {
        if (this.defaultImmutableBlockState == null) {
            this.getDefaultBlockState();
        }
        return this.defaultImmutableBlockState;
    }

    public Object getDefaultBlockState() {
        if (this.defaultBlockState != null) {
            return this.defaultBlockState;
        }
        Optional<CustomBlock> optionalCustomBlock = BukkitBlockManager.instance().blockById(this.targetBlock);
        if (optionalCustomBlock.isPresent()) {
            CustomBlock customBlock = optionalCustomBlock.get();
            this.defaultBlockState = customBlock.defaultState().customBlockState().handle();
            this.defaultImmutableBlockState = customBlock.defaultState();
        } else {
            CraftEngine.instance().logger().warn("Failed to create solid block " + this.targetBlock + " in ConcretePowderBlockBehavior");
            this.defaultBlockState = Reflections.instance$Blocks$STONE$defaultState;
            this.defaultImmutableBlockState = EmptyBlock.STATE;
        }
        return this.defaultBlockState;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Object level = context.getLevel().serverWorld();
        Object blockPos = LocationUtils.toBlockPos(context.getClickedPos());
        try {
            Object previousState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, blockPos);
            if (!shouldSolidify(level, blockPos, previousState)) {
                return super.updateStateForPlacement(context, state);
            } else {
                BlockState craftBlockState = (BlockState) Reflections.method$CraftBlockStates$getBlockState.invoke(null, level, blockPos);
                craftBlockState.setBlockData(BlockStateUtils.fromBlockData(getDefaultBlockState()));
                BlockFormEvent event = new BlockFormEvent(craftBlockState.getBlock(), craftBlockState);
                if (!EventUtils.fireAndCheckCancel(event)) {
                    return defaultImmutableBlockState();
                } else {
                    return super.updateStateForPlacement(context, state);
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to update state for placement " + context.getClickedPos(), e);
        }
        return super.updateStateForPlacement(context, state);
    }

    @Override
    public void onLand(Object thisBlock, Object[] args) throws Exception {
        Object world = args[0];
        Object blockPos = args[1];
        Object replaceableState = args[3];
        if (shouldSolidify(world, blockPos, replaceableState)) {
            Reflections.method$CraftEventFactory$handleBlockFormEvent.invoke(null, world, blockPos, getDefaultBlockState(), UpdateOption.UPDATE_ALL.flags());
        }
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object level;
        Object pos;
        if (VersionHelper.isOrAbove1_21_2()) {
            level = args[1];
            pos = args[3];
        } else {
            level = args[3];
            pos = args[4];
        }
        if (touchesLiquid(level, pos)) {
            if (!Reflections.clazz$Level.isInstance(level)) {
                return getDefaultBlockState();
            } else {
                BlockState craftBlockState = (BlockState) Reflections.method$CraftBlockStates$getBlockState.invoke(null, level, pos);
                craftBlockState.setBlockData(BlockStateUtils.fromBlockData(getDefaultBlockState()));
                BlockFormEvent event = new BlockFormEvent(craftBlockState.getBlock(), craftBlockState);
                if (!EventUtils.fireAndCheckCancel(event)) {
                    return Reflections.method$CraftBlockState$getHandle.invoke(craftBlockState);
                }
            }
        }
        return args[0];
    }

    private static boolean shouldSolidify(Object level, Object blockPos, Object blockState) throws ReflectiveOperationException {
        return canSolidify(blockState) || touchesLiquid(level, blockPos);
    }

    private static boolean canSolidify(Object state) throws ReflectiveOperationException {
        Object fluidState = Reflections.field$BlockStateBase$fluidState.get(state);
        if (fluidState == null) return false;
        Object fluidType = Reflections.method$FluidState$getType.invoke(fluidState);
        return fluidType == Reflections.instance$Fluids$WATER || fluidType == Reflections.instance$Fluids$FLOWING_WATER;
    }

    private static boolean touchesLiquid(Object level, Object pos) throws ReflectiveOperationException {
        boolean flag = false;
        Object mutablePos = Reflections.method$BlockPos$mutable.invoke(pos);
        int j = Direction.values().length;
        for (int k = 0; k < j; k++) {
            Object direction = Reflections.instance$Directions[k];
            Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, mutablePos);
            if (direction != Reflections.instance$Direction$DOWN || canSolidify(blockState)) {
                Reflections.method$MutableBlockPos$setWithOffset.invoke(mutablePos, pos, direction);
                blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, mutablePos);
                if (canSolidify(blockState) && !(boolean) Reflections.method$BlockStateBase$isFaceSturdy.invoke(blockState, level, pos, FastNMS.INSTANCE.method$Direction$getOpposite(direction), Reflections.instance$SupportType$FULL)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            String solidBlock = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("solid-block"), "warning.config.block.behavior.concrete.missing_solid");
            return new ConcretePowderBlockBehavior(block, Key.of(solidBlock));
        }
    }
}
