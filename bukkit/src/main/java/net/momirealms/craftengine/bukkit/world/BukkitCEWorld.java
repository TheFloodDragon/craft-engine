package net.momirealms.craftengine.bukkit.world;

import net.momirealms.craftengine.bukkit.util.LightUtils;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.util.SectionPosUtils;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;

public class BukkitCEWorld extends CEWorld {

    public BukkitCEWorld(World world, StorageAdaptor adaptor) {
        super(world, adaptor);
    }

    public BukkitCEWorld(World world, WorldDataStorage dataStorage) {
        super(world, dataStorage);
    }

    @Override
    public void tick() {
        if (ConfigManager.enableLightSystem()) {
            LightUtils.updateChunkLight((org.bukkit.World) world.platformWorld(), SectionPosUtils.toMap(super.updatedSectionPositions, world.worldHeight().getMinSection() - 1, world.worldHeight().getMaxSection() + 1));
            super.updatedSectionPositions.clear();
        }
    }
}
