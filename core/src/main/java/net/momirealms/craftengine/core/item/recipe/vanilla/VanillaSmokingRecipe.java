package net.momirealms.craftengine.core.item.recipe.vanilla;

import net.momirealms.craftengine.core.item.recipe.CookingRecipeCategory;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public class VanillaSmokingRecipe extends VanillaCookingRecipe {

    public VanillaSmokingRecipe(CookingRecipeCategory category, String group, RecipeResult result, List<String> ingredient, float experience, int cookingTime) {
        super(category, group, result, ingredient, experience, cookingTime);
    }

    @Override
    public Key type() {
        return RecipeTypes.SMOKING;
    }
}
