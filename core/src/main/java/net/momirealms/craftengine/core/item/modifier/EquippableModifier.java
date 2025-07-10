package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.setting.EquipmentData;

public class EquippableModifier<I> implements ItemDataModifier<I> {
    private final EquipmentData data;

    public EquippableModifier(EquipmentData data) {
        this.data = data;
    }

    @Override
    public String name() {
        return "equippable";
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.equippable(this.data);
        return item;
    }
}
