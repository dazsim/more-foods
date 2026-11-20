package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
//?} else {
/*import net.minecraft.world.item.alchemy.PotionUtils;
*///?}

import java.util.ArrayList;
import java.util.List;

/** Looks up datapack grinder, press and sink recipes, and recognises a water bottle. */
public final class MachineRecipes {
    private MachineRecipes() {
    }

    public static List<ProcessRecipe> of(Level level, RecipeType<ProcessRecipe> type) {
        final List<ProcessRecipe> out = new ArrayList<>();
        if (level == null) return out;
        for (RecipeHolder<ProcessRecipe> holder : MoreFoodKitchen.of(level, type)) out.add(holder.value());
        return out;
    }

    public static ProcessRecipe sinkRecipe(Level level, ItemStack stack) {
        if (level == null || stack.isEmpty()) return null;
        for (ProcessRecipe recipe : of(level, MoreFoodKitchen.WASHING)) {
            if (recipe.inputs().size() == 1 && recipe.inputs().get(0).test(stack)) return recipe;
        }
        return null;
    }

    public static ItemStack waterBottle() {
        //? if >=1.20.5 {
        return PotionContents.createItemStack(Items.POTION, Potions.WATER);
        //?} else {
        /*return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        *///?}
    }

    public static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        //? if >=1.20.5 {
        final PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        return potion != null && potion.is(Potions.WATER);
        //?} else {
        /*return PotionUtils.getPotion(stack) == Potions.WATER;
        *///?}
    }
}
