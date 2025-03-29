package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class FurnitureSounds {
    public static final SoundData EMPTY_SOUND = new SoundData(Key.of("minecraft:intentionally_empty"), 1, 1);
    public static final FurnitureSounds EMPTY = new FurnitureSounds(EMPTY_SOUND, EMPTY_SOUND, EMPTY_SOUND);

    private final SoundData breakSound;
    private final SoundData placeSound;
    private final SoundData rotateSound;

    public FurnitureSounds(SoundData breakSound, SoundData placeSound, SoundData rotateSound) {
        this.breakSound = breakSound;
        this.placeSound = placeSound;
        this.rotateSound = rotateSound;
    }

    public static FurnitureSounds fromMap(Map<String, Object> map) {
        if (map == null) return EMPTY;
        return new FurnitureSounds(
                SoundData.create(map.getOrDefault("break", "minecraft:intentionally_empty"), 1f, 0.8f),
                SoundData.create(map.getOrDefault("place", "minecraft:intentionally_empty"), 0f, 0.8f),
                SoundData.create(map.getOrDefault("rotate", "minecraft:intentionally_empty"), 1f, 0.8f)
        );
    }

    public SoundData breakSound() {
        return breakSound;
    }

    public SoundData placeSound() {
        return placeSound;
    }

    public SoundData rotateSound() {
        return rotateSound;
    }
}
