package net.momirealms.craftengine.bukkit.block;

import com.sk89q.worldedit.bukkit.BukkitBlockRegistry;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BlockType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import org.bukkit.Material;

import java.lang.reflect.Field;

public class WorldEditHook {
    private static final Field field$BlockType$blockMaterial;

    static {
        field$BlockType$blockMaterial = ReflectionUtils.getDeclaredField(BlockType.class, "blockMaterial");
    }

    public static void register(Key id) throws ReflectiveOperationException {
        BlockType blockType = new BlockType(id.toString(), blockState -> blockState);
        field$BlockType$blockMaterial.set(blockType, LazyReference.from(() -> new BukkitBlockRegistry.BukkitBlockMaterial(null, Material.STONE)));
        BlockType.REGISTRY.register(id.toString(), blockType);
    }
}
