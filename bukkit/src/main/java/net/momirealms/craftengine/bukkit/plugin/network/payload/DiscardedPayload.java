package net.momirealms.craftengine.bukkit.plugin.network.payload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record DiscardedPayload(Key channel, Object rawPayload) implements Payload {
    public static final boolean useNewMethod = Reflections.method$DiscardedPayload$data == null;

    public static DiscardedPayload from(Object payload) {
        try {
            Object type = Reflections.method$CustomPacketPayload$type.invoke(payload);
            Object id = Reflections.method$CustomPacketPayload$Type$id.invoke(type);
            Key channel = Key.of(id.toString());
            return new DiscardedPayload(channel, payload);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to create DiscardedPayload", e);
            return null;
        }
    }

    public byte[] getData() {
        try {
            if (useNewMethod) {
                return (byte[]) Reflections.method$DiscardedPayload$dataByteArray.invoke(this.rawPayload());
            } else {
                ByteBuf buf = (ByteBuf) Reflections.method$DiscardedPayload$data.invoke(this.rawPayload());
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                return data;
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to get data from DiscardedPayload", e);
            return new byte[0];
        }
    }

    public FriendlyByteBuf toBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.getData()));
    }
}
