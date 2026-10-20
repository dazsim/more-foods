package com.workshopcraft.morefood.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;

public class BakingRecipe extends AbstractCookingRecipe {
    public static final MapCodec<BakingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CookingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(BakingRecipe::input),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result()),
            Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(BakingRecipe::experience),
            Codec.INT.fieldOf("cookingtime").orElse(200).forGetter(BakingRecipe::cookingTime),
            Codec.BOOL.fieldOf("keep_components").orElse(false).forGetter(o -> o.keepComponents)
    ).apply(i, BakingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BakingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CookingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            Ingredient.CONTENTS_STREAM_CODEC, BakingRecipe::input,
            ItemStackTemplate.STREAM_CODEC, o -> o.result(),
            ByteBufCodecs.FLOAT, BakingRecipe::experience,
            ByteBufCodecs.INT, BakingRecipe::cookingTime,
            ByteBufCodecs.BOOL, o -> o.keepComponents,
            BakingRecipe::new);

    private final boolean keepComponents;

    public BakingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result,
                        float experience, int cookingTime, boolean keepComponents) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
        this.keepComponents = keepComponents;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        final ItemStack out = super.assemble(input);
        if (keepComponents) {
            out.applyComponents(input.item().getComponentsPatch());
            MoreFoodKitchen.feedToppings(out);
        }
        return out;
    }

    public boolean keepsComponents() {
        return keepComponents;
    }

    public ItemStack resultStack() {
        return result().create();
    }

    @Override
    protected Item furnaceIcon() {
        return MoreFoodBlocks.stove.asItem();
    }

    @Override
    public RecipeSerializer<BakingRecipe> getSerializer() {
        return MoreFoodKitchen.BAKING_SERIALIZER;
    }

    @Override
    public RecipeType<BakingRecipe> getType() {
        return MoreFoodKitchen.BAKING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return MoreFoodKitchen.BAKING_BOOK;
    }
}
