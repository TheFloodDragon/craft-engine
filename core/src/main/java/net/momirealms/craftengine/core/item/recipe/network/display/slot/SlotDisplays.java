package net.momirealms.craftengine.core.item.recipe.network.display.slot;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public final class SlotDisplays {
    public static final Key EMPTY = Key.of("empty");
    public static final Key ANY_FUEL = Key.of("any_fuel");
    public static final Key ITEM = Key.of("item");
    public static final Key ITEM_STACK = Key.of("item_stack");
    public static final Key TAG = Key.of("tag");
    public static final Key SMITHING_TRIM = Key.of("smithing_trim");
    public static final Key WITH_REMAINDER = Key.of("with_remainder");
    public static final Key COMPOSITE = Key.of("composite");

    public static void register() {
        register(EMPTY, new SlotDisplay.Type(EmptySlotDisplay::read));
        register(ANY_FUEL, new SlotDisplay.Type(AnyFuelDisplay::read));
        register(ITEM, new SlotDisplay.Type(ItemSlotDisplay::read));
        register(ITEM_STACK, new SlotDisplay.Type(ItemStackSlotDisplay::read));
        register(TAG, new SlotDisplay.Type(TagSlotDisplay::read));
        register(SMITHING_TRIM, new SlotDisplay.Type(SmithingTrimDemoSlotDisplay::read));
        register(WITH_REMAINDER, new SlotDisplay.Type(WithRemainderSlotDisplay::read));
        register(COMPOSITE, new SlotDisplay.Type(CompositeSlotDisplay::read));
    }

    public static void register(Key key, SlotDisplay.Type type) {
        Holder.Reference<SlotDisplay.Type> holder = ((WritableRegistry<SlotDisplay.Type>) BuiltInRegistries.SLOT_DISPLAY_TYPE)
                .registerForHolder(new ResourceKey<>(Registries.SLOT_DISPLAY_TYPE.location(), key));
        holder.bindValue(type);
    }
}
