package com.workshopcraft.morefood;

import com.workshopcraft.morefood.crafting.BakingRecipe;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import com.workshopcraft.morefood.stove.StoveMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;
import java.util.function.BiConsumer;

public class MoreFoodKitchen {
    public static final RecipeType<BakingRecipe> BAKING = new RecipeType<>() {
        @Override
        public String toString() {
            return "morefood:baking";
        }
    };
    public static final RecipeSerializer<BakingRecipe> BAKING_SERIALIZER = new RecipeSerializer<>(BakingRecipe.MAP_CODEC, BakingRecipe.STREAM_CODEC);
    public static final RecipeBookCategory BAKING_BOOK = new RecipeBookCategory();
    public static final TagKey<Item> VESSELS = TagKey.create(Registries.ITEM, MoreFood.id("vessels"));
    public static final MenuType<StoveMenu> STOVE_MENU = new MenuType<>(StoveMenu::new, FeatureFlags.VANILLA_SET);
    public static BlockEntityType<StoveBlockEntity> stoveEntity;

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> registry) {
        stoveEntity = new BlockEntityType<>(StoveBlockEntity::new, Set.of(MoreFoodBlocks.stove));
        registry.accept(MoreFood.id("stove"), stoveEntity);
    }

    public static void registerMenus(BiConsumer<Identifier, MenuType<?>> registry) {
        registry.accept(MoreFood.id("stove"), STOVE_MENU);
    }

    public static void registerRecipeTypes(BiConsumer<Identifier, RecipeType<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING);
    }

    public static void registerRecipeSerializers(BiConsumer<Identifier, RecipeSerializer<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_SERIALIZER);
    }

    public static void registerBookCategories(BiConsumer<Identifier, RecipeBookCategory> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_BOOK);
    }
}
