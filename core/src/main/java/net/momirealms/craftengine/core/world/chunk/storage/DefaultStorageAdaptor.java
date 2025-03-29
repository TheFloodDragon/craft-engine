package net.momirealms.craftengine.core.world.chunk.storage;

import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.World;
import org.jetbrains.annotations.NotNull;

public class DefaultStorageAdaptor implements StorageAdaptor {

    @Override
    public @NotNull WorldDataStorage adapt(@NotNull World world) {
        return new DefaultRegionFileStorage(world.directory().resolve(CEWorld.REGION_DIRECTORY));
    }
}
