package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.shared.block.BlockBehavior;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HangingBlockBehavior extends BushBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public HangingBlockBehavior(List<Object> tagsCanSurviveOn, Set<Object> blocksCansSurviveOn, Set<String> customBlocksCansSurviveOn) {
        super(tagsCanSurviveOn, blocksCansSurviveOn, customBlocksCansSurviveOn);
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws ReflectiveOperationException {
        int y = FastNMS.INSTANCE.field$Vec3i$y(blockPos);
        int x = FastNMS.INSTANCE.field$Vec3i$x(blockPos);
        int z = FastNMS.INSTANCE.field$Vec3i$z(blockPos);
        Object belowPos = FastNMS.INSTANCE.constructor$BlockPos(x, y + 1, z);
        Object belowState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world, belowPos);
        return mayPlaceOn(belowState, world, belowPos);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Tuple<List<Object>, Set<Object>, Set<String>> tuple = readTagsAndState(arguments);
            return new HangingBlockBehavior(tuple.left(), tuple.mid(), tuple.right());
        }
    }
}
