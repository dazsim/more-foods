package com.workshopcraft.morefood.compat.jei;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.ClientRecipes;
import com.workshopcraft.morefood.client.StoveScreen;
import com.workshopcraft.morefood.crafting.AssemblyRecipe;
import com.workshopcraft.morefood.crafting.BakingRecipe;
import com.workshopcraft.morefood.crafting.BoilingRecipe;
import com.workshopcraft.morefood.crafting.FryingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Supplier;

@JeiPlugin
public class MoreFoodJei implements IModPlugin {
    public static final Supplier<IRecipeHolderType<BakingRecipe>> BAKING = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.BAKING);
    public static final Supplier<IRecipeHolderType<FryingRecipe>> FRYING = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.FRYING);
    public static final Supplier<IRecipeHolderType<BoilingRecipe>> BOILING = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.BOILING);
    public static final Supplier<IRecipeHolderType<AssemblyRecipe>> ASSEMBLY = IRecipeHolderType.createDeferred(() -> MoreFoodKitchen.ASSEMBLY);

    static Component seconds(int ticks) {
        final float s = ticks / 20F;
        return Component.translatable("jei.morefood.seconds", s == (int) s ? String.valueOf((int) s) : String.valueOf(s));
    }

    @Override
    public Identifier getPluginUid() {
        return MoreFood.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        final IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BakingCategory(gui), new FryingCategory(gui),
                new BoilingCategory(gui, registration.getJeiHelpers().getPlatformFluidHelper().bucketVolume()), new AssemblyCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(BAKING.get(), ClientRecipes.of(MoreFoodKitchen.BAKING));
        registration.addRecipes(FRYING.get(), ClientRecipes.of(MoreFoodKitchen.FRYING));
        registration.addRecipes(BOILING.get(), ClientRecipes.of(MoreFoodKitchen.BOILING));
        registration.addRecipes(ASSEMBLY.get(), ClientRecipes.of(MoreFoodKitchen.ASSEMBLY));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(BAKING.get(), MoreFoodBlocks.stove);
        registration.addCraftingStation(FRYING.get(), MoreFoodBlocks.stove, MoreFoodItems.fryingPan);
        registration.addCraftingStation(BOILING.get(), MoreFoodBlocks.stove, MoreFoodItems.pot, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        registration.addCraftingStation(ASSEMBLY.get(), MoreFoodBlocks.assemblyBoard);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(StoveScreen.class, 80, 34, 24, 16, BAKING.get());
        registration.addRecipeClickArea(StoveScreen.class, 112, 22, 52, 9, FRYING.get(), BOILING.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        final List<ItemStack> retired = MoreFoodItems.hidden().stream().map(ItemStack::new).toList();
        if (!retired.isEmpty()) runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, retired);
    }
}
