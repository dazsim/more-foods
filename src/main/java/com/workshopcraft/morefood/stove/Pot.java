package com.workshopcraft.morefood.stove;

import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.crafting.BoilingRecipe;
import com.workshopcraft.morefood.crafting.PotInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pot {
    public static final int CAPACITY = 1000;
    public static final int WATER_COLOUR = 0xFF3F76E4;

    private final List<ItemStack> items = new ArrayList<>();
    private int water;
    private int time;
    private boolean cooking;
    private int colour = WATER_COLOUR;
    private int portions;
    private List<BoilingRecipe.Serving> servings = List.of();
    private final List<Identifier> bits = new ArrayList<>();
    private @Nullable RecipeHolder<BoilingRecipe> matched;
    private boolean dirty = true;
    private boolean keepTime;

    public List<ItemStack> items() {
        return items;
    }

    public int water() {
        return water;
    }

    public int portions() {
        return portions;
    }

    public boolean isCooking() {
        return cooking;
    }

    public int colour() {
        return colour;
    }

    public List<Identifier> bits() {
        return bits;
    }

    /** Height of the liquid above the bottom of the stove, in pixels. */
    public float surface() {
        final float fill = portions > 0 ? Math.min(1, 0.35F + 0.15F * portions) : water / (float) CAPACITY;
        return 17.5F + 3.5F * fill;
    }

    public boolean isEmpty() {
        return items.isEmpty() && water == 0 && portions == 0;
    }

    /** Nothing in it but water, which gets tipped out when the pot is picked up. */
    public boolean canPickUp() {
        return items.isEmpty() && portions == 0;
    }

    public void clear() {
        items.clear();
        bits.clear();
        water = time = portions = 0;
        cooking = false;
        colour = WATER_COLOUR;
        servings = List.of();
        dirty = true;
    }

    public static boolean isIngredient(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (RecipeHolder<BoilingRecipe> holder : MoreFoodKitchen.boiling(level)) {
            if (holder.value().uses(stack)) return true;
        }
        return false;
    }

    public boolean accepts(ServerLevel level, ItemStack stack) {
        return portions == 0 && items.size() < BoilingRecipe.MAX_INGREDIENTS && isIngredient(level, stack);
    }

    public void add(ItemStack stack) {
        items.add(stack.copyWithCount(1));
        bits.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        changed();
    }

    public ItemStack takeLast() {
        if (portions > 0 || items.isEmpty()) return ItemStack.EMPTY;
        bits.removeLast();
        final ItemStack out = items.removeLast();
        changed();
        return out;
    }

    public int room() {
        return portions > 0 ? 0 : CAPACITY - water;
    }

    public void addWater(int mb) {
        water = Math.min(CAPACITY, water + mb);
        changed();
    }

    public void drain(int mb) {
        water = Math.max(0, water - mb);
        changed();
    }

    private void changed() {
        dirty = true;
    }

    public void setWater(int mb) {
        water = Math.max(0, Math.min(CAPACITY, mb));
        dirty = true;
    }

    public static int waterIn(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) return CAPACITY;
        final PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        return stack.is(Items.POTION) && potion != null && potion.is(Potions.WATER) ? 250 : 0;
    }

    /** What is left of a stack once it goes in, or empty when the pot will not take it. */
    public Optional<ItemStack> insert(ServerLevel level, ItemStack stack, boolean simulate) {
        final int mb = waterIn(stack);
        if (mb > 0) {
            if (room() < mb) return Optional.empty();
            if (!simulate) addWater(mb);
            return Optional.of(new ItemStack(stack.is(Items.WATER_BUCKET) ? Items.BUCKET : Items.GLASS_BOTTLE));
        }
        if (!accepts(level, stack)) return Optional.empty();
        if (!simulate) add(stack);
        return Optional.of(MoreFoodItems.remainder(stack));
    }

    /** A player using an item on the pot: serve into a vessel, or put the item in. */
    public boolean use(ServerLevel level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        final BoilingRecipe.Serving serving = servingFor(stack);
        if (serving != null) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, serve(serving)));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
            return true;
        }
        final int back = stack.is(Items.BUCKET) ? CAPACITY : stack.is(Items.GLASS_BOTTLE) ? 250 : 0;
        if (back > 0 && canPickUp() && water >= back) {
            drain(back);
            final ItemStack filled = back == CAPACITY ? new ItemStack(Items.WATER_BUCKET) : PotionContents.createItemStack(Items.POTION, Potions.WATER);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
            level.playSound(null, pos, back == CAPACITY ? SoundEvents.BUCKET_FILL : SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
            return true;
        }
        final int mb = waterIn(stack);
        final Optional<ItemStack> left = insert(level, stack, false);
        if (left.isEmpty()) return false;
        if (left.get().isEmpty()) stack.consume(1, player);
        else player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, left.get()));
        if (mb > 0) level.playSound(null, pos, mb == CAPACITY ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public BoilingRecipe.@Nullable Serving servingFor(ItemStack vessel) {
        if (portions <= 0) return null;
        for (BoilingRecipe.Serving serving : servings) {
            if (serving.accepts(vessel)) return serving;
        }
        return null;
    }

    public ItemStack serve(BoilingRecipe.Serving serving) {
        portions--;
        if (portions == 0) {
            servings = List.of();
            bits.clear();
            colour = WATER_COLOUR;
        }
        return serving.result().create();
    }

    private @Nullable RecipeHolder<BoilingRecipe> match(ServerLevel level) {
        if (dirty) {
            dirty = false;
            final RecipeHolder<BoilingRecipe> before = matched;
            matched = null;
            if (portions == 0 && (!items.isEmpty() || water > 0)) {
                final PotInput input = new PotInput(items, water);
                for (RecipeHolder<BoilingRecipe> holder : MoreFoodKitchen.boiling(level)) {
                    if (holder.value().matches(input, level)) {
                        matched = holder;
                        break;
                    }
                }
            }
            if (keepTime) keepTime = false;
            else if (matched == null || before == null || !matched.id().equals(before.id())) time = 0;
        }
        return matched;
    }

    public boolean wantsHeat(ServerLevel level) {
        return match(level) != null;
    }

    /** Returns true when something visible changed. */
    public boolean tick(ServerLevel level, boolean hot, float slowdown, Kitchen kitchen) {
        final RecipeHolder<BoilingRecipe> holder = match(level);
        final boolean nowCooking = holder != null && hot;
        boolean changed = nowCooking != cooking;
        cooking = nowCooking;
        if (holder == null) {
            time = 0;
            return changed;
        }
        final BoilingRecipe recipe = holder.value();
        if (cooking && colour != recipe.colour()) {
            colour = recipe.colour();
            changed = true;
        }
        if (!hot) return changed;
        if (time < recipe.cookingTime() * slowdown) {
            time++;
            return changed;
        }
        if (!recipe.needsVessel()) {
            final ItemStack result = recipe.result();
            final ItemStack[] all = new ItemStack[recipe.portions()];
            for (int i = 0; i < all.length; i++) all[i] = result.copy();
            if (!kitchen.fits(all)) return changed;
            for (ItemStack stack : all) kitchen.output(stack);
            bits.clear();
            colour = WATER_COLOUR;
        } else {
            portions = recipe.portions();
            servings = recipe.servings();
        }
        items.clear();
        water -= recipe.water();
        time = 0;
        cooking = false;
        dirty = true;
        return true;
    }

    public int progress(ServerLevel level, float slowdown) {
        final RecipeHolder<BoilingRecipe> holder = match(level);
        return holder == null ? 0 : (int) (time * 100 / (holder.value().cookingTime() * slowdown));
    }

    public void save(ValueOutput output) {
        output.store("items", ItemStack.CODEC.listOf(), items);
        output.putInt("water", water);
        output.putInt("time", time);
        output.putBoolean("cooking", cooking);
        output.putInt("colour", colour);
        output.putInt("portions", portions);
        output.store("servings", BoilingRecipe.Serving.CODEC.listOf(), servings);
        output.store("bits", Identifier.CODEC.listOf(), bits);
    }

    public void load(ValueInput input) {
        items.clear();
        items.addAll(input.read("items", ItemStack.CODEC.listOf()).orElse(List.of()));
        water = input.getIntOr("water", 0);
        time = input.getIntOr("time", 0);
        cooking = input.getBooleanOr("cooking", false);
        colour = input.getIntOr("colour", WATER_COLOUR);
        portions = input.getIntOr("portions", 0);
        servings = input.read("servings", BoilingRecipe.Serving.CODEC.listOf()).orElse(List.of());
        bits.clear();
        bits.addAll(input.read("bits", Identifier.CODEC.listOf()).orElse(List.of()));
        dirty = true;
        keepTime = true;
    }
}
