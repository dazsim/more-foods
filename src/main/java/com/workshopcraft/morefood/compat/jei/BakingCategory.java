package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.crafting.BakingRecipe;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class BakingCategory extends CookingCategory<BakingRecipe> {
    public BakingCategory(IGuiHelper gui) {
        super(MoreFoodJei.BAKING.get(), Component.translatable("jei.morefood.baking"), gui.createDrawableItemLike(MoreFoodBlocks.stove));
    }

    @Override
    protected Ingredient input(BakingRecipe recipe) {
        return recipe.input();
    }

    @Override
    protected ItemStack output(BakingRecipe recipe) {
        return recipe.resultStack();
    }

    @Override
    protected int time(BakingRecipe recipe) {
        return recipe.cookingTime();
    }

    @Override
    protected Component note(BakingRecipe recipe) {
        return recipe.keepsComponents() ? Component.translatable("jei.morefood.keeps_toppings") : Component.empty();
    }
}
