package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;

public class TextDisplayEntityData<T> extends DisplayEntityData<T> {
    public static final DisplayEntityData<Object> Text = new TextDisplayEntityData<>(TextDisplayEntityData.class, EntityDataValue.Serializers$COMPONENT, CoreReflections.instance$Component$empty);
    public static final DisplayEntityData<Integer> LineWidth = new TextDisplayEntityData<>(TextDisplayEntityData.class, EntityDataValue.Serializers$INT, 200);
    public static final DisplayEntityData<Integer> BackgroundColor = new TextDisplayEntityData<>(TextDisplayEntityData.class, EntityDataValue.Serializers$INT, 0x40000000);
    public static final DisplayEntityData<Byte> TextOpacity = new TextDisplayEntityData<>(TextDisplayEntityData.class, EntityDataValue.Serializers$BYTE, (byte) -1);
    public static final DisplayEntityData<Byte> TextDisplayMasks = new TextDisplayEntityData<>(TextDisplayEntityData.class, EntityDataValue.Serializers$BYTE, (byte) 0);

    public TextDisplayEntityData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
