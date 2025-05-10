package net.momirealms.craftengine.bukkit.entity;

import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;

import java.lang.ref.WeakReference;

public class BukkitEntity extends Entity {
    private final WeakReference<org.bukkit.entity.Entity> entity;

    public BukkitEntity(org.bukkit.entity.Entity entity) {
        this.entity = new WeakReference<>(entity);
    }

    @Override
    public double x() {
        return literalObject().getLocation().getX();
    }

    @Override
    public double y() {
        return literalObject().getLocation().getY();
    }

    @Override
    public double z() {
        return literalObject().getLocation().getZ();
    }

    @Override
    public void tick() {
    }

    @Override
    public int entityID() {
        return literalObject().getEntityId();
    }

    @Override
    public float getXRot() {
        return literalObject().getLocation().getYaw();
    }

    @Override
    public float getYRot() {
        return literalObject().getLocation().getPitch();
    }

    @Override
    public World world() {
        return new BukkitWorld(literalObject().getWorld());
    }

    @Override
    public Direction getDirection() {
        return Direction.NORTH;
    }

    @Override
    public org.bukkit.entity.Entity literalObject() {
        return this.entity.get();
    }

    @Override
    public Key type() {
        return KeyUtils.namespacedKey2Key(literalObject().getType().getKey());
    }
}
