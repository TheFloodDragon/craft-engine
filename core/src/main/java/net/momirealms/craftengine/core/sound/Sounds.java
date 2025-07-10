package net.momirealms.craftengine.core.sound;

import net.momirealms.craftengine.core.block.BlockKeys;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public final class Sounds {
    private Sounds() {}

    public static final List<Key> WOODEN_TRAPDOORS = List.of(BlockKeys.OAK_TRAPDOOR, BlockKeys.SPRUCE_TRAPDOOR, BlockKeys.BIRCH_TRAPDOOR,
            BlockKeys.ACACIA_TRAPDOOR, BlockKeys.PALE_OAK_TRAPDOOR, BlockKeys.DARK_OAK_TRAPDOOR, BlockKeys.MANGROVE_TRAPDOOR, BlockKeys.JUNGLE_TRAPDOOR);
    public static final List<Key> CHERRY_TRAPDOORS = List.of(BlockKeys.CHERRY_TRAPDOOR);
    public static final List<Key> BAMBOO_TRAPDOORS = List.of(BlockKeys.BAMBOO_TRAPDOOR);
    public static final List<Key> NETHER_TRAPDOORS = List.of(BlockKeys.WARPED_TRAPDOOR, BlockKeys.CRIMSON_TRAPDOOR);
    public static final List<Key> COPPER_TRAPDOORS = List.of(BlockKeys.COPPER_TRAPDOOR, BlockKeys.EXPOSED_COPPER_TRAPDOOR, BlockKeys.WEATHERED_COPPER_TRAPDOOR, BlockKeys.OXIDIZED_COPPER_DOOR,
            BlockKeys.WAXED_COPPER_TRAPDOOR, BlockKeys.WAXED_EXPOSED_COPPER_TRAPDOOR, BlockKeys.WAXED_WEATHERED_COPPER_TRAPDOOR, BlockKeys.WAXED_OXIDIZED_COPPER_TRAPDOOR);

    public static final List<Key> WOODEN_DOORS = List.of(BlockKeys.OAK_DOOR, BlockKeys.SPRUCE_DOOR, BlockKeys.BIRCH_DOOR,
            BlockKeys.ACACIA_DOOR, BlockKeys.PALE_OAK_DOOR, BlockKeys.DARK_OAK_DOOR, BlockKeys.MANGROVE_DOOR, BlockKeys.JUNGLE_DOOR);
    public static final List<Key> CHERRY_DOORS = List.of(BlockKeys.CHERRY_DOOR);
    public static final List<Key> BAMBOO_DOORS = List.of(BlockKeys.BAMBOO_DOOR);
    public static final List<Key> NETHER_DOORS = List.of(BlockKeys.WARPED_DOOR, BlockKeys.CRIMSON_DOOR);
    public static final List<Key> COPPER_DOORS = List.of(BlockKeys.COPPER_DOOR, BlockKeys.EXPOSED_COPPER_DOOR, BlockKeys.WEATHERED_COPPER_DOOR, BlockKeys.OXIDIZED_COPPER_DOOR,
            BlockKeys.WAXED_COPPER_DOOR, BlockKeys.WAXED_EXPOSED_COPPER_DOOR, BlockKeys.WAXED_WEATHERED_COPPER_DOOR, BlockKeys.WAXED_OXIDIZED_COPPER_DOOR);

    public static final List<Key> WOODEN_FENCE_GATES = List.of(BlockKeys.OAK_FENCE_GATE, BlockKeys.SPRUCE_FENCE_GATE, BlockKeys.BIRCH_FENCE_GATE,
            BlockKeys.ACACIA_FENCE_GATE, BlockKeys.PALE_OAK_FENCE_GATE, BlockKeys.DARK_OAK_FENCE_GATE, BlockKeys.MANGROVE_FENCE_GATE, BlockKeys.JUNGLE_FENCE_GATE);
    public static final List<Key> CHERRY_FENCE_GATES = List.of(BlockKeys.CHERRY_FENCE_GATE);
    public static final List<Key> BAMBOO_FENCE_GATES = List.of(BlockKeys.BAMBOO_FENCE_GATE);
    public static final List<Key> NETHER_FENCE_GATES = List.of(BlockKeys.WARPED_FENCE_GATE, BlockKeys.CRIMSON_FENCE_GATE);

    public static final Key WOODEN_TRAPDOOR_OPEN = Key.of("block.wooden_trapdoor.open");
    public static final Key WOODEN_TRAPDOOR_CLOSE = Key.of("block.wooden_trapdoor.close");
    public static final Key WOODEN_DOOR_OPEN = Key.of("block.wooden_door.open");
    public static final Key WOODEN_DOOR_CLOSE = Key.of("block.wooden_door.close");
    public static final Key WOODEN_FENCE_GATE_OPEN = Key.of("block.fence_gate.open");
    public static final Key WOODEN_FENCE_GATE_CLOSE = Key.of("block.fence_gate.close");
    public static final Key NETHER_WOOD_TRAPDOOR_OPEN = Key.of("block.nether_wood_trapdoor.open");
    public static final Key NETHER_WOOD_TRAPDOOR_CLOSE = Key.of("block.nether_wood_trapdoor.close");
    public static final Key NETHER_WOOD_DOOR_OPEN = Key.of("block.nether_wood_door.open");
    public static final Key NETHER_WOOD_DOOR_CLOSE = Key.of("block.nether_wood_door.close");
    public static final Key NETHER_WOOD_FENCE_GATE_OPEN = Key.of("block.nether_wood_fence_gate.open");
    public static final Key NETHER_WOOD_FENCE_GATE_CLOSE = Key.of("block.nether_wood_fence_gate.close");
    public static final Key BAMBOO_WOOD_TRAPDOOR_OPEN = Key.of("block.bamboo_wood_trapdoor.open");
    public static final Key BAMBOO_WOOD_TRAPDOOR_CLOSE = Key.of("block.bamboo_wood_trapdoor.close");
    public static final Key BAMBOO_WOOD_DOOR_OPEN = Key.of("block.bamboo_wood_door.open");
    public static final Key BAMBOO_WOOD_DOOR_CLOSE = Key.of("block.bamboo_wood_door.close");
    public static final Key BAMBOO_WOOD_FENCE_GATE_OPEN = Key.of("block.bamboo_wood_fence_gate.open");
    public static final Key BAMBOO_WOOD_FENCE_GATE_CLOSE = Key.of("block.bamboo_wood_fence_gate.close");
    public static final Key CHERRY_WOOD_TRAPDOOR_OPEN = Key.of("block.cherry_wood_trapdoor.open");
    public static final Key CHERRY_WOOD_TRAPDOOR_CLOSE = Key.of("block.cherry_wood_trapdoor.close");
    public static final Key CHERRY_WOOD_DOOR_OPEN = Key.of("block.cherry_wood_door.open");
    public static final Key CHERRY_WOOD_DOOR_CLOSE = Key.of("block.cherry_wood_door.close");
    public static final Key CHERRY_WOOD_FENCE_GATE_OPEN = Key.of("block.cherry_wood_fence_gate.open");
    public static final Key CHERRY_WOOD_FENCE_GATE_CLOSE = Key.of("block.cherry_wood_fence_gate.close");
    public static final Key COPPER_TRAPDOOR_OPEN = Key.of("block.copper_trapdoor.open");
    public static final Key COPPER_TRAPDOOR_CLOSE = Key.of("block.copper_trapdoor.close");
    public static final Key COPPER_DOOR_OPEN = Key.of("block.copper_door.open");
    public static final Key COPPER_DOOR_CLOSE = Key.of("block.copper_door.close");
}
