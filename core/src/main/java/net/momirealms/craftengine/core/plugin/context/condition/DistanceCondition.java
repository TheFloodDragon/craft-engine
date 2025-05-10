package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

import java.util.Map;
import java.util.Optional;

// TODO It's designed for players for the moment, better using entities
public class DistanceCondition<CTX extends Context> implements Condition<CTX> {
    private final NumberProvider min;
    private final NumberProvider max;

    public DistanceCondition(NumberProvider min, NumberProvider max) {
        this.max = max;
        this.min = min;
    }

    @Override
    public Key type() {
        return CommonConditions.DISTANCE;
    }

    @Override
    public boolean test(CTX ctx) {
        float min = this.min.getFloat(ctx);
        float max = this.max.getFloat(ctx);
        Optional<Player> optionalPlayer = ctx.getOptionalParameter(CommonParameters.PLAYER);
        World world = ctx.getParameterOrThrow(CommonParameters.WORLD);
        Vec3d location = ctx.getParameterOrThrow(CommonParameters.LOCATION);
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();
        if (!player.world().uuid().equals(world.uuid())) {
            return false;
        }

        double dx = location.x() - player.x();
        double dy = location.y() - player.y();
        double dz = location.z() - player.z();
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double minSquared = min * min;
        double maxSquared = max * max;

        return distanceSquared >= minSquared && distanceSquared <= maxSquared;
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            NumberProvider min = NumberProviders.fromObject(arguments.getOrDefault("min", 0));
            NumberProvider max = NumberProviders.fromObject(arguments.getOrDefault("max", 32));
            return new DistanceCondition<>(min, max);
        }
    }
}
