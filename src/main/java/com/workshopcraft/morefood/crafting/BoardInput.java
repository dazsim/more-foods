package com.workshopcraft.morefood.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record BoardInput(List<ItemStack> layers) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return layers.get(index);
    }

    @Override
    public int size() {
        return layers.size();
    }
}
