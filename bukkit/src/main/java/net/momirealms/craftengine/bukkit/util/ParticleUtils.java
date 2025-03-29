package net.momirealms.craftengine.bukkit.util;

import org.bukkit.Particle;

public class ParticleUtils {

    public static Particle getParticle(String particle) {
        try {
            return Particle.valueOf(particle);
        } catch (IllegalArgumentException e) {
            return switch (particle) {
                case "REDSTONE" -> Particle.valueOf("DUST");
                case "VILLAGER_HAPPY" -> Particle.valueOf("HAPPY_VILLAGER");
                default -> Particle.valueOf(particle);
            };
        }
    }
}
