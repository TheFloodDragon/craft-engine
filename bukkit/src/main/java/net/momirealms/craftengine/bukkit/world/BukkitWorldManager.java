package net.momirealms.craftengine.bukkit.world;

import net.momirealms.craftengine.bukkit.compatibility.slimeworld.SlimeFormatStorageAdaptor;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.injector.BukkitInjector;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.WorldManager;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.serialization.ChunkSerializer;
import net.momirealms.craftengine.core.world.chunk.storage.DefaultStorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BukkitWorldManager implements WorldManager, Listener {
    private static BukkitWorldManager instance;
    private final BukkitCraftEngine plugin;
    private final Map<UUID, CEWorld> worlds;
    private CEWorld[] worldArray = new CEWorld[0];
    private final ReentrantReadWriteLock worldMapLock = new ReentrantReadWriteLock();
    private SchedulerTask tickTask;
    // cache
    private UUID lastVisitedUUID;
    private CEWorld lastVisitedWorld;
    private StorageAdaptor storageAdaptor;

    public BukkitWorldManager(BukkitCraftEngine plugin) {
        instance = this;
        this.plugin = plugin;
        this.worlds = new HashMap<>();
        if (VersionHelper.isVersionNewerThan1_21_4()) {
            try {
                Class.forName("com.infernalsuite.asp.api.AdvancedSlimePaperAPI");
                SlimeFormatStorageAdaptor adaptor = new SlimeFormatStorageAdaptor(this);
                this.storageAdaptor = adaptor;
                Bukkit.getPluginManager().registerEvents(adaptor, plugin.bootstrap());
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }
        this.storageAdaptor = new DefaultStorageAdaptor();
    }

    @Override
    public void setStorageAdaptor(@NotNull StorageAdaptor storageAdaptor) {
        this.storageAdaptor = storageAdaptor;
    }

    public static BukkitWorldManager instance() {
        return instance;
    }

    public CEWorld getWorld(World world) {
        return getWorld(world.getUID());
    }

    @Override
    public CEWorld getWorld(UUID uuid) {
        if (uuid.equals(this.lastVisitedUUID)) {
            return this.lastVisitedWorld;
        }
        this.worldMapLock.readLock().lock();
        try {
            CEWorld world = worlds.get(uuid);
            if (world != null) {
                this.lastVisitedUUID = uuid;
                this.lastVisitedWorld = world;
            }
            return world;
        } finally {
            this.worldMapLock.readLock().unlock();
        }
    }

    private void resetWorldArray() {
        this.worldArray = this.worlds.values().toArray(new CEWorld[0]);
    }

    public void delayedInit() {
        // events and tasks
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
        this.tickTask = plugin.scheduler().sync().runRepeating(() -> {
            for (CEWorld world : worldArray) {
                world.tick();
            }
        }, 1, 1);

        // load loaded chunks
        this.worldMapLock.writeLock().lock();
        try {
            for (World world : Bukkit.getWorlds()) {
                CEWorld ceWorld = new BukkitCEWorld(new BukkitWorld(world), this.storageAdaptor);
                this.worlds.put(world.getUID(), ceWorld);
                this.resetWorldArray();
                for (Chunk chunk : world.getLoadedChunks()) {
                    handleChunkLoad(ceWorld, chunk);
                }
            }
        } finally {
            this.worldMapLock.writeLock().unlock();
        }
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.storageAdaptor instanceof Listener listener) {
            HandlerList.unregisterAll(listener);
        }
        if (this.tickTask != null && !this.tickTask.cancelled()) {
            this.tickTask.cancel();
        }

        for (World world : Bukkit.getWorlds()) {
            CEWorld ceWorld = getWorld(world.getUID());
            for (Chunk chunk : world.getLoadedChunks()) {
                handleChunkUnload(ceWorld, chunk);
            }
        }
        this.worlds.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldLoadEvent event) {
        this.loadWorld(new BukkitWorld(event.getWorld()));
    }

    @Override
    public void loadWorld(net.momirealms.craftengine.core.world.World world) {
        this.worldMapLock.writeLock().lock();
        try {
            if (this.worlds.containsKey(world.uuid())) return;
            CEWorld ceWorld = new BukkitCEWorld(world, this.storageAdaptor);
            this.worlds.put(world.uuid(), ceWorld);
            this.resetWorldArray();
            for (Chunk chunk : ((World) world.platformWorld()).getLoadedChunks()) {
                handleChunkLoad(ceWorld, chunk);
            }
        } finally {
            this.worldMapLock.writeLock().unlock();
        }
    }

    @Override
    public void loadWorld(CEWorld world) {
        this.worldMapLock.writeLock().lock();
        try {
            if (this.worlds.containsKey(world.world().uuid())) return;
            this.worlds.put(world.world().uuid(), world);
            this.resetWorldArray();
            for (Chunk chunk : ((World) world.world().platformWorld()).getLoadedChunks()) {
                handleChunkLoad(world, chunk);
            }
        } finally {
            this.worldMapLock.writeLock().unlock();
        }
    }

    @Override
    public CEWorld createWorld(net.momirealms.craftengine.core.world.World world, WorldDataStorage storage) {
        return new BukkitCEWorld(world, storage);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldUnloadEvent event) {
        unloadWorld(new BukkitWorld(event.getWorld()));
    }

    @Override
    public void unloadWorld(net.momirealms.craftengine.core.world.World world) {
        CEWorld ceWorld;
        this.worldMapLock.writeLock().lock();
        try {
            ceWorld = this.worlds.remove(world.uuid());
            if (ceWorld == null) {
                return;
            }
            if (ceWorld == this.lastVisitedWorld) {
                this.lastVisitedWorld = null;
                this.lastVisitedUUID = null;
            }
            this.resetWorldArray();
        } finally {
            this.worldMapLock.writeLock().unlock();
        }
        for (Chunk chunk : ((World) world.platformWorld()).getLoadedChunks()) {
            handleChunkUnload(ceWorld, chunk);
        }
    }

    @Override
    public <T> net.momirealms.craftengine.core.world.World wrap(T world) {
        if (world instanceof World w) {
            return new BukkitWorld(w);
        } else {
            throw new IllegalArgumentException(world.getClass() + " is not a Bukkit World");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        this.worldMapLock.readLock().lock();
        CEWorld world;
        try {
            world = worlds.get(event.getWorld().getUID());
            if (world == null) {
                return;
            }
        } finally {
            this.worldMapLock.readLock().unlock();
        }
        handleChunkLoad(world, event.getChunk());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        CEWorld world;
        this.worldMapLock.readLock().lock();
        try {
            world = worlds.get(event.getWorld().getUID());
            if (world == null) {
                return;
            }
        } finally {
            this.worldMapLock.readLock().unlock();
        }
        handleChunkUnload(world, event.getChunk());
    }

    private void handleChunkUnload(CEWorld world, Chunk chunk) {
        ChunkPos pos = new ChunkPos(chunk.getX(), chunk.getZ());
        CEChunk ceChunk = world.getChunkAtIfLoaded(chunk.getX(), chunk.getZ());
        if (ceChunk != null) {
            try {
                world.worldDataStorage().writeChunkTagAt(pos, ChunkSerializer.serialize(ceChunk));
            } catch (IOException e) {
                plugin.logger().warn("Failed to write chunk tag at " + chunk.getX() + " " + chunk.getZ(), e);
                return;
            } finally {
                if (ConfigManager.restoreVanillaBlocks()) {
                    CESection[] ceSections = ceChunk.sections();
                    Object worldServer = FastNMS.INSTANCE.field$CraftChunk$worldServer(chunk);
                    Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(worldServer);
                    Object levelChunk = FastNMS.INSTANCE.method$ServerChunkCache$getChunkAtIfLoadedMainThread(chunkSource, chunk.getX(), chunk.getZ());
                    Object[] sections = FastNMS.INSTANCE.method$ChunkAccess$getSections(levelChunk);
                    for (int i = 0; i < ceSections.length; i++) {
                        CESection ceSection = ceSections[i];
                        Object section = sections[i];
                        BukkitInjector.uninjectLevelChunkSection(section);
                        if (ceSection.statesContainer().isEmpty()) continue;
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = 0; y < 16; y++) {
                                    ImmutableBlockState customState = ceSection.getBlockState(x, y, z);
                                    if (!customState.isEmpty() && customState.vanillaBlockState() != null) {
                                        FastNMS.INSTANCE.method$LevelChunkSection$setBlockState(section, x, y, z, customState.vanillaBlockState().handle(), false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ceChunk.unload();
        }
    }

    private void handleChunkLoad(CEWorld ceWorld, Chunk chunk) {
        ChunkPos pos = new ChunkPos(chunk.getX(), chunk.getZ());
        if (ceWorld.isChunkLoaded(pos.longKey)) return;
        CEChunk ceChunk;
        try {
            CompoundTag chunkNbt = ceWorld.worldDataStorage().readChunkTagAt(pos);
            if (chunkNbt != null) {
                ceChunk = ChunkSerializer.deserialize(ceWorld, pos, chunkNbt);
            } else {
                ceChunk = new CEChunk(ceWorld, pos);
            }
            try {
                CESection[] ceSections = ceChunk.sections();
                Object worldServer = FastNMS.INSTANCE.field$CraftChunk$worldServer(chunk);
                Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(worldServer);
                Object levelChunk = FastNMS.INSTANCE.method$ServerChunkCache$getChunkAtIfLoadedMainThread(chunkSource, chunk.getX(), chunk.getZ());
                Object[] sections = FastNMS.INSTANCE.method$ChunkAccess$getSections(levelChunk);
                for (int i = 0; i < ceSections.length; i++) {
                    CESection ceSection = ceSections[i];
                    Object section = sections[i];
                    if (ConfigManager.syncCustomBlocks()) {
                        Object statesContainer = FastNMS.INSTANCE.field$LevelChunkSection$states(section);
                        Object data = Reflections.varHandle$PalettedContainer$data.get(statesContainer);
                        Object palette = Reflections.field$PalettedContainer$Data$palette.get(data);
                        boolean requiresSync = false;
                        if (Reflections.clazz$SingleValuePalette.isInstance(palette)) {
                            Object onlyBlockState = Reflections.field$SingleValuePalette$value.get(palette);
                            if (!BlockStateUtils.isVanillaBlock(BlockStateUtils.blockStateToId(onlyBlockState))) {
                                requiresSync = true;
                            }
                        } else if (Reflections.clazz$LinearPalette.isInstance(palette)) {
                            Object[] blockStates = (Object[]) Reflections.field$LinearPalette$values.get(palette);
                            for (Object blockState : blockStates) {
                                if (blockState != null) {
                                    if (!BlockStateUtils.isVanillaBlock(BlockStateUtils.blockStateToId(blockState))) {
                                        requiresSync = true;
                                        break;
                                    }
                                }
                            }
                        } else if (Reflections.clazz$HashMapPalette.isInstance(palette)) {
                            Object biMap = Reflections.field$HashMapPalette$values.get(palette);
                            Object[] blockStates = (Object[]) Reflections.field$CrudeIncrementalIntIdentityHashBiMap$keys.get(biMap);
                            for (Object blockState : blockStates) {
                                if (blockState != null) {
                                    if (!BlockStateUtils.isVanillaBlock(BlockStateUtils.blockStateToId(blockState))) {
                                        requiresSync = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            requiresSync = true;
                        }
                        if (requiresSync) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int y = 0; y < 16; y++) {
                                        Object mcState = FastNMS.INSTANCE.method$LevelChunkSection$getBlockState(section, x, y, z);
                                        int stateId = BlockStateUtils.blockStateToId(mcState);
                                        ImmutableBlockState customState = this.plugin.blockManager().getImmutableBlockState(stateId);
                                        if (customState != null) {
                                            ceSection.setBlockState(x, y, z, customState);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (ConfigManager.restoreCustomBlocks()) {
                        if (!ceSection.statesContainer().isEmpty()) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int y = 0; y < 16; y++) {
                                        ImmutableBlockState customState = ceSection.getBlockState(x, y, z);
                                        if (!customState.isEmpty() && customState.customBlockState() != null) {
                                            FastNMS.INSTANCE.method$LevelChunkSection$setBlockState(section, x, y, z, customState.customBlockState().handle(), false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    BukkitInjector.injectLevelChunkSection(section, ceSection, ceWorld, new SectionPos(pos.x, ceChunk.sectionY(i), pos.z));
                }
                if (ConfigManager.enableRecipeSystem()) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> blockEntities = (Map<Object, Object>) FastNMS.INSTANCE.field$ChunkAccess$blockEntities(levelChunk);
                    for (Object blockEntity : blockEntities.values()) {
                        BukkitInjector.injectCookingBlockEntity(blockEntity);
                    }
                }
            } catch (ReflectiveOperationException e) {
                this.plugin.logger().warn("Failed to restore chunk at " + chunk.getX() + " " + chunk.getZ(), e);
                return;
            }
        } catch (IOException e) {
            this.plugin.logger().warn("Failed to read chunk tag at " + chunk.getX() + " " + chunk.getZ(), e);
            return;
        }
        ceChunk.load();
    }


}
