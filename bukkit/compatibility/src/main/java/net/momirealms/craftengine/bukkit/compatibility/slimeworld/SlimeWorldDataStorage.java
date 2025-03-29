package net.momirealms.craftengine.bukkit.compatibility.slimeworld;

import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

public class SlimeWorldDataStorage implements WorldDataStorage {
    private final WeakReference<SlimeWorld> slimeWorld;
    private final SlimeFormatStorageAdaptor adaptor;

    public SlimeWorldDataStorage(SlimeWorld slimeWorld, SlimeFormatStorageAdaptor adaptor) {
        this.slimeWorld = new WeakReference<>(slimeWorld);
        this.adaptor = adaptor;
    }

    public SlimeWorld getWorld() {
        return slimeWorld.get();
    }

    @Nullable
    @Override
    public CompoundTag readChunkTagAt(ChunkPos pos) {
        SlimeChunk slimeChunk = getWorld().getChunk(pos.x, pos.z);
        if (slimeChunk == null) return null;
        Object tag = slimeChunk.getExtraData().get("craftengine");
        if (tag == null) return null;
        try {
            return NBT.readCompound(new DataInputStream(new ByteArrayInputStream(adaptor.byteArrayTagToBytes(tag))));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read chunk tag from slime world. " + pos, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeChunkTagAt(ChunkPos pos, @Nullable CompoundTag nbt) {
        SlimeChunk slimeChunk = getWorld().getChunk(pos.x, pos.z);
        if (slimeChunk == null) return;
        if (nbt == null) {
            slimeChunk.getExtraData().remove("craftengine");
        } else {
            try {
                Object tag = adaptor.bytesToByteArrayTag(NBT.toBytes(nbt));
                Map<String, ?> data1 = slimeChunk.getExtraData();
                Map<String, Object> data2 = (Map<String, Object>) data1;
                data2.put("craftengine", tag);
            } catch (Exception e) {
                throw new RuntimeException("Failed to write chunk tag to slime world. " + pos, e);
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
    }
}
