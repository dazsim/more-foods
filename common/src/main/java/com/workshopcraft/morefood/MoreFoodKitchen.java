package com.workshopcraft.morefood;

import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import com.workshopcraft.morefood.crafting.AssemblyRecipe;
import com.workshopcraft.morefood.crafting.BakingRecipe;
import com.workshopcraft.morefood.crafting.BoilingRecipe;
import com.workshopcraft.morefood.crafting.FryingRecipe;
import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import com.workshopcraft.morefood.stove.StoveMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class MoreFoodKitchen {
    public static final RecipeType<BakingRecipe> BAKING = type("baking");
    public static final RecipeSerializer<BakingRecipe> BAKING_SERIALIZER = new RecipeSerializer<>(BakingRecipe.MAP_CODEC, BakingRecipe.STREAM_CODEC);
    public static final RecipeBookCategory BAKING_BOOK = new RecipeBookCategory();
    public static final RecipeType<FryingRecipe> FRYING = type("frying");
    public static final RecipeSerializer<FryingRecipe> FRYING_SERIALIZER = new RecipeSerializer<>(FryingRecipe.MAP_CODEC, FryingRecipe.STREAM_CODEC);
    public static final RecipeType<BoilingRecipe> BOILING = type("boiling");
    public static final RecipeSerializer<BoilingRecipe> BOILING_SERIALIZER = new RecipeSerializer<>(BoilingRecipe.MAP_CODEC, BoilingRecipe.STREAM_CODEC);
    public static final RecipeBookCategory HOB_BOOK = new RecipeBookCategory();
    public static final RecipeType<AssemblyRecipe> ASSEMBLY = type("assembly");
    public static final RecipeSerializer<AssemblyRecipe> ASSEMBLY_SERIALIZER = new RecipeSerializer<>(AssemblyRecipe.MAP_CODEC, AssemblyRecipe.STREAM_CODEC);
    public static final RecipeBookCategory BOARD_BOOK = new RecipeBookCategory();
    public static final DataComponentType<List<Holder<Item>>> TOPPINGS = DataComponentType.<List<Holder<Item>>>builder()
            .persistent(Item.CODEC.listOf().xmap(MoreFoodKitchen::capToppings, l -> l))
            .networkSynchronized(Item.STREAM_CODEC.apply(ByteBufCodecs.list()).map(MoreFoodKitchen::capToppings, l -> l))
            .build();
    public static final DataComponentType<ItemStackTemplate> PEEL_CONTENTS = DataComponentType.<ItemStackTemplate>builder()
            .persistent(ItemStackTemplate.CODEC)
            .networkSynchronized(ItemStackTemplate.STREAM_CODEC)
            .build();
    public static final TagKey<Block> BOARD_SUPPORTS = TagKey.create(Registries.BLOCK, MoreFood.id("assembly_board_supports"));
    public static final SimpleParticleType STEAM = new SimpleParticleType(false);
    public static final SoundEvent SIZZLE = SoundEvent.createVariableRangeEvent(MoreFood.id("block.stove.sizzle"));
    public static final SoundEvent BUBBLE = SoundEvent.createVariableRangeEvent(MoreFood.id("block.cooking_pot.bubble"));
    public static final TagKey<Item> VESSELS = TagKey.create(Registries.ITEM, MoreFood.id("vessels"));
    public static final MenuType<StoveMenu> STOVE_MENU = new MenuType<>(StoveMenu::new, FeatureFlags.VANILLA_SET);
    public static BlockEntityType<StoveBlockEntity> stoveEntity;
    public static BlockEntityType<CookingPotBlockEntity> cookingPotEntity;
    public static BlockEntityType<AssemblyBoardBlockEntity> boardEntity;

    private static <T extends Recipe<?>> RecipeType<T> type(String name) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return MoreFood.MODID + ":" + name;
            }
        };
    }

    public static List<RecipeHolder<BoilingRecipe>> boiling(ServerLevel level) {
        return all(level, BoilingRecipe.class);
    }

    public static List<RecipeHolder<AssemblyRecipe>> assembly(ServerLevel level) {
        return all(level, AssemblyRecipe.class);
    }

    private static <T extends Recipe<?>> List<RecipeHolder<T>> all(ServerLevel level, Class<T> type) {
        final List<RecipeHolder<T>> out = new ArrayList<>();
        for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
            if (type.isInstance(holder.value())) {
                @SuppressWarnings("unchecked") final RecipeHolder<T> cast = (RecipeHolder<T>) holder;
                out.add(cast);
            }
        }
        return out;
    }

    public static final int MAX_TOPPINGS = 3;

    // a pizza never carries more than 3 toppings, whatever a command or old save hands it
    private static List<Holder<Item>> capToppings(List<Holder<Item>> toppings) {
        return toppings.size() > MAX_TOPPINGS ? List.copyOf(toppings.subList(0, MAX_TOPPINGS)) : toppings;
    }

    public static void setToppings(ItemStack stack, List<Holder<Item>> toppings) {
        stack.set(TOPPINGS, List.copyOf(capToppings(toppings)));
    }

    /** A pizza fills 8 and 0.2 saturation, plus 1 and 0.1 for each topping. */
    public static FoodProperties pizzaFood(int toppings) {
        toppings = Math.min(toppings, MAX_TOPPINGS);
        return new FoodProperties.Builder().nutrition(8 + toppings).saturationModifier(0.2F + 0.1F * toppings).build();
    }

    /** Sets the food values of a baked pizza from its toppings. */
    public static void feedToppings(ItemStack stack) {
        final List<Holder<Item>> toppings = stack.get(TOPPINGS);
        if (toppings != null && !toppings.isEmpty() && stack.has(DataComponents.FOOD)) stack.set(DataComponents.FOOD, pizzaFood(toppings.size()));
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> registry) {
        stoveEntity = new BlockEntityType<>(StoveBlockEntity::new, Set.of(MoreFoodBlocks.stove));
        registry.accept(MoreFood.id("stove"), stoveEntity);
        cookingPotEntity = new BlockEntityType<>(CookingPotBlockEntity::new, Set.of(MoreFoodBlocks.cookingPot));
        registry.accept(MoreFood.id("cooking_pot"), cookingPotEntity);
        boardEntity = new BlockEntityType<>(AssemblyBoardBlockEntity::new, Set.of(MoreFoodBlocks.assemblyBoard));
        registry.accept(MoreFood.id("assembly_board"), boardEntity);
    }

    public static void registerComponents(BiConsumer<Identifier, DataComponentType<?>> registry) {
        registry.accept(MoreFood.id("toppings"), TOPPINGS);
        registry.accept(MoreFood.id("peel_contents"), PEEL_CONTENTS);
    }

    public static void registerMenus(BiConsumer<Identifier, MenuType<?>> registry) {
        registry.accept(MoreFood.id("stove"), STOVE_MENU);
    }

    public static void registerRecipeTypes(BiConsumer<Identifier, RecipeType<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING);
        registry.accept(MoreFood.id("frying"), FRYING);
        registry.accept(MoreFood.id("boiling"), BOILING);
        registry.accept(MoreFood.id("assembly"), ASSEMBLY);
    }

    public static void registerRecipeSerializers(BiConsumer<Identifier, RecipeSerializer<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_SERIALIZER);
        registry.accept(MoreFood.id("frying"), FRYING_SERIALIZER);
        registry.accept(MoreFood.id("boiling"), BOILING_SERIALIZER);
        registry.accept(MoreFood.id("assembly"), ASSEMBLY_SERIALIZER);
    }

    public static void registerBookCategories(BiConsumer<Identifier, RecipeBookCategory> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_BOOK);
        registry.accept(MoreFood.id("hob"), HOB_BOOK);
        registry.accept(MoreFood.id("board"), BOARD_BOOK);
    }

    public static void registerSounds(BiConsumer<Identifier, SoundEvent> registry) {
        registry.accept(SIZZLE.location(), SIZZLE);
        registry.accept(BUBBLE.location(), BUBBLE);
    }

    public static void registerParticles(BiConsumer<Identifier, ParticleType<?>> registry) {
        registry.accept(MoreFood.id("steam"), STEAM);
    }
}
