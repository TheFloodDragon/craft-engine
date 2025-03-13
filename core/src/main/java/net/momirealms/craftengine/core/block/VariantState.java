package net.momirealms.craftengine.core.block;

public class VariantState {
    private final String appearance;
    private final BlockSettings settings;
    private final int internalId;

    public VariantState(String appearance, BlockSettings settings, int internalId) {
        this.appearance = appearance;
        this.settings = settings;
        this.internalId = internalId;
    }

    public String appearance() {
        return appearance;
    }

    public BlockSettings settings() {
        return settings;
    }

    public int internalRegistryId() {
        return internalId;
    }
}
