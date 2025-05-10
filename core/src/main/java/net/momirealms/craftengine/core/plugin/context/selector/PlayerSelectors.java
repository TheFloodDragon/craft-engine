package net.momirealms.craftengine.core.plugin.context.selector;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

public class PlayerSelectors {
    public static final Key ALL = Key.of("craftengine:all");
    public static final Key SELF = Key.of("craftengine:self");

    public static <CTX extends Context> PlayerSelector<CTX> fromObject(Object object) {
        if (object == null) return null;
        if (object instanceof String string) {
            if (string.equals("self") || string.equals("@self") || string.equals("@s")) {
                return new SelfPlayerSelector<>();
            } else if (string.equals("all") || string.equals("@all") || string.equals("@a")) {
                return new AllPlayerSelector<>();
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
