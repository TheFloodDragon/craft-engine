package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTag implements TagResolver {
    private final Player player;

    public PlaceholderTag(@Nullable Player player) {
        this.player = player;
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (!this.has(name) || !CraftEngine.instance().compatibilityManager().hasPlaceholderAPI()) {
            return null;
        }
        String placeholder = "%" + arguments.popOr("No argument placeholder provided") + "%";
        String parsed = CraftEngine.instance().compatibilityManager().parse(player, placeholder);
        if (parsed.equals(placeholder)) {
            parsed = arguments.popOr("No default papi value provided").toString();
        }
        return Tag.selfClosingInserting(AdventureHelper.miniMessage().deserialize(parsed));
    }

    @Override
    public boolean has(@NotNull String name) {
        return "papi".equals(name);
    }
}
