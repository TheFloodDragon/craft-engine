package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.shared.block.EmptyBlockBehavior;

public class BukkitBlockBehaviors extends BlockBehaviors {
    public static final Key BUSH_BLOCK = Key.from("craftengine:bush_block");
    public static final Key HANGING_BLOCK = Key.from("craftengine:hanging_block");
    public static final Key FALLING_BLOCK = Key.from("craftengine:falling_block");
    public static final Key LEAVES_BLOCK = Key.from("craftengine:leaves_block");
    public static final Key STRIPPABLE_BLOCK = Key.from("craftengine:strippable_block");
    public static final Key SAPLING_BLOCK = Key.from("craftengine:sapling_block");
    public static final Key ON_LIQUID_BLOCK = Key.from("craftengine:on_liquid_block");
    public static final Key WATERLOGGED_BLOCK = Key.from("craftengine:waterlogged_block");
    public static final Key CONCRETE_POWDER_BLOCK = Key.from("craftengine:concrete_powder_block");
    public static final Key SUGARCANE_BLOCK = Key.from("craftengine:sugar_cane_block");
    public static final Key CROP_BLOCK = Key.from("craftengine:crop_block");

    public static void init() {
        register(EMPTY, (block, args) -> EmptyBlockBehavior.INSTANCE);
        register(FALLING_BLOCK, FallingBlockBehavior.FACTORY);
        register(BUSH_BLOCK, BushBlockBehavior.FACTORY);
        register(HANGING_BLOCK, HangingBlockBehavior.FACTORY);
        register(LEAVES_BLOCK, LeavesBlockBehavior.FACTORY);
        register(STRIPPABLE_BLOCK, StrippableBlockBehavior.FACTORY);
        register(SAPLING_BLOCK, SaplingBlockBehavior.FACTORY);
        register(ON_LIQUID_BLOCK, OnLiquidBlockBehavior.FACTORY);
        register(WATERLOGGED_BLOCK, WaterLoggedBlockBehavior.FACTORY);
        register(CONCRETE_POWDER_BLOCK, ConcretePowderBlockBehavior.FACTORY);
        register(SUGARCANE_BLOCK, SugarCaneBlockBehavior.FACTORY);
        register(CROP_BLOCK, CropBlockBehavior.FACTORY);
    }
}
