package net.momirealms.craftengine.core.pack.conflict.resolution;

import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.pack.conflict.matcher.PathMatchers;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.Map;

public record ResolutionConditional(Condition<PathContext> matcher, Resolution resolution) implements Resolution {
    public static final Factory FACTORY = new Factory();

    @Override
    public void run(PathContext existing, PathContext conflict) {
        if (this.matcher.test(existing)) {
            this.resolution.run(existing, conflict);
        }
    }

    @Override
    public Key type() {
        return Resolutions.CONDITIONAL;
    }

    public static class Factory implements ResolutionFactory {

        @Override
        public ResolutionConditional create(Map<String, Object> arguments) {
            Map<String, Object> term = MiscUtils.castToMap(arguments.get("term"), false);
            Map<String, Object> resolution = MiscUtils.castToMap(arguments.get("resolution"), false);
            return new ResolutionConditional(PathMatchers.fromMap(term), Resolutions.fromMap(resolution));
        }
    }
}
