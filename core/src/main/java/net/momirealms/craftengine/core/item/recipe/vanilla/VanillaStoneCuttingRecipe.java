package net.momirealms.craftengine.core.item.recipe.vanilla;

import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public class VanillaStoneCuttingRecipe extends VanillaGroupedRecipe {
    private final List<String> ingredient;

    public VanillaStoneCuttingRecipe(String group, RecipeResult result, List<String> ingredient) {
        super(group, result);
        this.ingredient = ingredient;
    }

    public List<String> ingredient() {
        return ingredient;
    }

    @Override
    public Key type() {
        return RecipeTypes.STONECUTTING;
    }
}
