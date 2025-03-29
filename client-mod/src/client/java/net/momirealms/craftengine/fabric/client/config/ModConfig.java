package net.momirealms.craftengine.fabric.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModConfig {
    public static boolean enableNetwork = true;

    public static Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.craftengine.config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.craftengine.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.craftengine.enable_network")
                                .formatted(Formatting.WHITE),
                        enableNetwork)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> enableNetwork = newValue)
                .setTooltip(
                        Text.translatable("tooltip.craftengine.enable_network")
                                .formatted(Formatting.GRAY)
                )
                .build());

        return builder.build();
    }
}
