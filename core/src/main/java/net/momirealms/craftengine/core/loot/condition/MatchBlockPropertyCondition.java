package net.momirealms.craftengine.core.loot.condition;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MatchBlockPropertyCondition implements LootCondition {
    public static final Factory FACTORY = new Factory();
    private final List<Pair<String, String>> properties;

    public MatchBlockPropertyCondition(List<Pair<String, String>> properties) {
        this.properties = properties;
    }

    @Override
    public Key type() {
        return LootConditions.MATCH_BLOCK_PROPERTY;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getOptionalParameter(LootParameters.BLOCK_STATE).map(state -> {
            CustomBlock block = state.owner().value();
            for (Pair<String, String> property : this.properties) {
                Property<?> propertyIns = block.getProperty(property.left());
                if (propertyIns == null) {
                    return false;
                }
                if (!state.get(propertyIns).toString().toLowerCase(Locale.ENGLISH).equals(property.right())) {
                    return false;
                }
            }
            return true;
        }).orElse(false);
    }

    public static class Factory implements LootConditionFactory {

        @SuppressWarnings("unchecked")
        @Override
        public LootCondition create(Map<String, Object> arguments) {
            Map<String, Object> properties = (Map<String, Object>) arguments.get("properties");
            if (properties == null) {
                throw new IllegalArgumentException("Missing 'properties' argument for 'match_block_property'");
            }
            List<Pair<String, String>> propertyList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                propertyList.add(new Pair<>(entry.getKey(), entry.getValue().toString()));
            }
            return new MatchBlockPropertyCondition(propertyList);
        }
    }
}
