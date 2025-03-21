package net.momirealms.craftengine.core.item.recipe.input;

import net.momirealms.craftengine.core.item.recipe.OptimizedIDItem;
import org.jetbrains.annotations.Nullable;

public class SmithingInput<T> implements RecipeInput {
    private final OptimizedIDItem<T> base;
    private final OptimizedIDItem<T> template;
    private final OptimizedIDItem<T> addition;

    public SmithingInput(@Nullable OptimizedIDItem<T> base,
                         @Nullable OptimizedIDItem<T> template,
                         @Nullable OptimizedIDItem<T> addition) {
        this.base = base;
        this.template = template;
        this.addition = addition;
    }

    @Nullable
    public OptimizedIDItem<T> base() {
        return base;
    }

    @Nullable
    public OptimizedIDItem<T> template() {
        return template;
    }

    @Nullable
    public OptimizedIDItem<T> addition() {
        return addition;
    }
}
