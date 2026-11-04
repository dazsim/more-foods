package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
//? if <1.21 {
/^import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
^///?} else {
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeInput;
//?}

/^* Fills in the older recipe methods from the 26.1 shaped ones. ^/
public interface LegacyRecipe<I extends RecipeInput> extends Recipe<I> {
    //? if <1.21 {
    /^Map<Recipe<?>, Identifier> IDS = Collections.synchronizedMap(new WeakHashMap<>());

    default void setId(Identifier id) {
        IDS.put(this, id);
    }

    @Override
    default Identifier getId() {
        final Identifier id = IDS.get(this);
        return id != null ? id : new Identifier("morefood", "unknown");
    }

    @Override
    default ItemStack assemble(I input, RegistryAccess registries) {
        return assemble(input);
    }

    @Override
    default ItemStack getResultItem(RegistryAccess registries) {
        return resultItem();
    }
    ^///?} else {
    @Override
    default ItemStack assemble(I input, HolderLookup.Provider registries) {
        return assemble(input);
    }

    @Override
    default ItemStack getResultItem(HolderLookup.Provider registries) {
        return resultItem();
    }
    //?}

    ItemStack assemble(I input);

    default ItemStack resultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    boolean matches(I input, Level level);

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    default String getGroup() {
        return group();
    }

    default String group() {
        return "";
    }

    default RecipeBookCategory recipeBookCategory() {
        return null;
    }

    default NonNullList<Ingredient> ingredientList() {
        return NonNullList.create();
    }

    @Override
    default NonNullList<Ingredient> getIngredients() {
        return ingredientList();
    }
}
*///?}
