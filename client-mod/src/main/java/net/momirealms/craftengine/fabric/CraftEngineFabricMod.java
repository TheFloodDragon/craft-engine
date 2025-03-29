package net.momirealms.craftengine.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.momirealms.craftengine.fabric.client.config.ModConfig;
import net.momirealms.craftengine.fabric.util.BlockUtils;
import net.momirealms.craftengine.fabric.util.LoggerFilter;
import net.momirealms.craftengine.fabric.util.RegisterBlocks;
import net.momirealms.craftengine.fabric.util.YamlUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CraftEngineFabricMod implements ModInitializer {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("craft-engine-fabric-mod/config.yml");
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitialize() {
        loadConfig();
        LoggerFilter.filter();
        try {
            YamlUtils.saveDefaultResource("additional-real-blocks.yml");
            YamlUtils.saveDefaultResource("mappings.yml");
            Map<Identifier, Integer> map = YamlUtils.loadMappingsAndAdditionalBlocks();
            for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
                Identifier replacedBlockId = entry.getKey();
                for (int i = 0; i < entry.getValue(); i++) {
                    BlockState blockState = YamlUtils.createBlockData("minecraft:" + replacedBlockId.getPath());
                    RegisterBlocks.register(
                            replacedBlockId.getPath() + "_" + i,
                            BlockUtils.canPassThrough(blockState),
                            BlockUtils.getShape(blockState),
                            BlockUtils.isTransparent(blockState),
                            BlockUtils.canPush(blockState)
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveConfig));
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            ModConfig.enableNetwork = true;
            return;
        }
        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            Yaml yaml = new Yaml();
            var config = yaml.loadAs(inputStream, java.util.Map.class);
            ModConfig.enableNetwork = (Boolean) config.getOrDefault("enable-network", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        var data = new java.util.HashMap<String, Object>();
        data.put("enable-network", ModConfig.enableNetwork);
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
