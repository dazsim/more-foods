package com.workshopcraft.morefood.client;

//? if >=26.1 {
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** The kitchen recipes the server sent this client, for recipe viewers. */
public final class ClientRecipes {
    private static Collection<RecipeHolder<?>> recipes = List.of();

    private ClientRecipes() {
    }

    public static void set(RecipeMap map) {
        set(map.values());
    }

    public static void set(Collection<RecipeHolder<?>> all) {
        recipes = List.copyOf(all);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> List<RecipeHolder<T>> of(RecipeType<T> type) {
        final List<RecipeHolder<T>> out = new ArrayList<>();
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value().getType() == type) out.add((RecipeHolder<T>) holder);
        }
        return out;
    }
}
//?}
