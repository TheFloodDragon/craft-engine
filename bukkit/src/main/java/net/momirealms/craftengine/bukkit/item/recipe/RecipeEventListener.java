package net.momirealms.craftengine.bukkit.item.recipe;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.injector.BukkitInjector;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ItemUtils;
import net.momirealms.craftengine.bukkit.util.LegacyInventoryUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.item.recipe.CustomCampfireRecipe;
import net.momirealms.craftengine.core.item.recipe.OptimizedIDItem;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.item.recipe.input.CraftingInput;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Pair;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.view.AnvilView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public class RecipeEventListener implements Listener {
    private static final OptimizedIDItem<ItemStack> EMPTY = new OptimizedIDItem<>(null, null);
    private final ItemManager<ItemStack> itemManager;
    private final BukkitRecipeManager recipeManager;
    private final BukkitCraftEngine plugin;

    public RecipeEventListener(BukkitCraftEngine plugin, BukkitRecipeManager recipeManager, ItemManager<ItemStack> itemManager) {
        this.itemManager = itemManager;
        this.recipeManager = recipeManager;
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onClickInventoryWithFuel(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory instanceof FurnaceInventory furnaceInventory)) return;
        ItemStack fuelStack = furnaceInventory.getFuel();
        Inventory clickedInventory = event.getClickedInventory();

        Player player = (Player) event.getWhoClicked();
        if (clickedInventory == player.getInventory()) {
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                ItemStack item = event.getCurrentItem();
                if (ItemUtils.isEmpty(item)) return;
                if (fuelStack == null || fuelStack.getType() == Material.AIR) {
                    Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(item);
                    Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                    if (idHolder.isEmpty()) return;

                    SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), item));
                    Key recipeType;
                    if (furnaceInventory.getType() == InventoryType.FURNACE) {
                        recipeType = RecipeTypes.SMELTING;
                    } else if (furnaceInventory.getType() == InventoryType.BLAST_FURNACE) {
                        recipeType = RecipeTypes.BLASTING;
                    } else {
                        recipeType = RecipeTypes.SMOKING;
                    }

                    Recipe<ItemStack> ceRecipe = recipeManager.getRecipe(recipeType, input);
                    // The item is an ingredient, we should never consider it as fuel firstly
                    if (ceRecipe != null) return;

                    int fuelTime = this.itemManager.fuelTime(item);
                    if (fuelTime == 0) {
                        if (ItemUtils.isCustomItem(item) && item.getType().isFuel()) {
                            event.setCancelled(true);
                            ItemStack smelting = furnaceInventory.getSmelting();
                            if (ItemUtils.isEmpty(smelting)) {
                                furnaceInventory.setSmelting(item.clone());
                                item.setAmount(0);
                            } else if (smelting.isSimilar(item)) {
                                int maxStackSize = smelting.getMaxStackSize();
                                int canGiveMaxCount = item.getAmount();
                                if (maxStackSize > smelting.getAmount()) {
                                    if (canGiveMaxCount + smelting.getAmount() >= maxStackSize) {
                                        int givenCount = maxStackSize - smelting.getAmount();
                                        smelting.setAmount(maxStackSize);
                                        item.setAmount(item.getAmount() - givenCount);
                                    } else {
                                        smelting.setAmount(smelting.getAmount() + canGiveMaxCount);
                                        item.setAmount(0);
                                    }
                                }
                            }
                            player.updateInventory();
                        }
                        return;
                    }
                    event.setCancelled(true);
                    furnaceInventory.setFuel(item.clone());
                    item.setAmount(0);
                    player.updateInventory();
                } else {
                    if (fuelStack.isSimilar(item)) {
                        event.setCancelled(true);
                        int maxStackSize = fuelStack.getMaxStackSize();
                        int canGiveMaxCount = item.getAmount();
                        if (maxStackSize > fuelStack.getAmount()) {
                            if (canGiveMaxCount + fuelStack.getAmount() >= maxStackSize) {
                                int givenCount = maxStackSize - fuelStack.getAmount();
                                fuelStack.setAmount(maxStackSize);
                                item.setAmount(item.getAmount() - givenCount);
                            } else {
                                fuelStack.setAmount(fuelStack.getAmount() + canGiveMaxCount);
                                item.setAmount(0);
                            }
                            player.updateInventory();
                        }
                    }
                }
            }
        } else {
            // click the furnace inventory
            int slot = event.getSlot();
            // click the fuel slot
            if (slot != 1) {
                return;
            }
            ClickType clickType = event.getClick();
            switch (clickType) {
                case SWAP_OFFHAND, NUMBER_KEY -> {
                    ItemStack item;
                    int hotBarSlot = event.getHotbarButton();
                    if (clickType == ClickType.SWAP_OFFHAND) {
                        item = player.getInventory().getItemInOffHand();
                    } else {
                        item = player.getInventory().getItem(hotBarSlot);
                    }
                    if (item == null) return;
                    int fuelTime = this.plugin.itemManager().fuelTime(item);
                    // only handle custom items
                    if (fuelTime == 0) {
                        if (ItemUtils.isCustomItem(item) && item.getType().isFuel()) {
                            event.setCancelled(true);
                        }
                        return;
                    }

                    event.setCancelled(true);
                    if (fuelStack == null || fuelStack.getType() == Material.AIR) {
                        furnaceInventory.setFuel(item.clone());
                        item.setAmount(0);
                    } else {
                        if (clickType == ClickType.SWAP_OFFHAND) {
                            player.getInventory().setItemInOffHand(fuelStack);
                        } else {
                            player.getInventory().setItem(hotBarSlot, fuelStack);
                        }
                        furnaceInventory.setFuel(item.clone());
                    }
                    player.updateInventory();
                }
                case LEFT, RIGHT -> {
                    ItemStack itemOnCursor = event.getCursor();
                    // pick item
                    if (ItemUtils.isEmpty(itemOnCursor)) return;
                    int fuelTime = this.plugin.itemManager().fuelTime(itemOnCursor);
                    // only handle custom items
                    if (fuelTime == 0) {
                        if (ItemUtils.isCustomItem(itemOnCursor) && itemOnCursor.getType().isFuel()) {
                            event.setCancelled(true);
                        }
                        return;
                    }

                    event.setCancelled(true);
                    // The slot is empty
                    if (fuelStack == null || fuelStack.getType() == Material.AIR) {
                        if (clickType == ClickType.LEFT) {
                            furnaceInventory.setFuel(itemOnCursor.clone());
                            itemOnCursor.setAmount(0);
                            player.updateInventory();
                        } else {
                            ItemStack cloned = itemOnCursor.clone();
                            cloned.setAmount(1);
                            furnaceInventory.setFuel(cloned);
                            itemOnCursor.setAmount(itemOnCursor.getAmount() - 1);
                            player.updateInventory();
                        }
                    } else {
                        boolean isSimilar = itemOnCursor.isSimilar(fuelStack);
                        if (clickType == ClickType.LEFT) {
                            if (isSimilar) {
                                int maxStackSize = fuelStack.getMaxStackSize();
                                int canGiveMaxCount = itemOnCursor.getAmount();
                                if (maxStackSize > fuelStack.getAmount()) {
                                    if (canGiveMaxCount + fuelStack.getAmount() >= maxStackSize) {
                                        int givenCount = maxStackSize - fuelStack.getAmount();
                                        fuelStack.setAmount(maxStackSize);
                                        itemOnCursor.setAmount(itemOnCursor.getAmount() - givenCount);
                                    } else {
                                        fuelStack.setAmount(fuelStack.getAmount() + canGiveMaxCount);
                                        itemOnCursor.setAmount(0);
                                    }
                                    player.updateInventory();
                                }
                            } else {
                                // swap item
                                event.setCursor(fuelStack);
                                furnaceInventory.setFuel(itemOnCursor.clone());
                                player.updateInventory();
                            }
                        } else {
                            if (isSimilar) {
                                int maxStackSize = fuelStack.getMaxStackSize();
                                if (maxStackSize > fuelStack.getAmount()) {
                                    fuelStack.setAmount(fuelStack.getAmount() + 1);
                                    itemOnCursor.setAmount(itemOnCursor.getAmount() - 1);
                                    player.updateInventory();
                                }
                            } else {
                                // swap item
                                event.setCursor(fuelStack);
                                furnaceInventory.setFuel(itemOnCursor.clone());
                                player.updateInventory();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();
        int fuelTime = this.itemManager.fuelTime(fuel);
        if (fuelTime != 0) {
            event.setBurnTime(fuelTime);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFurnaceInventoryOpen(InventoryOpenEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        if (!(event.getInventory() instanceof FurnaceInventory furnaceInventory)) {
            return;
        }
        Furnace furnace = furnaceInventory.getHolder();
        try {
            Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(furnace);
            BukkitInjector.injectCookingBlockEntity(blockEntity);
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to inject cooking block entity", e);
        }
    }

    // for 1.20.1-1.21.1
    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        if (VersionHelper.isVersionNewerThan1_21_2()) return;
        Block block = event.getBlock();
        Material material = block.getType();
        if (material == Material.CAMPFIRE) {
            if (block.getState() instanceof Campfire campfire) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(campfire);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    this.plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        Block block = event.getBlock();
        Material material = block.getType();
        if (material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER) {
            if (block.getState() instanceof Furnace furnace) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(furnace);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        } else if (!VersionHelper.isVersionNewerThan1_21_2() && material == Material.CAMPFIRE) {
            if (block.getState() instanceof Campfire campfire) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(campfire);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    this.plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        }
    }

    // for 1.21.2+
    @EventHandler(ignoreCancelled = true)
    public void onPutItemOnCampfire(PlayerInteractEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        if (!VersionHelper.isVersionNewerThan1_21_2()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        Material type = clicked.getType();
        if (type != Material.CAMPFIRE && type != Material.SOUL_CAMPFIRE) return;
        if (!VersionHelper.isVersionNewerThan1_21_2()) {
            if (clicked.getState() instanceof Campfire campfire) {
                try {
                    Object blockEntity = Reflections.field$CraftBlockEntityState$tileEntity.get(campfire);
                    BukkitInjector.injectCookingBlockEntity(blockEntity);
                } catch (Exception e) {
                    this.plugin.logger().warn("Failed to inject cooking block entity", e);
                }
            }
        }

        ItemStack itemStack = event.getItem();
        if (ItemUtils.isEmpty(itemStack)) return;
        try {
            @SuppressWarnings("unchecked")
            Optional<Object> optionalMCRecipe = (Optional<Object>) Reflections.method$RecipeManager$getRecipeFor1.invoke(
                    BukkitRecipeManager.minecraftRecipeManager(),
                    Reflections.instance$RecipeType$CAMPFIRE_COOKING,
                    Reflections.constructor$SingleRecipeInput.newInstance(Reflections.method$CraftItemStack$asNMSMirror.invoke(null, itemStack)),
                    Reflections.field$CraftWorld$ServerLevel.get(event.getPlayer().getWorld()),
                    null
            );
            if (optionalMCRecipe.isEmpty()) {
                return;
            }
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
            if (idHolder.isEmpty()) {
                return;
            }
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
            CustomCampfireRecipe<ItemStack> ceRecipe = (CustomCampfireRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.CAMPFIRE_COOKING, input);
            if (ceRecipe == null) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to handle interact campfire", e);
        }
    }

    // for 1.21.2+
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onCampfireCook(CampfireStartEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        if (!VersionHelper.isVersionNewerThan1_21_2()) return;
        CampfireRecipe recipe = event.getRecipe();
        Key recipeId = new Key(recipe.getKey().namespace(), recipe.getKey().value());

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        if (!isCustom) {
            return;
        }

        ItemStack itemStack = event.getSource();
        Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
        Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
        if (idHolder.isEmpty()) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
        CustomCampfireRecipe<ItemStack> ceRecipe = (CustomCampfireRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.CAMPFIRE_COOKING, input);
        if (ceRecipe == null) {
            event.setTotalCookTime(Integer.MAX_VALUE);
            return;
        }

        event.setTotalCookTime(ceRecipe.cookingTime());
    }

    // for 1.21.2+
    @EventHandler(ignoreCancelled = true)
    public void onCampfireCook(BlockCookEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        if (!VersionHelper.isVersionNewerThan1_21_2()) return;
        Material type = event.getBlock().getType();
        if (type != Material.CAMPFIRE && type != Material.SOUL_CAMPFIRE) return;
        CampfireRecipe recipe = (CampfireRecipe) event.getRecipe();
        if (recipe == null) return;

        Key recipeId = new Key(recipe.getKey().namespace(), recipe.getKey().value());

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        if (!isCustom) {
            return;
        }

        ItemStack itemStack = event.getSource();
        Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
        Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
        if (idHolder.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
        CustomCampfireRecipe<ItemStack> ceRecipe = (CustomCampfireRecipe<ItemStack>) this.recipeManager.getRecipe(RecipeTypes.CAMPFIRE_COOKING, input);
        if (ceRecipe == null) {
            event.setCancelled(true);
            return;
        }

        event.setResult(ceRecipe.result(ItemBuildContext.EMPTY));
    }

    // Paper only
    @EventHandler
    public void onPrepareResult(PrepareResultEvent event) {
//        if (!ConfigManager.enableRecipeSystem()) return;
        if (event.getInventory() instanceof CartographyInventory cartographyInventory) {
            if (ItemUtils.hasCustomItem(cartographyInventory.getStorageContents())) {
                event.setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAnvilCombineItems(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getFirstItem();
        ItemStack second = inventory.getSecondItem();
        if (first == null || second == null) return;
        Item<ItemStack> wrappedFirst = BukkitItemManager.instance().wrap(first);
        boolean firstCustom = wrappedFirst.isCustomItem();
        Item<ItemStack> wrappedSecond = BukkitItemManager.instance().wrap(second);
        boolean secondCustom = wrappedSecond.isCustomItem();
        // both are vanilla items
        if (!firstCustom && !secondCustom) {
            return;
        }

        // one of them is vanilla item
        if (!firstCustom || !secondCustom) {
            // block "vanilla + custom" recipes
            if (wrappedFirst.vanillaId().equals(wrappedSecond.vanillaId())) {
                event.setResult(null);
            }
            return;
        }

        // both of them are custom items
        // if the second is an enchanted book, then apply it
        if (wrappedSecond.vanillaId().equals(ItemKeys.ENCHANTED_BOOK)) {
            return;
        }

        // not the same item
        if (!wrappedFirst.customId().equals(wrappedSecond.customId())) {
            event.setResult(null);
            return;
        }

        // can not repair
        wrappedFirst.getCustomItem().ifPresent(it -> {
            if (!it.settings().canRepair()) {
                event.setResult(null);
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onAnvilRepairItems(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getFirstItem();
        ItemStack second = inventory.getSecondItem();
        if (first == null || second == null) return;

        Item<ItemStack> wrappedSecond = BukkitItemManager.instance().wrap(second);
        // if the second slot is not a custom item, ignore it
        Optional<CustomItem<ItemStack>> customItemOptional = plugin.itemManager().getCustomItem(wrappedSecond.id());
        if (customItemOptional.isEmpty()) {
            return;
        }

        CustomItem<ItemStack> customItem = customItemOptional.get();
        List<AnvilRepairItem> repairItems = customItem.settings().repairItems();
        // if the second slot is not a repair item, ignore it
        if (repairItems.isEmpty()) {
            return;
        }

        Item<ItemStack> wrappedFirst = BukkitItemManager.instance().wrap(first);
        int damage = wrappedFirst.damage().orElse(0);
        int maxDamage = wrappedFirst.maxDamage().orElse(0);
        // not a repairable item
        if (maxDamage == 0 || damage == 0) return;

        Key firstId = wrappedFirst.id();
        Optional<CustomItem<ItemStack>> optionalCustomTool = wrappedFirst.getCustomItem();
        // can not repair
        if (optionalCustomTool.isPresent() && !optionalCustomTool.get().settings().canRepair()) {
            return;
        }

        AnvilRepairItem repairItem = null;
        for (AnvilRepairItem item : repairItems) {
            for (String target : item.targets()) {
                if (target.charAt(0) == '#') {
                    Key tag = Key.of(target.substring(1));
                    if (optionalCustomTool.isPresent() && optionalCustomTool.get().is(tag)) {
                        repairItem = item;
                        break;
                    }
                    if (wrappedFirst.is(tag)) {
                        repairItem = item;
                        break;
                    }
                } else if (target.equals(firstId.toString())) {
                    repairItem = item;
                    break;
                }
            }
        }

        // no repair item matching
        if (repairItem == null) {
            return;
        }

        int realDurabilityPerItem = (int) (repairItem.amount() + repairItem.percent() * maxDamage);
        int consumeMaxAmount = damage / realDurabilityPerItem + 1;
        int actualConsumedAmount = Math.min(consumeMaxAmount, wrappedSecond.count());
        int actualRepairAmount = actualConsumedAmount * realDurabilityPerItem;
        int damageAfter = Math.max(damage - actualRepairAmount, 0);
        wrappedFirst.damage(damageAfter);
        event.setResult(wrappedFirst.loadCopy());
        if (VersionHelper.isVersionNewerThan1_21()) {
            AnvilView anvilView = event.getView();
            anvilView.setRepairCost(10);
            anvilView.setRepairItemCountCost(actualConsumedAmount);
        } else {
            LegacyInventoryUtils.setRepairCost(inventory, 10, actualRepairAmount);
        }
    }

    // only handle repair items for the moment
    @EventHandler(ignoreCancelled = true)
    public void onSpecialRecipe(PrepareItemCraftEvent event) {
//        if (!ConfigManager.enableRecipeSystem()) return;
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe == null)
            return;
        if (!(recipe instanceof ComplexRecipe complexRecipe))
            return;
        CraftingInventory inventory = event.getInventory();
        boolean hasCustomItem = ItemUtils.hasCustomItem(inventory.getMatrix());
        if (!hasCustomItem) {
            return;
        }

        if (!Reflections.clazz$CraftComplexRecipe.isInstance(complexRecipe)) {
            inventory.setResult(null);
            return;
        }

        try {
            Object mcRecipe = Reflections.field$CraftComplexRecipe$recipe.get(complexRecipe);
            if (!Reflections.clazz$RepairItemRecipe.isInstance(mcRecipe)) {
                inventory.setResult(null);
                return;
            }

            // repair item
            ItemStack[] itemStacks = inventory.getMatrix();
            Pair<ItemStack, ItemStack> onlyTwoItems = getTheOnlyTwoItem(itemStacks);
            if (onlyTwoItems.left() == null || onlyTwoItems.right() == null) {
                inventory.setResult(null);
                return;
            }

            Item<ItemStack> left = plugin.itemManager().wrap(onlyTwoItems.left());
            Item<ItemStack> right = plugin.itemManager().wrap(onlyTwoItems.right());
            if (!left.id().equals(right.id())) {
                inventory.setResult(null);
                return;
            }

            int totalDamage = right.damage().orElse(0) + left.damage().orElse(0);
            int totalMaxDamage = left.maxDamage().get() + right.maxDamage().get();
            // should be impossible, but take care
            if (totalDamage >= totalMaxDamage) {
                inventory.setResult(null);
                return;
            }

            Player player;
            try {
                player = (Player) Reflections.method$InventoryView$getPlayer.invoke(event.getView());
            } catch (ReflectiveOperationException e) {
                plugin.logger().warn("Failed to get inventory viewer", e);
                return;
            }

            Optional<CustomItem<ItemStack>> customItemOptional = plugin.itemManager().getCustomItem(left.id());
            if (customItemOptional.isEmpty()) {
                inventory.setResult(null);
                return;
            }

            CustomItem<ItemStack> customItem = customItemOptional.get();
            if (!customItem.settings().canRepair()) {
                inventory.setResult(null);
                return;
            }

            Item<ItemStack> newItem = customItem.buildItem(ItemBuildContext.of(plugin.adapt(player), ContextHolder.EMPTY));
            int remainingDurability = totalMaxDamage - totalDamage;
            int newItemDamage = Math.max(0, newItem.maxDamage().get() - remainingDurability);
            newItem.damage(newItemDamage);
            inventory.setResult(newItem.load());
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to handle minecraft custom recipe", e);
        }
    }

    private Pair<ItemStack, ItemStack> getTheOnlyTwoItem(ItemStack[] matrix) {
        ItemStack first = null;
        ItemStack second = null;
        for (ItemStack itemStack : matrix) {
            if (itemStack == null) continue;
            if (first == null) {
                first = itemStack;
            } else if (second == null) {
                second = itemStack;
            }
        }
        return new Pair<>(first, second);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftingRecipe(PrepareItemCraftEvent event) {
        if (!ConfigManager.enableRecipeSystem()) return;
        org.bukkit.inventory.Recipe recipe = event.getRecipe();
        if (recipe == null)
            return;

        // we only handle shaped and shapeless recipes
        boolean shapeless = event.getRecipe() instanceof ShapelessRecipe;
        boolean shaped = event.getRecipe() instanceof ShapedRecipe;
        if (!shaped && !shapeless) return;

        CraftingRecipe craftingRecipe = (CraftingRecipe) recipe;
        Key recipeId = Key.of(craftingRecipe.getKey().namespace(), craftingRecipe.getKey().value());

        boolean isCustom = this.recipeManager.isCustomRecipe(recipeId);
        // Maybe it's recipe from other plugins, then we ignore it
        if (!isCustom) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] ingredients = inventory.getMatrix();

        List<OptimizedIDItem<ItemStack>> optimizedIDItems = new ArrayList<>();
        for (ItemStack itemStack : ingredients) {
            if (ItemUtils.isEmpty(itemStack)) {
                optimizedIDItems.add(EMPTY);
            } else {
                Item<ItemStack> wrappedItem = this.itemManager.wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    // an invalid item is used in recipe, we disallow it
                    inventory.setResult(null);
                    return;
                } else {
                    optimizedIDItems.add(new OptimizedIDItem<>(idHolder.get(), itemStack));
                }
            }
        }

        CraftingInput<ItemStack> input;
        if (ingredients.length == 9) {
            input = CraftingInput.of(3, 3, optimizedIDItems);
        } else if (ingredients.length == 4) {
            input = CraftingInput.of(2, 2, optimizedIDItems);
        } else {
            return;
        }

        Player player;
        try {
            player = (Player) Reflections.method$InventoryView$getPlayer.invoke(event.getView());
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to get inventory viewer", e);
            return;
        }

        BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
        Key lastRecipe = serverPlayer.lastUsedRecipe();

        Recipe<ItemStack> ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SHAPELESS, input, lastRecipe);
        if (ceRecipe != null) {
            inventory.setResult(ceRecipe.result(new ItemBuildContext(serverPlayer, ContextHolder.EMPTY)));
            serverPlayer.setLastUsedRecipe(ceRecipe.id());
            correctCraftingRecipeUsed(inventory, ceRecipe);
            return;
        }
        ceRecipe = this.recipeManager.getRecipe(RecipeTypes.SHAPED, input, lastRecipe);
        if (ceRecipe != null) {
            inventory.setResult(ceRecipe.result(new ItemBuildContext(serverPlayer, ContextHolder.EMPTY)));
            serverPlayer.setLastUsedRecipe(ceRecipe.id());
            correctCraftingRecipeUsed(inventory, ceRecipe);
            return;
        }
        // clear result if not met
        inventory.setResult(null);
    }

    private void correctCraftingRecipeUsed(CraftingInventory inventory, Recipe<ItemStack> recipe) {
        Object holderOrRecipe = recipeManager.getRecipeHolderByRecipe(recipe);
        if (holderOrRecipe == null) {
            // it's a vanilla recipe but not injected
            return;
        }
        try {
            Object resultInventory = Reflections.field$CraftInventoryCrafting$resultInventory.get(inventory);
            Reflections.field$ResultContainer$recipeUsed.set(resultInventory, holderOrRecipe);
        } catch (ReflectiveOperationException e) {
            plugin.logger().warn("Failed to correct used recipe", e);
        }
    }
}
