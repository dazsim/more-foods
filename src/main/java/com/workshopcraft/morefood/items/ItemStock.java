package com.workshopcraft.morefood.items;

//? if >=26.1 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.UseRemainder;
//?}
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

//stock gives back whatever it was served in, a glass bottle unless the stack says otherwise
public class ItemStock extends Item {
    public ItemStock(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    public static ItemStackTemplate vesselOf(ItemInstance stack) {
        final UseRemainder remainder = stack.get(DataComponents.USE_REMAINDER);
        return remainder != null ? remainder.convertInto() : new ItemStackTemplate(Items.GLASS_BOTTLE);
    }

    //fabric
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return vesselOf(stack);
    }

    //neoforge
    public ItemStackTemplate getCraftingRemainder(ItemInstance stack) {
        return vesselOf(stack);
    }
    //?} else {
    /*public static ItemStackTemplate vesselOf(ItemStack stack) {
        final ItemStackTemplate remainder = com.workshopcraft.morefood.Compat.useRemainder(stack);
        return remainder != null ? remainder : new ItemStackTemplate(Items.GLASS_BOTTLE);
    }

    //fabric
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return vesselOf(stack).create();
    }

    //forge and neoforge
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return vesselOf(stack).create();
    }
    *///?}
}
