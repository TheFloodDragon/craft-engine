package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.Map;

public interface ConditionFactory<CTX extends Context> {

    Condition<CTX> create(Map<String, Object> args);
}
