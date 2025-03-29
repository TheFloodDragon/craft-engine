package net.momirealms.craftengine.bukkit.loot;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.loot.VanillaLoot;
import net.momirealms.craftengine.core.loot.VanillaLootManager;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.PreConditions;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.nio.file.Path;
import java.util.*;

// note: block listeners are in BlockEventListener to reduce performance cost
public class BukkitVanillaLootManager implements VanillaLootManager, Listener {
    private final BukkitCraftEngine plugin;
    private final Map<Integer, VanillaLoot> blockLoots;
    private final Map<Key, VanillaLoot> entityLoots;

    public BukkitVanillaLootManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        this.blockLoots = new HashMap<>();
        this.entityLoots = new HashMap<>();
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void unload() {
        this.blockLoots.clear();
        this.entityLoots.clear();
    }

    @Override
    public Optional<VanillaLoot> getBlockLoot(int vanillaBlockState) {
        return Optional.ofNullable(this.blockLoots.get(vanillaBlockState));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Key key = KeyUtils.namespacedKey2Key(entity.getType().getKey());
        Optional.ofNullable(this.entityLoots.get(key)).ifPresent(loot -> {
            if (loot.override()) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
            Location location = entity.getLocation();
            net.momirealms.craftengine.core.world.World world = new BukkitWorld(entity.getWorld());
            Vec3d vec3d = new Vec3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
            ContextHolder.Builder builder = ContextHolder.builder();
            builder.withParameter(LootParameters.WORLD, world);
            builder.withParameter(LootParameters.LOCATION, vec3d);
            if (VersionHelper.isVersionNewerThan1_20_5()) {
                if (event.getDamageSource().getCausingEntity() instanceof Player player) {
                    BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
                    builder.withParameter(LootParameters.PLAYER, serverPlayer);
                    builder.withOptionalParameter(LootParameters.TOOL, serverPlayer.getItemInHand(InteractionHand.MAIN_HAND));
                }
            }
            ContextHolder contextHolder = builder.build();
            for (LootTable<?> lootTable : loot.lootTables()) {
                for (Item<?> item : lootTable.getRandomItems(contextHolder, world)) {
                    world.dropItemNaturally(vec3d, item);
                }
            }
        });
    }

    @Override
    public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
        String type = (String) section.get("type");
        if (PreConditions.isNull(type, () -> this.plugin.logger().warn(path, "`type` option is required for vanilla-loot " + id))) {
            return;
        }
        VanillaLoot.Type typeEnum = VanillaLoot.Type.valueOf(type.toUpperCase(Locale.ENGLISH));
        boolean override = (boolean) section.getOrDefault("override", false);
        List<String> targets = MiscUtils.getAsStringList(section.getOrDefault("target", List.of()));
        LootTable<?> lootTable = LootTable.fromMap(MiscUtils.castToMap(section.get("loot"), false));
        switch (typeEnum) {
            case BLOCK -> {
                for (String target : targets) {
                    if (target.endsWith("]") && target.contains("[")) {
                        java.lang.Object blockState = BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData(target));
                        if (blockState == Reflections.instance$Blocks$AIR$defaultState) {
                            this.plugin.logger().warn(path, "Failed to load " + id + ". Invalid target " + target);
                            return;
                        }
                        VanillaLoot vanillaLoot = this.blockLoots.computeIfAbsent(BlockStateUtils.blockStateToId(blockState), k -> new VanillaLoot(VanillaLoot.Type.BLOCK));
                        vanillaLoot.addLootTable(lootTable);
                    } else {
                        for (Object blockState : BlockStateUtils.getAllVanillaBlockStates(Key.of(target))) {
                            if (blockState == Reflections.instance$Blocks$AIR$defaultState) {
                                this.plugin.logger().warn(path, "Failed to load " + id + ". Invalid target " + target);
                                return;
                            }
                            VanillaLoot vanillaLoot = this.blockLoots.computeIfAbsent(BlockStateUtils.blockStateToId(blockState), k -> new VanillaLoot(VanillaLoot.Type.BLOCK));
                            if (override) vanillaLoot.override(true);
                            vanillaLoot.addLootTable(lootTable);
                        }
                    }
                }
            }
            case ENTITY -> {
                for (String target : targets) {
                    Key key = Key.of(target);
                    VanillaLoot vanillaLoot = this.entityLoots.computeIfAbsent(key, k -> new VanillaLoot(VanillaLoot.Type.ENTITY));
                    vanillaLoot.addLootTable(lootTable);
                    if (override) vanillaLoot.override(true);
                }
            }
        }
    }
}
