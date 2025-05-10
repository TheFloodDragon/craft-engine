package net.momirealms.craftengine.core.plugin.context;

import org.jetbrains.annotations.NotNull;

public class ContextKey<T> {
    private final String id;

    public ContextKey(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String id() {
        return id;
    }

    @NotNull
    public static <T> ContextKey<T> of(@NotNull String id) {
        return new ContextKey<>(id);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContextKey<?> that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
