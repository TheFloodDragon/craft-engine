package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.modifier.*;
import net.momirealms.craftengine.core.pack.LegacyOverridesModel;
import net.momirealms.craftengine.core.pack.misc.EquipmentGeneration;
import net.momirealms.craftengine.core.pack.model.ItemModel;
import net.momirealms.craftengine.core.pack.model.generation.AbstractModelGenerator;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.TypeUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractItemManager<I> extends AbstractModelGenerator implements ItemManager<I> {
    protected static final Map<Key, List<ItemBehavior>> VANILLA_ITEM_EXTRA_BEHAVIORS = new HashMap<>();
    protected static final List<Key> VANILLA_ITEMS = new ArrayList<>();
    protected static final Map<Key, List<Holder<Key>>> VANILLA_ITEM_TAGS = new HashMap<>();

    protected final Map<String, Function<Object, ItemModifier<I>>> dataFunctions = new HashMap<>();
    protected final Map<Key, CustomItem<I>> customItems = new HashMap<>();
    protected final Map<Key, List<Holder<Key>>> customItemTags;
    protected final Map<Key, Map<Integer, Key>> cmdConflictChecker;
    protected final Map<Key, ItemModel> modernItemModels1_21_4;
    protected final Map<Key, List<LegacyOverridesModel>> modernItemModels1_21_2;
    protected final Map<Key, TreeSet<LegacyOverridesModel>> legacyOverrides;
    protected final Map<Key, TreeMap<Integer, ItemModel>> modernOverrides;
    protected final Set<EquipmentGeneration> equipmentsToGenerate;
    // Cached command suggestions
    protected final List<Suggestion> cachedSuggestions = new ArrayList<>();

    protected void registerDataFunction(Function<Object, ItemModifier<I>> function, String... alias) {
        for (String a : alias) {
            dataFunctions.put(a, function);
        }
    }

    protected static void registerVanillaItemExtraBehavior(ItemBehavior behavior, Key... items) {
        for (Key key : items) {
            VANILLA_ITEM_EXTRA_BEHAVIORS.computeIfAbsent(key, k -> new ArrayList<>()).add(behavior);
        }
    }

    @Override
    public void unload() {
        super.clearModelsToGenerate();
        this.customItems.clear();
        this.cachedSuggestions.clear();
        this.legacyOverrides.clear();
        this.modernOverrides.clear();
        this.customItemTags.clear();
        this.equipmentsToGenerate.clear();
        this.cmdConflictChecker.clear();
        this.modernItemModels1_21_4.clear();
        this.modernItemModels1_21_2.clear();
    }

    @Override
    public Optional<CustomItem<I>> getCustomItem(Key key) {
        return Optional.ofNullable(this.customItems.get(key));
    }

    public AbstractItemManager(CraftEngine plugin) {
        super(plugin);
        this.registerFunctions();
        this.legacyOverrides = new HashMap<>();
        this.modernOverrides = new HashMap<>();
        this.customItemTags = new HashMap<>();
        this.cmdConflictChecker = new HashMap<>();
        this.modernItemModels1_21_4 = new HashMap<>();
        this.modernItemModels1_21_2 = new HashMap<>();
        this.equipmentsToGenerate = new HashSet<>();
    }

    @Override
    public List<Holder<Key>> tagToItems(Key tag) {
        List<Holder<Key>> items = new ArrayList<>();
        List<Holder<Key>> holders = VANILLA_ITEM_TAGS.get(tag);
        if (holders != null) {
            items.addAll(holders);
        }
        List<Holder<Key>> customItems = customItemTags.get(tag);
        if (customItems != null) {
            items.addAll(customItems);
        }
        return items;
    }

    @Override
    public List<Holder<Key>> tagToVanillaItems(Key tag) {
        return Collections.unmodifiableList(VANILLA_ITEM_TAGS.getOrDefault(tag, List.of()));
    }

    @Override
    public List<Holder<Key>> tagToCustomItems(Key tag) {
        return Collections.unmodifiableList(this.customItemTags.getOrDefault(tag, List.of()));
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    @Override
    public Optional<List<ItemBehavior>> getItemBehavior(Key key) {
        Optional<CustomItem<I>> customItemOptional = getCustomItem(key);
        if (customItemOptional.isPresent()) {
            CustomItem<I> customItem = customItemOptional.get();
            Key vanillaMaterial = customItem.material();
            List<ItemBehavior> behavior = VANILLA_ITEM_EXTRA_BEHAVIORS.get(vanillaMaterial);
            if (behavior != null) {
                return Optional.of(Stream.concat(customItem.behaviors().stream(), behavior.stream()).toList());
            } else {
                return Optional.of(List.copyOf(customItem.behaviors()));
            }
        } else {
            List<ItemBehavior> behavior = VANILLA_ITEM_EXTRA_BEHAVIORS.get(key);
            if (behavior != null) {
                return Optional.of(List.copyOf(behavior));
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Collection<Key> items() {
        return Collections.unmodifiableCollection(this.customItems.keySet());
    }

    @Override
    public Map<Key, ItemModel> modernItemModels1_21_4() {
        return Collections.unmodifiableMap(this.modernItemModels1_21_4);
    }

    @Override
    public Map<Key, List<LegacyOverridesModel>> modernItemModels1_21_2() {
        return Collections.unmodifiableMap(this.modernItemModels1_21_2);
    }

    @Override
    public Collection<Key> vanillaItems() {
        return Collections.unmodifiableCollection(VANILLA_ITEMS);
    }

    @Override
    public Map<Key, TreeSet<LegacyOverridesModel>> legacyItemOverrides() {
        return Collections.unmodifiableMap(this.legacyOverrides);
    }

    @Override
    public Map<Key, TreeMap<Integer, ItemModel>> modernItemOverrides() {
        return Collections.unmodifiableMap(this.modernOverrides);
    }

    @Override
    public Collection<EquipmentGeneration> equipmentsToGenerate() {
        return Collections.unmodifiableCollection(this.equipmentsToGenerate);
    }

    private void registerFunctions() {
        registerDataFunction((obj) -> {
            String name = obj.toString();
            return new DisplayNameModifier<>(name);
        }, "custom-name");
        registerDataFunction((obj) -> {
            String name = obj.toString();
            return new ItemNameModifier<>(name);
        }, "item-name", "display-name");
        registerDataFunction((obj) -> {
            List<String> name = MiscUtils.getAsStringList(obj);
            return new LoreModifier<>(name);
        }, "lore", "display-lore", "description");
        registerDataFunction((obj) -> {
            Map<String, Object> data = MiscUtils.castToMap(obj, false);
            return new TagsModifier<>(data);
        }, "tags", "tag", "nbt");
        registerDataFunction((obj) -> {
            boolean value = TypeUtils.checkType(obj, Boolean.class);
            return new UnbreakableModifier<>(value);
        }, "unbreakable");
        registerDataFunction((obj) -> {
            Map<String, Object> data = MiscUtils.castToMap(obj, false);
            List<Enchantment> enchantments = new ArrayList<>();
            for (Map.Entry<String, Object> e : data.entrySet()) {
                if (e.getValue() instanceof Number number) {
                    enchantments.add(new Enchantment(Key.of(e.getKey()), number.intValue()));
                }
            }
            return new EnchantmentModifier<>(enchantments);
        }, "enchantment", "enchantments", "enchant");
        registerDataFunction((obj) -> {
            Map<String, Object> data = MiscUtils.castToMap(obj, false);
            String material = data.get("material").toString().toLowerCase(Locale.ENGLISH);
            String pattern = data.get("pattern").toString().toLowerCase(Locale.ENGLISH);
            return new TrimModifier<>(material, pattern);
        }, "trim");
        if (VersionHelper.isVersionNewerThan1_20_5()) {
            registerDataFunction((obj) -> {
                Map<String, Object> data = MiscUtils.castToMap(obj, false);
                return new ComponentModifier<>(data);
            }, "components", "component");
        }
        if (VersionHelper.isVersionNewerThan1_21()) {
            registerDataFunction((obj) -> {
                String song = obj.toString();
                return new JukeboxSongModifier<>(Key.of(song));
            }, "jukebox-playable");
        }
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            registerDataFunction((obj) -> {
                String id = obj.toString();
                return new TooltipStyleModifier<>(Key.of(id));
            }, "tooltip-style");
        }
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            registerDataFunction((obj) -> {
                Map<String, Object> data = MiscUtils.castToMap(obj, false);
                return new EquippableModifier<>(EquipmentData.fromMap(data));
            }, "equippable");
        }
    }
}
