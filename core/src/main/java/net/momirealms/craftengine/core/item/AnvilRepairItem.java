package net.momirealms.craftengine.core.item;

import java.util.List;

public record AnvilRepairItem(List<String> targets, int amount, double percent) {
}
