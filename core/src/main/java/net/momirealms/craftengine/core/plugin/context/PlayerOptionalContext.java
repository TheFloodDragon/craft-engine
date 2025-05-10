package net.momirealms.craftengine.core.plugin.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameterProvider;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.plugin.context.parameter.PlayerParameterProvider;
import net.momirealms.craftengine.core.plugin.text.minimessage.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PlayerOptionalContext extends AbstractAdditionalParameterContext implements Context {
    public static final PlayerOptionalContext EMPTY = new PlayerOptionalContext(null, ContextHolder.EMPTY);
    protected final Player player;

    public PlayerOptionalContext(@Nullable Player player, @NotNull ContextHolder contexts) {
        super(contexts, player == null ? List.of(new CommonParameterProvider()) : List.of(new CommonParameterProvider(), new PlayerParameterProvider(player)));
        this.player = player;
    }

    public PlayerOptionalContext(@Nullable Player player, @NotNull ContextHolder contexts, @NotNull List<LazyContextParameterProvider> providers) {
        super(contexts, providers);
        this.player = player;
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player, @NotNull ContextHolder contexts) {
        return new PlayerOptionalContext(player, contexts);
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player, @NotNull ContextHolder.Builder contexts) {
        if (player != null) contexts.withParameter(CommonParameters.PLAYER, player);
        return new PlayerOptionalContext(player, contexts.build());
    }

    @NotNull
    public static PlayerOptionalContext of(@Nullable Player player) {
        if (player == null) return EMPTY;
        return new PlayerOptionalContext(player, new ContextHolder(Map.of(CommonParameters.PLAYER, () -> player)));
    }

    @Nullable
    public Player player() {
        return this.player;
    }

    public boolean isPlayerPresent() {
        return this.player != null;
    }

    @Override
    @NotNull
    public TagResolver[] tagResolvers() {
        if (this.tagResolvers == null) {
            this.tagResolvers = new TagResolver[]{ShiftTag.INSTANCE, ImageTag.INSTANCE, new PlaceholderTag(this.player), new I18NTag(this),
                    new NamedArgumentTag(this), new ExpressionTag(this), new GlobalVariableTag(this)};
        }
        return this.tagResolvers;
    }
}
