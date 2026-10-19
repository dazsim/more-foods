package com.workshopcraft.morefood.stove;

import net.minecraft.world.item.ItemStack;

/** Where cooked food ends up: the output slots of a stove or a campfire pot. */
public interface Kitchen {
    boolean fits(ItemStack... stacks);

    void output(ItemStack stack);
}
