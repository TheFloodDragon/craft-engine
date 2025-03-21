package net.momirealms.craftengine.bukkit.util;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;

public class LegacyInventoryUtils {

    public static Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }

    public static void setRepairCost(AnvilInventory anvilInventory, int repairCost) {
        anvilInventory.setRepairCost(repairCost);
    }

    public static void setRepairCostAmount(AnvilInventory anvilInventory, int amount) {
        anvilInventory.setRepairCostAmount(amount);
    }

    @Nullable
    public static String getRenameText(AnvilInventory anvilInventory) {
        return anvilInventory.getRenameText();
    }

    public static int getMaxRepairCost(AnvilInventory anvilInventory) {
        return anvilInventory.getMaximumRepairCost();
    }

    public static int getRepairCost(AnvilInventory anvilInventory) {
        return anvilInventory.getRepairCost();
    }

    public static InventoryView getView(PrepareAnvilEvent event) {
        return event.getView();
    }
}
