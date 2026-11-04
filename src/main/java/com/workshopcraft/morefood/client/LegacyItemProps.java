package com.workshopcraft.morefood.client;

//? if <26.1 {
/*import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/^* Item properties picking the pizza and peel model overrides before item model definitions existed. ^/
public final class LegacyItemProps {
    private static final String[] TOPPINGS = {"morefood:item_tomato_chopped", "morefood:item_onion_chopped", "morefood:item_garlic_chopped",
            "minecraft:red_mushroom", "minecraft:brown_mushroom", "morefood:item_bacon", "morefood:item_minced_meat_cooked", "morefood:item_sausage",
            "morefood:item_spinach", "morefood:item_kale", "morefood:item_peas", "morefood:item_leek_sliced"};
    private static final List<List<Integer>> COMBOS = new ArrayList<>();

    static {
        final int n = TOPPINGS.length;
        for (int a = 0; a < n; a++) COMBOS.add(List.of(a));
        for (int a = 0; a < n; a++) for (int b = a + 1; b < n; b++) COMBOS.add(List.of(a, b));
        for (int a = 0; a < n; a++) for (int b = a + 1; b < n; b++) for (int c = b + 1; c < n; c++) COMBOS.add(List.of(a, b, c));
    }

    public interface Registrar {
        void register(Item item, Identifier id, ClampedItemPropertyFunction function);
    }

    private LegacyItemProps() {
    }

    public static void register(Registrar registrar) {
        final ClampedItemPropertyFunction toppings = (stack, level, entity, seed) -> toppingsIndex(stack);
        for (String name : new String[]{"item_pizza_cooked", "item_pizza_uncooked"}) {
            registrar.register(item(MoreFood.id(name)), MoreFood.id("toppings"), toppings);
        }
        registrar.register(item(MoreFood.id("item_baking_peel")), MoreFood.id("loaded"),
                (stack, level, entity, seed) -> MoreFoodKitchen.peelContents(stack) != null ? 1 : 0);
    }

    private static Item item(Identifier id) {
        return BuiltInRegistries.ITEM.get(id);
    }

    private static float toppingsIndex(ItemStack stack) {
        final TreeSet<Integer> found = new TreeSet<>();
        for (Holder<Item> holder : MoreFoodKitchen.toppings(stack)) {
            final Identifier id = BuiltInRegistries.ITEM.getKey(holder.value());
            for (int i = 0; i < TOPPINGS.length; i++) {
                if (TOPPINGS[i].equals(id.toString())) found.add(i);
            }
        }
        if (found.isEmpty()) return 0;
        final List<Integer> key = new ArrayList<>(found).subList(0, Math.min(3, found.size()));
        return COMBOS.indexOf(key) + 1;
    }
}
*///?}
