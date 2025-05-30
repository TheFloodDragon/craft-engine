package net.momirealms.craftengine.bukkit.item;

import com.saicone.rtag.RtagItem;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.NBT;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class LegacyItemWrapper implements ItemWrapper<ItemStack> {
    private final RtagItem rtagItem;
    private int count;

    public LegacyItemWrapper(RtagItem rtagItem, int count) {
        this.rtagItem = rtagItem;
        this.count = count;
    }

    @Override
    public ItemStack getItem() {
        ItemStack itemStack = this.rtagItem.getItem();
        itemStack.setAmount(this.count);
        return itemStack;
    }

    public boolean setTag(Object value, Object... path) {
        if (value instanceof Tag tag) {
            return this.rtagItem.set(Reflections.instance$SPARROW_NBT_OPS.convertTo(Reflections.instance$NBT_OPS, tag), path);
        } else {
            return this.rtagItem.set(value, path);
        }
    }

    public boolean add(Object value, Object... path) {
        if (value instanceof Tag tag) {
            return this.rtagItem.add(Reflections.instance$SPARROW_NBT_OPS.convertTo(Reflections.instance$NBT_OPS, tag), path);
        } else {
            return this.rtagItem.add(value, path);
        }
    }

    public <V> V getJavaTag(Object... path) {
        return this.rtagItem.get(path);
    }

    public Tag getNBTTag(Object... path) {
        Object tag = getExactTag(path);
        if (tag == null) return null;
        return Reflections.instance$NBT_OPS.convertTo(Reflections.instance$SPARROW_NBT_OPS, tag);
    }

    public int count() {
        return this.count;
    }

    public void count(int amount) {
        if (amount < 0) amount = 0;
        this.count = amount;
    }

    public Object getExactTag(Object... path) {
        return this.rtagItem.getExact(path);
    }

    public boolean remove(Object... path) {
        return this.rtagItem.remove(path);
    }

    public boolean hasTag(Object... path) {
        return this.rtagItem.hasTag(path);
    }

    public void update() {
        this.rtagItem.update();
    }

    @Override
    public ItemStack load() {
        ItemStack itemStack = this.rtagItem.load();
        itemStack.setAmount(Math.max(this.count, 0));
        return itemStack;
    }

    @Override
    public Object getLiteralObject() {
        return this.rtagItem.getLiteralObject();
    }

    @Override
    public ItemWrapper<ItemStack> copyWithCount(int count) {
        return new LegacyItemWrapper(new RtagItem(this.rtagItem.loadCopy()), count);
    }
}