package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

public interface Function<CTX extends Context> {

    void run(CTX ctx);

    Key type();
}
