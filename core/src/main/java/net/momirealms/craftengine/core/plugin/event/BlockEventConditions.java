package net.momirealms.craftengine.core.plugin.event;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.PlayerBlockActionContext;
import net.momirealms.craftengine.core.plugin.context.condition.InvertedCondition;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Factory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class BlockEventConditions {

    public static Condition<PlayerBlockActionContext> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.block.event.condition.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        if (key.value().charAt(0) == '!') {
            Factory<Condition<PlayerBlockActionContext>> factory = BuiltInRegistries.PLAYER_BLOCK_CONDITION_FACTORY.getValue(new Key(key.namespace(), key.value().substring(1)));
            if (factory == null) {
                throw new LocalizedResourceConfigException("warning.config.block.event.condition.invalid_type", type);
            }
            return new InvertedCondition<>(factory.create(map));
        } else {
            Factory<Condition<PlayerBlockActionContext>> factory = BuiltInRegistries.PLAYER_BLOCK_CONDITION_FACTORY.getValue(key);
            if (factory == null) {
                throw new LocalizedResourceConfigException("warning.config.block.event.condition.invalid_type", type);
            }
            return factory.create(map);
        }
    }
}
