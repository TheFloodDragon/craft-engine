package net.momirealms.craftengine.core.plugin.network;

import io.netty.channel.Channel;
import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public interface NetWorkUser {
    boolean isOnline();

    Channel nettyChannel();

    Plugin plugin();

    String name();

    void sendPacket(Object packet, boolean immediately);

    void receivePacket(Object packet);

    @ApiStatus.Internal
    ConnectionState decoderState();

    @ApiStatus.Internal
    ConnectionState encoderState();

    int clientSideSectionCount();

    Key clientSideDimension();

    Object serverPlayer();

    Object platformPlayer();

    Map<Integer, List<Integer>> furnitureView();
}
