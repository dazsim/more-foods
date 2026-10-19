package com.workshopcraft.morefood.stove;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.crafting.FryingRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class Pan {
    public static final int SPOTS = 4;
    private final ItemStack[] items = new ItemStack[SPOTS];
    private final int[] time = new int[SPOTS];
    private final boolean[] done = new boolean[SPOTS];

    public Pan() {
        clear();
    }

    public void clear() {
        for (int i = 0; i < SPOTS; i++) {
            items[i] = ItemStack.EMPTY;
            time[i] = 0;
            done[i] = false;
        }
    }

    public ItemStack get(int spot) {
        return items[spot];
    }

    public boolean isDone(int spot) {
        return done[spot];
    }

    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    private static Optional<RecipeHolder<FryingRecipe>> recipe(ServerLevel level, ItemStack stack) {
        return level.recipeAccess().getRecipeFor(MoreFoodKitchen.FRYING, new SingleRecipeInput(stack), level);
    }

    public static boolean canFry(ServerLevel level, ItemStack stack) {
        return !stack.isEmpty() && recipe(level, stack).isPresent();
    }

    public boolean accepts(ServerLevel level, ItemStack stack) {
        return freeSpot() >= 0 && canFry(level, stack);
    }

    private int freeSpot() {
        for (int i = 0; i < SPOTS; i++) {
            if (items[i].isEmpty()) return i;
        }
        return -1;
    }

    public void add(ItemStack stack) {
        final int spot = freeSpot();
        if (spot < 0) return;
        items[spot] = stack.copyWithCount(1);
        time[spot] = 0;
        done[spot] = false;
    }

    public ItemStack takeLast() {
        for (int i = SPOTS - 1; i >= 0; i--) {
            if (!items[i].isEmpty()) {
                final ItemStack out = items[i];
                items[i] = ItemStack.EMPTY;
                time[i] = 0;
                done[i] = false;
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean wantsHeat(ServerLevel level) {
        for (int i = 0; i < SPOTS; i++) {
            if (!items[i].isEmpty() && !done[i] && canFry(level, items[i])) return true;
        }
        return false;
    }

    /** Returns true when something visible changed. */
    public boolean tick(ServerLevel level, boolean hot, Kitchen kitchen) {
        boolean changed = false;
        for (int i = 0; i < SPOTS; i++) {
            if (items[i].isEmpty()) continue;
            if (done[i]) {
                if (kitchen.fits(items[i])) {
                    kitchen.output(items[i]);
                    items[i] = ItemStack.EMPTY;
                    done[i] = false;
                    changed = true;
                }
                continue;
            }
            final Optional<RecipeHolder<FryingRecipe>> recipe = recipe(level, items[i]);
            if (recipe.isEmpty() || !hot) continue;
            if (++time[i] >= recipe.get().value().cookingTime()) {
                final ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(items[i]));
                time[i] = 0;
                if (kitchen.fits(result)) {
                    kitchen.output(result);
                    items[i] = ItemStack.EMPTY;
                } else {
                    items[i] = result;
                    done[i] = true;
                }
                changed = true;
            }
        }
        return changed;
    }

    public int progress(ServerLevel level) {
        int best = 0;
        for (int i = 0; i < SPOTS; i++) {
            if (items[i].isEmpty() || done[i]) continue;
            final int at = i;
            best = Math.max(best, recipe(level, items[i]).map(r -> time[at] * 100 / r.value().cookingTime()).orElse(0));
        }
        return best;
    }

    public void save(ValueOutput output) {
        for (int i = 0; i < SPOTS; i++) {
            if (items[i].isEmpty()) continue;
            final ValueOutput spot = output.child("spot" + i);
            spot.store("item", ItemStack.CODEC, items[i]);
            spot.putInt("time", time[i]);
            spot.putBoolean("done", done[i]);
        }
    }

    public void load(ValueInput input) {
        clear();
        for (int i = 0; i < SPOTS; i++) {
            final int at = i;
            input.child("spot" + i).ifPresent(spot -> {
                items[at] = spot.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
                time[at] = spot.getIntOr("time", 0);
                done[at] = spot.getBooleanOr("done", false);
            });
        }
    }
}
