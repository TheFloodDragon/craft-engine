package net.momirealms.craftengine.core.util;

import java.util.Map;

public interface Factory<T> {

    T create(Map<String, Object> args);
}
