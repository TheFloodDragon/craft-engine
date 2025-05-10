package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public abstract class Entity {

    public abstract Key type();

    public abstract double x();

    public abstract double y();

    public abstract double z();

    public Vec3d position() {
        return new Vec3d(x(), y(), z());
    }

    public abstract void tick();

    public abstract int entityID();

    public abstract float getXRot();

    public abstract float getYRot();

    public abstract World world();

    public abstract Direction getDirection();

    public abstract Object literalObject();
}
