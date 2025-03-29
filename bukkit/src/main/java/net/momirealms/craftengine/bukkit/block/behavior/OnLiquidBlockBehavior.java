package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.shared.block.BlockBehavior;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnLiquidBlockBehavior extends BushBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final boolean onWater;
    private final boolean onLava;

    public OnLiquidBlockBehavior(boolean onWater, boolean onLava) {
        super(List.of(), Set.of(), Set.of());
        this.onWater = onWater;
        this.onLava = onLava;
    }

    public boolean onWater() {
        return this.onWater;
    }

    public boolean onLava() {
        return this.onLava;
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            List<String> liquidTypes = MiscUtils.getAsStringList(arguments.getOrDefault("liquid-type", List.of("water")));
            return new OnLiquidBlockBehavior(liquidTypes.contains("water"), liquidTypes.contains("lava"));
        }
    }

    @Override
    protected boolean mayPlaceOn(Object belowState, Object world, Object belowPos) throws ReflectiveOperationException {
        Object fluidState = Reflections.method$Level$getFluidState.invoke(world, belowPos);
        Object fluidStateAbove = Reflections.method$Level$getFluidState.invoke(world, LocationUtils.above(belowPos));
        if (Reflections.method$FluidState$getType.invoke(fluidStateAbove) != Reflections.instance$Fluids$EMPTY) {
            return false;
        }
        if (this.onWater && (Reflections.method$FluidState$getType.invoke(fluidState) == Reflections.instance$Fluids$WATER || Reflections.field$StateHolder$owner.get(belowState) == Reflections.instance$Blocks$ICE)) {
            return true;
        }
        if (this.onLava && Reflections.method$FluidState$getType.invoke(fluidState) == Reflections.instance$Fluids$LAVA) {
            return true;
        }
        return false;
    }
}
