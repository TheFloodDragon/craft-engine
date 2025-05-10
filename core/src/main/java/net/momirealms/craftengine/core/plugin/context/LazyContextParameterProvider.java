package net.momirealms.craftengine.core.plugin.context;

import java.util.Optional;

public interface LazyContextParameterProvider {

    <T> Optional<T> getOptionalParameter(ContextKey<T> parameter);

    static LazyContextParameterProvider dummy() {
        return DummyContextParameterProvider.INSTANCE;
    }

    class DummyContextParameterProvider implements LazyContextParameterProvider {
        static final DummyContextParameterProvider INSTANCE = new DummyContextParameterProvider();

        @Override
        public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
            return Optional.empty();
        }
    }
}
