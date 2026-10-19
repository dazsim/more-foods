package com.workshopcraft.morefood;

import com.workshopcraft.morefood.crafting.BakingRecipe;
import com.workshopcraft.morefood.crafting.BoilingRecipe;
import com.workshopcraft.morefood.crafting.FryingRecipe;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import com.workshopcraft.morefood.stove.StoveMenu;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
    public static final SimpleParticleType STEAM = new SimpleParticleType(false);
    public static final TagKey<Item> VESSELS = TagKey.create(Registries.ITEM, MoreFood.id("vessels"));
    public static final MenuType<StoveMenu> STOVE_MENU = new MenuType<>(StoveMenu::new, FeatureFlags.VANILLA_SET);
    public static BlockEntityType<StoveBlockEntity> stoveEntity;

    private static <T extends Recipe<?>> RecipeType<T> type(String name) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return MoreFood.MODID + ":" + name;
            }
        };
    }

    public static List<RecipeHolder<BoilingRecipe>> boiling(ServerLevel level) {
        final List<RecipeHolder<BoilingRecipe>> out = new ArrayList<>();
        for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
            if (holder.value() instanceof BoilingRecipe) {
                @SuppressWarnings("unchecked") final RecipeHolder<BoilingRecipe> boiling = (RecipeHolder<BoilingRecipe>) holder;
                out.add(boiling);
            }
        }
        return out;
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> registry) {
        stoveEntity = new BlockEntityType<>(StoveBlockEntity::new, Set.of(MoreFoodBlocks.stove));
        registry.accept(MoreFood.id("stove"), stoveEntity);
    }

    public static void registerMenus(BiConsumer<Identifier, MenuType<?>> registry) {
        registry.accept(MoreFood.id("stove"), STOVE_MENU);
    }

    public static void registerRecipeTypes(BiConsumer<Identifier, RecipeType<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING);
        registry.accept(MoreFood.id("frying"), FRYING);
        registry.accept(MoreFood.id("boiling"), BOILING);
    }

    public static void registerRecipeSerializers(BiConsumer<Identifier, RecipeSerializer<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_SERIALIZER);
        registry.accept(MoreFood.id("frying"), FRYING_SERIALIZER);
        registry.accept(MoreFood.id("boiling"), BOILING_SERIALIZER);
    }

    public static void registerBookCategories(BiConsumer<Identifier, RecipeBookCategory> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_BOOK);
        registry.accept(MoreFood.id("hob"), HOB_BOOK);
    }

    public static void registerParticles(BiConsumer<Identifier, ParticleType<?>> registry) {
        registry.accept(MoreFood.id("steam"), STEAM);
    }
}
