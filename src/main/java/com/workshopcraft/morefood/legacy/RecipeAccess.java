package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
//? if >=1.21 {
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
//?}

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/^* Stands in for the 26.1 server recipe access. ^/
public final class RecipeAccess {
    private final RecipeManager manager;

    private RecipeAccess(RecipeManager manager) {
        this.manager = manager;
    }

    public static RecipeAccess of(Level level) {
        return new RecipeAccess(level.getRecipeManager());
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> type, I input, Level level) {
        //? if <1.21 {
        /^return manager.getRecipeFor(type, input, level).map(RecipeHolder::of);
        ^///?} else {
        return manager.getRecipeFor(type, input, level);
        //?}
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        //? if <1.21 {
        /^final List<RecipeHolder<?>> out = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) out.add(RecipeHolder.of(recipe));
        return out;
        ^///?} else {
        return manager.getRecipes();
        //?}
    }

    public Optional<RecipeHolder<?>> byKey(Identifier id) {
        //? if <1.21 {
        /^return manager.byKey(id).map(RecipeHolder::of);
        ^///?} else {
        return manager.byKey(id);
        //?}
    }
}
*///?}
