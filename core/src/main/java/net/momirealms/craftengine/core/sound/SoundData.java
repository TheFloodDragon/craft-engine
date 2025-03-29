package net.momirealms.craftengine.core.sound;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.Map;

public record SoundData(Key id, float volume, float pitch) {

    public static SoundData create(Object obj, float volume, float pitch) {
        if (obj instanceof String key) {
            return new SoundData(Key.of(key), volume, pitch);
        } else if (obj instanceof Map<?,?> map) {
            Map<String, Object> data = MiscUtils.castToMap(map, false);
            Key id = Key.of((String) data.get("id"));
            float volumeFloat = MiscUtils.getAsFloat(data.getOrDefault("volume", volume));
            float pitchFloat = MiscUtils.getAsFloat(data.getOrDefault("pitch", pitch));
            return new SoundData(id, volumeFloat, pitchFloat);
        } else {
            throw new IllegalArgumentException("Illegal object type for sound data: " + obj.getClass());
        }
    }
}
