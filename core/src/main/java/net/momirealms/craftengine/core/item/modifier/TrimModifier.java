package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;

public class TrimModifier<I> implements ItemModifier<I> {
    private final String material;
    private final String pattern;

    public TrimModifier(String material, String pattern) {
        this.material = material;
        this.pattern = pattern;
    }

    @Override
    public String name() {
        return "trim";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.isVersionNewerThan1_20_5()) {
            item.setComponent(ComponentKeys.TRIM, Map.of(
                    "pattern", this.pattern,
                    "material", this.material
            ));
        } else {
            item.setTag(this.material, "Trim", "material");
            item.setTag(this.pattern, "Trim", "pattern");
        }
    }
}
