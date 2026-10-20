package com.workshopcraft.morefood;

import com.workshopcraft.morefood.items.ItemBakingPeel;
import com.workshopcraft.morefood.items.ItemKitchenTool;
import com.workshopcraft.morefood.items.ItemPot;
import com.workshopcraft.morefood.items.ItemStock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class MoreFoodItems {
    public static final List<Item> ALL = new ArrayList<>();
    public static Item pot;
    public static Item fryingPan;
    public static Item seedTomato;
    public static Item seedLettuce;
    public static Item seedOnion;
    public static final List<String> NEW_CROPS = List.of("cabbage", "spinach", "kale", "garlic", "leek", "peas", "haricot", "parsnip", "turnip", "swede");
    public static final Map<String, Item> SEEDS = new HashMap<>();
    //made by the stove now, kept so old stacks still finish, gone in 2.1
    public static final Set<String> RETIRED = Set.of("item_pot_water", "item_pot_salt", "item_pot_water_potato_peeled",
            "item_pot_water_potato_boiled", "item_stock_mix_uncooked", "item_egg_uncooked_frying_pan", "item_fried_egg_frying_pan",
            "item_mushroom_uncooked_frying_pan", "item_mushroom_fried_frying_pan", "item_french_toast_cooked_frying_pan",
            "item_yorkshire_pudding_pot", "item_bucket_cheese");
    private static final List<Item> HIDDEN = new ArrayList<>();

    public static void register(BiConsumer<Identifier, Item> registry) {
        food(registry, "item_fruit_tomato", 1, 0.1F, props());
        pot = add(registry, "item_pot", new ItemPot(withId("item_pot", props().stacksTo(1))));
        tool(registry, "item_knife");
        tool(registry, "item_mixing_bowl");
        tool(registry, "item_pestle_mortar");
        fryingPan = item(registry, "item_frying_pan", props().stacksTo(1));
        tool(registry, "item_juicer");
        tool(registry, "item_mincer");
        item(registry, "item_salt", props());
        item(registry, "item_flour", props());
        item(registry, "item_dough", props());
        item(registry, "item_suet", props());
        item(registry, "item_pot_water", props().stacksTo(1));
        item(registry, "item_pot_salt", props().stacksTo(1).craftRemainder(pot));
        item(registry, "item_onion_chopped", props());
        item(registry, "item_cheese_slice", props());
        item(registry, "item_tomato_chopped", props());
        item(registry, "item_sauce_tomato", props());
        item(registry, "item_minced_meat_uncooked", props());
        item(registry, "item_minced_meat_cooked", props());
        item(registry, "item_beef_patty_uncooked", props());
        item(registry, "item_beef_patty_cooked", props());
        item(registry, "item_dough_medium", props());
        item(registry, "item_dough_small", props());
        item(registry, "item_pizza_uncooked", props());
        item(registry, "item_french_toast_uncooked", props());
        item(registry, "item_french_toast_cooked_frying_pan", props().craftRemainder(fryingPan));
        food(registry, "item_sandwich_cheese", 6, 0.4F, props());
        food(registry, "item_sandwich_cheese_tomato", 6, 0.6F, props());
        food(registry, "item_sandwich_ham", 6, 0.4F, props());
        food(registry, "item_sandwich_beef", 6, 0.4F, props());
        food(registry, "item_sandwich_chicken", 6, 0.4F, props());
        food(registry, "item_french_toast_cooked", 7, 0.3F, props());
        food(registry, "item_pizza_cooked", 8, 0.2F, props());
        item(registry, "item_potato_peeled", props());
        item(registry, "item_potato_peeled_chipped", props());
        item(registry, "item_hashbrown_uncooked", props());
        add(registry, "item_stock", new ItemStock(withId("item_stock", props())));
        item(registry, "item_chicken_bone", props());
        item(registry, "item_stock_mix_uncooked", props());
        item(registry, "item_batter", props().craftRemainder(pot));
        item(registry, "item_fish_battered_uncooked", props());
        item(registry, "item_bacon_uncooked", props());
        item(registry, "item_egg_uncooked_frying_pan", props());
        item(registry, "item_fried_egg_frying_pan", props().craftRemainder(fryingPan));
        item(registry, "item_sausage_uncooked", props());
        item(registry, "item_mushroom_uncooked_frying_pan", props());
        item(registry, "item_mushroom_fried_frying_pan", props().craftRemainder(fryingPan));
        item(registry, "item_joint_beef", props());
        item(registry, "item_joint_pork", props());
        item(registry, "item_joint_bacon", props());
        item(registry, "item_intestines", props());
        item(registry, "item_yorkshire_pudding_pot", props().craftRemainder(pot));
        item(registry, "item_dumpling_uncooked", props());
        item(registry, "item_pot_water_potato_peeled", props());
        item(registry, "item_pot_water_potato_boiled", props().craftRemainder(pot));
        food(registry, "item_lettuce", 1, 0.6F, props());
        food(registry, "item_onion", 1, 0.1F, props());
        food(registry, "item_bread_slice", 1, 0.1F, props());
        food(registry, "item_toast", 1, 0.2F, props());
        food(registry, "item_bun", 1, 0.1F, props());
        food(registry, "item_bagette", 2, 0.2F, props());
        food(registry, "item_footlong", 9, 0.8F, props());
        food(registry, "item_beef_burger", 5, 0.5F, props());
        food(registry, "item_cheese_burger", 6, 0.6F, props());
        food(registry, "item_juice_apple", 2, 0.5F, props().usingConvertsTo(Items.GLASS_BOTTLE));
        food(registry, "item_juice_carrot", 2, 0.5F, props().usingConvertsTo(Items.GLASS_BOTTLE));
        food(registry, "item_juice_tomato", 2, 0.5F, props().usingConvertsTo(Items.GLASS_BOTTLE));
        food(registry, "item_stew_beef_dumplings", 10, 1F, props().usingConvertsTo(Items.BOWL));
        food(registry, "item_stew_chicken", 10, 1F, props().usingConvertsTo(Items.BOWL));
        food(registry, "item_stew_mutton", 10, 1F, props().usingConvertsTo(Items.BOWL));
        food(registry, "item_stew_pork", 10, 1F, props().usingConvertsTo(Items.BOWL));
        food(registry, "item_chips", 4, 0.1F, props());
        food(registry, "item_potato_roast", 4, 0.1F, props());
        food(registry, "item_potato_boiled", 2, 0.2F, props());
        food(registry, "item_potato_mashed", 2, 0.2F, props());
        food(registry, "item_butter", 3, 0.1F, props());
        food(registry, "item_hashbrown", 3, 0.1F, props());
        food(registry, "item_fish_battered", 6, 0.3F, props());
        food(registry, "item_fish_and_chips", 10, 1F, props());
        food(registry, "item_gravy", 1, 0.1F, props());
        food(registry, "item_roast_beef_dinner", 10, 1F, props());
        food(registry, "item_yorkshire_pudding", 1, 0.1F, props());
        food(registry, "item_sauce_apple", 1, 0.1F, props().craftRemainder(Items.GLASS_BOTTLE));
        food(registry, "item_roast_pork_dinner", 10, 1F, props());
        food(registry, "item_roast_mutton_dinner", 10, 1F, props());
        food(registry, "item_roast_rabbit_dinner", 10, 1F, props());
        food(registry, "item_bacon", 3, 0.1F, props());
        food(registry, "item_fried_egg", 2, 0.3F, props());
        food(registry, "item_sausage", 2, 0.2F, props());
        food(registry, "item_mushroom_fried", 1, 0.1F, props());
        food(registry, "item_breakfast_cooked", 10, 1F, props());
        food(registry, "item_chocolate", 3, 0.3F, props());
        food(registry, "item_dough_cookie", 5, 0.4F, props());
        food(registry, "item_cookie_double_chocolate", 6, 0.5F, props());
        food(registry, "item_sandwich_blt", 6, 0.4F, props());
        seedTomato = seed(registry, "item_seed_tomato", MoreFoodBlocks.cropTomato);
        seedLettuce = seed(registry, "item_seed_lettuce", MoreFoodBlocks.cropLettuce);
        seedOnion = seed(registry, "item_seed_onion", MoreFoodBlocks.cropOnion);
        item(registry, "item_bucket_cheese", props().craftRemainder(Items.BUCKET));
        food(registry, "item_cheese", 5, 0.4F, props());
        food(registry, "item_cabbage", 2, 0.3F, props());
        food(registry, "item_spinach", 1, 0.4F, props());
        food(registry, "item_kale", 1, 0.4F, props());
        item(registry, "item_garlic", props());
        food(registry, "item_leek", 1, 0.2F, props());
        food(registry, "item_peas", 1, 0.2F, props());
        item(registry, "item_haricot_beans", props());
        food(registry, "item_parsnip", 2, 0.2F, props());
        food(registry, "item_turnip", 2, 0.2F, props());
        food(registry, "item_swede", 2, 0.2F, props());
        for (String name : NEW_CROPS) {
            SEEDS.put(name, seed(registry, "item_seed_" + name, MoreFoodBlocks.CROPS.get(name)));
        }
        food(registry, "item_cabbage_shredded", 1, 0.2F, props());
        item(registry, "item_garlic_chopped", props());
        item(registry, "item_leek_sliced", props());
        item(registry, "item_minced_lamb_uncooked", props());
        item(registry, "item_pastry", props());
        item(registry, "item_spaghetti_uncooked", props());
        item(registry, "item_bubble_squeak_uncooked", props());
        item(registry, "item_roast_veg_uncooked", props());
        food(registry, "item_coleslaw", 3, 0.3F, props().usingConvertsTo(Items.BOWL));
        food(registry, "item_salad_garden", 4, 0.5F, props().usingConvertsTo(Items.BOWL));
        food(registry, "item_parsnip_roast", 4, 0.3F, props());
        food(registry, "item_baked_beans", 5, 0.5F, props().usingConvertsTo(Items.BOWL).craftRemainder(Items.BOWL));
        food(registry, "item_roast_chicken_dinner", 12, 1F, props());
        food(registry, "item_breakfast_full", 14, 1F, props());
        add(registry, "block_stove", new BlockItem(MoreFoodBlocks.stove, withId("block_stove", props().useBlockDescriptionPrefix())));
        food(registry, "item_turnip_roast", 4, 0.3F, props());
        food(registry, "item_roast_vegetables", 7, 0.6F, props());
        food(registry, "item_kale_crisps", 2, 0.3F, props());
        food(registry, "item_minced_lamb_cooked", 3, 0.3F, props());
        food(registry, "item_flatbread", 2, 0.2F, props());
        food(registry, "item_bubble_squeak", 5, 0.5F, props());
        food(registry, "item_leeks_fried", 2, 0.3F, props());
        bowl(registry, "item_soup_tomato", 6, 0.6F);
        bowl(registry, "item_soup_leek_potato", 7, 0.7F);
        bowl(registry, "item_soup_pea_ham", 7, 0.7F);
        bowl(registry, "item_soup_parsnip", 6, 0.6F);
        bowl(registry, "item_soup_kale_potato", 6, 0.6F);
        bowl(registry, "item_stew_vegetable", 8, 0.8F);
        bowl(registry, "item_stew_sausage_kale", 10, 1F);
        bowl(registry, "item_mushy_peas", 4, 0.4F);
        bowl(registry, "item_neeps", 4, 0.4F);
        food(registry, "item_spaghetti", 2, 0.2F, props());
        bowl(registry, "item_spaghetti_tomato", 7, 0.6F);
        bowl(registry, "item_spaghetti_bolognese", 10, 0.9F);
        item(registry, "item_filling_cottage", props());
        item(registry, "item_filling_shepherds", props());
        item(registry, "item_filling_chicken_leek", props());
        item(registry, "item_filling_vegetable", props());
        add(registry, "block_assembly_board", new BlockItem(MoreFoodBlocks.assemblyBoard, withId("block_assembly_board", props().useBlockDescriptionPrefix())));
        add(registry, "item_baking_peel", new ItemBakingPeel(withId("item_baking_peel", props().stacksTo(1))));
    }

    /** What an ingredient leaves behind when it is used up: the tool, the bucket, the stock's bottle. */
    public static ItemStack remainder(ItemStack stack) {
        if (stack.getItem() instanceof ItemKitchenTool) return stack.copyWithCount(1);
        if (stack.getItem() instanceof ItemStock) return ItemStock.vesselOf(stack).create();
        final ItemStackTemplate left = stack.getItem().getCraftingRemainder();
        return left != null ? left.create() : ItemStack.EMPTY;
    }

    private static Item bowl(BiConsumer<Identifier, Item> registry, String name, int amount, float saturation) {
        return food(registry, name, amount, saturation, props().usingConvertsTo(Items.BOWL));
    }

    private static Item.Properties props() {
        return new Item.Properties();
    }

    private static <T extends Item> T add(BiConsumer<Identifier, Item> registry, String name, T item) {
        registry.accept(MoreFood.id(name), item);
        ALL.add(item);
        if (RETIRED.contains(name)) HIDDEN.add(item);
        return item;
    }

    private static Item.Properties withId(String name, Item.Properties properties) {
        return properties.setId(ResourceKey.create(Registries.ITEM, MoreFood.id(name)));
    }

    private static Item item(BiConsumer<Identifier, Item> registry, String name, Item.Properties properties) {
        return add(registry, name, new Item(withId(name, properties)));
    }

    private static Item food(BiConsumer<Identifier, Item> registry, String name, int amount, float saturation, Item.Properties properties) {
        final FoodProperties food = new FoodProperties.Builder().nutrition(amount).saturationModifier(saturation).build();
        return add(registry, name, new Item(withId(name, properties.food(food))));
    }

    private static Item tool(BiConsumer<Identifier, Item> registry, String name) {
        return add(registry, name, new ItemKitchenTool(withId(name, props())));
    }

    private static Item seed(BiConsumer<Identifier, Item> registry, String name, Block crop) {
        return add(registry, name, new BlockItem(crop, withId(name, props().useItemDescriptionPrefix())));
    }

    public static void registerTabs(BiConsumer<Identifier, CreativeModeTab> registry) {
        registry.accept(MoreFood.id("tab"), CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.tabMoreFoods"))
                .icon(() -> new ItemStack(ALL.getFirst()))
                .displayItems((parameters, output) -> ALL.forEach(item -> {
                    if (!HIDDEN.contains(item)) output.accept(item);
                }))
                .build());
    }
}
