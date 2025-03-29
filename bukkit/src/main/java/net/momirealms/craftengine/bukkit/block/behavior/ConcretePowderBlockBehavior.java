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
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.shared.block.BlockBehavior;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFormEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

// TODO Inject FallingBlockEntity?
public class ConcretePowderBlockBehavior extends FallingBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Key targetBlock;
    private Object defaultBlockState;
    private ImmutableBlockState defaultImmutableBlockState;

    public ConcretePowderBlockBehavior(float hurtAmount, int maxHurt, Key block) {
        super(hurtAmount, maxHurt);
        this.targetBlock = block;
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
        Optional<CustomBlock> optionalCustomBlock = BukkitBlockManager.instance().getBlock(this.targetBlock);
        if (optionalCustomBlock.isPresent()) {
            CustomBlock customBlock = optionalCustomBlock.get();
            this.defaultBlockState = customBlock.defaultState().customBlockState().handle();
            this.defaultImmutableBlockState = customBlock.defaultState();
        } else {
            CraftEngine.instance().logger().warn("Failed to create solid block " + this.targetBlock + " in ConcretePowderBlockBehavior");
            this.defaultBlockState = Reflections.instance$Blocks$STONE$defaultState;
            this.defaultImmutableBlockState = EmptyBlock.INSTANCE.defaultState();
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
        } catch (ReflectiveOperationException e) {
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
        if (VersionHelper.isVersionNewerThan1_21_2()) {
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
        return super.updateShape(thisBlock, args, superMethod);
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
                if (canSolidify(blockState) && !(boolean) Reflections.method$BlockStateBase$isFaceSturdy.invoke(blockState, level, pos, Reflections.getOppositeDirection(direction), Reflections.instance$SupportType$FULL)) {
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
            float hurtAmount = MiscUtils.getAsFloat(arguments.getOrDefault("hurt-amount", -1f));
            int hurtMax = MiscUtils.getAsInt(arguments.getOrDefault("max-hurt", -1));
            String solidBlock = (String) arguments.get("solid-block");
            if (solidBlock == null) {
                throw new IllegalArgumentException("No `solid-block` specified for concrete powder block behavior");
            }
            return new ConcretePowderBlockBehavior(hurtAmount, hurtMax, Key.of(solidBlock));
        }
    }
}
