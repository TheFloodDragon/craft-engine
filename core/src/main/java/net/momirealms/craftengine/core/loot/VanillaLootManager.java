package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;

import java.util.Optional;

public interface VanillaLootManager extends ConfigSectionParser, Reloadable {
    String CONFIG_SECTION_NAME = "vanilla-loots";

    @Override
    default int loadingSequence() {
        return LoadingSequence.VANILLA_LOOTS;
    }

    @Override
    default String sectionId() {
        return CONFIG_SECTION_NAME;
    }

    Optional<VanillaLoot> getBlockLoot(int blockState);
}
