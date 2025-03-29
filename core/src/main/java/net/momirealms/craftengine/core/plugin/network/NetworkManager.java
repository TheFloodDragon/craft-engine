package net.momirealms.craftengine.core.plugin.network;

import io.netty.channel.Channel;
import net.momirealms.craftengine.core.entity.player.Player;

public interface NetworkManager {
    String MOD_CHANNEL = "craftengine:payload";

    void setUser(Channel channel, NetWorkUser user);

    NetWorkUser getUser(Channel channel);

    NetWorkUser removeUser(Channel channel);

    Channel getChannel(Player player);

    NetWorkUser[] onlineUsers();

    void init();

    void enable();

    void shutdown();
}
