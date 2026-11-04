package com.workshopcraft.morefood.crafting;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class FryingRecipe extends AbstractCookingRecipe {
    public static final MapCodec<FryingRecipe> MAP_CODEC = cookingMapCodec(FryingRecipe::new, 100);
    public static final StreamCodec<RegistryFriendlyByteBuf, FryingRecipe> STREAM_CODEC = cookingStreamCodec(FryingRecipe::new);

    public FryingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result,
                        float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    public ItemStack resultStack() {
        return result().create();
    }

    @Override
    protected Item furnaceIcon() {
        return MoreFoodBlocks.stove.asItem();
    }

    @Override
    public RecipeSerializer<FryingRecipe> getSerializer() {
        return MoreFoodKitchen.FRYING_SERIALIZER;
    }

    @Override
    public RecipeType<FryingRecipe> getType() {
        return MoreFoodKitchen.FRYING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return MoreFoodKitchen.HOB_BOOK;
    }
}
