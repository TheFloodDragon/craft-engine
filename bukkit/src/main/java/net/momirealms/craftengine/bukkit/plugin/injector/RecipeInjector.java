package net.momirealms.craftengine.bukkit.plugin.injector;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.ComponentTypes;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRecipeTypes;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistries;
import net.momirealms.craftengine.bukkit.util.ItemTags;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.data.FireworkExplosion;
import net.momirealms.craftengine.core.item.recipe.CustomCookingRecipe;
import net.momirealms.craftengine.core.item.recipe.RecipeType;
import net.momirealms.craftengine.core.item.recipe.UniqueIdItem;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.util.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class RecipeInjector {
    private static Class<?> clazz$InjectedCacheChecker;
    private static Class<?> clazz$InjectedArmorDyeRecipe;
    private static Class<?> clazz$InjectedRepairItemRecipe;
    private static Class<?> clazz$InjectedFireworkStarFadeRecipe;

    public static void init() {
        ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
        clazz$InjectedCacheChecker = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name("net.momirealms.craftengine.bukkit.entity.InjectedCacheChecker")
                .implement(CoreReflections.clazz$RecipeManager$CachedCheck)
                .implement(InjectedCacheCheck.class)
                .defineField("recipeType", Object.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("recipeType"))
                .intercept(FieldAccessor.ofField("recipeType"))
                .defineField("customRecipeType", RecipeType.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("customRecipeType"))
                .intercept(FieldAccessor.ofField("customRecipeType"))
                .defineField("lastRecipe", Object.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("lastRecipe"))
                .intercept(FieldAccessor.ofField("lastRecipe"))
                .defineField("lastCustomRecipe", Key.class, Visibility.PUBLIC)
                .method(ElementMatchers.named("lastCustomRecipe"))
                .intercept(FieldAccessor.ofField("lastCustomRecipe"))
                .method(ElementMatchers.named("getRecipeFor").or(ElementMatchers.named("a")))
                .intercept(MethodDelegation.to(
                        VersionHelper.isOrAbove1_21_2() ?
                        GetRecipeForMethodInterceptor1_21_2.INSTANCE :
                        (VersionHelper.isOrAbove1_21() ?
                        GetRecipeForMethodInterceptor1_21.INSTANCE :
                        VersionHelper.isOrAbove1_20_5() ?
                        GetRecipeForMethodInterceptor1_20_5.INSTANCE :
                        GetRecipeForMethodInterceptor1_20.INSTANCE)
                ))
                .make()
                .load(RecipeInjector.class.getClassLoader())
                .getLoaded();
        ElementMatcher.Junction<MethodDescription> matches = (VersionHelper.isOrAbove1_21() ?
                ElementMatchers.takesArguments(CoreReflections.clazz$CraftingInput, CoreReflections.clazz$Level) :
                ElementMatchers.takesArguments(CoreReflections.clazz$CraftingContainer, CoreReflections.clazz$Level)
        ).and(ElementMatchers.returns(boolean.class));
        ElementMatcher.Junction<MethodDescription> assemble = (
                VersionHelper.isOrAbove1_21() ?
                        ElementMatchers.takesArguments(CoreReflections.clazz$CraftingInput, CoreReflections.clazz$HolderLookup$Provider) :
                        VersionHelper.isOrAbove1_20_5() ?
                                ElementMatchers.takesArguments(CoreReflections.clazz$CraftingContainer, CoreReflections.clazz$HolderLookup$Provider) :
                                ElementMatchers.takesArguments(CoreReflections.clazz$CraftingContainer, CoreReflections.clazz$RegistryAccess)
        ).and(ElementMatchers.returns(CoreReflections.clazz$ItemStack));

        clazz$InjectedArmorDyeRecipe = byteBuddy
                .subclass(CoreReflections.clazz$ArmorDyeRecipe, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name("net.momirealms.craftengine.bukkit.item.recipe.ArmorDyeRecipe")
                .method(matches)
                .intercept(MethodDelegation.to(DyeMatchesInterceptor.INSTANCE))
                .method(assemble)
                .intercept(MethodDelegation.to(DyeAssembleInterceptor.INSTANCE))
                .make()
                .load(RecipeInjector.class.getClassLoader())
                .getLoaded();

        clazz$InjectedFireworkStarFadeRecipe = byteBuddy
                .subclass(CoreReflections.clazz$FireworkStarFadeRecipe)
                .name("net.momirealms.craftengine.bukkit.item.recipe.FireworkStarFadeRecipe")
                .method(matches)
                .intercept(MethodDelegation.to(FireworkStarFadeMatchesInterceptor.INSTANCE))
                .method(assemble)
                .intercept(MethodDelegation.to(FireworkStarFadeAssembleInterceptor.INSTANCE))
                .make()
                .load(RecipeInjector.class.getClassLoader())
                .getLoaded();

        clazz$InjectedRepairItemRecipe = byteBuddy
                .subclass(CoreReflections.clazz$RepairItemRecipe, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name("net.momirealms.craftengine.bukkit.item.recipe.RepairItemRecipe")
                // 只修改match逻辑，合并需要在事件里处理，否则无法应用变量
                .method(matches)
                .intercept(MethodDelegation.to(RepairMatchesInterceptor.INSTANCE))
                .make()
                .load(RecipeInjector.class.getClassLoader())
                .getLoaded();
    }

    public static Object createRepairItemRecipe(Key id) throws ReflectiveOperationException {
        return createSpecialRecipe(id, clazz$InjectedRepairItemRecipe);
    }

    public static Object createCustomDyeRecipe(Key id) throws ReflectiveOperationException {
        return createSpecialRecipe(id, clazz$InjectedArmorDyeRecipe);
    }

    public static Object createFireworkStarFadeRecipe(Key id) throws ReflectiveOperationException {
        return createSpecialRecipe(id, clazz$InjectedFireworkStarFadeRecipe);
    }

    @NotNull
    private static Object createSpecialRecipe(Key id, Class<?> clazz$InjectedRepairItemRecipe) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        if (VersionHelper.isOrAbove1_20_2()) {
            Constructor<?> constructor = ReflectionUtils.getConstructor(clazz$InjectedRepairItemRecipe, CoreReflections.clazz$CraftingBookCategory);
            return constructor.newInstance(CoreReflections.instance$CraftingBookCategory$MISC);
        } else {
            Constructor<?> constructor = ReflectionUtils.getConstructor(clazz$InjectedRepairItemRecipe, CoreReflections.clazz$ResourceLocation, CoreReflections.clazz$CraftingBookCategory);
            return constructor.newInstance(KeyUtils.toResourceLocation(id), CoreReflections.instance$CraftingBookCategory$MISC);
        }
    }

    public static void injectCookingBlockEntity(Object entity) throws ReflectiveOperationException {
        if (CoreReflections.clazz$AbstractFurnaceBlockEntity.isInstance(entity)) {
            Object quickCheck = CoreReflections.field$AbstractFurnaceBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            Object recipeType = FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$recipeType(entity);
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            if (recipeType == MRecipeTypes.SMELTING) {
                injectedChecker.customRecipeType(RecipeType.SMELTING);
                injectedChecker.recipeType(MRecipeTypes.SMELTING);
            } else if (recipeType == MRecipeTypes.BLASTING) {
                injectedChecker.customRecipeType(RecipeType.BLASTING);
                injectedChecker.recipeType(MRecipeTypes.BLASTING);
            } else if (recipeType == MRecipeTypes.SMOKING) {
                injectedChecker.customRecipeType(RecipeType.SMOKING);
                injectedChecker.recipeType(MRecipeTypes.SMOKING);
            } else {
                throw new IllegalStateException("RecipeType " + recipeType + " not supported");
            }
            CoreReflections.field$AbstractFurnaceBlockEntity$quickCheck.set(entity, injectedChecker);
        } else if (!VersionHelper.isOrAbove1_21_2() && CoreReflections.clazz$CampfireBlockEntity.isInstance(entity)) {
            Object quickCheck = CoreReflections.field$CampfireBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) ReflectionUtils.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            injectedChecker.customRecipeType(RecipeType.CAMPFIRE_COOKING);
            injectedChecker.recipeType(MRecipeTypes.CAMPFIRE_COOKING);
            CoreReflections.field$CampfireBlockEntity$quickCheck.set(entity, injectedChecker);
        }
    }

    private static final Function<Object, Integer> INGREDIENT_SIZE_GETTER =
            VersionHelper.isOrAbove1_21() ?
                    FastNMS.INSTANCE::method$CraftingInput$size :
                    FastNMS.INSTANCE::method$Container$getContainerSize;
    private static final BiFunction<Object, Integer, Object> INGREDIENT_GETTER =
            VersionHelper.isOrAbove1_21() ?
                    FastNMS.INSTANCE::method$CraftingInput$getItem :
                    FastNMS.INSTANCE::method$Container$getItem;

    private static final Function<Object, Boolean> REPAIR_INGREDIENT_COUNT_CHECKER =
            VersionHelper.isOrAbove1_21() ?
                    (input) -> FastNMS.INSTANCE.method$CraftingInput$ingredientCount(input) != 2 :
                    (container) -> false;

    public static class FireworkStarFadeMatchesInterceptor {
        public static final FireworkStarFadeMatchesInterceptor INSTANCE = new FireworkStarFadeMatchesInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            Object input = args[0];
            if (DYE_INGREDIENT_COUNT_CHECKER.apply(input)) {
                return false;
            }
            boolean hasDye = false;
            boolean hasFireworkStar = false;
            int size = INGREDIENT_SIZE_GETTER.apply(input);
            for (int i = 0; i < size; i++) {
                Object itemStack = INGREDIENT_GETTER.apply(input, i);
                if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    continue;
                }
                Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                if (isFireworkDye(wrapped)) {
                    hasDye = true;
                } else {
                    if (!wrapped.id().equals(ItemKeys.FIREWORK_STAR)) {
                        return false;
                    }
                    if (hasFireworkStar) {
                        return false;
                    }
                    hasFireworkStar = true;
                }
            }
            return hasDye && hasFireworkStar;
        }
    }

    public static class FireworkStarFadeAssembleInterceptor {
        public static final FireworkStarFadeAssembleInterceptor INSTANCE = new FireworkStarFadeAssembleInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            IntList colors = new IntArrayList();
            Item<ItemStack> starItem = null;
            Object input = args[0];
            int size = INGREDIENT_SIZE_GETTER.apply(input);
            for (int i = 0; i < size; i++) {
                Object itemStack = INGREDIENT_GETTER.apply(input, i);
                if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    continue;
                }
                Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                if (isFireworkDye(wrapped)) {
                    Color color = getFireworkColor(wrapped);
                    if (color == null) {
                        return CoreReflections.instance$ItemStack$EMPTY;
                    }
                    colors.add(color.color());
                } else if (wrapped.id().equals(ItemKeys.FIREWORK_STAR)) {
                    starItem = wrapped.copyWithCount(1);
                }
            }
            if (starItem == null || colors.isEmpty()) {
                return CoreReflections.instance$ItemStack$EMPTY;
            }
            FireworkExplosion explosion = starItem.fireworkExplosion().orElse(FireworkExplosion.DEFAULT);
            starItem.fireworkExplosion(explosion.withFadeColors(colors));
            return starItem.getLiteralObject();
        }
    }

    public static class RepairMatchesInterceptor {
        public static final RepairMatchesInterceptor INSTANCE = new RepairMatchesInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            Object input = args[0];
            if (REPAIR_INGREDIENT_COUNT_CHECKER.apply(input)) {
                return false;
            }
            return getItemsToCombine(input) != null;
        }
    }

    @Nullable
    private static Pair<Item<ItemStack>, Item<ItemStack>> getItemsToCombine(Object input) {
        Item<ItemStack> item1 = null;
        Item<ItemStack> item2 = null;
        int size = INGREDIENT_SIZE_GETTER.apply(input);
        for (int i = 0; i < size; i++) {
            Object itemStack = INGREDIENT_GETTER.apply(input, i);
            if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                continue;
            }
            Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
            if (item1 == null) {
                item1 = wrapped;
            } else {
                if (item2 != null) {
                    return null;
                }
                item2 = wrapped;
            }
        }
        if (item1 == null || item2 == null) {
            return null;
        }
        if (!canCombine(item1, item2)) {
            return null;
        }
        return new Pair<>(item1, item2);
    }

    private static boolean canCombine(Item<ItemStack> input1, Item<ItemStack> input2) {
        if (input1.count() != 1 || !isDamageableItem(input1)) return false;
        if (input2.count() != 1 || !isDamageableItem(input2)) return false;
        if (!input1.id().equals(input2.id())) return false;
        Optional<CustomItem<ItemStack>> customItem = input1.getCustomItem();
        return customItem.isEmpty() || customItem.get().settings().canRepair() != Tristate.FALSE;
    }

    private static boolean isDamageableItem(Item<ItemStack> item) {
        if (VersionHelper.isOrAbove1_20_5()) {
            return item.hasComponent(ComponentTypes.MAX_DAMAGE) && item.hasComponent(ComponentTypes.DAMAGE);
        } else {
            return FastNMS.INSTANCE.method$Item$canBeDepleted(FastNMS.INSTANCE.method$ItemStack$getItem(item.getLiteralObject()));
        }
    }

    private static final Function<Object, Boolean> DYE_INGREDIENT_COUNT_CHECKER =
            VersionHelper.isOrAbove1_21() ?
                    (input) -> FastNMS.INSTANCE.method$CraftingInput$ingredientCount(input) < 2 :
                    (container) -> false;

    public static class DyeMatchesInterceptor {
        public static final DyeMatchesInterceptor INSTANCE = new DyeMatchesInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            Object input = args[0];
            if (DYE_INGREDIENT_COUNT_CHECKER.apply(input)) {
                return false;
            }
            int size = INGREDIENT_SIZE_GETTER.apply(input);
            Item<ItemStack> itemToDye = null;
            boolean hasDye = false;
            for (int i = 0; i < size; i++) {
                Object itemStack = INGREDIENT_GETTER.apply(input, i);
                if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    continue;
                }
                Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                if (isDyeable(wrapped)) {
                    if (itemToDye != null) {
                        return false;
                    }
                    itemToDye = wrapped;
                } else {
                    if (!isArmorDye(wrapped)) {
                        return false;
                    }
                    hasDye = true;
                }
            }
            return hasDye && itemToDye != null;
        }
    }

    public static class DyeAssembleInterceptor {
        public static final DyeAssembleInterceptor INSTANCE = new DyeAssembleInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) {
            List<Color> colors = new ArrayList<>();
            Item<ItemStack> itemToDye = null;
            Object input = args[0];
            int size = INGREDIENT_SIZE_GETTER.apply(input);
            for (int i = 0; i < size; i++) {
                Object itemStack = INGREDIENT_GETTER.apply(input, i);
                if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    continue;
                }
                Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                if (isDyeable(wrapped)) {
                    itemToDye = wrapped.copyWithCount(1);
                } else {
                    Color dyeColor = getDyeColor(wrapped);
                    if (dyeColor != null) {
                        colors.add(dyeColor);
                    } else {
                        return CoreReflections.instance$ItemStack$EMPTY;
                    }
                }
            }
            if (itemToDye == null || itemToDye.isEmpty() || colors.isEmpty()) {
                return CoreReflections.instance$ItemStack$EMPTY;
            }
            return itemToDye.applyDyedColors(colors).getLiteralObject();
        }
    }

    @Nullable
    private static Color getDyeColor(final Item<ItemStack> dyeItem) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = dyeItem.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            return Optional.ofNullable(customItem.settings().dyeColor()).orElseGet(() -> getVanillaDyeColor(dyeItem));
        }
        return getVanillaDyeColor(dyeItem);
    }

    @Nullable
    private static Color getFireworkColor(final Item<ItemStack> dyeItem) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = dyeItem.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            return Optional.ofNullable(customItem.settings().fireworkColor()).orElseGet(() -> getVanillaFireworkColor(dyeItem));
        }
        return getVanillaFireworkColor(dyeItem);
    }

    private static final Predicate<Item<ItemStack>> IS_DYEABLE =
            VersionHelper.isOrAbove1_20_5() ?
                    (item -> item.is(ItemTags.DYEABLE)) :
                    (item -> {
                       Object itemLike = FastNMS.INSTANCE.method$ItemStack$getItem(item.getLiteralObject());
                       return CoreReflections.clazz$DyeableLeatherItem.isInstance(itemLike);
                    });

    private static boolean isDyeable(final Item<ItemStack> item) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = item.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            if (customItem.settings().dyeable() == Tristate.FALSE) {
                return false;
            }
            if (customItem.settings().dyeable() == Tristate.TRUE) {
                return true;
            }
        }
        return IS_DYEABLE.test(item);
    }

    @Nullable
    private static Color getVanillaDyeColor(final Item<ItemStack> item) {
        Object itemStack = item.getLiteralObject();
        Object dyeItem = FastNMS.INSTANCE.method$ItemStack$getItem(itemStack);
        if (!CoreReflections.clazz$DyeItem.isInstance(dyeItem)) return null;
        return Color.fromDecimal(FastNMS.INSTANCE.method$DyeColor$getTextureDiffuseColor(FastNMS.INSTANCE.method$DyeItem$getDyeColor(dyeItem)));
    }

    @Nullable
    private static Color getVanillaFireworkColor(final Item<ItemStack> item) {
        Object itemStack = item.getLiteralObject();
        Object dyeItem = FastNMS.INSTANCE.method$ItemStack$getItem(itemStack);
        if (!CoreReflections.clazz$DyeItem.isInstance(dyeItem)) return null;
        return Color.fromDecimal(FastNMS.INSTANCE.method$DyeColor$getFireworkColor(FastNMS.INSTANCE.method$DyeItem$getDyeColor(dyeItem)));
    }

    private static boolean isArmorDye(Item<ItemStack> dyeItem) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = dyeItem.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            return customItem.settings().dyeColor() != null || isVanillaDyeItem(dyeItem);
        }
        return isVanillaDyeItem(dyeItem);
    }

    private static boolean isFireworkDye(Item<ItemStack> dyeItem) {
        Optional<CustomItem<ItemStack>> optionalCustomItem = dyeItem.getCustomItem();
        if (optionalCustomItem.isPresent()) {
            CustomItem<ItemStack> customItem = optionalCustomItem.get();
            return customItem.settings().fireworkColor() != null || isVanillaDyeItem(dyeItem);
        }
        return isVanillaDyeItem(dyeItem);
    }

    private static boolean isVanillaDyeItem(Item<ItemStack> item) {
        return CoreReflections.clazz$DyeItem.isInstance(FastNMS.INSTANCE.method$ItemStack$getItem(item.getLiteralObject()));
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_20 {
        public static final GetRecipeForMethodInterceptor1_20 INSTANCE = new GetRecipeForMethodInterceptor1_20();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceLocation = injectedCacheCheck.lastRecipe();
            Optional<Pair<Object, Object>> optionalRecipe = FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceLocation);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            Pair<Object, Object> resourceLocationAndRecipe = optionalRecipe.get();
            Object rawRecipeResourceLocation = resourceLocationAndRecipe.getFirst();
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());
            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();

            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceLocation);
                return Optional.of(resourceLocationAndRecipe.getSecond());
            }

            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(
                    injectedCacheCheck.recipeType() == MRecipeTypes.CAMPFIRE_COOKING ?
                            FastNMS.INSTANCE.field$SimpleContainer$items(args[0]).getFirst() :
                            FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$getItem(args[0], 0)
            );

            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            if (ceRecipe == null) {
                return Optional.empty();
            }

            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(KeyUtils.toResourceLocation(ceRecipe.id()));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_20_5 {
        public static final GetRecipeForMethodInterceptor1_20_5 INSTANCE = new GetRecipeForMethodInterceptor1_20_5();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceLocation = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceLocation);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            Object rawRecipeHolder = optionalRecipe.get();
            Object rawRecipeResourceLocation = FastNMS.INSTANCE.field$RecipeHolder$id(rawRecipeHolder);
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());

            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(
                    injectedCacheCheck.recipeType() == MRecipeTypes.CAMPFIRE_COOKING ?
                            FastNMS.INSTANCE.field$SimpleContainer$items(args[0]).getFirst() :
                            FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$getItem(args[0], 0)
            );

            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceLocation);
                return optionalRecipe;
            }

            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            if (ceRecipe == null) {
                return Optional.empty();
            }

            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(KeyUtils.toResourceLocation(ceRecipe.id()));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_21 {
        public static final GetRecipeForMethodInterceptor1_21 INSTANCE = new GetRecipeForMethodInterceptor1_21();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceLocation = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceLocation);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            Object rawRecipeHolder = optionalRecipe.get();
            Object rawRecipeResourceLocation = FastNMS.INSTANCE.field$RecipeHolder$id(rawRecipeHolder);
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());

            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceLocation);
                return optionalRecipe;
            }

            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(FastNMS.INSTANCE.field$SingleRecipeInput$item(args[0]));
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            if (ceRecipe == null) {
                return Optional.empty();
            }

            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(KeyUtils.toResourceLocation(ceRecipe.id()));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class GetRecipeForMethodInterceptor1_21_2 {
        public static final GetRecipeForMethodInterceptor1_21_2 INSTANCE = new GetRecipeForMethodInterceptor1_21_2();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object lastRecipeResourceKey = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) FastNMS.INSTANCE.method$RecipeManager$getRecipeFor(BukkitRecipeManager.minecraftRecipeManager(), injectedCacheCheck.recipeType(), args[0], args[1], lastRecipeResourceKey);
            if (optionalRecipe.isEmpty()) {
                return Optional.empty();
            }

            // 获取配方的基础信息
            Object recipeHolder = optionalRecipe.get();
            Object rawRecipeResourceKey = FastNMS.INSTANCE.field$RecipeHolder$id(recipeHolder);
            Object rawRecipeResourceLocation = FastNMS.INSTANCE.field$ResourceKey$location(rawRecipeResourceKey);
            Key rawRecipeKey = Key.of(rawRecipeResourceLocation.toString());

            BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
            // 来自其他插件注册的自定义配方
            boolean isCustom = recipeManager.isCustomRecipe(rawRecipeKey);
            if (!isCustom) {
                injectedCacheCheck.lastRecipe(rawRecipeResourceKey);
                return optionalRecipe;
            }

            // 获取唯一内存地址id
            ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(FastNMS.INSTANCE.field$SingleRecipeInput$item(args[0]));
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
            SingleItemInput<ItemStack> input = new SingleItemInput<>(new UniqueIdItem<>(wrappedItem.recipeIngredientId(), wrappedItem));
            CustomCookingRecipe<ItemStack> ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(injectedCacheCheck.customRecipeType(), input, injectedCacheCheck.lastCustomRecipe());
            // 这个ce配方并不存在，那么应该返回空
            if (ceRecipe == null) {
                return Optional.empty();
            }

            // 记录上一次使用的配方(ce)
            injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
            // 更新上一次使用的配方(nms)
            if (!ceRecipe.id().equals(rawRecipeKey)) {
                injectedCacheCheck.lastRecipe(FastNMS.INSTANCE.method$ResourceKey$create(MRegistries.RECIPE, KeyUtils.toResourceLocation(ceRecipe.id())));
            }
            return Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe));
        }
    }
}
