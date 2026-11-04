package com.workshopcraft.morefood;

//? if <26.1 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
*///?}
//? if >=1.20.5 && <26.1 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
*///?}
//? if <1.20.5 {
/*import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Items;
*///?}
//? if <1.21 {
/*import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.crafting.Ingredient;
*///?}

/** Small bridges for calls that changed between Minecraft versions. Mostly empty on 26.1. */
public final class Compat {
    //? if <26.1 {
    /*public static final Codec<Integer> RGB = Codec.STRING.comapFlatMap(s -> {
        if (!s.startsWith("#") || s.length() != 7) return DataResult.error(() -> "Not a colour: " + s);
        try {
            return DataResult.success(0xFF000000 | Integer.parseInt(s.substring(1), 16));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not a colour: " + s);
        }
    }, c -> String.format("#%06x", c & 0xFFFFFF));

    public static void overlay(Player player, Component message) {
        player.displayClientMessage(message, true);
    }

    public static void limit(ItemStack stack, int max) {
        if (stack.getCount() > max) stack.setCount(max);
    }

    public static @Nullable ItemStackTemplate remainder(Item item) {
        final Item left = item.getCraftingRemainingItem();
        return left != null ? new ItemStackTemplate(left) : null;
    }

    /^* Food that hands back a bowl or bottle once eaten, filled in as items register. ^/
    public static final Map<Item, Item> CONVERTS = new HashMap<>();
    public static final String VESSEL_TAG = "morefood:vessel";

    /^* What a stack leaves behind once used, like the 26.1 use remainder. ^/
    public static @Nullable ItemStackTemplate useRemainder(ItemStack stack) {
        //? if >=1.20.5 {
        final Item vessel = stack.get(MoreFoodKitchen.VESSEL);
        if (vessel != null) return new ItemStackTemplate(vessel);
        final FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null && food.usingConvertsTo().isPresent()) return ItemStackTemplate.fromNonEmptyStack(food.usingConvertsTo().get());
        //?} else {
        /^final CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(VESSEL_TAG, Tag.TAG_STRING)) {
            final Identifier id = Identifier.tryParse(tag.getString(VESSEL_TAG));
            if (id != null && BuiltInRegistries.ITEM.containsKey(id)) return new ItemStackTemplate(BuiltInRegistries.ITEM.get(id));
        }
        if (stack.getItem() instanceof BowlFoodItem) return new ItemStackTemplate(Items.BOWL);
        if (stack.is(Items.HONEY_BOTTLE)) return new ItemStackTemplate(Items.GLASS_BOTTLE);
        ^///?}
        final Item converts = CONVERTS.get(stack.getItem());
        return converts != null ? new ItemStackTemplate(converts) : null;
    }

    public static Fuel fuel(Level level) {
        return Fuel.INSTANCE;
    }

    public static final class Fuel {
        static final Fuel INSTANCE = new Fuel();

        public int burnDuration(ItemStack stack) {
            if (stack.isEmpty()) return 0;
            //? if forge {
            /^return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, net.minecraft.world.item.crafting.RecipeType.SMELTING);
            ^///?} else if neoforge {
            /^return stack.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING);
            ^///?} else {
            return net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
            //?}
        }

        public boolean isFuel(ItemStack stack) {
            return burnDuration(stack) > 0;
        }
    }

    // blocks took no id in their properties before 26.1; the real map colour is set after this
    public static net.minecraft.world.level.material.MapColor unnamed(net.minecraft.resources.Identifier id) {
        return net.minecraft.world.level.material.MapColor.NONE;
    }

    /^* What 26.1 does for a block entity when its block goes. ^/
    public static void removed(net.minecraft.world.level.block.state.BlockState state, Level level, net.minecraft.core.BlockPos pos,
                               net.minecraft.world.level.block.state.BlockState newState) {
        if (state.is(newState.getBlock())) return;
        final net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof com.workshopcraft.morefood.legacy.LegacyContainerBlockEntity c) c.preRemoveSideEffects(pos, state);
        else if (be instanceof com.workshopcraft.morefood.legacy.LegacyBlockEntity b) b.preRemoveSideEffects(pos, state);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    public static int lerpDiscrete(float delta, int start, int end) {
        final int range = end - start;
        return start + net.minecraft.util.Mth.floor(delta * (range - 1)) + (delta > 0 ? 1 : 0);
    }

    public static java.util.stream.Stream<Item> items(net.minecraft.world.item.crafting.Ingredient ingredient) {
        return java.util.Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).distinct();
    }
    *///?}

    //? if <1.21 {
    /*public static void consume(ItemStack stack, int count, @Nullable LivingEntity entity) {
        if (!(entity instanceof Player player) || !player.getAbilities().instabuild) stack.shrink(count);
    }

    public static final IngredientCodec INGREDIENT = new IngredientCodec();

    /^* Reads and writes ingredients through their JSON form, as 1.20.1 recipes do. ^/
    public static final class IngredientCodec implements Codec<Ingredient> {
        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            try {
                final JsonElement json = new Dynamic<>(ops, input).convert(JsonOps.INSTANCE).getValue();
                return DataResult.success(Pair.of(Ingredient.fromJson(json, true), ops.empty()));
            } catch (JsonParseException | IllegalStateException e) {
                return DataResult.error(e::getMessage);
            }
        }

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, input.toJson()).convert(ops).getValue());
        }

        public Codec<List<Ingredient>> listOf(int min, int max) {
            return listOf().flatXmap(l -> l.size() < min || l.size() > max
                    ? DataResult.error(() -> "Expected " + min + " to " + max + " ingredients, got " + l.size()) : DataResult.success(l), DataResult::success);
        }
    }
    *///?}
    //? if >=26.3 {
    /*public static Fuel fuel(net.minecraft.world.level.Level level) {
        return Fuel.INSTANCE;
    }

    public static final class Fuel {
        static final Fuel INSTANCE = new Fuel();

        public boolean isFuel(net.minecraft.world.item.ItemStack stack) {
            return stack.has(net.minecraft.core.component.DataComponents.COOKING_FUEL);
        }
    }

    public static void giveBack(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack) {
        final net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        inventory.placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
    }
    *///?}
}
