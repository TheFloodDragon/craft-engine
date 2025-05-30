package net.momirealms.craftengine.bukkit.plugin.network.id;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;
import net.momirealms.craftengine.bukkit.util.Reflections;

public class PacketIds1_20 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundBlockUpdatePacket);
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSectionBlocksUpdatePacket);
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundLevelParticlesPacket);
    }

    @Override
    public int clientboundLevelEventPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundLevelEventPacket);
    }

    @Override
    public int clientboundAddEntityPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundAddEntityPacket);
    }

    @Override
    public int clientboundOpenScreenPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundOpenScreenPacket);
    }

    @Override
    public int clientboundSoundPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSoundPacket);
    }

    @Override
    public int clientboundRemoveEntitiesPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundRemoveEntitiesPacket);
    }

    @Override
    public int clientboundSetEntityDataPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetEntityDataPacket);
    }

    @Override
    public int clientboundSetTitleTextPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetTitleTextPacket);
    }

    @Override
    public int clientboundSetSubtitleTextPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetSubtitleTextPacket);
    }

    @Override
    public int clientboundSetActionBarTextPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetActionBarTextPacket);
    }

    @Override
    public int clientboundBossEventPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundBossEventPacket);
    }

    @Override
    public int clientboundSystemChatPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSystemChatPacket);
    }

    @Override
    public int clientboundTabListPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundTabListPacket);
    }

    @Override
    public int clientboundSetPlayerTeamPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetPlayerTeamPacket);
    }

    @Override
    public int clientboundSetObjectivePacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetObjectivePacket);
    }

    @Override
    public int clientboundLevelChunkWithLightPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundLevelChunkWithLightPacket);
    }

    @Override
    public int clientboundPlayerInfoUpdatePacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundPlayerInfoUpdatePacket);
    }

    @Override
    public int clientboundSetScorePacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetScorePacket);
    }

    @Override
    public int clientboundContainerSetContentPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundContainerSetContentPacket);
    }

    @Override
    public int clientboundContainerSetSlotPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundContainerSetSlotPacket);
    }

    @Override
    public int clientboundSetCursorItemPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetCursorItemPacket);
    }

    @Override
    public int clientboundSetEquipmentPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetEquipmentPacket);
    }

    @Override
    public int clientboundSetPlayerInventoryPacket() {
        return PacketIdFinder.clientboundByClazz(Reflections.clazz$ClientboundSetPlayerInventoryPacket);
    }

    @Override
    public int serverboundContainerClickPacket() {
        return PacketIdFinder.serverboundByClazz(Reflections.clazz$ServerboundContainerClickPacket);
    }

    @Override
    public int serverboundSetCreativeModeSlotPacket() {
        return PacketIdFinder.serverboundByClazz(Reflections.clazz$ServerboundSetCreativeModeSlotPacket);
    }
}
