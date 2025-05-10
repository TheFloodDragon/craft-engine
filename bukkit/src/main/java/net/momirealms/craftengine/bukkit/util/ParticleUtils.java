package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Particle;

public class ParticleUtils {

    public static Particle getParticle(String particle) {
        try {
            return Particle.valueOf(particle);
        } catch (IllegalArgumentException e) {
            return switch (particle) {
                case "REDSTONE" -> Particle.valueOf("DUST");
                case "VILLAGER_HAPPY", "HAPPY_VILLAGER" -> Particle.valueOf(VersionHelper.isOrAbove1_20_5() ? "HAPPY_VILLAGER" : "VILLAGER_HAPPY");
                default -> Particle.valueOf(particle);
            };
        }
    }
}
