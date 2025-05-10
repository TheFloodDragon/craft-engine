package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ViewerPlaceholderTag extends PlaceholderTag {

    public ViewerPlaceholderTag(@Nullable Player player) {
        super(player);
    }

    @Override
    public boolean has(@NotNull String name) {
        return "viewer_papi".equals(name);
    }
}
