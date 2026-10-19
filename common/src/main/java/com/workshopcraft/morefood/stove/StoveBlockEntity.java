package com.workshopcraft.morefood.stove;

import com.mojang.serialization.Codec;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.crafting.BakingRecipe;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoveBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int FUEL = 0;
    public static final int OVEN = 1;
    public static final int VESSEL = 2;
    public static final int OUT_FIRST = 3;
    public static final int OUT_COUNT = 3;
    public static final int SIZE = OUT_FIRST + OUT_COUNT;

    private static final int[] NONE = {};
    private static final int[] BACK = {OVEN};
    private static final int[] SIDES = {FUEL, VESSEL};
    private static final int[] BOTTOM = {3, 4, 5, FUEL};
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int litTime;
    private int litDuration;
    private int ovenTime;
    private int ovenTotal;
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> used = new Reference2IntOpenHashMap<>();
    private final RecipeManager.CachedCheck<SingleRecipeInput, BakingRecipe> bakingCheck = RecipeManager.createCheck(MoreFoodKitchen.BAKING);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int id) {
            return switch (id) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> ovenTime;
                case 3 -> ovenTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int id, int value) {
            switch (id) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> ovenTime = value;
                case 3 -> ovenTotal = value;
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return StoveMenu.DATA_COUNT;
        }
    };

    public StoveBlockEntity(BlockPos pos, BlockState state) {
        super(MoreFoodKitchen.stoveEntity, pos, state);
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        final boolean wasLit = litTime > 0;
        boolean changed = false;
        if (litTime > 0) litTime--;

        final ItemStack input = items.get(OVEN);
        RecipeHolder<BakingRecipe> recipe = null;
        ItemStack result = ItemStack.EMPTY;
        if (!input.isEmpty()) {
            final SingleRecipeInput in = new SingleRecipeInput(input);
            recipe = bakingCheck.getRecipeFor(in, level).orElse(null);
            if (recipe != null) result = recipe.value().assemble(in);
        }
        final boolean ovenReady = recipe != null && !result.isEmpty() && fitsOutputs(result, remainderOf(input));

        if (ovenReady && litTime <= 0) {
            final ItemStack fuel = items.get(FUEL);
            final int burn = level.fuelValues().burnDuration(fuel);
            if (burn > 0) {
                litTime = litDuration = burn;
                final Item fuelItem = fuel.getItem();
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    final ItemStackTemplate left = fuelItem.getCraftingRemainder();
                    items.set(FUEL, left != null ? left.create() : ItemStack.EMPTY);
                }
                changed = true;
            }
        }

        if (ovenReady) {
            ovenTotal = recipe.value().cookingTime();
            if (litTime > 0) {
                if (++ovenTime >= ovenTotal) {
                    ovenTime = 0;
                    final ItemStack remainder = remainderOf(input);
                    input.shrink(1);
                    insertOutputs(result);
                    insertOutputs(remainder);
                    used.addTo(recipe.id(), 1);
                    changed = true;
                }
            } else if (ovenTime > 0) {
                ovenTime = Mth.clamp(ovenTime - 2, 0, ovenTotal);
            }
        } else {
            ovenTime = 0;
        }

        final boolean lit = litTime > 0;
        if (wasLit != lit) {
            state = state.setValue(StoveBlock.LIT, lit);
            level.setBlock(pos, state, 3);
            changed = true;
        }
        if (changed) setChanged(level, pos, state);
    }

    private static ItemStack remainderOf(ItemStack input) {
        final ItemStackTemplate left = input.getItem().getCraftingRemainder();
        return left != null ? left.create() : ItemStack.EMPTY;
    }

    private boolean fitsOutputs(ItemStack result, ItemStack remainder) {
        final List<ItemStack> copy = new ArrayList<>();
        for (int i = 0; i < OUT_COUNT; i++) copy.add(items.get(OUT_FIRST + i).copy());
        return insert(copy, result.copy()) && insert(copy, remainder.copy());
    }

    private void insertOutputs(ItemStack stack) {
        insert(items.subList(OUT_FIRST, OUT_FIRST + OUT_COUNT), stack);
    }

    private boolean insert(List<ItemStack> slots, ItemStack stack) {
        for (int i = 0; i < slots.size() && !stack.isEmpty(); i++) {
            final ItemStack slot = slots.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                final int move = Math.min(stack.getCount(), Math.min(getMaxStackSize(), slot.getMaxStackSize()) - slot.getCount());
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

    public int outputSignal() {
        float total = 0;
        for (int i = OUT_FIRST; i < SIZE; i++) {
            final ItemStack stack = items.get(i);
            if (!stack.isEmpty()) total += (float) stack.getCount() / getMaxStackSize(stack);
        }
        return Mth.lerpDiscrete(total / OUT_COUNT, 0, 15);
    }

    public boolean isFuel(ItemStack stack) {
        return level != null && level.fuelValues().isFuel(stack);
    }

    public boolean canBake(ItemStack stack) {
        return level instanceof ServerLevel server && bakingCheck.getRecipeFor(new SingleRecipeInput(stack), server).isPresent();
    }

    public static boolean isVessel(ItemStack stack) {
        return stack.is(MoreFoodKitchen.VESSELS);
    }

    public void awardExperience(ServerPlayer player) {
        final List<RecipeHolder<?>> recipes = popExperience(player.level(), player.position());
        player.awardRecipes(recipes);
        used.clear();
    }

    private List<RecipeHolder<?>> popExperience(ServerLevel level, Vec3 at) {
        final List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (var entry : used.reference2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(holder -> {
                recipes.add(holder);
                if (holder.value() instanceof BakingRecipe baking) {
                    final float xp = entry.getIntValue() * baking.experience();
                    int whole = Mth.floor(xp);
                    if (level.getRandom().nextFloat() < Mth.frac(xp)) whole++;
                    ExperienceOrb.award(level, at, whole);
                }
            });
        }
        return recipes;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            popExperience(server, Vec3.atCenterOf(pos));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        litTime = input.getIntOr("lit_time", 0);
        litDuration = input.getIntOr("lit_duration", 0);
        ovenTime = input.getIntOr("oven_time", 0);
        ovenTotal = input.getIntOr("oven_total", 0);
        used.clear();
        used.putAll(input.read("recipes_used", USED_CODEC).orElse(Map.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("lit_time", litTime);
        output.putInt("lit_duration", litDuration);
        output.putInt("oven_time", ovenTime);
        output.putInt("oven_total", ovenTotal);
        output.store("recipes_used", USED_CODEC, used);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        final boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(items.get(slot), stack);
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        if (slot == OVEN && !same) {
            ovenTime = 0;
            setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case FUEL -> isFuel(stack);
            case OVEN -> true;
            case VESSEL -> isVessel(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return BOTTOM;
        if (side == Direction.UP) return NONE;
        final Direction front = getBlockState().getValue(StoveBlock.FACING);
        if (side == front) return NONE;
        if (side == front.getOpposite()) return BACK;
        return SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side == null) return canPlaceItem(slot, stack);
        if (side.getAxis() == Direction.Axis.Y) return false;
        final Direction front = getBlockState().getValue(StoveBlock.FACING);
        if (side == front) return false;
        if (side == front.getOpposite()) return slot == OVEN && canBake(stack);
        if (slot == VESSEL) return isVessel(stack);
        return slot == FUEL && !isVessel(stack) && isFuel(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side != Direction.DOWN) return false;
        return slot >= OUT_FIRST || slot == FUEL && stack.is(Items.BUCKET);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.morefood.stove");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new StoveMenu(id, inventory, this, data);
    }
}
