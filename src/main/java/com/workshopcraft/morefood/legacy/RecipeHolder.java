package com.workshopcraft.morefood.legacy;

//? if <1.21 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

/^* Stands in for the recipe holder added in 1.20.2. ^/
public record RecipeHolder<T extends Recipe<?>>(Identifier id, T value) {
    public static <T extends Recipe<?>> RecipeHolder<T> of(T recipe) {
        return new RecipeHolder<>(recipe.getId(), recipe);
    }
}
*///?}
