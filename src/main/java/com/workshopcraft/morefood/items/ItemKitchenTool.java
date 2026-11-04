package com.workshopcraft.morefood.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=26.1 {
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
//?}

//knives, mincers and the like come back out of the crafting grid
public class ItemKitchenTool extends Item {
    public ItemKitchenTool(Properties properties) {
        super(properties.stacksTo(1));
    }

    //? if >=26.1 {
    //fabric
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return new ItemStackTemplate(this);
    }

    //neoforge
    public ItemStackTemplate getCraftingRemainder(ItemInstance stack) {
        return new ItemStackTemplate(this);
    }
    //?} else {
    /*//fabric
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return new ItemStack(this);
    }

    //forge and neoforge
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(this);
    }
    *///?}
}
