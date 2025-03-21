package net.momirealms.craftengine.bukkit.loot;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.loot.VanillaLoot;
import net.momirealms.craftengine.core.loot.VanillaLootManager;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.PreConditions;
import org.bukkit.Bukkit;

import java.nio.file.Path;
import java.util.*;

public class BukkitVanillaLootManager implements VanillaLootManager {
    private final CraftEngine plugin;
    private final Map<Integer, VanillaLoot> blockLoots;

    public BukkitVanillaLootManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.blockLoots = new HashMap<>();
    }

    @Override
    public void unload() {
        this.blockLoots.clear();
    }

    @Override
    public Optional<VanillaLoot> getBlockLoot(int vanillaBlockState) {
        return Optional.ofNullable(this.blockLoots.get(vanillaBlockState));
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
                        for (Object blockState : BlockStateUtils.getAllBlockStates(Key.of(target))) {
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
        }
    }
}
