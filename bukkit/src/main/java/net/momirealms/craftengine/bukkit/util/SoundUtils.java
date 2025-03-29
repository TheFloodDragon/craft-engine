package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.block.BlockSounds;
import net.momirealms.craftengine.core.util.Key;

public class SoundUtils {

    private SoundUtils() {}

    public static Object toSoundType(BlockSounds sounds) throws ReflectiveOperationException {
        return Reflections.constructor$SoundType.newInstance(
            1f, 1f,
                getOrRegisterSoundEvent(sounds.breakSound().id()),
                getOrRegisterSoundEvent(sounds.stepSound().id()),
                getOrRegisterSoundEvent(sounds.placeSound().id()),
                getOrRegisterSoundEvent(sounds.hitSound().id()),
                getOrRegisterSoundEvent(sounds.fallSound().id())
        );
    }

    public static Object getOrRegisterSoundEvent(Key key) throws ReflectiveOperationException {
        return Reflections.method$SoundEvent$createVariableRangeEvent.invoke(null,
                Reflections.method$ResourceLocation$fromNamespaceAndPath.invoke(null, key.namespace(), key.value())
        );
    }
}
