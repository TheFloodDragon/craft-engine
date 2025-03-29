package net.momirealms.craftengine.core.plugin.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.pack.conflict.resolution.ConditionalResolution;
import net.momirealms.craftengine.core.pack.host.HostMode;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.PluginProperties;
import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.logger.filter.DisconnectLogFilter;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager implements Reloadable {
    private static ConfigManager instance;
    protected final CraftEngine plugin;
    private final Path configFilePath;
    private final String configVersion;
    private YamlDocument config;

    protected boolean debug;
    protected boolean checkUpdate;
    protected boolean metrics;
    protected boolean filterConfigurationPhaseDisconnect;

    protected boolean resource_pack$generate_mod_assets;
    protected boolean resource_pack$override_uniform_font;
    protected List<ConditionalResolution> resource_pack$duplicated_files_handler;
    protected List<String> resource_pack$merge_external_folders;

    protected boolean resource_pack$protection$crash_tools$method_1;
    protected boolean resource_pack$protection$crash_tools$method_2;
    protected boolean resource_pack$protection$crash_tools$method_3;

    protected boolean resource_pack$protection$obfuscation$enable;
    protected long resource_pack$protection$obfuscation$seed;
    protected boolean resource_pack$protection$obfuscation$fake_directory;
    protected boolean resource_pack$protection$obfuscation$escape_unicode;
    protected boolean resource_pack$protection$obfuscation$break_json;
    protected boolean resource_pack$protection$obfuscation$resource_location$enable;
    protected int resource_pack$protection$obfuscation$resource_location$random_namespace$length;
    protected int resource_pack$protection$obfuscation$resource_location$random_namespace$amount;
    protected String resource_pack$protection$obfuscation$resource_location$random_path$source;
    protected int resource_pack$protection$obfuscation$resource_location$random_path$depth;
    protected boolean resource_pack$protection$obfuscation$resource_location$random_path$anti_unzip;
    protected int resource_pack$protection$obfuscation$resource_location$random_atlas$amount;
    protected boolean resource_pack$protection$obfuscation$resource_location$random_atlas$use_double;
    protected List<String> resource_pack$protection$obfuscation$resource_location$bypass_textures;
    protected List<String> resource_pack$protection$obfuscation$resource_location$bypass_models;
    protected List<String> resource_pack$protection$obfuscation$resource_location$bypass_sounds;
    protected List<String> resource_pack$protection$obfuscation$resource_location$bypass_equipments;

    protected float resource_pack$supported_version$min;
    protected float resource_pack$supported_version$max;

    protected HostMode resource_pack$send$mode;
    protected boolean resource_pack$send$kick_if_declined;
    protected boolean resource_pack$send$send_on_join;
    protected boolean resource_pack$send$send_on_reload;
    protected Component resource_pack$send$prompt;

    protected int resource_pack$send$self_host$port;
    protected String resource_pack$send$self_host$ip;
    protected String resource_pack$send$self_host$protocol;
    protected boolean resource_pack$send$self_host$deny_non_minecraft_request;
    protected int resource_pack$send$self_host$rate_limit$max_requests;
    protected long resource_pack$send$self_host$rate_limit$reset_interval;
    protected String resource_pack$self_host$local_file_path;

    protected String resource_pack$external_host$url;
    protected String resource_pack$external_host$sha1;
    protected UUID resource_pack$external_host$uuid;

    protected int performance$max_block_chain_update_limit;

    protected boolean light_system$force_update_light;
    protected boolean light_system$enable;

    protected boolean chunk_system$restore_vanilla_blocks_on_chunk_unload;
    protected boolean chunk_system$restore_custom_blocks_on_chunk_load;
    protected boolean chunk_system$sync_custom_blocks_on_chunk_load;

    protected boolean furniture$remove_invalid_furniture_on_chunk_load$enable;
    protected Set<String> furniture$remove_invalid_furniture_on_chunk_load$list;
    protected boolean furniture$hide_base_entity;

    protected boolean block$sound_system$enable;
    protected boolean recipe$enable;

    protected boolean item$non_italic_tag;

    protected boolean image$illegal_characters_filter$command;
    protected boolean image$illegal_characters_filter$chat;
    protected boolean image$illegal_characters_filter$anvil;
    protected boolean image$illegal_characters_filter$sign;
    protected boolean image$illegal_characters_filter$book;

    public ConfigManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.configVersion = PluginProperties.getValue("config");
        this.configFilePath = this.plugin.dataFolderPath().resolve("config.yml");
        instance = this;
    }

    @Override
    public void load() {
        if (Files.exists(this.configFilePath)) {
            this.config = this.loadYamlData(this.configFilePath.toFile());
            String configVersion = config.getString("config-version");
            if (configVersion.equals(this.configVersion)) {
                loadSettings();
                return;
            }
        }
        this.updateConfigVersion();
        loadSettings();
    }

    private void updateConfigVersion() {
        this.config = this.loadYamlConfig(
                "config.yml",
                GeneralSettings.builder()
                        .setRouteSeparator('.')
                        .setUseDefaults(false)
                        .build(),
                LoaderSettings
                        .builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.builder()
                        .setEscapeUnprintable(false)
                        .setScalarFormatter((tag, value, role, def) -> {
                            if (role == NodeRole.KEY) {
                                return ScalarStyle.PLAIN;
                            } else {
                                return tag == Tag.STR ? ScalarStyle.DOUBLE_QUOTED : ScalarStyle.PLAIN;
                            }
                        })
                        .build(),
                UpdaterSettings
                        .builder()
                        .setVersioning(new BasicVersioning("config-version"))
                        .build()
        );
        try {
            config.save(new File(plugin.dataFolderFile(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unload() {
        Reloadable.super.unload();
    }

    private void loadSettings() {
        YamlDocument config = settings();
        plugin.translationManager().forcedLocale(TranslationManager.parseLocale(config.getString("forced-locale", "")));

        // basics
        debug = config.getBoolean("debug", false);
        metrics = config.getBoolean("metrics", false);
        checkUpdate = config.getBoolean("update-checker", false);
        filterConfigurationPhaseDisconnect = config.getBoolean("filter-configuration-phase-disconnect", false);
        DisconnectLogFilter.instance().setEnable(filterConfigurationPhaseDisconnect);

        // resource pack
        resource_pack$override_uniform_font = config.getBoolean("resource-pack.override-uniform-font", false);
        resource_pack$generate_mod_assets = config.getBoolean("resource-pack.generate-mod-assets", false);
        resource_pack$supported_version$min = getVersion(config.get("resource-pack.supported-version.min", "1.20").toString());
        resource_pack$supported_version$max = getVersion(config.get("resource-pack.supported-version.max", "LATEST").toString());
        resource_pack$merge_external_folders = config.getStringList("resource-pack.merge-external-folders");
        resource_pack$send$mode = HostMode.valueOf(config.getString("resource-pack.send.mode", "self-host").replace("-", "_").toUpperCase(Locale.ENGLISH));
        resource_pack$send$self_host$port = config.getInt("resource-pack.send.self-host.port", 8163);
        resource_pack$send$self_host$ip = config.getString("resource-pack.send.self-host.ip", "localhost");
        resource_pack$self_host$local_file_path = config.getString("resource-pack.send.self-host.local-file-path", "./generated/resource_pack.zip");
        resource_pack$send$self_host$protocol = config.getString("resource-pack.send.self-host.protocol", "http");
        resource_pack$send$send_on_join = config.getBoolean("resource-pack.send.send-on-join", true);
        resource_pack$send$send_on_reload = config.getBoolean("resource-pack.send.send-on-reload", true);
        resource_pack$send$kick_if_declined = config.getBoolean("resource-pack.send.kick-if-declined", true);
        resource_pack$external_host$url = config.getString("resource-pack.send.external-host.url", "");
        resource_pack$external_host$sha1 = config.getString("resource-pack.send.external-host.sha1", "");
        String packUUIDStr = config.getString("resource-pack.send.external-host.uuid", "");
        resource_pack$external_host$uuid = packUUIDStr.isEmpty() ? UUID.nameUUIDFromBytes(resource_pack$external_host$url.getBytes(StandardCharsets.UTF_8)) : UUID.fromString(packUUIDStr);
        resource_pack$send$prompt = AdventureHelper.miniMessage(config.getString("resource-pack.send.prompt", "<yellow>To fully experience our server, please accept our custom resource pack.</yellow>"));
        resource_pack$send$self_host$rate_limit$reset_interval = config.getLong("resource-pack.send.self-host.rate-limit.reset-interval", 30L);
        resource_pack$send$self_host$rate_limit$max_requests = config.getInt("resource-pack.send.self-host.rate-limit.max-requests", 3);
        resource_pack$send$self_host$deny_non_minecraft_request = config.getBoolean("resource-pack.send.deny-non-minecraft-request", true);

        resource_pack$protection$crash_tools$method_1 = config.getBoolean("resource-pack.protection.crash-tools.method-1", false);
        resource_pack$protection$crash_tools$method_2 = config.getBoolean("resource-pack.protection.crash-tools.method-2", false);
        resource_pack$protection$crash_tools$method_3 = config.getBoolean("resource-pack.protection.crash-tools.method-3", false);
        resource_pack$protection$obfuscation$enable = config.getBoolean("resource-pack.protection.obfuscation.enable", false);
        resource_pack$protection$obfuscation$seed = config.getLong("resource-pack.protection.obfuscation.seed", 0L);
        resource_pack$protection$obfuscation$fake_directory = config.getBoolean("resource-pack.protection.obfuscation.fake-directory", false);
        resource_pack$protection$obfuscation$escape_unicode = config.getBoolean("resource-pack.protection.obfuscation.escape-unicode", false);
        resource_pack$protection$obfuscation$break_json = config.getBoolean("resource-pack.protection.obfuscation.break-json", false);

        resource_pack$protection$obfuscation$resource_location$enable = config.getBoolean("resource-pack.protection.obfuscation.resource-location.enable", false);
        resource_pack$protection$obfuscation$resource_location$random_namespace$amount = config.getInt("resource-pack.protection.obfuscation.resource-location.random-namespace.amount", 32);
        resource_pack$protection$obfuscation$resource_location$random_namespace$length = config.getInt("resource-pack.protection.obfuscation.resource-location.random-namespace.length", 8);
        resource_pack$protection$obfuscation$resource_location$random_path$depth = config.getInt("resource-pack.protection.obfuscation.resource-location.random-path.depth", 16);
        resource_pack$protection$obfuscation$resource_location$random_path$source = config.getString("resource-pack.protection.obfuscation.resource-location.random-path.source", "obf");
        resource_pack$protection$obfuscation$resource_location$random_path$anti_unzip = config.getBoolean("resource-pack.protection.obfuscation.resource-location.random-path.anti-unzip", false);
        resource_pack$protection$obfuscation$resource_location$random_atlas$amount = config.getInt("resource-pack.protection.obfuscation.resource-location.random-atlas.amount", 5);
        resource_pack$protection$obfuscation$resource_location$random_atlas$use_double = config.getBoolean("resource-pack.protection.obfuscation.resource-location.random-atlas.use-double", true);
        resource_pack$protection$obfuscation$resource_location$bypass_textures = config.getStringList("resource-pack.protection.obfuscation.resource-location.bypass-textures");
        resource_pack$protection$obfuscation$resource_location$bypass_models = config.getStringList("resource-pack.protection.obfuscation.resource-location.bypass-models");
        resource_pack$protection$obfuscation$resource_location$bypass_sounds = config.getStringList("resource-pack.protection.obfuscation.resource-location.bypass-sounds");
        resource_pack$protection$obfuscation$resource_location$bypass_equipments = config.getStringList("resource-pack.protection.obfuscation.resource-location.bypass-equipments");

        try {
            resource_pack$duplicated_files_handler = config.getMapList("resource-pack.duplicated-files-handler").stream().map(it -> {
                Map<String, Object> args = MiscUtils.castToMap(it, false);
                return ConditionalResolution.FACTORY.create(args);
            }).toList();
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to load resource pack duplicated files handler", e);
            resource_pack$duplicated_files_handler = List.of();
        }

        // item
        item$non_italic_tag = config.getBoolean("item.non-italic-tag", false);

        // performance
        performance$max_block_chain_update_limit = config.getInt("performance.max-block-chain-update-limit", 64);

        // light
        light_system$force_update_light = config.getBoolean("light-system.force-update-light", false);
        light_system$enable = config.getBoolean("light-system.enable", true);

        // chunk
        chunk_system$restore_vanilla_blocks_on_chunk_unload = config.getBoolean("chunk-system.restore-vanilla-blocks-on-chunk-unload", true);
        chunk_system$restore_custom_blocks_on_chunk_load = config.getBoolean("chunk-system.restore-custom-blocks-on-chunk-load", true);
        chunk_system$sync_custom_blocks_on_chunk_load = config.getBoolean("chunk-system.sync-custom-blocks-on-chunk-load", false);

        // furniture
        furniture$remove_invalid_furniture_on_chunk_load$enable = config.getBoolean("furniture.remove-invalid-furniture-on-chunk-load.enable", false);
        furniture$remove_invalid_furniture_on_chunk_load$list = new HashSet<>(config.getStringList("furniture.remove-invalid-furniture-on-chunk-load.list"));
        furniture$hide_base_entity = config.getBoolean("furniture.hide-base-entity", true);

        // block
        block$sound_system$enable = config.getBoolean("block.sound-system.enable", true);

        // recipe
        recipe$enable = config.getBoolean("recipe.enable", true);

        // image
        image$illegal_characters_filter$anvil = config.getBoolean("image.illegal-characters-filter.anvil", true);
        image$illegal_characters_filter$book = config.getBoolean("image.illegal-characters-filter.book", true);
        image$illegal_characters_filter$chat = config.getBoolean("image.illegal-characters-filter.chat", true);
        image$illegal_characters_filter$command = config.getBoolean("image.illegal-characters-filter.command", true);
        image$illegal_characters_filter$sign = config.getBoolean("image.illegal-characters-filter.sign", true);

        Class<?> modClazz = ReflectionUtils.getClazz(CraftEngine.MOD_CLASS);
        if (modClazz != null) {
            Method setMaxChainMethod = ReflectionUtils.getStaticMethod(modClazz, void.class, new String[] {"setMaxChainUpdate"}, int.class);
            try {
                assert setMaxChainMethod != null;
                setMaxChainMethod.invoke(null, performance$max_block_chain_update_limit);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.logger().warn("Failed to set max chain update", e);
            }
        }
    }

    private static float getVersion(String version) {
        if (version.equalsIgnoreCase("LATEST")) {
            version = PluginProperties.getValue("latest-version");
        }
        String[] split = version.split("\\.", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        return Float.parseFloat(split[1]);
    }

    public static String configVersion() {
        return instance.configVersion;
    }

    public static boolean debug() {
        return instance.debug;
    }

    public static boolean checkUpdate() {
        return instance.checkUpdate;
    }

    public static boolean metrics() {
        return instance.metrics;
    }

    public static boolean filterConfigurationPhaseDisconnect() {
        return instance.filterConfigurationPhaseDisconnect;
    }

    public static boolean resourcePack$overrideUniform() {
        return instance.resource_pack$override_uniform_font;
    }

    public static int maxChainUpdate() {
        return instance.performance$max_block_chain_update_limit;
    }

    public static boolean removeInvalidFurniture() {
        return instance.furniture$remove_invalid_furniture_on_chunk_load$enable;
    }

    public static Set<String> furnitureToRemove() {
        return instance.furniture$remove_invalid_furniture_on_chunk_load$list;
    }

    public static boolean forceUpdateLight() {
        return instance.light_system$force_update_light;
    }

    public static boolean enableLightSystem() {
        return instance.light_system$enable;
    }

    public static float packMinVersion() {
        return instance.resource_pack$supported_version$min;
    }

    public static float packMaxVersion() {
        return instance.resource_pack$supported_version$max;
    }

    public static boolean enableSoundSystem() {
        return instance.block$sound_system$enable;
    }

    public static boolean enableRecipeSystem() {
        return instance.recipe$enable;
    }

    public static boolean nonItalic() {
        return instance.item$non_italic_tag;
    }

    public static boolean restoreVanillaBlocks() {
        return instance.chunk_system$restore_vanilla_blocks_on_chunk_unload && instance.chunk_system$restore_custom_blocks_on_chunk_load;
    }

    public static boolean denyNonMinecraftRequest() {
        return instance.resource_pack$send$self_host$deny_non_minecraft_request;
    }

    public static boolean restoreCustomBlocks() {
        return instance.chunk_system$restore_custom_blocks_on_chunk_load;
    }

    public static boolean syncCustomBlocks() {
        return instance.chunk_system$sync_custom_blocks_on_chunk_load;
    }

    public static List<String> foldersToMerge() {
        return instance.resource_pack$merge_external_folders;
    }

    public static HostMode hostMode() {
        return instance.resource_pack$send$mode;
    }

    public static String hostIP() {
        return instance.resource_pack$send$self_host$ip;
    }

    public static int hostPort() {
        return instance.resource_pack$send$self_host$port;
    }

    public static boolean kickOnDeclined() {
        return instance.resource_pack$send$kick_if_declined;
    }

    public static Component resourcePackPrompt() {
        return instance.resource_pack$send$prompt;
    }

    public static String hostProtocol() {
        return instance.resource_pack$send$self_host$protocol;
    }

    public static String externalPackUrl() {
        return instance.resource_pack$external_host$url;
    }

    public static String externalPackSha1() {
        return instance.resource_pack$external_host$sha1;
    }

    public static UUID externalPackUUID() {
        return instance.resource_pack$external_host$uuid;
    }

    public static boolean sendPackOnJoin() {
        return instance.resource_pack$send$send_on_join;
    }

    public static boolean sendPackOnReload() {
        return instance.resource_pack$send$send_on_reload;
    }

    public static int requestRate() {
        return instance.resource_pack$send$self_host$rate_limit$max_requests;
    }

    public static long requestInterval() {
        return instance.resource_pack$send$self_host$rate_limit$reset_interval;
    }

    public static String hostResourcePackPath() {
        return instance.resource_pack$self_host$local_file_path;
    }

    public static List<ConditionalResolution> resolutions() {
        return instance.resource_pack$duplicated_files_handler;
    }

    public static boolean crashTool1() {
        return instance.resource_pack$protection$crash_tools$method_1;
    }

    public static boolean crashTool2() {
        return instance.resource_pack$protection$crash_tools$method_2;
    }

    public static boolean crashTool3() {
        return instance.resource_pack$protection$crash_tools$method_3;
    }

    public static boolean crashTool4() {
        return false;
    }

    public static boolean enableObfuscation() {
        return instance.resource_pack$protection$obfuscation$enable;
    }

    public static long obfuscationSeed() {
        return instance.resource_pack$protection$obfuscation$seed;
    }

    public static boolean createFakeDirectory() {
        return instance.resource_pack$protection$obfuscation$fake_directory;
    }

    public static boolean escapeUnicode() {
        return instance.resource_pack$protection$obfuscation$escape_unicode;
    }

    public static boolean breakJson() {
        return instance.resource_pack$protection$obfuscation$break_json;
    }

    public static boolean enableRandomResourceLocation() {
        return instance.resource_pack$protection$obfuscation$resource_location$enable;
    }

    public static int namespaceLength() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_namespace$length;
    }

    public static int namespaceAmount() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_namespace$amount;
    }

    public static String atlasSource() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_path$source;
    }

    public static int pathDepth() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_path$depth;
    }

    public static boolean antiUnzip() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_path$anti_unzip;
    }

    public static int atlasAmount() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_atlas$amount;
    }

    public static boolean useDouble() {
        return instance.resource_pack$protection$obfuscation$resource_location$random_atlas$use_double;
    }

    public static List<String> bypassTextures() {
        return instance.resource_pack$protection$obfuscation$resource_location$bypass_textures;
    }

    public static List<String> bypassModels() {
        return instance.resource_pack$protection$obfuscation$resource_location$bypass_models;
    }

    public static List<String> bypassSounds() {
        return instance.resource_pack$protection$obfuscation$resource_location$bypass_sounds;
    }

    public static List<String> bypassEquipments() {
        return instance.resource_pack$protection$obfuscation$resource_location$bypass_equipments;
    }

    public static boolean generateModAssets() {
        return instance.resource_pack$generate_mod_assets;
    }

    public static boolean filterChat() {
        return instance().image$illegal_characters_filter$chat;
    }

    public static boolean filterAnvil() {
        return instance().image$illegal_characters_filter$anvil;
    }

    public static boolean filterCommand() {
        return instance().image$illegal_characters_filter$command;
    }

    public static boolean filterBook() {
        return instance().image$illegal_characters_filter$book;
    }

    public static boolean filterSign() {
        return instance().image$illegal_characters_filter$sign;
    }

    public static boolean hideBaseEntity() {
        return instance().furniture$hide_base_entity;
    }

    public YamlDocument loadOrCreateYamlData(String fileName) {
        File file = new File(this.plugin.dataFolderFile(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName);
        }
        return this.loadYamlData(file);
    }

    public YamlDocument loadYamlConfig(String filePath, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) {
        try (InputStream inputStream = new FileInputStream(resolveConfig(filePath).toFile())) {
            return YamlDocument.create(inputStream, this.plugin.resourceStream(filePath), generalSettings, loaderSettings, dumperSettings, updaterSettings);
        } catch (IOException e) {
            this.plugin.logger().severe("Failed to load config " + filePath, e);
            return null;
        }
    }

    public YamlDocument loadYamlData(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return YamlDocument.create(inputStream);
        } catch (IOException e) {
            this.plugin.logger().severe("Failed to load config " + file, e);
            return null;
        }
    }

    public Path resolveConfig(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
        filePath = filePath.replace('\\', '/');
        Path configFile = this.plugin.dataFolderPath().resolve(filePath);
        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException ignored) {
            }
            try (InputStream is = this.plugin.resourceStream(filePath)) {
                if (is == null) {
                    throw new IllegalArgumentException("The embedded resource '" + filePath + "' cannot be found");
                }
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return configFile;
    }

    public YamlDocument settings() {
        if (config == null) {
            throw new IllegalStateException("Main config not loaded");
        }
        return config;
    }

    public static ConfigManager instance() {
        return instance;
    }
}
