package net.momirealms.craftengine.core.pack.conflict.resolution;

import java.util.Map;

@FunctionalInterface
public interface ResolutionFactory {

    Resolution create(Map<String, Object> arguments);
}
