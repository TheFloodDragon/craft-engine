package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.world.Vec3d;

import java.util.UUID;

public final class PlayerParameters {
    private PlayerParameters() {}

    public static final ContextKey<Double> X = ContextKey.of("player.x");
    public static final ContextKey<Double> Y = ContextKey.of("player.y");
    public static final ContextKey<Double> Z = ContextKey.of("player.z");
    public static final ContextKey<Vec3d> POS = ContextKey.of("player.pos");
    public static final ContextKey<Integer> BLOCK_X = ContextKey.of("player.block_x");
    public static final ContextKey<Integer> BLOCK_Y = ContextKey.of("player.block_y");
    public static final ContextKey<Integer> BLOCK_Z = ContextKey.of("player.block_z");
    public static final ContextKey<String> NAME = ContextKey.of("player.name");
    public static final ContextKey<String> WORLD_NAME = ContextKey.of("player.world.name");
    public static final ContextKey<UUID> UUID = ContextKey.of("player.uuid");
    public static final ContextKey<Item<?>> MAIN_HAND_ITEM = ContextKey.of("player.main_hand");
    public static final ContextKey<Item<?>> OFF_HAND_ITEM = ContextKey.of("player.off_hand");
}
