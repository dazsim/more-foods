package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.morefood.client.ClientRecipes;
import com.workshopcraft.morefood.machine.CountedIngredient;
import com.workshopcraft.morefood.machine.ProcessRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Supplier;

/** Grinder, press and sink jobs: up to three inputs, a result and a byproduct. */
public class MachineCategory extends AbstractRecipeCategory<RecipeHolder<ProcessRecipe>> {
    public static final Supplier<IRecipeHolderType<ProcessRecipe>> GRINDING = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.GRINDING);
    public static final Supplier<IRecipeHolderType<ProcessRecipe>> PRESSING = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.PRESSING);
    public static final Supplier<IRecipeHolderType<ProcessRecipe>> WASHING = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.WASHING);

    private final String note;

    public MachineCategory(Supplier<IRecipeHolderType<ProcessRecipe>> type, String name, ItemLike icon, String note, IGuiHelper gui) {
        super(type.get(), Component.translatable("jei.morefood." + name), gui.createDrawableItemLike(icon), 124, 30);
        this.note = note;
    }

    public static void registerCategories(IRecipeCategoryRegistration registration) {
        final IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new MachineCategory(GRINDING, "grinding", MoreFoodMachines.grinder, "jei.morefood.cranked", gui),
                new MachineCategory(PRESSING, "pressing", MoreFoodMachines.press, "jei.morefood.cranked", gui),
                new MachineCategory(WASHING, "washing", MoreFoodMachines.sink, "jei.morefood.sink", gui));
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GRINDING.get(), ClientRecipes.of(MoreFoodKitchen.GRINDING));
        registration.addRecipes(PRESSING.get(), ClientRecipes.of(MoreFoodKitchen.PRESSING));
        registration.addRecipes(WASHING.get(), ClientRecipes.of(MoreFoodKitchen.WASHING));
    }

    public static void registerCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(GRINDING.get(), MoreFoodMachines.grinder, MoreFoodMachines.item("item_grinder_core"));
        registration.addCraftingStation(PRESSING.get(), MoreFoodMachines.press, MoreFoodMachines.item("item_press_core"));
        registration.addCraftingStation(WASHING.get(), MoreFoodMachines.sink);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ProcessRecipe> holder, IFocusGroup focuses) {
        final ProcessRecipe recipe = holder.value();
        final List<CountedIngredient> inputs = recipe.inputs();
        for (int i = 0; i < 3; i++) {
            final var slot = builder.addInputSlot(1 + i * 18, 1).setStandardSlotBackground();
            if (i < inputs.size()) slot.addItemStacks(inputs.get(i).shown());
        }
        builder.addOutputSlot(85, 1).setOutputSlotBackground().addItemStacks(List.of(recipe.result()));
        if (!recipe.byproduct().isEmpty()) builder.addOutputSlot(106, 1).setStandardSlotBackground().addItemStacks(List.of(recipe.byproduct()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<ProcessRecipe> holder, IFocusGroup focuses) {
        final ProcessRecipe recipe = holder.value();
        builder.addAnimatedRecipeArrowWidget(recipe.time()).setPosition(57, 1);
        builder.addText(Component.translatable(note, MoreFoodJei.seconds(recipe.time())), 124, 10).setPosition(0, 21).setColor(0xFF808080);
    }
}
