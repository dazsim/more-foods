package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.crafting.AssemblyRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public class AssemblyCategory extends AbstractRecipeCategory<RecipeHolder<AssemblyRecipe>> {
    public AssemblyCategory(IGuiHelper gui) {
        super(MoreFoodJei.ASSEMBLY.get(), Component.translatable("jei.morefood.assembly"), gui.createDrawableItemLike(MoreFoodBlocks.assemblyBoard), 176, 38);
    }

    private static void layer(IRecipeLayoutBuilder builder, int at, Ingredient ingredient, Component note) {
        builder.addInputSlot(1 + at * 18, 5).setStandardSlotBackground().add(ingredient)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(note));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AssemblyRecipe> holder, IFocusGroup focuses) {
        final AssemblyRecipe recipe = holder.value();
        int at = 0;
        layer(builder, at++, recipe.base(), Component.translatable("jei.morefood.layer.base"));
        for (Ingredient filling : recipe.fillings()) {
            layer(builder, at++, filling, Component.translatable(recipe.ordered() ? "jei.morefood.layer.ordered" : "jei.morefood.layer.any"));
        }
        if (recipe.extras().isPresent()) {
            final AssemblyRecipe.Extras extras = recipe.extras().get();
            layer(builder, at++, extras.ingredient(), Component.translatable("jei.morefood.layer.extras", extras.max()));
        }
        if (recipe.lid().isPresent()) layer(builder, at, recipe.lid().get(), Component.translatable("jei.morefood.layer.lid"));
        builder.addOutputSlot(155, 5).setOutputSlotBackground().add(recipe.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<AssemblyRecipe> holder, IFocusGroup focuses) {
        final AssemblyRecipe recipe = holder.value();
        builder.addRecipeArrowWidget().setPosition(128, 5);
        final String key;
        if (recipe.extras().isPresent()) key = recipe.keepsExtras() ? "jei.morefood.board.toppings" : "jei.morefood.board.extras";
        else if (recipe.fillings().size() > 1 && !recipe.ordered()) key = "jei.morefood.board.any";
        else key = "jei.morefood.board.ordered";
        builder.addText(Component.translatable(key, recipe.extras().map(AssemblyRecipe.Extras::max).orElse(0)), 176, 10)
                .setPosition(0, 27).setColor(0xFF808080);
    }
}
