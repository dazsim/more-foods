package com.workshopcraft.morefood.client;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.List;

/** The kitchen recipes the server sent this client, for recipe viewers. */
public final class ClientRecipes {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    private ClientRecipes() {
    }

    public static void set(RecipeMap map) {
        recipes = map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe<?>> List<RecipeHolder<T>> of(RecipeType<T> type) {
        return List.copyOf((Collection) recipes.byType((RecipeType) type));
    }
}
