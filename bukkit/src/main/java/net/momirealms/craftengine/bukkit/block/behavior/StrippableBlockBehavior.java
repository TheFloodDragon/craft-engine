package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class StrippableBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Key stripped;

    public StrippableBlockBehavior(CustomBlock block, Key stripped) {
        super(block);
        this.stripped = stripped;
    }

    public Key stripped() {
        return this.stripped;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            String stripped = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("stripped"), "warning.config.block.behavior.strippable.missing_stripped");
            return new StrippableBlockBehavior(block, Key.of(stripped));
        }
    }
}
