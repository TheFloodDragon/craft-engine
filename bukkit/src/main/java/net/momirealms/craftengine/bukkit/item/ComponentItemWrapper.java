package net.momirealms.craftengine.bukkit.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.item.ItemWrapper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ComponentItemWrapper implements ItemWrapper<ItemStack> {
    private final ItemStack item;
    private final Object handle;

    public ComponentItemWrapper(final ItemStack item) {
        this.item = FastNMS.INSTANCE.ensureCraftItemStack(item);
        this.handle = FastNMS.INSTANCE.field$CraftItemStack$handle(this.item);
    }

    public ComponentItemWrapper(final ItemStack item, int count) {
        this.item = FastNMS.INSTANCE.ensureCraftItemStack(item);
        this.item.setAmount(count);
        this.handle = FastNMS.INSTANCE.field$CraftItemStack$handle(this.item);
    }

    public void removeComponent(Object type) {
        FastNMS.INSTANCE.removeComponent(this.getLiteralObject(), ensureDataComponentType(type));
    }

    public void resetComponent(Object type) {
        FastNMS.INSTANCE.resetComponent(this.getLiteralObject(), ensureDataComponentType(type));
    }

    public void setComponent(Object type, final Object value) {
        if (value instanceof JsonElement jsonElement) {
            setJsonComponent(type, jsonElement);
        } else if (Reflections.clazz$Tag.isInstance(value)) {
            setNBTComponent(type, value);
        } else if (value instanceof Tag tag) {
            setSparrowNBTComponent(type, tag);
        } else {
            setJavaComponent(type, value);
        }
    }

    public Object getComponentExact(Object type) {
        return FastNMS.INSTANCE.getComponent(getLiteralObject(), ensureDataComponentType(type));
    }

    public <T> Optional<T> getJavaComponent(Object type) {
        return getComponentInternal(type, Reflections.instance$JAVA_OPS);
    }

    public Optional<JsonElement> getJsonComponent(Object type) {
        return getComponentInternal(type, Reflections.instance$JSON_OPS);
    }

    public Optional<Object> getNBTComponent(Object type) {
        return getComponentInternal(type, Reflections.instance$NBT_OPS);
    }

    public Optional<Tag> getSparrowNBTComponent(Object type) {
        return getComponentInternal(type, Reflections.instance$SPARROW_NBT_OPS);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> Optional<T> getComponentInternal(Object type, DynamicOps ops) {
        Object componentType = ensureDataComponentType(type);
        Codec codec = FastNMS.INSTANCE.method$DataComponentType$codec(componentType);
        try {
            Object componentData = FastNMS.INSTANCE.getComponent(getLiteralObject(), componentType);
            if (componentData == null) return Optional.empty();
            DataResult<Object> result = codec.encodeStart(ops, componentData);
            return (Optional<T>) result.result();
        } catch (Throwable t) {
            throw new RuntimeException("Cannot read component " + type.toString(), t);
        }
    }

    public boolean hasComponent(Object type) {
        return FastNMS.INSTANCE.hasComponent(getLiteralObject(), ensureDataComponentType(type));
    }

    public void setComponentExact(Object type, final Object value) {
        FastNMS.INSTANCE.setComponent(this.getLiteralObject(), ensureDataComponentType(type), value);
    }

    public void setJavaComponent(Object type, Object value) {
        setComponentInternal(type, Reflections.instance$JAVA_OPS, value);
    }

    public void setJsonComponent(Object type, JsonElement value) {
        setComponentInternal(type, Reflections.instance$JSON_OPS, value);
    }

    public void setNBTComponent(Object type, Object value) {
       setComponentInternal(type, Reflections.instance$NBT_OPS, value);
    }

    public void setSparrowNBTComponent(Object type, Tag value) {
        setComponentInternal(type, Reflections.instance$SPARROW_NBT_OPS, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setComponentInternal(Object type, DynamicOps ops, Object value) {
        if (value == null) return;
        Object componentType = ensureDataComponentType(type);
        Codec codec = FastNMS.INSTANCE.method$DataComponentType$codec(componentType);
        try {
            DataResult<Object> result = codec.parse(ops, value);
            if (result.isError()) {
                throw new IllegalArgumentException(result.toString());
            }
            result.result().ifPresent(it -> FastNMS.INSTANCE.setComponent(this.getLiteralObject(), componentType, it));
        } catch (Throwable t) {
            throw new RuntimeException("Cannot parse component " + type.toString(), t);
        }
    }

    private Object ensureDataComponentType(Object type) {
        assert Reflections.clazz$DataComponentType != null;
        if (!Reflections.clazz$DataComponentType.isInstance(type)) {
            Key key = Key.of(type.toString());
            return FastNMS.INSTANCE.getComponentType(key.namespace(), key.value());
        }
        return type;
    }

    @Override
    public ItemWrapper<ItemStack> copyWithCount(int count) {
        ItemStack copied = this.item.clone();
        copied.setAmount(count);
        return new ComponentItemWrapper(copied);
    }

    @Override
    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public ItemStack load() {
        return this.item;
    }

    @Override
    public Object getLiteralObject() {
        return this.handle;
    }

    @Override
    public int count() {
        return this.item.getAmount();
    }

    @Override
    public void count(int amount) {
        this.item.setAmount(Math.max(amount, 0));
    }
}
