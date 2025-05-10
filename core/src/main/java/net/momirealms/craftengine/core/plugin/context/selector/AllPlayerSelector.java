package net.momirealms.craftengine.core.plugin.context.selector;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllPlayerSelector<CTX extends Context> implements PlayerSelector<CTX> {
    private final List<Condition<CTX>> predicates;

    public AllPlayerSelector(List<Condition<CTX>> predicates) {
        this.predicates = predicates;
    }

    public AllPlayerSelector() {
        this.predicates = List.of();
    }

    public List<Condition<CTX>> predicates() {
        return predicates;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Player> get(CTX context) {
        if (this.predicates.isEmpty()) {
            return Arrays.asList(CraftEngine.instance().networkManager().onlineUsers());
        } else {
            List<Player> players = new ArrayList<>();
            outer: for (Player player : CraftEngine.instance().networkManager().onlineUsers()) {
                PlayerOptionalContext newContext = PlayerOptionalContext.of(player, ContextHolder.builder()
                        .withOptionalParameter(CommonParameters.WORLD, context.getOptionalParameter(CommonParameters.WORLD).orElse(null))
                        .withOptionalParameter(CommonParameters.LOCATION, context.getOptionalParameter(CommonParameters.LOCATION).orElse(null))
                );
                for (Condition<CTX> predicate : this.predicates) {
                    if (!predicate.test((CTX) newContext)) {
                        continue outer;
                    }
                }
                players.add(player);
            }
            return players;
        }
    }

    @Override
    public Key type() {
        return PlayerSelectors.ALL;
    }
}
