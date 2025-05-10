package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.block.behavior.CropBlockBehavior;
import net.momirealms.craftengine.bukkit.block.behavior.GrassBlockBehavior;
import net.momirealms.craftengine.bukkit.block.behavior.SaplingBlockBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitBlockInWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.block.Block;

import java.nio.file.Path;
import java.util.Map;

public class BoneMealItemBehavior extends ItemBehavior {
    public static final Factory FACTORY = new Factory();
    public static final BoneMealItemBehavior INSTANCE = new BoneMealItemBehavior();

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        if (context.getPlayer().isAdventureMode()) {
            return InteractionResult.PASS;
        }

        BukkitBlockInWorld clicked = (BukkitBlockInWorld) context.getLevel().getBlockAt(context.getClickedPos());
        Block block = clicked.block();
        ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockDataToId(block.getBlockData()));
        if (state == null || state.isEmpty()) return InteractionResult.PASS;

        boolean shouldHandle =false;
        if (state.behavior() instanceof CropBlockBehavior blockBehavior) {
            if (!blockBehavior.isMaxAge(state)) {
                shouldHandle = true;
            }
        } else if (state.behavior() instanceof SaplingBlockBehavior) {
            shouldHandle = true;
        } else if (state.behavior() instanceof GrassBlockBehavior) {
            if (block.getLocation().add(0, 1, 0).getBlock().isEmpty()) {
                shouldHandle = true;
            }
        }

        if (!shouldHandle) return InteractionResult.PASS;

        boolean sendSwing = false;
        try {
            Object visualState = state.vanillaBlockState().handle();
            Object visualStateBlock = Reflections.method$BlockStateBase$getBlock.invoke(visualState);
            if (Reflections.clazz$BonemealableBlock.isInstance(visualStateBlock)) {
                boolean is = FastNMS.INSTANCE.method$BonemealableBlock$isValidBonemealTarget(visualStateBlock, context.getLevel().serverWorld(), LocationUtils.toBlockPos(context.getClickedPos()), visualState);
                if (!is) {
                    sendSwing = true;
                }
            } else {
                sendSwing = true;
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to check visual state bone meal state", e);
        }
        if (sendSwing) {
            context.getPlayer().swingHand(context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    public static class Factory implements ItemBehaviorFactory {

        @Override
        public ItemBehavior create(Pack pack, Path path, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
