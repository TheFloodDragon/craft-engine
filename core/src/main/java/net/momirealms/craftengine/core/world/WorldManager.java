package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface WorldManager extends Reloadable {

    void setStorageAdaptor(@NotNull StorageAdaptor storageAdaptor);

    CEWorld getWorld(UUID uuid);

    void delayedInit();

    void loadWorld(World world);

    void loadWorld(CEWorld world);

    CEWorld createWorld(World world, WorldDataStorage storage);

    void unloadWorld(World world);

    <T> World wrap(T world);
}
