package net.momirealms.craftengine.core.plugin.context;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.plugin.text.minimessage.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class AbstractCommonContext implements Context {
    protected final ContextHolder contexts;
    protected TagResolver[] tagResolvers;

    public AbstractCommonContext(ContextHolder contexts) {
        this.contexts = contexts;
    }

    @Override
    public ContextHolder contexts() {
        return this.contexts;
    }

    @Override
    @NotNull
    public TagResolver[] tagResolvers() {
        if (this.tagResolvers == null) {
            this.tagResolvers = new TagResolver[]{ShiftTag.INSTANCE, ImageTag.INSTANCE, new I18NTag(this), new NamedArgumentTag(this),
                    new PlaceholderTag(null), new ExpressionTag(this), new GlobalVariableTag(this)};
        }
        return this.tagResolvers;
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return this.contexts.getOptional(parameter);
    }

    @Override
    public <T> T getParameterOrThrow(ContextKey<T> parameter) {
        return this.contexts.getOrThrow(parameter);
    }

    // It's not designed as mutable
//    @Override
//    public <T> AbstractCommonContext withParameter(ContextKey<T> parameter, T value) {
//        this.contexts.withParameter(parameter, value);
//        return this;
//    }
}
