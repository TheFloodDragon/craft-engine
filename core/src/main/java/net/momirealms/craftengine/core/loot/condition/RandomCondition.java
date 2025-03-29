package net.momirealms.craftengine.core.loot.condition;

import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.RandomUtils;

import java.util.Map;

public class RandomCondition implements LootCondition {
    public static final Factory FACTORY = new Factory();
    private final float chance;

    public RandomCondition(float chance) {
        this.chance = chance;
    }

    @Override
    public Key type() {
        return LootConditions.RANDOM;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return RandomUtils.generateRandomFloat(0, 1) < this.chance;
    }

    public static class Factory implements LootConditionFactory {
        @Override
        public LootCondition create(Map<String, Object> arguments) {
            float chance = MiscUtils.getAsFloat(arguments.getOrDefault("value", 0.5f));
            return new RandomCondition(chance);
        }
    }
}
