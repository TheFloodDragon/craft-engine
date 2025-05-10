package net.momirealms.craftengine.core.plugin.context.selector;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public class SelfPlayerSelector<CTX extends Context> implements PlayerSelector<CTX> {

    @Override
    public List<Player> get(CTX context) {
        return List.of(context.getParameterOrThrow(CommonParameters.PLAYER));
    }

    @Override
    public Key type() {
        return PlayerSelectors.SELF;
    }
}
