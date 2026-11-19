package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.morefood.machine.MachineRecipe;
import com.workshopcraft.morefood.machine.MachineRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.List;

/** Grinder, press and sink jobs: up to three inputs, a result and a byproduct. */
public class MachineCategory extends AbstractRecipeCategory<MachineRecipe> {
    public static final IRecipeType<MachineRecipe> GRINDING = IRecipeType.create(MoreFood.id("grinding"), MachineRecipe.class);
    public static final IRecipeType<MachineRecipe> PRESSING = IRecipeType.create(MoreFood.id("pressing"), MachineRecipe.class);
    public static final IRecipeType<MachineRecipe> WASHING = IRecipeType.create(MoreFood.id("washing"), MachineRecipe.class);

    private final String note;

    public MachineCategory(IRecipeType<MachineRecipe> type, String name, ItemLike icon, String note, IGuiHelper gui) {
        super(type, Component.translatable("jei.morefood." + name), gui.createDrawableItemLike(icon), 124, 30);
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
        registration.addRecipes(GRINDING, MachineRecipes.grinder());
        registration.addRecipes(PRESSING, MachineRecipes.press());
        registration.addRecipes(WASHING, MachineRecipes.sink());
    }

    public static void registerCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(GRINDING, MoreFoodMachines.grinder, MoreFoodMachines.item("item_grinder_core"));
        registration.addCraftingStation(PRESSING, MoreFoodMachines.press, MoreFoodMachines.item("item_press_core"));
        registration.addCraftingStation(WASHING, MoreFoodMachines.sink);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        final List<MachineRecipe.Input> inputs = recipe.inputs();
        for (int i = 0; i < 3; i++) {
            final var slot = builder.addInputSlot(1 + i * 18, 1).setStandardSlotBackground();
            if (i < inputs.size()) slot.addItemStacks(inputs.get(i).shown());
        }
        builder.addOutputSlot(85, 1).setOutputSlotBackground().addItemStacks(List.of(recipe.result()));
        if (!recipe.byproduct().isEmpty()) builder.addOutputSlot(106, 1).setStandardSlotBackground().addItemStacks(List.of(recipe.byproduct()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(recipe.time()).setPosition(57, 1);
        builder.addText(Component.translatable(note, MoreFoodJei.seconds(recipe.time())), 124, 10).setPosition(0, 21).setColor(0xFF808080);
    }
}
