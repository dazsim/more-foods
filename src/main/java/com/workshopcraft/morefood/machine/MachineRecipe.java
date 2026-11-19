package com.workshopcraft.morefood.machine;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/** A grinder, press or sink job: up to three inputs, a result and an optional byproduct. */
public record MachineRecipe(Identifier id, List<Input> inputs, ItemStack result, ItemStack byproduct, int time) {
    public record Input(Predicate<ItemStack> test, List<ItemStack> shown, int count) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && stack.getCount() >= count && test.test(stack);
        }
    }

    public boolean uses(ItemStack stack) {
        for (Input input : inputs) {
            if (input.test().test(stack)) return true;
        }
        return false;
    }

    /** The slot each input comes from, or null when the slots do not hold this recipe. */
    public int[] match(Container container, int first, int count) {
        final int[] slots = new int[inputs.size()];
        int used = 0;
        for (int i = 0; i < inputs.size(); i++) {
            slots[i] = -1;
            for (int s = first; s < first + count; s++) {
                if ((used & (1 << s)) == 0 && inputs.get(i).matches(container.getItem(s))) {
                    slots[i] = s;
                    used |= 1 << s;
                    break;
                }
            }
            if (slots[i] < 0) return null;
        }
        return slots;
    }
}
