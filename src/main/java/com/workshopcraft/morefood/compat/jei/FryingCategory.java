package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.crafting.FryingRecipe;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class FryingCategory extends CookingCategory<FryingRecipe> {
    public FryingCategory(IGuiHelper gui) {
        super(MoreFoodJei.FRYING.get(), Component.translatable("jei.morefood.frying"), gui.createDrawableItemLike(MoreFoodItems.fryingPan));
    }

    @Override
    protected Ingredient input(FryingRecipe recipe) {
        return recipe.input();
    }

    @Override
    protected ItemStack output(FryingRecipe recipe) {
        return recipe.resultStack();
    }

    @Override
    protected int time(FryingRecipe recipe) {
        return recipe.cookingTime();
    }

    @Override
    protected Component note(FryingRecipe recipe) {
        return Component.translatable("jei.morefood.on_the_hob");
    }
}
