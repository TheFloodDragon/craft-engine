package net.momirealms.craftengine.core.util;

import org.jetbrains.annotations.NotNull;

public enum HorizontalDirection {
    NORTH,
    SOUTH,
    WEST,
    EAST;

    public Direction toDirection() {
        return switch (this) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
        };
    }

    @NotNull
    public HorizontalDirection opposite() {
        return switch (this) {
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
        };
    }
}
