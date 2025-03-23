package net.momirealms.craftengine.bukkit.item.factory;

import com.saicone.rtag.RtagItem;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import net.momirealms.craftengine.bukkit.item.RTagItemWrapper;
import net.momirealms.craftengine.core.item.Enchantment;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.SkullUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class UniversalItemFactory extends BukkitItemFactory {

    public UniversalItemFactory(CraftEngine plugin) {
        super(plugin);
    }

    @Override
    public Object encodeJava(Key componentType, @Nullable Object component) {
        throw new UnsupportedOperationException("This feature is only available on 1.20.5+");
    }

    @Override
    protected void customName(ItemWrapper<ItemStack> item, String json) {
        if (json != null) {
            item.set(json, "display", "Name");
        } else {
            item.remove("display", "Name");
        }
    }

    @Override
    protected Optional<String> customName(ItemWrapper<ItemStack> item) {
        if (!item.hasTag("display", "Name")) return Optional.empty();
        return Optional.of(item.get("display", "Name"));
    }

    @Override
    protected void itemName(ItemWrapper<ItemStack> item, String json) {
        customName(item, json);
    }

    @Override
    protected Optional<String> itemName(ItemWrapper<ItemStack> item) {
        return customName(item);
    }

    @Override
    protected void customModelData(ItemWrapper<ItemStack> item, Integer data) {
        if (data == null) {
            item.remove("CustomModelData");
        } else {
            item.set(data, "CustomModelData");
        }
    }

    @Override
    protected Optional<Integer> customModelData(ItemWrapper<ItemStack> item) {
        if (!item.hasTag("CustomModelData")) return Optional.empty();
        return Optional.of(item.get("CustomModelData"));
    }

    @Override
    protected void skull(ItemWrapper<ItemStack> item, String skullData) {
        if (skullData == null) {
            item.remove("SkullOwner");
        } else {
            item.set(UUID.nameUUIDFromBytes(SkullUtils.identifierFromBase64(skullData).getBytes(StandardCharsets.UTF_8)), "SkullOwner", "Id");
            item.set(
                    List.of(Map.of("Value", skullData)),
                    "SkullOwner", "Properties", "textures"
            );
        }
    }

    @Override
    protected Optional<List<String>> lore(ItemWrapper<ItemStack> item) {
        if (!item.hasTag("display", "Lore")) return Optional.empty();
        return Optional.of(item.get("display", "Lore"));
    }

    @Override
    protected void lore(ItemWrapper<ItemStack> item, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            item.remove("display", "Lore");
        } else {
            item.set(lore, "display", "Lore");
        }
    }

    @Override
    protected boolean unbreakable(ItemWrapper<ItemStack> item) {
        return Optional.ofNullable((Boolean) item.get("Unbreakable")).orElse(false);
    }

    @Override
    protected void unbreakable(ItemWrapper<ItemStack> item, boolean unbreakable) {
        item.set(unbreakable, "Unbreakable");
    }

    @Override
    protected Optional<Boolean> glint(ItemWrapper<ItemStack> item) {
        return Optional.of(false);
    }

    @Override
    protected void glint(ItemWrapper<ItemStack> item, Boolean glint) {
        throw new UnsupportedOperationException("This feature is only available on 1.20.5+");
    }

    @Override
    protected Optional<Integer> damage(ItemWrapper<ItemStack> item) {
        if (!item.hasTag("Damage")) return Optional.empty();
        return Optional.of(item.get("Damage"));
    }

    @Override
    protected void damage(ItemWrapper<ItemStack> item, Integer damage) {
        item.set(damage, "Damage");
    }

    @Override
    protected Optional<Integer> maxDamage(ItemWrapper<ItemStack> item) {
        return Optional.of((int) item.getItem().getType().getMaxDurability());
    }

    @Override
    protected void maxDamage(ItemWrapper<ItemStack> item, Integer damage) {
        throw new UnsupportedOperationException("This feature is only available on 1.20.5+");
    }

    @Override
    protected void enchantments(ItemWrapper<ItemStack> item, List<Enchantment> enchantments) {
        ArrayList<Object> tags = new ArrayList<>();
        for (Enchantment enchantment : enchantments) {
            tags.add((Map.of("id", enchantment.id().toString(), "lvl", (short) enchantment.level())));
        }
        item.set(tags, "Enchantments");
    }

    @Override
    protected void storedEnchantments(ItemWrapper<ItemStack> item, List<Enchantment> enchantments) {
        ArrayList<Object> tags = new ArrayList<>();
        for (Enchantment enchantment : enchantments) {
            tags.add((Map.of("id", enchantment.id().toString(), "lvl", (short) enchantment.level())));
        }
        item.set(tags, "StoredEnchantments");
    }

    @Override
    protected void addEnchantment(ItemWrapper<ItemStack> item, Enchantment enchantment) {
        Object enchantments = item.getExact("Enchantments");
        if (enchantments != null) {
            for (Object enchant : TagList.getValue(enchantments)) {
                if (TagBase.getValue(TagCompound.get(enchant, "id")).equals(enchant.toString())) {
                    TagCompound.set(enchant, "lvl", TagBase.newTag(enchantment.level()));
                    return;
                }
            }
            item.add(Map.of("id", enchantment.toString(), "lvl", (short) enchantment.level()), "Enchantments");
        } else {
            item.set(List.of(Map.of("id", enchantment.toString(), "lvl", (short) enchantment.level())), "Enchantments");
        }
    }

    @Override
    protected void addStoredEnchantment(ItemWrapper<ItemStack> item, Enchantment enchantment) {
        Object enchantments = item.getExact("StoredEnchantments");
        if (enchantments != null) {
            for (Object enchant : TagList.getValue(enchantments)) {
                if (TagBase.getValue(TagCompound.get(enchant, "id")).equals(enchant.toString())) {
                    TagCompound.set(enchant, "lvl", TagBase.newTag(enchantment.level()));
                    return;
                }
            }
            item.add(Map.of("id", enchantment.toString(), "lvl", (short) enchantment.level()), "StoredEnchantments");
        } else {
            item.set(List.of(Map.of("id", enchantment.toString(), "lvl", (short) enchantment.level())), "StoredEnchantments");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Optional<Enchantment> getEnchantment(ItemWrapper<ItemStack> item, Key key) {
        int level = item.getItem().getEnchantmentLevel(Objects.requireNonNull(Registry.ENCHANTMENT.get(new NamespacedKey(key.namespace(), key.value()))));
        if (level <= 0) return Optional.empty();
        return Optional.of(new Enchantment(key, level));
    }

    @Override
    protected void itemFlags(ItemWrapper<ItemStack> item, List<String> flags) {
        if (flags == null || flags.isEmpty()) {
            item.remove("HideFlags");
            return;
        }
        int f = 0;
        for (String flag : flags) {
            ItemFlag itemFlag = ItemFlag.valueOf(flag);
            f = f | 1 << itemFlag.ordinal();
        }
        item.set(f, "HideFlags");
    }

    @Override
    protected int maxStackSize(ItemWrapper<ItemStack> item) {
        return item.getItem().getType().getMaxStackSize();
    }

    @Override
    protected void maxStackSize(ItemWrapper<ItemStack> item, Integer maxStackSize) {
        throw new UnsupportedOperationException("This feature is only available on 1.20.5+");
    }

    @Override
    protected void repairCost(ItemWrapper<ItemStack> item, Integer data) {
        item.set(data, "RepairCost");
    }

    @Override
    protected Optional<Integer> repairCost(ItemWrapper<ItemStack> item) {
        if (!item.hasTag("RepairCost")) return Optional.empty();
        return Optional.of(item.get("RepairCost"));
    }

    @Override
    protected ItemWrapper<ItemStack> mergeCopy(ItemWrapper<ItemStack> item1, ItemWrapper<ItemStack> item2) {
        Object itemStack = ItemObject.copy(item2.getLiteralObject());
        ItemObject.setCustomDataTag(itemStack, TagCompound.clone(ItemObject.getCustomDataTag(item1.getLiteralObject())));
        // one more step than vanilla
        TagCompound.merge(ItemObject.getCustomDataTag(itemStack), ItemObject.getCustomDataTag(item2.getLiteralObject()), true, true);
        return new RTagItemWrapper(new RtagItem(ItemObject.asCraftMirror(itemStack)), item2.count());
    }

    @Override
    protected void merge(ItemWrapper<ItemStack> item1, ItemWrapper<ItemStack> item2) {
        // load previous changes on nms items
        item1.load();
        TagCompound.merge(ItemObject.getCustomDataTag(item1.getLiteralObject()), ItemObject.getCustomDataTag(item2.getLiteralObject()), true, true);
    }
}