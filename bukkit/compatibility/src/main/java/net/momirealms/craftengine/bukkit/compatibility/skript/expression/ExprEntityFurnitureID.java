package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExprEntityFurnitureID extends SimplePropertyExpression<Object, String> {

    public static void register() {
        register(ExprEntityFurnitureID.class, String.class, "furniture id", "entities");
    }

    @Override
    public @Nullable String convert(Object object) {
        if (object instanceof Entity entity && CraftEngineFurniture.isFurniture(entity)) {
            return Optional.ofNullable(CraftEngineFurniture.getLoadedFurnitureByBaseEntity(entity))
                    .map(it -> it.id().toString())
                    .orElse(null);
        }
        return null;
    }

    @Override
    protected String getPropertyName() {
        return "furniture id";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
