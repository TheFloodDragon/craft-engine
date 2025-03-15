package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.modifier.EquippableModifier;
import net.momirealms.craftengine.core.item.modifier.ItemModifier;
import net.momirealms.craftengine.core.pack.misc.EquipmentGeneration;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ItemSettings {
    int fuelTime;
    Set<Key> tags = Set.of();
    @Nullable
    EquipmentGeneration equipment;

    private ItemSettings() {}

    public <I> List<ItemModifier<I>> modifiers() {
        ArrayList<ItemModifier<I>> modifiers = new ArrayList<>();
        if (VersionHelper.isVersionNewerThan1_21_2() && this.equipment != null && this.equipment.modernData() != null) modifiers.add(new EquippableModifier<>(this.equipment.modernData()));

        return modifiers;
    }

    public static ItemSettings of() {
        return new ItemSettings();
    }

    public static ItemSettings fromMap(Map<String, Object> map) {
        return applyModifiers(ItemSettings.of(), map);
    }

    public static ItemSettings ofFullCopy(ItemSettings settings) {
        ItemSettings newSettings = of();
        newSettings.fuelTime = settings.fuelTime;
        newSettings.tags = settings.tags;
        newSettings.equipment = settings.equipment;
        return newSettings;
    }

    public static ItemSettings applyModifiers(ItemSettings settings, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ItemSettings.Modifier.Factory factory = ItemSettings.Modifiers.FACTORIES.get(entry.getKey());
            if (factory != null) {
                factory.createModifier(entry.getValue()).apply(settings);
            } else {
                throw new IllegalArgumentException("Unknown item settings key: " + entry.getKey());
            }
        }
        return settings;
    }

    public int fuelTime() {
        return fuelTime;
    }

    public Set<Key> tags() {
        return tags;
    }

    @Nullable
    public EquipmentGeneration equipment() {
        return equipment;
    }

    public ItemSettings fuelTime(int fuelTime) {
        this.fuelTime = fuelTime;
        return this;
    }

    public ItemSettings tags(Set<Key> tags) {
        this.tags = tags;
        return this;
    }

    public ItemSettings equipment(EquipmentGeneration equipment) {
        this.equipment = equipment;
        return this;
    }

    @FunctionalInterface
    public interface Modifier {

        void apply(ItemSettings settings);

        @FunctionalInterface
        interface Factory {

            ItemSettings.Modifier createModifier(Object value);
        }
    }

    public static class Modifiers {
        private static final Map<String, ItemSettings.Modifier.Factory> FACTORIES = new HashMap<>();

        static {
            registerFactory("fuel-time", (value -> {
                int intValue = MiscUtils.getAsInt(value);
                return settings -> settings.fuelTime(intValue);
            }));
            registerFactory("tags", (value -> {
                List<String> tags = MiscUtils.getAsStringList(value);
                return settings -> settings.tags(tags.stream().map(Key::of).collect(Collectors.toSet()));
            }));
            registerFactory("equippable", (value -> {
                Map<String, Object> args = MiscUtils.castToMap(value, false);
                EquipmentData data;
                if (VersionHelper.isVersionNewerThan1_21_2() && args.containsKey("slot")) data = EquipmentData.fromMap(args);
                else data = null;
                EquipmentGeneration equipment = new EquipmentGeneration(
                        EquipmentGeneration.Layer.fromConfig(args.get("humanoid")),
                        EquipmentGeneration.Layer.fromConfig(args.get("humanoid-leggings")),
                        EquipmentGeneration.Layer.fromConfig(args.get("llama-body")),
                        EquipmentGeneration.Layer.fromConfig(args.get("horse-body")),
                        EquipmentGeneration.Layer.fromConfig(args.get("wolf-body")),
                        EquipmentGeneration.Layer.fromConfig(args.get("wings")),
                        data,
                        MiscUtils.getAsInt(args.getOrDefault("trim", -1))
                );
                return settings -> settings.equipment(equipment);
            }));
        }

        private static void registerFactory(String id, ItemSettings.Modifier.Factory factory) {
            FACTORIES.put(id, factory);
        }
    }
}
