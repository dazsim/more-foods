package com.workshopcraft.morefood.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** One item in, one item out, on a timer: the oven and the frying pan. */
public abstract class CookingCategory<R extends Recipe<?>> extends AbstractRecipeCategory<RecipeHolder<R>> {
    protected CookingCategory(IRecipeType<RecipeHolder<R>> type, Component title, IDrawable icon) {
        super(type, title, icon, 116, 42);
    }

    protected abstract Ingredient input(R recipe);

    protected abstract ItemStack output(R recipe);

    protected abstract int time(R recipe);

    protected Component note(R recipe) {
        return Component.empty();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> holder, IFocusGroup focuses) {
        final R recipe = holder.value();
        builder.addInputSlot(17, 5).setStandardSlotBackground().add(input(recipe));
        builder.addOutputSlot(77, 5).setOutputSlotBackground().add(output(recipe));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<R> holder, IFocusGroup focuses) {
        final R recipe = holder.value();
        builder.addAnimatedRecipeArrowWidget(time(recipe)).setPosition(42, 5);
        builder.addText(MoreFoodJei.seconds(time(recipe)), 116, 10).setPosition(0, 25).setColor(0xFF808080).setTextAlignment(HorizontalAlignment.CENTER);
        final Component note = note(recipe);
        if (!note.getString().isEmpty()) builder.addText(note, 116, 10).setPosition(0, 33).setColor(0xFF808080).setTextAlignment(HorizontalAlignment.CENTER);
    }
}
