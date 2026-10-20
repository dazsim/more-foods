package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.crafting.BoilingRecipe;
import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class BoilingCategory extends AbstractRecipeCategory<RecipeHolder<BoilingRecipe>> {
    private final long bucket;

    public BoilingCategory(IGuiHelper gui, long bucket) {
        super(MoreFoodJei.BOILING.get(), Component.translatable("jei.morefood.boiling"), gui.createDrawableItemLike(MoreFoodItems.pot), 124, 58);
        this.bucket = bucket;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BoilingRecipe> holder, IFocusGroup focuses) {
        final BoilingRecipe recipe = holder.value();
        final List<Ingredient> ingredients = recipe.ingredients();
        for (int i = 0; i < BoilingRecipe.MAX_INGREDIENTS; i++) {
            final IRecipeSlotBuilder slot = builder.addInputSlot(1 + i % 3 * 18, 1 + i / 3 * 18).setStandardSlotBackground();
            if (i < ingredients.size()) slot.add(ingredients.get(i));
        }
        if (recipe.water() > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 58, 1).setFluidRenderer(bucket, false, 12, 34)
                    .add(Fluids.WATER, recipe.water() * bucket / 1000);
        }
        final List<ItemStack> vessels = new ArrayList<>();
        final List<ItemStack> results = new ArrayList<>();
        for (BoilingRecipe.Serving serving : recipe.servings()) {
            vessels.add(serving.vessel().map(ItemStack::new).orElse(null));
            results.add(serving.result().create().copyWithCount(recipe.portions()));
        }
        final IRecipeSlotBuilder out = builder.addOutputSlot(103, 1).setOutputSlotBackground().addItemStacks(results);
        if (vessels.stream().anyMatch(v -> v != null)) {
            final IRecipeSlotBuilder vessel = builder.addInputSlot(103, 21).setStandardSlotBackground()
                    .addIngredients(VanillaTypes.ITEM_STACK, vessels)
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.morefood.serve_with")));
            builder.createFocusLink(out, vessel);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<BoilingRecipe> holder, IFocusGroup focuses) {
        final BoilingRecipe recipe = holder.value();
        builder.addAnimatedRecipeArrowWidget(recipe.cookingTime()).setPosition(74, 1);
        builder.addText(Component.translatable("jei.morefood.portions", recipe.portions(), MoreFoodJei.seconds(recipe.cookingTime())), 124, 10)
                .setPosition(0, 39).setColor(0xFF808080);
        builder.addText(Component.translatable("jei.morefood.campfire", MoreFoodJei.seconds((int) (recipe.cookingTime() * CookingPotBlockEntity.SLOWDOWN))), 124, 10)
                .setPosition(0, 48).setColor(0xFF808080);
    }
}
