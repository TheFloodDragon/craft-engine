package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityUtils {

    private EntityUtils() {}

    public static BlockPos getOnPos(Player player) {
        try {
            Object serverPlayer = FastNMS.INSTANCE.method$CraftPlayer$getHandle(player);
            Object blockPos = Reflections.method$Entity$getOnPos.invoke(serverPlayer, 1.0E-5F);
            return LocationUtils.fromBlockPos(blockPos);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public static Entity spawnEntity(World world, Location loc, EntityType type, org.bukkit.util.Consumer<Entity> function) {
        try {
            return (Entity) Reflections.method$World$spawnEntity.invoke(world, loc, type, CreatureSpawnEvent.SpawnReason.CUSTOM, function);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to spawn entity", e);
        }
    }
}
