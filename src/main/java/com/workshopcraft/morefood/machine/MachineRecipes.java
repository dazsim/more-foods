package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodMachines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
//?} else {
/*import net.minecraft.world.item.alchemy.PotionUtils;
*///?}

import java.util.ArrayList;
import java.util.List;

/** What the grinder, press and sink make. The grinder and press also do every pestle, mincer and juicer recipe. */
public final class MachineRecipes {
    private static List<MachineRecipe> grinder;
    private static List<MachineRecipe> press;
    private static List<MachineRecipe> sink;

    private MachineRecipes() {
    }

    public static List<MachineRecipe> grinder() {
        if (grinder == null) {
            final List<MachineRecipe> r = new ArrayList<>();
            r.add(recipe("grinding/sausage", 80, stack(mf("item_sausage_uncooked"), 4), in(Items.PORKCHOP), in(mf("item_intestines"))));
            r.add(recipe("grinding/sauce_apple", 80, stack(mf("item_sauce_apple"), 1), in(Items.APPLE), in(Items.SUGAR), in(Items.GLASS_BOTTLE)));
            r.add(recipe("grinding/sauce_tomato", 80, stack(mf("item_sauce_tomato"), 1), in(mf("item_fruit_tomato")), in(Items.SUGAR), in(Items.GLASS_BOTTLE)));
            r.add(recipe("grinding/flour", 60, stack(mf("item_flour"), 1), in(Items.WHEAT)));
            r.add(recipe("grinding/minced_meat", 60, stack(mf("item_minced_meat_uncooked"), 1), in(Items.BEEF)));
            r.add(recipe("grinding/minced_lamb", 60, stack(mf("item_minced_lamb_uncooked"), 1), in(Items.MUTTON)));
            r.add(recipe("grinding/potato_mashed", 60, stack(mf("item_potato_mashed"), 1), in(mf("item_potato_boiled"))));
            r.add(recipe("grinding/ground_soy", 60, stack(mf("item_ground_soy"), 1), in(mf("item_soybeans"))));
            r.add(recipe("grinding/chickpea_paste", 60, stack(mf("item_chickpea_paste"), 1), in(mf("item_chickpeas"))));
            r.add(recipe("grinding/peanut_butter", 60, stack(mf("item_peanut_butter"), 1), in(mf("item_peanuts"))));
            r.add(recipe("grinding/oat_flour", 60, stack(mf("item_oat_flour"), 1), in(mf("item_oats"))));
            grinder = List.copyOf(r);
        }
        return grinder;
    }

    public static List<MachineRecipe> press() {
        if (press == null) {
            final List<MachineRecipe> r = new ArrayList<>();
            r.add(recipe("pressing/soy_milk_bucket", 80, stack(mf("item_soy_milk"), 1), new ItemStack(Items.BUCKET),
                    in(mf("item_ground_soy")), in(Items.WATER_BUCKET), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/oat_milk_bucket", 80, stack(mf("item_oat_milk"), 1), new ItemStack(Items.BUCKET),
                    in(mf("item_oats")), in(Items.WATER_BUCKET), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/juice_apple", 60, stack(mf("item_juice_apple"), 1), in(Items.APPLE), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/juice_carrot", 60, stack(mf("item_juice_carrot"), 1), in(Items.CARROT), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/juice_tomato", 60, stack(mf("item_juice_tomato"), 1), in(mf("item_fruit_tomato")), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/soy_milk", 80, stack(mf("item_soy_milk"), 1), in(mf("item_ground_soy")), water()));
            r.add(recipe("pressing/oat_milk", 80, stack(mf("item_oat_milk"), 1), in(mf("item_oats")), water()));
            r.add(recipe("pressing/olive_oil", 80, stack(mf("item_olive_oil"), 1), in(mf("item_olive"), 2), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/peanut_oil", 80, stack(mf("item_vegetable_oil"), 1), in(mf("item_peanuts"), 2), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/avocado_oil", 80, stack(mf("item_vegetable_oil"), 1), in(mf("item_avocado")), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/coconut_milk", 80, stack(mf("item_coconut_milk"), 1), in(mf("item_coconut")), in(Items.GLASS_BOTTLE)));
            r.add(recipe("pressing/tofu", 100, stack(mf("item_tofu"), 1), new ItemStack(Items.GLASS_BOTTLE), in(mf("item_soy_milk"))));
            press = List.copyOf(r);
        }
        return press;
    }

    public static List<MachineRecipe> sink() {
        if (sink == null) {
            final List<MachineRecipe> r = new ArrayList<>();
            r.add(recipe("washing/seitan", 20, stack(mf("item_seitan"), 1), in(mf("item_dough"))));
            r.add(recipe("washing/water_bottle", 10, waterBottle(), in(Items.GLASS_BOTTLE)));
            r.add(recipe("washing/water_bucket", 10, new ItemStack(Items.WATER_BUCKET), in(Items.BUCKET)));
            sink = List.copyOf(r);
        }
        return sink;
    }

    public static MachineRecipe sinkRecipe(ItemStack stack) {
        for (MachineRecipe recipe : sink()) {
            if (recipe.inputs().get(0).matches(stack)) return recipe;
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

    private static Item mf(String name) {
        final Item item = MoreFoodMachines.item(name);
        if (item != null) return item;
        for (Item i : MoreFoodItems.ALL) {
            if (BuiltInRegistries.ITEM.getKey(i).equals(MoreFood.id(name))) return i;
        }
        throw new IllegalStateException("No More Foods item " + name);
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(item, count);
    }

    private static MachineRecipe.Input in(Item item) {
        return in(item, 1);
    }

    private static MachineRecipe.Input in(Item item, int count) {
        return new MachineRecipe.Input(s -> s.is(item), List.of(new ItemStack(item, count)), count);
    }

    private static MachineRecipe.Input water() {
        return new MachineRecipe.Input(MachineRecipes::isWaterBottle, List.of(waterBottle()), 1);
    }

    private static MachineRecipe recipe(String id, int time, ItemStack result, MachineRecipe.Input... inputs) {
        return recipe(id, time, result, ItemStack.EMPTY, inputs);
    }

    private static MachineRecipe recipe(String id, int time, ItemStack result, ItemStack byproduct, MachineRecipe.Input... inputs) {
        return new MachineRecipe(MoreFood.id(id), List.of(inputs), result, byproduct, time);
    }
}
