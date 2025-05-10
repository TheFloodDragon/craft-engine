package net.momirealms.craftengine.bukkit.block;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockBreakEvent;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlockEventListener implements Listener {
    private final BukkitCraftEngine plugin;
    private final boolean enableNoteBlockCheck;
    private final BukkitBlockManager manager;

    public BlockEventListener(BukkitCraftEngine plugin, BukkitBlockManager manager, boolean enableNoteBlockCheck) {
        this.plugin = plugin;
        this.manager = manager;
        this.enableNoteBlockCheck = enableNoteBlockCheck;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!VersionHelper.isOrAbove1_20_5()) {
            if (event.getDamager() instanceof Player player) {
                BukkitServerPlayer serverPlayer = plugin.adapt(player);
                serverPlayer.setClientSideCanBreakBlock(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        BukkitServerPlayer serverPlayer = plugin.adapt(player);
        // send swing if player is clicking a replaceable block
        if (serverPlayer.shouldResendSwing()) {
            player.swingHand(event.getHand());
        }
        // send sound if the placed block's sounds are removed
        if (Config.enableSoundSystem()) {
            Block block = event.getBlock();
            Object blockState = BlockStateUtils.blockDataToBlockState(block.getBlockData());
            if (blockState != Reflections.instance$Blocks$AIR$defaultState) {
                Object ownerBlock = BlockStateUtils.getBlockOwner(blockState);
                if (this.manager.isBlockSoundRemoved(ownerBlock)) {
                    if (player.getInventory().getItemInMainHand().getType() != Material.DEBUG_STICK) {
                        try {
                            Object soundType = Reflections.field$BlockBehaviour$soundType.get(ownerBlock);
                            Object placeSound = Reflections.field$SoundType$placeSound.get(soundType);
                            player.playSound(block.getLocation(), FastNMS.INSTANCE.field$SoundEvent$location(placeSound).toString(), SoundCategory.BLOCKS, 1f, 0.8f);
                        } catch (ReflectiveOperationException e) {
                            this.plugin.logger().warn("Failed to get sound type", e);
                        }
                    }
                    return;
                }
            }
        }
        // resend sound if the clicked block is interactable on client side
        if (serverPlayer.shouldResendSound()) {
            try {
                Block block = event.getBlock();
                Object blockState = BlockStateUtils.blockDataToBlockState(block.getBlockData());
                Object ownerBlock = BlockStateUtils.getBlockOwner(blockState);
                Object soundType = Reflections.field$BlockBehaviour$soundType.get(ownerBlock);
                Object placeSound = Reflections.field$SoundType$placeSound.get(soundType);
                player.playSound(block.getLocation(), FastNMS.INSTANCE.field$SoundEvent$location(placeSound).toString(), SoundCategory.BLOCKS, 1f, 0.8f);
            } catch (ReflectiveOperationException e) {
                this.plugin.logger().warn("Failed to get sound type", e);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreak(BlockBreakEvent event) {
        org.bukkit.block.Block block = event.getBlock();
        Object blockState = BlockStateUtils.blockDataToBlockState(block.getBlockData());
        int stateId = BlockStateUtils.blockStateToId(blockState);
        Player player = event.getPlayer();
        if (!BlockStateUtils.isVanillaBlock(stateId)) {
            ImmutableBlockState state = manager.getImmutableBlockStateUnsafe(stateId);
            if (!state.isEmpty()) {
                Location location = block.getLocation();
                BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
                // double check adventure mode to prevent dupe
                if (!FastNMS.INSTANCE.mayBuild(serverPlayer.serverPlayer()) && !serverPlayer.canBreak(LocationUtils.toBlockPos(location), null)) {
                    return;
                }

                // trigger event
                CustomBlockBreakEvent customBreakEvent = new CustomBlockBreakEvent(serverPlayer, location, block, state);
                boolean isCancelled = EventUtils.fireAndCheckCancel(customBreakEvent);
                if (isCancelled) {
                    event.setCancelled(true);
                    return;
                }

                net.momirealms.craftengine.core.world.World world = new BukkitWorld(location.getWorld());
                // handle waterlogged blocks
                @SuppressWarnings("unchecked")
                Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
                if (waterloggedProperty != null) {
                    boolean waterlogged = state.get(waterloggedProperty);
                    if (waterlogged) {
                        location.getWorld().setBlockData(location, Material.WATER.createBlockData());
                    }
                }
                // play sound
                Vec3d vec3d = new Vec3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
                world.playBlockSound(vec3d, state.sounds().breakSound());
                if (player.getGameMode() == GameMode.CREATIVE || !customBreakEvent.dropItems()) {
                    return;
                }

                Item<ItemStack> itemInHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                // do not drop if it's not the correct tool
                if (!BlockStateUtils.isCorrectTool(state, itemInHand)) {
                    return;
                }

                // drop items
                ContextHolder.Builder builder = ContextHolder.builder()
                        .withParameter(CommonParameters.WORLD, world)
                        .withParameter(CommonParameters.LOCATION, vec3d)
                        .withParameter(CommonParameters.PLAYER, serverPlayer);
                        //mark item .withOptionalParameter(CommonParameters.MAIN_HAND_ITEM, itemInHand);
                for (Item<Object> item : state.getDrops(builder, world, serverPlayer)) {
                    world.dropItemNaturally(vec3d, item);
                }
            }
        } else {
            // override vanilla block loots
            if (player.getGameMode() != GameMode.CREATIVE) {
                this.plugin.vanillaLootManager().getBlockLoot(stateId).ifPresent(it -> {
                    if (it.override()) {
                        event.setDropItems(false);
                        event.setExpToDrop(0);
                    }
                    Location location = block.getLocation();
                    BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
                    net.momirealms.craftengine.core.world.World world = new BukkitWorld(player.getWorld());
                    Vec3d vec3d = new Vec3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
                    ContextHolder.Builder builder = ContextHolder.builder()
                            .withParameter(CommonParameters.WORLD, world)
                            .withParameter(CommonParameters.LOCATION, vec3d)
                            .withParameter(CommonParameters.PLAYER, serverPlayer);
                            //mark item .withOptionalParameter(CommonParameters.MAIN_HAND_ITEM, serverPlayer.getItemInHand(InteractionHand.MAIN_HAND));
                    ContextHolder contextHolder = builder.build();
                    for (LootTable<?> lootTable : it.lootTables()) {
                        for (Item<?> item : lootTable.getRandomItems(contextHolder, world, serverPlayer)) {
                            world.dropItemNaturally(vec3d, item);
                        }
                    }
                });
            }
            // sound system
            if (Config.enableSoundSystem()) {
                Object ownerBlock = BlockStateUtils.getBlockOwner(blockState);
                if (this.manager.isBlockSoundRemoved(ownerBlock)) {
                    try {
                        Object soundType = Reflections.field$BlockBehaviour$soundType.get(ownerBlock);
                        Object breakSound = Reflections.field$SoundType$breakSound.get(soundType);
                        block.getWorld().playSound(block.getLocation(), FastNMS.INSTANCE.field$SoundEvent$location(breakSound).toString(), SoundCategory.BLOCKS, 1f, 0.8f);
                    } catch (ReflectiveOperationException e) {
                        this.plugin.logger().warn("Failed to get sound type", e);
                    }
                }
            }
        }
    }

    // BlockBreakBlockEvent = liquid + piston
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreakBlock(BlockBreakBlockEvent event) {
        Block block = event.getBlock();
        Object blockState = BlockStateUtils.blockDataToBlockState(block.getBlockData());
        int stateId = BlockStateUtils.blockStateToId(blockState);
        if (!BlockStateUtils.isVanillaBlock(stateId)) {
            // custom blocks
            ImmutableBlockState immutableBlockState = this.manager.getImmutableBlockStateUnsafe(stateId);
            if (!immutableBlockState.isEmpty()) {
                Location location = block.getLocation();
                net.momirealms.craftengine.core.world.World world = new BukkitWorld(block.getWorld());
                Vec3d vec3d = new Vec3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
                ContextHolder.Builder builder = ContextHolder.builder()
                        .withParameter(CommonParameters.WORLD, world)
                        .withParameter(CommonParameters.LOCATION, vec3d);
                for (Item<?> item : immutableBlockState.getDrops(builder, world, null)) {
                    world.dropItemNaturally(vec3d, item);
                }
            }
        } else {
            // override vanilla block loots
            this.plugin.vanillaLootManager().getBlockLoot(stateId).ifPresent(it -> {
                if (it.override()) {
                    event.getDrops().clear();
                    event.setExpToDrop(0);
                }

                Location location = block.getLocation();
                Vec3d vec3d = new Vec3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
                net.momirealms.craftengine.core.world.World world = new BukkitWorld(location.getWorld());
                ContextHolder.Builder builder = ContextHolder.builder();
                builder.withParameter(CommonParameters.WORLD, world);
                builder.withParameter(CommonParameters.LOCATION, vec3d);
                ContextHolder contextHolder = builder.build();
                for (LootTable<?> lootTable : it.lootTables()) {
                    for (Item<?> item : lootTable.getRandomItems(contextHolder, world, null)) {
                        world.dropItemNaturally(vec3d, item);
                    }
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStep(GenericGameEvent event) {
        if (event.getEvent() != GameEvent.STEP) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        BlockPos pos = EntityUtils.getOnPos(player);
        Location playerLocation = player.getLocation();
        BlockData blockData = player.getWorld().getBlockData(pos.x(), pos.y(), pos.z());
        Object blockState = BlockStateUtils.blockDataToBlockState(blockData);
        int stateId = BlockStateUtils.blockStateToId(blockState);
        if (!BlockStateUtils.isVanillaBlock(stateId)) {
            ImmutableBlockState state = manager.getImmutableBlockStateUnsafe(stateId);
            player.playSound(playerLocation, state.sounds().stepSound().id().toString(), SoundCategory.BLOCKS, state.sounds().stepSound().volume(), state.sounds().stepSound().pitch());
        } else if (Config.enableSoundSystem()) {
            Object ownerBlock = BlockStateUtils.getBlockOwner(blockState);
            if (manager.isBlockSoundRemoved(ownerBlock)) {
                try {
                    Object soundType = Reflections.field$BlockBehaviour$soundType.get(ownerBlock);
                    Object stepSound = Reflections.field$SoundType$stepSound.get(soundType);
                    player.playSound(playerLocation, FastNMS.INSTANCE.field$SoundEvent$location(stepSound).toString(), SoundCategory.BLOCKS, 0.15f, 1f);
                } catch (ReflectiveOperationException e) {
                    plugin.logger().warn("Failed to get sound type", e);
                }
            }
        }
    }

// Use BlockBreakBlock event
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    public void onPistonRetract(BlockPistonRetractEvent event) {
//        handlePistonEvent(event.getDirection(), event.getBlocks(), event.getBlock());
//    }
//
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    public void onPistonExtend(BlockPistonExtendEvent event) {
//        handlePistonEvent(event.getDirection(), event.getBlocks(), event.getBlock());
//    }
//
//    private void handlePistonEvent(BlockFace face, List<Block> blocksList, Block piston) {
//        int blocks = blocksList.size();
//        net.momirealms.craftengine.core.world.World world = new BukkitWorld(piston.getWorld());
//        for (int i = blocks - 1; i >= 0; --i) {
//            Location oldLocation = blocksList.get(i).getLocation();
//            BlockPos oldPos = new BlockPos(oldLocation.getBlockX(), oldLocation.getBlockY(), oldLocation.getBlockZ());
//            Block block = blocksList.get(i);
//            ImmutableBlockState blockState = manager.getImmutableBlockState(BlockStateUtils.blockDataToId(block.getBlockData()));
//            if (blockState != null && blockState.pushReaction() == PushReaction.DESTROY) {
//                // break actions
//                ContextHolder.Builder builder = ContextHolder.builder();
//                Vec3d vec3d = Vec3d.atCenterOf(oldPos);
//                builder.withParameter(LootParameters.LOCATION, vec3d);
//                builder.withParameter(LootParameters.WORLD, world);
//                for (Item<Object> item : blockState.getDrops(builder, world)) {
//                    world.dropItemNaturally(vec3d, item);
//                }
//                world.playBlockSound(vec3d, blockState.sounds().breakSound());
//            }
//        }
//    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (VersionHelper.isOrAbove1_21()) {
            if (!ExplosionUtils.isDroppingItems(event)) return;
        }
        handleExplodeEvent(event.blockList(), new BukkitWorld(event.getEntity().getWorld()), event.getYield());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (VersionHelper.isOrAbove1_21()) {
            if (!ExplosionUtils.isDroppingItems(event)) return;
        }
        handleExplodeEvent(event.blockList(), new BukkitWorld(event.getBlock().getWorld()), event.getYield());
    }

    private void handleExplodeEvent(List<org.bukkit.block.Block> blocks, net.momirealms.craftengine.core.world.World world, float yield) {
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block block = blocks.get(i);
            Location location = block.getLocation();
            BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            ImmutableBlockState state = manager.getImmutableBlockState(BlockStateUtils.blockDataToId(block.getBlockData()));
            if (state != null && !state.isEmpty()) {
                ContextHolder.Builder builder = ContextHolder.builder();
                Vec3d vec3d = Vec3d.atCenterOf(blockPos);
                builder.withParameter(CommonParameters.LOCATION, vec3d);
                builder.withParameter(CommonParameters.WORLD, world);
                if (yield < 1f) {
                    builder.withParameter(CommonParameters.EXPLOSION_RADIUS, 1.0f / yield);
                }
                for (Item<Object> item : state.getDrops(builder, world, null)) {
                    world.dropItemNaturally(vec3d, item);
                }
                world.playBlockSound(vec3d, state.sounds().breakSound());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!this.enableNoteBlockCheck) return;
        // for vanilla blocks
        if (event.getChangedType() == Material.NOTE_BLOCK) {
            Block block = event.getBlock();
            World world = block.getWorld();
            Location location = block.getLocation();
            Block sourceBlock = event.getSourceBlock();
            BlockFace direction = sourceBlock.getFace(block);
            if (direction == BlockFace.UP || direction == BlockFace.DOWN) {
                Object serverLevel = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(world);
                Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
                Object blockPos = LocationUtils.toBlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                FastNMS.INSTANCE.method$ServerChunkCache$blockChanged(chunkSource, blockPos);
                if (direction == BlockFace.UP) {
                    NoteBlockChainUpdateUtils.noteBlockChainUpdate(serverLevel, chunkSource, Reflections.instance$Direction$UP, blockPos, 0);
                } else {
                    NoteBlockChainUpdateUtils.noteBlockChainUpdate(serverLevel, chunkSource, Reflections.instance$Direction$DOWN, blockPos, 0);
                }
            }
        }
    }
}
