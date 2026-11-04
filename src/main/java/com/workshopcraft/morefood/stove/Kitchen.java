package com.workshopcraft.morefood.stove;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Where cooked food ends up: the output slots of a stove or a campfire pot. */
public interface Kitchen {
    boolean fits(ItemStack... stacks);

    void output(ItemStack stack);

    /** Merges a stack into the slots, filling partial stacks first. True when all of it went in. */
    static boolean insert(List<ItemStack> slots, ItemStack stack, int max) {
        for (int i = 0; i < slots.size() && !stack.isEmpty(); i++) {
            final ItemStack slot = slots.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                final int move = Math.min(stack.getCount(), Math.min(max, slot.getMaxStackSize()) - slot.getCount());
                if (move > 0) {
                    slot.grow(move);
                    stack.shrink(move);
                }
            }
        }
        for (int i = 0; i < slots.size() && !stack.isEmpty(); i++) {
            if (slots.get(i).isEmpty()) {
                slots.set(i, stack.copy());
                stack.setCount(0);
            }
        }
        return stack.isEmpty();
    }
}
