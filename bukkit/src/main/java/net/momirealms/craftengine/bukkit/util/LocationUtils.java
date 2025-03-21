package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {

    private LocationUtils() {}

    public static Vec3d toVec3d(Location loc) {
        return new Vec3d(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Vec3d fromVec(Object vec) throws ReflectiveOperationException {
        return new Vec3d(
            Reflections.field$Vec3$x.getDouble(vec),
            Reflections.field$Vec3$y.getDouble(vec),
            Reflections.field$Vec3$z.getDouble(vec)
        );
    }

    public static Object toBlockPos(BlockPos pos) {
        try {
            return Reflections.constructor$BlockPos.newInstance(pos.x(), pos.y(), pos.z());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create BlockPos", e);
        }
    }

    public static Object above(Object blockPos) throws ReflectiveOperationException {
        return toBlockPos(
            Reflections.field$Vec3i$x.getInt(blockPos),
            Reflections.field$Vec3i$y.getInt(blockPos) + 1,
            Reflections.field$Vec3i$z.getInt(blockPos)
        );
    }

    public static Object toBlockPos(int x, int y, int z) {
        try {
            return Reflections.constructor$BlockPos.newInstance(x, y, z);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create BlockPos", e);
        }
    }


    public static BlockPos toBlockPos(Location pos) {
        return new BlockPos(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    public static BlockPos fromBlockPos(Object pos) throws ReflectiveOperationException {
        return new BlockPos(
                Reflections.field$Vec3i$x.getInt(pos),
                Reflections.field$Vec3i$y.getInt(pos),
                Reflections.field$Vec3i$z.getInt(pos)
        );
    }

    public static double getDistance(Location location1, Location location2) {
        return Math.sqrt(Math.pow(location2.getX() - location1.getX(), 2) +
                Math.pow(location2.getY() - location1.getY(), 2) +
                Math.pow(location2.getZ() - location1.getZ(), 2)
        );
    }

    @NotNull
    public static Location toBlockLocation(Location location) {
        Location blockLoc = location.clone();
        blockLoc.setX(location.getBlockX());
        blockLoc.setY(location.getBlockY());
        blockLoc.setZ(location.getBlockZ());
        return blockLoc;
    }

    @NotNull
    public static Location toBlockCenterLocation(Location location) {
        Location centerLoc = location.clone();
        centerLoc.setX(location.getBlockX() + 0.5);
        centerLoc.setY(location.getBlockY() + 0.5);
        centerLoc.setZ(location.getBlockZ() + 0.5);
        return centerLoc;
    }

    @NotNull
    public static Location toSurfaceCenterLocation(Location location) {
        Location centerLoc = location.clone();
        centerLoc.setX(location.getBlockX() + 0.5);
        centerLoc.setZ(location.getBlockZ() + 0.5);
        centerLoc.setY(location.getBlockY());
        return centerLoc;
    }
}
