package net.momirealms.craftengine.bukkit.compatibility.skript.expression;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExprCustomItem extends SimpleExpression<ItemStack> {

    public static void register() {
        Skript.registerExpression(ExprCustomItem.class, ItemStack.class, ExpressionType.SIMPLE, "[(the|a)] custom item [with id] %string%");
    }

    private Expression<String> itemId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        itemId = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    @Nullable
    protected ItemStack[] get(Event e) {
        String itemId = this.itemId.getSingle(e);
        if (itemId == null)
            return null;
        CustomItem<ItemStack> customItem = CraftEngineItems.byId(Key.of(itemId));
        return customItem == null ? null : new ItemStack[] {customItem.buildItemStack(ItemBuildContext.EMPTY)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "the custom item with id " + itemId.toString(e, debug);
    }
}
