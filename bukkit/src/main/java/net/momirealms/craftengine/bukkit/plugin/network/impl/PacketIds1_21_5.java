package net.momirealms.craftengine.bukkit.plugin.network.impl;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;

public class PacketIds1_21_5 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return 8;
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return 72;
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return 40;
    }

    @Override
    public int clientboundLevelEventPacket() {
        return 39;
    }

    @Override
    public int clientboundAddEntityPacket() {
        return 1;
    }
}
