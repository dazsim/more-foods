package com.workshopcraft.morefood.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;

//stock gives back whatever it was served in, a glass bottle unless the stack says otherwise
public class ItemStock extends Item {
    public ItemStock(Properties properties) {
        super(properties);
    }

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
}
